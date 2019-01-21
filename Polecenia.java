package pl.nanaki.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Polecenia {

    public static void addPortToFile(String port) {
        Writer output;
        try {
            output = new BufferedWriter(new FileWriter(
                    "C:\\Users\\GrzegorzPawlaszek\\IntelliJ-workspace\\portNumbers.txt", true));
            output.append(port + System.lineSeparator());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removePortFromFile(String port) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "C:\\Users\\GrzegorzPawlaszek\\IntelliJ-workspace\\portNumbers.txt"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(port))
                    continue;
                sb.append(line + System.lineSeparator());
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(
                    "C:\\Users\\GrzegorzPawlaszek\\IntelliJ-workspace\\portNumbers.txt"));
            writer.write(sb.toString());
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void wypiszPliki(String path, PrintWriter out) throws IOException {
        File folder = new File(path);
        if (folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList.length > 0) {
                for (File plik : fileList)
                    out.print("  " + plik.getName() + "\n\tSuma kontrolna " + obliczMD5(plik) + "\n");
                out.flush();
            } else
                out.println("No files available");
            out.flush();
        }
    }

    public static String obliczMD5(File file) {
        MessageDigest md5 = null;
        FileInputStream fis = null;
        BigInteger bigInt = null;
        String result = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            if (file.length() > 0) {
                byte data[] = new byte[(int) file.length()];
                int read = 0;
                while ((read = fis.read(data)) != -1)
                    md5.update(data, 0, read);
                byte tab[] = md5.digest();
                bigInt = new BigInteger(1, tab);
                result = bigInt.toString(16);
                fis.close();
            } else
                result = "- nie można obliczyć - wielkosc pliku 0 B";
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void wyslijPlik(PrintWriter out, String filePath, String name) {
        FileInputStream fis = null;
        int sizeOfFile = -1;
        try {
            File file = new File(filePath + name);
            fis = new FileInputStream(file);
            sizeOfFile = (int) file.length();
            int counter = 0;
            out.println(sizeOfFile);
            out.flush();
            out.println(obliczMD5(file));
            out.flush();
            int wrt = fis.read();
            while (wrt != -1) {
                out.write(wrt);
                out.flush();
                wrt = fis.read();
                if ((++counter) == 0x100) {
                    Thread.sleep(300);
                    counter = 0;
                }
            }
            out.write(wrt);
            fis.close();
            System.out.print("Wysłano plik: " + name);
            System.out.println("\trozmiar: " + sizeOfFile + " B");
            System.out.println("\t" + obliczMD5(file));
        } catch (FileNotFoundException e) {
            System.err.println("Brak pliku o podanej nazwie");
            out.println(sizeOfFile);
            out.flush();
        } catch (IOException e) {
            System.err.println("IOException w wyslijPlik");
            e.printStackTrace();
            out.flush();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void pobierzPlik(BufferedReader in, String pathToSave, String name) throws SocketException, IOException {
        pobierzPlik(in, pathToSave, name, 0);
    }
    public static void pobierzPlik(BufferedReader in, String pathToSave, String name, double fromByte) throws SocketException, IOException {
        try {
            String size = in.readLine();
            int sizeOfFile = Integer.parseInt(size);
            int downloadedBytes = 0;
            int counter = 0;
            if (sizeOfFile != -1) {
                String md5 = in.readLine();
                File file = new File(pathToSave + name);
                FileOutputStream fos = new FileOutputStream(file, true);
                int data  = in.read();
                for (int bytesToSkip=0; bytesToSkip<fromByte; bytesToSkip++) {
                    data = in.read();
                }
                for (int i = 0; i < (sizeOfFile-fromByte); i++) {
                    fos.write(data);
                    data = in.read();
                    downloadedBytes++;
                    if ((++counter) == 0x100) {
                        System.out.print(downloadedBytes + "B ");
                        Thread.sleep(300);
                        counter = 0;
                    }
                }
                fos.close();
                System.out.print("\nPobrano plik " + name);
                System.out.println("\trozmiar: " + downloadedBytes + " B of " + sizeOfFile + " B ");
                System.out.println("\tSuma Kontrolna: " + md5);
            } else {
                System.out.println("Brak pliku o podanej nazwie");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Błąd pliku podczas pobierania");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
