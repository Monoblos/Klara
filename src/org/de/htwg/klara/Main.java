package org.de.htwg.klara;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
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

public class Main {
	private static final String PROG_NAME = "Klara";
	private static final FilterType DEFAULT_FILTER = FilterType.NOTHING;

	public static void main(final String[] args) throws Exception {
		List<Class<? extends TransformationEventListener>> listeners = new LinkedList<>();
		Map<Pattern, LineSpecification> filter = new HashMap<>();
		FilterType filterType = DEFAULT_FILTER;
		LineSpecification generalLinespec = new LineSpecification();
		
		int i;
		for (i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-"))
				break;
			if (args[i].equalsIgnoreCase("-h")) {
				usage();
				System.exit(0);
			} else if (args[i].equals("-f")) {
				if (filterType != DEFAULT_FILTER && filterType != FilterType.WHITELIST) {
					System.out.println("Unable to use multiple kinds of filtering in one call.");
					System.exit(1);
				}
				filterType = FilterType.WHITELIST;
				Pattern p = Pattern.compile(args[++i]);
				LineSpecification filterLines = null;
				try {
					filterLines = new LineSpecification(args[i + 1]);
					i++;
				} catch (IllegalArgumentException consumed) { }
				filter.put(p, filterLines);
			} else if (args[i].equals("-F")) {
				if (filterType != DEFAULT_FILTER && filterType != FilterType.BLACKLIST) {
					System.out.println("Unable to use multiple kinds of filtering in one call.");
					System.exit(1);
				}
				filterType = FilterType.BLACKLIST;
				filter.put(Pattern.compile(args[++i]), null);
			} else if (args[i].equals("-l")) {
				generalLinespec = new LineSpecification(args[++i]);
			} else if (args[i].equals("-t")) {
				listeners.add(LineTracer.class);
			} else if (args[i].equals("-v")) {
				listeners.add(VariableChangePrinter.class);
			} else if (args[i].equals("-i")) {
				interactiveStart();
			} else {
				System.out.println("Invalid option " + args[i]);
				usage();
				System.exit(2);
			}
		}
		
		TransformingClassLoader tcl = new TransformingClassLoader(true,
				filterType,
				filter,
				listeners);
		if (i >= args.length) {
			System.out.println("No class to debug specified!");
			usage();
			System.exit(3);
		}
		Class<?> cls = tcl.loadClass(args[i++]);
		Method main = cls.getMethod("main", (new String[0]).getClass());
		Object argArray[] = { Arrays.copyOfRange(args, i, args.length) };
		main.invoke(null, argArray);
	}

	public static void usage() {
		System.out.println("This is the commandline interface of " + PROG_NAME + ".");
		System.out.println(PROG_NAME + " can be used to track bugs in Java programs.");
		System.out.println("");
		System.out.println("Argument specification:");
		System.out.println("java -jar Klara { -h | -H | { "
				+ "[ -f regex [ lines ] [ -f regex [ lines ]]... | -F regex [ -F regex]... ] "
				+ "[ -l lines ] [ -t ] [ -v ] } } "
				+ "progname [argument [ agrumen]...]");
		System.out.println("");
		System.out.println("Argument details:");
		System.out.println("  -h | -H");
		System.out.println("    Show this help message");
		System.out.println("  -f <regex> (<lines>)");
		System.out.println("    Specify a whitelist rule. Can be used multiple times for multiple entries. Can not be combined with -F."
				+ " Can optionaly specify a specific lineset to debug in matching classes, overriding those set by -l");
		System.out.println("  -F <regex>");
		System.out.println("    Specify a blacklist rule. Can be used multiple times for multiple entries. Can not be combined with -f");
		System.out.println("  -l <lines>");
		System.out.println("    Specify a line set to be loged. Use minus to specify a range, use comma or semicolon to seperate blocks.");
		System.out.println("  -t");
		System.out.println("    Trace the exact line order by printing every line run.");
		System.out.println("  -v");
		System.out.println("    Trace any variable assignment. Variables will be printed when declared and every time they are updated.");
		System.out.println("  -i");
		System.out.println("    Use interactive mode instead of detailed call.");
		System.out.println("... more to come");
		System.out.println("");
		System.out.println("Example call:");
		System.out.println("java -jar Klara -t -v -f my.cool.class 20-50 MyProg arg1 arg2");
		System.out.println("");
		System.out.println("");

		System.out.println("Press any key to exit.");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void interactiveStart() {
		System.out.println("Welcome to the interactive mode of " + PROG_NAME + ".");
		
		System.out.println("This feature is not yet available.");
	}
}
