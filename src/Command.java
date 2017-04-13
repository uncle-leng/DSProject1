import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

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
	public ArrayList<String> serverList;
	public JSONArray resourceTemplate;
	
	public Command(String command){
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
	
	public JSONObject toJson() throws ParseException{
		String jsonStr="";
		JSONParser parser = new JSONParser();
		switch(this.command){
		case "publish":
			jsonStr="\"command\":\"PUBLISH\","+resource.toString();
			break;
		case "remove":
			jsonStr="\"command\":\"REMOVE\","+resource.toString();
			break;
		case "share":
			jsonStr="\"command\":\"SHARE\",\"secret\":"+secret+","+resource.toString();
			break;
		case "query":
			jsonStr="\"command\":\"QUERY\",\"relay\":"+relay+","+resource.toString();
			break;
		case "fetch":
			jsonStr="\"command\":\"FETCH\","+resource.toString();
			break;
		default:break;
		}
		return (JSONObject)parser.parse(jsonStr);
	}
	
	/**
	 * this publish method takes a json as input
	 * and write a json file on server.
	 * @throws ParseException 
	 * **/
	public void parseCommand(String command) throws ParseException {
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
	public void publish(JSONObject cmd){
		String resourceStr=cmd.get("resource").toJSONString();
		String name=cmd.get("name").toString();
		File resource=new File(name+".json");
		FileOutputStream out = new FileOutputStream(resource);
		byte[] bytes=new byte[512];
		bytes=resourceStr.getBytes();
		int length=resourceStr.length();
		out.write(bytes, 0, length);
		out.close();
	}

}
