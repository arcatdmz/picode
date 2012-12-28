package jp.digitalmuseum.roboko.ui.editor;

import javax.swing.JScrollPane;

public class RobokoEditorPane extends JScrollPane {
	private static final long serialVersionUID = 2560377694389172816L;
	private RobokoEditor robokoEditor;

	public RobokoEditorPane(RobokoEditor robokoEditor) {
		this.robokoEditor = robokoEditor;
		setViewportView(robokoEditor);
	}

	public RobokoEditor getRobokoEditor() {
		return robokoEditor;
	}
}
