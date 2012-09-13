/**Shimeji-ie*/

package com.group_finity.mascot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.group_finity.mascot.config.Settings;
import com.group_finity.mascot.exception.CantBeAliveException;

public class MascotEventHandler implements MouseListener {

	private static final Logger log = Logger.getLogger(MascotEventHandler.class.getName());

	/**
	 * �g���C�A�C�R���̕��̃��j���[���\�����邩�ǂ���.
	 * �g���C�A�C�R���̍쐬�Ɏ��s�������͂����Ńg���C�A�C�R���̕��̃��j���[���\������K�v������.
	 */
	private static boolean showSystemTrayMenu = false;

	public static void setShowSystemTrayMenu(boolean showSystemTrayMenu) {
		MascotEventHandler.showSystemTrayMenu = showSystemTrayMenu;
	}

	public static boolean isShowSystemTrayMenu() {
		return showSystemTrayMenu;
	}

	private final Mascot mascot;

	public MascotEventHandler(Mascot mascot) {
		this.mascot = mascot;
	}

	public void mousePressed(final MouseEvent event) {

		// �}�E�X�������ꂽ��h���b�O�A�j���[�V�����ɐ؂�ւ���
		if (getMascot().getBehavior() != null) {
			try {
				getMascot().getBehavior().mousePressed(event);
			} catch (final CantBeAliveException e) {
				log.log(Level.SEVERE, "���������邱�Ƃ��o���Ȃ���", e);
				getMascot().dispose();
			}
		}

	}

	public void mouseReleased(final MouseEvent event) {

		if (event.isPopupTrigger()) {
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					showPopup(event.getX(), event.getY());
				}
			});
		} else {
			if (getMascot().getBehavior() != null) {
				try {
					getMascot().getBehavior().mouseReleased(event);
				} catch (final CantBeAliveException e) {
					log.log(Level.SEVERE, "���������邱�Ƃ��o���Ȃ���", e);
					getMascot().dispose();
				}
			}
		}

	}

	private void showPopup(final int x, final int y) {
		final JPopupMenu popup = new JPopupMenu();

		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				getMascot().setAnimating(true);
			}

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
				getMascot().setAnimating(false);
			}
		});

		final JMenuItem disposeMenu = new JMenuItem(Settings.getString("shimeji.gui.bye_bye","Bye Bye!"));
		disposeMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				getMascot().dispose();
			}
		});

		popup.add(disposeMenu);

		if (MascotEventHandler.isShowSystemTrayMenu()) {

			popup.add(new JSeparator());

			// �u���₷�v���j���[�A�C�e��
			final JMenuItem increaseMenu = new JMenuItem(Settings.getString("shimeji.gui.one_more","One more!"));
			increaseMenu.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					Main.getInstance().createMascot();
				}
			});

			// �u���܂�I�v���j���[�A�C�e��
			final JMenuItem gatherMenu = new JMenuItem(Settings.getString("shimeji.gui.follow_cursor","Follow cursor!"));
			gatherMenu.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					getMascot().getManager().setBehaviorAll(Main.getInstance().getConfiguration(getMascot().getPackageName()), Main.BEHAVIOR_GATHER);
				}
			});

			// �u��C�����c���v���j���[�A�C�e��
			final JMenuItem oneMenu = new JMenuItem(Settings.getString("shimeji.gui.gather_one","Reduce to one!"));
			oneMenu.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					getMascot().getManager().remainOne();
				}
			});

			// �uIE�����ɖ߂��v���j���[�A�C�e��
			final JMenuItem restoreMenu = new JMenuItem(Settings.getString("shimeji.gui.restore_ie","Restore IE!"));
			restoreMenu.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					NativeFactory.getInstance().getEnvironment().restoreIE();
				}
			});

			// �u�S���΂��΂��v���j���[�A�C�e��
			final JMenuItem closeMenu = new JMenuItem(Settings.getString("shimeji.gui.bye_bye","Bye Bye!"));
			closeMenu.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					Main.getInstance().exit();
				}
			});

			popup.add(increaseMenu);
			popup.add(gatherMenu);
			popup.add(oneMenu);
			popup.add(restoreMenu);
			popup.add(new JSeparator());
			popup.add(closeMenu);
		}

		popup.show(getMascot().getWindow().asJWindow(), x, y);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	private Mascot getMascot() {
		return mascot;
	}

}
