package com.phybots.picode.ui.editor;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import processing.app.SketchCode;

import com.phybots.picode.PicodeMain;
import com.phybots.picode.api.PoseLibrary;
import com.phybots.picode.ui.editor.Decoration.Type;

public class PicodeEditor extends JEditorPane {
	private static final long serialVersionUID = -6366895407636859766L;
	private static final Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private DocumentManager documentManager;
	private SketchCode code;
	private JScrollPane jScrollPane;

	public static Font getDefaultFont() {
		return defaultFont;
	}

	public PicodeEditor(PicodeMain picodeMain, SketchCode code) {
		this.code = code;
		documentManager = new DocumentManager(picodeMain, this);
		setFont(defaultFont);

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
					}
				}
			}
		});

		setDragEnabled(true);
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
}
