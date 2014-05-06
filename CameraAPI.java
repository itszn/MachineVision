import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui;


public class CameraAPI implements ActionListener{
	CanvasFrame mainCameraFrame;
	ArrayList<CanvasFrame> cameraOutputFrames;
	
	JFrame optionsFrame;
		JCheckBox cameraModeCheckbox;
		JLabel loadedPicsLabel;
	
	JFrame configFrame;
		
	
	
	OpenCVFrameGrabber grabber;
	String fileDir;
	ArrayList<IplImage> loadedPics = new ArrayList<IplImage>();
	int selectedPic = 0;
	
	Dimension imgDim = null;
	
	IplImage currentImage;
	IplImage lastImage;
	
	IplImage[] outputImages;
	
	boolean cameraMode = false;
	
	ImageProcesser ip;
	
	
	public CameraAPI(int cID, boolean startCamera, String file, ImageProcesser ip) {
		this.ip = ip;
		cameraMode = startCamera;
		fileDir = file;
		if (startCamera) {
			startCamera(cID);
		}
		else {
			if (file != null ) {
				loadImage(file);
			}
		}
		updateImage();
		imgDim = new Dimension(currentImage.width(), currentImage.height());
		outputImages = new IplImage[]{getNewImage()};
		mainCameraFrame = new CanvasFrame("Camera");
		mainCameraFrame.setLocation(0,0);
		cameraOutputFrames = new ArrayList<CanvasFrame>();
		
		
		optionsFrame = new JFrame("Options"); 
		optionsFrame.setLocation(2*imgDim.width+15, 0);
		JPanel jp = new JPanel();
			JButton jb = new JButton("Refresh");
				jb.addActionListener(this);
				jb.setActionCommand("resetWindows");
			jp.add(jb);
			JLabel jl = new JLabel("Use Camera");
			jp.add(jl);
			cameraModeCheckbox = new JCheckBox();
				cameraModeCheckbox.setSelected(startCamera);
				cameraModeCheckbox.setActionCommand("cameraModeCheckbox");
				cameraModeCheckbox.addActionListener(this);
			jp.add(cameraModeCheckbox);
			jb = new JButton("Load Image");
				jb.setActionCommand("loadImageButton");
				jb.addActionListener(this);
			jp.add(jb);
			jb = new JButton("<");
				jb.setActionCommand("selectImageLeft");
				jb.addActionListener(this);
			jp.add(jb);
			loadedPicsLabel = new JLabel("0/0");
			jp.add(loadedPicsLabel);
			jb = new JButton(">");
			jb.setActionCommand("selectImageRight");
			jb.addActionListener(this);
		jp.add(jb);
		optionsFrame.add(jp);

		optionsFrame.pack();
		optionsFrame.setVisible(true);
		
		
	}
	
	public void start() {
		ip.init(currentImage);
		mainLoop();
	}
	
	public IplImage getNewImage() {
		return IplImage.create(currentImage.width(), currentImage.height(), currentImage.depth(),currentImage.nChannels());
	}
	
	public IplImage getNewImage(IplImage img) {
		return IplImage.create(img.width(), img.height(), img.depth(),img.nChannels());
	}
	
	boolean updateCameraMode = false;
	
	public void mainLoop() {
		boolean outClosed = true;
		while(mainCameraFrame.isVisible() && outClosed && optionsFrame.isVisible()) {
			for (CanvasFrame cf : cameraOutputFrames) {
				outClosed &= cf.isVisible();
			}
			long startTime = System.currentTimeMillis();
			lastImage = getNewImage();
			opencv_core.cvCopy(currentImage, lastImage);
			if (updateCameraMode) {
				updateCameraMode = false;
				if (cameraMode) {
					startCamera(0);
				}
				else {
					if (loadedPics.size()>0)
						stopCamera();
					else {
						cameraMode= true;
						cameraModeCheckbox.setSelected(true);
					}
				}
			}
			
			
			updateImage();
			imgDim = new Dimension(currentImage.width(), currentImage.height());
			
			mainCameraFrame.showImage(currentImage);
			outputImages = ip.process(currentImage.clone(), lastImage.clone());
			
			for (int i=0; i< outputImages.length; i++) {
				
				if (cameraOutputFrames.size()>i && cameraOutputFrames.get(i)!=null) {
					if (outputImages[i]==null) {
						cameraOutputFrames.get(i).showImage(getNewImage());
					}
					else {
						cameraOutputFrames.get(i).showImage(outputImages[i]);
					}
				}
				else {
					CanvasFrame f = new CanvasFrame("Output "+i);
					int l = (i+1)*(imgDim.width+5);
					int m = (1+1)*(imgDim.width+5);
					f.setLocation(mainCameraFrame.getLocation().x+l%m, mainCameraFrame.getLocation().y+(l/m)*(imgDim.height));
					if (cameraOutputFrames.size()>i)
						cameraOutputFrames.set(i, f);
					else
						cameraOutputFrames.add(f);
				}
			}
			if (cameraOutputFrames.size()>outputImages.length) {
				for (int i=outputImages.length;i<cameraOutputFrames.size(); i++) {
					CanvasFrame f = cameraOutputFrames.get(i);
					if (f!=null)
						f.dispose();
					cameraOutputFrames.remove(i);
				}
			}
			
			long timeDiff = System.currentTimeMillis()-startTime;
			int fps = (int) (1000/timeDiff);
			mainCameraFrame.setTitle("Camera "+fps);
			
		}
		
		try {
			grabber.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mainCameraFrame.dispose();
		for(CanvasFrame f: cameraOutputFrames)
			f.dispose();
		optionsFrame.dispose();
	}
	
	private void updateImage() {
		if (cameraMode) {
			if (grabber != null) {
				try {
					currentImage = grabber.grab();
				} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			currentImage = loadedPics.get(selectedPic);
		}
	}
	
	
	
	public void loadImage(String s) {
		try {
			File f = new File(s);
			IplImage img = opencv_highgui.cvLoadImage(s);
			loadedPics.add(img);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startCamera(int cID) {
		if (grabber != null ) {
			try {
				grabber.stop();
				grabber.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		grabber = new OpenCVFrameGrabber(cID);
		try {
			grabber.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopCamera() {
		if (grabber != null) {
			try {
				grabber.stop();
				grabber.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cameraModeCheckbox")) {
			if (cameraMode != ((JCheckBox)e.getSource()).isSelected()) {
				cameraMode = ((JCheckBox)e.getSource()).isSelected();
				updateCameraMode = true;
			}
		}
		else if (e.getActionCommand().equals("selectImageLeft")) {
			if (selectedPic>0) {
				selectedPic--;
				loadedPicsLabel.setText((selectedPic+1)+"/"+loadedPics.size());
			}
		}
		else if (e.getActionCommand().equals("selectImageRight")) {
			if (selectedPic<loadedPics.size()-1) {
				selectedPic++;
				loadedPicsLabel.setText((selectedPic+1)+"/"+loadedPics.size());
			}
		}
		else if (e.getActionCommand().equals("loadImageButton")) {
			JFileChooser fc = new JFileChooser();
			int val = fc.showOpenDialog(optionsFrame);
			if (val == JFileChooser.APPROVE_OPTION ) {
				loadImage(fc.getSelectedFile().getPath());
				selectedPic = loadedPics.size()-1;

				loadedPicsLabel.setText((selectedPic+1)+"/"+loadedPics.size());
			}
		}
		else if (e.getActionCommand().equals("resetWindows")) {
			for(CanvasFrame f: cameraOutputFrames)
				f.dispose();
			cameraOutputFrames = new ArrayList<CanvasFrame>();
		}
	}
}
