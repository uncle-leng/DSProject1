import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;


public class Resource {
	public String name;
	public String description;
	public String[] tags;
	public String uri;
	public String channel;
	public String owner;
	public String ezserver;
	
	public Resource() {
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
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setTags(String[] tags) {
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
	
	public String toString(JSONObject jsonObj) {
		return jsonObj.toString();
	}
	
	public JSONObject toJSON(String jsonString) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject)parser.parse(jsonString);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
