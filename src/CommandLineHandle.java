import javax.sound.sampled.Line;

import org.apache.commons.cli.*;
import org.json.simple.*;

public class CommandLineHandle {
	
	public Command command;
	public Options getOptions() {
		Options options = new Options();
		options.addOption("channel", true, "channel");
		options.addOption("debug", false, "print debug information");
		options.addOption("description", true, "resource description");
		options.addOption("exchange", false, "exchange server list with server");
		options.addOption("fetch", false, "fetch resources from server");
		options.addOption("host", true, "server host, a domain name or IP address");
		options.addOption("name", true, "resource name");
		options.addOption("owner", true, "owner");
		options.addOption("port", true, "server port, an integer");
		options.addOption("publish", false, "publish resource on server");
		options.addOption("query", false, "query for resources from server");
		options.addOption("remove", false, "remove resource from server");
		options.addOption("secret", true, "secret");
		options.addOption("servers", true, "server list, host1:port1,host2:port2,...");
		options.addOption("share", false, "share resource on server");
		options.addOption("tags", true, "resource tags, tag1,tag2,tag3,...");
		options.addOption("uri", true, "resource URI");
		return options;
	}
	
	public String parse(String[] JSONString, Options options) {
		CommandLineParser parser = new DefaultParser();
		JSONObject result = new JSONObject();
		//String lineString = "";
		try {
			CommandLine line = parser.parse(options, JSONString);
			//if (line.hasOption("channel")) {result.put("channel", line.getOptionValue("channel"));}
			if (line.hasOption("channel")) {command.resource.setter("channel", line.getOptionValue("channel"));}
			//if (line.hasOption("description")) {result.put("description", line.getOptionValue("description"));}
			if (line.hasOption("description")) {command.resource.setter("description", line.getOptionValue("discription"));}
			if (line.hasOption("host")) {result.put("host", line.getOptionValue("host"));}//to  be improved
			//if (line.hasOption("name")) {result.put("name", line.getOptionValue("name"));}
			if (line.hasOption("name")) {command.resource.setter("name", line.getOptionValue("name"));}
			//if (line.hasOption("owner")) {result.put("owner", line.getOptionValue("owner"));}
			if (line.hasOption("owner")) {command.resource.setter("owner", line.getOptionValue("owner"));}
			if (line.hasOption("port")) {result.put("port", line.getOptionValue("port"));}//to be improved
			//if (line.hasOption("secret")) {result.put("secret", line.getOptionValue("secret"));}
			if (line.hasOption("secret")) {command.setSecret(line.getOptionValue("secret"));}
			//if (line.hasOption("servers")) {result.put("servers", line.getOptionValue("servers"));}
			if (line.hasOption("servers")) {command.addServer(line.getOptionValue("servers"));}//to be improved
			//if (line.hasOption("tags")) {result.put("tags", line.getOptionValue("tags"));}
			if (line.hasOption("tags")) {command.resource.setter("tags", line.getOptionValue("tags"));}
			//if (line.hasOption("uri")) {result.put("uri", line.getOptionValue("uri"));}
			if (line.hasOption("uri")) {command.resource.setter("uri", line.getOptionValue("uri"));}
			if (line.hasOption("debug")) {result.put("debug", "");}
			//if (line.hasOption("exchange")) {result.put("command", "EXCHANGE");}
			if (line.hasOption("exchange")) {command.setCommand("exchange");}
			//if (line.hasOption("fetch")) {result.put("command", "FETCH");}
			if (line.hasOption("fetch")) {command.setCommand("fetch");}
			//if (line.hasOption("publish")) {result.put("command", "PUBLISH");}
			if (line.hasOption("publish")) {command.setCommand("publish");}
			//if (line.hasOption("query")) {result.put("command", "QUERY");}
			if (line.hasOption("query")) {command.setCommand("query");}
			//if (line.hasOption("remove")) {result.put("command", "REMOVE");}
			if (line.hasOption("remove")) {command.setCommand("remove");}
			//if (line.hasOption("share")) {result.put("command", "SHARE");}
			if (line.hasOption("share")) {command.setCommand("share");}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//return result.toString();
		return command.toJson().toString();
	}
	
	
	
}
