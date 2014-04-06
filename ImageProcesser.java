import com.googlecode.javacv.cpp.opencv_core.IplImage;


public interface ImageProcesser {

	public void process(IplImage currentImage, IplImage finalImage);
}
