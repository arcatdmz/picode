package com.phybots.picode.ui.library.internal;

import java.applet.Applet;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

public class JMutableList<T> extends JList implements CellEditorListener {
	private static final long serialVersionUID = 6748911850025823661L;
	protected Component editorComp = null;
	protected int editingIndex = -1;
	protected ListCellEditor<T> editor = null;
	private PropertyChangeListener editorRemover = null;

	public JMutableList(ListModel dataModel) {
		super(dataModel);
		initialize();
	}

	private void initialize() {
		getActionMap().put("startEditing", new StartEditingAction());
		getActionMap().put("cancel", new CancelEditingAction());
		addMouseListener(new MouseListener());
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
				"startEditing");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}

	public void setListCellEditor(ListCellEditor<T> editor) {
		this.editor = editor;
	}

	public ListCellEditor<T> getListCellEditor() {
		return editor;
	}

	public boolean isEditing() {
		return (editorComp == null) ? false : true;
	}

	public Component getEditorComponent() {
		return editorComp;
	}

	public int getEditingIndex() {
		return editingIndex;
	}

	public Component prepareEditor(int index) {
		Object value = getModel().getElementAt(index);
		boolean isSelected = isSelectedIndex(index);
		Component comp = editor.getListCellEditorComponent(this, value,
				isSelected, index);
		comp.setFont(getFont());
		/*
		 * if (comp instanceof JComponent) { JComponent jComp = (JComponent)
		 * comp; if (jComp.getNextFocusableComponent() == null) {
		 * jComp.setNextFocusableComponent(this); } }
		 */
		return comp;
	}

	public void removeEditor() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removePropertyChangeListener("permanentFocusOwner",
						editorRemover);
		editorRemover = null;

		if (editor != null) {
			editor.removeCellEditorListener(this);

			if (editorComp != null) {
				remove(editorComp);
			}

			Rectangle cellRect = getCellBounds(editingIndex, editingIndex);

			editingIndex = -1;
			editorComp = null;

			repaint(cellRect);
		}
	}

	public boolean editCellAt(int index, EventObject e) {

		if (editor != null && !editor.stopCellEditing()) {
			return false;
		}

		if (index < 0 || index >= getModel().getSize()) {
			return false;
		}

		if (!isCellEditable(index)) {
			return false;
		}

		if (editorRemover == null) {
			KeyboardFocusManager fm = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();
			editorRemover = new CellEditorRemover(fm);
			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
		}

		if (editor != null && editor.isCellEditable(e)) {
			editorComp = prepareEditor(index);
			if (editorComp == null) {
				removeEditor();
				return false;
			}
			editorComp.setBounds(getCellBounds(index, index));
			add(editorComp);
			editorComp.validate();

			editingIndex = index;
			editor.addCellEditorListener(this);

			return true;
		}
		return false;
	}

	public void removeNotify() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removePropertyChangeListener("permanentFocusOwner",
						editorRemover);
		super.removeNotify();
	}

	// This class tracks changes in the keyboard focus state. It is used
	// when the XList is editing to determine when to cancel the edit.
	// If focus switches to a component outside of the XList, but in the
	// same window, this will cancel editing.
	class CellEditorRemover implements PropertyChangeListener {
		KeyboardFocusManager focusManager;

		public CellEditorRemover(KeyboardFocusManager fm) {
			this.focusManager = fm;
		}

		public void propertyChange(PropertyChangeEvent ev) {
			if (!isEditing()
					|| getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
				return;
			}

			Component c = focusManager.getPermanentFocusOwner();
			while (c != null) {
				if (c == JMutableList.this) {
					// focus remains inside the table
					return;
				} else if ((c instanceof Window)
						|| (c instanceof Applet && c.getParent() == null)) {
					if (c == SwingUtilities.getRoot(JMutableList.this)) {
						if (!getListCellEditor().stopCellEditing()) {
							getListCellEditor().cancelCellEditing();
						}
					}
					break;
				}
				c = c.getParent();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean isCellEditable(int index) {
		if (getModel() instanceof MutableListModel)
			return ((MutableListModel<T>) getModel()).isCellEditable(index);
		return false;
	}

	@SuppressWarnings("unchecked")
	public void setValueAt(T value, int index) {
		((MutableListModel<T>) getModel()).setElementAt(value, index);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getSelectedValue() {
		return (T) super.getSelectedValue();
	}

	public void editingStopped(ChangeEvent e) {
		if (editor != null) {
			T value = editor.getCellEditorValue();
			setValueAt(value, editingIndex);
			removeEditor();
		}
	}

	public void editingCanceled(ChangeEvent e) {
		removeEditor();
	}

	private static class StartEditingAction extends AbstractAction {
		private static final long serialVersionUID = -3422859310737238290L;

		public void actionPerformed(ActionEvent e) {
			JMutableList<?> list = (JMutableList<?>) e.getSource();
			if (!list.hasFocus()) {
				CellEditor cellEditor = list.getListCellEditor();
				if (cellEditor != null && !cellEditor.stopCellEditing()) {
					return;
				}
				list.requestFocus();
				return;
			}
			ListSelectionModel lsm = list.getSelectionModel();
			int anchorRow = lsm.getAnchorSelectionIndex();
			list.editCellAt(anchorRow, null);
			Component editorComp = list.getEditorComponent();
			if (editorComp != null) {
				editorComp.requestFocus();
			}
		}
	}

	private class CancelEditingAction extends AbstractAction {
		private static final long serialVersionUID = 8902524219720293514L;

		public void actionPerformed(ActionEvent e) {
			JMutableList<?> list = (JMutableList<?>) e.getSource();
			list.removeEditor();
		}

		public boolean isEnabled() {
			return isEditing();
		}
	}

	private class MouseListener extends MouseAdapter {
		private Component dispatchComponent;

		private void setDispatchComponent(MouseEvent e) {
			Component editorComponent = getEditorComponent();
			Point p = e.getPoint();
			Point p2 = SwingUtilities.convertPoint(JMutableList.this, p,
					editorComponent);
			dispatchComponent = SwingUtilities.getDeepestComponentAt(
					editorComponent, p2.x, p2.y);
		}

		private boolean repostEvent(MouseEvent e) {
			if (dispatchComponent == null || !isEditing()) {
				return false;
			}
			MouseEvent e2 = SwingUtilities.convertMouseEvent(JMutableList.this,
					e, dispatchComponent);
			dispatchComponent.dispatchEvent(e2);
			// TODO Here, the text field does not get its focus...
			return true;
		}

		private boolean shouldIgnore(MouseEvent e) {
			return e.isConsumed()
					|| (!(SwingUtilities.isLeftMouseButton(e) && isEnabled()));
		}

		public void mousePressed(MouseEvent e) {
			if (shouldIgnore(e)) {
				return;
			}

			int index = locationToIndex(e.getPoint());
			if (index == -1) {
				return;
			}

			if (editCellAt(index, e)) {
				setDispatchComponent(e);
				repostEvent(e);
			} else if (isRequestFocusEnabled()) {
				requestFocus();
			}
		}
	}
}