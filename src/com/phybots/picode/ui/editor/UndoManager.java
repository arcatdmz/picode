package com.phybots.picode.ui.editor;

import java.util.LinkedList;

import javax.swing.text.BadLocationException;

public class UndoManager {
	private DocumentManager documentManager;
	private LinkedList<Edit> edits;
	private int pointer;

	public UndoManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
		edits = new LinkedList<Edit>();
		pointer = -1;
	}

	void registerEdit(Edit edit) {
		while (canRedo()) {
			edits.removeLast();
		}
		edits.addLast(edit);
		pointer ++;
		System.out.print("--- reg:");
		System.out.println(edit.type);
		System.out.println(edit.offset);
		System.out.println(edit.beforeText);
		System.out.println(edit.afterText);
	}

	public boolean canUndo() {
		return pointer >= 0;
	}

	public void undo() {
		if (!canUndo()) {
			return;
		}
		documentManager.doc.removeDocumentListener(documentManager);
		Edit edit = edits.get(pointer);
		try {
			switch (edit.type) {
			case INSERT:
				documentManager.doc.remove(
						edit.offset,
						edit.afterText.length());
				break;
			case REMOVE:
				documentManager.doc.insertString(
						edit.offset,
						edit.beforeText,
						DocumentManager.defaultAttrs);
				break;
			case REPLACE:
				documentManager.doc.remove(
						edit.offset,
						edit.afterText.length());
				documentManager.doc.insertString(
						edit.offset,
						edit.beforeText,
						DocumentManager.defaultAttrs);
				break;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		documentManager.doc.addDocumentListener(documentManager);
		pointer --;
		documentManager.syncCode();
		documentManager.update();
	}

	public boolean canRedo() {
		return edits.size() > pointer + 1;
	}

	public void redo() {
		if (!canRedo()) {
			return;
		}
		pointer ++;
		documentManager.doc.removeDocumentListener(documentManager);
		Edit edit = edits.get(pointer);
		try {
			switch (edit.type) {
			case INSERT:
				documentManager.doc.insertString(
						edit.offset,
						edit.afterText,
						DocumentManager.defaultAttrs);
				break;
			case REMOVE:
				documentManager.doc.remove(
						edit.offset,
						edit.beforeText.length());
				break;
			case REPLACE:
				documentManager.doc.remove(
						edit.offset,
						edit.beforeText.length());
				documentManager.doc.insertString(
						edit.offset,
						edit.afterText,
						DocumentManager.defaultAttrs);
				break;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		documentManager.doc.addDocumentListener(documentManager);
		documentManager.syncCode();
		documentManager.update();
	}

	public static enum EditType {
		INSERT, REMOVE, REPLACE
	}

	public static class Edit {
		EditType type;
		int offset;
		String beforeText;
		String afterText;
	}
}
