
// ListDevices.java
// Andrew Davison, June 2013, ad@fivedots.psu.ac.th

/* List the FrameGrabber video device info.
   I've hardwired the IDs for my two cameras into SnapPics.java
*/

import com.googlecode.javacv.cpp.videoInputLib.*;


public class ListDevices
{

  public static void main( String args[] )
  {  
    int numDevs = videoInput.listDevices();
    System.out.println("No of video input devices: " + numDevs);
    for (int i = 0; i < numDevs; i++)
      System.out.println(" " + i + ": " + videoInput.getDeviceName(i));
  }  // end of main()

} // end of ListDevices class
