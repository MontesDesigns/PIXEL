
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;


public class PinballHttpHandler extends ImageResourceHttpHandler {
  protected LCDPixelcade lcdDisplay = null;

  public PinballHttpHandler(WebEnabledPixel application) {
    super(application);

    if(WebEnabledPixel.getLCDMarquee().equals("yes"))
      lcdDisplay = new LCDPixelcade();

    this.basePath = "";
    this.defaultImageClassPath = "btime.png";
    this.modeName = "pinball";
  }

  public void handleGIF(String pinTable, String PinAnimationName, Boolean saveAnimation, int loop) {

      try {
          //pixel.writeArcadeAnimation(pinTable, PinAnimationName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
          this.application.getPixel().writePinballAnimation(pinTable, PinAnimationName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
      } catch (NoSuchAlgorithmException ex) {
          Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
  }

//  public void handleGIF(String pinTable, String PinAnimationName, Boolean saveAnimation, int loop) {
//    Pixel pixel = this.application.getPixel();
//
//    try {
//      //pixel.writeArcadeAnimation(pinTable, PinAnimationName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
//      pixel.writePinballAnimation(pinTable, PinAnimationName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
//
//    } catch (NoSuchAlgorithmException ex) {
//      Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
//    }
//  }

  public void writeImageResource(String urlParams) throws IOException, ConnectionLostException {
    Pixel pixel = this.application.getPixel();
    String streamOrWrite = null;
    String pinTable = null;
    String PinAnimationName = null;
    String pinAnimationNameExtension = null;
    String pinAnimationNameOnly = null;
    String arcadeFilePathGIF = null;
    String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
    LogMe logMe = null;
    
    boolean saveAnimation = false;
    boolean overlay = true;
    boolean trainingMode = false;
    int loop_ = 0;
    String text_ = "";
    int scrollsmooth_ = 1;
    Long speeddelay_ = Long.valueOf(10L);
    String speed_ = null;
    Long speed = null;
    String color_ = null;
    Color color = null;
    int i = 0;
    boolean textSelected = false;
    int fontSize_ = 0;
    int yOffset_ = 0;
    int lines_ = 1;
    String font_ = null;
    
    pixelHome = WebEnabledPixel.getHome();
    
    if (WebEnabledPixel.isWindows())
      scrollsmooth_ = 3; 
    
    List<NameValuePair> params = null;
    try {
      params = URLEncodedUtils.parse(new URI(urlParams), "UTF-8");
    } catch (URISyntaxException ex) {
      Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    URI tempURI = null;
    
    try {
      tempURI = new URI("http://localhost:8080" + urlParams);
    } catch (URISyntaxException ex) {
      Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    
    String URLPath = tempURI.getPath();
    String[] arcadeURLarray = URLPath.split("/");
    logMe = LogMe.getInstance();
    
    if (!CliPixel.getSilentMode()) {
      System.out.println("pinball handler received: " + urlParams);
      LogMe.aLogger.info("pinball handler received: " + urlParams);
    } 
    
//    System.out.println(URLPath.toString());
//    System.out.println(arcadeURLarray);
//    System.out.println("length " + arcadeURLarray.length);
//    System.out.println("length " + arcadeURLarray[3]);
    
    if (arcadeURLarray.length == 5) {
      streamOrWrite = arcadeURLarray[2];
      pinTable = arcadeURLarray[3];
      PinAnimationName = arcadeURLarray[4];
      PinAnimationName = PinAnimationName.trim();
      PinAnimationName = PinAnimationName.replace("\n", "").replace("\r", "");
      pinAnimationNameExtension = FilenameUtils.getExtension(PinAnimationName);
      
      if (pinAnimationNameExtension.length() > 3) {
        pinAnimationNameOnly = PinAnimationName;
      } else {
        pinAnimationNameOnly = FilenameUtils.removeExtension(PinAnimationName);
      } 
      
      i = 0;
      for (NameValuePair param : params) {
        i++;
        switch (param.getName()) {
          case "l":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "loop":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "no":  //no overlay , did not implement this yet
            overlay = false;
            break;
          case "t":  //no overlay , did not implement this yet
            trainingMode = true;
            break;
          case "training":  //no overlay , did not implement this yet
            trainingMode = true;
            break;
        } 
      } 
      
      pinTable = pinTable.toLowerCase();
      pinAnimationNameOnly = pinAnimationNameOnly.toLowerCase();
      PinAnimationName = PinAnimationName.toLowerCase();

      
      String noDefaultsPath = pixelHome + "pinball" + File.separator + pinTable + File.separator + "nodefaults.txt";
      File noDefaultsFile = new File(noDefaultsPath);
      
      if (!CliPixel.getSilentMode()) {
        System.out.println(streamOrWrite.toUpperCase() + " MODE");
        System.out.println("Pinball Table or ROM: " + pinTable);
        System.out.println("Pinball Animation: " + pinAnimationNameOnly);
        LogMe.aLogger.info("Pin Table Name: " + pinTable);
        LogMe.aLogger.info("Pinball Animation: " + pinAnimationNameOnly);
      } 
      
      arcadeFilePathGIF = pixelHome + "pinball" + File.separator + pinTable + File.separator + pinAnimationNameOnly + ".gif";  //pixelcade/pinball/table/animation
      File arcadeFileGIF = new File(arcadeFilePathGIF);
      
      if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
        pinAnimationNameOnly = FilenameUtils.removeExtension(PinAnimationName);
        pinTable = "pinball" + File.separator + pinTable;
      }
      else if (trainingMode) {
        text_ = pinAnimationNameOnly;
        int LED_MATRIX_ID = WebEnabledPixel.getMatrixID();
        speed = Long.valueOf(WebEnabledPixel.getScrollingTextSpeed(LED_MATRIX_ID));
        if (speed_ != null) {
          speed = Long.valueOf(speed_);
          if (speed.longValue() == 0L)
            speed = Long.valueOf(10L);
        }

        if (scrollsmooth_ == 0) {
          String scrollSpeedSettings = WebEnabledPixel.getTextScrollSpeed();
          scrollsmooth_ = WebEnabledPixel.getScrollingSmoothSpeed(scrollSpeedSettings);
        }        
        if (font_ == null)
          font_ = WebEnabledPixel.getDefaultFont();

        this.application.getPixel();
        Pixel.setFontFamily(font_);
        if (yOffset_ == 0)
          yOffset_ = WebEnabledPixel.getDefaultyTextOffset();

       
        this.application.getPixel();
        Pixel.setYOffset(yOffset_);
        if (fontSize_ == 0)
          fontSize_ = WebEnabledPixel.getDefaultFontSize();
        this.application.getPixel();
        Pixel.setFontSize(fontSize_);

        pixel.scrollText(text_, loop_, speed.longValue(), color, WebEnabledPixel.pixelConnected, scrollsmooth_);
      }
     else {  //if not      //pixelcade/pinball/auto​
            //magic
            Integer number = Integer.valueOf(pinAnimationNameOnly.substring(1));
            Integer res = 17;
            while (res > 16){
              res = number % 16;
              if (res <= 0) res = 1;
            }

            //magic let's look for an s1 - s16  in the specific table folder
              pinAnimationNameOnly = String.format("s%d",res);
              arcadeFilePathGIF = pixelHome + "pinball" + File.separator + pinTable + File.separator + pinAnimationNameOnly + ".gif";
              arcadeFileGIF = new File(arcadeFilePathGIF);
              if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
                System.out.println("Magic, in romPath: " + pinAnimationNameOnly);
                pinTable = "pinball" + File.separator + pinTable;
                  // pinAnimationNameOnly = FilenameUtils.removeExtension(PinAnimationName);
            } else {
                //Unless we were told not to...
                if (noDefaultsFile.exists()) {
                  if (!CliPixel.getSilentMode()) {
                    System.out.println("NoDefs requested, bailing ");
                    LogMe.aLogger.info("NoDefs requested, bailing ");
                  }
                  return;
                }
                //magic let's get it from the pinball folder...
                pinTable = "pinball/auto";
                pinAnimationNameOnly = String.format("s%d",res);
                arcadeFilePathGIF = pixelHome + "pinball" + File.separator +"auto" + File.separator + pinAnimationNameOnly + ".gif";
                arcadeFileGIF = new File(arcadeFilePathGIF);
                System.out.println("Magically: " + pinAnimationNameOnly);
              }
    } 
      
      //String requestedPath = pixelHome + pinTable + "\\" + pinAnimationNameOnly;
      String requestedPath = arcadeFilePathGIF;
      System.out.println("Actual Path: " + requestedPath);
      
      if (!CliPixel.getSilentMode()) {
            System.out.println("Looking for: " + requestedPath);
            LogMe.aLogger.info("Looking for: " + requestedPath);
      }  
     
        saveAnimation = false; //we're streaming which would be the most common case
        
      if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
            handleGIF(pinTable, pinAnimationNameOnly + ".gif", Boolean.valueOf(saveAnimation), loop_);  //either pinball/gif or pinball/table/gif
      }
      else {
          System.out.println("[ERROR] Cannot find " + requestedPath);
      }
      
    } else {
            System.out.println("[ERROR] URL format incorect, use http://localhost:8080/pinball/stream/<Pinball Table/ROM Name>/<Pinball GIF Name>");
            System.out.println("Example: http://localhost:8080/pinball/stream/tron/s02");
            LogMe.aLogger.severe("[ERROR] URL format incorect, use http://localhost:8080/pinball/stream/<Pinball Table/ROM Name>/<Pinball GIF Name>");
            LogMe.aLogger.severe("Example: http://localhost:8080/pinball/stream/tron/s02");
    } 
  }
}

