import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.IOException;
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
		this.tags = new ArrayList<String>();
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
	public Resource fromString(String resourceStr) throws ParseException {
		Resource resource = new Resource();
		JSONParser parser = new JSONParser();
		JSONObject resourceObj = (JSONObject) parser.parse(resourceStr);
		for(Iterator iterator = resourceObj.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			this.setter(key, (String) resourceObj.get(key));
		}
		return resource;
		
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
	
	public Resource fromJSON(JSONObject obj) throws ParseException {
		Resource resource = new Resource();
		String objStr = obj.toJSONString();
		return fromString(objStr);
	}
	
	/**
	 * This method determines whether two resources are the same resource
	 * The primary key for a resource being a tuple(owner,channel,uri)
	 */
	public boolean equal(Resource resource){
		if(
				this.owner.equals(resource.owner)&&
				this.channel.equals(resource.channel)&&
				this.uri.equals(resource.uri)
				)
			return true;
		else
			return false;
	}

}
