import java.io.*;
import java.net.*;

public class Client1 {
    private static final String SCRIPT1 = "./login.sh";
    private static final String SCRIPT2 = "./check.sh";
    private static final int SERVER_PORT = 1300;
    private static final long REQUEST_INTERVAL = 5 * 60 * 1000; // 5 mins in milliseconds

    public static void main(String[] args) {
        Socket clientSocket = null;
        BufferedReader userInputReader = null;
        PrintWriter serverWriter = null;

        try {
            // Get server address from user input 
            userInputReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the server IP address:");
            String serverAddress = userInputReader.readLine();

            // get connection to the server 
            clientSocket = new Socket(serverAddress, SERVER_PORT);
            System.out.println("Connected to the server at " + serverAddress);

            //Execute shell scripts and display their output
            executeShellScript(SCRIPT1);
            executeShellScript(SCRIPT2);

            // Handle system information requests 
            long lastRequestTime = System.currentTimeMillis();

            while (true) {
                long currentTime = System.currentTimeMillis();

                // Check if the 5 mins interval has passed 
                if (currentTime - lastRequestTime >= REQUEST_INTERVAL) {
                    lastRequestTime = currentTime;

                    // Ask the user if they want to request system info 
                    System.out.println("5 minutes passed. Do you want to request system info from the server? (yes/no)");
                    String userResponse = userInputReader.readLine().trim().toLowerCase();

                    if (userResponse.equals("yes")) {
                        requestSystemInfo(clientSocket);
                    } else {
                        System.out.println("Do you want to disconnect from the server? (yes/no)");
                        String disconnectResponse = userInputReader.readLine().trim().toLowerCase();
                        if (disconnectResponse.equals("yes")) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
        } finally {
            // Cleanup resources
            try {
                if (clientSocket != null) clientSocket.close();
                if (userInputReader != null) userInputReader.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private static void executeShellScript(String scriptPath) {
        System.out.println("Running script: " + scriptPath);
        try {
            Process process = new ProcessBuilder(scriptPath).start();
            BufferedReader scriptOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = scriptOutput.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
            System.out.println("Finished executing " + scriptPath);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running script " + scriptPath + ": " + e.getMessage());
        }
    }

    private static void requestSystemInfo(Socket socket) {
        try {
            // Send request to the server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("REQUEST_SYSTEM_INFO");

            // Receive system info file from the server
            InputStream inputStream = socket.getInputStream();
            File systemInfoFile = new File("system_info_client1.txt");
            FileOutputStream fileOutput = new FileOutputStream(systemInfoFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }
            fileOutput.close();

            System.out.println("System info saved to " + systemInfoFile.getName());

            // Display the content of the file
            BufferedReader fileReader = new BufferedReader(new FileReader(systemInfoFile));
            String line;
            System.out.println("Contents of the system info file:");
            while ((line = fileReader.readLine()) != null) {
                System.out.println(line);
            }
            fileReader.close();

        } catch (IOException e) {
            System.err.println("Error requesting or processing system info: " + e.getMessage());
        }
    }
}
