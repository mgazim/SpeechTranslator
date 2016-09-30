package org.speechtr.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class Languages
{
	private final TreeMap<String, String> languages = new TreeMap<>();

	public Languages() throws FileNotFoundException, IOException, URISyntaxException
	{
		try (final FileInputStream fis = new FileInputStream(
				Languages.class.getClassLoader().getResource("langs.properties").getPath().replace("!", "")))
		{
			final Properties properties = new Properties();
			properties.load(fis);

			final Set<Object> keys = properties.keySet();

			keys.stream().forEach(key -> languages.put((String) key, properties.getProperty((String) key)));
		}
	}

	public Object[] getValues()
	{
		return languages.keySet().toArray();
	}

	public String valueOf(final String key)
	{
		return this.languages.get(key);
	}
}
