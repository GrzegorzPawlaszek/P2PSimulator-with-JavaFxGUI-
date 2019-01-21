package pl.nanaki.main;

import pl.nanaki.controllers.SecondWindowController;

import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

public class p2pServer {

    private ServerSocket serverSocket = null;
    private DatagramSocket datagramSocket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String filesPath;
    private String host = "127.0.0.1";
    private int serverPort;
    private SecondWindowController controller;

    public p2pServer(SecondWindowController controller, int port) {
        serverPort = port;
        filesPath = controller.path;
        this.controller = controller;
        Polecenia.addPortToFile(String.valueOf(serverPort));

        if (controller.isTCP) {
            try {
                InetSocketAddress isa = new InetSocketAddress(host, serverPort);
                serverSocket = new ServerSocket();
                serverSocket.bind(isa);
                controller.cmdServer.appendText("Port: " + serverSocket.getLocalPort() + "\n");
                controller.cmdServer.appendText("Bind address: " + serverSocket.getInetAddress());
                controller.cmdServer.appendText("Protocol: TCP");

                acceptNewClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                datagramSocket = new DatagramSocket(serverPort);
                controller.cmdServer.appendText("Port: " + datagramSocket.getLocalPort() + "\n");
                controller.cmdServer.appendText("Protocol: UDP");
            } catch (SocketException e) {
                e.printStackTrace();
            }

        }
    }

    private static int setServerPort(String[] args) {
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            //controller.cmdServer.appendText("Podaj numer portu z zakresu 10000-65000");
        } catch (NumberFormatException e) {
            System.err.println("Niewłaściwy format danych");
            System.exit(1);
        }
        if (port < 10000 || port > 65000) {
            System.out.println("Podaj numer portu z zakresu 10000-65000");
            System.exit(1);
        }
        return port;
    }

    private void acceptNewClient() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                controller.cmdServer.appendText("\nCONNECTION: \n\t" + clientSocket.getRemoteSocketAddress());
                process(clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Pattern requestPattern = Pattern.compile(" +", 2);

    private void process(Socket clientSocket) {

        try {
            InputStream input = clientSocket.getInputStream();
            in = new BufferedReader(new InputStreamReader(input));
            OutputStream output = clientSocket.getOutputStream();
            out = new PrintWriter(output, true);

            for (String msg; (msg = in.readLine()) != null; ) {

                String[] requests = requestPattern.split(msg, 2); // rozbiór zlecenia
                controller.cmdServer.appendText("\tRequest: " + msg);
                String fileName = requests[1];
                if (msg.equals("view files")) {
                    Polecenia.wypiszPliki(filesPath, out);
                    out.println(".\\.");
                } else if (msg.startsWith("get")) {
                    out.flush();                                            ////////////////////////////WPROWADZONE NA RADZIE
                    Polecenia.wyslijPlik(out, filesPath + "\\", fileName);
                    out.flush();
                } else if (msg.startsWith("put")) {
                    out.flush();                                            ////////////////////////////WPROWADZONE NA RADZIE
                    Polecenia.pobierzPlik(in, filesPath + "\\", fileName);
                    out.flush();
                } else {
                    controller.cmdServer.appendText("Invalid request");
                    out.println("Invalid request");
                    out.println(".\\.");
                }

            }

        } catch (SocketException e) {
            controller.cmdServer.appendText("Client disconnected cause socket Exception");

        } catch (Exception e) {
            e.printStackTrace();

        }
        controller.cmdServer.appendText("END\n");
    }

}
