/**Shimeji-ie*/

package com.group_finity.mascot;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import org.apache.commons.io.FilenameUtils;

import com.group_finity.mascot.config.Settings;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.group_finity.mascot.ResourcePackage;
import com.group_finity.mascot.ResourceManager;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.ConfigurationException;

class ResourcePackage implements AutoCloseable
{
	private static final Logger log = Logger.getLogger(ResourcePackage.class.getName());
	/**
	 * Generated ID of the last package.
	 */
	private static AtomicInteger lastId = new AtomicInteger();

	/**
	 * Current ID of this package.
	 */
	private final int id;
	
	private final Configuration configuration = new Configuration();
	
	private String name;
	private boolean isDirectory;
	private ZipFile archive = null;
	private String mapper = null;
	private Path path;
	//private FileSystem fs = null;
	
	public ResourcePackage(String name, boolean isDirectory)
	{
		this.id = lastId.incrementAndGet();
		
		setName(isDirectory ? name : FilenameUtils.removeExtension(name));
		setPath(name);
		isDirectory(isDirectory);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setPath(String path)
	{
		this.path = Paths.get(Settings.getString("shimeji.cfg.resources_folder",""), path);
	}
	
	public Path getPath()
	{
		return this.path;
	}
	public boolean isDirectory()
	{
		return this.isDirectory;
	}
	public void isDirectory(boolean f)
	{
		this.isDirectory = f;
	}

	
	public void open() throws IOException
	{
		if(!isDirectory)
		{
			if(archive == null)
				archive = new ZipFile(getPath().toString());
		}		
	}
	
	public void close()
	{
		if(archive != null)
		{
			try
			{
				archive.close();
			}
			catch(IOException exc)
			{}
			archive = null;
		}
	}
	
	public InputStream getResourceAsStream(String name)
	{
		InputStream in = null;
		log.log(Level.INFO, "Loading resource({0})", name);
		if( !isDirectory() )
		{
			try
				{
					log.log(Level.INFO, "Getting entry({0})", (name.startsWith("/") ? name.substring(1) : name));
					ZipEntry entry = archive.getEntry((name.startsWith("/") ? name.substring(1) : name));
					if(entry == null)
					{
						log.log(Level.INFO, "Cannot find entry({0})", name);
					}
					else if(entry.isDirectory())
					{
						log.log(Level.INFO, "entry is a directory!({0})", entry.getName());
					}
					else
						in = archive.getInputStream(entry);
				}
				catch(Exception e)
				{
					log.log(Level.SEVERE, "Error loading resource", e);
				}		
		}
		else
		{
			log.log(Level.INFO, "Getting local resource {0}", "/"+getPath().toString()+name);
			in = ResourcePackage.class.getResourceAsStream("/"+getPath().toString()+name);
		}
		return in;
	}
	
	public void loadConfiguration()
	{
		try
		{
			log.log(Level.INFO, "Reading the configuration file({0})", "/conf/Behavior.xml");

			InputStream in = this.getResourceAsStream("/conf/Behavior.xml");
			if (in==null)
				if ((in=this.getResourceAsStream("/conf/çsìÆ.xml"))==null)
					in = ResourcePackage.class.getResourceAsStream("/Behavior.xml");
			final Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);

			log.log(Level.INFO, "Reading the configuration file({0})", "/conf/Actions.xml");

			this.getConfiguration().load(new Entry(actions.getDocumentElement()), this.getName());

			in = this.getResourceAsStream("/conf/Actions.xml");
			if (in==null)
				if ((in=this.getResourceAsStream("/conf/ìÆçÏ.xml"))==null)
					in = ResourcePackage.class.getResourceAsStream("/Actions.xml");
			final Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);

			this.getConfiguration().load(new Entry(behaviors.getDocumentElement()), this.getName());

			this.getConfiguration().validate();

		} catch (final IOException e) {
			log.log(Level.SEVERE, "Failed to read the configuration file", e);
		} catch (final SAXException e) {
			log.log(Level.SEVERE, "Failed to read the configuration file", e);
		} catch (final ParserConfigurationException e) {
			log.log(Level.SEVERE, "Failed to read the configuration file", e);
		} catch (final ConfigurationException e) {
			log.log(Level.SEVERE, "There is an error in the configuration file", e);
		}
	}
	
	public Configuration getConfiguration()
	{
		return this.configuration;
	}
}