	
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Server {
	private String secret  = "rxchfgjvhbjknlm24356784irokfjmnv";
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
		try(Socket clientSocket = client){
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.
					getInputStream());
			// Output Stream
		    DataOutputStream output = new DataOutputStream(clientSocket.
		    		getOutputStream());
		    //System.out.println(input.readUTF());
		    String response=command.parseCommand(input.readUTF());
		    output.writeUTF("Server: Hi Client "+counter+" !!!");
		    output.writeUTF(response);
		    if(command.command.equals("fetch")){
				//File f = new File(command.resourceTemplate.uri.toString());
				File f = new File("/Users/HuJP/Desktop/eclipseworkspace/DSProject1/serverfile/sauron.jpg");
				if(f.exists()){
				RandomAccessFile byteFile = new RandomAccessFile(f,"r");
				System.out.println(f.length());

				byte[] sendingBuffer = new byte[1024*1024];
				int num;
				// While there are still bytes to send..
				while((num = byteFile.read(sendingBuffer)) > 0){
					System.out.println(num);
					output.write(Arrays.copyOf(sendingBuffer, num));
				}
				byteFile.close();
			}
		    }
		    //output.writeUTF("over");
		   
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
