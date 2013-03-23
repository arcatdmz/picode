package com.phybots.picode.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;

/**
 * <a href="http://terai.xrea.jp/Swing/WaitCursor.html">Swing/WaitCursor</a>
 */
public class LockingGlassPane extends JComponent {
	private static final long serialVersionUID = -5849931332134848601L;

	public LockingGlassPane() {
		this(true);
	}

	public LockingGlassPane(boolean cursor) {
		setOpaque(false);
		setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
			private static final long serialVersionUID = -1604235471535054871L;
			@Override
			public boolean accept(Component c) {
				return false;
			}
		});
		addKeyListener(new KeyAdapter() {});
		addMouseListener(new MouseAdapter() {});
		requestFocusInWindow();
		if (cursor) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}

	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		setFocusTraversalPolicyProvider(flag);
	}
}