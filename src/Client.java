
import java.io.DataInputStream;
import org.apache.commons.cli.*;
import org.json.simple.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Client {
	
	// IP and port
	private static String ip = "localhost";
	private static int port = 3000;
	private static CommandLineHandle commandLine = new CommandLineHandle();
	private static Options options = commandLine.getOptions();
	
	public static void main(String[] args) throws URISyntaxException {
		try(Socket socket = new Socket(ip, port)){
			// Output and Input Stream
			DataInputStream input = new DataInputStream(socket.
					getInputStream());
		    DataOutputStream output = new DataOutputStream(socket.
		    		getOutputStream());
		    String outCommand = commandLine.parse(args, options);
		    
	    	output.writeUTF(outCommand);
	    	output.flush();
	    	
		    while(true){
                        if(input.available() > 0) {
                            String message = input.readUTF();
                            System.out.println(message);
                        }
	    		
		    }
		    
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

