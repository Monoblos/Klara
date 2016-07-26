package org.de.htwg.klara;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
			} else if (args[i].equals("-b")) {
				System.out.println(args[i] + " is not yet implemented.");
			} else if (args[i].equals("-v")) {
				listeners.add(VariableChangePrinter.class);
			} else if (args[i].equals("-i")) {
				interactiveStart();
				System.exit(0);
			} else {
				System.out.println("Invalid option " + args[i]);
				usage();
				System.exit(2);
			}
		}

		if (i >= args.length) {
			System.out.println("No class to debug specified!");
			usage();
			System.exit(3);
		}
		String classToLoad = args[i++];
		String argArray[] = Arrays.copyOfRange(args, i, args.length);
		
		start(filterType, filter, classToLoad, listeners, generalLinespec, argArray);
	}

	public static void usage() {
		System.out.println("This is the commandline interface of " + PROG_NAME + ".");
		System.out.println(PROG_NAME + " can be used to track bugs in Java programs.");
		System.out.println("");
		System.out.println("Argument specification:");
		System.out.println("java -jar Klara.jar { -h | -H | { "
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
		System.out.println("  -b");
		System.out.println("    Trace the branching used. Will evaluate which if case was executed, at what switch-case block it jumped and how many times loops where executed.");
		System.out.println("  -v");
		System.out.println("    Trace any variable assignment. Variables will be printed when declared and every time they are updated.");
		System.out.println("  -i");
		System.out.println("    Use interactive mode instead of detailed call. Will ignore other arguments.");
		System.out.println("... more to come");
		System.out.println("");
		System.out.println("Example call:");
		System.out.println("java -jar Klara.jar -t -v -f my.cool.class.* 20-50 my.cool.pkg.MyClass arg1 arg2");
		System.out.println("");
		System.out.println("");

		System.out.println("Press Enter to exit.");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void interactiveStart() throws Exception {
		Scanner s = new Scanner(System.in);
		String choice = "";

		String classToLoad = "";
		List<String> arguments = new LinkedList<>();
		List<Class<? extends TransformationEventListener>> listeners = new LinkedList<>();
		Map<Pattern, LineSpecification> filter = new HashMap<>();
		FilterType filterType = DEFAULT_FILTER;
		LineSpecification generalLinespec = new LineSpecification();
		
		System.out.println("Welcome to the interactive mode of " + PROG_NAME + ".");
		
		while (classToLoad.equals("")) {
			System.out.println("What class do you want to debug?");
			classToLoad = s.nextLine();
		}
		if (ask("Do you want to pass any arguments?", s)) {
			System.out.println("Please specify one argument per line. Type \":exit\" to continue.");
			while (true) {
				choice = s.nextLine();
				if (choice.equalsIgnoreCase(":exit"))
					break;
				arguments.add(choice);
			}
		}

		while (!choice.equalsIgnoreCase("w") && !choice.equalsIgnoreCase("b") && !choice.equalsIgnoreCase("s")) {
			System.out.println("Do you want to Whitelist (w) or Blacklist (b) classes to debug? You can also skip (s) this.");
			choice = s.nextLine();
		}
		if (choice.equalsIgnoreCase("w")) {
			filterType = FilterType.WHITELIST;
			while (true) {
				System.out.println("Please provide a Pattern to whitelist or type exit to continue.");
				choice = s.nextLine();
				if (choice.equalsIgnoreCase("exit")) {
					break;
				}
				Pattern p;
				try {
					p = Pattern.compile(choice);
				} catch (PatternSyntaxException e) {
					System.out.println("Invalid pattern format. The Pattern must be a valid Regular Expression.");
					continue;
				}
				if (ask("Do you want to set a specific Lineset for classes matching this filter?", s)) {
					filter.put(p, readLineSpec(s));
				} else {
					filter.put(p, null);
				}
			}
		} else if (choice.equalsIgnoreCase("b")) {
			filterType = FilterType.BLACKLIST;
			while (true) {
				System.out.println("Please provide a Pattern to blacklist or type exit to continue.");
				choice = s.nextLine();
				if (choice.equalsIgnoreCase("exit")) {
					break;
				}
				try {
					filter.put(Pattern.compile(choice), null);
				} catch (PatternSyntaxException e) {
					System.out.println("Invalid pattern format. The Pattern must be a valid Regular Expression.");
					continue;
				}
			}
		}
		//General linespec
		if (ask("Do you want to set a general Lineset for all clases without a specific filter?", s)) {
			generalLinespec = readLineSpec(s);
		}
		//
		if (ask("Do you want to trace the exakt line order?", s)) {
			listeners.add(LineTracer.class);
		}
		if (ask("Do you want to get general branching information?", s)) {
			System.out.println("Not yet implemented.");
		}
		if (ask("Do you want to get information about every variable assignment?", s)) {
			listeners.add(VariableChangePrinter.class);
		}

		start(filterType, filter, classToLoad, listeners, generalLinespec, arguments.toArray(new String[0]));
	}
	
	private static LineSpecification readLineSpec(Scanner s) {
		LineSpecification result = new LineSpecification();
		String input = "";
		while (true) {
			System.out.println("Please enter line specification. Use - to specify ranges. Use , or ; to split blocks. Use \"exit\" to allow all. Example: 5-13,19;60-70;");
			input = s.nextLine();
			if (input.equalsIgnoreCase("exit"))
				break;
			try {
				result = new LineSpecification(input);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid format.");
			}
		}
		return result;
	}
	
	private static boolean ask(final String question, final Scanner s) {
		String choice = "";
		while (!choice.equalsIgnoreCase("y") && !choice.equalsIgnoreCase("n")) {
			System.out.println(question + " (y/n)");
			choice = s.nextLine();
		}
		return choice.equalsIgnoreCase("y");
	}
	
	private static void start(FilterType filterType,
			Map<Pattern, LineSpecification> filter,
			String classToLoad,
			List<Class<? extends TransformationEventListener>> listeners,
			LineSpecification generalLinespec,
			String[] arguments) throws Exception {
		if (filterType == DEFAULT_FILTER) {
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
