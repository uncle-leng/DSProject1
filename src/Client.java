
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.html.parser.Parser;

import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Client {

	// IP and port
	private static String host = "localhost";
	private static int port = 3000;
	
	
	private static CommandLineHandle commandLine = new CommandLineHandle();
	private static Options options = commandLine.getOptions();

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		Client.host = host;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Client.port = port;
	}

	public static void main(String[] args) throws URISyntaxException, ParseException {
		JSONObject outCommand = commandLine.parse(args, options);
		String out = outCommand.toString();
		JSONParser parser = new JSONParser();
		// System.out.println(out);
		try (Socket socket = new Socket(host, port)) {
			// Output and Input Stream
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			// String outCommand = commandLine.parse(args, options);
			// System.out.println(outCommand);

			// output.writeUTF(outCommand);

			output.writeUTF(out);
			output.flush();

			Logger logger = Logger.getLogger("Client");
			logger.setLevel(Level.ALL);
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.ALL);
			logger.addHandler(consoleHandler);
			if (commandLine.debug(args, options)) {

				logger.info("setting debug on");
				logger.fine("SENT:" + outCommand);
			}

			if (input.available() > 0){
			
					String message = input.readUTF();
					//System.out.println("message" + message);

					// message = input.readUTF();
					if (commandLine.debug(args, options)) {
						logger.fine("RECEIVED:" + message);
					}
					
				
			}
					// System.out.println(message);
					if (!outCommand.isEmpty()) {
						if (outCommand.get("command").toString().equals("fetch")) {
							String resource = input.readUTF();
							//System.out.println("resource" + resource);
							if (commandLine.debug(args, options)) {
								logger.fine("RECEIVED:" + resource);
							}
								JSONObject jsonResource = (JSONObject) parser.parse(resource);
								String fileName = "clientfile/" + jsonResource.get("name").toString();
								long fileSizeRemaining = Long.parseLong(jsonResource.get("resourceSize").toString());
								RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");

								int chunkSize = setChunkSize(fileSizeRemaining);
								byte[] receiveBuffer = new byte[chunkSize];
								int num;

								System.out.println("Downloading " + fileName + " of size " + fileSizeRemaining);
								try {
									while ((num = input.read(receiveBuffer)) > 0) {
										// Write the received bytes into the
										// RandomAccessFile
										downloadingFile.write(Arrays.copyOf(receiveBuffer, num));

										// Reduce the file size left to read..
										fileSizeRemaining -= num;

										// Set the chunkSize again
										chunkSize = setChunkSize(fileSizeRemaining);
										receiveBuffer = new byte[chunkSize];

										// If you're done then break
										if (fileSizeRemaining == 0) {
											break;
										}
									}
								} catch (EOFException e) {

								}
								downloadingFile.close();

								String resourceSize = input.readUTF();
								//System.out.println(resourceSize);
								if (commandLine.debug(args, options)) {
									logger.fine("RECEIVED:" + resourceSize);
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

	public static int setChunkSize(long fileSizeRemaining) {
		// Determine the chunkSize
		int chunkSize = 1024 * 1024;

		// If the file size remaining is less than the chunk size
		// then set the chunk size to be equal to the file size.
		if (fileSizeRemaining < chunkSize) {
			chunkSize = (int) fileSizeRemaining;
		}

		return chunkSize;
	}

}
