
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
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
	private static boolean secure = false;
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
	
	public static void setSecure() {
		Client.secure = true;
	}

	public static void main(String[] args) throws URISyntaxException, ParseException {
		JSONObject outCommand = commandLine.parse(args, options);
		String out = outCommand.toString();
		JSONParser parser = new JSONParser();
		try (Socket socket = new Socket(host, port)) {
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
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
			
			if (!outCommand.isEmpty()) {
				if (!outCommand.get("command").toString().equals("FETCH")) {
					while (true) {
						if (input.available() > 0) {

							try {
								String message = input.readUTF();
								System.out.println(message);

								if (commandLine.debug(args, options)) {
									logger.fine("RECEIVED:" + message);
								}
							} catch (EOFException e) {

							}
						}
					}
				} else {
					try {
						String message = input.readUTF();
						System.out.println(message);

						if (commandLine.debug(args, options)) {
							logger.fine("RECEIVED:" + message);
						}
					} catch (EOFException e) {

					}

					String resource = input.readUTF();
					System.out.println(resource);
					if (commandLine.debug(args, options)) {
						logger.fine("RECEIVED:" + resource);
					}

					JSONObject jsonResource = (JSONObject) parser.parse(resource);
					if (jsonResource.containsKey("resultSize"))
						return;
					File clientfile = new File("clientfile");

					if (!clientfile.isDirectory()) {

						clientfile.mkdir();

					}
					String fileName = "clientfile/";
					if (jsonResource.get("name").toString().equals("")) {
						fileName += "nonamefile";
					} else {
						fileName += jsonResource.get("name").toString();
					}
					long fileSizeRemaining = Long.parseLong(jsonResource.get("resourceSize").toString());
					RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");

					int chunkSize = setChunkSize(fileSizeRemaining);
					byte[] receiveBuffer = new byte[chunkSize];
					int num;

					System.out.println("Downloading " + fileName + " of size " + fileSizeRemaining);
					try {
						while ((num = input.read(receiveBuffer)) > 0) {
							downloadingFile.write(Arrays.copyOf(receiveBuffer, num));

							fileSizeRemaining -= num;

							chunkSize = setChunkSize(fileSizeRemaining);
							receiveBuffer = new byte[chunkSize];

							if (fileSizeRemaining == 0) {
								break;
							}
						}
					} catch (EOFException e) {

					}
					downloadingFile.close();

					String resultSize = input.readUTF();
					System.out.println(resultSize);
					if (commandLine.debug(args, options)) {
						logger.fine("RECEIVED:" + resultSize);
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
		int chunkSize = 1024 * 1024;
		if (fileSizeRemaining < chunkSize) {
			chunkSize = (int) fileSizeRemaining;
		}

		return chunkSize;
	}

}
