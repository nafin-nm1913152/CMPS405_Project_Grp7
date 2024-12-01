import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Server {

	
	private static final Semaphore semaphore = new Semaphore(1); // Synchronize access to system info
    private static final List<ClientRequestInfo> clientRequests = new ArrayList<>();
    private static final List<String> connectedClients = new ArrayList<>();
    
	public static void main(String args[]) 
	{
		ServerSocket server = null;
		Socket nextClient = null;
		
		try{
			//Bind to service port
			server=new ServerSocket(1300);
			
			System.out.println("Server started...");
			System.out.println("Server waiting for client on port "+ server.getLocalPort());
			
			for(;;){
				
				//Get the next Client
				nextClient=server.accept();
				
				//Display connection details
				System.out.println("Server receiving request from "+nextClient.getInetAddress()+ ":" + nextClient.getPort());
				
				//To get the client IP in string format
				String clientIP = nextClient.getInetAddress().getHostAddress();
				
				synchronized (connectedClients) {
                    connectedClients.add(clientIP);
                }
				
				// Run Network.sh when a new client connects using a separate function
                runScript(clientIP);
				
				// Handle client communication in a separate thread using ClientHandler defined later below
                new ClientHandler(nextClient,clientIP).start();
				
			}
		}
		catch(IOException ioe){
			System.out.println("!! Error while running server !!" + ioe);
		}
		finally {
			if (server != null)
				try{
					server.close();
				}
				catch (Exception e){System.err.println(e);}
		}
	}
	
	
	// Method to run Network.sh for the client connection
    private static void runScript(String clientIP) {
        try {
            System.out.println("Running Network.sh to test connection to: " + clientIP);
            Process process = new ProcessBuilder("./network.sh", clientIP).start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running Network.sh: " + e.getMessage());
        }
    }
	
	 // ClientHandler class to manage communication with each client
    static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final String clientIP;
        private final PrintWriter out;
        private final BufferedReader in;

        public ClientHandler(Socket socket, String clientIP) throws IOException {
            this.clientSocket = socket;
            this.clientIP = clientIP;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                // Handle client requests
                String request;
                while ((request = in.readLine()) != null) {
                    if ("GET_SYSTEM_INFO".equals(request)) {
                        handleSystemInfoRequest();
                    } else if ("SHOW_CLIENTS".equals(request)) {
                        showConnectedClients();
                    } else {
                        out.println("Invalid request.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    synchronized (connectedClients) {
                        connectedClients.remove(clientIP);
                    }
                    System.out.println("Client " + clientIP + " disconnected.");
                } catch (IOException e) {
                    System.err.println("Error closing client connection: " + e.getMessage());
                }
            }
        }

        // Handle the GET_SYSTEM_INFO request
        private void handleSystemInfoRequest() {
            if (semaphore.tryAcquire()) {
                try {
                    // Log the client request
                    logClientRequest(clientIP);

                    // Run System.sh and send the generated file to the client
                    Process systemProcess = new ProcessBuilder("./System.sh").start();
                    systemProcess.waitFor();

                    File systemFile = new File("mem_cpu_info.log");
                    File renamedFile = new File("SYSTEM_INFO.log");
                    if (systemFile.exists()) {
                        // Rename the file to SYSTEM_INFO.log
                        Files.move(systemFile.toPath(), renamedFile.toPath());

                        sendFile(renamedFile);
                        System.out.println("System info sent to client: " + clientIP);
                    } else {
                        out.println("System info file not found.");
                    }
                } catch (IOException | InterruptedException e) {
                    out.println("Error processing system info request: " + e.getMessage());
                } finally {
                    semaphore.release();
                }
            } else {
                out.println("System information service is busy. Please try again later.");
            }
        }

        // Log the client request for system info
        private void logClientRequest(String clientIP) {
            synchronized (clientRequests) {
                clientRequests.add(new ClientRequestInfo(clientIP, new Date()));
            }
        }

        // Show the list of connected clients
        private void showConnectedClients() {
            synchronized (connectedClients) {
                if (connectedClients.isEmpty()) {
                    out.println("No clients connected.");
                } else {
                    out.println("Connected clients:");
                    for (String client : connectedClients) {
                        out.println(client);
                    }
                }
            }

            // Display request logs
            synchronized (clientRequests) {
                out.println("\nClient request logs:");
                for (ClientRequestInfo requestInfo : clientRequests) {
                    out.println(requestInfo);
                }
            }
        }

        
     
     // Method to send the system info file to the client using SCP
        private void sendFile(File file) {
            try {
                // Define the target path on the client
                String clientTargetPath = clientIP + ":/Desktop/operating_systems_project/" + file.getName();

                // Use scp to transfer the file to the client's machine
                ProcessBuilder scpProcessBuilder = new ProcessBuilder(
                        "scp", file.getAbsolutePath(), clientTargetPath);

                // Start the SCP process
                Process scpProcess = scpProcessBuilder.start();

                // Wait for the process to complete
                int exitCode = scpProcess.waitFor();

                if (exitCode == 0) {
                    System.out.println("File " + file.getName() + " successfully sent to client: " + clientIP);
                    out.println("File transfer complete. You can find the file at /Desktop/operating_systems_project/" + file.getName() + " on your machine.");
                } else {
                    System.err.println("Error sending file to client: " + clientIP);
                    out.println("Failed to send the file to your machine.");
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error in file transfer: " + e.getMessage());
                out.println("An error occurred during the file transfer.");
            }
        }


    }

    
    // Class to store client request information
    static class ClientRequestInfo {
        private final String clientIP;
        private final Date requestTime;

        public ClientRequestInfo(String clientIP, Date requestTime) {
            this.clientIP = clientIP;
            this.requestTime = requestTime;
        }

        @Override
        public String toString() {
            return "Client: " + clientIP + ", Request Time: " + requestTime;
        }
    }
		
    }
