package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import ioio.lib.api.exception.ConnectionLostException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
//import org.apache.commons.lang3.StringUtils;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;

/**
 * @author Roberto Marquez
 */
public abstract class ImageResourceHttpHandler extends TextHttpHandler
{
    protected String basePath;
    protected String defaultImageClassPath;
    protected String modeName;
    protected WebEnabledPixel application;
    protected Logger logger;
    LogMe logMe = null;
        
    public ImageResourceHttpHandler(WebEnabledPixel application)
    {
        String name = getClass().getName();
        logger = Logger.getLogger(name);
        
        this.application = application;
        
        //logMe = LogMe.getInstance();
        LogMe logMe = LogMe.getInstance();
    }
    
    @Override
    protected String getHttpText(HttpExchange exchange)
    {        
        String imageClassPath;
        String redirect = "";
        String arcadeNameExtension = "";
        String consoleNameExtension = "";
        
         //thought i might need to do this but turns out not
        //String encoding = "UTF-8";
        //exchange.getResponseHeaders().set("Content-Type", "text/html"); 
        //exchange.getRequestURI().set("Content-Type", "text/html; charset=" + encoding);
        //url is http://localhost:8080/arcade/stream/nes/marios brows.&l=0
        
        //String encoding = "UTF-8";
        //exchange.getResponseHeaders().set("Content-Type", "text/html; charset=" + encoding);
        
        //String encodedValue = "Hell%C3%B6%20W%C3%B6rld%40Java";

        // Decoding the URL encoded string
      
        
        //logMe.aLogger.info("RAW PATHA: " + exchange.getResponseBody()); 
        //System.out.println("RAW PATHA: " + exchange.getResponseBody());
        //logMe.aLogger.info("RAW PATHB: " + exchange.getRequestURI()); 
        //System.out.println("RAW PATHB: " + exchange.getRequestURI());
        //System.out.println("RAW PATHC: " + exchange.getLocalAddress());

        try {
            URI requestURI = exchange.getRequestURI();
            String path = requestURI.getPath();
            String revisedURL = "";
            //System.out.println("[Original URL] " + path);
                     
            int i = path.lastIndexOf("/") + 1;
            String name = path.substring(i);
            
            
            if (WebEnabledPixel.getLCDMarquee().equals("yes") && (path.contains("/arcade/") || path.contains("/console/") )) {  //relaying the api call to pixelcadedx.local but don't relay quit and shutdown commands
                try {
                    if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)) { //to do should we be checking everytime if reachable?
                        WebEnabledPixel.dxEnvironment = true;
                        //if it's a console call, let's re-direct to arcade because pixelcade embedded doesn't know about the console calls
                        if (requestURI.getPath().contains("console")) {
                           if (!CliPixel.getSilentMode()) System.out.println("Request Console Redirected: " + requestURI.getPath());
                           
                           String consoleName = "";
                           String urlPath = requestURI.getPath().toString();
                           String lastchar = (requestURI.getPath().substring(requestURI.getPath().length() - 1));
                            // on linux, it's /console/stream/atari2600/
                            //on pc, it's /console/stream/Atari 2600  
                           if (lastchar.charAt(0) == '/') { 
                               urlPath = removeLastChar(urlPath);
                           }
                           consoleName = (urlPath.substring(urlPath.lastIndexOf("/") + 1)).toLowerCase(); //get the last part of the string
                           consoleName = consoleName.replace(" ", "_"); //had to add this one as Dennis made change to send native console names with no more _
                           
                           consoleNameExtension = FilenameUtils.getExtension(consoleName);
                             
                            if (!consoleName.equals("black")) { 
                                //if (arcadeNameExtension.isEmpty()) {  //if its empty, then we need to add the extension
                                if (consoleNameExtension.isEmpty()) {  //if its empty, then we need to add the extension    
                                       redirect = "/arcade/stream/default/" + consoleName + ".jpg"; //made this change so users can maintain console artwork in one place
                                       if (!CliPixel.getSilentMode()) {
                                            System.out.println("yoyo");
                                            System.out.println(redirect);
                                       }
                                        //redirect = "/arcade/stream/mame/" + consoleName + ".jpg";
                                       //redirect = "/console/stream/" + consoleName + ".jpg";  //this may break on Pi LCD as it doesn't accept console
                                }
                                else {  //there is already an extension so no need to add an additional one
                                       redirect = "/arcade/stream/default/" + consoleName;
                                       //redirect = "/arcade/stream/mame/" + consoleName;
                                       //redirect = "/console/stream/" + consoleName + ".jpg";
                                }
                                //String redirect = "/console/stream/" + consoleName + ".jpg";
                                URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + redirect);
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                con.getResponseCode();
                                con.disconnect();
                             }
                        }
                        else { // it's not a console call
                            //System.out.println("Requested: " + requestURI.getPath());  
                            
                            String string = requestURI.getPath();
                            String[] bits = string.split("/");
                            String gameName = bits[bits.length - 1];
                            
                            String console = bits[bits.length - 2];
                            console = console.replace(" ", "_").toUpperCase(); //had to add this one as Dennis made change to send native console names with no more _, to do fix this later, _ and Uppercase is what was originally there and works so didn't want to mess with it
                            
                            //String gameName = (requestURI.getPath().substring(requestURI.getPath().lastIndexOf("/") + 1));
                          
                            //System.out.println("Game name original: " + gameName);
                            
//                            if(gameName.contains("_")) {  // this hack no longer needed with the LEDBlinky fix to maintain the native file names
//                            //if(gameName.contains("_") && WebEnabledPixel.isWindows()) { //only do this check if there is an _ and we are on Windows
//                                
//                                //we want to capital the first char after the delimineters of: space, left paranth, and comma
//                                //for example on Windows with LEDBlinky, we will get this 007_-_NIGHTFIRE_(USA,_EUROPE)_(EN,FR,DE) which we will convert to 007 - Nightfire (Usa, Europe) (En,Fr,De)
//                                
//                                gameName = WordUtils.capitalizeFully(gameName.replace("_"," "),new char[]{' ', '(', ','});
//                                gameName = gameName.replace("Usa","USA").replace("(Xbox)","(XBOX)".replace("Nfl ","NFL ").replace("Lego ","LEGO ").replace("Nba ","NBA ").replace("Nhl ","NHL ").replace("Mlb ","MLB ").replace("Bmx ","BMX ").replace("Gt ","GT ").replace("Ncaa ","NCAA ").replace("Ii ","II ").replace("Iii ","III ").replace("Nascar ","NASCAR ").replace("Espn ","ESPN ").replace("Sd ","SD "));
//                                //System.out.println("Game name2 : " + gameName);
//                                
//                                //String console = bits[bits.length - 2];
//                                //console = console.replace(" ", "_"); //had to add this one as Dennis made change to send native console names with no more _
//                                
//                                String redirect = "/arcade/stream/" + console + "/" + gameName + ".jpg";
//                                System.out.println("_ Detected and Redirected:" + redirect);
//                                redirect = redirect.replace(" ","%20"); //if we don't add this, the URL call wont' make it and will be truncated at the spaces
//                               
//                                URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + redirect);
//                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                                con.setRequestMethod("GET");
//                                con.getResponseCode();
//                                con.disconnect();
//                            }
//                            
//                            else { 
                                
                                //let's see if the front end also included the extension
                                if (!gameName.equals("dummy")) {                                          // let's no longer send the black frame calls to LCD
                                    arcadeNameExtension = FilenameUtils.getExtension(gameName);
                                    if (arcadeNameExtension.isEmpty()) {  //if its empty, then we need to add the extension
                                        revisedURL = "/arcade/stream/" + console + "/" + gameName + ".jpg"; //console has the _ and game name will be original with spaces
                                    }
                                    else {  //there is already an extension so no need to add an additional one
                                        revisedURL = "/arcade/stream/" + console + "/" + gameName;
                                    }

                                    revisedURL = revisedURL.replace(" ","%20"); //if we don't add this, the URL call wont' make it and will be truncated at the spaces

                                    URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + revisedURL);
                                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                    con.setRequestMethod("GET");
                                    con.getResponseCode();
                                    con.disconnect();
                                }
                           // }
                        }
                    } else { //let's do a second time LCD search if it's not reachable
                        if (WebEnabledPixel.getLCDSearchCounter() < 2) {  //it ran one time during startup so let's try one more time. But not after that as perf will be bad
                            WebEnabledPixel.LCDSearch();
                            WebEnabledPixel.setLCDSearchCounter(); //this will make the counter 2 so we won't run this again
                        }
                    }
                }catch (  Exception e){}
            }

            if(name.equals(modeName))
            {
                // this is just a request change to still image mode
                imageClassPath = defaultImageClassPath;
            }
             else if( path.contains("/animations/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/save/"))
            {
                imageClassPath = path;
            }
            else if( path.contains("/console/"))
            { 
                imageClassPath = requestURI.toString(); //this returns /console/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/arcade/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/achievements/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/pinball/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
             else if( path.contains("/localplayback"))
            {
                imageClassPath = requestURI.toString(); 
            }
            else
            {
                imageClassPath = basePath + name;
                
                //to do need to add text here too
            }
        }
        catch(Exception e)
        {
            imageClassPath = defaultImageClassPath;
            
            String message = "An error occurred while determining the image from the request.  " +
                             "The default is used now.";
            
            logger.log(Level.SEVERE, message, e);
        }

        try
        {
            //System.out.println("loading " + modeName + " image");

            try
            {
                //System.out.println("writing image resource to the Pixel");
                writeImageResource(imageClassPath);
                
                //System.out.println(modeName + " image resource was written to the Pixel");
            } 
            catch (ConnectionLostException ex)
            {
                String message = "connection lost";
                logger.log(Level.SEVERE, message, ex);
            }
        }
        catch (IOException ex)
        {
            String message = "error with image resource";
            logger.log(Level.SEVERE, message, ex);
        }
        finally
        {
            if (!CliPixel.getSilentMode()) {
                return "REST call received for " + imageClassPath;
            }
            else {
                return "";
            }
        }
    }
    
 boolean isBlankString(String string) {
    return string == null || string.trim().isEmpty();
}
 
 public static String removeLastChar(String s) {
    return (s == null || s.length() == 0)
      ? null 
      : (s.substring(0, s.length() - 1));
}
    
    protected abstract void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException;
            
    }
