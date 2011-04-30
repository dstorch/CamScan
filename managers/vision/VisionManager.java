package vision;

import java.awt.image.BufferedImage;
import java.awt.Point;
import core.Corners;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;

public class VisionManager {
	public VisionManager(){}
	
	public ConfigurationDictionary estimateConfigurationValues(BufferedImage img){
		return new ConfigurationDictionary();
	}
	
	public Point snapCorner(BufferedImage img, Point point){
		return point;
	}
	
	private static double distance(Point a, Point b){
		return Math.sqrt( (a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) );
	}
	private static Corners idealizedReprojection(Corners corners){
		double averageWidth = distance(corners.upleft(), corners.upright()) + distance(corners.downright(), corners.downleft());
		averageWidth /= 2;
		double averageHeight = distance(corners.upleft(), corners.downleft()) + distance(corners.upright(), corners.downright());
		averageHeight /= 2;
		
		double largestWidth = Math.max(distance(corners.upleft(), corners.upright()), distance(corners.downright(), corners.downleft()));
		double largestHeight = Math.max(distance(corners.upleft(), corners.downleft()), distance(corners.upright(), corners.downright()));
		
		int width, height;
		if (largestWidth > largestHeight){
			width = (int) largestWidth;
			height = (int) ((averageHeight/averageWidth)*largestWidth);
		}else{
			height = (int) largestHeight;
			width = (int) ((averageWidth/averageHeight)*largestHeight);
		}
		
		return new Corners(new Point(0,0), new Point(width,0), new Point(0,height), new Point(width,height));
	}
	public BufferedImage rerenderImage(BufferedImage img, Corners points, ConfigurationDictionary config){
		img = this.imageGlobalTransforms(img, config);
		
		Corners targets = this.idealizedReprojection(points);
		
		//solve for the homography between points and targets
		
		
		//backproject the target area
		
		
		return img;
	}
	
	private BufferedImage applyTemperatureCorrection(BufferedImage img, ConfigurationValue temp){
		return img;
	}
	private BufferedImage applyFlipCorrection(BufferedImage img, ConfigurationValue flip){
		return img;
	}
	private BufferedImage applyContrastBoost(BufferedImage img, ConfigurationValue boost){
		return img;
	}
	public BufferedImage imageGlobalTransforms(BufferedImage img, ConfigurationDictionary config){
		for(Object _name: config.getAllKeys()){
			String name = (String)_name;
			ConfigurationValue currentValue = config.getKey(name);
			
			if (currentValue.type == ConfigurationValue.ValueType.ColorTemperature){
				img = this.applyTemperatureCorrection(img, currentValue);
			}
			else if (currentValue.type == ConfigurationValue.ValueType.FlipHorizontal ||
					currentValue.type == ConfigurationValue.ValueType.FlipVertical){
				img = this.applyFlipCorrection(img, currentValue);
			}else if (currentValue.type == ConfigurationValue.ValueType.ContrastBoost){
				img = this.applyContrastBoost(img, currentValue);
			}else{
				System.err.println("A type in a ConfigurationDictionary given to recolorImage() is invalid and non-processable.");
			}
		}
		return img;
	}
	
	public Corners findCorners(BufferedImage img){
		
		//take the magnitude of the differential
		
		//create the Harris matrix (window size?)
		
		//seed the image randomly with points
		
		//hill-climbing to convergence
		
		//pick four points (which/how?)
		
		//figure out which ones are which corners
		
		return new Corners(new Point(0,0), new Point(img.getWidth(),0), new Point(0,img.getHeight()), new Point(img.getWidth(),img.getHeight()));
	}
	
	private void writeImageToFile(BufferedImage img, String path) throws IOException{
		File output = new File(path);;
		ImageIO.write(img, "TIFF", output);
	}
	public void outputToFile(BufferedImage img, String path, Corners points, ConfigurationDictionary config) throws IOException{
		this.writeImageToFile(this.rerenderImage(img, points, config), path);
	}
	
	private BufferedImage IplImageToBufferedImage(IplImage img){
		return null;
	}
	private IplImage BufferedImageToIplImage(BufferedImage img){
		return null;
	}
	
	public static void main(String[] args){
		System.out.println("Vision library stub launcher");
		IplImage image = cvLoadImage("tests/images/DSC_7384.JPG");
		System.out.println("Loaded");
        if (image != null) {
        	/*
        	IplImage gray = cvCreateImage(cvSize(image.width(), image.height()), IPL_DEPTH_8U, 1);
        	cvCvtColor(image, gray, CV_RGB2GRAY);
        	IplImage edges = cvCreateImage(cvSize(image.width(), image.height()), IPL_DEPTH_32F, 1);
        	
        	//cvSmooth(gray, gray, CV_GAUSSIAN, 3);
            //cvCanny(gray, edges, 100, 3, 5);
        	//cvLaplace(gray, edges, 3);
        	cvCornerHarris(gray, edges, 10, 3, 0.04); // (int) (Math.max(image.width(), image.height())*0.10)
        	
        	IplImage output = cvCreateImage(cvSize(image.width(), image.height()), IPL_DEPTH_8U, 1);
        	cvConvertScale(edges, output, 1, 0);
        	
            cvSaveImage("output.png", output);
            */
        	
            Corners corners = new Corners(new Point(961, 531), new Point(2338, 182), new Point(1411, 2393), new Point(2874, 1986));
        	System.out.println(corners);
        	
        	Corners reprojected = idealizedReprojection(corners);
        	System.out.println(reprojected);
            
            
        }else{
        	System.out.println("Error loading image");
        }
        System.out.println("Done");
	}
	
}