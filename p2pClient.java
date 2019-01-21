package pl.nanaki.main;

import pl.nanaki.controllers.SecondWindowController;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class p2pClient {

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private String filesPath;
    private ArrayList<Integer> ports = new ArrayList<Integer>();
    private static int clientPort;

//==========================================================================
//    private DataInputStream in;
//    private DataOutputStream out;
    private SecondWindowController controller;

    private InetAddress IPAddress;
//    private DatagramSocket socket;
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;


    public p2pClient(SecondWindowController controller, int port) {
        String host = "127.0.0.1";
        this.controller = controller;
        clientPort = port;
        filesPath = controller.path;
        try {
            String msg = controller.cmd.getText();// + "\n";
            sendRequest(msg);

        } catch (UnknownHostException e) {
            controller.cmdClient.appendText("Nieznany host: " + host);
            System.exit(2);
        } catch (IOException e) {
            controller.cmdClient.appendText("I/O exception");
            e.printStackTrace();
            System.exit(3);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(4);
        }
    }

    public void connect(int port) {
        String host = "127.0.0.1";
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            controller.cmdClient.appendText("\n\tRECEIVED FROM: " + socket.getRemoteSocketAddress() + "\n");
        } catch (ConnectException e) {
//			e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Pattern requestPattern = Pattern.compile(" +", 2);

    private void sendRequest(String req) throws InterruptedException, IOException {
        String[] requests = requestPattern.split(req, 2);
        String keyWord = requests[0];
        ports = getPortsFromFile();
        switch (keyWord) {
            case "get": {
                if (requests.length < 2 || requests[1].length() == 0)
                    controller.cmdClient.appendText("Invalid request");
                else {
                    for (int i = 0; i < ports.size(); i++) {
                        connect(ports.get(i));
                        controller.cmdClient.appendText("Request: " + req + "\n");
                        out.println(req);
                        String fileName = requests[1];
                        try {
                            Polecenia.pobierzPlik(in, filesPath + "\\", fileName);
                        } catch (SocketException e) {
                            controller.cmdClient.appendText(filesPath + "\\" + fileName);
                            controller.cmdClient.appendText("\nPrzerwanie połączenia z hostem\n");
                            File tmp = new File(filesPath + "\\" + fileName);
                            double downloadedBytes = tmp.length();
                            controller.cmdClient.appendText("Udalo sie pobrac " + downloadedBytes);
                            for (int ponownePołaczenie = 0; ponownePołaczenie < 5; ponownePołaczenie++) {
                                controller.cmdClient.appendText("Próba ponownego nawiązania połączenia");
                                connect(ports.get(i));
                                Thread.sleep(1000);
                                try {
                                    out.println(req);
                                    Polecenia.pobierzPlik(in, filesPath + "\\", fileName, downloadedBytes);
                                    out.flush();
                                    if (socket.isConnected()) {
                                        break;
                                    }
                                } catch (SocketException ex) {}
                                if (ponownePołaczenie==4)
                                    controller.cmdClient.appendText("\nNieudana próba pobrania pliku");
                            }
                            try {
                                in.close();
                                out.close();
                                socket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        try {
                            in.close();
                            out.close();
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            break;
            case "putAll": {
                if (requests.length < 2 || requests[1].length() == 0)
                    controller.cmdClient.appendText("Invalid request");
                else {
                    for (int i = 0; i < ports.size(); i++) {
                        connect(ports.get(i));
                        controller.cmdClient.appendText("Request: " + req + "\n");
                        out.println(req);
                        String fileName = requests[1];
                        Polecenia.wyslijPlik(out, filesPath + "\\", fileName);
                        try {
                            in.close();
                            out.close();
                            socket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            break;
            case "put": {
                if (requests.length < 2 || requests[1].length() == 0)
                    controller.cmdClient.appendText("1. Invalid request - usage \"put <port> <file>\"");
                else {
                    String[] put = requestPattern.split(requests[1], 2);
                    if ((put.length < 2) || (put[1].length() == 0))
                        controller.cmdClient.appendText("2. Invalid request - usage \"put <port> <file>\"");
                    else {
                        int hostPort = 0;
                        try {
                            hostPort = Integer.parseInt(put[0]);
                        } catch (NumberFormatException e) {
                            controller.cmdClient.appendText("3. Invalid request - usage \"put <port> <file>\"");
                        }
                        try {
                            connect(hostPort);
                            controller.cmdClient.appendText("Request: " + req + "\n");
                            req = "put " + put[1];
                            out.println(req);
                            String fileName = put[1];
                            Polecenia.wyslijPlik(out, filesPath + "\\", fileName);
                            in.close();
                            out.close();
                            socket.close();
                        } catch (NullPointerException e) {
                            controller.cmdClient.appendText("4. Invalid request - usage \"put <port> <file>\"");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            break;
            case "viewAll": {
                // if (ports.size()!=1)
                for (int i = 0; i < ports.size(); i++) {
                    connect(ports.get(i));
                    controller.cmdClient.appendText("connect poszlo");
                    out.println("view files");
                    receiveResponse();
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            break;
            case "view": {
                viewMyFiles();
            }
            break;
            default: {
                controller.cmdClient.appendText("Invalid request w default");
            }
        }

    }

    private void viewMyFiles() {
        File folder = new File(filesPath);
        if (folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList.length > 0)
                for (File plik : fileList)
                    controller.cmdClient.appendText("  " + plik.getName() + "\n\tSuma kontrolna " + Polecenia.obliczMD5(plik) + "\n");
            else
                controller.cmdClient.appendText("No files available");
        }
    }

    private static ArrayList<Integer> getPortsFromFile() {
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "C:\\Users\\GrzegorzPawlaszek\\IntelliJ-workspace\\portNumbers.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(clientPort + ""))
                    continue;
                tmp.add(Integer.parseInt(line));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    private void receiveResponse() throws InterruptedException {
        boolean receiving = true;
        try {
            String resp = in.readLine();
            while (receiving) {
                controller.cmdClient.appendText(resp);
                resp = in.readLine();
                if (resp.equals(".\\."))
                    receiving = false;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Thread.sleep(1500);
            controller.cmdClient.appendText("Connection closed");
        } catch (IOException e) {
            controller.cmdClient.appendText("You are disconnected.");
        }

    }

}
