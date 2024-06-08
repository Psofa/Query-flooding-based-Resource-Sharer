import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Client <host> <port> <file name>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String fileName = args[2];

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("QUERY " + fileName);
            String response = in.readLine();

            if (response.startsWith("HIT")) {
                String[] parts = response.split(" ");
                String hitHost = parts[1];
                int hitPort = Integer.parseInt(parts[2]);

                try (Socket downloadSocket = new Socket(hitHost, hitPort);
                     PrintWriter downloadOut = new PrintWriter(downloadSocket.getOutputStream(), true);
                     InputStream downloadIn = downloadSocket.getInputStream()) {

                    downloadOut.println("DOWNLOAD " + fileName);
                    try (FileOutputStream fos = new FileOutputStream("downloaded_" + fileName)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = downloadIn.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }
}
