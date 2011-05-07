package vision;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Point;
import core.Corners;
import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;


public class VisionManager {
	
	/*
	 * Estimate good values for the configuration dictionary for a raw image.
	 * Only call once on import.
	 * ConfigurationDictionary specifies the transformations done by imageGlobalTransform.
	 * TODO: guesses for the temperature, flippedness, decide which other things to enable by default
	 */
	public static ConfigurationDictionary estimateConfigurationValues(BufferedImage img){
		ConfigurationDictionary cd = new ConfigurationDictionary();
		
		try {
			cd.setKey(new ConfigurationValue(ConfigurationValue.ValueType.ContrastBoost, false));
			cd.setKey(new ConfigurationValue(ConfigurationValue.ValueType.BilateralFilter, false));
			cd.setKey(new ConfigurationValue(ConfigurationValue.ValueType.FlipHorizontal, false));
			cd.setKey(new ConfigurationValue(ConfigurationValue.ValueType.FlipVertical, false));
		} catch (InvalidTypingException e) {
			System.err.println("InvalidTypingException while setting up ConfigurationDictionary.");
		}
		
		return cd;
	}
	
	/*
	 * Given a user point in the raw image snap it to a close, but slightly more accurate point.
	 * TODO: implement!
	 */
	public static Point snapCorner(BufferedImage img, Point point){
		return point;
	}
	
	private static double distance(Point a, Point b){
		return Math.sqrt( (a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) );
	}
	
	/*
	 * Make some educated guesses about the aspect ratio of the image and
	 * its appropriate size. This works increasingly poorly as angle increases.
	 * TODO: maybe incorporate info about common aspect ratios?
	 */
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
	
	/*
	 * Return the image after applying global transformations and the homography implicit in the four corners.
	 * The result will be a flat, pretty page.
	 */
	public static BufferedImage rerenderImage(BufferedImage img, Corners corners, ConfigurationDictionary config){		
		IplImage image = BufferedImageToIplImage(img);
		image = _imageGlobalTransforms(image, config);
		
    	Corners reprojected = idealizedReprojection(corners);
        
    	IplImage transformed = cvCreateImage(cvSize(reprojected.width(), reprojected.height()), IPL_DEPTH_8U, 3);
    	
    	CvMat homography = cvCreateMat(3,3,CV_64F);
    	CvMat source_points = cornersToMat(corners);
    	CvMat dest_points = cornersToMat(reprojected);

    	cvFindHomography(source_points, dest_points, homography);
    	cvWarpPerspective(image, transformed, homography, CV_INTER_LINEAR+CV_WARP_FILL_OUTLIERS, cvScalarAll(0));
		
		return IplImageToBufferedImage(transformed);
	}
	
	/*
	 * Applies temperature correction to an image.
	 * TODO: implement this! (how is this done?)
	 */
	private static IplImage applyTemperatureCorrection(IplImage img, ConfigurationValue temp){
		return img;
	}
	
	/*
	 * Flips an image.
	 */
	private static IplImage applyFlipCorrection(IplImage img, ConfigurationValue flip){
		if (!(Boolean)flip.value()){return img;}
		int flipmode = 0;
		if (flip.type == ConfigurationValue.ValueType.FlipVertical){
			flipmode = 1;
		}
		cvFlip(img, img, flipmode);
		return img;
	}
	
	/*
	 * Apply a contrast boost by equalizing the image in grayscale & reapplying that
	 * relative difference in the luma channel. Also see alternative algorithm (faster,
	 * but has chroma artifacts.)
	 */
	private static IplImage applyContrastBoost(IplImage img, ConfigurationValue boost){
		if (!(Boolean)boost.value()){return img;}
		
		IplImage gray = cvCreateImage(cvSize(img.width(), img.height()), IPL_DEPTH_8U, 1);
    	cvCvtColor(img, gray, CV_RGB2GRAY);
		cvEqualizeHist(gray, gray);
		
		IplImage hsl = cvCreateImage(cvSize(img.width(), img.height()), IPL_DEPTH_8U, 3);
		cvCvtColor(img, hsl, CV_RGB2HLS);
		
		final ByteBuffer graybuf = gray.getByteBuffer();
		final ByteBuffer hslbuf = hsl.getByteBuffer();
		
		byte equalized;
		
		for(int y=0;y<img.height();y++){
			for (int x=0;x<img.width();x++){
				equalized = graybuf.get( y*img.width() + x );
				hslbuf.put(y*img.width()*3 + x*3 + 1, equalized );
			}
		}
		
		
		cvCvtColor(hsl, img, CV_HLS2RGB);
		
		//alternate algorithm balancing each channel seperately. leads to weird chroma artifacts.
		/*IplImage ch1 = cvCreateImage(cvSize(img.width(), img.height()), IPL_DEPTH_8U, 1);
		IplImage ch2 = cvCreateImage(cvSize(img.width(), img.height()), IPL_DEPTH_8U, 1);
		IplImage ch3 = cvCreateImage(cvSize(img.width(), img.height()), IPL_DEPTH_8U, 1);
		cvSplit(img, ch1, ch2, ch3, null);
		
		cvEqualizeHist(ch1, ch1);
		cvEqualizeHist(ch2, ch2);
		cvEqualizeHist(ch3, ch3);
		
		cvMerge(ch1, ch2, ch3, null, img);*/
		
		return img;
	}
	
	/*
	 * Applies a bilateral filter to the image
	 * TODO: returns a black image!, prohibitively slow.
	 */
	private static IplImage applyBilateralFilter(IplImage img, ConfigurationValue filter){
		if (!(Boolean)filter.value()){return img;}
		IplImage nimg = cvCloneImage(img);
		cvSmooth(img, nimg, CV_BILATERAL, 5);
		return nimg;
	}
	
	/*
	 * Internal implementation of global transforms using IplImage (to avoid the transformation)
	 */
	private static IplImage _imageGlobalTransforms(IplImage img, ConfigurationDictionary config){
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
			}else if (currentValue.type == ConfigurationValue.ValueType.BilateralFilter){
				img = applyBilateralFilter(img, currentValue);
			}else{
				System.err.println("A type in a ConfigurationDictionary given to recolorImage() is invalid and non-processable.");
			}
		}
		return img;
	}
	
	/*
	 * Apply global transformations to an image as specified by the ConfigurationDictionary.
	 * This image is the one which should be shown in edit mode. It does not need to be applied before
	 * calling rerenderImage (as rerender does it interally.)
	 */
	public static BufferedImage imageGlobalTransforms(BufferedImage img, ConfigurationDictionary config){
		return IplImageToBufferedImage( _imageGlobalTransforms(BufferedImageToIplImage(img), config) );
	}
	
	/*
	 * Return the best estimate of the four corners of a piece of paper in the image.
	 */
	public static Corners findCorners(BufferedImage img){
		//TODO
		System.out.println(img);
		
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
		//File output = new File(path);;
		//ImageIO.write(img, "png", output);
		cvSaveImage(path, BufferedImageToIplImage(img));
	}
	
	/*
	 * Write an image out to a path as a TIFF
	 */
	public static void writeTIFF(BufferedImage img, String path){
		cvSaveImage(path, BufferedImageToIplImage(img));
	}
	
	/*
	 * Write a rendered image out to a path. Like calling rerenderImage, but instead of returning it writes the image to a file.
	 */
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
	
	public static BufferedImage loadImage(String path){
		return IplImageToBufferedImage(cvLoadImage(path));
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
	
	public static CvPoint makePoint(double x, double y){
		CvPoint pt = new CvPoint();
		pt.set((int)x, (int)y);
		return pt;
	}
	
	private static IplImage customGrayTransform(IplImage color, double r, double g, double b){
		IplImage gray = cvCreateImage(cvSize(color.width(), color.height()), IPL_DEPTH_8U, 1);
		
		byte set = 0;
		final ByteBuffer graybuf = gray.getByteBuffer();
		final ByteBuffer colorbuf = color.getByteBuffer();
		
		for(int x=0;x<color.width();x++){
			for(int y=0;y<color.height();y++){
				
				double ng = colorbuf.get(y*color.width()*3 + x*3 + 0)*r + colorbuf.get(y*color.width()*3 + x*3 + 1)*g + colorbuf.get(y*color.width()*3 + x*3 + 2)*b;
				set = (byte)ng;
				
				graybuf.put(y*color.width()+x, set);
			}
		}
		
		return gray;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException{
		System.out.println("Vision library stub launcher");
		IplImage image = cvLoadImage("tests/images/DSC_7384.JPG");
		System.out.println("Loaded");
        if (image != null) {
        	
        	int maxsize = 200;
        	
        	int nw = 0;
        	int nh = 0;
        	if (image.width() > image.height()){
        		nw = maxsize;
        		nh = (int) ((image.height()*1.0/image.width())*nw);
        	}else{
        		nh = maxsize;
        		nw = (int) ((image.width()*1.0/image.height())*nh);
        	}
        	IplImage bgray = cvCreateImage(cvSize(image.width(), image.height()), IPL_DEPTH_8U, 1);
        	cvCvtColor(image, bgray, CV_RGB2GRAY);
        	cvSmooth(bgray, bgray, CV_GAUSSIAN, 5);
        	
        	//IplImage gray = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_8U, 1);
        	//cvResize(bgray, gray);
        	//cvEqualizeHist(gray, gray);
        	
        	IplImage mini = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_8U, 3);
        	cvResize(image, mini);
        	
        	if (true){
        		IplImage gray = customGrayTransform(mini, 1, 0, 0);
        		
        		
        		IplImage edges = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_32F, 1);
        		
        		//cvSmooth(gray, gray, CV_GAUSSIAN, 3);
                //cvCanny(gray, edges, 100, 3, 5);
            	//cvLaplace(gray, edges, 3); //cvConvertScale(edges, output, 1, 0);
            	//cvCornerMinEigenVal(gray, edges, 80, 3);
        		cvCornerHarris(gray, edges, (int) (maxsize*0.05), 5, 0.04);
        		final FloatBuffer edgebuf = edges.getByteBuffer().asFloatBuffer();
        		int width = edges.width();
        		int height = edges.height();
        		
        		IplImage warpage = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_32F, 1);
        		final FloatBuffer warpbuf = warpage.getByteBuffer().asFloatBuffer();
        		
        		
        		for(int wx=0;wx<width;wx++){
        			for(int wy=0;wy<height;wy++){
        				Double result = 0.0; //edgebuf.get(width*wy + wx);
        				
        				for(int x=0;x<width;x++){
        					for(int y=0;y<height;y++){
        						if (x==wx && y==wy){continue;}
        						//edgebuf.get(width*y + x)
        						//System.out.println( 1.0/ Math.sqrt( (wx-x)*(wx-x) + (wy-y)*(wy-y) ) );
        						result += edgebuf.get(width*y + x)/Math.sqrt( (wx-x)*(wx-x) + (wy-y)*(wy-y) ); //0.01 / Math.sqrt( (wx-x)*(wx-x) + (wy-y)*(wy-y) );
        					}
        				}
        				
        				warpbuf.put(width*wy + wx, result.floatValue());
        			}
        		}

        		
        		//cluster some points!
        		ArrayList<Point> points = new ArrayList<Point>();
        		
        		
        		for(int x=0;x<width;x+=5){
        			for(int y=0;y<height;y+=5){
        				points.add( new Point(x,y) );
        			}
        		}
        		
        		int round = 0;
        		while(round < 500){
        			for(Point p: points){
        				float current = warpbuf.get( p.y*width + p.x );
        				Point best = p;
        				
        				for(int sx=-1;sx<=1;sx++){
        					for(int sy=-1;sy<=1;sy++){
        						if ((sx==0&&sy==0) || p.x+sx < 0 || p.y+sy < 0 || p.x+sx >= width || p.y+sy >= height){continue;}
        						
        						float newpoint = warpbuf.get( (p.y+sy)*width + p.x+sx );
        						if (newpoint > current){
        							best = new Point(p.x+sx, p.y+sy);
        						}
        					}
        				}
        				p.x = best.x;
        				p.y = best.y;
        			}
        			
        			round ++;
        		}
        		
        		//convert to 8bit for output
        		IplImage output = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_8U, 1);
            	
            	
            	float v;
            	float min = Float.MAX_VALUE;
            	float max = 0;
            	for(int i=0;i<warpbuf.capacity();i++){
            		v = warpbuf.get(i);
            		if (v < min){min=v;}
            		if (v > max){max=v;}
            	}
            	System.out.println(min);
            	System.out.println(max);
            	cvConvertScale(warpage, output, 255.0/(max-min), -min);
            	
            	
            	for(Point p: points){
            		cvCircle(output, makePoint(p.x,p.y), 1, CV_RGB(255,255,255), 1, 8, 0);
            	}
            	
            	
            	//save!
            	cvSaveImage("edges.png", output);
                cvSaveImage("output.png", gray);
        	}else if (false){
        		IplImage output = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_8U, 1);
        		
        		CvMat lines_storage = cvCreateMat(4, 1, CV_32SC4);
            	CvSeq results = cvHoughLines2(output, lines_storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI/180, 100, 5, 3);
            	for(int i=0;i<4;i++){
            		double p1x = lines_storage.get(0, i, 0);
            		double p1y = lines_storage.get(0, i, 1);
            		double p2x = lines_storage.get(0, i, 2);
            		double p2y = lines_storage.get(0, i, 3);
            		System.out.printf("(%f, %f) to (%f, %f)\n", p1x, p1y, p2x, p2y);
            		cvLine(output, makePoint(p1x, p1y), makePoint(p2x, p2y), CV_RGB(255,255,255), 3, 8, 0);
            		System.out.println(makePoint(p1x,p1y));
            	}
        	}else if (false){
        		Corners corners = new Corners(new Point(961, 531), new Point(2338, 182), new Point(1411, 2393), new Point(2874, 1986));        	
            	outputToFile(IplImageToBufferedImage(image), "output.png", corners, estimateConfigurationValues(IplImageToBufferedImage(image)));
        	}

        	
        }else{
        	System.out.println("Error loading image");
        }
        System.out.println("Done");
	}
	
}
