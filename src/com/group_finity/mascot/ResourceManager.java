/**Shimeji-ie*/

package com.group_finity.mascot;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Exception;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;

import com.group_finity.mascot.config.Settings;

public class ResourceManager
{
	private static final Logger log = Logger.getLogger(ResourceManager.class.getName());
	private static final Map<String, ResourcePackage> resPackages = new HashMap<String, ResourcePackage>();
	
	public static void loadPackages()
	{
		log.log(Level.INFO, "Loading packages...");
		File dir = new File(Settings.getString("shimeji.cfg.resources_folder", "mascots"));
		// The list of files can also be retrieved as File objects
		File[] files = dir.listFiles();

		// This filter only returns directories and files with specified extensions
		// TODO: Get archives first then folders (override for debugging)
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return ( (file.isDirectory()||FilenameUtils.getExtension(file.getName()).equals("zip")) ? true : false );
			}
		};
		files = dir.listFiles(fileFilter);
		for(File f:files)
		{
			ResourcePackage pack = new ResourcePackage(f.getName(), f.isDirectory());
			resPackages.put(pack.getName(), pack);
			try
			{
				pack.open();
				pack.loadConfiguration();
				pack.close();
			}
			catch(IOException exc)
			{
				log.log(Level.WARNING, "Exception occurred while trying to open package({0})", pack.getName());
			}
		}
		log.log(Level.INFO, "Done loading packages");
	}
	
	public static ResourcePackage getPackage(String name)
	{ 
		ResourcePackage res = resPackages.get(name);
		try
		{
			res.open();
		}
		catch(IOException exc)
		{
			log.log(Level.WARNING, "Exception occurred while trying to open package({0})", name);
		}
		return res;
	}
	
	public static Set<String> getPackageNames()
	{
		return resPackages.keySet();
	}
	

	public static InputStream getResourceAsStream (String name)
	{
		return getResourceAsStream(name, null);
	}
	
	public static InputStream getResourceAsStream(String name, String packageName)
	{
		InputStream in = null;
		log.log(Level.INFO, "getResourceAsStream({0},{1})", new Object[] {name, packageName});
		if(packageName == null)
			in = ResourceManager.class.getResourceAsStream(name);
		else
		{
			ResourcePackage res = getPackage(packageName);
			log.log(Level.INFO, "res isNull? {0}", (res==null ? "true":"false"));
			in = res.getResourceAsStream(name);
		}
		log.log(Level.INFO, "in isNull? {0}", (in==null ? "true":"false"));
		return in;
	}
}