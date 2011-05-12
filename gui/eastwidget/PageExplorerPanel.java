package eastwidget;

import gui.MainPanel;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.Document;
import core.Page;
import core.Parameters;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
//import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

/**
 * The page tree that will appear on the
 * page explorer panel.
 * 
 * @author Stelios
 *
 */
public class PageExplorerPanel extends JPanel {

    /****************************************
     *
     * Private Instance Variables
     *
     ****************************************/
    /**
     * The document tree.
     */
    private JList pageList;
    /**
     * The list scroller.
     */
    private JScrollPane listScroller;
    /**
     * Reference to the Main Panel.
     */
    private MainPanel mainPanel;

    private int indexFrom;
    private int indexTo;

    /****************************************
     *
     * Constructor(s)
     *
     ****************************************/
    /**
     * Constructor.
     */
    public PageExplorerPanel(MainPanel mainPanel) {

        this.mainPanel = mainPanel;
        
        Parameters.setPageExplorerPanel(this);

        this.pageList = new JList(this.getPageNames());

        this.pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.pageList.getSelectionModel().addListSelectionListener(new SelectionListener());
        this.pageList.setLayoutOrientation(JList.VERTICAL);
        this.pageList.addKeyListener(new deleteListener());
        this.pageList.addMouseListener(new MouseMotion());

        // for DnD
//        this.pageList.setDropMode(DropMode.INSERT);
//        this.pageList.setDragEnabled(true);
//        this.pageList.setTransferHandler(new TH());
//        this.pageList.setVisibleRowCount(-1); 


        this.listScroller = new JScrollPane(this.pageList);
        this.listScroller.setPreferredSize(new Dimension(150, 600));
        this.add(this.listScroller);
    }

    /****************************************
     *
     * Public Methods
     *
     ****************************************/
    /**
     * Updates the Page Panel. To be called
     * when the working document has changed.
     */
    public void update() {
        this.pageList.setListData(this.getPageNames());
        this.listScroller.revalidate();
        this.pageList.setSelectedIndex(0);
    }

    /**
     * Sets the page order.
     *
     * @param order The page order to set
     */
    public void setPageOrder(int order) {
        this.pageList.setSelectedIndex(order);
        this.mainPanel.updateCentralPanels(false);
    }
    
	
	public void incrementIndex() {
		int order = this.pageList.getSelectedIndex();
		order++;
		this.pageList.setSelectedIndex(order);
	}
	
	public void decrementIndex() {
		int order = this.pageList.getSelectedIndex();
		order--;
		this.pageList.setSelectedIndex(order);
	}

    /****************************************
     *
     * Private Methods
     *
     ****************************************/
    /**
     * Given the path to the workspace, it returns
     * a vector of of all the Documents in that directory.
     *
     * @param workspacePath The path to the workspace
     */
    private Vector<String> getPageNames() {
        Vector<String> pages = new Vector<String>();

        Document workingDoc = Parameters.getCoreManager().workingDocument();

        if (workingDoc != null) {

            for (Page page : workingDoc.pages()) {
                //model.addElement(Integer.toString(page.order()));
                pages.add(page.order()+": "+page.name());
            }
        }

        return pages;
    }


    /****************************************
     *
     * Private Classes
     *
     ****************************************/
    /**
     * The ActionListener class for selecting items
     * on the doc list.
     */
    private class SelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {

                // Serialize the previous page
                if (Parameters.getCoreManager().getWorkingPage() != null) {
                    try {
                        Parameters.getCoreManager().getWorkingPage().serialize();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                // Get the current page and draw it on the panel.
                String currPageName = (String) pageList.getSelectedValue();
                if (currPageName != null) {
                    System.out.println("CurrPageName="+currPageName);
                    Page currPage = Parameters.getCoreManager().getWorkingDocPageFromName(Integer.parseInt(currPageName.substring(0, 1)));

                    try {
                        Parameters.getCoreManager().setWorkingPageAndImage(currPage);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    mainPanel.updateCentralPanels(false);
                }
            }
        }
    }

    private class deleteListener implements KeyListener {

        public void keyPressed(java.awt.event.KeyEvent evt) {

            if (evt.getKeyCode() == 8) {
                int index = pageList.getSelectedIndex() + 1;
                Document d = Parameters.getCoreManager().workingDocument();
                int selected = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete page " + index + "?", "Delete Document", JOptionPane.OK_CANCEL_OPTION);
                if (selected == JOptionPane.OK_OPTION) {

                    try {
                        Parameters.getCoreManager().deletePage(d, index);
                        update();
                        mainPanel.updateCentralPanels(false);
                        Parameters.getDocExpPanel().update();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Delete Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        }

        public void keyTyped(KeyEvent ke) {}

        public void keyReleased(KeyEvent ke) {}
    }

   private class MouseMotion implements MouseListener {

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount() == 2) {
                int index = pageList.locationToIndex(evt.getPoint())+1;
                String input = JOptionPane.showInputDialog(null, "Enter new Page name: ", "Rename Page", 1);
                if(input != null){
                    try {
                        Parameters.getCoreManager().renamePage(Parameters.getCoreManager().workingDocument(),index, input);
                        update();
                        setPageOrder(index-1);

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Rename Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
       }
        

        public void mousePressed(java.awt.event.MouseEvent evt) {}
        public void mouseReleased(java.awt.event.MouseEvent evt) {}
        public void mouseEntered(MouseEvent me) {}
        public void mouseExited(MouseEvent me) {}
    }

    /**
     * Class to support drag and dropping for reordering pages
     *
     */
//    protected class TH extends TransferHandler {
//
//        public boolean canImport(TransferHandler.TransferSupport info) {
//            // we only import Strings
//            if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//                return false;
//            }
//
//            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
//            if (dl.getIndex() == -1) {
//                System.out.println("INDEX = -1");
//                //return false;
//            }
//            System.out.println("Can Import!");
//            return true;
//        }
//
//        public boolean importData(TransferHandler.TransferSupport info) {
//            if (!info.isDrop()) {
//                return false;
//            }
//
//            // Check for String flavor
//            if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//                return false;
//            }
//
//            System.out.println("Creating transferable");
//
//            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
//            //DefaultListModel listModel = (DefaultListModel) pageList.getModel();
//            int index = dl.getIndex();
//            Document d = Parameters.getCoreManager().workingDocument();
//            indexTo = index;
//            String value2 = (String) pageList.getModel().getElementAt(dl.getIndex());
//            indexFrom = Integer.parseInt(value2);
//            Page p = Parameters.getCoreManager().getPageFromOrder(d, indexFrom+1);
//            boolean insert = dl.isInsert();
//            // Get the current string under the drop.
//            String value = (String) pageList.getModel().getElementAt(index);//listModel.getElementAt(index);
//
//            // Get the string that is being dropped.
//            Transferable t = info.getTransferable();
//            String data;
//            try {
//                data = (String) t.getTransferData(DataFlavor.stringFlavor);
//            } catch (Exception e) {
//                System.out.println("In exception!");
//                return false;
//            }
//            
//            System.out.println("Created transferable");
//
//            String value1 = (String) pageList.getModel().getElementAt(dl.getIndex() - 1);
//
//            // Display a dialog with the drop information.
//            //String dropValue = "\"" + data + "\" dropped ";
//            /*if (dl.isInsert()) {
//                if (dl.getIndex() == 0) {
//                    displayDropLocation(dropValue + "at beginning of list");
//                } else if (dl.getIndex() >= pageList.getModel().getSize()) {
//                    displayDropLocation(dropValue + "at end of list");
//                } else {
//                    String value1 = (String) pageList.getModel().getElementAt(dl.getIndex() - 1);
//                    String value2 = (String) pageList.getModel().getElementAt(dl.getIndex());
//                    displayDropLocation(dropValue + "between \"" + value1 + "\" and \"" + value2 + "\"");
//                }
//            } else {
//                displayDropLocation(dropValue + "on top of " + "\"" + value + "\"");
//            }*/
//
//            /**  This is commented out for the basicdemo.html tutorial page.
//             **  If you add this code snippet back and delete the
//             **  "return false;" line, the list will accept drops
//             **  of type string.*/
//            // Perform the actual import (rearrange List of Pages).
//            if (insert) {
//                try {
//                    System.out.println("Insert");
//                    Parameters.getCoreManager().reorderPage(d, p, indexTo + 1);
//                } catch (IOException ex) {
//                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Reordering Error", JOptionPane.ERROR_MESSAGE);
//                }
//            } else {
//                System.out.println("Not insert");
//                //Parameters.getCoreManager().reorderPage(d, p, Integer.parseInt(value2));
//            }
//            return true;
//        }
//
//        public int getSourceActions(JComponent c) {
//            return MOVE;
//        }
//
//        protected Transferable createTransferable(JComponent c) {
//            JList list = (JList) c;
//            Object[] values = list.getSelectedValues();
//
//            System.out.println("in transferable: "+list.getSelectedIndex());
//            indexTo = list.getSelectedIndex();
//            StringBuffer buff = new StringBuffer();
//
//            for (int i = 0; i < values.length; i++) {
//                Object val = values[i];
//                buff.append(val == null ? "" : val.toString());
//                if (i != values.length - 1) {
//                    buff.append("\n");
//                }
//            }
//            System.out.println(buff.toString());
//            return new StringSelection("Transferable: "+buff.toString());
//        }
//
//        @Override
//        protected void exportDone(JComponent c, Transferable t, int action) {
//            if (action == MOVE) {
//                System.out.println("*****MOVE****");
//                System.out.println("FROM: "+indexFrom+", TO: "+indexTo);
//                update();
//            }
//        }
//    }

}
