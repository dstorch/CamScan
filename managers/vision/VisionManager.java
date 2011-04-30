package vision;

import java.awt.image.BufferedImage;
import java.awt.Point;
import core.Corners;

public class VisionManager {
	public VisionManager(){
		
	}
	public ConfigurationDictionary estimateConfigurationFiles(BufferedImage img){
		return new ConfigurationDictionary();
	}
	public Point snapCorner(BufferedImage img, Point point){
		return point;
	}
	public BufferedImage rerenderImage(BufferedImage img, Corners points, ConfigurationDictionary config){
		return img;
	}
	public BufferedImage recolorImage(BufferedImage img, ConfigurationDictionary config){
		return img;
	}
	public Corners findCorners(BufferedImage img){
		return new Corners(new Point(0,0), new Point(img.getWidth(),0), new Point(0,img.getHeight()), new Point(img.getWidth(),img.getHeight()));
	}
	public void outputToFile(BufferedImage img, String path, Corners points, ConfigurationDictionary config){
		this.writeImageToFile(this.rerenderImage(img, points, config), path);
	}
	
	private void writeImageToFile(BufferedImage img, String path){
		return;
	}
}