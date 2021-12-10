package de.fault.localization.api.services.comparer;

import de.fault.localization.api.model.ControllerFile;
import de.fault.localization.api.utilities.FileUtil;
import de.fault.localization.api.utilities.ProjectCompiler;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * this class provides methods for detecting controllers
 */
@Log
public class ControllerFinder {

	private ControllerFinder(){

	}

	private static List<File> getMavenFolders(final File baseDir) throws IOException {

		final List<File> mavenFolders = new ArrayList<>();

		try(val stream = Files.walk(baseDir.toPath())){

			val folders = stream.filter(Files::isDirectory).collect(Collectors.toList());

			for (final Path folder : folders) {

				if (folder.toFile().getName().equals("pom.xml")) {
					mavenFolders.clear();
					mavenFolders.add(folder.toFile());
					return mavenFolders;
				}

				final File[] fileList = folder.toFile().listFiles();
				if (fileList == null) {
					continue;
				}
				for (val file : fileList) {
					if (file.getName().equals("pom.xml")) {
						mavenFolders.add(folder.toFile());
					}
				}

			}
		}

		return mavenFolders;
	}

	public static List<ControllerFile> getControllers(final File classDirectory) throws IOException {
		return getControllers(classDirectory, true);
	}

	public static List<ControllerFile> getControllers(final File rootDir, final boolean compile) throws IOException {

		if (compile) {
			try {
				ProjectCompiler.compile(rootDir);
			} catch (final Exception e) {
				log.severe("error building maven project in " + rootDir.getAbsolutePath());
				e.printStackTrace();
				return new ArrayList<>();
			}
		}

		val mavenFolders = getMavenFolders(rootDir);

		final List<ControllerFile> foundControllers = new LinkedList<>();

		for (final File mavenFolder : mavenFolders) {

			final File classDirectory = new File(mavenFolder, "target/classes");

			if (!classDirectory.isDirectory()) {
				log.info("skipping: " + mavenFolder.getAbsolutePath());
				continue;
			}

			// convert File to a url
			final URL[] urls = new URL[] { classDirectory.toURI().toURL() };

			// create a new class loader with the directory
			URLClassLoader urlClassLoader = null;

			try {
				urlClassLoader = new URLClassLoader(urls);
				final Iterator<File> fileIterator = FileUtils.iterateFiles(classDirectory, new String[] { "class" },
						true);

				final File srcBaseDir = new File(mavenFolder, "src/main/java/");

				while (fileIterator.hasNext()) {

					final File file = fileIterator.next();

					final String relativePath = FileUtil.relativePath(classDirectory, file);
					final String classPath = FilenameUtils.removeExtension(relativePath).replace(File.separator,
							".");

					try {
						final Class<?> controllerClass = urlClassLoader.loadClass(classPath);

						final File javaFile = new File(srcBaseDir,
								FilenameUtils.removeExtension(relativePath) + ".java");
						for (final Annotation i : controllerClass.getAnnotations()) {
							if (i.annotationType().equals(RestController.class)
									|| i.annotationType().equals(Controller.class)) {
								foundControllers.add(new ControllerFile(controllerClass, javaFile, rootDir));
							}
						}
					} catch (final Throwable ignored) {
					}

				}
			} finally {
				if (urlClassLoader != null)
					urlClassLoader.close();
			}

		}
		return foundControllers;
	}

}
