import com.googlecode.javacv.cpp.opencv_core.IplImage;


public interface ImageProcesser {

	public IplImage[] process(IplImage currentImage, IplImage lastImage, IplImage... finalImage);
	
	public void init(IplImage initialImage);
}
