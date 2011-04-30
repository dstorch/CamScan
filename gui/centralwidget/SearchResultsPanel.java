package centralwidget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class SearchResultsPanel extends JPanel {

	public SearchResultsPanel() {
		super();
		this.setBackground(Color.LIGHT_GRAY);
	}
	
	/**
	 * The paintComponent method.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D brush = (Graphics2D) g;
	}
}
