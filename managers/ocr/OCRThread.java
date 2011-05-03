package ocr;

import java.io.IOException;

import core.Page;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import vision.VisionManager;


public class OCRThread extends Thread {

	private Page _page;
	
	public OCRThread(Page page) {
		_page = page;
	}
	
	public void run() {
		try {
			System.out.println("writing processed image");
			//_page.writeProcessedImage();
			System.out.println("doing OCR");
			_page.setOcrResults();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("OCR thread exiting");
	}


}
