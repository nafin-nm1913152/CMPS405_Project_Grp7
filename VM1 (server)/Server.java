
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Server {

	
	private static List<String> connectedClients = new ArrayList<>();
    private static Semaphore semaphore = new Semaphore(1); //allow only one client at a time 
    
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
				
				// Run Network.sh when a new client connects using a separate function
                runScript("Network.sh");
				
				// Handle client communication in a separate thread using ClientHandler defined later below
                new ClientHandler(nextClient).start();
				
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
