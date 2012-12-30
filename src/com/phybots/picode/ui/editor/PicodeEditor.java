package com.phybots.picode.ui.editor;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JEditorPane;

import processing.app.SketchCode;

import com.phybots.picode.Pose;
import com.phybots.picode.ui.PicodeMain;
import com.phybots.picode.ui.editor.Decoration.Type;

public class PicodeEditor extends JEditorPane {
	private static final long serialVersionUID = -6366895407636859766L;
	private static final Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 21);
	private PicodeMain picodeMain;
	private DocumentManager documentManager;
	private SketchCode code;

	public static Font getDefaultFont() {
		return defaultFont;
	}

	public PicodeEditor(PicodeMain picodeMain, SketchCode code) {

		this.picodeMain = picodeMain;
		this.code = code;
		documentManager = new DocumentManager(picodeMain, this);
		setFont(defaultFont);

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = viewToModel(e.getPoint());
					Decoration decoration = documentManager.getDecoration(index);
					if (decoration != null &&
							decoration.getType() == Type.POSE) {
						PicodeMain picodeMain = PicodeEditor.this.picodeMain;
						String poseName = (String) decoration.getOption();
						Pose pose = picodeMain.getPoseManager().get(poseName);
						picodeMain.getFrame().editPoseName(pose);
						System.out.println(decoration.getOption());
					}
					/*
					Element element = ((StyledDocument) getDocument()).getCharacterElement(index);
					System.out.println(String.format("%s (%s) at %d",
							element.getClass(),
							element.getName(),
							element.getStartOffset()));
					dumpElementAttributes(element);
					*/
				}
			}
		});

		setDragEnabled(true);
	}

	/*
	private void dumpElementAttributes(Element elem) {
		AttributeSet attrs = elem.getAttributes();
		Enumeration<?> names = attrs.getAttributeNames();
		while (names.hasMoreElements()) {
			Object key = names.nextElement();
			System.out.println("\t" + key + " : " + attrs.getAttribute(key));
		}
	}
	*/

	public SketchCode getCode() {
		return code;
	}
}
