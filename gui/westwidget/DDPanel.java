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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
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
public class DDPanel extends JPanel implements DropTargetListener, ActionListener {

	/****************************************
	 * 
	 * Private Instance Variables
	 * 
	 ****************************************/
	
	/**
	 * The drop target.
	 */
	private DocExplorerPanel docExpPanel;
	
	private Timer timer;
	
	private int repeats = 0;
	
	private Color dndColor = new Color(255, 80, 67);
	
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
		
		this.timer = new Timer(75, this);
		
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
	
	/**************************************************
	 * 
	 * RED FLASHING BEHAVIOR - for drag and drop
	 * 
	 **************************************************/
	
	public void dragEnter(DropTargetDragEvent arg0) {
		this.repeats = 0;
		this.setBackground(dndColor);
		timer.start();
	}

	public void dragExit(DropTargetEvent arg0) {
		timer.stop();
		this.setBackground(Color.WHITE);
	}

	public void dragOver(DropTargetDragEvent arg0) {
		timer.start();
	}

	public void dropActionChanged(DropTargetDragEvent arg0) {
		timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		Color current = this.getBackground();
		if (this.repeats < 10) {
			this.setBackground(lighten(current));
			this.repeats++;
		} else {
			this.setBackground(darken(current));
			this.repeats = (this.repeats + 1) % 20;
		}
	}
	
	private Color darken(Color c) {
		int red = c.getRed() - 10;
		int blue = c.getBlue() - 5;
		int green = c.getGreen() - 5;
		if (red < 0) red = 0;
		if (green < 0) green = 0;
		if (blue < 0) blue = 0;
		return new Color(red, blue, green);
	}
	
	private Color lighten(Color c) {
		int red = c.getRed() + 10;
		int blue = c.getBlue() + 5;
		int green = c.getGreen() + 5;
		if (red > 255) red = 255;
		if (green > 255) green = 255;
		if (blue > 255) blue = 255;
		return new Color(red, blue, green);
	}
}
