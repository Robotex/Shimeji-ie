/**Shimeji-ie*/

package com.group_finity.mascot.config;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.FileInputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
*
*/

public class Settings
{
	private static final Logger log = Logger.getLogger(Settings.class.getName());
	private static final  Properties settings = new Properties();

	public static void load(String path)
	{
		Properties properties = new Properties();
		try
		{
			properties.load(new FileInputStream(path));
			for(Enumeration e = properties.propertyNames() ; e.hasMoreElements() ;)
			{
				Object k = e.nextElement();
				log.log(Level.INFO, "{0} = {1}", new Object[] {k,  properties.getProperty(k.toString())});
			}
		}
		catch (IOException e)
		{
		}
		settings.putAll(properties);
	}
	
	public static String getString(String k)
	{
		return settings.getProperty(k);
	}
	
	public static String getString(String k, String v)
	{
		return settings.getProperty(k, v);
	}
}