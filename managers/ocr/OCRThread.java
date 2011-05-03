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
                    String[] oldPath = _page.raw().split("/");
                    String name = oldPath[oldPath.length-1];
                    // check if image is Tiff (convert if not)
                    if(!(_page.raw().endsWith(".tif") && _page.raw().endsWith(".tiff"))){
                        String[] d = name.split(".");
                        name = "";
                        for(int i=0;i<d.length-1;i++) name += d[i]+".";
                        BufferedImage img = ImageIO.read(new File(_page.raw()));
                        // convert image and copy to proper directory
                        VisionManager.writeTIFF(img, "workspace/processed/"+name+"tiff");
                    }else{
                        File raw = new File(_page.raw());
                        File outputFile = new File("work/procced/"+name);

                        FileReader in = new FileReader(raw);
                        FileWriter out = new FileWriter(outputFile);
                        int c;

                        while ((c = in.read()) != -1)
                            out.write(c);

                            in.close();
                            out.close();
                    }
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
