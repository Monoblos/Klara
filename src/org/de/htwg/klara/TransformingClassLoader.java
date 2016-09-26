package org.de.htwg.klara;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.de.htwg.klara.linespec.LineSpecification;
import org.de.htwg.klara.transformers.Transformer;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Class loader that can modify loaded classes using the {@link Transformer}.
 * @author mrs
 *
 */
public class TransformingClassLoader extends ClassLoader {
	public static enum FilterType {
		BLACKLIST,
		WHITELIST,
		ALL,
		NOTHING
	}
	
	private final boolean cache;
	private final FilterType filterType;
	private final Map<Pattern, LineSpecification> filter;
	private final Hashtable<String, Class<?>> cachedClasses = new Hashtable<>();
	private final List<Class<? extends TransformationEventListener>> transformers;
	private final LineSpecification defaultLineSpec;
	private final boolean debug;
	
	/**
	 * Create a new transforming class loader.
	 * @param cache			If loaded classes should be cached
	 * @param transformers	List of transformers to be used by the {@link Transformer}
	 * @param filterType	How filtering should be done
	 * @param filter		List of filters to use with the {@link LineSpecification} to use if it matches
	 * @param defaultLineSpec	Line specification if the filter did not specify any
	 * @param debug			Set to true to print bytecode of loaded classes
	 */
	public TransformingClassLoader(boolean cache, 
			List<Class<? extends TransformationEventListener>> transformers, 
			FilterType filterType, 
			Map<Pattern, LineSpecification> filter, 
			LineSpecification defaultLineSpec,
			boolean debug) {
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
		this.defaultLineSpec = defaultLineSpec;
		this.debug = debug;
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
			for(Pattern p : filter.keySet()) {
				if(p.matcher(name).matches())
					return false;
			}
			return true;
		case WHITELIST:
			// If any matches modify
			for(Pattern p : filter.keySet()) {
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
	
	/**
	 * Method for advanced searching for class files.
	 * Can be used to implement some actual logic of searching for classes.
	 * @param relativeFilePath	relative path of the class
	 * @return	A full qualified path to the class file. Invalid if file was not found.
	 */
	private String findClassFile(final Path relativeFilePath) {
		String searchedSubpath[] = { "." };
		for (String subpath : searchedSubpath) {
			Path root = Paths.get(subpath);
			Path combined = root.resolve(relativeFilePath);
			if (Files.exists(combined))
				return combined.toAbsolutePath().toString();
		}
		return relativeFilePath.toAbsolutePath().toString();
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		//Try to find the class file
		String filePath = name.replace('.', File.separatorChar) + ".class";
		InputStream stream = getResourceAsStream(filePath);
		if (stream == null) {
			//Not found by standard class loader.
			filePath = findClassFile(Paths.get(filePath));
			try {
				stream = new FileInputStream(filePath);
			} catch (IOException e) {
				throw new ClassNotFoundException("Unable to find class " + name);
			}
		}

		//Load the class
		byte[] classBytes = null;
		try {
			int size = stream.available();
			classBytes = new byte[size];
			try (DataInputStream in = new DataInputStream(stream)) {
				in.readFully(classBytes);
			}
		} catch (IOException | NullPointerException e ) {
			throw new ClassNotFoundException("Unable to find class " + name);
		} finally {
			try {
				stream.close();
			} catch (IOException consumed) { /*Dead anyway*/ }
		}
		
		//Get the line-filter that will be used for this class
		LineSpecification lineSpec = null;
		for(Pattern p : filter.keySet()) {
			if(p.matcher(name).matches())
				lineSpec = filter.get(p);
		}
		if (lineSpec == null) {
			lineSpec = defaultLineSpec;
		}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
			@Override
		    protected String getCommonSuperClass(final String type1, final String type2) {
				// Exactly the same as the original implementation, beside using this class loader instead of the default
				// Method is used when computing the frames, the default implementation will sometimes not be able to find classes
		        Class<?> c, d;
		        ClassLoader classLoader = TransformingClassLoader.this;
		        try {
		            c = Class.forName(type1.replace('/', '.'), false, classLoader);
		            d = Class.forName(type2.replace('/', '.'), false, classLoader);
		        } catch (Exception e) {
		            throw new RuntimeException(e.toString());
		        }
		        if (c.isAssignableFrom(d)) {
		            return type1;
		        }
		        if (d.isAssignableFrom(c)) {
		            return type2;
		        }
		        if (c.isInterface() || d.isInterface()) {
		            return "java/lang/Object";
		        } else {
		            do {
		                c = c.getSuperclass();
		            } while (!c.isAssignableFrom(d));
		            return c.getName().replace('.', '/');
		        }
		    }
		};
		Transformer trans;
		try {
			if (debug) {
				ClassVisitor printer = BytcodeInstructionPrinter.getClassVisitorForThis().getConstructor(Integer.TYPE, ClassVisitor.class).newInstance(Opcodes.ASM4, cw);
				trans = new Transformer(Opcodes.ASM4, printer);
			} else {
				trans = new Transformer(Opcodes.ASM4, cw);
			}
			for (Class<? extends TransformationEventListener> tel : transformers) {
				tel.getConstructor(Transformer.class).newInstance(trans);
			}
			trans.setLineScope(lineSpec);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// We exactly know the method and parameters, so this should never fail.
			System.err.println("Somthing somewhere went terribly wrong with the given observer.");
			e.printStackTrace();
			return null;
		}
		ClassReader cr = new ClassReader(classBytes);
		cr.accept(trans, 0);
		byte[] modifiedBytes = cw.toByteArray();
		
		Class<?> c = defineClass(name, modifiedBytes, 0, modifiedBytes.length);
		if (cache) {
			cachedClasses.put(name, c);
		}
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
