package org.de.htwg.klara.transformers;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class OutputStreamProvider {
	private OutputStreamProvider() {}
	
	public static PrintStream stream = System.out;
	private static boolean isCustomStream = false;
	
	public static void setToFile(String filePath) throws IOException {
		setToFile(Paths.get(filePath));
	}
	
	public static void setToFile(Path filePath) throws IOException {
		Files.createDirectories(filePath, (FileAttribute<?>)null);
		if (!Files.exists(filePath))
			Files.createFile(filePath, (FileAttribute<?>)null);
		OutputStreamProvider.stream = new PrintStream(filePath.toFile());
		isCustomStream = true;
	}
	
	public static void close() {
		if (isCustomStream)
			stream.close();
	}
}
