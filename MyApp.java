import java.io.*;
import java.net.*;
import java.util.*;

public class MyApp {
    private static final int SERVER_PORT = 8081;
    private static final String[] NODES = {"node1", "node2", "node3", "node4"};

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java MyApp [server|client] [nodeNum]");
            System.exit(1);
        }

        String mode = args[0];
        int nodeNum = Integer.parseInt(args[1]);

        if (mode.equals("server")) {
            startServer(nodeNum);
        } else if (mode.equals("client")) {
            startClient(nodeNum);
        } else {
            System.err.println("Invalid mode: " + mode);
            System.exit(1);
        }
    }

    private static void startServer(int nodeNum) {
        try {
            // Start server socket
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started on port " + SERVER_PORT);

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());

                // Read message from client
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = reader.readLine();
                System.out.println("Received message: " + message);

                // Add node number to message
                String[] tokens = message.split(",");
                int srcNode = Integer.parseInt(tokens[tokens.length - 1]);
                int[] destNodes = getDestNodes(srcNode);
                for (int i = 0; i < destNodes.length; i++) {
                    if (destNodes[i] != nodeNum) {
                        sendMessage(tokens[0] + "," + tokens[1] + "," + (destNodes[i]), NODES[destNodes[i] - 1]);
                    }
                }

                // Close connection
                reader.close();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient(int nodeNum) {
        // Create initial message
        String message = nodeNum + "";
        for (int i = 1; i <= NODES.length; i++) {
            if (i != nodeNum) {
                message += "," + i;
            }
        }

        // Send initial message to server
        sendMessage(message, NODES[nodeNum - 1]);
        System.out.println("Sent message: " + message + " to server");

        // Listen for incoming messages
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Listening for incoming messages on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String receivedMessage = reader.readLine();
                System.out.println("Received message: " + receivedMessage);

                // If message contains this node's number, terminate program
                if (receivedMessage.contains(nodeNum + "")) {
                    System.out.println("Received final message, terminating program");
                    reader.close();
                    clientSocket.close();
                    serverSocket.close();
                    System.exit(0);
                }

            // Add this node's number to message and send to server
            int[] destNodes = getDestNodes(nodeNum);
            String[] tokens = receivedMessage.split(",");
            int srcNode = Integer.parseInt(tokens[tokens.length - 1]);
            for (int i = 0; i < destNodes.length; i++) {
                if (destNodes[i] != srcNode) {
                    sendMessage(receivedMessage + "," + nodeNum, NODES[destNodes[i] - 1]);
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static void sendMessage(String message, String hostname) {
    try {
        Socket socket = new Socket(hostname, SERVER_PORT);
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.println(message);
        writer.close();
        socket.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static int[] getDestNodes(int srcNode) {
    int[] destNodes = new int[NODES.length - 1];
    int j = 0;
    for (int i = 0; i < NODES.length; i++) {
        if (i + 1 != srcNode) {
            destNodes[j++] = i + 1;
        }
    }
    return destNodes;
}
}
