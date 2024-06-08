import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    private File sharedDirectory;
    private List<InetSocketAddress> peers;
    private int port;
    private InetSocketAddress centralServer;

    public Peer(File sharedDirectory, InetSocketAddress centralServer, int port) {
        this.sharedDirectory = sharedDirectory;
        this.centralServer = centralServer;
        this.port = port;
        this.peers = new ArrayList<>();
    }

    public void start() throws IOException {
        registerWithCentralServer();
        fetchPeersFromCentralServer();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Peer started at port: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Handler(socket)).start();
        }
    }

    private void registerWithCentralServer() throws IOException {
        try (Socket socket = new Socket(centralServer.getAddress(), centralServer.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("REGISTER " + InetAddress.getLocalHost().getHostAddress() + " " + port);
            String response = in.readLine();
            System.out.println(response);
        }
    }

    private void fetchPeersFromCentralServer() throws IOException {
        try (Socket socket = new Socket(centralServer.getAddress(), centralServer.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_PEERS");
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(":");
                peers.add(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
            }
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

                String query = in.readLine();
                if (query.startsWith("QUERY")) {
                    handleQuery(query, out);
                } else if (query.startsWith("DOWNLOAD")) {
                    handleDownload(query, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleQuery(String query, PrintWriter out) throws IOException {
            String[] parts = query.split(" ");
            String fileName = parts[1];

            File file = new File(sharedDirectory, fileName);
            if (file.exists()) {
                out.println("HIT " + socket.getLocalAddress().getHostAddress() + " " + port);
            } else {
                for (InetSocketAddress peer : peers) {
                    try (Socket peerSocket = new Socket(peer.getAddress(), peer.getPort());
                         PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                         BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()))) {

                        peerOut.println(query);
                        String response = peerIn.readLine();
                        if (response.startsWith("HIT")) {
                            out.println(response);
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void handleDownload(String query, PrintWriter out) throws IOException {
            String[] parts = query.split(" ");
            String fileName = parts[1];

            File file = new File(sharedDirectory, fileName);
            if (file.exists()) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                     OutputStream os = socket.getOutputStream()) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Peer <shared directory> <port>");
            return;
        }

        File sharedDirectory = new File(args[0]);
        int port = Integer.parseInt(args[1]);
        InetSocketAddress centralServer = new InetSocketAddress("localhost", 12340);

        Peer peer = new Peer(sharedDirectory, centralServer, port);
        peer.start();
    }
}
