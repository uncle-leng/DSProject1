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
         ·ð×æ±£ÓÓ       ÓÀÎÞBUG 
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
	

}
