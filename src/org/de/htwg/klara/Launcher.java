package org.de.htwg.klara;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.de.htwg.klara.TransformingClassLoader.FilterType;
import org.de.htwg.klara.linespec.LineSpecification;
import org.de.htwg.klara.transformers.events.TransformationEventListener;

public final class Launcher {

	static void start(FilterType filterType,
			Map<Pattern, LineSpecification> filter,
			String classToLoad,
			List<Class<? extends TransformationEventListener>> listeners,
			LineSpecification generalLinespec,
			String[] arguments) throws Exception {
		if (filterType == Main.DEFAULT_FILTER) {
			filterType = FilterType.WHITELIST;
			filter.put(Pattern.compile(classToLoad.replace(".", "\\.")), null);
		}
		TransformingClassLoader tcl = new TransformingClassLoader(true,
				filterType,
				filter,
				listeners,
				generalLinespec);
		Class<?> cls = tcl.loadClass(classToLoad);
		Method main = cls.getMethod("main", (new String[0]).getClass());
		Object argArray[] = { arguments };
		try {
			main.invoke(null, argArray);
		} catch (InvocationTargetException e) {
			//Cut out the Stacktrace created by wrapping the program
			Exception cause = (Exception)e.getCause();
			StackTraceElement[] trace = cause.getStackTrace();
			for (int i = 0; i < trace.length; i++) {
				if (trace[i].getClassName().equals(cls.getName()) && trace[i].getMethodName().equals("main")) {
					//cause.setStackTrace(Arrays.copyOfRange(trace, 0, i + 1));
					break;
				}
			}
			throw cause;
		}
	}

}
