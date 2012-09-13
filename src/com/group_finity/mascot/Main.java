/**Shimeji-ie*/

package com.group_finity.mascot;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.group_finity.mascot.config.Settings;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.sun.jna.Platform;
import java.util.Random;

/**
 * Main entry point.
 */
public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getName());

	static final String BEHAVIOR_GATHER = Settings.getString("shimeji.mapper.chase_mouse","マウスの周りに集まる");

	static {
		try {
			LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static Main instance = new Main();

	public static Main getInstance() {
		return instance;
	}

	private final Manager manager = new Manager();

	private final Configuration configuration = new Configuration();

	public static void main(final String[] args) {

		getInstance().run();
	}

	public void run() {
	
		// Reads the settings file
		Settings.load("./conf/settings.cfg");
		{
			if (Settings.getString("shimeji.mapper") != null)
				Settings.load(Settings.getString("shimeji.mapper"));
			if (Settings.getString("shimeji.lang") != null)
				Settings.load(Settings.getString("shimeji.lang"));
		}

		// Load packages
		ResourceManager.loadPackages();
		
		// Creates a tray icon
		createTrayIcon();

		// Creates the mascot
		for (String packageName:ResourceManager.getPackageNames())
			createMascot(packageName);

		getManager().start();
	}

	/**
	 * Creates the tray icon.
	 * @throws AWTException
	 * @throws IOException
	 */
	private void createTrayIcon() {

		log.log(Level.INFO, "Creating a tray icon");

		if ( SystemTray.getSystemTray()==null ) {
			return;
		}

		// [One more] Menu item
		final MenuItem increaseMenu = new MenuItem(Settings.getString("shimeji.gui.one_more","One more!"));
		increaseMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				createMascot();
			}
		});

		// [Follow cursor] Menu item
		final MenuItem gatherMenu = new MenuItem(Settings.getString("shimeji.gui.follow_cursor","Follow cursor!"));
		gatherMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				gatherAll();
			}
		});

		// [Gather one] Menu item
		final MenuItem oneMenu = new MenuItem(Settings.getString("shimeji.gui.gather_one","Reduce to one!"));
		oneMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				remainOne();
			}
		});

		// [Restore IE] Menu item
		final MenuItem restoreMenu = new MenuItem(Settings.getString("shimeji.gui.restore_ie","Restore IE!"));
		restoreMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				restoreIE();
			}
		});

		// [Bye Bye] Menu item
		final MenuItem closeMenu = new MenuItem(Settings.getString("shimeji.gui.bye_bye","Bye Bye!"));
		closeMenu.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				exit();
			}
		});

		// Create a pop-up menu
		final PopupMenu trayPopup = new PopupMenu();
		trayPopup.add(increaseMenu);
		trayPopup.add(gatherMenu);
		trayPopup.add(oneMenu);
		trayPopup.add(restoreMenu);
		trayPopup.add(new MenuItem("-"));
		trayPopup.add(closeMenu);

		try {
			// Create a tray icon
			log.log(Level.INFO, "Creating tray icon");
			final TrayIcon icon = new TrayIcon(ImageIO.read(ResourceManager.getResourceAsStream("/img/icon.png", Settings.getString("shimeji.cfg.default_mascot", "default"))), "Shimeji-ie", trayPopup);
			icon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					// When the icon is clicked perform [Ancora]
					if (SwingUtilities.isLeftMouseButton(e)) {
						createMascot();
					}
				}
			});

			// Show the tray icon
			SystemTray.getSystemTray().add(icon);

		} catch (final IOException e) {
			log.log(Level.SEVERE, "Failed to create tray icon", e);
			exit();

		} catch (final AWTException e) {
			log.log(Level.SEVERE, "Failed to create tray icon", e);
			MascotEventHandler.setShowSystemTrayMenu(true);
			getManager().setExitOnLastRemoved(true);
		}

	}
	
	/**
	* Randomly creates a mascot
	*/
	public void createMascot()
	{
		Random generator = new Random();
		Object[] values = ResourceManager.getPackageNames().toArray();
		String packageName = (String)values[generator.nextInt(values.length)];
		createMascot(packageName);
	}


	/**
	 * Create one Shimeji
	 */
	public void createMascot(String packageName) {

		log.log(Level.INFO, "Creating a mascot");

		// Creates one mascot
		final Mascot mascot = new Mascot(packageName);

		// Initiates outside the screen
		mascot.setAnchor(new Point(-1000, -1000));
		// Random orientation
		mascot.setLookRight(Math.random() < 0.5);

		try {
			mascot.setBehavior(getConfiguration(packageName).buildBehavior(null, mascot));

			this.getManager().add(mascot);

		} catch (final BehaviorInstantiationException e) {
			log.log(Level.SEVERE, "Failed to initialize the first action", e);
			mascot.dispose();
		} catch (final CantBeAliveException e) {
			log.log(Level.SEVERE, "Situation in which I cannot stay alive", e);
			mascot.dispose();
		}

	}

	public void gatherAll()
	{
		for (String packageName:ResourceManager.getPackageNames())
			Main.this.getManager().setBehaviorAll(Main.this.getConfiguration(packageName), BEHAVIOR_GATHER);
	}

	public void remainOne() {
		Main.this.getManager().remainOne();
	}

	public void restoreIE() {
		NativeFactory.getInstance().getEnvironment().restoreIE();
	}

	public void exit() {

		this.getManager().disposeAll();
		this.getManager().stop();

		System.exit(0);
	}

	public Configuration getConfiguration(String packageName)
	{
		return ResourceManager.getPackage(packageName).getConfiguration();
	}

	private Manager getManager() {
		return this.manager;
	}

}
