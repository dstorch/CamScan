package westwidget;

import gui.ParamHolder;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JPanel;

import core.Parameters;



public class DDPanel extends JPanel implements DropTargetListener {

	/**
	 * The drop target.
	 */
	private DocExplorerPanel docExpPanel;
	
	/**
	 * Constructor.
	 */
	public DDPanel(DocExplorerPanel docExpPanel) {
		super();
		this.setBackground(Color.YELLOW);
		this.setPreferredSize(new Dimension(150, 100));
		this.docExpPanel = docExpPanel;
		new DropTarget(this, this);
	}
	
	/**
	 * Handles the drop action.
	 * 
	 * @param evt
	 */
	public void drop(DropTargetDropEvent evt) {
		//final List result = new ArrayList();
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

	/**
	 * Processes the files passed in the drag'n'drop panel.
	 * 
	 * @param files List of all the files
	 * @throws IOException
	 */
	private void processFiles(List<File> files) throws IOException {
		for (File file : files) {
			Parameters.getCoreManager().createDocumentFromFolder(new File(file.getAbsolutePath()));
			//this.copyDirectory(new File(file.getAbsolutePath()), new File(ParamHolder.getWorkspace() + File.separator + file.getName()));
		}
		
		this.docExpPanel.update();
	}

	/**
	 * Copies a file from the given path to the target path.
	 * 
	 * @param fromFileName The path where the file is
	 * @param toFileName The target path (filename included).
	 * @throws IOException
	 */
	public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
		  if (sourceLocation.isDirectory()) {

	            if (!targetLocation.exists()) {
	                targetLocation.mkdir();
	            }
    
	            String[] children = sourceLocation.list();
	            for (int i=0; i<children.length; i++) {
	                copyDirectory(new File(sourceLocation, children[i]),
	                        new File(targetLocation, children[i]));
	            }
	        } else {
	            
	            InputStream in = new FileInputStream(sourceLocation);
	            OutputStream out = new FileOutputStream(targetLocation);
	            
	            // Copy the bits from instream to outstream
	            byte[] buf = new byte[1024];
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	            in.close();
	            out.close();
	        }
	}
	
	public void dragEnter(DropTargetDragEvent arg0) {}

	public void dragExit(DropTargetEvent arg0) {}

	public void dragOver(DropTargetDragEvent arg0) {}

	public void dropActionChanged(DropTargetDragEvent arg0) {}
}
