package org.onebeartoe.lcdfinder.cade.pixel;

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

public class lcdfinder implements ServiceListener {
    
    @Override
    public void serviceAdded(ServiceEvent event) {
      //System.out.println("Service added: " + event.getInfo());
      //System.out.println("Service Port: " + event.getInfo().getPort());
      embeddedLoc = event.getInfo().getServer().replace("._pixelcade._tcp","");
      embeddedLoc = embeddedLoc.substring(0, embeddedLoc.length() - 1);
      embeddedLoc = embeddedLoc.replace("SuperPixelcade-","");
      System.out.println("Pixelcade LCD Detected: [" + embeddedLoc +"]");
      //String test = OPiCheck(embeddedLoc,"name",":8080/v2/info");
      //System.out.println(test);
      Pixelcades.add(embeddedLoc);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
      System.out.println("Service removed: " + event.getInfo());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
      //System.out.println("Service resolved: " + event.getDNS().getHostName());
      //event.getDNS().getHostName();
    }
    
    public static List<String> Pixelcades = new ArrayList<String>(); 
    
    public static List<String> UnpairedPixelcades = new ArrayList<String>(); 
    
    public static List<String> PairedPixelcades = new ArrayList<String>(); 
    
    public String embeddedLoc = "pixelcadedx.local"; 
  
    public static String pixelcadeLCDFinderVersion = "3.5.5";

    private CliPixel cli;
    
    private String pairingAPIResult_ = null;
    
    private String V2CheckResult_ = null;
   
    private String LCDMarqueeHostName_ = "";
    
    private JSONParser parse = new JSONParser();
    
    private JSONObject pairingResult = new JSONObject();
    
    private boolean PixelcadeV1Detected = false;
    
    public static String OS = System.getProperty("os.name").toLowerCase();
   
    public lcdfinder(String[] args)
    {
        //cli = new CliPixel(args);
        //cli.parse();
        
        System.out.println("Searching your network for Pixelcades for 10 seconds...");
        
      //***************************************************
      // Create a JmDNS instance, we'll use this to auto-detect Pixelcade LCD on the network using Bonjour mDNS
      Thread thread = new Thread(() -> {
      //Thread thread = new Thread(() {
        JmDNS jmdns = null;
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
                
                System.out.println("Total number of Pixelcades Discovered: " +  Pixelcades.size());
                    
                for (int i = 0; i < Pixelcades.size(); i++) {

                    //V2CheckResult_ = OPiCheck(Pixelcades.get(i),"name",":8080/v2/info"); //this call doesn't always work for some reason so need to do hack
                    V2CheckResult_ = OPiCheckHack(Pixelcades.get(i)); //this call doesn't always work for some reason so need to do hack
                    System.out.println("Pixelcade V2 Check: " + "[" + V2CheckResult_ + "]");  
     
                    if (!V2CheckResult_.equals("nodata")) { //then we got something back from the v2/info call so we know it's an OPi

                        pairingAPIResult_ = PairingAPICall(Pixelcades.get(i),"message",":8080/v2/utility/pairing/");
                        System.out.println(Pixelcades.get(i) + " Pairing Status: " + "[" + pairingAPIResult_ +"]"); 

                         if (pairingAPIResult_.equals("unpaired")) { //let's first see if we have more than 1 unpaired
                             UnpairedPixelcades.add(Pixelcades.get(i));
                         }  

                         if (pairingAPIResult_.equals("paired")) { //let's first see if we have more than 1 unpaired
                             PairedPixelcades.add(Pixelcades.get(i));
                         }
                    }  
                    else {
                        System.out.println(Pixelcades.get(i) + " is a V1 Pixelcade LCD, skipping...");   
                        PixelcadeV1Detected = true;
                    }
                }  //end for loop so not let's check if there is more than one unpaired
                    
                //UnpairedPixelcades.add("pixelcadedx.local"); //TO DO undo this one just for testing
                //ok now we're done looping through them, let's see if we have more than 1 unpaired
                    
                if (UnpairedPixelcades.size() == 1) { //ok we just have one unpaired so let's pair it, easy
                     System.out.println("One unpaired Pixelcade LCD: [" + UnpairedPixelcades.get(0) + "] detected, now pairing...");  
                     pairingAPIResult_ = PairingAPICall(UnpairedPixelcades.get(0),"message",":8080/v2/utility/pairing/set/on");
                     System.out.println("[PAIRED] " + UnpairedPixelcades.get(0) + " paired with result: " + pairingAPIResult_); 
                     //sendURL(UnpairedPixelcades.get(0),":8080/text?t=PAIRED&color=green");
                     sendURL(UnpairedPixelcades.get(0),":8080/arcade/stream/mame/paired.jpg");
                     writeSettingsINI(UnpairedPixelcades.get(0));
                     //System.exit(0);
                     PauseAndExit();
                } 

                else if (UnpairedPixelcades.size() > 1) { //then we have more than 1 unpaired so the user will need to pick one
                    System.out.println("Multiple Pixelcade LCDs have been detected, please now select the one you want to pair with");
                    System.out.println("Type: 'y' or 'yes' to pair; 'n' or 'no' to not pair; or 'q' to quit:");
                    Scanner scanner = new Scanner(System.in);    
                    String token = "";

                    for (int i = 0; i < UnpairedPixelcades.size(); i++) {

                        System.out.println("[QUESTION] Do you want to pair with: " + UnpairedPixelcades.get(i) + "?");

                        //sendURL(UnpairedPixelcades.get(i),":8080/text?t=Type%20y%20to%20pair%20to%20this%20Pixelcade%20LCD%20or%20Type%20n%20to%20select%20another%20one");
                        sendURL(UnpairedPixelcades.get(i),":8080/arcade/stream/mame/paired-prompt.jpg");

                        while(scanner.hasNextLine())
                        {
                           token = scanner.nextLine().trim();

                           if(token.equalsIgnoreCase("q")) {
                               System.out.println("You have not paired with a Pixelcade LCD so you can run this program again later");
                               //System.exit(0);
                               PauseAndExit();
                           }

                           if(token.equalsIgnoreCase("y")||token.equalsIgnoreCase("yes")) 
                           {
                               System.out.println("Now pairing with: " + UnpairedPixelcades.get(i));
                               //sendURL(UnpairedPixelcades.get(i),":8080/text?t=PAIRED&color=green");
                               sendURL(UnpairedPixelcades.get(i),":8080/arcade/stream/mame/paired.jpg");
                               pairingAPIResult_ = PairingAPICall(UnpairedPixelcades.get(i),"message",":8080/v2/utility/pairing/set/on");
                               System.out.println("[PAIRED] " + UnpairedPixelcades.get(i) + " paired with result: " + pairingAPIResult_); 
                               writeSettingsINI(UnpairedPixelcades.get(i));
                               //System.exit(0);
                               PauseAndExit();
                           }
                           else if (token.equalsIgnoreCase("n")||token.equalsIgnoreCase("no"))
                           {
                               //ok we're not pairing to this one so let's put a generic marquee on and break the while and move on to the next one
                               sendURL(UnpairedPixelcades.get(i),":8080/arcade/stream/mame/pixelcade.jpg");
                               break;
                           }
                           else
                           {
                               System.out.println("Oops, not a valid input");
                               System.out.println("Type: 'y' or 'yes' to pair; 'n' or 'no' to not pair; or 'q' to quit:");
                           }
                        }
                    }
                    System.out.println("You did not pair to a Pixelcade LCD");
                    System.out.println("This computer is NOT paired to a Pixelcade LCD but you may run this program again later to pair to a Pixelcade LCD");
                    System.out.println("Exiting...");
                    //System.exit(0);
                    PauseAndExit();
                }
                else if (UnpairedPixelcades.size() == 0 && PairedPixelcades.size() > 0 ) { //there are no unpaired BUT there are paired, so let's prompt the user if the want to unpair
                    System.out.println("No Unpaired Pixelcade LCDs were detected, you may however unpair an existing Pixelcade LCD from another host and pair instead with this computer");
                    System.out.println("Type: 'y' or 'yes' to unpair and pair; 'n' or 'no' to skip; or 'q' to quit:");
                    Scanner scanner = new Scanner(System.in);    
                    String token = "";

                    for (int i = 0; i < PairedPixelcades.size(); i++) {

                        System.out.println("[QUESTION] Do you want to unpair and pair with: " + PairedPixelcades.get(i) + "?");
                        //sendURL(PairedPixelcades.get(i),":8080/text?t=Type%20y%20to%20unpair%20to%20this%20Pixelcade%20LCD%20or%20Type%20n%20to%20skip");
                        sendURL(PairedPixelcades.get(i),":8080/arcade/stream/mame/paired-prompt.jpg");
                        //To Do , change this to the graphic "pair-prompt.jpg"
                      

                        while(scanner.hasNextLine())
                        {
                           token = scanner.nextLine().trim();

                           if(token.equalsIgnoreCase("q")) {
                               System.out.println("You did not unpair a Pixelcade LCD so you can run this program again later if you like");
                               //System.exit(0);
                               PauseAndExit(); 
                           }

                           if(token.equalsIgnoreCase("y")||token.equalsIgnoreCase("yes")) 
                           {

                               //let's unpair
                               pairingAPIResult_ = PairingAPICall(PairedPixelcades.get(i),"message",":8080/v2/utility/pairing/set/off");
                               System.out.println("[UNPAIRED] " + PairedPixelcades.get(i) + " unpaired with result: " + pairingAPIResult_); 
                               //and then pair
                               pairingAPIResult_ = PairingAPICall(PairedPixelcades.get(i),"message",":8080/v2/utility/pairing/set/on");
                               System.out.println("[PAIRED] " + PairedPixelcades.get(i) + " paired with result: " + pairingAPIResult_); 

                               //now we've paired so let's write to settings.ini
                               writeSettingsINI(PairedPixelcades.get(i));
                               //sendURL(PairedPixelcades.get(i),":8080/text?t=PAIRED&color=green");
                               sendURL(PairedPixelcades.get(i),":8080/arcade/stream/mame/paired.jpg");
                               //TO DO change this to the graphic "paired.jpg"
                               //System.exit(0);
                               PauseAndExit();
                           }
                           else if (token.equalsIgnoreCase("n")||token.equalsIgnoreCase("no"))
                           {
                               //ok we're not pairing to this one so let's put a generic marquee on and break the while and move on to the next one
                               sendURL(PairedPixelcades.get(i),":8080/arcade/stream/mame/pixelcade.jpg");
                               break;
                           }
                           else
                           {
                               System.out.println("Oops, not a valid input");
                               System.out.println("Type: 'y' or 'yes' to unpair; 'n' or 'no' to not pair; or 'q' to quit:");
                           }
                        }
                    }
                    System.out.println("You did not unpair an existing Pixelcade LCD");
                    System.out.println("This computer is NOT paired to a Pixelcade LCD now but you may run this program again later");
//                  Scanner scan = new Scanner(System.in);
//                  System.out.print("Press [ENTER] to exit . . . ");
//                  scan.nextLine();
//                  System.exit(0);
                    PauseAndExit();
                }
                else if (PixelcadeV1Detected) { //then there was only a Pixelcade V1 on the network which will always be pixelcadedx.local
                    System.out.println("Pixelcade LCD V1 detected...");
                    writeSettingsINI("pixelcadedx.local");
                    PauseAndExit();
                    //System.exit(0);
                }
                else {
                    System.out.println("No unpaired Pixelcade LCDs OR Paired Pixelcade LCDs were detected");
                    System.out.println("Please ensure that this computer is on the same WiFi network as your Pixelcade LCD(s)");
                    System.out.println("Or that your Pixelcade LCD is connected via Ethernet to the same network as this computer");
                    PauseAndExit();
                    //System.out.println("Exiting...");
                    //System.exit(0);
                }
            }
        };

        Timer timer = new Timer("Timer");
        long delay = 10000L;
        timer.schedule(task, delay);
    }
    
    
    private String PairingAPICall(String Pixelcade, String key, String APIURL) {
        
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
    
    private String OPiCheck(String Pixelcade, String key, String APIURL) {
        
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
    
       private String OPiCheckHack(String Pixelcade) {
        
        String pairingResult_ = "nodata";
        
        if (Pixelcade.contains("-")) {  //if in format like pixelcadedx-es33200, then it's a V2
            pairingResult_ = "v2";
        }
         
        return pairingResult_;
        
    }
    
     
    
    private void sendURL(String PixelcadeHost, String urlString ) {
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
    
    private void writeOutput(String fileName, String output) {
         
        
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
    
    private void writeSettingsINI(String selectedPixelcadeHost) {
        
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
             System.out.println("Pixelcade LCD host name [" + selectedPixelcadeHost + "] written to settings.ini");
            } catch (IOException ex) {
                Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
            }
            } 
          else {                                      try {
              //the key wasn't there so let's add it
              sec.add("LCDMarqueeHostName", selectedPixelcadeHost);
              sec.add("LCDMarquee", "yes");
              ini.store();
              System.out.println("Pixelcade LCD host name [" + selectedPixelcadeHost + "] written to settings.ini");
            } catch (IOException ex) {
                Logger.getLogger(lcdfinder.class.getName()).log(Level.SEVERE, null, ex);
            }
         }  
        }
        else {
             System.out.println("[ERROR] Could not load settings.ini");
        }
    }
    
    private void PauseAndExit() {  //adding pause and exit so the user can read the pop up windows before it disappears
        Scanner scan = new Scanner(System.in);
        System.out.print("Press [ENTER] to exit . . . ");
        scan.nextLine();
        System.exit(0);
}

     
    public static boolean isWindows() {

            return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

            return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

            return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

    public static void main(String[] args)
    {

        lcdfinder app = new lcdfinder(args);
    }
}
