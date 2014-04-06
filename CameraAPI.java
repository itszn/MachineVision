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
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui;


public class CameraAPI implements ActionListener{
	CanvasFrame mainCameraFrame;
	CanvasFrame cameraOutputFrame;
	
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
	
	IplImage outputImage;
	
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
		outputImage = getNewImage();
		mainCameraFrame = new CanvasFrame("Camera");
		mainCameraFrame.setLocation(0,0);
		cameraOutputFrame = new CanvasFrame("Output");
		cameraOutputFrame.setLocation(imgDim.width+5, 0);
		
		optionsFrame = new JFrame("Options"); 
		optionsFrame.setLocation(0, imgDim.height+40);
		JPanel jp = new JPanel();
			JLabel jl = new JLabel("Use Camera");
			jp.add(jl);
			cameraModeCheckbox = new JCheckBox();
				cameraModeCheckbox.setSelected(startCamera);
				cameraModeCheckbox.setActionCommand("cameraModeCheckbox");
				cameraModeCheckbox.addActionListener(this);
			jp.add(cameraModeCheckbox);
			JButton jb = new JButton("Load Image");
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
		mainLoop();
		
	}
	
	public IplImage getNewImage() {
		return IplImage.create(currentImage.width(), currentImage.height(), currentImage.depth(),currentImage.nChannels());
	}
	boolean updateCameraMode = false;
	
	public void mainLoop() {
		while(mainCameraFrame.isVisible() && cameraOutputFrame.isVisible() && optionsFrame.isVisible()) {
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
			
			long startTime = System.currentTimeMillis();
			updateImage();
			mainCameraFrame.showImage(currentImage);
			outputImage = getNewImage();
			ip.process(currentImage, outputImage);
			
			
			cameraOutputFrame.showImage(outputImage);
			
		}
		
		try {
			grabber.stop();
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mainCameraFrame.dispose();
		cameraOutputFrame.dispose();
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
	}
}
