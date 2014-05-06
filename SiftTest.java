import java.io.File;
import java.util.ArrayList;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_flann.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.ObjectFinder;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_highgui;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_legacy.CvSURFParams;
import com.googlecode.javacv.cpp.opencv_nonfree;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;


public class SiftTest implements ImageProcesser{
	static CameraAPI api;
	static SiftTest instance;
	
	public static void main(String...args) {
		instance = new SiftTest();
		api = new CameraAPI(0,true,"bigger.png",instance);
		api.start();
	}



	@Override
	public IplImage[] process(IplImage currentImage, IplImage lastImage, IplImage... finalImage) {
		try {
		// TODO Auto-generated method stub
		IplImage correspond = api.getNewImage();//IplImage.create(currentImage.width(), train.height()+ currentImage.height(), 8, 1);
		
		//cvSetImageROI(correspond, cvRect(0, 0, train.width(), train.height()));
		//cvCopy(train, correspond);
		//cvSetImageROI(correspond, cvRect(0, train.height(), correspond.width(), correspond.height()));
		cvCopy(currentImage, correspond);
		cvResetImageROI(correspond);
		
		ObjectFinder.Settings set = new ObjectFinder.Settings();
		set.setObjectImage(train);
		set.setUseFLANN(true);
		set.setRansacReprojThreshold(10);
//		set.setMatchesMin(7);
		ObjectFinder find = new ObjectFinder(set);
		
		long start = System.currentTimeMillis();
		double[] dst_corners = find.find(currentImage);
		System.out.println("Finding time = " + (System.currentTimeMillis() - start) + " ms");
		
		if (dst_corners != null) {
			for (int i=0; i<4; i++) {
				int j = (i+1)%4;
				int x1 = (int)Math.round(dst_corners[2*i]);
				int y1 = (int)Math.round(dst_corners[2*i+1]);
				int x2 = (int)Math.round(dst_corners[2*j]);
				int y2 = (int)Math.round(dst_corners[2*j+1]);
				cvLine(correspond, cvPoint(x1, y1 ), cvPoint(x2,y2 ), CvScalar.RED, 5, 8, 0);
			}
		}
		
		//for (int i=0; i<find.)

		return new IplImage[]{correspond,train};

		} catch(Exception e) {
			
		}
		return  new IplImage[]{};
	}

	ObjectFinder objFinder;
	IplImage train;
	IplImage trainG;
	@Override
	public void init(IplImage initialImage) {
		Loader.load(opencv_objdetect.class);
		Loader.load(opencv_nonfree.class);
		train = opencv_highgui.cvLoadImage("test2.png", CV_LOAD_IMAGE_GRAYSCALE);
		trainG = IplImage.create(train.width(), train.height(), 8, 3);
		cvCvtColor(train, trainG, CV_GRAY2BGR);
		
		
		
	}

}
