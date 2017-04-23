	
import java.io.DataInputStream;
import java.util.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.*;
public class Server {
	//private String secret  = "rxchfgjvhbjknlm24356784irokfjmnv";
	public static String secret  = "rxchfgjvhbjknlm24356784irokfjmnv";
	// Declare the port number
	private static int port = 3000;
	
	// Identifies the user number connected
	private static int counter = 0;
	
	public static String resourceFolder="./Resource/";
	//filename which stores resource information
	public static void main(String[] args) {
		
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			System.out.println("Waiting for client connection..");
			
			// Wait for connections.
			while(true){
				Socket client = server.accept();
				counter++;
				System.out.println("Client "+counter+": Applying for connection!");
				
				
				// Start a new thread for a connection
				Thread t = new Thread(() -> {
					try {
						serveClient(client);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				t.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	private static void serveClient(Socket client) throws URISyntaxException{
		Command command=new Command();
		JSONParser parser = new JSONParser();
		ArrayList<String> serverList = new ArrayList<String>();
		
		try(Socket clientSocket = client){
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.
					getInputStream());
			// Output Stream
		    DataOutputStream output = new DataOutputStream(clientSocket.
		    		getOutputStream());
		   System.out.println(input.readUTF());
		    //System.out.println();
		    String response=command.parseCommand(input.readUTF());
		    //System.out.println(response);
		    JSONObject responseObj = (JSONObject) parser.parse(response);
		    JSONObject inputObj = (JSONObject) parser.parse(input.readUTF());
		    System.out.println(inputObj.get("command"));
		    if (inputObj.get("command").toString().equals("exchange") && responseObj.get("response").toString().equals("success")) {
		    	String serverListStr = inputObj.get("serverList").toString();
		    	JSONArray serverArray = (JSONArray) parser.parse(serverListStr);
		    	for (int i = 0; i < serverArray.size(); i++) {
		    		String ip = serverArray.get(i).toString().split(",")[0].split(":")[1].replaceAll("\"", "");
					String port = serverArray.get(i).toString().split(",")[1].split(":")[1].replace("}", "").replaceAll("\"", "");
					String serverRecord = ip + ":" + port;
					serverList.add(serverRecord);
		    	}
		    	System.out.println(serverList);
		    	
		    }
		    
		    
		   //System.out.println("hhhh");
		    
		    output.writeUTF("Server: Hi Client "+counter+" !!!");
		    output.writeUTF(response);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
