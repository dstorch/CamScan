/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eastwidget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;

public class ReorderableJList extends JList
        implements DragSourceListener, DropTargetListener, DragGestureListener {

    static DataFlavor localObjectFlavor;
    private PageExplorerPanel panel;
    private int indexToHighlight;

    static {
        try {
            localObjectFlavor =
                    new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
    static DataFlavor[] supportedFlavors = {localObjectFlavor};
    DragSource dragSource;
    DropTarget dropTarget;
    Object dropTargetCell;
    int draggedIndex = -1;

    public ReorderableJList(DefaultListModel model, PageExplorerPanel p) {
        super();
        panel = p;
        this.setModel(model);
        setCellRenderer((ListCellRenderer) new ReorderableListCellRenderer());
        dragSource = new DragSource();
        DragGestureRecognizer dgr =
                dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE,
                this);
        dropTarget = new DropTarget(this, this);
    }

    // DragGestureListener
    public void dragGestureRecognized(DragGestureEvent dge) {
        // find object at this x,y
        Point clickPoint = dge.getDragOrigin();
        int index = locationToIndex(clickPoint);
        if (index == -1) {
            return;
        }
        Object target = getModel().getElementAt(index);
        Transferable trans = new RJLTransferable(target);
        draggedIndex = index;
        dragSource.startDrag(dge, Cursor.getDefaultCursor(),
                trans, this);
    }
    // DragSourceListener events

    public void dragDropEnd(DragSourceDropEvent dsde) {
        dropTargetCell = null;
        draggedIndex = -1;
        repaint();
        
        try {
            panel.reOrderPages(indexToHighlight);
        } catch (IOException ex) {
            Logger.getLogger(ReorderableJList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }
    // DropTargetListener events

    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
        // figure out which cell it's over, no drag to self
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrag();
        }
        Point dragPoint = dtde.getLocation();
        int index = locationToIndex(dragPoint);
        if (index == -1) {
            dropTargetCell = null;
        } else {
            dropTargetCell = getModel().getElementAt(index);
        }
        repaint();
    }

    public void drop(DropTargetDropEvent dtde) {
        if (dtde.getSource() != dropTarget) {
            dtde.rejectDrop();
            return;
        }
        Point dropPoint = dtde.getLocation();
        int index = locationToIndex(dropPoint);
        boolean dropped = false;
        try {
            if ((index == -1) || (index == draggedIndex)) {
                dtde.rejectDrop();
                return;
            }
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            Object dragged =
                    dtde.getTransferable().getTransferData(localObjectFlavor);
            // move items - note that indicies for insert will
            // change if [removed] source was before target
            boolean sourceBeforeTarget = (draggedIndex < index);
            DefaultListModel mod = (DefaultListModel) getModel();
            mod.remove(draggedIndex);
            mod.add((sourceBeforeTarget ? index - 1 : index), dragged);
            dropped = true;
            indexToHighlight = index-1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        dtde.dropComplete(dropped);
    }

    class RJLTransferable implements Transferable {

        Object object;

        public RJLTransferable(Object o) {
            object = o;
        }

        public Object getTransferData(DataFlavor df)
                throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(df)) {
                return object;
            } else {
                throw new UnsupportedFlavorException(df);
            }
        }

        public boolean isDataFlavorSupported(DataFlavor df) {
            return (df.equals(localObjectFlavor));
        }

        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavors;
        }
    }

    class ReorderableListCellRenderer
            extends DefaultListCellRenderer {

        boolean isTargetCell;
        boolean isLastItem;

        public ReorderableListCellRenderer() {
            super();
        }

        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected, boolean hasFocus) {
            isTargetCell = (value == dropTargetCell);
            isLastItem = (index == list.getModel().getSize() - 1);
            boolean showSelected = isSelected
                    & (dropTargetCell == null);
            return super.getListCellRendererComponent(list, value,
                    index, showSelected,
                    hasFocus);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isTargetCell) {
                g.setColor(Color.black);
                g.drawLine(0, 0, getSize().width, 0);
            }
        }
    }

}
