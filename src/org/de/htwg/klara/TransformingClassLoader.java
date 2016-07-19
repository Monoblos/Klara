package org.de.htwg.klara;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.de.htwg.klara.transformers.Transformer;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class TransformingClassLoader extends ClassLoader {
	public enum FilterType {
		BLACKLIST,
		WHITELIST,
		ALL,
		NOTHING
	}
	
	private boolean cache;
	private FilterType filterType;
	private Pattern[] filter;
	private Hashtable<String, Class<?>> cachedClasses = new Hashtable<>();
	private List<Class<? extends TransformationEventListener>> transformers;
	
	public TransformingClassLoader(boolean cache, FilterType filterType, Pattern[] filter, List<Class<? extends TransformationEventListener>> transformers) {
		//Default init of a custom class loader
		super(TransformingClassLoader.class.getClassLoader());
		
		try {
			for (Class<? extends TransformationEventListener> tel : transformers) {
				tel.getConstructor(Transformer.class);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid Transformation-event-listener. Make sure it has a (Transformer)-Constructor. If it is an inner class it needs to be static.", e);
		}
		
		this.cache = cache;
		this.filterType = filterType;
		this.filter = filter;
		this.transformers = transformers;
	}

	/**
	 * If caching is enabled, try to find the given class in the cache.
	 * @param name	The name of the class to search for.
	 * @return	The cached class if found and caching is enabled, null otherwise.
	 */
	private Class<?> findClassInCache(String name) {
		Class<?> result = null;
		if (cache) {
			result = cachedClasses.get(name);
		}
		return result;
	}
	
	/**
	 * Evaluates if the given class name with the set filter.
	 * @param name	The name of the class
	 * @return	Result of the filter or false if invalid filter.
	 */
	private boolean matchesFilter(String name) {
		switch(filterType) {
		case BLACKLIST:
			//If any matches do not modify
			for(Pattern p : filter) {
				if(p.matcher(name).matches())
					return false;
			}
			return true;
		case WHITELIST:
			// If any matches modify
			for(Pattern p : filter) {
				if (p.matcher(name).matches())
					return true;
			}
			return false;
		case ALL:
			return true;
		case NOTHING:
			return false;
		}
		return false;
	}
		
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String filePath = name.replace('.', File.separatorChar) + ".class";
		byte[] classBytes = null;
		try (InputStream stream = getResourceAsStream(filePath)) {
			int size = stream.available();
			classBytes = new byte[size];
			try (DataInputStream in = new DataInputStream(stream)) {
				in.readFully(classBytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException("Unable to find class " + name, e);
		}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassVisitor printer;
		Transformer trans;
		try {
			printer = BytcodeInstructionPrinter.getClassVisitorForThis().getConstructor(Integer.TYPE, ClassVisitor.class).newInstance(Opcodes.ASM4, cw);
			trans = new Transformer(Opcodes.ASM4, printer);
			trans.setAddLineInfo(true);
			for (Class<? extends TransformationEventListener> tel : transformers) {
				tel.getConstructor(Transformer.class).newInstance(trans);
			}
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// We exactly know the method and parameters, so this should never fail.
			System.out.println("Somthing somewhere went terribly wrong with the given visitor.");
			e.printStackTrace();
			return null;
		}
		ClassReader cr = new ClassReader(classBytes);
		cr.accept(trans, 0);
		byte[] modifiedBytes = cw.toByteArray();
		
		Class<?> c = defineClass(name, modifiedBytes, 0, modifiedBytes.length);
		cachedClasses.put(name, c);
		return c;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// Make sure we can not use a already cached version
		Class<?> result = findClassInCache(name);
		if (result != null)
			return result;
		
		// Evaluate if it matches with the given filter
		boolean modify = matchesFilter(name);
		if (!modify)
			return super.loadClass(name, resolve);
		
		Class<?> c = findClass(name);
		if (resolve)
			resolveClass(c);
		return c;
	}
	
	/**
	 * Reset the local class cache.
	 */
	public void clearCache() {
		cachedClasses.clear();
	}
}
