import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TSPParallelBFS {
    private static final int NUM_CITIES = 4;
    private static final int START_CITY = 1;

    public static void main(String[] args) {
        // Create a list of city nodes
        List<CityNode> cityNodes = new ArrayList<>();
        for (int i = 1; i <= NUM_CITIES; i++) {
            cityNodes.add(new CityNode(i));
        }

        // Start the server nodes
        for (CityNode cityNode : cityNodes) {
            new Thread(() -> {
                try {
                    cityNode.startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Connect the client nodes to the server nodes
        for (int i = 0; i < NUM_CITIES; i++) {
            final int cityIndex = i;
            new Thread(() -> {
                try {
                    cityNodes.get(cityIndex).connectToServers(cityNodes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    static class CityNode {
        private final int city;
        private final int port;

        public CityNode(int city) {
            this.city = city;
            this.port = 5000 + city;
        }

        public void startServer() throws IOException {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("City " + city + " is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        }

        public void connectToServers(List<CityNode> cityNodes) throws IOException {
            for (CityNode targetNode : cityNodes) {
                if (targetNode.city != city) {
                    Socket socket = new Socket("localhost", targetNode.port);
                    sendRequest(socket, targetNode.city);
                    receiveResponse(socket);
                    socket.close();
                }
            }
        }

        private void handleRequest(Socket socket) throws IOException {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            int receivedCity = inputStream.read();
            int nextCity = (receivedCity * 10) + city;

            System.out.println("City " + city + " received " + receivedCity + ", sending " + nextCity);
            outputStream.write(nextCity);

            inputStream.close();
            outputStream.close();
            socket.close();
        }

        private void sendRequest(Socket socket, int targetCity) throws IOException {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(targetCity);
            outputStream.flush();
        }

        private void receiveResponse(Socket socket) throws IOException {
            InputStream inputStream = socket.getInputStream();
            int receivedCity = inputStream.read();
            System.out.println("City " + city + " received " + receivedCity);
            inputStream.close();
        }
    }
}
