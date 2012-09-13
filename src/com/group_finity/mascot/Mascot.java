/**Shimeji-ie*/

package com.group_finity.mascot;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.config.Settings;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.image.TranslucentWindow;

/**
 * Mascot class object.
 *
 * A mascot represents the long-term and complex behavior {@link Behavior} and
 * an action {@link Action} represents a short-term monotic gesture.
 *
 * The mascot has an internal timer, which calls {@link Action} at a constant interval.
 * {@link Action} is executed by calling the method {@link #animate(Point, MascotImage, boolean)}
 * to animate the mascot.
 *
 * Once a {@link Action} has finished, following the {@link Behavior}  order, the next {@link Action} is called.
 *
 */
public class Mascot {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(Mascot.class.getName());

	/**
	 * Generated ID of the last mascot.
	 */
	private static AtomicInteger lastId = new AtomicInteger();

	/**
	 * Current ID of this mascot.
	 * It exists to make the log easier to read for debugging purposes.
	 */
	private final int id;

	/**
	 * Window that displays the mascot.
	 */
	private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

	/**
	 * Manager that manages the mascot.
	 */
	private Manager manager = null;

	/**
	 * Coordinates of the mascot.
	 * For example it points the feet, or hands, or such as when you you are hanging it.
	 * It becomes the center of the image displayed.
	 */
	private Point anchor = new Point(0, 0);

	/**
	 * Image to be displayed.
	 */
	private MascotImage image = null;

	/**
	 * Whether is looking right.
	 * Drawn by flipping the original image treated as left.
	 */
	private boolean lookRight = false;

	/**
	 * Object representing the long-term behavior.
	 */
	private Behavior behavior = null;

	/**
	 * Time increased at every tick of the timer.
	 */
	private int time = 0;

	/**
	 * Whether an animation is running.
	 */
	private boolean animating = true;

	/**
	 * Display enviroment of the mascot.
	 */
	private MascotEnvironment environment = new MascotEnvironment(this);
	
	/**
	* Package name this mascot belongs to
	*/
	private String packageName = null;
	
	public Mascot() {
		this(Settings.getString("shimeji.cfg.default_mascot","default"));
	}

	public Mascot(String packageName)
	{
		this.id = lastId.incrementAndGet();
		this.packageName = packageName;

		log.log(Level.INFO, "Mascot constructor({0})", this);

		// Better always on top
		getWindow().asJWindow().setAlwaysOnTop(true);

		// Register mouse handler
		getWindow().asJWindow().addMouseListener(new MascotEventHandler(this));
	}
	
	@Override
	public String toString() {
		return "マスコット" + this.id;
	}

	void tick() {
		if (isAnimating()) {
			if (getBehavior() != null) {

				try {
					getBehavior().next();
				} catch (final CantBeAliveException e) {
					log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
					dispose();
				}

				setTime(getTime() + 1);
			}
		}
	}

	public void apply() {
		if (isAnimating()) {

			// 表示できる画像が無ければ何も出来ない
			if (getImage() != null) {

				// ウィンドウの領域を設定
				getWindow().asJWindow().setBounds(getBounds());

				// 画像を設定
				getWindow().setImage(getImage().getImage());

				// 表示
				if (!getWindow().asJWindow().isVisible()) {
					getWindow().asJWindow().setVisible(true);
				}

				// 再描画
				getWindow().updateImage();
			} else {
				if (getWindow().asJWindow().isVisible()) {
					getWindow().asJWindow().setVisible(false);
				}
			}
		}
	}

	public void dispose() {
		log.log(Level.INFO, "マスコット破棄({0})", this);

		getWindow().asJWindow().dispose();
		if (getManager() != null) {
			getManager().remove(Mascot.this);
		}
	}

	public Manager getManager() {
		return this.manager;
	}

	public void setManager(final Manager manager) {
		this.manager = manager;
	}

	public Point getAnchor() {
		return this.anchor;
	}

	public void setAnchor(final Point anchor) {
		this.anchor = anchor;
	}

	public MascotImage getImage() {
		return this.image;
	}

	public void setImage(final MascotImage image) {
		this.image = image;
	}

	public boolean isLookRight() {
		return this.lookRight;
	}

	public void setLookRight(final boolean lookRight) {
		this.lookRight = lookRight;
	}

	public Rectangle getBounds() {

		// 接地座標と画像の中心座標からウィンドウの領域を求める.
		final int top = getAnchor().y - getImage().getCenter().y;
		final int left = getAnchor().x - getImage().getCenter().x;

		final Rectangle result = new Rectangle(left, top, getImage().getSize().width, getImage().getSize().height);

		return result;
	}

	public int getTime() {
		return this.time;
	}

	private void setTime(final int time) {
		this.time = time;
	}

	public Behavior getBehavior() {
		return this.behavior;
	}

	public void setBehavior(final Behavior behavior) throws CantBeAliveException {
		this.behavior = behavior;
		this.behavior.init(this);
	}

	public int getTotalCount() {
		return getManager().getCount();
	}

	private boolean isAnimating() {
		return this.animating;
	}

	void setAnimating(final boolean animating) {
		this.animating = animating;
	}

	TranslucentWindow getWindow() {
		return this.window;
	}

	public MascotEnvironment getEnvironment() {
		return environment;
	}
	
	public String getPackageName()
	{
                return packageName;
    }      
}
