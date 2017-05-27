
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {
	// public static String secret = "rxchfgjvhbjknlm24356784irokfjmnv";
	public static String secret = RandomStringUtils.randomAlphanumeric(32);
	// Declare the port number
	private static int port = 3000;
	private static int sport=3781;
	private static String hostName = "Default Hostname";
	private static int connectionIntervallimit = 1;

	// Identifies the user number connected
	private static int counter = 0;
	private static int exchangeinterval = 10 * 60;
	public static String resourceFolder = "./Resource/";
	public static HashMap<String, JSONObject> resourceDict = new HashMap<String, JSONObject>();
	
	// filename which stores resource information

	public static HashMap<String, Boolean> subscribeFlag = new HashMap<String, Boolean>();
	public static Resource newResource;
	
	static ArrayList<String> serverList = new ArrayList<String>();
	
	static ArrayList<String> secureServerList = new ArrayList<String>();

	public static void setHostName(String hostName) {
		Server.hostName = hostName;
	}

	public static void setConnectionIntervallimit(int connectionIntervallimit) {
		Server.connectionIntervallimit = connectionIntervallimit;
	}

	// Identifies the user number connected
	private static boolean queryComplete = false;
	private static String queryRelayResult = "";
	
	//public static int totalSize = 0;

	public static void main(String[] args) throws URISyntaxException {
		
		CommandLineHandle commandLine = new CommandLineHandle();
		Options options = commandLine.getServerOptions();
		commandLine.parseServerCmd(args, options);
		if (commandLine.debug(args, options)) {
			Logger logger = Logger.getLogger("Server");
			logger.setLevel(Level.ALL);
			logger.info("Starting the EZShare Server");
			logger.info("using secret:" + Server.secret);
			logger.info("using advertised hostname: " + Server.hostName);
			logger.info("bound to port " + Server.port);
			logger.info("started");
		}
		

		
			System.out.println("Server waiting for client connection..");

			Command command = new Command();
			JSONParser parser = new JSONParser();


			Thread interaction = new Thread(() -> timer());
			interaction.start();
			// Wait for connections.
		/*	while (true) {
				Socket client = server.accept();
				//SSLSocket sslclient = (SSLSocket) sslserversocket.accept();
				counter++;
				System.out.println("Client " + counter + ": Applying for connection!");
	

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
			}*/
			Thread securethread=new Thread(()->secureSocket());
			securethread.start();
			Thread unsecurethread=new Thread(()->unsecureSocket());
			unsecurethread.start();
			}
			

		



	private static void serveClient(Socket client) throws URISyntaxException {
		Command command = new Command();
		JSONParser parser = new JSONParser();
		
		try (Socket clientSocket = client) {
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			// Output Stream
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			String inputUTF = input.readUTF();
			JSONObject responseObj = new JSONObject();
			JSONObject inputObj = (JSONObject) parser.parse(inputUTF);
			// String response = "";

			//System.out.println(inputObj);
			if (inputObj.containsKey("relay") && inputObj.get("command").toString().equals("QUERY")
					&& inputObj.get("relay").toString().equals("true")) {

				JSONObject tempObj = inputObj;
				tempObj.put("relay", false);
				JSONObject resourceTemplate = (JSONObject) parser.parse(tempObj.get("resourceTemplate").toString());
				resourceTemplate.put("owner", "");
				resourceTemplate.put("channel", "");
				tempObj.put("resourceTemplate", resourceTemplate);
				/*
				 * while (queryComplete == false){ queryRelayResult =
				 * getAllQuery(serverList, tempObj.toJSONString() ); }
				 */
				// Thread queryRelay = new Thread( () -> {
				try (Socket clientSocketTemp = client) {
					queryRelayResult = getAllQuery(serverList, tempObj.toJSONString());
					// System.out.println(queryRelayResult);
					DataInputStream inputTemp = new DataInputStream(clientSocketTemp.getInputStream());
					// Output Stream
					DataOutputStream outputTemp = new DataOutputStream(clientSocketTemp.getOutputStream());

					// System.out.println(queryRelayResult);

					String[] localQuery = command.parseCommand(inputUTF).split("\n");
					JSONObject localQueryObj = (JSONObject) parser.parse(localQuery[0]);
					if (localQueryObj.get("response").toString().equals("success")) {
						String finalQueryResult = "";
						for (int i = 0; i < localQuery.length - 1; i++) {
							finalQueryResult += localQuery[i] + "\n";
						}
						finalQueryResult += queryRelayResult;
						int size = finalQueryResult.split("\n").length - 1;
						JSONObject resultSize = new JSONObject();
						resultSize.put("resultSize", size);
						finalQueryResult += resultSize.toJSONString() + "\n";
						//System.out.println(finalQueryResult);
						outputTemp.writeUTF("Server: Hi Client " + counter + " !!!");
						outputTemp.writeUTF(finalQueryResult);
						outputTemp.flush();

					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// });
				// queryRelay.start();

			}

			else if (inputObj.containsKey("command") && inputObj.get("command").toString().equals("SUBSCRIBE") )
			{	
				ArrayList<JSONObject> resultJsonList = (ArrayList<JSONObject>) ((JSONObject)parser.parse(command.parseCommand(inputUTF))).get("result");
				String resultStr = "";
				int subscribeResultSize = 0;
				boolean success = false;
				String id = "";
				for (JSONObject result : resultJsonList) {
					if(result.containsKey("resultSize")){
						subscribeResultSize = Integer.parseInt(result.get("resultSize").toString());
					}
					else{
					if(result.containsKey("response")){
						if(result.get("response").toString().equals("success"))
							success = true;
						id = result.get("id").toString();
					}
					resultStr += result.toJSONString() + "\n";
					}
				}
				output.writeUTF(resultStr.substring(0, resultStr.length() - 2));
				
				
				if(success){					
					subscribeFlag.put(id, false);
				if (inputObj.containsKey("relay") && inputObj.get("relay").toString().equals("false")) {
					while(true){
						if(input.available() > 0){
							String inputUTFSubscribe = input.readUTF();
							//System.out.println(inputUTFSubscribe);
							//JSONObject responseObjSubscribe = new JSONObject();
							//JSONObject inputObjSubscribe = (JSONObject) parser.parse(inputUTFSubscribe);
							//if(inputObjSubscribe.get("command").toString().equals("UNSUBSCRIBE")){
								JSONObject responJson = new JSONObject();
								responJson.put("resultSize", subscribeResultSize);
								output.writeUTF(responJson.toString());
								clientSocket.close();
								return;
							//}
						}
					
						if(subscribeFlag.get(id)){
						
							if(Command.queryMatch(newResource,(JSONObject)inputObj.get("resourceTemplate"))){
								String outputStr = newResource.toJSON().toString();	
								output.writeUTF(outputStr);
								subscribeResultSize ++;
							}
							subscribeFlag.put(id, false);
						}
					}
				}
				
				else if (inputObj.containsKey("relay") && inputObj.get("relay").toString().equals("true")) {
					JSONObject inputObjTemp = inputObj;
					inputObjTemp.put("relay", false);
					ArrayList<Integer> totalSize = new ArrayList();
					totalSize.add(0);
					
					Thread t = new Thread(() -> {
						try {

							subscribeRelay(input, output, serverList, inputObjTemp.toJSONString(), totalSize);
						} catch (ParseException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					t.start();
					while (true) {
			
						
						if (input.available() > 0) {
							String inputUTFSubscribe = input.readUTF();
							System.out.println(inputUTFSubscribe);
							JSONObject inputObjSubscribe = (JSONObject) parser.parse(inputUTFSubscribe);
							//if (inputObjSubscribe.containsKey("command") && inputObjSubscribe.get("command").toString().equals("UNSUBSCRIBE")) {
								JSONObject responJson = new JSONObject();
								responJson.put("resultSize", totalSize.get(0));
								output.writeUTF(responJson.toString());
								output.flush();
								clientSocket.close();
								return;
//							}
//							else {
//								output.writeUTF(inputUTFSubscribe);
//								System.out.println(inputUTFSubscribe);
//								totalSize.set(0, totalSize.get(0) + 1);
//							}
						}
						if(subscribeFlag.get(id)){
							
							if(Command.queryMatch(newResource,(JSONObject)inputObj.get("resourceTemplate"))){
								String outputStr = newResource.toString();	
								output.writeUTF(outputStr);
								System.out.println(outputStr);
								totalSize.set(0, totalSize.get(0) + 1);
							}
							subscribeFlag.put(id, false);
						}
					}
					
				}
				
				
				
				}
				
			}
			

			

			else {

				String response = command.parseCommand(inputUTF);
				// System.out.println(input.readUTF());

				System.out.println(response);

				// System.out.println(responseObj.get("response"));

				
				
				
				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////
				
				
				// to be added into secure server socket
				/*
				 * if (inputObj.containsKey("relay") && inputObj.get("command").toString().equals("QUERY")
					&& inputObj.get("relay").toString().equals("true")) {

				JSONObject tempObj = inputObj;
				tempObj.put("relay", false);
				JSONObject resourceTemplate = (JSONObject) parser.parse(tempObj.get("resourceTemplate").toString());
				resourceTemplate.put("owner", "");
				resourceTemplate.put("channel", "");
				tempObj.put("resourceTemplate", resourceTemplate);

				try (SSLSocket clientSocketTemp = client) {
					queryRelayResult = getAllQuery(secureServerList, tempObj.toJSONString());
					// System.out.println(queryRelayResult);
					DataInputStream inputTemp = new DataInputStream(clientSocketTemp.getInputStream());
					// Output Stream
					DataOutputStream outputTemp = new DataOutputStream(clientSocketTemp.getOutputStream());

					// System.out.println(queryRelayResult);

					String[] localQuery = command.parseCommand(inputUTF).split("\n");
					JSONObject localQueryObj = (JSONObject) parser.parse(localQuery[0]);
					if (localQueryObj.get("response").toString().equals("success")) {
						String finalQueryResult = "";
						for (int i = 0; i < localQuery.length - 1; i++) {
							finalQueryResult += localQuery[i] + "\n";
						}
						finalQueryResult += queryRelayResult;
						int size = finalQueryResult.split("\n").length - 1;
						JSONObject resultSize = new JSONObject();
						resultSize.put("resultSize", size);
						finalQueryResult += resultSize.toJSONString() + "\n";
						//System.out.println(finalQueryResult);
						outputTemp.writeUTF("Server: Hi Client " + counter + " !!!");
						outputTemp.writeUTF(finalQueryResult);
						outputTemp.flush();

					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else {

				String response = command.parseCommand(inputUTF);
				System.out.println(response);
	

				 */
				
				
			////////////////////////////////////////////////////////////////////////////////////////	
				
				
				
				if (!inputObj.isEmpty()) {
					if (!inputObj.get("command").toString().equals("QUERY")) {
						//System.out.println(response);
						String resStatus = response.split("\n")[0];
						responseObj = (JSONObject) parser.parse(resStatus);
					}
					// System.out.println(inputObj.get("command"));
					//System.out.println(inputObj.toJSONString());
					//System.out.println(responseObj);
					if (inputObj.get("command").toString().equals("EXCHANGE")
							&& responseObj.get("response").toString().equals("success")) {
						
						String serverListStr = inputObj.get("serverList").toString();
						JSONArray serverArray = (JSONArray) parser.parse(serverListStr);
						for (int i = 0; i < serverArray.size(); i++) {
							boolean dup = false;
							String ip = serverArray.get(i).toString().split(",")[0].split(":")[1].replaceAll("\"", "");
							String port = serverArray.get(i).toString().split(",")[1].split(":")[1].replace("}", "")
									.replaceAll("\"", "");
							String serverRecord = ip + ":" + port;
							for (int j = 0; j < serverList.size(); j++) {
								if (serverList.get(j).equals(serverRecord)) {
									dup = true;
								}
							}
							if (dup == false && !serverRecord.split(":")[0]
									.equals(InetAddress.getLocalHost().toString().split("/")[1])) {
								serverList.add(serverRecord);
							}

						}
						//System.out.println(serverList);
		
					}
					/////////////////////////////////////////////////////////////////////////////
					// to be added into secure server socket
					/*
						if (inputObj.get("command").toString().equals("exchange")
							&& responseObj.get("response").toString().equals("success")) {
						String serverListStr = inputObj.get("serverList").toString();
						JSONArray serverArray = (JSONArray) parser.parse(serverListStr);
						for (int i = 0; i < serverArray.size(); i++) {
							boolean dup = false;
							String ip = serverArray.get(i).toString().split(",")[0].split(":")[1].replaceAll("\"", "");
							String port = serverArray.get(i).toString().split(",")[1].split(":")[1].replace("}", "")
									.replaceAll("\"", "");
							String serverRecord = ip + ":" + port;
							for (int j = 0; j < serverList.size(); j++) {
								if (serverList.get(j).equals(serverRecord)) {
									dup = true;
								}
							}
							if (dup == false && !serverRecord.split(":")[0]
									.equals(InetAddress.getLocalHost().toString().split("/")[1])) {
								secureServerList.add(serverRecord);

							}

						}
						
					}
					*/
					/////////////////////////////////////////////////////////////////////////
					
				}
				
				
				
				
				
				
				
				
				
				
				
				

				output.writeUTF(response);
				if (command.getCommand().equals("FETCH")) {
					responseObj = (JSONObject) parser.parse(response);
					if (responseObj.get("response").equals("success") && command.isFetchSuccess()) {
						String ftfilename = command.getResourceTemplate().getPK().replaceAll(":", "").replaceAll("/",
								"") + ".json";
						String filePath = Server.resourceFolder + ftfilename;
						File resfile = new File(filePath);
						BufferedReader reader = null;
						reader = new BufferedReader(new FileReader(resfile));
						String tempString = reader.readLine();
						output.writeUTF(tempString);
						System.out.println(tempString);
						JSONObject jsonResourceFile = (JSONObject) parser.parse(tempString);
						Resource fetres = new Resource(jsonResourceFile);

						int fileSize = Integer.parseInt(jsonResourceFile.get("resourceSize").toString());
						URI resourceURI = fetres.getUri();

						// File f = new
						// File(command.resourceTemplate.uri.toString());
						File f = new File(resourceURI);
						if (f.exists()) {
							RandomAccessFile byteFile = new RandomAccessFile(f, "r");
							//System.out.println(f.length());

							byte[] sendingBuffer = new byte[1024 * 1024];
							int num;
							// While there are still bytes to send..
							while ((num = byteFile.read(sendingBuffer)) > 0) {
								//System.out.println(num);
								output.write(Arrays.copyOf(sendingBuffer, num));
							}
							byteFile.close();
							JSONObject resultSize = new JSONObject();
							resultSize.put("resultSize", 1);
							output.writeUTF(resultSize.toJSONString());
						}

					} else {
						JSONObject resultSize = new JSONObject();
						resultSize.put("resultSize", 0);
						output.writeUTF(resultSize.toJSONString());
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {

			e.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}

	public static String Client(String ip, int port, String command) {
		try (Socket socket = new Socket(ip, port)) {
			// Output and Input Stream
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.writeUTF(command);
			output.flush();
			while (true) {
				if (input.available() > 0) {

					String message = input.readUTF();
					// System.out.println(message);
					return message;
				}
			}
		} catch (ConnectException e) {
			System.out.println("Invild Host " + ip);
			return "error";
		} catch (UnknownHostException e) {
			System.out.println("invalid host " + ip);
			return "error";

		} catch (IOException e) {
			e.printStackTrace();
			return "error";

		}

	}
	
	public static String SSLClient(String ip, int port, String command) {
		
		System.setProperty("javax.net.ssl.trustStore", "clientKeystore/root");
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			// Output and Input Stream
			SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);
			DataInputStream input = new DataInputStream(sslsocket.getInputStream());
			DataOutputStream output=new DataOutputStream(sslsocket.getOutputStream());
			output.writeUTF(command);
			output.flush();
			while (true) {
				

					String message = input.readUTF();
					// System.out.println(message);
					return message;
				
			}
		} catch (ConnectException e) {
			System.out.println("Invild Host " + ip);
			return "error";
		} catch (UnknownHostException e) {
			System.out.println("invalid host " + ip);
			return "error";

		} catch (IOException e) {
			e.printStackTrace();
			return "error";

		}

	}
	
	public static void subscribeRelay(DataInputStream input, DataOutputStream output, ArrayList<String> serverList, String command, ArrayList<Integer> totalSize) throws ParseException, IOException {

		for (String server : serverList) {			
			String ip = server.split(":")[0];
			int port = Integer.parseInt(server.split(":")[1]);
			Thread t = new Thread(() -> {
				try {
					subscribeOne(input, output, ip, port, command, totalSize);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			t.start();

	}
	}
	public static void SSLsubscribeRelay(DataInputStream input, DataOutputStream output, ArrayList<String> serverList, String command, ArrayList<Integer> totalSize) throws ParseException, IOException {

		for (String server : secureServerList) {
			///
			String ip = server.split(":")[0];
			int port = Integer.parseInt(server.split(":")[1]);
			Thread t = new Thread(() -> {
				try {
					subscribeOne(input, output, ip, port, command ,totalSize);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			t.start();

	}
	}
	
	public static void subscribeOne(DataInputStream input, DataOutputStream output, String ip, int port, String command, ArrayList<Integer> totalSize) throws ParseException {

		try (Socket socket = new Socket(ip, port)) {
			// Output and Input Stream
			DataInputStream subinput = new DataInputStream(socket.getInputStream());
			DataOutputStream suboutput = new DataOutputStream(socket.getOutputStream());
			JSONParser parser = new JSONParser();
			suboutput.writeUTF(command);
		
			suboutput.flush();
			while (true) {
				if (subinput.available() > 0) {
					JSONObject messageObj = new JSONObject();
					String message = subinput.readUTF();
					if (message.split("\n").length == 1){
						messageObj = (JSONObject) parser.parse(message);
						if (!messageObj.containsKey("response")) {
							output.writeUTF(message);
							totalSize.set(0, totalSize.get(0) + 1);
							
						}
					}
					else {
						JSONArray arrayTemp = new JSONArray();
						String temp = "";
						for (int i = 1; i < message.split("\n").length; i++) {
							arrayTemp.add(message.split("\n")[i]);
							totalSize.set(0, totalSize.get(0) + 1);
							temp += message.split("\n")[i];
						}
						
						output.writeUTF(temp);
						//totalSize.set(0, totalSize.get(0) + arrayTemp.size() - 1);
						///
						//messageObj = (JSONObject) parser.parse(message.split("\n")[0]);
					}
	
	
				}
				if (input.available() > 0) {
					String Input = input.readUTF();
					JSONObject InputObj = (JSONObject) parser.parse(Input);
//					if (InputObj.containsKey("command") && InputObj.get("command").toString().equals("UNSUBSCRIBE")) {
//				;
						socket.close();
						//System.out.println("1111");
					//}
				}
			}
		} catch (ConnectException e) {
			System.out.println("Invild Host " + ip);

		} catch (UnknownHostException e) {
			System.out.println("invalid host " + ip);


		} catch (IOException e) {
			e.printStackTrace();
			

		}

	}
	
	public static void subscribeOneSSL(DataInputStream input, DataOutputStream output, String ip, int port, String command) throws ParseException {
		try (Socket socket = new Socket(ip, port)) {
			// Output and Input Stream
			DataInputStream subinput = new DataInputStream(socket.getInputStream());
			DataOutputStream suboutput = new DataOutputStream(socket.getOutputStream());
			suboutput.writeUTF(command);
			suboutput.flush();
			while (true) {
				//if (subinput.available() > 0) {
				String message = subinput.readUTF();
					JSONParser parser = new JSONParser();
					JSONObject messageObj = (JSONObject) parser.parse(message);
					if (!messageObj.containsKey("response")) {
						output.writeUTF(message);
					}
					// System.out.println(message);
	
				//}
			}
		} catch (ConnectException e) {
			System.out.println("Invild Host " + ip);

		} catch (UnknownHostException e) {
			System.out.println("invalid host " + ip);


		} catch (IOException e) {
			e.printStackTrace();
			

		}

	}

	public static String getAllQuery(ArrayList<String> serverList, String command) throws ParseException {
		// boolean queryComplete = false;
		String queryResult = "";
		JSONParser parser = new JSONParser();
		for (String server : serverList) {
			String ip = server.split(":")[0];
			int port = Integer.parseInt(server.split(":")[1]);
			String[] queryTemp = Client(ip, port, command).split("\n");
			JSONObject queryRes = (JSONObject) parser.parse(queryTemp[0]);
			if (queryRes.get("response").equals("success")) {
				for (int i = 1; i < queryTemp.length - 1; i++) {
					queryResult += queryTemp[i] + "\n";
				}
			}

		}
		queryComplete = true;
		return queryResult;

	}
	public static String getAllQuerySSL(ArrayList<String> serverList, String command) throws ParseException {
		// boolean queryComplete = false;
		String queryResult = "";
		JSONParser parser = new JSONParser();
		for (String server : serverList) {
			String ip = server.split(":")[0];
			int port = Integer.parseInt(server.split(":")[1]);
			String[] queryTemp = SSLClient(ip, port, command).split("\n");
			JSONObject queryRes = (JSONObject) parser.parse(queryTemp[0]);
			if (queryRes.get("response").equals("success")) {
				for (int i = 1; i < queryTemp.length - 1; i++) {
					queryResult += queryTemp[i] + "\n";
				}
			}

		}
		queryComplete = true;
		return queryResult;

	}
	


	public static void timer() {
		Timer myTimer = new Timer();
		myTimer.schedule(new Timertest(), 1000 * exchangeinterval, 1000 * exchangeinterval);
	}

	public static void setExchangeinterval(int exchangeinterval) {
		Server.exchangeinterval = exchangeinterval;
	}

	public static void setSecret(String secret) {
		Server.secret = secret;
	}

	public static void setPort(int port) {
		Server.port = port;
	}
	
	public static void setSport(int sport){
		Server.sport=sport;
	}


	static class Timertest extends TimerTask {

		public void run() {
			System.out.println("Start automatical exchange...");
			/////
			if (serverList.isEmpty() && secureServerList.isEmpty()) {
				System.out.println("empty serverlist!");
				return;
			}
			if(!serverList.isEmpty()){
			Random rdm = new Random();
			int rdmint = (int) (serverList.size() * rdm.nextDouble());
			String ip = serverList.get(rdmint).split(":")[0];
			int port = Integer.parseInt(serverList.get(rdmint).split(":")[1]);
			JSONObject command = new JSONObject();
			JSONArray serversArray = new JSONArray();
			for (int i = 0; i < serverList.size(); i++) {
				JSONObject obj = new JSONObject();
				obj.put("hostname", serverList.get(i).split(":")[0]);
				obj.put("port", serverList.get(i).split(":")[1]);
				serversArray.add(obj);
			}
			command.put("command", "EXCHANGE");
			command.put("serverList", serversArray.toJSONString());
			String outCommand = command.toString();
			try (Socket socket = new Socket(ip, port)) {
				// Output and Input Stream
				DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				output.writeUTF(outCommand);
				output.flush();

				if (input.available() > 0) {

					String message = input.readUTF();
					System.out.println("Server " + serverList.get(rdmint).split(":")[0]);
					System.out.println("port " + serverList.get(rdmint).split(":")[1]);
					System.out.println("incoming:");
					System.out.println(message);
				}

			} catch (ConnectException e) {
				System.out.println("Invild Host" + ip);
				serverList.remove(rdmint);
				// hahhahahha
			} catch (UnknownHostException e) {
				System.out.println("Invild Host" + ip);
				serverList.remove(rdmint);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		if(!secureServerList.isEmpty()){
			Random rdm = new Random();
			int rdmint = (int) (secureServerList.size() * rdm.nextDouble());
			String ip = secureServerList.get(rdmint).split(":")[0];
			int port = Integer.parseInt(secureServerList.get(rdmint).split(":")[1]);
			JSONObject command = new JSONObject();
			JSONArray serversArray = new JSONArray();
			for (int i = 0; i < secureServerList.size(); i++) {
				JSONObject obj = new JSONObject();
				obj.put("hostname", secureServerList.get(i).split(":")[0]);
				obj.put("port", secureServerList.get(i).split(":")[1]);
				serversArray.add(obj);
			}
			command.put("command", "EXCHANGE");
			command.put("serverList", serversArray.toJSONString());
			String outCommand = command.toString();
				// Output and Input Stream
//				DataInputStream input = new DataInputStream(socket.getInputStream());
//				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
//
//				output.writeUTF(outCommand);
//				output.flush();
//
//				if (input.available() > 0) {
//
//					String message = input.readUTF();
//					System.out.println("Server " + secureServerList.get(rdmint).split(":")[0]);
//					System.out.println("port " + secureServerList.get(rdmint).split(":")[1]);
//					System.out.println("incoming:");
//					System.out.println(message);
//				}
				System.setProperty("javax.net.ssl.trustStore", "clientKeystore/root");
				//System.setProperty("javax.net.debug","all");
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				try {
					SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);
					DataInputStream inputstream = new DataInputStream(sslsocket.getInputStream());
					DataOutputStream output=new DataOutputStream(sslsocket.getOutputStream());
					output.writeUTF(outCommand);

			} catch (ConnectException e) {
				System.out.println("Invild Host" + ip);
				secureServerList.remove(rdmint);
				// hahhahahha
			} catch (UnknownHostException e) {
				System.out.println("Invild Host" + ip);
				secureServerList.remove(rdmint);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
			
	}
	}
	private static void SSLserveClient(Socket client) throws URISyntaxException {
		Command command = new Command();
		JSONParser parser = new JSONParser();
		
		try (Socket clientSocket = client) {
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			// Output Stream
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			String inputUTF = input.readUTF();
			JSONObject responseObj = new JSONObject();
			JSONObject inputObj = (JSONObject) parser.parse(inputUTF);
			// String response = "";

			//System.out.println(inputObj);
			if (inputObj.containsKey("relay") && inputObj.get("command").toString().equals("QUERY")
					&& inputObj.get("relay").toString().equals("true")) {

				JSONObject tempObj = inputObj;
				tempObj.put("relay", false);
				JSONObject resourceTemplate = (JSONObject) parser.parse(tempObj.get("resourceTemplate").toString());
				resourceTemplate.put("owner", "");
				resourceTemplate.put("channel", "");
				tempObj.put("resourceTemplate", resourceTemplate);
				/*
				 * while (queryComplete == false){ queryRelayResult =
				 * getAllQuery(serverList, tempObj.toJSONString() ); }
				 */
				// Thread queryRelay = new Thread( () -> {
				try (Socket clientSocketTemp = client) {
					queryRelayResult = getAllQuerySSL(secureServerList, tempObj.toJSONString());
					// System.out.println(queryRelayResult);
					DataInputStream inputTemp = new DataInputStream(clientSocketTemp.getInputStream());
					// Output Stream
					DataOutputStream outputTemp = new DataOutputStream(clientSocketTemp.getOutputStream());

					// System.out.println(queryRelayResult);

					String[] localQuery = command.parseCommand(inputUTF).split("\n");
					JSONObject localQueryObj = (JSONObject) parser.parse(localQuery[0]);
					if (localQueryObj.get("response").toString().equals("success")) {
						String finalQueryResult = "";
						for (int i = 0; i < localQuery.length - 1; i++) {
							finalQueryResult += localQuery[i] + "\n";
						}
						finalQueryResult += queryRelayResult;
						int size = finalQueryResult.split("\n").length - 1;
						JSONObject resultSize = new JSONObject();
						resultSize.put("resultSize", size);
						finalQueryResult += resultSize.toJSONString() + "\n";
						//System.out.println(finalQueryResult);
						//outputTemp.writeUTF("Server: Hi Client " + counter + " !!!");
						outputTemp.writeUTF(finalQueryResult);
						//outputTemp.writeUTF("finished");
						System.out.println("finished");
						outputTemp.flush();

					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// });
				// queryRelay.start();

			}

			else if (inputObj.containsKey("command") && inputObj.get("command").toString().equals("SUBSCRIBE") )
			{	
				ArrayList<JSONObject> resultJsonList = (ArrayList<JSONObject>) ((JSONObject)parser.parse(command.parseCommand(inputUTF))).get("result");
				String resultStr = "";
				int subscribeResultSize[] = {0};
				boolean success = false;
				String id = "";
				for (JSONObject result : resultJsonList) {
					if(result.containsKey("resultSize")){
						subscribeResultSize[0] = Integer.parseInt(result.get("resultSize").toString());
					}
					else{
					if(result.containsKey("response")){
						if(result.get("response").toString().equals("success"))
							success = true;
						id = result.get("id").toString();
					}
					resultStr += result.toJSONString() + "\n";
					}
				}
				//System.out.println(resultStr);
				output.writeUTF(resultStr.substring(0, resultStr.length() - 2));
				//System.out.println(resultStr);
				
				
				if(success){					
					subscribeFlag.put(id, false);
				if (inputObj.containsKey("relay") && inputObj.get("relay").toString().equals("false")) {
					Thread inputwaiting = new Thread(() -> inputWaitingSSL(input, output, subscribeResultSize,clientSocket));
					inputwaiting.start();
					while(true){
						//if(input.available() > 0){
						
//						try{
//							if(input.!= -1){
//							//input.readUTF();
//							//System.out.println(inputUTFSubscribe);
//							//JSONObject responseObjSubscribe = new JSONObject();
//							//JSONObject inputObjSubscribe = (JSONObject) parser.parse(inputUTFSubscribe);
//							//if(inputObjSubscribe.get("command").toString().equals("UNSUBSCRIBE")){
//								JSONObject responJson = new JSONObject();
//								responJson.put("resultSize", subscribeResultSize);
//								output.writeUTF(responJson.toString());
//								clientSocket.close();
//								return;
//							//}}
//						//}}
//							}
//						}catch(EOFException e){
//							
//						}
							
						//System.out.println("dtcvghbjjncfcfvbxcvb");
						if(subscribeFlag.get(id)){
							
							if(Command.queryMatch(newResource,(JSONObject)inputObj.get("resourceTemplate"))){
								String outputStr = newResource.toJSON().toString();	
								output.writeUTF(outputStr);
								subscribeResultSize[0] ++;
							}
							subscribeFlag.put(id, false);
						}
					}
				}
				
				else if (inputObj.containsKey("relay") && inputObj.get("relay").toString().equals("true")) {
					JSONObject inputObjTemp = inputObj;
					inputObjTemp.put("relay", false);
					ArrayList<Integer> totalSize = new ArrayList();
					totalSize.add(subscribeResultSize[0]);
					//
					Thread t = new Thread(() -> {
						try {

							subscribeRelay(input, output,  serverList, inputObj.toJSONString(), totalSize);
						} catch (ParseException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					t.start();
					
					Thread inputwaiting = new Thread(() -> inputWaitingSSL(input, output,totalSize ,clientSocket));
					inputwaiting.start();
					while (true) {
			
						
//						//if (input.available() > 0) {
//						try {
////							String inputUTFSubscribe = input.readUTF();
////							System.out.println(inputUTFSubscribe);
////							JSONObject inputObjSubscribe = (JSONObject) parser.parse(inputUTFSubscribe);
////							//if (inputObjSubscribe.containsKey("command") && inputObjSubscribe.get("command").toString().equals("UNSUBSCRIBE")) {
////								JSONObject responJson = new JSONObject();
////								responJson.put("resultSize", subscribeResultSize[0]);
////								output.writeUTF(responJson.toString());
////								clientSocket.close();
////								return;
//						}catch (EOFException e){
//							
//						}
//							}
//							else {
//								output.writeUTF(inputUTFSubscribe);
//								subscribeResultSize ++;
//							}
//						//}
					
						if(subscribeFlag.get(id)){
							
							if(Command.queryMatch(newResource,(JSONObject)inputObj.get("resourceTemplate"))){
								String outputStr = newResource.toJSON().toString();	
								output.writeUTF(outputStr);
								subscribeResultSize[0] ++;
							}
							subscribeFlag.put(id, false);
						}
					}
					
				}
				
				
				
				}
				
			}
			

			

			else {

				String response = command.parseCommand(inputUTF);
				// System.out.println(input.readUTF());

				System.out.println(response);
//responseObj=(JSONObject)parser.parse(response);
				// System.out.println(responseObj.get("response"));

				
				
				
				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////
				
				
				// to be added into secure server socket
				/*
				 * if (inputObj.containsKey("relay") && inputObj.get("command").toString().equals("QUERY")
					&& inputObj.get("relay").toString().equals("true")) {

				JSONObject tempObj = inputObj;
				tempObj.put("relay", false);
				JSONObject resourceTemplate = (JSONObject) parser.parse(tempObj.get("resourceTemplate").toString());
				resourceTemplate.put("owner", "");
				resourceTemplate.put("channel", "");
				tempObj.put("resourceTemplate", resourceTemplate);

				try (SSLSocket clientSocketTemp = client) {
					queryRelayResult = getAllQuery(secureServerList, tempObj.toJSONString());
					// System.out.println(queryRelayResult);
					DataInputStream inputTemp = new DataInputStream(clientSocketTemp.getInputStream());
					// Output Stream
					DataOutputStream outputTemp = new DataOutputStream(clientSocketTemp.getOutputStream());

					// System.out.println(queryRelayResult);

					String[] localQuery = command.parseCommand(inputUTF).split("\n");
					JSONObject localQueryObj = (JSONObject) parser.parse(localQuery[0]);
					if (localQueryObj.get("response").toString().equals("success")) {
						String finalQueryResult = "";
						for (int i = 0; i < localQuery.length - 1; i++) {
							finalQueryResult += localQuery[i] + "\n";
						}
						finalQueryResult += queryRelayResult;
						int size = finalQueryResult.split("\n").length - 1;
						JSONObject resultSize = new JSONObject();
						resultSize.put("resultSize", size);
						finalQueryResult += resultSize.toJSONString() + "\n";
						//System.out.println(finalQueryResult);
						outputTemp.writeUTF("Server: Hi Client " + counter + " !!!");
						outputTemp.writeUTF(finalQueryResult);
						outputTemp.flush();

					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else {

				String response = command.parseCommand(inputUTF);
				System.out.println(response);
	

				 */
				
				
			////////////////////////////////////////////////////////////////////////////////////////	
				
				
				
				if (!inputObj.isEmpty()) {
					if (!inputObj.get("command").toString().equals("QUERY")) {
						//System.out.println(response);
						String resStatus = response.split("\n")[0];
						responseObj = (JSONObject) parser.parse(resStatus);
					}
					// System.out.println(inputObj.get("command"));
					if (inputObj.get("command").toString().equals("EXCHANGE")
							&& responseObj.get("response").toString().equals("success")) {
						String serverListStr = inputObj.get("serverList").toString();
						JSONArray serverArray = (JSONArray) parser.parse(serverListStr);
						for (int i = 0; i < serverArray.size(); i++) {
							boolean dup = false;
							String ip = serverArray.get(i).toString().split(",")[0].split(":")[1].replaceAll("\"", "");
							String port = serverArray.get(i).toString().split(",")[1].split(":")[1].replace("}", "")
									.replaceAll("\"", "");
							String serverRecord = ip + ":" + port;
							for (int j = 0; j < secureServerList.size(); j++) {
								if (secureServerList.get(j).equals(serverRecord)) {
									dup = true;
								}
							}
							if (dup == false && !serverRecord.split(":")[0]
									.equals(InetAddress.getLocalHost().toString().split("/")[1])) {
								secureServerList.add(serverRecord);
							}

						}
		
					}
					/////////////////////////////////////////////////////////////////////////////
					// to be added into secure server socket
					/*
						if (inputObj.get("command").toString().equals("exchange")
							&& responseObj.get("response").toString().equals("success")) {
						String serverListStr = inputObj.get("serverList").toString();
						JSONArray serverArray = (JSONArray) parser.parse(serverListStr);
						for (int i = 0; i < serverArray.size(); i++) {
							boolean dup = false;
							String ip = serverArray.get(i).toString().split(",")[0].split(":")[1].replaceAll("\"", "");
							String port = serverArray.get(i).toString().split(",")[1].split(":")[1].replace("}", "")
									.replaceAll("\"", "");
							String serverRecord = ip + ":" + port;
							for (int j = 0; j < serverList.size(); j++) {
								if (serverList.get(j).equals(serverRecord)) {
									dup = true;
								}
							}
							if (dup == false && !serverRecord.split(":")[0]
									.equals(InetAddress.getLocalHost().toString().split("/")[1])) {
								secureServerList.add(serverRecord);

							}

						}
						
					}
					*/
					/////////////////////////////////////////////////////////////////////////
					
				}
				
				
				
				
				
				
				
				
				
				
				
				

				output.writeUTF(response);
				if (command.getCommand().equals("FETCH")) {
					responseObj = (JSONObject) parser.parse(response);
					if (responseObj.get("response").equals("success") && command.isFetchSuccess()) {
						String ftfilename = command.getResourceTemplate().getPK().replaceAll(":", "").replaceAll("/",
								"") + ".json";
						String filePath = Server.resourceFolder + ftfilename;
						File resfile = new File(filePath);
						BufferedReader reader = null;
						reader = new BufferedReader(new FileReader(resfile));
						String tempString = reader.readLine();
						output.writeUTF(tempString);
						System.out.println(tempString);
						JSONObject jsonResourceFile = (JSONObject) parser.parse(tempString);
						Resource fetres = new Resource(jsonResourceFile);

						int fileSize = Integer.parseInt(jsonResourceFile.get("resourceSize").toString());
						URI resourceURI = fetres.getUri();

						// File f = new
						// File(command.resourceTemplate.uri.toString());
						File f = new File(resourceURI);
						if (f.exists()) {
							RandomAccessFile byteFile = new RandomAccessFile(f, "r");
							//System.out.println(f.length());

							byte[] sendingBuffer = new byte[1024 * 1024];
							int num;
							// While there are still bytes to send..
							while ((num = byteFile.read(sendingBuffer)) > 0) {
								//System.out.println(num);
								output.write(Arrays.copyOf(sendingBuffer, num));
							}
							byteFile.close();
							JSONObject resultSize = new JSONObject();
							resultSize.put("resultSize", 1);
							output.writeUTF(resultSize.toJSONString());
						}

					} else {
						JSONObject resultSize = new JSONObject();
						resultSize.put("resultSize", 0);
						output.writeUTF(resultSize.toJSONString());
					}
				}
			}
			/*if(!inputObj.get("command").toString().equals("SUBSCRIBE")){
				clientSocket.close();
				}*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {

			e.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}

	private static void inputWaitingSSL(DataInputStream input, DataOutputStream output, int[] subscribeResultSize,
			Socket clientSocket) {
		
		
		try {
			input.readUTF();
			JSONObject responJson = new JSONObject();
			
			responJson.put("resultSize", subscribeResultSize[0]);
			output.writeUTF(responJson.toString());
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	private static void inputWaitingSSL(DataInputStream input, DataOutputStream output, ArrayList<Integer> subscribeResultSize,
			Socket clientSocket) {
		
		
		try {
			input.readUTF();
			JSONObject responJson = new JSONObject();
			
			responJson.put("resultSize", subscribeResultSize.get(0));
			output.writeUTF(responJson.toString());
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}

	private static void secureSocket(){
		System.setProperty("javax.net.ssl.keyStore","serverKeystore/serverstore.jks");
		System.setProperty("javax.net.ssl.trustStore", "serverKeyStore/root");
		System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
		//System.setProperty("javax.net.debug","all");
		
		try {
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(sport);
			while(true){
				SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
				Thread securethread=new Thread(()->{
					try {
						SSLserveClient(sslsocket);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				});
				securethread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void unsecureSocket(){
		
        try {
        	ServerSocketFactory factory = ServerSocketFactory.getDefault();
			ServerSocket server = factory.createServerSocket(port);
			while(true){
				Socket client = server.accept();
				Thread t= new Thread(()->{
					try {
						serveClient(client);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				});
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
