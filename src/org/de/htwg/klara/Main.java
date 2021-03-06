package org.de.htwg.klara;

import java.io.IOException;
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
import org.de.htwg.klara.utils.ConsoleUtil;

public class Main {
	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_INVALID_FILTER = 1;
	public static final int EXIT_INVALID_OPTION = 2;
	public static final int EXIT_NO_DEBUG_CLASS = 3;
	private static final String PROG_NAME = "Klara";
	static final FilterType DEFAULT_FILTER = FilterType.NOTHING;

	public static void main(final String[] args) throws Throwable {
		List<Class<? extends TransformationEventListener>> listeners = new LinkedList<>();
		Map<Pattern, LineSpecification> filter = new HashMap<>();
		FilterType filterType = DEFAULT_FILTER;
		LineSpecification generalLinespec = new LineSpecification();
		boolean debug = false;
		
		int i;
		for (i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-"))
				break;
			if (args[i].equalsIgnoreCase("-h")) {
				usage();
				exit(EXIT_SUCCESS);
			} else if (args[i].equals("-f")) {
				if (filterType != DEFAULT_FILTER && filterType != FilterType.WHITELIST) {
					System.err.println("Unable to use multiple kinds of filtering in one call.");
					exit(EXIT_INVALID_FILTER);
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
					exit(EXIT_INVALID_FILTER);
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
					OutputStreamProvider.setToFile(writeTo);
				}
			} else if (args[i].equals("-t")) {
				listeners.add(LineTracer.class);
			} else if (args[i].equals("-b")) {
				System.out.println(args[i] + " is not yet implemented.");
			} else if (args[i].equals("-v")) {
				listeners.add(VariableChangePrinter.class);
			} else if (args[i].equals("-d")) {
				debug = true;
			} else if (args[i].equals("-i")) {
				interactiveStart();
				exit(EXIT_SUCCESS);
			} else {
				System.err.println("Invalid option " + args[i]);
				usage();
				exit(EXIT_INVALID_OPTION);
			}
		}

		if (i >= args.length) {
			System.err.println("No class to debug specified!");
			usage();
			exit(EXIT_NO_DEBUG_CLASS);
		}
		String classToLoad = args[i++];
		String argArray[] = Arrays.copyOfRange(args, i, args.length);
		
		Launcher.start(listeners, filterType, filter, generalLinespec, classToLoad, argArray, debug);
		exit(EXIT_SUCCESS);
	}
	
	/**
	 * Exit with the given code, doing any cleanup first.
	 * @param code	Exit-Code to use
	 */
	private static void exit(int code) {
		OutputStreamProvider.close();
		System.exit(code);
	}

	/**
	 * Print usage of the commandline interface
	 */
	public static void usage() {
		ConsoleUtil.fixwidthPrint("This is the commandline interface of " + PROG_NAME + ".");
		ConsoleUtil.fixwidthPrint(PROG_NAME + " can be used to track bugs in Java programs.");
		ConsoleUtil.fixwidthPrint("");
		ConsoleUtil.fixwidthPrint("Argument specification:");
		ConsoleUtil.fixwidthPrint("java -jar " + PROG_NAME + ".jar { -h | -H | { "
				+ "[{ -f regex[ lines][ -f regex[ lines]]...}|{ -F regex[ -F regex]...}] "
				+ "[ -l lines ] [ -t ] [ -v ] } "
				+ "progname [argument[ argument]...] }");
		ConsoleUtil.fixwidthPrint("");
		ConsoleUtil.fixwidthPrint("Argument details:");
		ConsoleUtil.fixwidthPrint("  -h | -H");
		ConsoleUtil.fixwidthPrint("    Show this help message");
		ConsoleUtil.fixwidthPrint("  -f <regex> [<lines>]");
		ConsoleUtil.fixwidthPrint("    Specify a whitelist rule. Can be used multiple times for multiple entries. Can not be combined with -F."
				+ " Can optionaly specify a specific lineset to debug in matching classes, overriding those set by -l");
		ConsoleUtil.fixwidthPrint("  -F <regex>");
		ConsoleUtil.fixwidthPrint("    Specify a blacklist rule. Can be used multiple times for multiple entries. Can not be combined with -f");
		ConsoleUtil.fixwidthPrint("  -l <lines>");
		ConsoleUtil.fixwidthPrint("    Specify a line set to be loged. Use minus to specify a range, use comma or semicolon to seperate blocks.");
		ConsoleUtil.fixwidthPrint("  -t");
		ConsoleUtil.fixwidthPrint("    Trace the exact line order by printing every line run.");
		ConsoleUtil.fixwidthPrint("  -o e | <file>");
		ConsoleUtil.fixwidthPrint("    Write output to stderr or a file.");
//		ConsoleUtil.fixwidthPrint("  -b");
//		ConsoleUtil.fixwidthPrint("    Trace the branching used. Will evaluate which if case was executed, at what switch-case block it jumped and how many times loops where executed.");
		ConsoleUtil.fixwidthPrint("  -v");
		ConsoleUtil.fixwidthPrint("    Trace any variable assignment. Variables will be printed when declared and every time they are updated. Will not track changes of encapsuled variables (Like changing the \"x\" value of a \"Point\")");
		ConsoleUtil.fixwidthPrint("  -i");
		ConsoleUtil.fixwidthPrint("    Use interactive mode instead of detailed call. Will ignore other arguments.");
		ConsoleUtil.fixwidthPrint("");
		ConsoleUtil.fixwidthPrint("Example call:");
		ConsoleUtil.fixwidthPrint("java -jar " + PROG_NAME + ".jar -t -v -f my.cool.class.* 20-50 my.cool.pkg.MyClass arg1 arg2");
		ConsoleUtil.fixwidthPrint("");
		ConsoleUtil.fixwidthPrint("");

		ConsoleUtil.fixwidthPrint("Press Enter to exit.");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Print a prompt for each possible option, asking for the desired setting. 
	 * @throws Exception	Not handling any exceptions
	 */
	public static void interactiveStart() throws Throwable {
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
		if (ConsoleUtil.ask("Do you want to pass any arguments?", s)) {
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
				if (ConsoleUtil.ask("Do you want to set a specific Lineset for classes matching this filter?", s)) {
					filter.put(p, ConsoleUtil.readLineSpec(s));
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
		if (ConsoleUtil.ask("Do you want to set a general Lineset for all clases without a specific filter?", s)) {
			generalLinespec = ConsoleUtil.readLineSpec(s);
		}
		if (ConsoleUtil.ask("Do you want to trace the exakt line order?", s)) {
			listeners.add(LineTracer.class);
		}
		if (ConsoleUtil.ask("Do you want to reroute output?", s)) {
			if (ConsoleUtil.ask("Choose location? Choosing no means output goes to stderr.", s)) {
				OutputStreamProvider.stream = System.err;
			} else {
				choice = null;
				while (choice == null) {
					System.out.println("Please provide a File-Path to write to.");
					choice = s.nextLine();
				}
				OutputStreamProvider.setToFile(choice);
			}
		}
//		if (ConsoleUtil.ask("Do you want to get general branching information?", s)) {
//			System.out.println("Not yet implemented.");
//		}
		if (ConsoleUtil.ask("Do you want to get information about every variable assignment?", s)) {
			listeners.add(VariableChangePrinter.class);
		}

		Launcher.start(listeners, filterType, filter, generalLinespec, classToLoad, arguments.toArray(new String[0]), false);
	}
}
