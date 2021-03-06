import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore.Entry;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.Map;

import java.util.HashMap;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/* 
                   _ooOoo_ 
                  o8888888o 
                  88" . "88 
                  (| -_- |) 
                  O\  =  /O 
               ____/`---'\____ 
             .'  \\|     |//  `. 
            /  \\|||  :  |||//  \ 
           /  _||||| -:- |||||-  \ 
           |   | \\\  -  /// |   | 
           | \_|  ''\---/''  |   | 
           \  .-\__  `-`  ___/-. / 
         ___`. .'  /--.--\  `. . __ 
      ."" '<  `.___\_<|>_/___.'  >'"". 
     | | :  `- \`.;`\ _ /`;.`/ - ` : | | 
     \  \ `-.   \_ __\ /__ _/   .-` /  / 
======`-.____`-.___\_____/___.-`____.-'====== 
                   `=---=' 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
         ���汣��       ����BUG 
*/
public class Command {

	private String command;
	private String secret;
	private Resource resource;
	private boolean relay;
	private JSONArray serverList;
	public Resource resourceTemplate;
	private boolean fetchSuccess;
	private String id;

	public boolean isFetchSuccess() {
		return fetchSuccess;
	}

	public Command() throws URISyntaxException {
		command = "";
		secret = "";
		resource = new Resource();
		relay = false;
		serverList = new JSONArray();
		resourceTemplate = new Resource();
		fetchSuccess = false;
	}

	public Command(String command) {
		this.command = command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getCommand() {
		return this.command;
	}

	public String getSecret() {
		return this.secret;
	}

	public Resource getResource() {
		return this.resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setRelay(boolean relay) {
		this.relay = relay;
	}
	public void setId(String id) {
		this.id = id;
	}

	public void addServer(String servers) {
		try {
			JSONArray serversArray = new JSONArray();
			String server[] = servers.replaceAll("\"", "").split(",");
			for (String s : server) {
				JSONObject obj = new JSONObject();
				obj.put("hostname", s.split(":")[0]);
				obj.put("port", s.split(":")[1]);
				serversArray.add(obj);
			}
			this.serverList = serversArray;
		} catch (Exception e) {
			System.out.println("invalid server!");
		}

	}

	public JSONObject toJSON() {
		JSONObject JSONcmd = new JSONObject();
		switch (this.command) {
		case "PUBLISH":
			JSONcmd.put("command", "PUBLISH");
			JSONcmd.put("resource", resource.toJSON());
			break;
		case "REMOVE":
			JSONcmd.put("command", "REMOVE");
			JSONcmd.put("resource", resource.toJSON());
			break;
		case "SHARE":
			JSONcmd.put("command", "SHARE");
			JSONcmd.put("secret", this.getSecret());
			JSONcmd.put("resource", resource.toJSON());
			break;
		case "QUERY":
			JSONcmd.put("command", "QUERY");
			JSONcmd.put("relay", this.relay);
			JSONcmd.put("resourceTemplate", resourceTemplate.toJSON());
			break;
		case "FETCH":
			JSONcmd.put("command", "FETCH");
			JSONcmd.put("resourceTemplate", resourceTemplate.toJSON());
			break;
		case "EXCHANGE":
			JSONcmd.put("command", "EXCHANGE");
			JSONcmd.put("serverList", serverList);
			// JSONcmd.put("resourceTemplate",
			// resourceTemplate.toJSON().toJSONString());
			break;
		case "SUBSCRIBE":
			JSONcmd.put("command", "SUBSCRIBE");
			JSONcmd.put("relay", this.relay);
			JSONcmd.put("id", this.id);
			JSONcmd.put("resourceTemplate", resourceTemplate.toJSON());
			break;
		case "UNSUBSCRIBE":
			JSONcmd.put("command", "UNSUBSCRIBE");
			JSONcmd.put("id", this.id);
			break;
		default:
			break;
		}
		return JSONcmd;
	}

	/**
	 * this publish method takes a json as input and write a json file on
	 * server.
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws URISyntaxException
	 **/
	public String parseCommand(String command) throws ParseException, IOException, URISyntaxException {
		JSONObject response = new JSONObject();
		JSONParser parser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) parser.parse(command);
		String result = "";

		if (jsonCommand.isEmpty()) {
			response.put("response", "error");
			response.put("errorMeaasge", "missing or incorrect type for command");
			return response.toJSONString();
		}
		switch (jsonCommand.get("command").toString()) {
		case "PUBLISH":
			result = publish(jsonCommand); // call publish function
			break;
		case "REMOVE":
			result = remove(jsonCommand);
			break;
		case "SHARE":
			result = share(jsonCommand);
			break;
		case "QUERY":
			result = query(jsonCommand);
			break;
		case "FETCH":
			result = fetch(jsonCommand);
			break;
		case "EXCHANGE":
			// System.out.println("exchangeexchange");
			result = exchange(jsonCommand);
			break;
		case "SUBSCRIBE" :
			JSONObject resultJson = new JSONObject();
			resultJson.put("result",subscribe(jsonCommand));
			result = resultJson.toString();
			break;
		default:
			response.put("response", "error");
			response.put("errorMessage", "invalid command");
			result = response.toJSONString();
			break;
		}
		return result;
	}

	public String publish(JSONObject cmd) throws URISyntaxException {
		JSONObject response = new JSONObject();
		String newresStr = cmd.get("resource").toString();
		JSONObject newresJSON = toJSON(newresStr);
		Resource newres = new Resource(newresJSON);
		URI newresUri = newres.getUri();
		// System.out.println(newresUri.isAbsolute());
		if (newres.isEmpty()) {
			response.put("response", "error");
			response.put("errorMessage", "missing resource");
			return response.toJSONString();
		}
		if (newresUri.toString().equals("")) {
			// URI must be present
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
			return response.toJSONString();
		} else if (newresUri.getScheme() != null && newresUri.getScheme().equals("file")) {
			// URI cannot be a file scheme
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
			return response.toJSONString();
		}
		if (!newresUri.isAbsolute() || newres.getOwner().equals("*")) {
			// URI must be absolute
			// Owner field must not be "*"
			response.put("response", "error");
			response.put("errorMessage", "invalid resource");
			return response.toJSONString();
		}
	
		if(!Server.resourceDict.isEmpty()){
			// same channel and URI but different owner is not allowed
			for(String tempkey:Server.resourceDict.keySet()){
				if(newres.isconfict(tempkey)){
					response.put("response", "error");
					response.put("errorMessage", "cannot publish resource");
					return response.toJSONString();
				}
			}
		}
		
		Server.resourceDict.put(newres.getPK(), newres.toJSON());
		response.put("response", "success");
		Iterator iter = Server.subscribeFlag.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Boolean> entry = (Map.Entry<String, Boolean>) iter.next();
			entry.setValue(true);
		}
		Server.newResource = newres;
		return response.toJSONString();
	}

	public JSONObject toJSON(String jsonStr) {
		JSONParser parser = new JSONParser();
		JSONObject json = new JSONObject();
		try {
			Object obj = parser.parse(jsonStr);
			json = (JSONObject) obj;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	private boolean equal(JSONObject res1, JSONObject res2) {
		if (res1.get("owner").toString().equals(res2.get("owner").toString())
				&& res1.get("channel").toString().equals(res2.get("channel").toString())
				&& res1.get("uri").toString().equals(res2.get("uri").toString()))
			return true;
		else
			return false;
	}

	public void writeFile(String filePath, String resource) throws IOException {
		FileWriter fw = new FileWriter(filePath);
		PrintWriter out = new PrintWriter(fw);
		out.write(resource);
		out.println();
		fw.close();
		out.close();
	}

	public ArrayList<String> readFile(String filePath) {
		ArrayList<String> resourcelist = new ArrayList<String>();
		File file = new File(filePath);
		if (file.exists() && file.isDirectory()) {
			String[] filelist = file.list();
			for (String tempfile : filelist) {
				resourcelist.add(tempfile);
			}
			return resourcelist;
		} else
			return resourcelist;
	}

	public String remove(JSONObject cmd) throws URISyntaxException {
		JSONObject response = new JSONObject();
		String rmresStr = cmd.get("resource").toString();
		JSONObject rmresJSON = toJSON(rmresStr);
		Resource rmres = new Resource(rmresJSON);
		if (rmres.isEmpty()) {
			response.put("response", "error");
			response.put("errorMessage", "missing resource");
			return response.toJSONString();
		}
		if(Server.resourceDict.containsKey(rmres.getPK())){
			Server.resourceDict.remove(rmres.getPK());
			response.put("response", "success");
		}else {
			response.put("response", "error");
			response.put("errorMessage", "cannot remove resource");
		}
		return response.toJSONString();

	}


	

	public static boolean intersection(ArrayList<String> l1, ArrayList<String> l2) {
		if (l1.size()==0 && l2.size()==0) {
			return true;
		}
		if (l2.size() == 0) {
			return true;
		}
		l1.retainAll(l2);
		if (l1.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean queryMatch(Resource resource, JSONObject resourceTemplate)
			throws ParseException, URISyntaxException {
		boolean match = false;
		JSONParser parser = new JSONParser();
		// JSONObject resourceTemplate = (JSONObject)
		// parser.parse((String)queryJSON.get("resourceTemplate"));
		Resource rt = new Resource(resourceTemplate);
		boolean channelMatch = resource.getChannel().equals(rt.getChannel()) || rt.getChannel().equals("");
		boolean ownerMatch = resource.getOwner().equals(rt.getOwner()) || rt.getOwner().equals("");
		boolean tagMatch = intersection(resource.getTags(), rt.getTags());
		boolean uriMatch = resource.getUri().equals(rt.getUri()) || rt.getUri().toString().equals("");
		boolean nameAndDesMatch = (((rt.getName().equals("")) || resource.getName().contains(rt.getName()))
				|| ((rt.getDescription().equals("")) || resource.getDescription().contains(rt.getDescription()))
				|| ((rt.getName().equals("")) && rt.getDescription().equals("")));

		
		if (channelMatch && ownerMatch && tagMatch && uriMatch && nameAndDesMatch) {
			match = true;
		}
		return match;
	}

	public String query(JSONObject cmd) throws ParseException, URISyntaxException {
		JSONParser parser = new JSONParser();
		ArrayList<JSONObject> queryResult = new ArrayList<JSONObject>();
		ArrayList<JSONObject> finalResult = new ArrayList<JSONObject>();
		String resultStr = "";
		String filePath = "./Resource";
		// System.out.println(cmd.toJSONString());
		JSONObject resourceObj = (JSONObject) parser.parse(cmd.get("resourceTemplate").toString());
		Resource res = new Resource(resourceObj);
		JSONObject resourceTemplate = res.toJSON();
		// System.out.println(resourceTemplate);
		//ArrayList<Resource> allResource = getAllResource(filePath);
		ArrayList<Resource> allResource = new ArrayList();
		Iterator iter = Server.resourceDict.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) iter.next();
			Resource resourceTemp = new Resource(entry.getValue());
			allResource.add(resourceTemp);
		}
		Resource test = new Resource(resourceTemplate);

		try {
			
			if (resourceTemplate.toJSONString().equals("")) {
				JSONObject error = new JSONObject();
				error.put("response", "error");
				error.put("errorMesage", "missing resourceTemplate");
				finalResult.add(error);
			} 
			else {
				
				
				for (Resource resource : allResource) {
					for (int i = 0; i < resource.getTags().size(); i++) {
						String temp = resource.getTags().get(i).replace("[", "").replace("]", "");
						resource.getTags().set(i, temp);
					}

					if (queryMatch(resource, resourceTemplate)) {
						if (!resource.getOwner().equals("")) {
						resource.setter("owner", "*");
						}
						queryResult.add(resource.toJSON());
					}
				}
				// System.out.println(resourceTemplate.get("tags"));
				JSONObject success = new JSONObject();
				success.put("response", "success");
				finalResult.add(success);
				for (JSONObject q : queryResult) {
					if(q.containsKey("resourceSize")){
						q.remove("resourceSize");
					}
					finalResult.add(q);
				}
				JSONObject resultSize = new JSONObject();
				resultSize.put("resultSize", Integer.toString(queryResult.size()));
				finalResult.add(resultSize);
			}
		} catch (ParseException e) {
			JSONObject error = new JSONObject();
			error.put("response", "error");
			error.put("errorMesage", "invalid resourceTemplate");
			finalResult.add(error);
			e.printStackTrace();
		}
		for (JSONObject result : finalResult) {
			resultStr += result.toJSONString() + "\n";
		}
		return resultStr;

	}

	public String share(JSONObject cmd) throws URISyntaxException {
		JSONObject response = new JSONObject();
		String shresStr = cmd.get("resource").toString();
		String shSecret = cmd.get("secret").toString();
		JSONObject shresJSON = toJSON(shresStr);
		Resource shres = new Resource(shresJSON);
		URI shresUri = shres.getUri();
		if (shres.isEmpty() || shSecret.equals("")) {
			// the resource or secret field was not given or not of the correct
			// type
			response.put("response", "error");
			response.put("errorMessage", "missing resource and/or secret");
			return response.toJSONString();
		}
		if (shresUri.toString().equals("")) {
			// URI must be present
			response.put("response", "error");
			response.put("errorMessage", "invalid resource1");
			return response.toJSONString();
		} else if (shresUri.getScheme() != null && !shresUri.getScheme().equals("file")) {
			// URI must be a file scheme
			response.put("response", "error");
			response.put("errorMessage", "invalid resource2");
			return response.toJSONString();
		}
		if (!shresUri.isAbsolute() || shresUri.getAuthority() != null) {
			// URI must be absolute,non-authoritative
			response.put("response", "error");
			response.put("errorMessage", "invalid resource3");
			return response.toJSONString();
		}
		
		if(!Server.resourceDict.isEmpty()){
			// same channel and URI but different owner is not allowed
			for(String tempkey:Server.resourceDict.keySet()){
				if(shres.isconfict(tempkey)){
					response.put("response", "error");
					response.put("errorMessage", "cannot publish resource");
					return response.toJSONString();
				}
			}
		}
		if (!shSecret.equals(Server.secret)) {
			// secret was incorrect
			response.put("response", "error");
			response.put("errorMessage", "incorrect secret");
			return response.toJSONString();
		} else {
			try {
				File sharefile = new File(shres.getUri());
				
				if (sharefile.exists()) {
					long size = sharefile.length();
					shresJSON.put("resourceSize", size);

					Server.resourceDict.put(shres.getPK(), shres.toJSON());
					response.put("response", "success");
				} else {
					response.put("response", "error");
					response.put("errorMessage", "invalid resource");
				}
			} catch (IllegalArgumentException e) {
				response.put("response", "error");
				response.put("errorMessage", "cannot share resource");
				e.printStackTrace();
			}
		}
		return response.toJSONString();
	}

	public String fetch(JSONObject cmd) throws URISyntaxException {
		this.command = "FETCH";
		JSONObject response = new JSONObject();
		// if(cmd.get("resourceTemplate") == null){
		// response.put("response", "error");
		// response.put("errorMessage", "missing resourceTemplate");
		// return response.toJSONString();
		// }
		String ftresStr = cmd.get("resourceTemplate").toString();
		JSONObject ftresJSON = toJSON(ftresStr);
		Resource ftres = new Resource(ftresJSON);
		this.resourceTemplate = ftres;
		String ftfilename = ftres.getPK().replaceAll(":", "").replaceAll("/", "") + ".json";
		if (ftres.isEmpty()) {
			response.put("response", "error");
			response.put("errorMessage", "missing resourceTemplate");
			return response.toJSONString();
		}
		File file = new File("Resource");
		response.put("response", "success");
		String[] filelist = file.list();
		ArrayList<String> list = new ArrayList();
		for (String tempfile : filelist) {
			list.add(tempfile);
		}
		if (list.contains(ftfilename)) {
			this.fetchSuccess = true;
			String filepath = Server.resourceFolder + ftfilename;
			File ftfile = new File(filepath);

			//
		}

		return response.toJSONString();

	}

	public Resource getResourceTemplate() {
		return resourceTemplate;
	}

	public boolean validIP(String ip) {
		try {
			String[] ipArray = ip.split(".");
			for (String eachIP : ipArray) {
				if (Integer.parseInt(eachIP) < 0 || Integer.parseInt(eachIP) > 255) {
					return false;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public boolean validPort(String port) {
		try {
			int portNumber = Integer.parseInt(port);
			if (portNumber > 0 && portNumber < 65535) {
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return false;

	}

	public String exchange(JSONObject cmd) {
		try {
			// System.out.println(cmd.toJSONString());
			JSONParser parser = new JSONParser();
			// String serverStr = cmd.get("serverList").toString().replace("[",
			// "").replace("]", "");
			// System.out.println(serverStr);
			String serverStr = cmd.get("serverList").toString();
			JSONArray serverArray = (JSONArray) parser.parse(serverStr);
			// System.out.println(serverArray);
			if (serverArray.size() == 0) {
				JSONObject errorMsg = new JSONObject();
				errorMsg.put("response", "error");
				errorMsg.put("errorMessage", "missing of invalid server list");
				return errorMsg.toJSONString();
			}
			for (int i = 0; i < serverArray.size(); i++) {
				String ip = serverArray.get(i).toString().split(",")[0].split(":")[1].replaceAll("\"", "");
				String port = serverArray.get(i).toString().split(",")[1].split(":")[1].replace("}", "")
						.replaceAll("\"", "");
				if (!validIP(ip) || !validPort(port)) {
					JSONObject errorMsg = new JSONObject();
					errorMsg.put("response", "error");
					errorMsg.put("errorMessage", "missing resourceTemplate");
					return errorMsg.toJSONString();
				}

			}
		} catch (ParseException e) {
			JSONObject errorMsg = new JSONObject();
			errorMsg.put("response", "error");
			errorMsg.put("errorMessage", "missing of invalid server list");
			return errorMsg.toJSONString();
		}

		JSONObject successMsg = new JSONObject();
		successMsg.put("response", "success");
		return successMsg.toJSONString();

	}
	
	
	public ArrayList<JSONObject> subscribe(JSONObject cmd) throws ParseException, URISyntaxException {
		JSONParser parser = new JSONParser();
		ArrayList<JSONObject> subscribeResult = new ArrayList<JSONObject>();
		ArrayList<JSONObject> finalResult = new ArrayList<JSONObject>();
		String resultStr = "";
		//String filePath = "./Resource";
		// System.out.println(cmd.toJSONString());
		JSONObject resourceObj = (JSONObject) parser.parse(cmd.get("resourceTemplate").toString());
		this.resourceTemplate = new Resource(resourceObj);
		JSONObject resourceTemplate = this.resourceTemplate.toJSON();
		if(!(cmd.get("id") == null) ){
			this.id = cmd.get("id").toString();
		}
		else {
			JSONObject error = new JSONObject();
			error.put("response", "error");
			error.put("errorMesage", "missing id");
			finalResult.add(error);
		}
		// System.out.println(resourceTemplate);
		//ArrayList<Resource> allResource = getAllResource(filePath);
		ArrayList<Resource> allResource = new ArrayList();
		Iterator iter = Server.resourceDict.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) iter.next();
			Resource resourceTemp = new Resource(entry.getValue());
			allResource.add(resourceTemp);
		}
		Resource test = new Resource(resourceTemplate);

		try {
			
			if (resourceTemplate.toJSONString().equals("")) {
				JSONObject error = new JSONObject();
				error.put("response", "error");
				error.put("errorMesage", "missing resourceTemplate");
				finalResult.add(error);
			} 
			else {
				for (Resource resource : allResource) {
					for (int i = 0; i < resource.getTags().size(); i++) {
						String temp = resource.getTags().get(i).replace("[", "").replace("]", "");
						resource.getTags().set(i, temp);
					}

					if (queryMatch(resource, resourceTemplate)) {
						if (!resource.getOwner().equals("")) {
						resource.setter("owner", "*");
						}
						subscribeResult.add(resource.toJSON());
					}
				}
				// System.out.println(resourceTemplate.get("tags"));
				JSONObject success = new JSONObject();
				success.put("response", "success");
				success.put("id", this.id);
				finalResult.add(success);
				for (JSONObject q : subscribeResult) {
					if(q.containsKey("resourceSize")){
						q.remove("resourceSize");
					}
					finalResult.add(q);
				}
				JSONObject resultSize = new JSONObject();
				resultSize.put("resultSize", Integer.toString(subscribeResult.size()));
				finalResult.add(resultSize);
			}
		} catch (ParseException e) {
			JSONObject error = new JSONObject();
			error.put("response", "error");
			error.put("errorMesage", "invalid resourceTemplate");
			finalResult.add(error);
			e.printStackTrace();
		}
		return finalResult;
		
		//return resultStr;

	}
		
		
		
		

}
