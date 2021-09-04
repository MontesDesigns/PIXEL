
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import java.net.*;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.web.enabled.pixel.CliPixel;

public class TickerHttpHandler extends TextHttpHandler  //TO DO have TextHttpHandler send a return
{
    
    protected WebEnabledPixel app;
    
    public TickerHttpHandler(WebEnabledPixel application)
    {
        //super(application);
       
        String name = getClass().getName();
        
        this.app = application;
    }

    @Override
    protected String getHttpText(HttpExchange exchange)
    {
        
        if (WebEnabledPixel.isMister()) {
            return "MiSTer detected so skipping Ticker mode";
        }
        
        
        String returnCode = "";
        String returnMessage = "";
        Boolean returnBoolean = false;
        String RETURN_JSON = "{\n" +
"	\"succeeded\": \"${success}\",\n" +
"	\"message\" : ${message},\n" +
"}";
        
        Boolean runTickerCommand = true;
        LogMe logMe = LogMe.getInstance();
        URI requestURI = exchange.getRequestURI();
        
         if (!CliPixel.getSilentMode()) {
             logMe.aLogger.info("Ticker handler received a request: " + requestURI);
             System.out.println("Ticker handler received a request: " + requestURI);
         }
         
        String encodedQuery = requestURI.getQuery();
        
        if(encodedQuery == null)
        {
            
            logMe.aLogger.info("starting ticker by default no param");
            System.out.println("starting ticker by default no param");
            
        }
        else  {
            
            //we'll have something /ticker?start or /ticker?stop. If something else, we'll assume it's a start command but we will check if it was already running also
            List<NameValuePair> params = null;
            try {
                    params = URLEncodedUtils.parse(new URI(requestURI.toString()), "UTF-8");
            } catch (URISyntaxException ex) {
            }

            for (NameValuePair param : params) {

                switch (param.getName()) {

                    case "start": 
                        runTickerCommand = true;
                        break;
                    case "stop": 
                        runTickerCommand = false;
                        break;
                    default: 
                        runTickerCommand = true;
                     break;
                }
            }

            if (!runTickerCommand) {
                logMe.aLogger.info("Stopping Ticker");
                System.out.println("Stopping Ticker");
                WebEnabledPixel.setTickerRunning(false);
                returnMessage = "Stopping Ticker";
                returnBoolean = true;
                 
            }
            else {                                      //else let's start it
                logMe.aLogger.info("Starting Ticker");
                System.out.println("Starting Ticker");
                // BUT let's check and not start the ticker if it was already running
                //if (WebEnabledPixel.TickerEnabled() && WebEnabledPixel.getIsTickerRunning() == false) {  //was the ticker enabled and was it not running. if it was running, then let's skip this as it's already running
                if (WebEnabledPixel.getIsTickerRunning() == false) {  
                    WebEnabledPixel.setTickerRunning(true);
                    
                    Thread thread = new Thread("Ticker Thread") {  //had to put this in a thread as this goes into an indefinte ticker loop and the call was not finishing
                        public void run(){
                           WebEnabledPixel.startTicker(); 
                        }
                     };

                     thread.start();
                    returnMessage = "Starting Ticker...";
                    returnBoolean = true;
                }
                else {
                    System.out.println("Ticker was already running, doing nothing");
                    returnMessage = "Ticker was already running, doing nothing";
                    returnBoolean = true;
                }
            }
        }
   
    RETURN_JSON = RETURN_JSON.replace("${success}",returnBoolean.toString());
    RETURN_JSON = RETURN_JSON.replace("${message}",returnMessage); 
    returnCode =  RETURN_JSON;
    return returnCode;
    
    }
}