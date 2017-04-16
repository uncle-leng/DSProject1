import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

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
	public JSONArray resourceTemplate;
	
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
	
	public void setResourceTemplate(JSONArray resourceTemplate){
		this.resourceTemplate=resourceTemplate;
	}
	
	public JSONObject toJson(){
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
			JSONcmd.put("resource", resource.toJSON().toJSONString());
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
	public void parseCommand(String command) throws ParseException, IOException {
		JSONParser parser = new JSONParser();
		JSONObject jsonCommand = (JSONObject) parser.parse(command);
		switch(jsonCommand.get("command").toString()) {
		case "PUBLISH":
			publish(jsonCommand); // call publish function
			break;
		case "REMOVE":
			remove(jsonCommand);
			break;
		case "SHARE":
			share(jsonCommand);
			break;
		case "QUERY":
			query(jsonCommand);
			break;
		case "FETCH":
			fetch(jsonCommand);
			break;
		case "EXCHANGE":
			exchange(jsonCommand);
			break;
		default:
			break;
		}
	}
	public void publish(JSONObject cmd) throws IOException{
		Resource publishResource = new Resource();
		for (Object key : cmd.keySet()) {
			publishResource.setter(key.toString(), cmd.get(key).toString());
		}	
		
		String resourceStr=cmd.get("resource").toString();
		String name=cmd.get("name").toString();
		File resource=new File(name+".json");
		FileOutputStream out = new FileOutputStream(resource);
		byte[] bytes=new byte[512];
		bytes=resourceStr.getBytes();
		int length=resourceStr.length();
		out.write(bytes, 0, length);
		out.close();
	}
	public void remove(JSONObject cmd){
		
	}
	public void query(JSONObject cmd){
		
	}
	public void share(JSONObject cmd){
		
	}
	public void fetch(JSONObject cmd){
		
	}
	public void exchange(JSONObject cmd){
		
	}

}
