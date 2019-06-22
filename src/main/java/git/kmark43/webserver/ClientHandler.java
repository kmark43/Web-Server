package git.kmark43.webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClientHandler implements Runnable {
    private File docroot;
    private Socket clientSocket;

    public ClientHandler(File docroot, Socket clientSocket) {
        this.docroot = docroot;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){
            while (true) {
                if (clientSocket.isClosed()) {
                    return;
                }
                List<String> lines = new ArrayList<>();
                String line;
                line = in.readLine();
                while (line != null && !line.equals("")) {
                    lines.add(line);
                    line = in.readLine();
                }

                if (line == null) {
                    clientSocket.close();
                    return;
                }

                HttpRequest request = parseRequest(lines);
                HttpResponse response = respondToRequest(request);
                if (response != null) {
                    try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {
                        out.println("HTTP/1.1 " + response.getCode());
                        for (String key : response.getProperties().stringPropertyNames()) {
                            String property = response.getProperties().getProperty(key);
                            out.println(key + ": " + property);
                        }
                        out.println();
                        out.flush();
                        clientSocket.getOutputStream().write(response.getBody());
                    }
                }
                if (request == null || response == null ||
                        request.getProperties().getProperty("connection", "close").toLowerCase().equals("close")) {
                    clientSocket.close();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpRequest parseRequest(List<String> lines) {
        String headLine = lines.get(0);
        String[] headTokens = headLine.split(" ");
        if (headTokens.length != 3) {
            return null;
        }

        String path = headTokens[1];
        File file = new File(docroot, path);
        if (!file.exists()) {
            file = null;
        } else {
            if (file.isDirectory()) {
                file = new File(file, "index.html");
            }

            if (isDirectoryTraversal(file)) {
                file = null;
            }
        }

        Properties properties = new Properties();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] tokens = line.split(":");
            String key = tokens[0].toLowerCase().trim();
            String value = tokens[0].trim();
            properties.setProperty(key, value);
        }

        return new HttpRequest(headTokens[0], file, properties);
    }

    private HttpResponse respondToRequest(HttpRequest request) {
        HttpResponse response;
        if (request == null) {
            Properties properties = new Properties();
            properties.setProperty("Content-Length", "0");
            response = new HttpResponse("405 Invalid request", properties, new byte[0]);
            return response;
        }

        if (request.getFile() == null) {
            Properties properties = new Properties();
            properties.setProperty("Content-Length", "0");
            response = new HttpResponse("404 Not found", properties, new byte[0]);
            return response;
        }

        byte[] body;
        try {
            body = Files.readAllBytes(request.getFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Properties properties = new Properties();
        properties.setProperty("Content-Length", "" + body.length);

        response = new HttpResponse("200 ok", properties, body);

        return response;
    }

    private boolean isDirectoryTraversal(File file) {
        try {
            if (!file.getCanonicalPath().equals(file.getAbsolutePath())) {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
        return false;
    }
}
