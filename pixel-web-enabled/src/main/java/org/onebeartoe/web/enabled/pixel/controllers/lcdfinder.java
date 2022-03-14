/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onebeartoe.web.enabled.pixel.controllers;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Ini;
import org.ini4j.Config;
import org.ini4j.Profile;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.util.Scanner;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.logMe;



public class lcdfinder implements ServiceListener {
    
    @Override
    public void serviceAdded(ServiceEvent event) {
      embeddedLoc = event.getInfo().getServer().replace("._pixelcade._tcp","");
      embeddedLoc = embeddedLoc.substring(0, embeddedLoc.length() - 1);
      embeddedLoc = embeddedLoc.replace("SuperPixelcade-","");
      if (!CliPixel.getSilentMode()) System.out.println("Pixelcade LCD Detected: [" + embeddedLoc +"]");
      Pixelcades.add(embeddedLoc);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
      if (!CliPixel.getSilentMode()) System.out.println("Service removed: " + event.getInfo());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
      //System.out.println("Service resolved: " + event.getDNS().getHostName());
      //event.getDNS().getHostName();
    }
    
    public static List<String> Pixelcades = new ArrayList<String>(); 
    
    public static List<String> UnpairedPixelcades = new ArrayList<String>(); 
    
    public static List<String> PairedPixelcades = new ArrayList<String>(); 
    
    public static String embeddedLoc = "pixelcadedx.local"; 
    
    private static String pairingAPIResult_ = null;
    
    private static String V2CheckResult_ = null;
   
    private static String LCDMarqueeHostName_ = "";
    
    private static JSONParser parse = new JSONParser();
    
    private static JSONObject pairingResult = new JSONObject();
    
    private static boolean PixelcadeV1Detected = false;
    
    public static String OS = System.getProperty("os.name").toLowerCase();
    
    private static Integer hyphenCount = 0;
    
    private static Integer returnCode = 0;
    
    private JmDNS jmdns = null;
    
    private static String LCDTargetid = "";
    
    private InetAddress host = null;
    
    public static LogMe logMe = null;
    
    public static boolean LCDFound = false;
   
    public boolean getLCD(String LCDNamefromSettings)
    {  
     
       logMe = LogMe.getInstance();
        
        
       if (!CliPixel.getSilentMode()) System.out.println("[Pixelcade LCD NOT FOUND] " + LCDNamefromSettings);
       LogMe.aLogger.info("[Pixelcade LCD NOT FOUND] " + LCDNamefromSettings);
       LCDTargetid = getLCDid(LCDNamefromSettings);
       //now does this target id still have a hyphen , ie, it is a network duplicate, then we only want what is left of that hyphen
       if (LCDTargetid.contains("-")) {
           LCDTargetid = getLCDidLeftHyphen(LCDTargetid);
       }
       
       if (!CliPixel.getSilentMode()) System.out.println("Searching your network for a Pixelcade LCD containing: [" + LCDTargetid + "]");
       LogMe.aLogger.info("Searching your network for a Pixelcade LCD containing: [" + LCDTargetid + "]");
        
      //***************************************************
      // Create a JmDNS instance, we'll use this to auto-detect Pixelcade LCD on the network using Bonjour mDNS
      Thread thread;
        thread = new Thread(() -> {
            try {
                jmdns = JmDNS.create(InetAddress.getLocalHost());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Add a service listener
            jmdns.addServiceListener("_pixelcade._tcp.local.", this);
            // Wait a bit
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            Thread.currentThread().interrupt();
        });
      thread.start();
      //****************************************
      
       TimerTask task = new TimerTask() {  //one time timer that run afer 10 seconds of searching for Pixelcades
            public void run() {
                
                if (!CliPixel.getSilentMode()) System.out.println("Number of Pixelcade LCD(s) Discovered: [" +  Pixelcades.size() + "]");
                LogMe.aLogger.info("Number of Pixelcade LCD(s) Discovered: [" +  Pixelcades.size() + "]");
                    
                for (int i = 0; i < Pixelcades.size(); i++) {
                    
                    V2CheckResult_ = OPiCheckHack(Pixelcades.get(i)); //this call doesn't always work for some reason so need to do hack
                    
                    if (!V2CheckResult_.equals("nodata")) {
                        
                        if (Pixelcades.get(i).contains(LCDTargetid)) { //do any of the discovered Pixelcades match the target id. If yes, then let's use that one and tell the user to run pairing again
                            
                            LCDFound = true;
                            
                            WebEnabledPixel.setLCDMarqueeHostName(Pixelcades.get(i));
                            
                            if (!CliPixel.getSilentMode()) System.out.println("[INFO] " +  "Your Pixelcade LCD Host Name changed, pairing to new host name: " + Pixelcades.get(i));
                            LogMe.aLogger.info("[INFO] " +  "Your Pixelcade LCD Host Name changed, pairing to new host name: " + Pixelcades.get(i));
                            
                            pairingAPIResult_ = PairingAPICall(Pixelcades.get(i),"message",":8080/v2/utility/pairing/set/on");
                            
                            if (!CliPixel.getSilentMode()) System.out.println("[PAIRED] " + Pixelcades.get(i)); 
                            LogMe.aLogger.info("[PAIRED] " + Pixelcades.get(i)); 
                            
                            try {
                            host = InetAddress.getByName(Pixelcades.get(i));
                            if (!CliPixel.getSilentMode()) System.out.println(host.getHostAddress());
                            LogMe.aLogger.info(host.getHostAddress());
                            } catch (UnknownHostException ex) {
                                ex.printStackTrace();
                            }
                            
                            writeSettingsINI(Pixelcades.get(i),host.getHostAddress()); //let's update settings.ini so the user saves time on the next run
                        }
                        else {
                            if (!CliPixel.getSilentMode()) System.out.println("[INFO] " +  "A different Pixelcade LCD was discovered on your network: " + Pixelcades.get(i));
                            LogMe.aLogger.info("[INFO] " +  "A different Pixelcade LCD was discovered on your network: " + Pixelcades.get(i));
                            if (!CliPixel.getSilentMode()) System.out.println("[ACTION] " +  "Please run the Pixelcade LCD Pairing Utility to switch to this LCD");
                            LogMe.aLogger.info("[ACTION] " +  "Please run the Pixelcade LCD Pairing Utility to switch to this LCD");
                        }
                    }
     
                }
            }
        };

        Timer timer = new Timer("Timer");
        long delay = 10000L;
        timer.schedule(task, delay);
        
        return LCDFound; 
    }
    
    private static String getLCDid(String LCDWholeName) {
       String[] LCDArray1 = LCDWholeName.split("\\-");
       String LCDString1 = LCDArray1[1];
       String[] LCDArray2 = LCDString1.split("\\.");
       String LCDid = LCDArray2[0];
       return LCDid;
    }
    
     private static String getLCDidLeftHyphen(String LCDWholeName) {
       String[] LCDArray1 = LCDWholeName.split("\\-");
       String LCDid = LCDArray1[0];
       return LCDid;
    }
    
    
    private static String PairingAPICall(String Pixelcade, String key, String APIURL) {
        
          String pairingResult_ = "nodata";
          URL url = null;
          BufferedReader reader = null;
          StringBuilder stringBuilder;
        
          try {  
              
                url = new URL("http://" + Pixelcade + APIURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                
                BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                   response.append(inputLine);
                }
                in.close();
                int responsecode = conn.getResponseCode();
                 if (responsecode != 200 && responsecode != 400 ) {
                    throw new RuntimeException("HttpResponseCode: " + responsecode);
                } else {
                    //Using the JSON simple library parse the string into a json object
                    parse = new JSONParser();
                    pairingResult = (JSONObject) parse.parse(response.toString());
                    
                    Boolean APICallSuccess = (Boolean) pairingResult.get("success"); 
                    //System.out.println("boolean: " + APICallSuccess);
                    
                    if (APICallSuccess)
                          pairingResult_ = (String) pairingResult.get(key); 
                    else
                          pairingResult_ = "error";
                }

            } catch (Exception e) {
                //e.printStackTrace();  //removed as it prints an error message not needed for Pi LCDs
            }
        return pairingResult_;
    }
    
    private static String OPiCheck(String Pixelcade, String key, String APIURL) {
        
        //may need to switch to this https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
        
          String pairingResult_ = "nodata";
          URL url = null;
          BufferedReader reader = null;
          StringBuilder stringBuilder;
        
          try {  
                url = new URL("http://" + Pixelcade + APIURL);  
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "text/plain"); 
                conn.setRequestProperty("charset", "utf-8");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();
                
		//System.out.println("Response Code : " + conn.getResponseCode());
		//System.out.println("Response Message : " + conn.getResponseMessage());
                
                BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                   response.append(inputLine);
                }
                in.close();
                //System.out.println("Response Code : " + Pixelcade + ": " + conn.getResponseCode());
                //System.out.println("Response for : " + Pixelcade + ": " + response);
                int responsecode = conn.getResponseCode();
                if (responsecode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responsecode);
                } else {
                    //Using the JSON simple library parse the string into a json object
                    parse = new JSONParser();
                    pairingResult = (JSONObject) parse.parse(response.toString()); 
                    pairingResult_ = (String) pairingResult.get(key); 
                }
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        return pairingResult_;
        
    }
    
       private static String OPiCheckHack(String Pixelcade) {
        
        String pairingResult_ = "nodata";
        
        if (Pixelcade.contains("-")) {  //if in format like pixelcadedx-es33200, then it's a V2
            pairingResult_ = "v2";
        }
         
        return pairingResult_;
        
    }
    
     
    
    private static void sendURL(String PixelcadeHost, String urlString ) {
        try {  
            URL url = new URL("http://" + PixelcadeHost + urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            
            BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }
            in.close();
            int responsecode = conn.getResponseCode();
            
            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
                //System.out.println("Sent: " + "http://" + PixelcadeHost + urlString);
            }
            
         } catch (Exception e) {
            e.printStackTrace();  
         }   
    }
    
    private static void writeOutput(String fileName, String output) {
         
        
        File file = new File(fileName);
        
        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }
        
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] strToBytes = output.getBytes();
        try {
            outputStream.write(strToBytes);
        } catch (IOException ex) {
            Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void writeSettingsINI(String selectedPixelcadeHost, String selectedPixelcadeIP) {
        
        File file = new File("settings.ini"); //let's open settings.ini which is in the same Pixelcade directory as this code is lauching from or to do, define pixelcade home
        
        if (file.exists() && !file.isDirectory()) {
        Ini ini = null;
        try {
          ini = new Ini(new File("settings.ini"));
          Config config = ini.getConfig();
          config.setStrictOperator(true);
          ini.setConfig(config);
        }
         catch (IOException ex) {
            
        }
          
         Profile.Section sec = (Profile.Section)ini.get("PIXELCADE SETTINGS");

         if (sec.containsKey("LCDMarqueeHostName")) { try {
             //if this key is already there
             sec.put("LCDMarqueeHostName", selectedPixelcadeHost);
             sec.put("LCDMarquee", "yes");
             ini.store();
             if (!CliPixel.getSilentMode()) System.out.println("Pixelcade LCD host name [" + selectedPixelcadeHost + "] written to settings.ini");
             LogMe.aLogger.info("Pixelcade LCD host name [" + selectedPixelcadeHost + "] written to settings.ini");
            } catch (IOException ex) {
                Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            } 
          else {                                      try {
              //the key wasn't there so let's add it
              sec.add("LCDMarqueeHostName", selectedPixelcadeHost);
              sec.add("LCDMarquee", "yes");
              ini.store();
              if (!CliPixel.getSilentMode()) System.out.println("Pixelcade LCD host name [" + selectedPixelcadeHost + "] written to settings.ini");
              LogMe.aLogger.info("Pixelcade LCD host name [" + selectedPixelcadeHost + "] written to settings.ini");
            } catch (IOException ex) {
                Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
            }
         } 
         
         if (sec.containsKey("LCDMarqueeIPAddress")) { try {
             //if this key is already there
             sec.put("LCDMarqueeIPAddress", selectedPixelcadeIP);
             ini.store();
             if (!CliPixel.getSilentMode()) System.out.println("Pixelcade LCD IP Address [" + selectedPixelcadeIP + "] written to settings.ini");
             LogMe.aLogger.info("Pixelcade LCD IP Address [" + selectedPixelcadeIP + "] written to settings.ini");
            } catch (IOException ex) {
                Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            } 
          else {                                      try {
              //the key wasn't there so let's add it
              sec.add("LCDMarqueeIPAddress", selectedPixelcadeIP);
              ini.store();
              if (!CliPixel.getSilentMode()) System.out.println("Pixelcade LCD IP Address [" + selectedPixelcadeIP + "] written to settings.ini");
              LogMe.aLogger.info("Pixelcade LCD IP Address [" + selectedPixelcadeIP + "] written to settings.ini");
            } catch (IOException ex) {
                Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
         
        }
        else {
             if (!CliPixel.getSilentMode()) System.out.println("[ERROR] Could not load settings.ini");
             LogMe.aLogger.info("[ERROR] Could not load settings.ini");
        }
    }
    
    private static void PauseAndExit() {  //adding pause and exit so the user can read the pop up windows before it disappears
        Scanner scan = new Scanner(System.in);
        System.out.print("Press [ENTER] to exit . . . ");
        scan.nextLine();
        System.exit(0);
    }
}