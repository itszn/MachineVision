import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;


public class FinalProject implements ImageProcesser{
	static CameraAPI api;
	static FinalProject instance;
	public static void main(String...args) {
		instance = new FinalProject();
		api = new CameraAPI(0,true,"",instance);
		api.start();
	}


	IplImage fgMask;
	@Override
	public IplImage[] process(IplImage currentImage, IplImage lastImage, IplImage... finalImage) {
		// TODO Auto-generated method stub
		IplImage background = api.getNewImage();
		 
		IplImage foreground = api.getNewImage();

		mog.apply(currentImage, fgMask, 0.005);
		mog.getBackgroundImage(background);
		
		opencv_core.cvAbsDiff(background, currentImage , foreground);

		opencv_imgproc.cvErode(foreground, foreground, null, 5);
		opencv_imgproc.cvDilate(foreground, foreground, null, 5);
		opencv_imgproc.cvSmooth(foreground, foreground, opencv_imgproc.CV_BLUR, 1);  // more noise reduction
		opencv_imgproc.cvThreshold(foreground, foreground, 25, 255, opencv_imgproc.CV_THRESH_BINARY);   // make b&w 

		return new IplImage[]{foreground.clone(),background.clone()};
	}


	BackgroundSubtractorMOG2 mog;
	private static CvMemStorage contourStorage;
	@Override
	public void init(IplImage initialImage) {
		Loader.load(opencv_objdetect.class);
		
	    contourStorage = CvMemStorage.create();
		mog = new BackgroundSubtractorMOG2(300, 16, false);
		// a larger motion history tends to create larger blobs
		// motion history, var Threshold, Shadow Detection
		mog.set("nmixtures", 3);    // was 5
		System.out.println("MOG num. mixtures: " + mog.getInt("nmixtures"));
		System.out.println("MOG shadow detection: " + mog.getBool("detectShadows"));

		try {
			System.out.println("MOG background ratio: " + mog.getDouble("backgroundRatio"));
			System.out.println("MOG var threshold gen: " + mog.getDouble("varThresholdGen"));
			System.out.println("MOG fVar init: " + mog.getDouble("fVarInit") + ", min: " +
					mog.getDouble("fVarMin") + ", max: " + mog.getDouble("fVarMax") );
		}
		catch (RuntimeException e)
		{  System.out.println(e);  }
		
		fgMask = api.getNewImage();
	}

}
