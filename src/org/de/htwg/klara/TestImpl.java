package org.de.htwg.klara;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class TestImpl extends CustomClassLoader {
	
	public TestImpl() {
		super(true, FilterType.WHITELIST, new Pattern[] { Pattern.compile(".*htwg\\.klara.*") });
	}

	public static void main(String[] args) throws Exception {
		TestImpl x = new TestImpl();
		Class<?> foo = x.loadClass("org.de.htwg.klara.Foo");
		Method main = foo.getMethod("main", (new String[0]).getClass());
		Object argArray[] = { new String[0] };
		main.invoke(null, argArray);
	}

	@Override
	protected byte[] modifyResult(byte[] rawClass) {
		System.out.println("I can modify anything here! Yay!");
		return rawClass;
	}

}
