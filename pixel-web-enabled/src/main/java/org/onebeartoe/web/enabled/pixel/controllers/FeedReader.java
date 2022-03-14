package org.onebeartoe.web.enabled.pixel.controllers;

import java.net.URL;
import java.io.InputStreamReader;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
//import ioio.lib.api.RgbLedMatrix;
//import ioio.lib.api.RgbLedMatrix.Matrix;
//import ioio.lib.api.RgbLedMatrix;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.onebeartoe.web.enabled.pixel.CliPixel;
//import org.onebeartoe.pixel.PixelEnvironment;
//import org.onebeartoe.pixel.hardware.Pixel;
//import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
//import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
//import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextHttpHandlerBasic;




public class FeedReader {
    
  //  protected WebEnabledPixel app;

//    private static int LED_MATRIX_ID = 15;
//  
    //private static PixelEnvironment pixelEnvironment;
//    
//    private static Pixel pixel;
//  
   // public static RgbLedMatrix.Matrix MATRIX_TYPE;
//    
//    private static Long speed = null;
    
  //  private static Pixel pixel;
    
  //  private static Color  color = Color.RED;
    
//    private static int scrollsmooth_ = 0;
    
     public FeedReader()
    {
    //    super(application);
    
//        if(WebEnabledPixel.getLCDMarquee().equals("yes"))
//            lcdDisplay = new LCDPixelcade(); //bombing out on windows here
    
        //String name = getClass().getName();
        
       // this.app = application;
    }

    
    
    public void  getTitles(String rssURL) {
        
         //String name = getClass().getName();
        
        //Pixel pixel = this.application.getPixel();
         
//        pixelEnvironment = new PixelEnvironment(WebEnabledPixel.getMatrixID());
//        MATRIX_TYPE = pixelEnvironment.LED_MATRIX;
//        pixel = new Pixel(pixelEnvironment.LED_MATRIX, pixelEnvironment.currentResolution);
//        
//        if (scrollsmooth_ == 0) {
//            String scrollSpeedSettings = WebEnabledPixel.getTextScrollSpeed();
//            scrollsmooth_ = WebEnabledPixel.getScrollingSmoothSpeed(scrollSpeedSettings);
//        } 
        
       // ScrollingTextHttpHandlerBasic scroller = new ScrollingTextHttpHandlerBasic(WebEnabledPixel application);
         
        boolean ok = false;
        //if (args.length==1) {
            try {
                URL feedUrl = new URL(rssURL);

                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(feedUrl));
                
          
                List res = feed.getEntries();
                Integer len = res.size();
                
               // speed = Long.valueOf(WebEnabledPixel.getScrollingTextSpeed(LED_MATRIX_ID));
    
//                if (speed_ != null) {
//                  speed = Long.valueOf(speed_);
//                  if (speed.longValue() == 0L)
//                    speed = Long.valueOf(10L); 
//                } 

                //Pixel pixel = new Pixel(Matrix.ADAFRUIT_128x32,11);    
                //Pixel pixel = this.application.getPixel();
              
                
                int i = 0;
                for(Object o : res) {
                    i++;
                    //System.out.println(((SyndEntryImpl) o).getDescription().getValue());
                    //pixel.scrollText("hello " + i, 1, speed, color, WebEnabledPixel.pixelConnected, scrollsmooth_);
                   // pixel.scrollText(((SyndEntryImpl) o).getTitle(), 1, speed, color, WebEnabledPixel.pixelConnected, scrollsmooth_);
                   
                  
                   
                    // app.getPixel().scrollText("hi there yo you yo", 1, 10, Color.red, true, 1);
                //   ScrollingTextHttpHandlerBasic.scroll("hello",1,Color.red);

                   if (!CliPixel.getSilentMode()) System.out.println(((SyndEntryImpl) o).getTitle());
                    //SyndEntryImpl) o).getTitle()
                }
                
                 //pixel.scrollText(text_, loop_, speed.longValue(), color, WebEnabledPixel.pixelConnected, scrollsmooth_);
    

                //System.out.println(feed);
                
                //Integer len = feed.length();
                
                //System.out.println(feed.getTitle());
                //System.out.println(feed.getEntries());
                //System.out.println(feed.getFeedType());

                ok = true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                if (!CliPixel.getSilentMode()) System.out.println("ERROR: "+ex.getMessage());
            }
        //}

        if (!ok) {
            if (!CliPixel.getSilentMode())  {
                System.out.println();
                System.out.println("FeedReader reads and prints any RSS/Atom feed type.");
                System.out.println("The first parameter must be the URL of the feed to read.");
                System.out.println();
            }
        }
        
        
    }
    
    public static List<String> getTitlesArray(String rssURL) {
        
        //String[]headLinesArray = null;
        ArrayList<String> headLinesArray = new ArrayList<String>();
 
        
        boolean ok = false;
        //if (args.length==1) {
            try {
                URL feedUrl = new URL(rssURL);

                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(feedUrl));
                
          
                List res = feed.getEntries();
                Integer len = res.size();
                
                int i = 0;
                for(Object o : res) {
                    i++;
                    //System.out.println(((SyndEntryImpl) o).getTitle() + "count: " + i);
                    headLinesArray.add(((SyndEntryImpl) o).getTitle());
                    //System.out.println(((SyndEntryImpl) o).getTitle());
                }

                ok = true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                if (!CliPixel.getSilentMode()) System.out.println("ERROR: "+ex.getMessage());
            }
        //}

        if (!ok) {
            if (!CliPixel.getSilentMode())  {
                System.out.println();
                System.out.println("[ERROR] Could not read RSS feed");
                System.out.println();
            }
        }
        
        return headLinesArray;
        
    }


     
}
