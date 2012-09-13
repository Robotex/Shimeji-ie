/**Shimeji-ie*/

package com.group_finity.mascot.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.group_finity.mascot.ResourceManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Graphics2D;
/**
 *Å@ImagePairLoader class.
 */
public class ImagePairLoader {
	private static final Logger log = Logger.getLogger(ResourceManager.class.getName());
	/**
	 * ImagePairLoader class.
	 *
	 * Reads the left image to generate automatically the right one.
	 *
	 * @param name - Left image to be read.
	 * @param center - Coordinates of the image's center.
	 * @return - Returned image pair.
	 */
	public static ImagePair load(final String name, final Point center, final String packageName) throws IOException {

		// There seems to be problems flipping semi-transparent images
		// Workaround is to use shime1-r.png instead of shime1.png
		String rightName = name.replaceAll("\\.[a-zA-Z]+$", "-r$0");

		final BufferedImage leftImage = ImageIO.read(ResourceManager.getResourceAsStream(name, packageName));

		if(leftImage==null)
			log.log(Level.INFO, "ImageIO returned null image!");
		
		final BufferedImage rightImage;
		if ( ResourceManager.getResourceAsStream(rightName, packageName)==null ) {
			rightImage = flip(leftImage);
			log.log(Level.INFO, "flipped.");
		} else {
			log.log(Level.INFO, "Loading flipped image from file");
			rightImage = ImageIO.read(ResourceManager.getResourceAsStream(rightName, packageName));
		}

		return new ImagePair(new MascotImage(leftImage, center), new MascotImage(rightImage, new Point(rightImage
				.getWidth()
				- center.x, center.y)));
	}

	/**
	 * Flips the image left or right.
	 * @param src - The image to be flipped
	 * @returnÅ@- Returns the inverse image
	 */
	private static BufferedImage flip(final BufferedImage src) {
		log.log(Level.INFO, "Flipping image({0})", src);
		int w = src.getWidth();  
        int h = src.getHeight();  
		log.log(Level.INFO, "Flipping {0}x{1} image", new Object[]{w,h});
		final BufferedImage copy = new BufferedImage(w, h, src.getType());

        Graphics2D g = copy.createGraphics();  
        g.drawImage(src, 0, 0, w, h, w, 0, 0, h, null);  
        g.dispose();  
		return copy;
	}

}
