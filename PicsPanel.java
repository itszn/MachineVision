import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JPanel;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.videoInputLib.videoInput;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.avutil.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;


public class PicsPanel extends JPanel implements Runnable{
	private static final int DELAY = 100;
	private static final int CAMERA_ID = 0;
	
	private volatile boolean isRunning;
	private volatile boolean isFinished;
	private volatile boolean takeSnap = false;
	
	private long totalTime = 0;
	private int imageCount = 0;
	
	private IplImage snapIm = null;
	
	public PicsPanel() {
		setBackground(Color.white);
		msgFont = new Font("SansSerif", Font.BOLD, 18);
		prepareSnapDir();
		new Thread(this).start();
	}
	
	public Dimension getPreferredSize()
	  // make the panel wide enough for an image
	  {   return new Dimension(WIDTH, HEIGHT); }
	
	private void prepareSnapDir()
	  /* make sure there's a SAVE_DIR directory, and backup
	     any images in there by prefixing them with "OLD_"
	  */
	  {
	    File saveDir = new File(SAVE_DIR);

	    if (saveDir.exists()) {   // backup any existing files
	      File[] listOfFiles = saveDir.listFiles();
	      if (listOfFiles.length > 0) {
	        System.out.println("Backing up files in " + SAVE_DIR);
	        for (int i = 0; i < listOfFiles.length; i++) {
	          if (listOfFiles[i].isFile()) {
	            File nFile = new File(SAVE_DIR + "OLD_" + listOfFiles[i].getName()); 
	            listOfFiles[i].renameTo(nFile);
	          }
	        }
	      }
	    }
	    else {   // directory does not exist, so create it
	      System.out.println("Creating directory: " + SAVE_DIR);
	      boolean isCreated = saveDir.mkdir();  
	      if(!isCreated) {
	        System.out.println("-- could not create");  
	        System.exit(1);
	      }
	    }
	  }  // end of prepareSnapDir()

	
	public void run() {
		FrameGrabber grabber = initGrabber(CAMERA_ID);
		if (grabber == null) {
			return;
		}
		
		long duration;
		int snapCount = 0;
		isRunning = true;
		isFinished = false;
		
		while(isRunning) {
			long startTime = System.currentTimeMillis();
			
			snapIm = picGrab(grabber, CAMERA_ID);
			
			if (takeSnap) {
				saveImage(snapIm, PIC_FNM, snapCount);
				snapCount++;
				takeSnap = false;
			}
			
			imageCount++;
			repaint();
			
			duration = System.currentTimeMillis() - startTime;
			totalTime += duration;
			if (duration < DELAY) {
				try {
					Thread.sleep(DELAY-duration);
				}catch(Exception e){}
			}
		}
		closeGrabber(grabber, CAMERA_ID);
		System.out.println("Execution end");
		isFinished = true;
	}
	
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;
	
	private FrameGrabber initGrabber(int id) {
		FrameGrabber grab = null;
		System.out.println("Init grabber for "+ videoInput.getDeviceName(id));
		try {
			grab = FrameGrabber.createDefault(id);
			grab.setFormat("dshow");
			grab.setImageWidth(WIDTH);
			grab.setImageHeight(HEIGHT);
			grab.start();
		}
		catch(Exception e) {
			e.printStackTrace();System.exit(1);
		}
		
		return grab;
	}
	
	private IplImage picGrab(FrameGrabber grabber, int id) {
		IplImage img = null;
		
		try{
			img = grabber.grab();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img;
	}
	
	public void closeDown() {
		isRunning = false;
		while(!isFinished) {
			try{ Thread.sleep(DELAY);}
			catch(Exception e){}
		}
	}
	
	private void closeGrabber(FrameGrabber grabber, int ID) {
		try{
			grabber.stop();
			grabber.release();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Font msgFont;
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(msgFont);
		if (snapIm != null) {
			g.setColor(Color.YELLOW);
			g.drawImage(snapIm.getBufferedImage(), 0, 0, this);
			String stats = String.format("Snap Avg. Time:%.1f ms", ((double) totalTime/imageCount));
			g.drawString(stats, 5, HEIGHT-10);
		}
		else {
			g.setColor(Color.BLUE);
			g.drawString("Loading from camera "+CAMERA_ID+"...", 5, HEIGHT-10);
		}
	}
	
	private static final String SAVE_DIR = "pics/";
	private static final String PIC_FNM = "pic";
	
	private void saveImage(IplImage snapIM, String saveFnm, int snapCount) {
		if (snapIM == null) {
			return;
		}
		
		IplImage grayImage = IplImage.create(WIDTH, HEIGHT, IPL_DEPTH_8U,1);
		cvCvtColor(snapIm, grayImage, CV_BGR2GRAY);
		
		String fnm = (snapCount<10)? SAVE_DIR+saveFnm+"0"+snapCount+".jpg":SAVE_DIR+saveFnm+snapCount+".jpg";
		System.out.println("Saving "+fnm);
		cvSaveImage(fnm,grayImage);
	}
	public void takeSnap()
	  {  takeSnap = true;   } 
	
}
