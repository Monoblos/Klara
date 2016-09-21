package org.de.htwg.klara.transformers;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class used by generated prints of the {@link Transformer}. Prints will always be sent to the stream of this class.
 * Closing of the custom stream needs to be done manually if it was set.
 * 
 * @author mrs
 *
 */
public class OutputStreamProvider {
	private OutputStreamProvider() {}
	
	/**
	 * {@link PrintStream} used by prints of the {@link Transformer}.
	 * By default points to System.out but can be changed to redirect output.
	 * When Setting to a file please use {@link #setToFile(Path)} or {@link #setToFile(String)} and {@link #close()} after use.
	 */
	public static PrintStream stream = System.out;
	private static boolean isCustomStream = false;
	
	/**
	 * Redirect stream to a File. Folder-Path and file will be created if not existing.
	 * Will not prompt if overriding a existing file!
	 * @param filePath	The path to the file as a String
	 * @throws IOException	If something goes wrong
	 */
	public static void setToFile(String filePath) throws IOException {
		setToFile(Paths.get(filePath));
	}
	
	/**
	 * Redirect stream to a File. Folder-Path and file will be created if not existing.
	 * Will not prompt if overriding a existing file!
	 * @param filePath	The path to the file
	 * @throws IOException	If something goes wrong
	 */
	public static void setToFile(Path filePath) throws IOException {
		Files.createDirectories(filePath.toAbsolutePath().getParent());
		if (!Files.exists(filePath))
			Files.createFile(filePath);
		System.out.println(filePath.toAbsolutePath().toString());
		OutputStreamProvider.stream = new PrintStream(filePath.toFile());
		isCustomStream = true;
	}
	
	/**
	 * Close the stream if it was a custom stream (created by a "setToFile" method).
	 * Has no effect if it is not a custom stream.
	 */
	public static void close() {
		if (isCustomStream)
			stream.close();
	}
}
