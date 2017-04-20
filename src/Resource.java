import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
	
	public Resource(JSONObject resjson){
		this.name=resjson.get("name").toString();
		this.description=resjson.get("description").toString();
		if(resjson.get("tags").toString().equals("[]"))
			this.tags=new ArrayList<String>();
		else{
			String[] tagArray = resjson.get("tags").toString().split(",");
			ArrayList<String> tagArrayList= new ArrayList<String>();
			Collections.addAll(tagArrayList, tagArray);
			this.tags = tagArrayList;
			}
		this.uri=resjson.get("uri").toString();
		this.channel=resjson.get("channel").toString();
		this.owner=resjson.get("owner").toString();
		if(resjson.get("ezserver")==null)
			this.ezserver=null;
		else
			this.ezserver=resjson.get("ezserver").toString();
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
	
	public Resource(String str) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject obj = (JSONObject) parser.parse(str);
		this.name = obj.get("name").toString();
		this.description = obj.get("description").toString();
		if(obj.get("tags").toString().equals("[]"))
			this.tags=new ArrayList<String>();
		else{
			String[] tagArray = obj.get("tags").toString().split(",");
		    ArrayList<String> tagArrayList= new ArrayList<String>();
		    Collections.addAll(tagArrayList, tagArray);
		    this.tags = tagArrayList;
		    }
		this.uri = obj.get("uri").toString();
		this.channel = obj.get("channel").toString();
		this.owner = obj.get("owner").toString();
		if (obj.get("ezserver") == null) {
			this.ezserver = null;
		}
		else {
			this.ezserver = obj.get("ezserver").toString();
		}
		
		
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
	
	public String getUri() {
		return uri;
	}

	public String getChannel() {
		return channel;
	}

	public String getOwner() {
		return owner;
	}
	
	public Resource fromString(String resourceStr) throws ParseException {
		Resource resource = new Resource();
		JSONParser parser = new JSONParser();
		JSONObject resourceObj = (JSONObject) parser.parse(resourceStr);
		for(Iterator iterator = resourceObj.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			this.setter(key,resourceObj.get(key).toString());
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
	/*
	public Resource fromJSON(JSONObject obj) throws ParseException {
		Resource resource = new Resource();
		String objStr = obj.toJSONString();
		return fromString(objStr);
	}
	*/
	
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
	
	public boolean isEmpty(){
		if (this.name.equals("") && this.description.equals("") && this.tags.isEmpty()
				&& this.uri.equals("") && this.channel.equals("") && this.owner.equals("")
				&& this.ezserver == null)
			return true;
		else
			return false;
	}
	
	public String getPK(){
		String owner=this.owner;
		String channel=this.channel;
		String uri=this.uri;
		String PK="("+owner+","+channel+","+uri+")";
		return PK.trim();
	}

}
