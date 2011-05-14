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
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
//import javax.swing.DropMode;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
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
    DefaultListModel model;

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

        model = new DefaultListModel();

        //this.pageList = new JList(this.getPageNames());

        this.pageList = new JList(model);
        update();

        this.pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.pageList.getSelectionModel().addListSelectionListener(new SelectionListener());
        this.pageList.setLayoutOrientation(JList.VERTICAL);
        this.pageList.addKeyListener(new deleteListener());
        this.pageList.addMouseListener(new MouseMotion());

        // added for DnD
        this.pageList.setTransferHandler(new ToTransferHandler());
        this.pageList.setDropMode(DropMode.INSERT);
        this.pageList.setDragEnabled(true);
        this.pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        //this.pageList.setListData(this.getPageNames());
        model.removeAllElements();
        getPageNames();
//        this.listScroller.revalidate();
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
                model.addElement(page.order()+": "+page.name());
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
    * For reordering Pages
    */

    class ToTransferHandler extends TransferHandler {
        int action = TransferHandler.MOVE;

        public int getSourceActions(JComponent comp) {
            return TransferHandler.MOVE;
        }

        private int index = 0;

        public Transferable createTransferable(JComponent comp) {
            index = pageList.getSelectedIndex();
            indexFrom = index+1;
            System.err.println("****INDEX From: "+indexFrom);
            if (index < 0 || index >= model.getSize()) {
                return null;
            }

            return new StringSelection((String)pageList.getSelectedValue());
        }

        public void exportDone(JComponent comp, Transferable trans, int action) {
            //model.removeElementAt(index);
            Document d = Parameters.getCoreManager().workingDocument();
            Page p = Parameters.getCoreManager().getPageFromOrder(d, indexFrom);
            try {
                Parameters.getCoreManager().reorderPage(d, p, indexTo);
            } catch (IOException ex) {
                Logger.getLogger(PageExplorerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            update();
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            // for the demo, we'll only support drops (not clipboard paste)
            if (!support.isDrop()) {
                return false;
            }

            // we only import Strings
            if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            boolean actionSupported = (action & support.getSourceDropActions()) == action;
            if (actionSupported) {
                support.setDropAction(action);
                return true;
            }

            return false;
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            // if we can't handle the import, say so
            if (!canImport(support)) {
                return false;
            }

            // fetch the drop location
            JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();

            int index = dl.getIndex();
            indexTo = index;
            System.err.println("****INDEX To: "+indexTo);
            // fetch the data and bail if this fails
            String data;
            try {
                data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (java.io.IOException e) {
                return false;
            }

            JList list = (JList)support.getComponent();
            DefaultListModel model = (DefaultListModel)list.getModel();
            model.insertElementAt(data, index);

            Rectangle rect = list.getCellBounds(index, index);
            list.scrollRectToVisible(rect);
            list.setSelectedIndex(index);
            list.requestFocusInWindow();

            return true;
        }
    }

}
