
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.ByteOrderMark.UTF_8;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;

/**
 Logic for LCD and LED Text Scrolling
LED Only - Scroll text on LED as normal
LCD Only - Scroll text on LCD as normal including sub-displays if there
LED + LCD - Scroll text on LED, do not scroll text on LCD
But note that alt text and text send to LCD scrolls on the smaller displays
Therefore we need to send a new flag that tells LCD that LED is there and don't scroll on LCD but still scroll on the sub displays
 **/

public class ScrollingTextHttpHandlerBasic extends TextHttpHandler  //TO DO have TextHttpHandler send a return
{
    protected LCDPixelcade lcdDisplay = null;
    protected WebEnabledPixel app;
   // public RgbLedMatrix matrix;

//TODO: rename this matrix_type    
   // public final RgbLedMatrix.Matrix KIND = null;
    
    public ScrollingTextHttpHandlerBasic(WebEnabledPixel application)
    //public ScrollingTextHttpHandlerBasic()
    {
        //super(application);
    
        if(WebEnabledPixel.getLCDMarquee().equals("yes"))
            lcdDisplay = new LCDPixelcade(); //bombing out on windows here
    
        String name = getClass().getName();
        
        this.app = application;
    }

    public ScrollingTextHttpHandlerBasic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //@Override
    public  void scroll(String message, Integer loop, Color color)
    {
        
//        if (WebEnabledPixel.isMister()) {
//            return "MiSTer detected so skipping scrolling text";
//        }
        
       
        String speed_ = null;
        Long speed = null;
       
        int scrollsmooth_ = 0;
        Long speeddelay_ = 10L;
        int fontSize_ = 0;
        int yOffset_ = 0;
        int lines_ = 1;
        String font_ = null;
        LogMe logMe = LogMe.getInstance();
        Font font = null;
        
         

//        if (WebEnabledPixel.getLCDMarquee().equals("yes")) {  //this is where we relay the call to LCD
//            try {
//               if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)){
//                   WebEnabledPixel.dxEnvironment = true;
//                   System.out.println("Requested: " + requestURI.getPath());
//
//                    URL url = null;
//                    if (WebEnabledPixel.getLCDLEDCompliment() == true && WebEnabledPixel.pixelConnected == true) { //then we need to add &led to the end of the URL params
//                       String textURL = requestURI.toString();
//                       url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + textURL + "&led"); //this flag tells LCD not to scroll as we already have LED scrolling
//                    }
//                    else {
//                       url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + requestURI);
//                    }
//
//                   HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                   con.setRequestMethod("GET");
//                   con.getResponseCode();
//                   con.disconnect();
//                   
//               }
//           }catch (  Exception e){}
//        }
         
        

            /* TO DO catch this wrong URL format as I made this mistake of ? instead of & after the first one!!!!
            Scrolling text handler received a request: /text/?t=hello%20world?c=red?s=10?l=2
            t : hello world?c=red?s=10?l=2
            */
           
         //   }
        
    if (color == null) 
        color = WebEnabledPixel.getRandomColor();
    
    if (loop == null)
      loop = 0; 
    
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
    
    Pixel.setFontFamily(font_);
    
    if (yOffset_ == 0)
      yOffset_ = WebEnabledPixel.getDefaultyTextOffset(); 
    
    Pixel.setYOffset(yOffset_);
    
    if (fontSize_ == 0)
      fontSize_ = WebEnabledPixel.getDefaultFontSize(); 
    
    Pixel.setFontSize(fontSize_);
    
    if (lines_ == 2) 
        Pixel.setDoubleLine(true);
    else if (lines_ == 4)
         Pixel.setFourLine(true);
    else {
        Pixel.setDoubleLine(false); //don't forget to set it back
        Pixel.setFourLine(false); //don't forget to set it back
    }
    
    Pixel pixel = this.app.getPixel();
    
    pixel.scrollText(message, loop, speed, color,WebEnabledPixel.pixelConnected,scrollsmooth_);
    
    //app.getPixel().scrollText(message, loop, speed, color,WebEnabledPixel.pixelConnected,scrollsmooth_);
        
    //return "scrolling text request received: " + message ;
    
    }
    
public static Map<String, String> getQueryMap(String query) {  
    String[] params = query.split("&");  
    Map<String, String> map = new HashMap<String, String>();

    for (String param : params) {  
        String name = param.split("=")[0];  
        String value = param.split("=")[1];  
        map.put(name, value);  
    }  
    return map;  
}

public Map<String, String> getUrlValues(String url) throws UnsupportedEncodingException {
    int i = url.indexOf("?");
    Map<String, String> paramsMap = new HashMap<>();
    if (i > -1) {
        String searchURL = url.substring(url.indexOf("?") + 1);
        String params[] = searchURL.split("&");

        for (String param : params) {
            String temp[] = param.split("=");
            paramsMap.put(temp[0], java.net.URLDecoder.decode(temp[1], "UTF-8"));
        }
    }

    return paramsMap;
}

    @Override
    protected String getHttpText(HttpExchange he) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


 
    
}