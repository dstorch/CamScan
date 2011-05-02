package westwidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import core.Parameters;


/**
 * The Drag and Drop class. The user can use it to
 * drag and drop directories of images and create new
 * documents out of them.
 * 
 * @author Stelios
 *
 */
public class DDPanel extends JPanel implements DropTargetListener {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The drop target.
	 */
	private DocExplorerPanel docExpPanel;
	
	/****************************************
	 * 
	 * Constructor(s)
	 * 
	 ****************************************/
	
	/**
	 * Constructor.
	 */
	public DDPanel(DocExplorerPanel docExpPanel) {
		super();
		this.setBackground(Color.WHITE);
		this.setPreferredSize(new Dimension(150, 100));
		this.setBorder(new LineBorder(Color.GRAY));
		this.setLayout(new BorderLayout());
		
		JLabel dndLabel = new JLabel("Drag 'n' Drop Box", SwingConstants.CENTER);
		this.add(dndLabel, BorderLayout.CENTER);
		
		this.docExpPanel = docExpPanel;
		new DropTarget(this, this);
	}
	
	/****************************************
	 * 
	 * Public Methods
	 * 
	 ****************************************/

	/**
	 * Processes the files passed in the drag'n'drop panel.
	 * 
	 * @param files List of all the files
	 * @throws IOException
	 */
	private void processFiles(List<File> files) throws IOException {
		for (File file : files) {
			Parameters.getCoreManager().createDocumentFromFolder(new File(file.getAbsolutePath()));
		}
		
		this.docExpPanel.update();
	}
	
	/****************************************
	 * 
	 * Event Methods
	 * 
	 ****************************************/
	
	/**
	 * Handles the drop action.
	 * 
	 * @param evt
	 */
	public void drop(DropTargetDropEvent evt) {
		
		int action = evt.getDropAction();
		evt.acceptDrop(action);
		try {
			Transferable data = evt.getTransferable();
			if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> list = (List<File>) data.getTransferData(DataFlavor.javaFileListFlavor);
				this.processFiles(list);
			}
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			evt.dropComplete(true);
			repaint();
		}
	}
	
	public void dragEnter(DropTargetDragEvent arg0) {}

	public void dragExit(DropTargetEvent arg0) {}

	public void dragOver(DropTargetDragEvent arg0) {}

	public void dropActionChanged(DropTargetDragEvent arg0) {}
}
