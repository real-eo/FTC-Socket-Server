import java.io.*;
import java.net.*;
import java.util.*;

import java.lang.Math;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

// * Requires 3 libs: bridj-0.7.0.jar, slf4j-api-1.7.2.jar, webcam-capture-0.3.12.jar
import com.github.sarxos.webcam.Webcam;

// Robot code should be placed in this class
public class robot {
    static String returnedRequest = "";
    // PC replacement for "runOpMode" method
    public static void main(String[] args) throws InterruptedException {
        initializeServer();  // Should be called before "waitForStart()"
        
        // Run loop
        for (int i = 0; i < 10; i++) {
            wait(100);
            // System.out.println("[#] Iteration: " + i);

            /* 
            if (returnedRequest != "") {
                System.out.println("[#] Returned value: " + returnedRequest);
                break;
            }
            */
        }
    }

    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void setReturnedRequestValue(String output) {
        returnedRequest = output;
    }

    public static void initializeServer() throws InterruptedException {
        ServerClass server = new ServerClass();
        Thread object = new Thread(server);
        object.start();
    }
}

class ServerClass implements Runnable {
    public void run() {
        try {
            runServer();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    static int port = 8800;
    static String returnedValue = "";
    static String[] requestData;

    public static void runServer() throws Exception {
        String fromClient;
        String toClient;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[$] Waiting for connection on port: \"" + port + "\"");

            parseRequest("void.client:0");

            boolean run = true;
            while(run) {
                Socket client = server.accept();
                System.out.println("[ ] Got connection on port \"" + port + "\"");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                fromClient = in.readLine();
                System.out.println("[-] Received: \"" + fromClient + "\"");

                if (fromClient.indexOf("stop:0") != -1) {
                    client.close();
                    run = false;

                    System.out.println("[$] Socket closed!");
                } else if (fromClient.indexOf("echo:") != -1) {
                    continue;
                } else {
                    parseRequest(fromClient);
                }
                
                if (requestData[0].indexOf("return") != -1) {
                    toClient = returnedValue;
                    returnedValue = "";
                    
                    if (toClient.length() > 1024) {
                        out.println(Math.min(8192, toClient.length()));
                    } else {
                        out.println(1024);
                    }

                    System.out.println("[$] Waiting for size confirmation packet...");

                    // confirmPacket = in.readLine();
                    in.readLine();

                    System.out.println("[$] Size confirmation packet received!");

                    System.out.println("[$] Attempting to send requested item...");

                    boolean dataRemaining = true;
                    String returnPacket;
                    String dataToSend = toClient;

                    while (dataRemaining) {
                        if (dataToSend.length() > 8192) {
                            toClient = dataToSend.substring(0, 8192);
                            dataToSend = dataToSend.substring(8192, dataToSend.length());
                        } else {
                            toClient = dataToSend;
                        }
                        out.println(toClient);
                        returnPacket = in.readLine();
                        if (returnPacket.indexOf("&") != -1) {
                            dataRemaining = false;
                        }
                    }

                    System.out.println("[$] Successfully sent requested item!");
                } else {
                    toClient = fromClient;

                    out.println(toClient);
                }
            }
        }
        System.exit(0);
    }

    public static void parseRequest(String request) throws InterruptedException {
        requestData = request.split("\\.");

        requestHandler RH = new requestHandler(requestData[1], requestData[0]);
        Thread object = new Thread(RH);
        object.start();

        if (requestData[0].indexOf("return") != -1) {
            object.join();
            returnedValue = RH.getValue();
            // System.out.println(returnedValue);
        }
    }
}

class requestHandler implements Runnable {
    String[] request;
    String returnValue = "";

    Map<String, Runnable> commands = new HashMap<>();

    public requestHandler(String argvRequest, String argvType) {
        request = argvRequest.split(":");

        try {
            commandMap();
        } catch (Exception exc) {
            System.out.println("[!] Exception caught in \"requestHandler()\" when attempting to run \"commandMap()\": " + exc);
        }
    }

    public void run() {
        try {
            // Displaying the thread that is running
            // System.out.println("[:] Thread \"" + Thread.currentThread().getId() + "\" is running");
            System.out.println("[:] Active Count: " + Thread.activeCount());
            System.out.println("[-] Request: \"" + request[0] + ":" + request[1] + "\"");

            commands.get(request[0]).run();
        } catch (Exception exc) {
            // Throwing an exception
            System.out.println("[!] Exception caught in \"run()\": " + exc);
        }
    }

    public String getValue() {
        return returnValue;
    }

    // Populate commandMap
    public void commandMap() throws Exception {
        commands.put("request", () -> request(request[1]));
        commands.put("send", () -> send(request[1]));

        commands.put("client", () -> {
            try {
                client();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void request(String item) {
        if (item.indexOf("tfView") != -1) {
            try {
                // Get default webcam and open it
                Webcam webcam = Webcam.getDefault();
                webcam.open();

                // Get image
                BufferedImage image = webcam.getImage();
                System.out.println("[:] Image captured!");

                // Save image to .png file
                ImageIO.write(image, "PNG", new File("res/img.png"));
                System.out.println("[:] Image Saved!");

                StringBuilder sb = new StringBuilder();
                try (BufferedInputStream is = new BufferedInputStream(new FileInputStream("res/img-blue.png"))) {
                    for (int i; (i = is.read()) != -1;) {
                        String temp = Integer.toHexString(i).toUpperCase();
                        if (temp.length() == 1) {
                            sb.append('0');
                        }
                        sb.append(temp).append(' ');
                    }
                }
                returnValue = sb + "&"; // "&" End character to stop the socket stream
            } catch (Exception exc) {
                System.out.println("[!] Exception caught in \"request()\": " + exc);
            }
        }
    }
    
    public void send(String value) {
        System.out.println("[-] Value: " + value);
    }

    public static void client() throws IOException {
        // * This IP and PORT is the port of the PC Server
        String host = "127.0.0.1"; // For debugging locally
        // String host = "10.163.121.160"; // ? PC Server ipv4
        int port = 8008; // ? PC Server port

        try (Socket echoSocket = new Socket(host, port)) {
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            
            String packet = "server.start:0";

            System.out.println("[%] Sending packet: \"" + packet + "\"!");
            
            out.println(packet);

            String returnedPacket = in.readLine();
            
            System.out.println("[%] Echo: \"" + returnedPacket + "\"!");
        } catch (UnknownHostException exc) {
            System.err.println("[%] Couldn't resolve host: \"" + host + "\"!");
            System.exit(1);
        } catch (IOException exc) {
            System.err.println("[%] Couldn't get I/O for connection: \"" +
                host + "\"!");
            System.exit(1);
        } 
    }
}

