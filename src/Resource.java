import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.util.*;

public class Resource {
	public String name;
	public String description;
	//public String[] tags;
	public ArrayList<String> tags;
	public String uri;
	public String channel;
	public String owner;
	public String ezserver;
	
	public Resource() {
		this.name = "";
		this.description = "";
		this.tags = new ArrayList();
		this.uri = "";
		this.channel = "";
		this.owner = "";
		this.ezserver = null;
	}
	
	public Resource(Resource obj) {
		this.name = obj.name;
		this.description = obj.description;
		this.tags = obj.tags;
		this.uri = obj.uri;
		this.channel = obj.channel;
		this.owner = obj.owner;
		this.ezserver = obj.ezserver;
	}
	/*
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public void setEzerver(String ezserver) {
		this.ezserver = ezserver;
	}
	*/
	public void setter(String key, String value) {
		switch (key) {
		case "name" : 
			this.name = value;
			break;
		case "description" :
			this.description = value;
			break;
		case "uri" :
			this.uri = value;
			break;
		case "channel" : 
			this.channel = value;
			break;
		case "owner" :
			this.owner = value;
			break;
		case "ezserver" :
			this.ezserver = value;
			break;
		case "tags" :
			String[] tagArray = value.split(",");
			ArrayList<String> tagArrayList= new ArrayList<String>();
			Collections.addAll(tagArrayList, tagArray);
			this.tags = tagArrayList;
			break;
		default :
			break;
			
		}
	}
	/*
	public void setter(String key, ArrayList<String> value) {
		if (key.equals("tags")) {
			this.tags = value;
		}
	}
	*/
	public String toString(/*Resource obj*/) {
		//return obj.toString();
		return this.toJSON().toString();
	}
	
	public JSONObject toJSON(){
		/*JSONParser parser = new JSONParser();
		return (JSONObject)parser.parse(obj.toString());*/
		JSONObject resource=new JSONObject();
        resource.put("name", this.name);
        resource.put("description", this.description);
        resource.put("tags",this.tags);
        resource.put("uri", this.uri);
        resource.put("channel", this.channel);
        resource.put("owner",this.owner);
        resource.put("ezserver", this.ezserver);
        return resource;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
