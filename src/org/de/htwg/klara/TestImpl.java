package org.de.htwg.klara;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.de.htwg.klara.TransformingClassLoader.FilterType;
import org.de.htwg.klara.linespec.LineSpecification;
import org.de.htwg.klara.transformers.LineTracer;
import org.de.htwg.klara.transformers.VariableChangePrinter;
import org.de.htwg.klara.transformers.events.TransformationEventListener;

public class TestImpl {
	public static void main(String[] args) throws Exception {
		List<Class<? extends TransformationEventListener>> listeners = new LinkedList<>();
		listeners.add(VariableChangePrinter.class);
		listeners.add(LineTracer.class);
		Map<Pattern, LineSpecification> filter = new HashMap<>();
		filter.put(Pattern.compile(".*htwg\\.klara.*"), null);
		TransformingClassLoader x = new TransformingClassLoader(true,
				FilterType.WHITELIST,
				filter,
				listeners);
		Class<?> foo = x.loadClass("org.de.htwg.klara.Foo");
		Method main = foo.getMethod("main", (new String[0]).getClass());
		Object argArray[] = { new String[0] };
		main.invoke(null, argArray);
	}
}
