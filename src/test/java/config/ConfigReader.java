package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads values from src/test/resources/config.properties. Usage:
 * ConfigReader.get("base.url")
 */
public class ConfigReader {
	private static final Properties props = new Properties();

	static {
		try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (is == null)
				throw new RuntimeException("config.properties not found on classpath");
			props.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load config.properties", e);
		}
	}

	public static String get(String key) {
		String value = props.getProperty(key);
		if (value == null)
			throw new RuntimeException("Missing config key: " + key);
		return value.trim();
	}

	public static boolean getBool(String key) {
		return Boolean.parseBoolean(get(key));
	}

	public static int getInt(String key) {
		return Integer.parseInt(get(key));
	}

	public static double getDouble(String key) {
		return Double.parseDouble(get(key));
	}
}
