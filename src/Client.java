
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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
		
		if(secure){
			System.setProperty("javax.net.ssl.trustStore", "clientKeystore/root");
			//System.setProperty("javax.net.debug","all");
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			try {
				SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, port);
				DataInputStream input = new DataInputStream(sslsocket.getInputStream());
				InputStreamReader inputstreamreader = new InputStreamReader(input);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				
				OutputStream outputstream = sslsocket.getOutputStream();
				DataOutputStream output=new DataOutputStream(sslsocket.getOutputStream());
				OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
				BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);
				output.writeUTF(out);
				
				//JSONObject outjson =(JSONObject) parser.parse(out);
				
				
				/*bufferedwriter.write(out);
				bufferedwriter.flush();*/
				
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
					if (!outCommand.get("command").toString().equals("FETCH") && !outCommand.get("command").toString().equals("SUBSCRIBE")) {
								try {
									String message=input.readUTF();
									System.out.println(message);

									if (commandLine.debug(args, options)) {
										logger.fine("RECEIVED:" + message);
									}
									sslsocket.close();
									return;
									
								} catch (EOFException e) {
										//System.out.println("hello");
								}
							
						}else if(outCommand.get("command").toString().equals("FETCH")){
							try {
								String message = bufferedreader.readLine();
								System.out.println(message);

								if (commandLine.debug(args, options)) {
									logger.fine("RECEIVED:" + message);
								}
							} catch (EOFException e) {

							}
							String resource = bufferedreader.readLine();
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
								while ((num = input.read(receiveBuffer)) > 0) {//may cause bug
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

							String resultSize = bufferedreader.readLine();
							System.out.println(resultSize);
							if (commandLine.debug(args, options)) {
								logger.fine("RECEIVED:" + resultSize);
							}
						}
						else {
							
							Thread interaction = new Thread(() -> {
								try {
									SSLinputWaiting(output,input,sslsocket,outCommand.get("id").toString());
								} catch (URISyntaxException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							});
							interaction.start();
							
							
							String message=null;
							while(true){
							//while ((message = bufferedreader.readLine()) != null){
										try{
										message = input.readUTF();
										System.out.println(message);

										if (commandLine.debug(args, options)) {
											logger.fine("RECEIVED:" + message);
										}
										}catch (EOFException e){
											//System.out.println(message);
											return;
										}
										catch (SocketException e){
											return;
										}
									 
								}
							//}
						}
					}
			}	

				catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			}
		else{

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

				if (!outCommand.get("command").toString().equals("FETCH") && !outCommand.get("command").toString().equals("SUBSCRIBE")) {

					while (true) {
						if (input.available() > 0) {
							try {
								String message = input.readUTF();
								System.out.println(message);

								if (commandLine.debug(args, options)) {
									logger.fine("RECEIVED:" + message);
								}
								socket.close();
								return;
								
							} catch (EOFException e) {

							}
						}
					}
				} else if(outCommand.get("command").toString().equals("FETCH")){
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
				else {
					
					Thread interaction = new Thread(() -> {
						try {
							inputWaiting(output,input,socket,outCommand.get("id").toString());
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					});
					interaction.start();

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
				}

			}

		}

		catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			return;
		}
		}
	}

		
	public static void inputWaiting(DataOutputStream output,DataInputStream input,Socket socket,String id) throws URISyntaxException, IOException{
		Scanner scanner = new Scanner(System.in);
		String inputString = scanner.nextLine();
		String commandString[] = ("-unsubscribe -id " + id ).split(" ");
		JSONObject outCommand = commandLine.parse(commandString, options);
		//String out = outCommand.toString();
		//System.out.println(outCommand);
		output.writeUTF(outCommand.toString());
		String message = "";
		while (true) {
			//System.out.println("ssssssss");
			if (input.available() > 0) {

				try {
					message = input.readUTF();
					System.out.println(message);

					
					socket.close();
					return;
					
				} catch (EOFException e) {
					System.out.println(message);
					socket.close();
				}
			}
		}
	}
	public static void SSLinputWaiting(DataOutputStream output,DataInputStream input, SSLSocket sslsocket,String id) throws URISyntaxException, IOException{
		Scanner scanner = new Scanner(System.in);
		String inputString = scanner.nextLine();
		String commandString[] = ("-unsubscribe -id " + id ).split(" ");
		JSONObject outCommand = commandLine.parse(commandString, options);
		//String out = outCommand.toString();
		//System.out.println(out);
		//System.out.println(outCommand);
		output.writeUTF(outCommand.toString());
		
		String message="";
		try{
			
		while ((message = input.readUTF()) != null){

		
					System.out.println(message);

					
					sslsocket.close();
					return;
					
				
			}
		} catch (EOFException e) {
			System.out.println(message);

			
			sslsocket.close();
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
