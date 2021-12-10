package de.fault.localization.api.utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * contains methods for handling files and io
 */
public class FileUtil {

	private FileUtil(){

	}

	/**
	 * @param dir  - base directory
	 * @param file - the file
	 * @return relative path
	 */
	public static String relativePath(final File dir, final File file) {
		final Path pathAbsolute = file.toPath();
		final Path pathBase = dir.toPath();
		final Path pathRelative = pathBase.relativize(pathAbsolute);
		return pathRelative.toString();
	}

	/**
	 * updates file with {@code content}
	 *
	 * @param file    - the file to update
	 * @param content - the content
	 * @throws IOException - if io related error occurs, e.g. no permissions, should
	 *                     not occur anyway, since docker is run with uid 0 (root)
	 */
	public static void write(final File file, final String content) throws IOException {
		final PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
		writer.print(content);
		writer.close();
	}

	/**
	 * @param file   - the file
	 * @param search - the text to search
	 * @return the first matching line number
	 * @throws IOException - if io related error occurs, e.g. no permissions, should
	 *                     not occur anyway, since docker is run with uid 0 (root)
	 */
	public static int getLineNumber(final File file, final String search) throws IOException {
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			assert file.canRead(); // check whether file is available
			String line;
			int currentLine = 0;
			while ((line = in.readLine()) != null) {
				currentLine++;
				if (line.contains(search)) {
					in.close();
					return currentLine;
				}
			}
		}
		return -1;
	}
}
