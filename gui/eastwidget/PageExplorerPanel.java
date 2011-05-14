package eastwidget;

import gui.MainPanel;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.Document;
import core.Page;
import core.Parameters;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JOptionPane;

/**
 * The page tree that will appear on the
 * page explorer panel.
 * 
 * @author Stelios
 *
 */
@SuppressWarnings("serial")
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
                //model.addElement(page.order()+": "+page.name());
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

}
