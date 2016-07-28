package org.de.htwg.klara;

import java.io.IOException;
import java.io.PrintStream;
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
import org.de.htwg.klara.transformers.OutputStreamProvider;
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
		boolean customStream = false;
		
		int i;
		for (i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-"))
				break;
			if (args[i].equalsIgnoreCase("-h")) {
				usage();
				exit(0, customStream);
			} else if (args[i].equals("-f")) {
				if (filterType != DEFAULT_FILTER && filterType != FilterType.WHITELIST) {
					System.err.println("Unable to use multiple kinds of filtering in one call.");
					exit(1, customStream);
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
					System.err.println("Unable to use multiple kinds of filtering in one call.");
					exit(1, customStream);
				}
				filterType = FilterType.BLACKLIST;
				filter.put(Pattern.compile(args[++i]), null);
			} else if (args[i].equals("-l")) {
				generalLinespec = new LineSpecification(args[++i]);
			} else if (args[i].equals("-o")) {
				String writeTo = args[++i];
				if (writeTo.equals("e")) {
					OutputStreamProvider.stream = System.err;
				} else {
					customStream = true;
					OutputStreamProvider.stream = new PrintStream(writeTo);
				}
			} else if (args[i].equals("-t")) {
				listeners.add(LineTracer.class);
			} else if (args[i].equals("-b")) {
				System.out.println(args[i] + " is not yet implemented.");
			} else if (args[i].equals("-v")) {
				listeners.add(VariableChangePrinter.class);
			} else if (args[i].equals("-i")) {
				interactiveStart();
				exit(0, customStream);
			} else {
				System.err.println("Invalid option " + args[i]);
				usage();
				exit(2, customStream);
			}
		}

		if (i >= args.length) {
			System.err.println("No class to debug specified!");
			usage();
			exit(3, customStream);
		}
		String classToLoad = args[i++];
		String argArray[] = Arrays.copyOfRange(args, i, args.length);
		
		start(filterType, filter, classToLoad, listeners, generalLinespec, argArray);
		exit(0, customStream);
	}
	
	private static void exit(int code, boolean customStream) {
		if (customStream)
			OutputStreamProvider.stream.close();
		System.exit(code);
	}

	public static void usage() {
		fixwidthPrint("This is the commandline interface of " + PROG_NAME + ".");
		fixwidthPrint(PROG_NAME + " can be used to track bugs in Java programs.");
		fixwidthPrint("");
		fixwidthPrint("Argument specification:");
		fixwidthPrint("java -jar Klara.jar { -h | -H | { "
				+ "[ -f regex [ lines ] [ -f regex [ lines ]]... | -F regex [ -F regex]... ] "
				+ "[ -l lines ] [ -t ] [ -v ] } } "
				+ "progname [argument [ agrumen]...]");
		fixwidthPrint("");
		fixwidthPrint("Argument details:");
		fixwidthPrint("  -h | -H");
		fixwidthPrint("    Show this help message");
		fixwidthPrint("  -f <regex> [<lines>]");
		fixwidthPrint("    Specify a whitelist rule. Can be used multiple times for multiple entries. Can not be combined with -F."
				+ " Can optionaly specify a specific lineset to debug in matching classes, overriding those set by -l");
		fixwidthPrint("  -F <regex>");
		fixwidthPrint("    Specify a blacklist rule. Can be used multiple times for multiple entries. Can not be combined with -f");
		fixwidthPrint("  -l <lines>");
		fixwidthPrint("    Specify a line set to be loged. Use minus to specify a range, use comma or semicolon to seperate blocks.");
		fixwidthPrint("  -t");
		fixwidthPrint("    Trace the exact line order by printing every line run.");
		fixwidthPrint("  -o e | <file>");
		fixwidthPrint("    Write output to stderr or a file.");
		fixwidthPrint("  -b");
		fixwidthPrint("    Trace the branching used. Will evaluate which if case was executed, at what switch-case block it jumped and how many times loops where executed.");
		fixwidthPrint("  -v");
		fixwidthPrint("    Trace any variable assignment. Variables will be printed when declared and every time they are updated.");
		fixwidthPrint("  -i");
		fixwidthPrint("    Use interactive mode instead of detailed call. Will ignore other arguments.");
		fixwidthPrint("");
		fixwidthPrint("Example call:");
		fixwidthPrint("java -jar Klara.jar -t -v -f my.cool.class.* 20-50 my.cool.pkg.MyClass arg1 arg2");
		fixwidthPrint("");
		fixwidthPrint("");

		fixwidthPrint("Press Enter to exit.");
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
					System.err.println("Invalid pattern format. The Pattern must be a valid Regular Expression.");
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
					System.err.println("Invalid pattern format. The Pattern must be a valid Regular Expression.");
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
		if (ask("Do you want to reroute output?", s)) {
			if (ask("Choose location? If no output goes to stderr.", s)) {
				OutputStreamProvider.stream = System.err;
			} else {
				
			}
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
				System.err.println("Invalid format.");
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
	
	private static void fixwidthPrint(String text) {
		fixwidthPrint(text, 80);
	}
	
	private static void fixwidthPrint(String text, final int lineLength) {
		final int frontSpaces = 4;
		if (text.length() < lineLength) {
			System.out.println(text);
			return;
		}
		String outBlock = text.substring(0, lineLength);
		text = text.substring(lineLength);
		System.out.println(outBlock);
		while(text.length() > lineLength - frontSpaces) {
			outBlock = text.substring(0, lineLength - frontSpaces);
			text = text.substring(lineLength - frontSpaces);
			for (int i = 0; i < frontSpaces; i++) {
				outBlock = " " + outBlock;
			}
			System.out.println(outBlock);
		}
		for (int i = 0; i < frontSpaces; i++) {
			System.out.print(" ");
		}
		System.out.println(text);
	}
}
