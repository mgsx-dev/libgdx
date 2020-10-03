/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.setup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.setup.DependencyBank.ProjectDependency;
import com.badlogic.gdx.setup.DependencyBank.ProjectType;
import com.badlogic.gdx.setup.Executor.CharCallback;

/** Command line tool to generate libgdx projects
 * @author badlogic
 * @author Tomski */
public class GdxSetup {
	public void build (ProjectBuilder builder, String outputDir, String appName, String packageName, String mainClass,
			Language language, String sdkLocation, CharCallback callback, List<String> gradleArgs) {
		Project project = new Project();

		String packageDir = packageName.replace('.', '/');
		String sdkPath = sdkLocation.replace('\\', '/');

		if (sdkLocation != null && !AndroidSDKHelper.isSdkLocationValid(sdkLocation)) {
			System.out.println("Android SDK location '" + sdkLocation + "' doesn't contain an SDK");
		}

		// root dir/gradle files
		project.files.add(new ProjectFile("gitignore", ".gitignore", false));
		project.files.add(new TemporaryProjectFile(builder.settingsFile, "settings.gradle", false));
		project.files.add(new TemporaryProjectFile(builder.buildFile, "build.gradle", true));
		project.files.add(new ProjectFile("gradlew", false));
		project.files.add(new ProjectFile("gradlew.bat", false));
		project.files.add(new ProjectFile("gradle/wrapper/gradle-wrapper.jar", false));
		project.files.add(new ProjectFile("gradle/wrapper/gradle-wrapper.properties", false));
		project.files.add(new ProjectFile("gradle.properties"));

		// core project
		project.files.add(new ProjectFile("core/build.gradle"));
		project.files.add(new ProjectFile("core/src/MainClass", "core/src/" + packageDir + "/" + mainClass + ".java", true));
		if (builder.modules.contains(ProjectType.HTML) && language.gwtSupported) {
			project.files.add(new ProjectFile("core/CoreGdxDefinition", "core/src/" + mainClass + ".gwt.xml", true));
		}

		// desktop project
		if (builder.modules.contains(ProjectType.DESKTOP)) {
			project.files.add(new ProjectFile("desktop/build.gradle"));
			project.files.add(new ProjectFile("desktop/src/DesktopLauncher", "desktop/src/" + packageDir + "/desktop/DesktopLauncher.java", true));
		}

		// Assets
		String assetPath = builder.modules.contains(ProjectType.ANDROID) ? "android/assets" : "core/assets";
		project.files.add(new ProjectFile("android/assets/badlogic.jpg", assetPath + "/badlogic.jpg", false));

		// android project
		if (builder.modules.contains(ProjectType.ANDROID)) {
			project.files.add(new ProjectFile("android/res/values/color.xml"));
			project.files.add(new ProjectFile("android/res/values/strings.xml"));
			project.files.add(new ProjectFile("android/res/values/styles.xml", false));
			project.files.add(new ProjectFile("android/res/drawable-anydpi-v26/ic_launcher.xml", false));
			project.files.add(new ProjectFile("android/res/drawable-anydpi-v26/ic_launcher_foreground.xml", false));
			project.files.add(new ProjectFile("android/res/drawable-hdpi/ic_launcher.png", false));
			project.files.add(new ProjectFile("android/res/drawable-mdpi/ic_launcher.png", false));
			project.files.add(new ProjectFile("android/res/drawable-xhdpi/ic_launcher.png", false));
			project.files.add(new ProjectFile("android/res/drawable-xxhdpi/ic_launcher.png", false));
			project.files.add(new ProjectFile("android/res/drawable-xxxhdpi/ic_launcher.png", false));
			project.files.add(new ProjectFile("android/src/AndroidLauncher", "android/src/" + packageDir + "/AndroidLauncher.java", true));
			project.files.add(new ProjectFile("android/AndroidManifest.xml"));
			project.files.add(new ProjectFile("android/build.gradle", true));
			project.files.add(new ProjectFile("android/ic_launcher-web.png", false));
			project.files.add(new ProjectFile("android/proguard-rules.pro", false));
			project.files.add(new ProjectFile("android/project.properties", false));
			project.files.add(new ProjectFile("local.properties", true));
		}

		// html project
		if (builder.modules.contains(ProjectType.HTML)) {
			project.files.add(new ProjectFile("html/build.gradle"));
			project.files.add(new ProjectFile("html/src/HtmlLauncher", "html/src/" + packageDir + "/client/HtmlLauncher.java", true));
			project.files.add(new ProjectFile("html/GdxDefinition", "html/src/" + packageDir + "/GdxDefinition.gwt.xml", true));
			project.files.add(new ProjectFile("html/GdxDefinitionSuperdev", "html/src/" + packageDir + "/GdxDefinitionSuperdev.gwt.xml", true));
			project.files.add(new ProjectFile("html/war/index", "html/webapp/index.html", true));
			project.files.add(new ProjectFile("html/war/styles.css", "html/webapp/styles.css", false));
			project.files.add(new ProjectFile("html/war/refresh.png", "html/webapp/refresh.png", false));
			project.files.add(new ProjectFile("html/war/WEB-INF/web.xml", "html/webapp/WEB-INF/web.xml", true));
		}

		// ios robovm
		if (builder.modules.contains(ProjectType.IOS)) {
			project.files.add(new ProjectFile("ios/src/IOSLauncher", "ios/src/" + packageDir + "/IOSLauncher.java", true));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/Contents.json", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/app-store-icon-1024@1x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/Contents.json", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-app-icon-76@1x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-app-icon-76@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-notifications-icon-20@1x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-notifications-icon-20@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-pro-app-icon-83.5@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-settings-icon-29@1x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-settings-icon-29@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-spotlight-icon-40@1x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/ipad-spotlight-icon-40@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-app-icon-60@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-app-icon-60@3x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-notification-icon-20@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-notification-icon-20@3x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-spotlight-icon-40@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-spotlight-icon-40@3x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-spotlight-settings-icon-29@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/AppIcon.appiconset/iphone-spotlight-settings-icon-29@3x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/Logo.imageset/Contents.json", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/Logo.imageset/libgdx@1x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/Logo.imageset/libgdx@2x.png", false));
			project.files.add(new ProjectFile("ios/data/Media.xcassets/Logo.imageset/libgdx@3x.png", false));
			project.files.add(new ProjectFile("ios/data/Base.lproj/LaunchScreen.storyboard", false));

			project.files.add(new ProjectFile("ios/build.gradle", true));
			project.files.add(new ProjectFile("ios/Info.plist.xml", false));
			project.files.add(new ProjectFile("ios/robovm.properties"));
			project.files.add(new ProjectFile("ios/robovm.xml", true));
		}

		Map<String, String> values = new HashMap<String, String>();
		values.put("%APP_NAME%", appName);
		values.put("%APP_NAME_ESCAPED%", appName.replace("'", "\\'"));
		values.put("%LANG%", language.name);
		values.put("%PACKAGE%", packageName);
		values.put("%PACKAGE_DIR%", packageDir);
		values.put("%MAIN_CLASS%", mainClass);
		values.put("%ANDROID_SDK%", sdkPath);
		values.put("%ASSET_PATH%", assetPath);
		values.put("%BUILD_TOOLS_VERSION%", DependencyBank.buildToolsVersion);
		values.put("%API_LEVEL%", DependencyBank.androidAPILevel);
		values.put("%GWT_VERSION%", DependencyBank.gwtVersion);
		if (builder.modules.contains(ProjectType.HTML)) {
			values.put("%GWT_INHERITS%", parseGwtInherits(builder));
		}

		copyAndReplace(outputDir, project, values);

		builder.cleanUp();

		// HACK executable flag isn't preserved for whatever reason...
		new File(outputDir, "gradlew").setExecutable(true);

		Executor.execute(new File(outputDir), "gradlew.bat", "gradlew", "clean" + parseGradleArgs(builder.modules, gradleArgs), callback);
	}

	private void copyAndReplace (String outputDir, Project project, Map<String, String> values) {
		File out = new File(outputDir);
		if (!out.exists() && !out.mkdirs()) {
			throw new RuntimeException("Couldn't create output directory '" + out.getAbsolutePath() + "'");
		}

		for (ProjectFile file : project.files) {
			copyFile(file, out, values);
		}
	}

	private byte[] readResource (String resource, String path) {
		InputStream in = null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 10];
			in = GdxSetup.class.getResourceAsStream(path + resource);
			if (in == null) throw new RuntimeException("Couldn't read resource '" + resource + "'");
			int read = 0;
			while ((read = in.read(buffer)) > 0) {
				bytes.write(buffer, 0, read);
			}
			return bytes.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read resource '" + resource + "'", e);
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
			}
		}
	}

	private String readResourceAsString (String resource, String path) {
		try {
			return new String(readResource(resource, path), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeFile (File outFile, byte[] bytes) {
		OutputStream out = null;

		try {
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			out.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write file '" + outFile.getAbsolutePath() + "'", e);
		} finally {
			if (out != null) try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	private void writeFile (File outFile, String text) {
		try {
			writeFile(outFile, text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private void copyFile (ProjectFile file, File out, Map<String, String> values) {
		File outFile = new File(out, file.outputName);
		if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
			throw new RuntimeException("Couldn't create dir '" + outFile.getAbsolutePath() + "'");
		}

		boolean isTemp = file instanceof TemporaryProjectFile;

		if (file.isTemplate) {
			String txt;
			if (isTemp) {
				txt = readResourceAsString(((TemporaryProjectFile)file).file);
			} else {
				txt = readResourceAsString(file.resourceName, file.resourceLoc);
			}
			txt = replace(txt, values);
			writeFile(outFile, txt);
		} else {
			if (isTemp) {
				writeFile(outFile, readResource(((TemporaryProjectFile)file).file));
			} else {
				writeFile(outFile, readResource(file.resourceName, file.resourceLoc));
			}
		}
	}

	private String replace (String txt, Map<String, String> values) {
		for (String key : values.keySet()) {
			String value = values.get(key);
			txt = txt.replace(key, value);
		}
		return txt;
	}

	private static void printHelp () {
		System.out
			.println("Usage: GdxSetup --dir <dir-name> --name <app-name> --package <package> --mainClass <mainClass> --sdkLocation <SDKLocation> [--excludeModules <modules>] [--extensions <extensions>]");
		System.out.println("dir ... the directory to write the project files to");
		System.out.println("name ... the name of the application");
		System.out.println("package ... the Java package name of the application");
		System.out.println("mainClass ... the name of your main ApplicationListener");
		System.out.println("sdkLocation ... the location of your android SDK. Uses ANDROID_HOME if not specified. Ignored if android module is excluded");
		System.out.println("excludeModules ... the modules to exclude on the project generation separated by ';'. Optional");
		System.out.println("extensions ... the extensions to include in the project separated by ';'. Optional");
	}

	private static Map<String, String> parseArgs (String[] args) {
		if (args.length % 2 != 0) {
			printHelp();
			System.exit(-1);
		}

		Map<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < args.length; i += 2) {
			String param = args[i].replace("--", "");
			String value = args[i + 1];
			params.put(param, value);
		}
		return params;
	}

	 private static List<String> parseExcludedModules (String excludedModules) {
		  List<String> excludedModulesList = new ArrayList<String>();

		  while (excludedModules.contains(";")) {
				excludedModulesList.add(excludedModules.substring(0, excludedModules.indexOf(";")).toLowerCase());
				excludedModules = excludedModules.substring(excludedModules.indexOf(";") + 1);
		  }
		  excludedModulesList.add(excludedModules.toLowerCase());

		  return excludedModulesList;
	 }

	 private static List<Dependency> parseDependencies (String dependencies, DependencyBank bank) {
		  List<String> dependencyNames = new ArrayList<String>();
		  while (dependencies.contains(";")) {
				dependencyNames.add(dependencies.substring(0, dependencies.indexOf(";")).toLowerCase());
				dependencies = dependencies.substring(dependencies.indexOf(";") + 1);
		  }
		  dependencyNames.add(dependencies.toLowerCase());

		  Map<String, Dependency> dependencyMap = new HashMap<String, Dependency>();
		  for (ProjectDependency pd : ProjectDependency.values()) {
				dependencyMap.put(pd.name().toLowerCase(), bank.getDependency(pd));
		  }

		  List<Dependency> dependencyList = new ArrayList<Dependency>();
		  dependencyList.add(bank.getDependency(ProjectDependency.GDX));
		  for (String name : dependencyNames) {
				if (dependencyMap.containsKey(name)) {
					 System.out.println("Extension " + name + " found");
					 dependencyList.add(dependencyMap.get(name));
				} else
					 System.out.println("Extension " + name + " not found");
		  }

		  return dependencyList;
	 }

	private String parseGwtInherits (ProjectBuilder builder) {
		String parsed = "";

		for (Dependency dep : builder.dependencies) {
			if (dep.getGwtInherits() != null) {
				for (String inherit : dep.getGwtInherits()) {
					parsed += "\t<inherits name='" + inherit + "' />\n";
				}
			}
		}

		return parsed;
	}

	private String parseGradleArgs (List<ProjectType> modules, List<String> args) {
		String argString = "";
		if (args == null) return argString;
		for (String argument : args) {
			argString += " " + argument;
		}
		return argString;
	}

	public static void main (String[] args) throws IOException {
		Map<String, String> params = parseArgs(args);
		List<String> excludedModules = null;
		if (params.containsKey("excludeModules"))
			excludedModules = parseExcludedModules(params.get("excludeModules"));

		if (!params.containsKey("dir") ||
			!params.containsKey("name") ||
			!params.containsKey("package") ||
			!params.containsKey("mainClass") ||
			(!params.containsKey("sdkLocation") && System.getenv("ANDROID_HOME") == null &&
				(excludedModules == null || !excludedModules.contains("android")))) {
			new GdxSetupUI();
			printHelp();
		} else {
			String sdkLocation = "";
			 if (excludedModules == null || !excludedModules.contains("android")) {
				  if (System.getenv("ANDROID_HOME") != null && !params.containsKey("sdkLocation")) {
						sdkLocation = System.getenv("ANDROID_HOME");
				  } else {
						sdkLocation = params.get("sdkLocation");
				  }
			 }

			DependencyBank bank = new DependencyBank();
			ProjectBuilder builder = new ProjectBuilder(bank);
			List<ProjectType> projects = new ArrayList<ProjectType>();

			projects.add(ProjectType.CORE);
			 if (excludedModules == null) {
				  projects.add(ProjectType.DESKTOP);
				  projects.add(ProjectType.ANDROID);
				  projects.add(ProjectType.IOS);
				  projects.add(ProjectType.HTML);
			 } else {
				  if (!excludedModules.contains("desktop"))
						projects.add(ProjectType.DESKTOP);
				  if (!excludedModules.contains("android"))
						projects.add(ProjectType.ANDROID);
				  if (!excludedModules.contains("ios"))
						projects.add(ProjectType.IOS);
				  if (!excludedModules.contains("html"))
						projects.add(ProjectType.HTML);
			 }

			List<Dependency> dependencies = new ArrayList<Dependency>();
			if (params.containsKey("extensions")) {
				 dependencies.addAll(parseDependencies(params.get("extensions"), bank));
			} else {
				 dependencies.add(bank.getDependency(ProjectDependency.GDX));
			}

			String language = params.containsKey("language") ? params.get("language") : "java";
			Language languageEnum = Language.JAVA;
			for(Language l: Language.values()) {
				if(l.name.equals(language)) {
					languageEnum = l;
				}
			}
			builder.buildProject(projects, dependencies);
			builder.build(languageEnum);
			new GdxSetup().build(builder, params.get("dir"), params.get("name"), params.get("package"), params.get("mainClass"), languageEnum,
				sdkLocation, new CharCallback() {
					@Override
					public void character (char c) {
						System.out.print(c);
					}
				}, null);
		}
	}
}
