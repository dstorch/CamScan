package vision;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Point;
import core.Corners;
import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

import java.io.File;
import java.io.IOException;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;

public class VisionManager {
	public static ConfigurationDictionary estimateConfigurationValues(BufferedImage img){
		return new ConfigurationDictionary();
	}
	
	public static Point snapCorner(BufferedImage img, Point point){
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
	public static BufferedImage rerenderImage(BufferedImage img, Corners corners, ConfigurationDictionary config){
		img = imageGlobalTransforms(img, config);
		
		IplImage image = BufferedImageToIplImage(img);
		
    	Corners reprojected = idealizedReprojection(corners);
        
    	IplImage transformed = cvCreateImage(cvSize(reprojected.width(), reprojected.height()), IPL_DEPTH_8U, 3);
    	
    	CvMat homography = cvCreateMat(3,3,CV_64F);
    	CvMat source_points = cornersToMat(corners);
    	CvMat dest_points = cornersToMat(reprojected);

    	cvFindHomography(source_points, dest_points, homography);
    	cvWarpPerspective(image, transformed, homography, CV_INTER_LINEAR+CV_WARP_FILL_OUTLIERS, cvScalarAll(0));
		
		return IplImageToBufferedImage(transformed);
	}
	
	private static BufferedImage applyTemperatureCorrection(BufferedImage img, ConfigurationValue temp){
		return img;
	}
	private static BufferedImage applyFlipCorrection(BufferedImage img, ConfigurationValue flip){
		return img;
	}
	private static BufferedImage applyContrastBoost(BufferedImage img, ConfigurationValue boost){
		return img;
	}
	public static BufferedImage imageGlobalTransforms(BufferedImage img, ConfigurationDictionary config){
		if (config == null){return img;}
		
		for(Object _name: config.getAllKeys()){
			String name = (String)_name;
			ConfigurationValue currentValue = config.getKey(name);
			
			if (currentValue.type == ConfigurationValue.ValueType.ColorTemperature){
				img = applyTemperatureCorrection(img, currentValue);
			}
			else if (currentValue.type == ConfigurationValue.ValueType.FlipHorizontal ||
					currentValue.type == ConfigurationValue.ValueType.FlipVertical){
				img = applyFlipCorrection(img, currentValue);
			}else if (currentValue.type == ConfigurationValue.ValueType.ContrastBoost){
				img = applyContrastBoost(img, currentValue);
			}else{
				System.err.println("A type in a ConfigurationDictionary given to recolorImage() is invalid and non-processable.");
			}
		}
		return img;
	}
	
	public static Corners findCorners(BufferedImage img){
		
		//take the magnitude of the differential
		
		//create the Harris matrix (window size?)
		
		//seed the image randomly with points
		
		//hill-climbing to convergence
		
		//pick four points (which/how?)
		
		//figure out which ones are which corners
		
		return new Corners(new Point(0,0), new Point(img.getWidth(),0), new Point(0,img.getHeight()), new Point(img.getWidth(),img.getHeight()));
	}
	
	private static void writeImageToFile(BufferedImage img, String path) throws IOException{
		//ImageIO is slow and clunky, switch to cvSave?
		File output = new File(path);;
		ImageIO.write(img, "png", output);
	}
	public static void outputToFile(BufferedImage img, String path, Corners points, ConfigurationDictionary config) throws IOException{
		writeImageToFile(rerenderImage(img, points, config), path);
	}
	
	private static BufferedImage IplImageToBufferedImage(IplImage image){
		return image.getBufferedImage();
	}
	
	/*
	 * Convert a BufferedImage to an IplImage
	 * 
	 * This is inefficient, but it seems to be the best possible (according to the author of javacv)
	 * http://code.google.com/p/javacv/issues/detail?id=2
	 */
	private static IplImage BufferedImageToIplImage(BufferedImage image){
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return IplImage.createFrom(bufferedImage);
	}
	
	private static CvMat cornersToMat(Corners c){
		CvMat points = cvCreateMat(4, 2, CV_64F);
		points.put(0,0,c.upleft().x);
		points.put(0,1,c.upleft().y);
		
		points.put(1,0,c.upright().x);
		points.put(1,1,c.upright().y);
		
		points.put(2,0,c.downleft().x);
		points.put(2,1,c.downleft().y);
		
		points.put(3,0,c.downright().x);
		points.put(3,1,c.downright().y);
		
		return points;
	}
	
	public static void main(String[] args) throws IOException{
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
        	outputToFile(IplImageToBufferedImage(image), "output.png", corners, null);
        	
        }else{
        	System.out.println("Error loading image");
        }
        System.out.println("Done");
	}
	
}