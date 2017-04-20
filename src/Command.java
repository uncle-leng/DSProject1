import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.*;
import java.util.*;
import java.lang.*;

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
	public String command;
	public String secret;
	public Resource resource;
	public boolean relay;
	public JSONArray serverList;
	//public JSONArray resourceTemplate;
	public JSONObject resourceTemplate;
	
	public Command(){
		command="";
		secret="";
		resource=new Resource();
		relay=false;
		serverList=null;
		resourceTemplate=new Resource().toJSON();
	}
	
	public Command(String command){
		this.command=command;
	}
	
	public void setCommand(String command){
		this.command=command;
	}
	public void setSecret(String secret){
		this.secret=secret;
	}
	
	public void setResource(Resource resource){
		this.resource=resource;
	}
	
	public void setRelay(boolean relay){
		this.relay=relay;
	}
	
	public void addServer(String server){
		this.serverList.add(server);
	}
	
	public void setResourceTemplate(JSONObject resourceTemplate){
		this.resourceTemplate=resourceTemplate;
	}
	
	public JSONObject toJSON(){
		JSONObject JSONcmd=new JSONObject();
		switch(this.command){
		case "publish":
			JSONcmd.put("command", "publish");
			JSONcmd.put("resource", resource.toJSON().toJSONString());
			break;
		case "remove":
			JSONcmd.put("command", "remove");
			JSONcmd.put("resource", resource.toJSON().toJSONString());
			break;
		case "share":
			JSONcmd.put("command", "share");
			JSONcmd.put("resource", resource.toJSON().toJSONString());
			break;
		case "query":
			JSONcmd.put("command", "query");
			JSONcmd.put("resourceTemplate", resource);
			break;
		case "fetch":
			JSONcmd.put("command", "fetch");
			JSONcmd.put("resource", resource.toJSON().toJSONString());
			break;
		default:break;
		}
		return JSONcmd;
	}
	
	/**
	 * this publish method takes a json as input
	 * and write a json file on server.
	 * @throws ParseException 
	 * @throws IOException 
	 * **/
	public String parseCommand(String command) throws ParseException, IOException {
		JSONObject response=new JSONObject();
		JSONParser parser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) parser.parse(command);
		String result="";
		if(jsonCommand.isEmpty()){
			response.put("response", "error");
			response.put("errorMeaasge", "missing or incorrect type for command");
			return response.toJSONString();
		}
		switch(jsonCommand.get("command").toString()) {
		case "publish":
			result=publish(jsonCommand); // call publish function
			break;
		case "remove":
			remove(jsonCommand);
			break;
		case "share":
			share(jsonCommand);
			break;
		case "query":
			query(jsonCommand);
			break;
		case "fetch":
			fetch(jsonCommand);
			break;
		case "exchange":
			exchange(jsonCommand);
			break;
		default:
			response.put("response", "error");
			response.put("errorMessage", "invalid command");
			result=response.toJSONString();
			break;
		}
		return result;
	}

	/*public String publish(JSONObject cmd){
		JSONObject response=new JSONObject();
		String newres=cmd.get("resource").toString();
		JSONObject obj1=toJSON(newres);
		Resource rs=new Resource(obj1);
	//System.out.println(rs.toString());	
		if(rs.isEmpty()){
			response.put("response", "error");
			response.put("errorMessage", "missing resource");
			return response.toJSONString();
		}
		try{
			ArrayList<String> resList=readFile(Server.resourceList);
			if(resList==null){
			writeFile(Server.resourceList,newres);
			}
			else
			{
				//for(String oldres:resList){
				for(int i=0;i<resList.size();i++){	
				//JSONObject obj2=toJSON(oldres);
				JSONObject obj2=toJSON(resList.get(i));	
				if(equal(obj1,obj2)){
					resList.remove(resList.get(i));
					//resList.remove(oldres);
						resList.add(newres);
					}
					else{
						resList.add(newres);
					}
				}
				writeFile(Server.resourceList,newres);
				response.put("response", "success");
			}
		}
	catch (IOException e) {
			response.put("response", "error");
			response.put("errorMessage", "cannot publish resource");
		}
		return response.toJSONString();
	}*/
	
	public String publish(JSONObject cmd){
		JSONObject response=new JSONObject();
		String newresStr=cmd.get("resource").toString();
		JSONObject newresJSON=toJSON(newresStr);
		Resource newres=new Resource(newresJSON);
	//System.out.println(rs.toString());	
		if(newres.isEmpty()){
			response.put("response", "error");
			response.put("errorMessage", "missing resource");
			return response.toJSONString();
		}
		try{
			String resOwner=newres.getOwner();
			String resChannel=newres.getChannel();
			String resUri=newres.getUri();
			String resFilename="("+resOwner+","+resChannel+","+resUri+").json";
			String filePath=Server.resourceFolder+resFilename;
			File file=new File("Resource");
			if(!file.exists())
				{file.mkdirs();}
			writeFile(filePath,newresStr);
			response.put("response", "success");		
		}
	catch (IOException e) {
			response.put("response", "error");
			response.put("errorMessage", "cannot publish resource");
			e.printStackTrace();
		}
		return response.toJSONString();
	}
	
	public JSONObject toJSON(String jsonStr){
		JSONParser parser=new JSONParser();
		JSONObject json=new JSONObject();
		try {
			Object obj=parser.parse(jsonStr);
			json=(JSONObject)obj;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	private boolean equal(JSONObject res1,JSONObject res2){
		if(res1.get("owner").toString().equals(res2.get("owner").toString())&&
				res1.get("channel").toString().equals(res2.get("channel").toString())&&
				res1.get("uri").toString().equals(res2.get("uri").toString())
				)
			return true;
		else
			return false;
	}
	public void writeFile(String filePath,String resource) throws IOException{
	    FileWriter fw = new FileWriter(filePath);
	    PrintWriter out = new PrintWriter(fw);
	    out.write(resource);
	    out.println();
	    fw.close();
	    out.close();
	    }

	public ArrayList<String> readFile(String filePath) throws IOException{
		ArrayList<String> resourcelist=new ArrayList<String>();
		File file=new File(filePath);
		if(!file.exists())
			return null;
		BufferedReader reader=null;
		reader=new BufferedReader(new FileReader(file));
		String string=null;
		while((string=reader.readLine())!=null){
			resourcelist.add(string);
		}
		reader.close();
		return resourcelist;
		}

	public Resource readJSON(File JSONTXT) throws ParseException {
		String JSONStr = "";
		Resource resource = new Resource();
		try {
			
			if (JSONTXT.isFile() && JSONTXT.exists()) {
				InputStreamReader read = new InputStreamReader(
                new FileInputStream(JSONTXT));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                	JSONStr += lineTxt;
                }
                bufferedReader.close();
                read.close();
			}
			else {
				System.out.println("can not find the file !");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return resource.fromString(JSONStr);
	}
	
	public ArrayList<Resource> getAllResource(String filePath) throws ParseException {
		ArrayList<Resource> allResource = new ArrayList<Resource>();
		File f = null;
		f = new File(filePath);
		File[] files = f.listFiles();
		for (File file : files) {
			Resource fileResource = readJSON(file);
			allResource.add(fileResource);
		}
		return allResource;
	}
	
	public boolean intersection(ArrayList<String> l1, ArrayList<String> l2) {
		List list = new ArrayList(Arrays.asList(new Object[l1.size()])); 
		Collections.copy(list, l1); 
        list.retainAll(l2); 
        if (list.size() == 0) {
        	return false;
        }
        else {
        	return true;
        }
	}
	
	
	public boolean queryMatch(Resource resource, JSONObject resourceTemplate) throws ParseException {
		//boolean match = true;
		JSONParser parser = new JSONParser();
		//JSONObject resourceTemplate = (JSONObject) parser.parse((String)queryJSON.get("resourceTemplate"));
		Resource rt = new Resource().fromJSON(resourceTemplate);
		if (! resource.channel.equals(rt.channel)) {return false;}
		if (( rt.owner.equals("")) || (! resource.owner.equals(rt.owner))) {return false;}
		if (! intersection(resource.tags, rt.tags)) {return false;}
		if (! resource.uri.equals(rt.uri)) {return false;}
		if ((rt.name.equals("") || resource.name.contains(rt.name)) && 
			(rt.description.equals("") || resource.description.contains(rt.description)) && 
			((!rt.description.equals("")) || (!rt.name.equals("")))) {return false;}
		return true;
	}
	
	public void remove(JSONObject cmd){
		
	}
	public String query(JSONObject cmd) throws ParseException{
		ArrayList<JSONObject> queryResult = new ArrayList<JSONObject>();
		ArrayList<JSONObject> finalResult = new ArrayList<JSONObject>();
		String resultStr = "";
		String filePath = "./resource";
		Resource res = new Resource().fromJSON(cmd);
		JSONObject resourceTemplate = res.toJSON();
		ArrayList<Resource> allResource = getAllResource(filePath);
		try{
			for (Resource resource : allResource) {
				if (queryMatch(resource, resourceTemplate)) {
					resource.setter("owner", "*");
					queryResult.add(resource.toJSON());
				}
			}
			if (queryResult.size() != 0) {
				JSONObject success = new JSONObject();
				success.put("response", "success");
				finalResult.add(success);
				for (JSONObject q : queryResult) {
					finalResult.add(q);
				}
				JSONObject resultSize = new JSONObject();
				resultSize.put("resultSize", Integer.toString(queryResult.size()));
				finalResult.add(resultSize);
			}
			else if (resourceTemplate.toJSONString().equals("")) {
				JSONObject error = new JSONObject();
				error.put("response", "error");
				error.put("errorMesage", "missing resourceTemplate");
				finalResult.add(error);
			}
			}
		catch (ParseException e) {
			
			JSONObject error = new JSONObject();
			error.put("response", "error");
			error.put("errorMesage", "invalid resourceTemplate");
			finalResult.add(error);

		}
		for (JSONObject result : finalResult) {
			resultStr += result.toJSONString();
		}
		return resultStr;
		
	}
	
	
	public void share(JSONObject cmd){
		
	}
	public void fetch(JSONObject cmd){
		
	}
	public void exchange(JSONObject cmd){
		
	}

}
