package org.de.htwg.klara;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.de.htwg.klara.TransformingClassLoader.FilterType;
import org.de.htwg.klara.linespec.LineSpecification;
import org.de.htwg.klara.transformers.events.TransformationEventListener;

public final class Launcher {
	private Launcher () { }

	/**
	 * Start a program with the {@link TransformingClassLoader}
	 * @param listeners		The listeners argument to pass to the class loader
	 * @param filterType	The filterType argument to pass to the class loader
	 * @param filter		The filter argument to pass to the class loader
	 * @param generalLinespec	The defaultLineSpec argument to pass to the class loader
	 * @param classToLoad	The initial class to load and start. Needs a typical "main" method
	 * @param arguments		Arguments to pass to the stated class
	 * @param debug			The debug argument to pass to the class loader
	 * @throws Throwable
	 */
	static void start(List<Class<? extends TransformationEventListener>> listeners,
			FilterType filterType,
			Map<Pattern, LineSpecification> filter,
			LineSpecification generalLinespec,
			String classToLoad,
			String[] arguments,
			boolean debug) throws Throwable {
		if (filterType == Main.DEFAULT_FILTER) {
			filterType = FilterType.WHITELIST;
			filter.put(Pattern.compile(classToLoad.replace(".", "\\.")), null);
		}
		TransformingClassLoader tcl = new TransformingClassLoader(true,
				listeners,
				filterType,
				filter,
				generalLinespec,
				debug);
		Class<?> cls = tcl.loadClass(classToLoad);
		Method main = cls.getMethod("main", (new String[0]).getClass());
		Object[] argArray = { arguments };
		try {
			main.invoke(null, argArray);
		} catch (InvocationTargetException e) {
			//Cut out the Stacktrace created by wrapping the program
			Throwable cause = e.getCause();
			StackTraceElement[] trace = cause.getStackTrace();
			for (int i = trace.length - 1; i >= 0; i--) {
				if (trace[i].getClassName().equals(cls.getName()) && trace[i].getMethodName().equals("main")) {
					//Comment this out if you are having weird issues to get the original stacktrace
					cause.setStackTrace(Arrays.copyOfRange(trace, 0, i + 1));
					break;
				}
			}
			throw cause;
		}
	}

}
