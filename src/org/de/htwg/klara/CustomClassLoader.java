package org.de.htwg.klara;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.regex.Pattern;

public abstract class CustomClassLoader extends ClassLoader {
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
	
	public CustomClassLoader() {
		this(true, FilterType.NOTHING, null);
	}
	
	public CustomClassLoader(boolean cache, FilterType filterType, Pattern[] filter) {
		//Default init of a custom class loader
		super(CustomClassLoader.class.getClassLoader());
		
		this.cache = cache;
		this.filterType = filterType;
		this.filter = filter;
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
		boolean match;
		switch(filterType) {
		case BLACKLIST:
			//If any matches do not modify
			match = true;
			for(Pattern p : filter) {
				match &= p.matcher(name).matches();
			}
			return match;
		case WHITELIST:
			// If any matches modify
			match = false;
			for(Pattern p : filter) {
				match |= p.matcher(name).matches();
			}
			return match;
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
		
		classBytes = modifyResult(classBytes);
		
		Class<?> c = defineClass(name, classBytes, 0, classBytes.length);
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
	
	protected abstract byte[] modifyResult(byte[] rawClass);
}
