import java.io.*;
import java.net.*;
// import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            int city = Integer.parseInt(args[0]);
            int port = 5000;
            int numCities = 4;
            String host = "localhost";
            int[] distances = { 0, 1, 3, 2 }; // ระยะทางระหว่างเมือง 1-4

            Socket[] sockets = new Socket[numCities];
            PrintWriter[] writers = new PrintWriter[numCities];
            BufferedReader[] readers = new BufferedReader[numCities];

            // เชื่อมต่อกับโหนดที่เหลือในเครือข่าย
            for (int i = 0; i < numCities; i++) {
                if (i + 1 != city) {
                    sockets[i] = new Socket(host, port + i + 1);
                    writers[i] = new PrintWriter(sockets[i].getOutputStream(), true);
                    readers[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
                }
            }

            // ส่งข้อมูลตัวเองไปยังโหนดอื่น
            for (int i = 0; i < numCities; i++) {
                if (i + 1 != city) {
                    writers[i].println(city);
                    writers[i].flush();
                }
            }

            // รับข้อมูลจากโหนดอื่น
            for (int i = 0; i < numCities; i++) {
                if (i + 1 != city) {
                    String receivedData = readers[i].readLine();
                    System.out.println("Node " + city + " received: " + receivedData);
                }
            }

            // ปิดการเชื่อมต่อ
            for (int i = 0; i < numCities; i++) {
                if (i + 1 != city) {
                    readers[i].close();
                    writers[i].close();
                    sockets[i].close();
                }
            }

            // คำนวณระยะทาง
            int totalDistance = 0;
            for (int i = 0; i < numCities; i++) {
                if (i + 1 != city) {
                    totalDistance += distances[i];
                }
            }

            System.out.println("Node " + city + " total distance: " + totalDistance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
