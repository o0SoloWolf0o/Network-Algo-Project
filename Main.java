import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final int NUM_CITIES = 4;
    private static final int[] PORTS = {5001, 5002, 5003, 5004};

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<>();
        ReentrantLock lock = new ReentrantLock();
    
        // Add a 2D array to represent distances between cities
        int[][] distances = {
            {0, 100, 15, 20},
            {100, 0, 35, 25},
            {150, 35, 0, 30},
            {200, 25, 30, 0}
        };
    
        for (int i = 0; i < NUM_CITIES; i++) {
            int port = PORTS[i];
            // Update this line to pass the distances to the Node constructor
            Node node = new Node(i + 1, port, lock, distances[i]);
            nodes.add(node);
            node.start();
        }

        for(Node node: nodes){
            try {
                node.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Node node: nodes){
            node.calculateDistances();
        }
    }

    static class Node extends Thread {
        private final int city;
        private final int port;
        private final List<String> neighbors;
        private boolean isSent;
        private List<String> receivedMessages;
        private ReentrantLock lock;
        private final int[] distances;

        public Node(int city, int port, ReentrantLock lock, int[] distances) {
            this.city = city;
            this.port = port;
            this.lock = lock;
            this.neighbors = new ArrayList<>();
            this.receivedMessages = new ArrayList<>();
            this.isSent = false;
            this.distances = distances;
            for (int i = 0; i < NUM_CITIES; i++) {
                if (i + 1 != city) {
                    this.neighbors.add("node " + (i + 1));
                }
            }
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println(String.format("\n[Info] Node %d is running on port %d\n", city, port));

                lock.lock();
                try{
                    if(!isSent){
                        for (int i = 1; i <= NUM_CITIES; i++) {
                            if (i != city) {
                                sendMessage("node " + i, String.valueOf(city));
                            }
                        }
                        isSent = true;
                    }
                } finally{
                    lock.unlock();
                }

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message = reader.readLine();
                    receivedMessages.add(message);

                    if (receivedMessages.size() == NUM_CITIES - 1) {
                        System.out.println(String.format("[Received] Node %d received: %s", city, receivedMessages));
                        break;
                    }
                }

                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendMessage(String neighbor, String message) {
            try {
                String[] neighborInfo = neighbor.split(" ");
                String neighborHost = "localhost";
                int neighborPort = PORTS[Integer.parseInt(neighborInfo[1]) - 1];
                Socket socket = new Socket(neighborHost, neighborPort);

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(message);

                socket.close();
                TimeUnit.SECONDS.sleep(2);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void calculateDistances() {
            int totalDistance = 0;
            for (String receivedCity : receivedMessages) {
                int cityIndex = Integer.parseInt(receivedCity) - 1;
                totalDistance += distances[cityIndex];
            }
        System.out.println(String.format("[Neighbors] Node %d has neighbors: %s", city, neighbors));
        System.out.println(String.format("[Total Distance] Total distance for node %d: %d\n", city, totalDistance));
    }
    }
}
