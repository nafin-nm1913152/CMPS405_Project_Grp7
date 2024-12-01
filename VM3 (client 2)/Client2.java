import java.net.*;
import java.io.*;

public class Client2 {
	private static final String SCRIPT1 = "./Search.sh";
	private static final String SCRIPT2 = "./Clientinfo.sh";
	public static void main(String[] args) {
		Socket sock = null;
		BufferedReader userReader = null;
		BufferedReader fileReader = null;
		PrintWriter writer = null;
		
		ProcessBuilder scriptPb1 = new ProcessBuilder(SCRIPT1);
		ProcessBuilder scriptPb2 = new ProcessBuilder(SCRIPT2);
		
		try {
			// Getting the IP address of the server from the user:
			userReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the IP adddress of the server: ");
			String serverAddr = userReader.readLine();
			
			// Trying to connect to the server:
			sock = new Socket(serverAddr, 1300);
			
			// Running both scripts (Search.sh) and (Clientinfo.sh):
			Process scriptProcess1 = scriptPb1.start();
			Process scriptProcess2 = scriptPb2.start();
			
			// Displaying the results of both shell scripts:
			// First, we display the results of script 1 (Search.sh):
			System.out.println("Displaying the results of script 1 (Search.sh):\n");
			fileReader = new BufferedReader(new FileReader("bigfile.log"));
			String line;
			while ((line = fileReader.readLine()) != null) {
				System.out.println(line);
			}
			
			// Adding blank lines to improve readability:
			System.out.println("\n\n\n\n");
			
			// Now we display the results of script 2 (Clientinfo.sh):
			System.out.println("Displaying the results of script 2 (Clientinfo.sh):\n");
			fileReader = new BufferedReader(new FileReader("process_info.log"));
			while ((line = fileReader.readLine()) != null) {
				System.out.println(line);
			}
			
			long interval = 5 * 60 * 1000; // An interval of 5 minutes
			long start_time = System.currentTimeMillis();
			long current_time;
			// At the beginning, we assume the 5 minutes interval has passed:
			boolean intervalPassed = true; 
			while (true) {
				current_time = System.currentTimeMillis();
				// Checking whether the 5 minutes interval has passed:
				if (current_time - start_time >= interval) {
					intervalPassed = true;
				}
				
				if (intervalPassed) {
					// Interval is set to false until another 5 minutes are passed:
					// (The user is given a chance to request system information every 5 minutes exactly):
					intervalPassed = false;
					// Resetting the start time:
					start_time = System.currentTimeMillis();
					// Asking the user whether to request system information from the server:
					System.out.println("Would you like to request system information from the server? (yes/no): ");
					String response = userReader.readLine().trim().toLowerCase();
					if (response.equals("yes")) {
						writer = new PrintWriter(sock.getOutputStream(), true);
						writer.println("GET_SYSTEM_INFO");
						fileReader = new BufferedReader(new FileReader("SYSTEM_INFO.log"));  
						while ((line = fileReader.readLine()) != null) {
							System.out.println(line);
						}
					}
					else {
						System.out.println("Would you like to disconnect from the server? (yes/no): ");
						response = userReader.readLine().trim().toLowerCase();
						if (response.equals("yes")) {
							break;
						}
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		}
		catch (IOException ioe) {
			System.err.println("An I/O error occured: " + ioe.getMessage());
		}
		finally {
			try {
				if (sock != null) {
					sock.close();
				}
				if (userReader != null) {
					userReader.close();
				}
				if (fileReader != null) {
					fileReader.close();
				}
			}
			catch (IOException ioe) {
				System.err.println("An I/O error occurred: " + ioe.getMessage());
			}
		}
		
	}
}
