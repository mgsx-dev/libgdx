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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

public class AndroidSDKHelper {

	public static boolean isSdkLocationValid (String sdkLocation) {
		return new File(sdkLocation, "platforms").exists();
	}
	
	public static boolean isSdkUpToDate (String sdkLocation) {
		File buildTools = new File(sdkLocation, "build-tools");
		if (!buildTools.exists()) {
			JOptionPane.showMessageDialog(null, "You have no build tools!\nUpdate your Android SDK with build tools version: "
				+ DependencyBank.buildToolsVersion);
			return false;
		}

		File apis = new File(sdkLocation, "platforms");
		if (!apis.exists()) {
			JOptionPane.showMessageDialog(null, "You have no Android APIs!\nUpdate your Android SDK with API level: "
				+ DependencyBank.androidAPILevel);
			return false;
		}
		String newestLocalTool = getLatestTools(buildTools);
		int[] localToolVersion = convertTools(newestLocalTool);
		int[] targetToolVersion = convertTools(DependencyBank.buildToolsVersion);
		if (compareVersions(targetToolVersion, localToolVersion)) {
			int value = JOptionPane.showConfirmDialog(null,
				"You have a more recent version of android build tools than the recommended.\nDo you want to use your more recent version?",
				"Warning!", JOptionPane.YES_NO_OPTION);
			if (value != 0) {
				JOptionPane.showMessageDialog(null, "Using build tools: " + DependencyBank.buildToolsVersion);
			} else {
				DependencyBank.buildToolsVersion = newestLocalTool;
			}
		} else {
			if (!versionsEqual(localToolVersion, targetToolVersion)) {
				JOptionPane.showMessageDialog(null, "Please update your Android SDK, you need build tools: "
					+ DependencyBank.buildToolsVersion);
				return false;
			}
		}

		int newestLocalApi = getLatestApi(apis);
		if (newestLocalApi > Integer.parseInt(DependencyBank.androidAPILevel)) {
			int value = JOptionPane.showConfirmDialog(null,
				"You have a more recent Android API than the recommended.\nDo you want to use your more recent version?", "Warning!",
				JOptionPane.YES_NO_OPTION);
			if (value != 0) {
				JOptionPane.showMessageDialog(null, "Using API level: " + DependencyBank.androidAPILevel);
			} else {
				DependencyBank.androidAPILevel = String.valueOf(newestLocalApi);
			}
		} else {
			if (newestLocalApi != Integer.parseInt(DependencyBank.androidAPILevel)) {
				JOptionPane.showMessageDialog(null, "Please update your Android SDK, you need the Android API: "
					+ DependencyBank.androidAPILevel);
				return false;
			}
		}
		return true;
	}
	
	private static boolean versionsEqual(int[] testVersion, int[] targetVersion) {
		for (int i = 0; i < 3; i++) {
			if (testVersion[i] != targetVersion[i]) return false;
		}
		return true;
	}
	
	private static boolean compareVersions(int[] version, int[] testVersion) {
		if (testVersion[0] > version[0]) {
			return true;
		} else if (testVersion[0] == version[0]) {
			if (testVersion[1] > version[1]) {
				return true;
			} else if (testVersion[1] == version[1]) {
				return testVersion[2] > version[2];
			}
		}
		return false;
	}
	
	private static String getLatestTools (File buildTools) {
		String version = null;
		int[] versionSplit = new int[3];
		int[] testSplit = new int[3];
		for (File toolsVersion : buildTools.listFiles()) {
			if (version == null) {
				version = readBuildToolsVersion(toolsVersion);
				versionSplit = convertTools(version);
				continue;
			}
			testSplit = convertTools(readBuildToolsVersion(toolsVersion));
			if (compareVersions(versionSplit, testSplit)) {
				version = readBuildToolsVersion(toolsVersion);
				versionSplit = convertTools(version);
			}
		}
		if (version != null) {
			return version;
		} else {
			return "0.0.0";
		}
	}

	private static int getLatestApi (File apis) {
		int apiLevel = 0;
		for (File api : apis.listFiles()) {
			int level = readAPIVersion(api);
			if (level > apiLevel) apiLevel = level;
		}
		return apiLevel;
	}
	
	private static String readBuildToolsVersion (File parentFile) {
		File propertiesFile = new File(parentFile, "source.properties");
		Properties properties;
		try {
			properties = readPropertiesFromFile(propertiesFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "0.0.0";
		}

		String versionString = properties.getProperty("Pkg.Revision");
		if (versionString == null) {
			return "0.0.0";
		}

		int count = versionString.split("\\.").length;
		for (int i = 0; i < 3 - count; i++) {
			versionString += ".0";
		}

		return versionString;
	}
	
	private static int[] convertTools (String toolsVersion) {
		String[] stringSplit = toolsVersion.split("\\.");
		int[] versionSplit = new int[3];
		if (stringSplit.length == 3) {
			try {
				versionSplit[0] = Integer.parseInt(stringSplit[0]);
				versionSplit[1] = Integer.parseInt(stringSplit[1]);
				versionSplit[2] = Integer.parseInt(stringSplit[2]);
				return versionSplit;
			} catch (NumberFormatException nfe) {
				return new int[] {0, 0, 0};
			}
		} else {
			return new int[] {0, 0, 0};
		}
	}
	
	private static Properties readPropertiesFromFile (File propertiesFile) throws IOException {
		InputStream stream = null;
		try {
			stream = new FileInputStream(propertiesFile);
			Properties properties = new Properties();
			properties.load(stream);
			return properties;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static int readAPIVersion (File parentFile) {
		File propertiesFile = new File(parentFile, "source.properties");
		Properties properties;
		try {
			properties = readPropertiesFromFile(propertiesFile);

			String versionString = properties.getProperty("AndroidVersion.ApiLevel");

			return Integer.parseInt(versionString);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
