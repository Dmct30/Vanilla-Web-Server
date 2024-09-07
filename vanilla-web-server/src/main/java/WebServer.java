import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebServer {

    private static final int PORT = 8095;
    private static final String SERVER_ROOT = "src/main/www/";

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }

    //Start connection
    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClientRequest(clientSocket);

                } catch (Exception e) {
                    System.out.println("Error handling client request: " + e.getMessage());
                }
            }
        } catch (Exception e){
            System.out.println("Server error: " + e.getMessage());
        }
    }

    //Handle client request
    private void handleClientRequest (Socket clientsocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
             OutputStream out = clientsocket.getOutputStream()) {

            //Handle GET request
            String requestLine = in.readLine();
            if (requestLine != null && requestLine.startsWith("GET")) {
                String[] requestParts = requestLine.split(" ");
                String requestPath = requestParts[1].equals("/") ? "index.html" : requestParts[1];
                File file = new File(SERVER_ROOT + requestPath);

                //Send status code
                if (file.exists() && !file.isDirectory()) {
                    sendResponse(out, 200, "OK", file);
                } else {
                    sendResponse(out, 404, "Not Found", new File(SERVER_ROOT + "/404.html"));
                }
            }

        }catch (Exception e) {
            System.out.println("Client request error: " + e.getMessage());
        }
    }

    //Server response
    private void sendResponse (OutputStream out, int statusCode, String statusText, File file) {
        try (PrintWriter writer = new PrintWriter(out, false)) {
            byte[] fileContent = Files.readAllBytes(Paths.get(file.getPath()));
            String contentType = Files.probeContentType(file.toPath());

            //Print Header
            writer.println("HTTP/1.0 " + statusCode + " " + statusText);
            writer.println("Content-Type: " + contentType);
            writer.println("Content-Length: " + fileContent.length);
            writer.println();
            writer.flush();

            out.write(fileContent);
            out.flush();

        } catch (Exception e) {
            System.out.println("Error sending response: " + e.getMessage());
        }
    }
}
