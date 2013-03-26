package com.phybots.picode.ui.editor;

import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import processing.app.SketchCode;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.PoseLibrary;
import com.phybots.picode.ui.editor.Decoration.Type;

public class PicodeEditor extends JEditorPane {
	private static final long serialVersionUID = -6366895407636859766L;
	private static final Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private PicodeMain picodeMain;
	private DocumentManager documentManager;
	private SketchCode code;
	private JScrollPane jScrollPane;

	public static Font getDefaultFont() {
		return defaultFont;
	}

	public PicodeEditor(PicodeMain picodeMain, SketchCode code) {
		this.picodeMain = picodeMain;
		this.code = code;
		documentManager = new DocumentManager(picodeMain, this);
		setFont(defaultFont);
		initializeListeners();
		setDragEnabled(true);
	}

	private void initializeListeners() {
		addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (!e.isControlDown()) {
					return;
				}
				switch (e.getKeyCode()) {
				case KeyEvent.VK_Z:
					documentManager.undo();
					e.consume();
					break;
				case KeyEvent.VK_Y:
					documentManager.redo();
					e.consume();
					break;
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});
		addPropertyChangeListener("dropLocation",
				new PropertyChangeListener() {
			private int x, y, lastX, lastY;
			public void propertyChange(PropertyChangeEvent evt) {
				Point p = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(p, PicodeEditor.this);
				x = p.x; y = p.y;
				if (lastX != x || lastY != y) {
//					int index = viewToModel(p);
//					System.out.println(
//							String.format(
//									"dnd: location update -> [%d, %d] (%d)",
//									x, y, index));
					lastX = x; lastY = y;
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = viewToModel(e.getPoint());
					Decoration decoration = documentManager.getDecoration(index);
					if (decoration != null && decoration.getType() == Type.POSE) {
						System.out.println(decoration.getOption());
						String poseName = (String) decoration.getOption();
						PoseLibrary poseLibrary = PoseLibrary.getInstance();
						poseLibrary.get(poseName);
						// TODO Handle double-click of the pose image
					}
				}
			}
		});
	}

	public void setOuterScrollPane(JScrollPane jScrollPane) {
		this.jScrollPane = jScrollPane;
	}

	public JScrollPane getOuterScrollPane() {
		return jScrollPane;
	}

	public SketchCode getCode() {
		return code;
	}

	public DocumentManager getDocumentManager() {
		return documentManager;
	}

}
