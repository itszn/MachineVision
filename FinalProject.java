import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class FinalProject implements ImageProcesser{

	public static void main(String...args) {
		FinalProject instance = new FinalProject();
		new CameraAPI(0,true,"",instance);
	}
	
	@Override
	public void process(IplImage currentImage, IplImage finalImage) {
		// TODO Auto-generated method stub
		opencv_core.cvCopy(currentImage, finalImage);
	}
	
}
