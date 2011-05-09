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
import java.util.Collections;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;


public class VisionManager {
	
	private static final boolean OPENCV_ENABLED = false;
	
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
		if (!OPENCV_ENABLED){return img;}
		
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
		int flipmode = 1;
		if (flip.type == ConfigurationValue.ValueType.FlipVertical){
			flipmode = 0;
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
			ConfigurationValue currentValue = config.getKeyWithName(name);
			
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
		if (!OPENCV_ENABLED){return img;}
		return IplImageToBufferedImage( _imageGlobalTransforms(BufferedImageToIplImage(img), config) );
	}
	
	/*
	 * Return the best estimate of the four corners of a piece of paper in the image.
	 */
	public static Corners findCorners(BufferedImage img){
		if (!OPENCV_ENABLED){return new Corners(new Point(0,0), new Point(img.getWidth(),0), new Point(0,img.getHeight()), new Point(img.getWidth(),img.getHeight()));}
		
		IplImage image = BufferedImageToIplImage(img);
		
		IplImage mini = resizeMaxSide(image, 200);
    	final ByteBuffer minibuf = mini.getByteBuffer();
    	
    	IplImage gray = optimalGrayImage(mini, 1);
    	IplImage gray_original = cvCloneImage(gray);
    		
    	ArrayList<MergeZone> merged = findPotentialZones(gray);
    		
    	while(merged.size() > 4){
    		merged.remove(merged.size() - 1);
    	}
    	
    	if (merged.size() < 4){
    		//handle this!
    		//oh god!
    	}
		
		return pointsToCorners(merged);
	}
	
	private static double angular_distance(double a1, double a2){
		a1 += Math.PI;
		a2 += Math.PI;
		
		double d1 = Math.abs(a2-a1);
		double d2 = Math.abs(Math.min(a1,a2)) + Math.abs(Math.PI*2 - Math.max(a1,a2));
		
		return Math.min(d1,d2);
	}
	
	private static Corners pointsToCorners(List<MergeZone> merged){
		double mx = 0;
		double my = 0;
		for(MergeZone pp: merged){
			mx += pp.point.x;
			my += pp.point.y;
		}
		mx /= merged.size();
		my /= merged.size();
		
		for(MergeZone pp: merged){
			pp.weight = angular_distance(Math.atan2( pp.point.y-my, pp.point.x-mx ), Math.PI);
		}
		Collections.sort(merged);
		
		return new Corners(merged.get(0).point, merged.get(1).point, merged.get(3).point, merged.get(2).point);
	}
	
	
	private static void writeImageToFile(BufferedImage img, String path) throws IOException{
		if (!OPENCV_ENABLED){
			File output = new File(path);
			ImageIO.write(img, "png", output);
		}else{
			cvSaveImage(path, BufferedImageToIplImage(img));
		}
	}
	
	/*
	 * Write an image out to a path as a TIFF
	 */
	public static void writeTIFF(BufferedImage img, String path) throws IOException{
		if (!OPENCV_ENABLED){
			File output = new File(path);
			ImageIO.write(img, "tiff", output);
		}else{
			cvSaveImage(path, BufferedImageToIplImage(img));
		}
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
	
	public static BufferedImage loadImage(String path) throws IOException{
		if (!OPENCV_ENABLED){
			File input = new File(path);
			return ImageIO.read(input);
		}else{
			return IplImageToBufferedImage(cvLoadImage(path));
		}
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
	
	private static IplImage linearScaledImage(IplImage scaled){
		final FloatBuffer scaledbuf = scaled.getByteBuffer().asFloatBuffer();
		IplImage output = cvCreateImage(cvSize(scaled.width(), scaled.height()), IPL_DEPTH_8U, 1);
		
    	float v;
    	float min = Float.MAX_VALUE;
    	float max = 0;
    	for(int i=0;i<scaledbuf.capacity();i++){
    		v = scaledbuf.get(i);
    		if (v < min){min=v;}
    		if (v > max){max=v;}
    	}
    	cvConvertScale(scaled, output, 255.0/(max-min), -min);
    	
    	return output;
	}
	
	private static IplImage customGrayTransform(IplImage color, double r, double g, double b){
		IplImage gray = cvCreateImage(cvSize(color.width(), color.height()), IPL_DEPTH_8U, 1);
		
		int set = 0;
		final ByteBuffer graybuf = gray.getByteBuffer();
		final ByteBuffer colorbuf = color.getByteBuffer();
		
		for(int x=0;x<color.width();x++){
			for(int y=0;y<color.height();y++){
				
				double ng = (colorbuf.get(y*color.width()*3 + x*3 + 0)&0xff)*r + (colorbuf.get(y*color.width()*3 + x*3 + 1)&0xff)*g + (colorbuf.get(y*color.width()*3 + x*3 + 2)&0xff)*b;				
				set = (int)ng;
				
				graybuf.put(y*color.width()+x, (byte)set);
			}
		}
		
		return gray;
	}
	
	/*
	 * Return a sorted list of potential corners. Ordered by weight (neighborhood corneriness, & set to 0 if it's in the corner of the image)
	 */
	private static ArrayList<MergeZone> findPotentialZones(IplImage gray){
		int width = gray.width();
		int height = gray.height();
		IplImage edges = cvCreateImage(cvSize(width, height), IPL_DEPTH_32F, 1);
		
		cvCornerHarris(gray, edges, (int) (Math.max(width, height)*0.05), 5, 0.04);
		final FloatBuffer edgebuf = edges.getByteBuffer().asFloatBuffer();
		
		IplImage warpage = cvCreateImage(cvSize(width, height), IPL_DEPTH_32F, 1);
		final FloatBuffer warpbuf = warpage.getByteBuffer().asFloatBuffer();
		
		double[][] warp_arr = new double[width][height];
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				warp_arr[x][y] = 0;
			}
		}
		
		float _v;
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				_v = edgebuf.get(width*y + x);
				
				if ( Math.abs(_v) < 1e-4 ){continue;}
				
				for(int wx=0;wx<width;wx++){
					for(int wy=0;wy<height;wy++){
						if (x==wx && y==wy){continue;}
						warp_arr[wx][wy] += _v/Math.sqrt((wx-x)*(wx-x) + (wy-y)*(wy-y));
					}
				}
				
			}
		}
		
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				warpbuf.put( y*width+x, (float)warp_arr[x][y] );
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
		int last_still_round = 0;
		double motion;
		while(round-last_still_round < 5 && round < 500){
			motion = 0;
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
				motion += Math.sqrt( (p.x-best.x)*(p.x-best.x) + (p.y-best.y)*(p.y-best.y) );
				
				p.x = best.x;
				p.y = best.y;
			}
			
			if (motion >= 1){
				last_still_round = round;
			}
			
			round++;
		}
		System.out.println(round + " rounds");
		
		
		//integrate the warped image
		IplImage integral = cvCloneImage(warpage);
		final FloatBuffer integralbuf = integral.getByteBuffer().asFloatBuffer();
		
		//integralbuf.put( 0, Math.abs(integralbuf.get(0)) );
		for(int i=1;i<width;i++){
			//integralbuf.put(i, Math.abs(integralbuf.get(i)) + integralbuf.get(i-1));
			integralbuf.put(i, Math.abs(integralbuf.get(i)) + integralbuf.get(i-1));
		}for(int i=1;i<height;i++){
			//integralbuf.put(i*width, Math.abs(integralbuf.get(i*width)) + integralbuf.get((i-1)*width));
			integralbuf.put(i*width, Math.abs(integralbuf.get(i*width)) + integralbuf.get((i-1)*width));
		}
		for(int y=1;y<height;y++){
			for(int x=1;x<width;x++){
				//integralbuf.put(y*width+x, Math.abs(integralbuf.get(y*width+x)) + integralbuf.get((y-1)*width+x) + integralbuf.get(y*width+x-1) - integralbuf.get((y-1)*width+x-1));
				integralbuf.put(y*width+x, integralbuf.get(y*width+x) + integralbuf.get((y-1)*width+x) + integralbuf.get(y*width+x-1) - integralbuf.get((y-1)*width+x-1));
			}
		}
		
		//extract the points
		ArrayList<MergeZone> merged = new ArrayList<MergeZone>();
		for(Point p: points){
			boolean already_inserted = false;
			for(MergeZone pp: merged){
				if ( pp.distance(p) < 5 ){
					already_inserted = true;
					break;
				}
			}
			
			if (!already_inserted){
				merged.add(new MergeZone(p));
			}
		}
		System.out.println(merged.size() + " merged points");
		
		for(MergeZone pp: merged){
			Point p = pp.point;
			
			int yl = Math.max(0,p.y-3);
			int xl = Math.max(0,p.x-3);
			int yr = Math.min(p.y+3,height-1);
			int xr = Math.min(p.x+3,width-1);
			
			pp.weight = integralbuf.get(yl*width+xl) + integralbuf.get(yr*width+xr) - integralbuf.get(yl*width+xr) - integralbuf.get(yr*width+xl);
			//pp.weight /= (yr-yl) * (xr-xl);
			
			if (pp.distance(new Point(0,0)) < 3 || pp.distance(new Point(width,0)) < 3 || pp.distance(new Point(0,height)) < 3 || pp.distance(new Point(width,height)) < 3){
				pp.weight = -1e100;
			}
			
		}
		Collections.sort( merged );
		
		//debugging
		warpage = linearScaledImage(warpage);
		for(MergeZone pp: merged){
			Point p = pp.point;
			
			cvCircle(warpage, makePoint(p.x,p.y), 1, CV_RGB(255,255,255), 1, 8, 0);
    		cvCircle(gray, makePoint(p.x,p.y), 1, CV_RGB(255,255,255), 1, 8, 0);
    	}
		
		cvSaveImage("gray.png", gray);
		cvSaveImage("warpage.png", warpage);
		cvSaveImage("integral.png", linearScaledImage(integral));
		
		return merged;
	}
	
	private static IplImage optimalGrayImage(IplImage color, int power){
		final ByteBuffer colorbuf = color.getByteBuffer();
		int width = color.width();
		int height = color.height();
		
		double mu_r = 0;
		double mu_g = 0;
		double mu_b = 0;
		int c = 0;
		
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				mu_r += colorbuf.get(y*width*3 + x*3 + 0)&0xff;
				mu_g += colorbuf.get(y*width*3 + x*3 + 1)&0xff;
				mu_b += colorbuf.get(y*width*3 + x*3 + 2)&0xff;
				c++;
			}
		}
		mu_r /= c;
		mu_g /= c;
		mu_b /= c;
		
		double var_r = 0;
		double var_g = 0;
		double var_b = 0;
		
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				var_r += Math.abs(Math.pow((colorbuf.get(y*width*3 + x*3 + 0)&0xff) - mu_r, power));
				var_g += Math.abs(Math.pow((colorbuf.get(y*width*3 + x*3 + 1)&0xff) - mu_g, power));
				var_b += Math.abs(Math.pow((colorbuf.get(y*width*3 + x*3 + 2)&0xff) - mu_b, power));
			}
		}
		var_r /= c;
		var_g /= c;
		var_b /= c;
		
		System.out.println("Averages:");
		System.out.println(mu_r);
		System.out.println(mu_g);
		System.out.println(mu_b);
		System.out.println("Variances:");
		System.out.println(var_r);
		System.out.println(var_g);
		System.out.println(var_b);
		
		double tt = var_r + var_g + var_b;
		IplImage gray = customGrayTransform(color, var_r/tt, var_g/tt, var_b/tt);
		return gray;
	}
	private static IplImage optimalGrayImage(IplImage color){
		return optimalGrayImage(color, 2);
	}
	
	private static IplImage resizeMaxSide(IplImage image, int maxSide){
    	int nw = 0;
    	int nh = 0;
    	if (image.width() > image.height()){
    		nw = maxSide;
    		nh = (int) ((image.height()*1.0/image.width())*nw);
    	}else{
    		nh = maxSide;
    		nw = (int) ((image.width()*1.0/image.height())*nh);
    	}
    	
    	IplImage mini = cvCreateImage(cvSize(nw, nh), IPL_DEPTH_8U, 3);
    	cvResize(image, mini, CV_INTER_AREA);
    	return mini;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException{
		
		if (!OPENCV_ENABLED){
			System.out.println("OpenCV disabled!");
			System.exit(1);
		}
		
		System.out.println("Vision library stub launcher");
		IplImage image = cvLoadImage("tests/images/DSC_7381.JPG");
		System.out.println("Loaded");
        if (image != null) {
        	
        	IplImage mini = resizeMaxSide(image, 200);
        	final ByteBuffer minibuf = mini.getByteBuffer();
        	
        	if (true){
        		/*
        		 * Ideas:
        		 * -use angle invariance to do RANSAC on the points
        		 * -clustering + morphological ops to segment image for best corners
        		 * -absolute value of harris detector for warpage? (doesn't seem to work)
        		 * -absolute value of harris detector for integral neighborhood eval.? (doesn't seem to improve)
        		 */
        		
        		long start = System.nanoTime();
        		int width = mini.width();
        		int height = mini.height();
        		
        		
        		IplImage gray = optimalGrayImage(mini, 1);
        		IplImage gray_original = cvCloneImage(gray);
        		
        		ArrayList<MergeZone> merged = findPotentialZones(gray);
        		
        		while(merged.size() > 4){
        			merged.remove(merged.size() - 1);
        		}
        		
        		for(MergeZone pp: merged){
        			Point p = pp.point;
        			cvCircle(gray_original, makePoint(p.x,p.y), 1, CV_RGB(255,255,255), 1, 8, 0);
            	}
        		cvSaveImage("final_points.png", gray_original);
        		        		
        		
        		System.out.println((System.nanoTime() - start)/1e9 + " seconds");
        		
        	}else if (false){
        		IplImage output = cvCreateImage(cvSize(mini.width(), mini.height()), IPL_DEPTH_8U, 1);
        		
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