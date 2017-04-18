import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	public JSONArray serverList;
	public JSONArray resourceTemplate;
	
	public Command(){
		command="";
		secret="";
		resource=new Resource();
		relay=false;
		serverList=null;
		resourceTemplate=null;
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
		case "publish":
			publish(jsonCommand); // call publish function
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
			break;
		}
	}

	public void publish(JSONObject cmd){
		String newres=cmd.get("resource").toString();
		JSONObject obj1=toJSON(newres);
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
			}
		}
	catch (IOException e) {
			e.printStackTrace();
		}
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
	    FileWriter fw = new FileWriter(filePath,true);
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
