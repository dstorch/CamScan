package westwidget;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import centralwidget.CentralPanel;

import core.Document;
import core.Mode;
import core.Page;
import core.Parameters;

import eastwidget.PageExplorerPanel;
import gui.ParamHolder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JOptionPane;

/**
 * The document tree that will appear on the
 * document explorer panel.
 * 
 * @author Stelios
 *
 */
public class DocExplorerPanel extends JPanel {

    /****************************************
     *
     * Private Instance Variables
     *
     ****************************************/
    /**
     * The document tree.
     */
    private JList docList;
    /**
     * The list scroller.
     */
    private JScrollPane listScroller;
    /**
     * Reference to the page explorer panel.
     */
    private PageExplorerPanel pageExpPanel;
    /**
     * Reference to the Central Panel.
     */
    private CentralPanel centralPanel;
    /**
     * Used for merging
     */
    private int fromDrag;

    /****************************************
     *
     * Constructor(s)
     *
     ****************************************/
    /**
     * Constructor.
     */
    public DocExplorerPanel(PageExplorerPanel pageExpPanel, CentralPanel centralPanel) {
        super();
        Parameters.setDocExplorerPanel(this);
        this.centralPanel = centralPanel;

        this.pageExpPanel = pageExpPanel;

        this.docList = new JList(this.getDocumentNames());

        this.docList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.docList.getSelectionModel().addListSelectionListener(new SelectionListener());
        this.docList.setLayoutOrientation(JList.VERTICAL);
        this.docList.setSelectedIndex(0);

        this.docList.addMouseListener(new MouseMotion());
        this.docList.addKeyListener(new deleteListener());

        this.listScroller = new JScrollPane(this.docList);
        this.listScroller.setPreferredSize(new Dimension(150, 600));
        this.add(this.listScroller);
    }

    /****************************************
     *
     * Public Methods
     *
     ****************************************/
    /**
     * Updates the Document Panel. To be called
     * when files have been added or removed in the workspace.
     */
    public void update() {
        this.docList.setListData(this.getDocumentNames());
        this.listScroller.revalidate();
    }

    /**
     * Sets the page order.
     *
     * @param order The page order to set
     */
    public void setDocOrder(String docName) {

        int order = -1;
        Vector<String> docNames = this.getDocumentNames();

        for (int i = 0; i < docNames.size(); i++) {
            if (docNames.get(i).equals(docName)) {
                order = i;
            }
        }

        this.docList.setSelectedIndex(order);
        this.centralPanel.updatePanels(false);
    }

    /****************************************
     *
     * Private Methods
     *
     ****************************************/
    /**
     * Returns a vector of of all the document names
     * in the workspace.
     *
     * @return Vector of all the document names in the
     * workspace
     */
    private Vector<String> getDocumentNames() {
        Vector<String> docs = new Vector<String>();

        for (Document doc : Parameters.getCoreManager().getDocuments()) {
            docs.add(doc.name());
        }

        return docs;
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

                // Get the current selection and set it as the working document.
                String currDocName = (String) docList.getSelectedValue();

                if (currDocName != null) {

	                try {
	                	System.out.println(currDocName);
	                    Parameters.getCoreManager().setWorkingDocumentFromName(currDocName);
	                } catch (IOException e1) {
	                    e1.printStackTrace();
	                }
	
	                // Update the page explorer panel with the pages of the
	                // new working document.
	                pageExpPanel.update();
	
	                // Get the very first page and display its image on the
	                // central panel.
	                centralPanel.updatePanels(false);
	                
                }   

            }
        }
    }

    private class deleteListener implements KeyListener{

        public void keyPressed(java.awt.event.KeyEvent evt) {

            if (evt.getKeyCode() == 8) {
                int index = docList.getSelectedIndex();
                String docName = getDocumentNames().get(index);
                int selected = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + docName+"?", "Delete Document", JOptionPane.OK_CANCEL_OPTION);
                if (selected == JOptionPane.OK_OPTION) {

                    try {
                        Parameters.getCoreManager().deleteDocument(docName);
                        update();
                        centralPanel.updatePanels(false);
                        centralPanel.getEastPanel().getPageExpPanel().update();
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
                int index = docList.locationToIndex(evt.getPoint());
                String docName = getDocumentNames().get(index);
                String input = JOptionPane.showInputDialog(null, "Enter new document name: ", "Rename Document", 1);
                if(input != null){
                    try {
                        Parameters.getCoreManager().renameDocument(docName, input);                       
                        update();
                        setDocOrder(input);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Rename Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
            int index = docList.locationToIndex(evt.getPoint());
            fromDrag = index;
            //docList.setDragEnabled(true);
            //docList.setValueIsAdjusting(true);
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            int index = docList.locationToIndex(evt.getPoint());
            String doc1 = getDocumentNames().get(fromDrag);
            String doc2 = getDocumentNames().get(index);
            if (fromDrag != index) {
                int selected = JOptionPane.showConfirmDialog(null, "Are you sure you want to merge "+ doc1 +" and "+doc2+"?", "Merge Documents", JOptionPane.OK_CANCEL_OPTION);
                if (selected == JOptionPane.OK_OPTION) {
                    try {
                        Parameters.getCoreManager().mergeDocuments(doc1, doc2);
                        update();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Merge Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        public void mouseEntered(MouseEvent me) {}

        public void mouseExited(MouseEvent me) {}
    }
}
