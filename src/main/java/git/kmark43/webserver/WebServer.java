package git.kmark43.webserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    public static void main(String[] args) {
        String usage = "Usage: WebServer <port> <docroot>";
        if (args.length != 2) {
            System.out.println(usage);
            return;
        }

        int port;

        try {
             port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println(usage);
            return;
        }

        File docroot = new File(args[1]);
        if (!docroot.exists() || !docroot.isDirectory()) {
            System.out.println(usage);
            return;
        }

        int numThreads = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService service = Executors.newFixedThreadPool(numThreads);

        try (ServerSocket listeningSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = listeningSocket.accept();
                ClientHandler handler = new ClientHandler(docroot, clientSocket);
                service.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
