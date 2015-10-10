package com.vinecom.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Initializer {

	public static final void bootstrap(Class<?> anchorClass) {
		FileSystemUtils.initBasePath(anchorClass);
		bootstrap();
	}

	public static final void bootstrap() {
		String systemConfiguration = FileSystemUtils.createPathFrom(FileSystemUtils.getBasePath(), "conf",
				"system.properties");
		File systemConfigFile = new File(systemConfiguration);
		if (systemConfigFile.exists() && systemConfigFile.isFile()) {
			initSystemProperties(systemConfigFile);
		} else {
			System.out.println("system properties file doesn't exists at: " + systemConfigFile.getAbsolutePath());
		}
	}

	private static final void initSystemProperties(File systemConfigFile) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(systemConfigFile);
			Properties props = new Properties();
			props.load(inputStream);
			for (Object key : props.keySet()) {
				System.setProperty((String) key, props.getProperty((String) key));
			}
			if (System.getProperty("log4j.configurationFile") == null) {
				System.setProperty("log4j.configurationFile",
						"file:" + FileSystemUtils.createPathFrom(FileSystemUtils.getBasePath(), "conf", "log4j2.xml"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
