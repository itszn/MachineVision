import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;


public class FinalProject implements ImageProcesser{
	static CameraAPI api;
	static FinalProject instance;
	public static void main(String...args) {
		instance = new FinalProject();
		api = new CameraAPI(0,true,"",instance);
		api.start();
	}



	@Override
	public void process(IplImage currentImage, IplImage lastImage, IplImage finalImage) {
		// TODO Auto-generated method stub
		IplImage background = api.getNewImage();
		IplImage fgMask = api.getNewImage();


		mog.apply(currentImage, fgMask, 0.005);
		mog.getBackgroundImage(background);

		opencv_imgproc.cvErode(fgMask, fgMask, null, 5);
		opencv_imgproc.cvDilate(fgMask, fgMask, null, 5);
		opencv_imgproc.cvSmooth(fgMask, fgMask, opencv_imgproc.CV_BLUR, 5);  // more noise reduction
		opencv_imgproc.cvThreshold(fgMask, fgMask, 128, 255, opencv_imgproc.CV_THRESH_BINARY);   // make b&w 

		opencv_core.cvCopy(background, finalImage);
	}


	BackgroundSubtractorMOG2 mog;

	@Override
	public void init(IplImage initialImage) {
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
	}

}
