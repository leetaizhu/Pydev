package org.python.pydev.refactoring.ui.model.constructorfield;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.python.pydev.refactoring.ast.adapters.ClassDefAdapter;
import org.python.pydev.refactoring.ui.model.tree.ITreeNode;

public class ClassFieldTreeProvider implements ITreeContentProvider {

	private ClassDefAdapter rootClass;

	public ClassFieldTreeProvider(ClassDefAdapter rootClass) {
		this.rootClass = rootClass;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITreeNode) {
			return ((ITreeNode) parentElement).getChildren();
		}
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ITreeNode) {
			ITreeNode node = (ITreeNode) element;
			return node.hasChildren();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return new Object[] { new TreeNodeClassField(rootClass) };
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
