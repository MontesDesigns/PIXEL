
package org.onebeartoe.web.enabled.pixel.controllers;
//For LED
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.setCurrentPlatformGame;
import com.sun.net.httpserver.HttpExchange;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
//import static org.apache.velocity.texen.util.FileUtil.file;

//This class should do the following
//1. check if the achievement animation or image is there and play it if so
//2. if not there, then play a generic animation choosing from 1 of 10 different ones
//3. Scroll text of the achievement
//4. Then go back to the arcade marquee or go back to cycle mode if we were in cycle mode prior cycling high scores
//5. API call will be: localhost:8080/achievements/stream/mame/achievement-id?t=scrollingtext&currentgame=rom-name&cyclemodeprior=yes

public class AchievementsHttpHandler extends ImageResourceHttpHandler {
  //protected LCDPixelcade lcdDisplay = null;
 private static Integer LEDStripRed = 0;
 private static Integer LEDStripGreen = 0;
 private static Integer LEDStripBlue = 0;
 private String arcadeNameOnlyPNG = null;
 private String arcadeFilePathPNG = null;
 private String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"

 private String streamOrWrite = null;
 private String consoleName = null;
 private String arcadeName = null;
 private String arcadeNameExtension = null;
 private String arcadeNameOnlyGIF = null;
 private String achievementsGIF = null;
 private String arcadeFilePathGIF = null;
 private String consoleFilePathPNG = null;
 private String consoleFilePathGIF = null;
 private String defaultConsoleFilePathPNG = null;
 private String consoleNameMapped = null;
 private Integer animationVersionCounter = 3;
 private File   arcadeFilePNG;
 private File   arcadeFileGIF;
 private String currentGameConsole = null;
private   boolean saveAnimation = false;
private    int loop_ = 0;
private    String text_ = "";
 private   int scrollsmooth_ = 1;
 private   Long speeddelay_ = Long.valueOf(10L);
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
    String event_ = null;
    boolean cycle_ = false;
    String currentgame_ = null;
    String cyclemodeprior_ = null;


//  private static String speed_ = null;
//  private static Long speed = null;
//  private static int scrollsmooth_ = 1;
//  private static Long speeddelay_ = Long.valueOf(10L);
//  private static int fontSize_ = 0;
//  private static int yOffset_ = 0;
//  private static int lines_ = 1;
//  private static String font_ = null;

  public AchievementsHttpHandler(WebEnabledPixel application) {
    super(application);
    
    //if(WebEnabledPixel.getLCDMarquee().equals("yes")) //no longer needed since we are not doings 2nd HDMI use case
    //   lcdDisplay = new LCDPixelcade();

    this.basePath = "";
    this.defaultImageClassPath = "btime.png";
    this.modeName = "arcade";
  }
  
  public void handlePNG(File arcadeFilePNGFullPath, Boolean saveAnimation, int loop, String consoleNameMapped, String PNGNameWithExtension) throws MalformedURLException, IOException, ConnectionLostException {
    
    LogMe logMe = LogMe.getInstance();
    Pixel pixel = this.application.getPixel();
    pixel.writeArcadeImage(arcadeFilePNGFullPath, saveAnimation, loop, consoleNameMapped, PNGNameWithExtension, WebEnabledPixel.pixelConnected);
    
  }
  
  public void handlePNGCycle(File PNGFileFullPath, Boolean writeMode, int loop, String consoleNameMapped, String PNGNameWithExtension, boolean pixelConnected, String text, int Textloop, long speed, Color color, int scrollsmooth) {  //per AtGames request, this will loop between a PNG/GIF and Scrolling Text until Q is interrupted
    Pixel pixel = this.application.getPixel();
    try {
      pixel.ArcadeCyclePNG(PNGFileFullPath,false, 5, consoleNameMapped, PNGNameWithExtension, WebEnabledPixel.pixelConnected, text, 1, speed, color, scrollsmooth); //hard code text loop to 1 as the png loop will be longer like 5s and hard coding PNG to 5 as we aren't going to know if PNG or GIF
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
  
   public void handleGIFCycle(String consoleName, String arcadeName, boolean writeMode, int Marqueeloop, boolean pixelConnected, String text, int Textloop, long speed, Color color, int scrollsmooth) {  //per AtGames request, this will loop between a PNG/GIF and Scrolling Text until Q is interrupted
    Pixel pixel = this.application.getPixel();
    try {
      //pixel.writeArcadeAnimation(consoleName, arcadeName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
      pixel.ArcadeCycleGIF(consoleName, arcadeName, false, Marqueeloop, WebEnabledPixel.pixelConnected, text, 1, speed, color, scrollsmooth); //hard coding the text loop to 1
      
      //String selectedPlatformName, String selectedFileName, boolean writeMode, int Marqueeloop, boolean pixelConnected, String text, int Textloop, long speed, Color color, int scrollsmooth
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    }
  }
  
  
  
  public void writeImageResource(String urlParams) throws IOException, ConnectionLostException {
    Pixel pixel = this.application.getPixel();
    LogMe logMe = null;
    File arcadeFileGIF = new File(pixelHome);
    
    String[] consoleArray = { 
        "mame", "atari2600", "daphne", "nes", "neogeo", "atarilynx", "snes", "atari5200", "atari7800", "atarijaguar", 
        "c64", "genesis", "capcom", "n64", "psp", "psx", "coleco", "dreamcast", "fba", "gb", 
        "gba", "ngp", "ngpc", "odyssey", "saturn", "megadrive", "gbc", "gamegear", "mastersystem", "sega32x", 
        "3do", "msx", "atari800", "pc", "nds", "amiga", "fds", "futurepinball", "amstradcpc", "apple2", 
        "intellivision", "macintosh", "ps2", "pcengine", "segacd", "sg-1000", "ti99", "vectrex", "virtualboy", "visualpinball", 
        "wonderswan", "wonderswancolor", "zinc", "sss", "zmachine", "zxspectrum" };
    
    pixelHome = WebEnabledPixel.getHome();
    WebEnabledPixel.setTickerRunning(false); //this will kill the ticker (if it's running), note it may not kill immeditely though if only loop commands come in, to do here
    
    if (WebEnabledPixel.isWindows())
      scrollsmooth_ = 3; 
    
    List<NameValuePair> params = null;
    try {
      params = URLEncodedUtils.parse(new URI(urlParams), "UTF-8");
    } catch (URISyntaxException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    URI tempURI = null;
    
    try {
      tempURI = new URI("http://127.0.0.1:8080" + urlParams);
    } catch (URISyntaxException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    
    String URLPath = tempURI.getPath();
    //System.out.println("[Original URL]: " + URLPath);
    
    
    String[] arcadeURLarray = URLPath.split("/");
    logMe = LogMe.getInstance();
    
    if (!CliPixel.getSilentMode()) {
      System.out.println("achievements handler received: " + urlParams);
      LogMe.aLogger.info("achievements handler received: " + urlParams);
    } 
    
    if (arcadeURLarray.length == 5) {
      streamOrWrite = arcadeURLarray[2];
      consoleName = arcadeURLarray[3];
      arcadeName = arcadeURLarray[4];
      arcadeName = arcadeName.trim();
      arcadeName = arcadeName.replace("\n", "").replace("\r", "");
      arcadeNameExtension = FilenameUtils.getExtension(arcadeName);
      
      if (arcadeNameExtension.length() > 3) {
        arcadeNameOnlyGIF = arcadeName;
        arcadeNameOnlyPNG = arcadeName;
      } else {
        arcadeNameOnlyGIF = FilenameUtils.removeExtension(arcadeName);
        arcadeNameOnlyPNG = FilenameUtils.removeExtension(arcadeName);
      } 
      
      i = 0;
      for (NameValuePair param : params) {
        i++;
        switch (param.getName()) {
          case "t":
            text_ = param.getValue();
            textSelected = true;
            break;
          case "text":
            text_ = param.getValue();
            textSelected = true;
             break;
          case "l":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "loop":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "gt":
            text_ = WebEnabledPixel.getGameName(arcadeNameOnlyGIF);
             break;
          case "gametitle":
            text_ = WebEnabledPixel.getGameName(arcadeNameOnlyGIF);
             break;
          case "ss":
            scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "font":
            font_ = param.getValue();
             break;
          case "size":
            fontSize_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "yoffset":
            yOffset_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "lines":
            lines_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "scrollsmooth":
            scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "speed":
            speed_ = param.getValue();
             break;
          case "c":
            color_ = param.getValue();
             break;
          case "color":
            color_ = param.getValue();
            break;
          case "event":
           event_ = param.getValue();
           break;
          case "cycle":  //cycles indefintely from PNG/GIF to Text until Q is interrupted
           cycle_ = true;
           break;
          case "currentgame": 
           currentgame_ = param.getValue();
           break;
          case "cyclemodeprior":
           cyclemodeprior_ = param.getValue();
           break;
        } 
      } 
      
      consoleName = consoleName.replace(" ", "_"); //had to add this as Dennis made the change to send the native console name with spaces as prior code and mapping tables assumed an _ instead of space
      consoleName = consoleName.toLowerCase();
      if (!consoleMatch(consoleArray, consoleName)) {
        consoleNameMapped = WebEnabledPixel.getConsoleMapping(consoleName);
      } else {
        consoleNameMapped = consoleName;
      } 
      
      if (consoleNameMapped.equals("mame-libretro"))
        consoleNameMapped = "mame"; 
      
      currentGameConsole = consoleNameMapped;
     // consoleNameMapped = "achievements"; //not sure why I had this as achievements is part of the core url?
      
      if (!CliPixel.getSilentMode()) {
        System.out.println(streamOrWrite.toUpperCase() + " MODE");
        System.out.println("Console Before Mapping: " + consoleName);
        System.out.println("Console Mapped: " + consoleNameMapped);
        System.out.println("Game Name Only: " + arcadeNameOnlyGIF);
        
        if (loop_ == 0) {
          System.out.println("# of Times to Loop: null");
        } else {
          System.out.println("# of Times to Loop: " + loop_);
        } 
        
        
        if (text_ != "")
          System.out.println("alt text if game file not found: " + text_);
 
        LogMe.aLogger.info(streamOrWrite.toUpperCase() + " MODE");
        LogMe.aLogger.info("Console Before Mapping: " + consoleName);
        LogMe.aLogger.info("Console Mapped: " + consoleNameMapped);
        LogMe.aLogger.info("Game Name Only: " + arcadeNameOnlyGIF);
        if (loop_ == 0) {
          LogMe.aLogger.info("# of Times to Loop: null");
        } else {
          LogMe.aLogger.info("# of Times to Loop: " + loop_);
        } 
        if (text_ != "")
          LogMe.aLogger.info("alt text if marquee file not found: " + text_); 
        } 
      
      //let's set the text & font properties if we have alt text
     if (text_ != "" && !WebEnabledPixel.isMister()) {  //scrolling text does not work on MiSTER :-(
          
     if (WebEnabledPixel.getLCDMarquee().equals("yes")) {
            try {
                if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)) {
                    WebEnabledPixel.dxEnvironment = true;

                    URL url = null;
                    if (WebEnabledPixel.getLCDLEDCompliment() && WebEnabledPixel.pixelConnected) {
                        String encodedText = URLEncoder.encode(text_, StandardCharsets.UTF_8.toString());
                        String encodedCurrentGame = URLEncoder.encode(currentgame_, StandardCharsets.UTF_8.toString());

                        url = new URL("http://" + getLCDMarqueeHostName() + ":8080/achievements/stream/"
                                + consoleNameMapped + "/" + arcadeNameOnlyPNG
                                + "?t=" + encodedText + "&currentgame=" + encodedCurrentGame + "&led");
                    } else {
                        String encodedText = URLEncoder.encode(text_, StandardCharsets.UTF_8.toString());
                        String encodedCurrentGame = URLEncoder.encode(currentgame_, StandardCharsets.UTF_8.toString());

                        url = new URL("http://" + getLCDMarqueeHostName() + ":8080/achievements/stream/"
                                + consoleNameMapped + "/" + arcadeNameOnlyPNG
                                + "?t=" + encodedText + "&currentgame=" + encodedCurrentGame);
                    }

                    System.out.println("Achievements URL to send to LCD is: " + url.toString());

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.getResponseCode();
                    con.disconnect();
                }
            } catch (Exception e) {
                // Handle exceptions
            }
    }   

          
    //***** if we have an LCD, let's relay the call over to Pixelcade LCD too
//     if (WebEnabledPixel.getLCDMarquee().equals("yes")) {  //this is where we relay the scrolling text call to LCD
//        try {
//                if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)){
//                    WebEnabledPixel.dxEnvironment = true;
//                    
//                     URL url = null;
//                     if (WebEnabledPixel.getLCDLEDCompliment() == true && WebEnabledPixel.pixelConnected == true) { //then we need to add &led to the end of the URL params
//                        //String textURL = requestURI.toString();
//                        url = new URL("http://" + getLCDMarqueeHostName() + ":8080/achievements/stream/" + consoleNameMapped + "/" + arcadeNameOnlyPNG + "?t=" + text_ + "&currentgame=" + currentgame_ + "&led"); //this flag tells LCD not to scroll as we already have LED scrolling
//                     }
//                     else {
//                        //url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + requestURI);
//                        url = new URL("http://" + getLCDMarqueeHostName() + ":8080/achievements/stream/" + consoleNameMapped + "/" + arcadeNameOnlyPNG + "?t=" + text_ + "&currentgame=" + currentgame_); //this flag tells LCD not to scroll as we already have LED scrolling
//                     }
//                     
//                    System.out.println("Achievements URL to send to LCD is: " + url.toString());
//
//                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                    con.setRequestMethod("GET");
//                    con.getResponseCode();
//                    con.disconnect();
//                }
//        }catch (Exception e){}
//    }    
                  
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
                //Pixel.setFontFamily(font_);
                Pixel.setFontFamily("Advanced Pixel-7");
                if (yOffset_ == 0)
                  yOffset_ = WebEnabledPixel.getDefaultyTextOffset(); 

                this.application.getPixel();
                Pixel.setYOffset(yOffset_);
                if (fontSize_ == 0)
                  fontSize_ = WebEnabledPixel.getDefaultFontSize(); 
                this.application.getPixel();
               // Pixel.setFontSize(fontSize_);
                Pixel.setFontSize(20);

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

        //now let's find the matching PNG
      
      arcadeFilePathPNG = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyPNG + ".png";
      File arcadeFilePNG = new File(arcadeFilePathPNG);
      
      if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
        arcadeNameOnlyPNG = FilenameUtils.removeExtension(arcadeName);
        
        if (WebEnabledPixel.LEDStripExists()) { 
            setStripColor(arcadeFilePNG);
        }
        
      } else {
        String arcadeNameOnlyGIFUnderscore = arcadeNameOnlyPNG.replaceAll("_", " ");
        String arcadeFilePathPNGUnderscore = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyGIFUnderscore + ".png";
        arcadeFilePNG = new File(arcadeFilePathPNGUnderscore);
        
        if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
          arcadeNameOnlyPNG = arcadeNameOnlyGIFUnderscore;
           if (WebEnabledPixel.LEDStripExists()) {  
                setStripColor(arcadeFilePNG);
           }
        } else {
          String arcadeNamelowerCase = arcadeNameOnlyPNG.toLowerCase();
          String arcadeFilePathPNGlowerCase = pixelHome + consoleNameMapped + "/" + arcadeNamelowerCase + ".png";
          arcadeFilePNG = new File(arcadeFilePathPNGlowerCase);
          if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory())
            arcadeNameOnlyPNG = arcadeNamelowerCase; 
            if (WebEnabledPixel.LEDStripExists()) {  
                setStripColor(arcadeFilePNG);
           }
        } 
      } 
      
       // Now let's find the matching GIF
       // let's first check if there is a ( and if so, take only text to the left
       // then let's check if there is basename_01, basename_02, or basename_03
       // let's first check if there is a ( and if so , we'll take what is to the left
       // So here's our logic for the gifs, let's first check if arcadenameonly_03 exists and if so we'll take that and then increment down to arcadenameonly_02 for the next one
       
      
        int iend = arcadeNameOnlyGIF.indexOf("("); //this finds the first occurrence of "(" 
        //in string thus giving you the index of where it is in the string

        //String subString;
        if (iend != -1)  { //then there was a ( there
            arcadeNameOnlyGIF = (arcadeNameOnlyGIF.substring(0 , iend)).trim(); //this will the name to the left of (
            //System.out.println("parathesis is here: " + arcadeNameOnlyGIF);
            
            if (WebEnabledPixel.isWindows()) {
               arcadeNameOnlyGIF = arcadeNameOnlyGIF.replaceAll("_", " ").trim(); //windows will add an _ for a space so taking care of that here
               //System.out.println("windows call parathesis is here: " + arcadeNameOnlyGIF);
            }
        
            //ok now since we have a ( match for the GIF, let's also see if we have a matching PNG without the (
             String arcadeFilePathPNGTest = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyGIF + ".png";
             File arcadeFilePNGTest = new File(arcadeFilePathPNGTest);
             if (arcadeFilePNGTest.exists() && !arcadeFilePNGTest.isDirectory()) {
                    arcadeNameOnlyPNG = arcadeNameOnlyGIF;
                    arcadeFilePNG = new File(arcadeFilePathPNGTest); //we have to set the new arcadeFilePNG here as we use it in the handlePNG call
             }     
        }  
        
       arcadeFilePathGIF = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyGIF + "_0" + WebEnabledPixel.getAnimationNumber().toString() + ".gif";
       arcadeFileGIF = new File(arcadeFilePathGIF);
       
       if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
            arcadeNameOnlyGIF = FilenameUtils.getBaseName(arcadeFilePathGIF);
       } else {  //this means we did not find the multiple version so proceed how we were searching before
           
            arcadeFilePathGIF = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyGIF + ".gif";
            arcadeFileGIF = new File(arcadeFilePathGIF);
      
            if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
              //arcadeNameOnlyGIF = FilenameUtils.removeExtension(arcadeName);  //not sure why this was here?
            } 
            else {
              String arcadeNameOnlyGIFUnderscore = arcadeNameOnlyGIF.replaceAll("_", " ");
              String arcadeFilePathGIFUnderscore = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyGIFUnderscore + ".gif";
              arcadeFileGIF = new File(arcadeFilePathGIFUnderscore);

              if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
                arcadeNameOnlyGIF = arcadeNameOnlyGIFUnderscore;
              } else {
                String arcadeNamelowerCase = arcadeNameOnlyGIF.toLowerCase();
                String arcadeFilePathGIFlowerCase = pixelHome + consoleNameMapped + "/" + arcadeNamelowerCase + ".gif";
                arcadeFileGIF = new File(arcadeFilePathGIFlowerCase);
                if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory())
                  arcadeNameOnlyGIF = arcadeNamelowerCase; 
              } 
            } 
       } 
      
      String requestedPathPNG = pixelHome + consoleNameMapped + "\\" + arcadeNameOnlyPNG;
      String requestedPath = pixelHome + consoleNameMapped + "\\" + arcadeNameOnlyGIF;
      
    if (!CliPixel.getSilentMode()) {
            System.out.println("Looking for PNG: " + requestedPathPNG + ".png");
            LogMe.aLogger.info("Looking for PNG: " + requestedPathPNG + ".png");
            System.out.println("Looking for GIF: " + requestedPath + ".gif");
            LogMe.aLogger.info("Looking for GIF: " + requestedPath + ".gif");
            System.out.println("arcadename only gif: " + requestedPath);
            System.out.println("arcadename only png: " + requestedPathPNG);
    }
       saveAnimation = false; //we're streaming , no writes here
        
        if (WebEnabledPixel.arduino1MatrixConnected) {
          WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getGameMetaData(arcadeNameOnlyPNG));
          LogMe.aLogger.info("Accessory Call: " + WebEnabledPixel.getGameMetaData(arcadeNameOnlyPNG));
        } 
       
        if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory() && arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {  //if there is both a png and a gif, we'll play gif and then png
            
             if (WebEnabledPixel.isUnix() && !WebEnabledPixel.isEmuELEC()) { //this is a quick hack and breaks the Q on Pi but on EmuELEC we can add the extra scroll event params so let's not do the hack there
                        handlePNG(arcadeFilePNG, Boolean.valueOf(false), 0, "black", "nodata");
                        handleGIF(consoleNameMapped, arcadeNameOnlyGIF + ".gif", Boolean.valueOf(saveAnimation), 1); //send the gif with a loop
                        //handlePNG(arcadeFilePNG, Boolean.valueOf(saveAnimation), 99999, consoleNameMapped, arcadeNameOnlyPNG + ".png");
                        continueAchievements();
             }
             else {


                    if (pixel.getLoopStatus() == false || event_.equals("FEScroll")) {   //note if it's a text scroll before this like high score for example, that would be looping so we won't interrupt here
                        handlePNG(arcadeFilePNG, Boolean.valueOf(false), 0, "black", "nodata"); //interrupting the previous one playing
                        //System.out.println("FEScroll Interrupt");                             // to do this is an issue for front end who have not added FEScroll
                    }

                    if (loop_ == 0 || loop_ == 99999) {  //we'll need to loop the GIF in the Q before the PNG plays, had to add 99999 because the gif will loop on 99999 forever and not get to the PNG
                       loop_ = 1;                       // to do this is screwing up other stuff
                    }

                    handleGIF(consoleNameMapped, arcadeNameOnlyGIF + ".gif", Boolean.valueOf(saveAnimation), loop_); //send the gif with a loop
                    continueAchievements();
                    //handlePNG(arcadeFilePNG, Boolean.valueOf(saveAnimation), 99999, consoleNameMapped, arcadeNameOnlyPNG + ".png"); //send the PNG with 99999 so stays on the PNG, changed to arcadeNameOnlyPNG as the GIF can now be different with the animation versions
            }       
           

            //to do known issue here in that if scrolling through front end and one with gif and png are selected back to back, the second one won't interrupt and must complete before the next
        
        } else if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {  //there is only a PNG match
            
               
              handlePNG(arcadeFilePNG, false, 10, consoleNameMapped, arcadeNameOnlyPNG + ".png");
              continueAchievements();
                
            
	} else if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {   
               
               handleGIF(consoleNameMapped, arcadeNameOnlyGIF + ".gif", Boolean.valueOf(saveAnimation), 1);
               continueAchievements();
                
                
        //so there was no achievement specific image so let's do our normal thing and play a random animation followed by scrolling text
        
        } else {
            //so first let's pick the random animation we want to play, there are 10 of them and loop it one time
            
            Random rand = new Random(); //instance of random class
            int upperbound = 11;
            //generate random values from 0-10
            int int_random = rand.nextInt(upperbound);
            
            String achievementsPath = pixelHome + "achievements" + "/" + "achievements" + int_random + ".gif";
            File achievementsFile = new File(achievementsPath);

            if (achievementsFile.exists() && !achievementsFile.isDirectory()) {  //need to make sure the file is actually there, it should be unless the user deleted it from the /pixelcade/achievements folder
                handleGIF("achievements", "achievements" + int_random + ".gif", false, 1); //no loop here as we need to interrupt
            }
            else {
                 if (!CliPixel.getSilentMode()) System.out.println("[ERROR] " + achievementsPath + " does not exist");
            }
            
            continueAchievements();
                
        }
                
        
//      
//          
//        } else {                                                                                            //no alt text was given and no PNG or GIF match so let's get the default console GIF
//                consoleFilePathPNG = pixelHome + "console/default-" + consoleNameMapped + ".png";
//                File consoleFilePNG = new File(consoleFilePathPNG);
//                consoleFilePathGIF = pixelHome + "console/default-" + consoleNameMapped + ".gif";
//                File consoleFileGIF = new File(consoleFilePathGIF);
//                if (consoleFilePNG.exists() && !consoleFilePNG.isDirectory()) {
//                    
//                    
//                    if (cycle_ && text_ != "" && !WebEnabledPixel.isMister()) {
//                          handlePNGCycle(consoleFilePNG, false, loop_,"console",FilenameUtils.getName(consoleFilePathPNG),true,text_,loop_,speed,color,scrollsmooth_);
//                    }  
//                    else {    
//                          handlePNG(consoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(consoleFilePathPNG));
//                    } 
//                  
//                  
//                } else if (consoleFileGIF.exists() && !consoleFileGIF.isDirectory()) {
//                  if (!CliPixel.getSilentMode()) {
//                    System.out.println("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
//                    LogMe.aLogger.info("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
//                  } 
//                  handleGIF("console", "default-" + consoleNameMapped + ".gif", Boolean.valueOf(saveAnimation), loop_);
//                } else {
//                  if (!CliPixel.getSilentMode()) {
//                    System.out.println("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
//                    LogMe.aLogger.info("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
//                  } 
//                  defaultConsoleFilePathPNG = pixelHome + "console/default-marquee.png";
//                  File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
//                  if (defaultConsoleFilePNG.exists() && !defaultConsoleFilePNG.isDirectory()) {
//                    handlePNG(defaultConsoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(defaultConsoleFilePathPNG));
//                  } else if (!CliPixel.getSilentMode()) {
//                    System.out.println("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
//                    System.out.println("Skipping LED marquee " + streamOrWrite + ", please check the files");
//                    LogMe.aLogger.info("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
//                    LogMe.aLogger.info("Skipping LED marquee " + streamOrWrite + ", please check the files");
//                  } 
//                } 
//              } 
      
    } else {
      if (!CliPixel.getSilentMode()) {
        System.out.println("[ERROR] URL format incorect, use http://localhost:8080/arcade/<stream or write>/<platform name>/<game name .gif or .png>");
        System.out.println("Example: http://localhost:8080/arcade/write/mame/pacman.png or http://localhost:8080/arcade/stream/atari2600/digdug.gif");
        LogMe.aLogger.severe("[ERROR] URL format incorect, use http://localhost:8080/arcade/<stream or write>/<platform name>/<game name .gif or .png>");
        LogMe.aLogger.severe("Example: http://localhost:8080/arcade/write/mame/pacman.png or http://localhost:8080/arcade/stream/atari2600/digdug.gif");
      }
      //the URL call was bad so lets just display a default so at least something happens
      String noMatchFilePathPNG = pixelHome + "console" + "/" + "default-marquee" + ".png";
      File noMatchFilePNG = new File(noMatchFilePathPNG);
      handlePNG(noMatchFilePNG, false, 0, "console", "default-marquee.png");
      
    } 
  }
  
  
  private void continueAchievements() throws IOException, MalformedURLException, ConnectionLostException {
            Pixel pixel = this.application.getPixel();
            
            if (!WebEnabledPixel.isMister() && text_ != "")  {
                    pixel.scrollText(text_, 1, speed.longValue(), color, WebEnabledPixel.pixelConnected, scrollsmooth_);
            }
              
            String PNGPath = pixelHome + currentGameConsole + "/" + currentgame_ + ".png";
            File PNGPathFile = new File(PNGPath);
            
            if (PNGPathFile.exists() && !PNGPathFile.isDirectory()) { 
                handlePNG(PNGPathFile, false, 99999, currentGameConsole, currentgame_ + ".png");
            }
            else {  //there is no game PNG so let's get the default console PNG
                
                consoleFilePathPNG = pixelHome + "console/default-" + currentGameConsole + ".png";
                File consoleFilePNG = new File(consoleFilePathPNG);
                consoleFilePathGIF = pixelHome + "console/default-" + currentGameConsole + ".gif";
                File consoleFileGIF = new File(consoleFilePathGIF);
                if (consoleFilePNG.exists() && !consoleFilePNG.isDirectory()) {
                   
                     handlePNG(consoleFilePNG, Boolean.valueOf(saveAnimation), 99999, "console", FilenameUtils.getName(consoleFilePathPNG));
                
                } else if (consoleFileGIF.exists() && !consoleFileGIF.isDirectory()) {
                  if (!CliPixel.getSilentMode()) {
                    System.out.println("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
                    LogMe.aLogger.info("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
                  } 
                  handleGIF("console", "default-" + currentGameConsole + ".gif", Boolean.valueOf(saveAnimation), 99999);
                } else {
                  if (!CliPixel.getSilentMode()) {
                    System.out.println("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
                    LogMe.aLogger.info("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
                  } 
                  defaultConsoleFilePathPNG = pixelHome + "console/default-marquee.png";
                  File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
                  if (defaultConsoleFilePNG.exists() && !defaultConsoleFilePNG.isDirectory()) {
                    handlePNG(defaultConsoleFilePNG, Boolean.valueOf(saveAnimation), 99999, "console", FilenameUtils.getName(defaultConsoleFilePathPNG));
                  } else if (!CliPixel.getSilentMode()) {
                    System.out.println("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
                    System.out.println("Skipping LED marquee " + streamOrWrite + ", please check the files");
                    LogMe.aLogger.info("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
                    LogMe.aLogger.info("Skipping LED marquee " + streamOrWrite + ", please check the files");
                  } 
                }
                
            }
  }
  

  
  private static void setStripColor (File file) {
        
    try {
        processPNGDominateColor(file);
    } catch (IOException ex) {
        Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
    if (!CliPixel.getSilentMode()) {
        System.out.println("Red: " + LEDStripRed);
        System.out.println("Green: " + LEDStripGreen);
        System.out.println("Blue: " + LEDStripBlue);
     }
    WebEnabledPixel.setLEDStripColor(LEDStripRed, LEDStripGreen, LEDStripBlue);
}
           
  public static boolean consoleMatch(String[] arr, String targetValue) {
    for (String s : arr) {
      if (s.equals(targetValue))
        return true; 
    } 
    return false;
  }
  
  public int getMaxAnimationsPerGame(String baseName) { //this will tell us how many GIFs there are per rom, ex. pacman_01, pacman_02, pacman_03, etc.
      
      int max = 0;
      Boolean match = true;     
      int i = 0;
      while (match) {
          i++;
          File targetFile = new File(String.format("%s/%s_%d.gif",WebEnabledPixel.getHome(),baseName,i));
          if (targetFile.exists() && !targetFile.isDirectory())
              match = true;
          else
              match = false;
      } 
      // ok we didn't have a match so are done with the while loop, let's set max to i
      max = i;
      return max;
  }
  
  private static void processPNGDominateColor (File file) throws IOException {
        ImageInputStream is = ImageIO.createImageInputStream(file);
        Iterator iter = ImageIO.getImageReaders(is);

        if (!iter.hasNext())
        {
            if (!CliPixel.getSilentMode()) System.out.println("Cannot load the specified file "+ file);
            System.exit(1);
        }
        ImageReader imageReader = (ImageReader)iter.next();
        imageReader.setInput(is);

        BufferedImage image = imageReader.read(0);

        int height = image.getHeight();
        int width = image.getWidth();

        Map m = new HashMap();
        for(int i=0; i < width ; i++)
        {
            for(int j=0; j < height ; j++)
            {
                int rgb = image.getRGB(i, j);
                int[] rgbArr = getRGBArr(rgb);                
                // Filter out grays....                
                if (!isGray(rgbArr)) {                
                        Integer counter = (Integer) m.get(rgb);   
                        if (counter == null)
                            counter = 0;
                        counter++;                                
                        m.put(rgb, counter);                
                }                
            }
        }        
        String colourRGB = getMostCommonColour(m);
        if (!CliPixel.getSilentMode()) System.out.println("most common color is: " + colourRGB);
  }
  
//  public static String getMostCommonColour(Map map) {
//        List list = new LinkedList(map.entrySet());
//        Collections.sort(list, new Comparator() {
//              public int compare(Object o1, Object o2) {
//                return ((Comparable) ((Map.Entry) (o1)).getValue())
//                  .compareTo(((Map.Entry) (o2)).getValue());
//              }
//        });    
//        Map.Entry me = (Map.Entry )list.get(list.size()-1);
//        int[] rgb= getRGBArr((Integer)me.getKey());
//        return Integer.toHexString(rgb[0])+" "+Integer.toHexString(rgb[1])+" "+Integer.toHexString(rgb[2]);        
//    }    
  
    public static String getMostCommonColour(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                  .compareTo(((Map.Entry) (o2)).getValue());
              }
        });    
        Map.Entry me = (Map.Entry )list.get(list.size()-1);
        int[] rgb= getRGBArr((Integer)me.getKey());
        //return Integer.toHexString(rgb[0])+" "+Integer.toHexString(rgb[1])+" "+Integer.toHexString(rgb[2]);        
        return (rgb[0])+" "+ (rgb[1])+" "+ (rgb[2]);        
    }   

    public static int[] getRGBArr(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        LEDStripRed = red;
        LEDStripGreen = green;
        LEDStripBlue = blue;
        return new int[]{red,green,blue};

  }
    public static boolean isGray(int[] rgbArr) {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        if (rgDiff > tolerance || rgDiff < -tolerance) 
            if (rbDiff > tolerance || rbDiff < -tolerance) { 
                return false;
            }                 
        return true;
    }
  
}
   

 