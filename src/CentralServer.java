import java.io.*;
import java.net.*;
import java.util.*;

public class CentralServer {
    private static final int PORT = 12340;
    private List<InetSocketAddress> peers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new CentralServer().start();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Central server started at port: " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Handler(socket)).start();
        }
    }

    private class Handler implements Runnable {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String message = in.readLine();
                if (message.startsWith("REGISTER")) {
                    handleRegister(message, out);
                } else if (message.startsWith("GET_PEERS")) {
                    handleGetPeers(out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleRegister(String message, PrintWriter out) {
            String[] parts = message.split(" ");
            String host = parts[1];
            int port = Integer.parseInt(parts[2]);
            InetSocketAddress peer = new InetSocketAddress(host, port);
            synchronized (peers) {
                if (!peers.contains(peer)) {
                    peers.add(peer);
                }
            }
            out.println("REGISTERED " + host + ":" + port);
        }

        private void handleGetPeers(PrintWriter out) {
            synchronized (peers) {
                for (InetSocketAddress peer : peers) {
                    out.println(peer.getAddress().getHostAddress() + ":" + peer.getPort());
                }
            }
        }
    }
}
