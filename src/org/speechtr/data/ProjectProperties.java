package org.speechtr.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ProjectProperties
{
	private static final Logger LOG = Logger.getRootLogger();

	private static Properties properties;
	static
	{
		try (final FileInputStream fis = new FileInputStream(
				ProjectProperties.class.getClassLoader().getResource("config.properties").getPath().replace("!", "")))
		{
			properties = new Properties();
			properties.load(fis);
		}
		catch (final FileNotFoundException e)
		{
			LOG.error("Configuration file was not found", e);
		}
		catch (final IOException e)
		{
			LOG.error("Problem with reading configuration file", e);
		}
	}

	public static String getProperty(final String key) throws NoSuchPropertyException
	{
		final String property = properties.getProperty(key);
		if (property == null && !"conn.proxy".equals(key))
		{
			throw new NoSuchPropertyException(key);
		}
		return property;
	}

	public static class NoSuchPropertyException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public NoSuchPropertyException(final String property)
		{
			super("No " + property + " property in config.properties file");
		}
	}
}
