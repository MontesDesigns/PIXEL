
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.system.Sleeper;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;

/**
 * @author Roberto Marquez
 */
public class AnimationsHttpHandler extends ImageResourceHttpHandler
{
    public AnimationsHttpHandler(WebEnabledPixel application)
    {
        super(application);
        
        basePath = "animations/";
        defaultImageClassPath = "0rain";
        modeName = "animation";
    }
    
    public void handleGIFCycle(String consoleName, String arcadeName, boolean writeMode, int Marqueeloop, boolean pixelConnected, String text, int Textloop, long speed, Color color, int scrollsmooth) {  //per AtGames request, this will loop between a PNG/GIF and Scrolling Text until Q is interrupted
            Pixel pixel = this.application.getPixel();
            try {
              //pixel.writeArcadeAnimation(consoleName, arcadeName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
              //pixel.ArcadeCycleGIF(consoleName, arcadeName, false, Marqueeloop, WebEnabledPixel.pixelConnected, text, 1, speed, color, scrollsmooth); //hard coding the text loop to 1
              pixel.ArcadeCycleGIF(consoleName, arcadeName, false, 1, WebEnabledPixel.pixelConnected, text, 1, speed, color, scrollsmooth); //hard coding the text loop to 1 and marquee loop to 1

              //String selectedPlatformName, String selectedFileName, boolean writeMode, int Marqueeloop, boolean pixelConnected, String text, int Textloop, long speed, Color color, int scrollsmooth
            } catch (NoSuchAlgorithmException ex) {
              Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
            }
    }
  
  public void handleGIF(String consoleName, String arcadeName, Boolean saveAnimation, int loop) {   
    Pixel pixel = this.application.getPixel();
    try {
      pixel.writeArcadeAnimation(consoleName, arcadeName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    }
  }
  

    
    @Override
    //protected void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException
    protected void writeImageResource(String urlParams) throws IOException, ConnectionLostException
    {
        
        String text_ = "";
        int loop_ = 0;
        String color_ = "";
        String streamOrWrite = null ;
 	String consoleName = null ;
 	String arcadeName = null ;
        String arcadeNameExtension = null; 
        String arcadeNameOnly = null;
        boolean saveAnimation = false;
        boolean randomGIF = false;
        int scrollsmooth_ = 1;
        Long speeddelay_ = Long.valueOf(10L);
        String speed_ = null;
        Long speed = null;
        Color color = null;
        boolean cycle_ = false;
        int fontSize_ = 0;
        int yOffset_ = 0;
        int lines_ = 1;
        String font_ = null;

        //logger.log(Level.INFO, "animation handler is writing " + imageClassPath);
        LogMe logMe = LogMe.getInstance();
        if (!CliPixel.getSilentMode()) {
                System.out.println("animation handler is writing " + urlParams);
                logMe.aLogger.info("animation handler is writing " + urlParams);
        }
        
        List<NameValuePair> params = null;
        try {
                params = URLEncodedUtils.parse(new URI(urlParams), "UTF-8");
            } catch (URISyntaxException ex) {
                Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (NameValuePair param : params) {
           
             switch (param.getName()) {

                    case "t": //scrolling text value
                        text_ = param.getValue();
                        break;
                    case "text": //scrolling speed
                        text_ = param.getValue();
                        break;
                    case "l": //how many times to loop
                        loop_ = Integer.valueOf(param.getValue());
                        // Long speed = Long.valueOf(s); //to do for integer
                        break;
                    case "loop": //loop
                       loop_ = Integer.valueOf(param.getValue());
                       break;
                    case "font":
                        font_ = param.getValue();
                        break;
                    case "c": //color
                       color_ = param.getValue();
                       break;
                    case "r": //random   
                        randomGIF = true;
                        break;
                    case "ss":
                        scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
                        break;    
                    case "scrollsmooth":
                        scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
                         break;
                    case "speed":
                        speed_ = param.getValue();
                         break;
                    case "color":
                        color_ = param.getValue();
                        break;    
                    case "cycle":  //cycles indefintely from PNG/GIF to Text until Q is interrupted
                        cycle_ = true;
                        break;    
                    }
        }
  
        // /animation/stream/pacman?t=x?5=x
        //so now we just need to the left of the ?
        URI tempURI = null;
        try {
             tempURI = new URI("http://localhost:8080" + urlParams);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String URLPath = tempURI.getPath();
        System.out.println("path is: " + URLPath); //path is: /animation/0fire
      
        String [] animationURLarray = URLPath.split("/"); 
        
        logMe = LogMe.getInstance();
        if (!CliPixel.getSilentMode()) {
            System.out.println("animation handler received: " + urlParams);
            logMe.aLogger.info("animation handler received: " + urlParams);
        }
        
        //animations not supported on LCD yet so no need for this call
        
//        if (WebEnabledPixel.getLCDMarquee().equals("yes")) {
//            try {
//                    if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)){
//                        WebEnabledPixel.dxEnvironment = true;
//                        System.out.println("Requested: " + tempURI.getPath());
//                        URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + tempURI.getPath());
//                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                        con.setRequestMethod("GET");
//                        con.getResponseCode();
//                        con.disconnect();
//                    }
//                }catch (  Exception e){}
//        }
        
        //System.out.println("length: " + animationURLarray.length);  //a
        
       if (animationURLarray.length == 4) {
        	
                streamOrWrite = animationURLarray[2];
                arcadeName = animationURLarray[3];

                arcadeName = arcadeName.trim();
                arcadeName = arcadeName.replace("\n", "").replace("\r", "");

                String name1 = FilenameUtils.getName(arcadeName);
                String name2 = FilenameUtils.getBaseName(arcadeName);
                arcadeNameOnly = FilenameUtils.getBaseName(arcadeName); //stripping out the extension

                if (streamOrWrite.equals("write"))  saveAnimation = true;


            
            /*
            // the writeAnimation() method just takes the name of the file
            int i = urlParams.lastIndexOf("/") + 1;
            String animationName = urlParams.substring(i);
            //String arcadeNameOnly = "";

            //System.out.println("string: " + imageClassPath);  //animation/0pacgosts.png or //animation/save/0pacghosts
            //System.out.println("animationName: " + animationName);  //0pacghosts.png

            //boolean saveAnimation = false;

            if( urlParams.contains("/save/") )
            {
                saveAnimation = true;
            }

            arcadeNameOnly = FilenameUtils.getBaseName(urlParams); //get the name only WITHOUT extension as we'll add .gif later

            if( animationName.equals("animations") )  //this is the default when the mode has switched here
            {
                arcadeNameOnly = defaultImageClassPath;
            } 
           */
            
            

             if (!CliPixel.getSilentMode()) {
                System.out.println(streamOrWrite.toUpperCase() + " MODE");
                System.out.println("Animation Name: " +  arcadeNameOnly);
                if (loop_ == 0) {
                    System.out.println("# of Times to Loop: null");
                } else {
                    System.out.println("# of Times to Loop: " + loop_);
                }

                logMe.aLogger.info(streamOrWrite.toUpperCase() + " MODE");
                logMe.aLogger.info("Animation Name: " +  arcadeNameOnly);
                 if (loop_ == 0) {
                    logMe.aLogger.info("# of Times to Loop: null");
                } else {
                    logMe.aLogger.info("# of Times to Loop: " + loop_);
                }
             }
            
            if (!CliPixel.getSilentMode()) {
                    System.out.println("Animation Handler sending GIF: " + arcadeNameOnly + ".gif");
                    logMe.aLogger.info("Animation Handler sending GIF: " + arcadeNameOnly + ".gif");
            }
            
            if (text_ != "" && !WebEnabledPixel.isMister()) {  //scrolling text does not work on MiSTER :-(
                  
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

                if (lines_ == 2) 
                    Pixel.setDoubleLine(true);
                else if (lines_ == 4)
                    Pixel.setFourLine(true);
                else
                    Pixel.setDoubleLine(false); //don't forget to set it back
                
                 if (color_ == null) {
                    if (WebEnabledPixel.getTextColor().equals("random")) {
                      color = WebEnabledPixel.getRandomColor();
                    } else {
                      color = WebEnabledPixel.getColorFromHexOrName(WebEnabledPixel.getTextColor());
                    } 
                  } else {
                    color = WebEnabledPixel.getColorFromHexOrName(color_);
                } 
            } 

            Pixel pixel = application.getPixel();

            if (randomGIF == true) {
                List<String> animationrandom = application.loadAnimationList();
                Random rand = new Random();
                String randomAnimationName = animationrandom.get(new Random().nextInt(animationrandom.size()));
                System.out.println("Random Animation: " + randomAnimationName );
                logMe.aLogger.info("Random Animation:: " + randomAnimationName);
                arcadeNameOnly = FilenameUtils.getBaseName(randomAnimationName); //stripping out the extension
            }
            
            
            if (cycle_ && !WebEnabledPixel.isMister()) {  //scrolling text does not work on MiSTER :-(

                if (text_ != "") {
                  handleGIFCycle("animations/gifsource", arcadeNameOnly + ".gif", false, loop_,true,text_,loop_,speed,color,scrollsmooth_);
                }
                else {
                   if (!CliPixel.getSilentMode()) System.out.println("[ERROR] The cycle param must also have text specified");
                }

            }
            else {
                   try {
                       // pixel.writeAnimation(animationName, saveAnimation); //old code, this only worked for gifs that were in the original jar
                       pixel.writeAnimationFilePath("animations", arcadeNameOnly + ".gif", saveAnimation,loop_,WebEnabledPixel.pixelConnected);  
                   } catch (NoSuchAlgorithmException ex) {
                       //Logger.getLogger(AnimationsHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
                   }
            }
            
            
            
//            try {
//                // pixel.writeAnimation(animationName, saveAnimation); //old code, this only worked for gifs that were in the original jar
//                pixel.writeAnimationFilePath("animations", arcadeNameOnly + ".gif", saveAnimation,loop_,WebEnabledPixel.pixelConnected);  
//            } catch (NoSuchAlgorithmException ex) {
//                //Logger.getLogger(AnimationsHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
        
        } else {
            
             System.out.println("** ERROR ** URL format incorect, use http://localhost:8080/animations/<stream or write>/<animation name");
             System.out.println("Example: http://localhost:8080/animations/stream/0fire.gif");
             logMe.aLogger.severe("** ERROR ** URL format incorect, use http://localhost:8080/animations/<stream or write>/<animation name");
             logMe.aLogger.severe("Example: http://localhost:8080/animations/stream/0fire.gif");
        }
        
        
    }
}
