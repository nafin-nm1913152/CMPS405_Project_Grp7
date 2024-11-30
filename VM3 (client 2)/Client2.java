import java.net.*;
import java.io.*;

public class Client2 {
	private static final String SCRIPT1_PATH = "/home/m_reyad/Desktop/operating_systems_project/phase_02/scripts/search.sh";
	private static final String SCRIPT2_PATH = "/home/m_reyad/Desktop/operating_systems_project/phase_02/scripts/clientinfo.sh";
	public static void main(String[] args) {
		Socket sock = null;
		BufferedReader userReader = null;
		BufferedReader fileReader = null;
		
		ProcessBuilder scriptPb1 = new ProcessBuilder(SCRIPT1_PATH);
		ProcessBuilder scriptPb2 = new ProcessBuilder(SCRIPT2_PATH);
		
		try {
			// Getting the IP address of the server from the user:
			userReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter the IP adddress of the server: ");
			String serverAddr = userReader.readLine();
			
			// Trying to connect to the server:
			sock = new Socket(serverAddr, 1300);
			
			// Running both scripts (search.sh) and (clientinfo.sh):
			Process scriptProcess1 = scriptPb1.start();
			Process scriptProcess2 = scriptPb2.start();
			
			// Displaying the results of both shell scripts:
			// First, we display the results of script 1 (search.sh):
			System.out.println("Displaying the results of script 1 (search.sh):\n");
			fileReader = new BufferedReader(new FileReader("bigfile.log"));
			String line;
			while ((line = fileReader.readLine()) != null) {
				System.out.println(line);
			}
			
			// Adding blank lines to improve readability:
			System.out.println("\n\n\n\n");
			
			// Now we display the results of script 2 (clientinfo.sh):
			System.out.println("Displaying the results of script 2 (clientinfo.sh):\n");
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
						// To be implemented later (according to Server.java)
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