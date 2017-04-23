
import java.io.DataInputStream;
import org.apache.commons.cli.*;
import org.json.simple.*;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;

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

		    //String outCommand = commandLine.parse(args, options);
		    //System.out.println(outCommand);
		    
	    	//output.writeUTF(outCommand);

		    JSONObject outCommand = commandLine.parse(args, options);
		    String out = outCommand.toString();
	    	output.writeUTF(out);

	    	output.flush();
	    	
		    while(true){
                        if(input.available() > 0) {

                            String message = input.readUTF();
                            //System.out.println("incoming:");
                            //System.out.println(message);
                            System.out.println(message);
                            message = input.readUTF();
                            System.out.println(message);
                            if(outCommand.get("command").toString().equals("fetch")){
        						String fileName = "/Users/HuJP/Desktop/eclipseworkspace/DSProject1/clientfile/testfile.jpg";
        						RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");
        						long fileSizeRemaining = 108817;

        						int chunkSize = setChunkSize(fileSizeRemaining);
        						byte[] receiveBuffer = new byte[chunkSize];
        						int num;
        						
        						System.out.println("Downloading "+fileName+" of size "+108817);
        						try{
        						while((num=input.read(receiveBuffer))>0){
        							// Write the received bytes into the RandomAccessFile
        							downloadingFile.write(Arrays.copyOf(receiveBuffer, num));
        							
        							// Reduce the file size left to read..
        							fileSizeRemaining-=num;
        							
        							// Set the chunkSize again
        							chunkSize = setChunkSize(fileSizeRemaining);
        							receiveBuffer = new byte[chunkSize];
        							
        							// If you're done then break
        							if(fileSizeRemaining==0){
        								break;
        							}
        						}
        						}
        						catch(EOFException e){
        							
        						}
        						downloadingFile.close();

                        }
                        }
		    }
		    
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

		

		public static int setChunkSize(long fileSizeRemaining){
			// Determine the chunkSize
			int chunkSize=1024*1024;
			
			// If the file size remaining is less than the chunk size
			// then set the chunk size to be equal to the file size.
			if(fileSizeRemaining<chunkSize){
				chunkSize=(int) fileSizeRemaining;
			}
			
			return chunkSize;
		}

}

