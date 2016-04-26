package org.de.htwg.klara;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.de.htwg.klara.CustomClassLoader.FilterType;

public class TestImpl {
	public static void main(String[] args) throws Exception {
		CustomClassLoader x = new CustomClassLoader(true, FilterType.WHITELIST, new Pattern[] { Pattern.compile(".*htwg\\.klara.*") }, BytcodeInstructionPrinter.getClassVisitorForThis());
		Class<?> foo = x.loadClass("org.de.htwg.klara.Foo");
		Method main = foo.getMethod("main", (new String[0]).getClass());
		Object argArray[] = { new String[0] };
		main.invoke(null, argArray);
	}
}
