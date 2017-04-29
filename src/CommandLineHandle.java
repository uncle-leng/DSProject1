import java.net.URISyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;

public class CommandLineHandle {

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
		options.addOption("relay", true, "set relay");
		return options;
	}

	public Options getServerOptions() {
		Options options = new Options();
		options.addOption("advertisedhostname", true, "advertised hostname");
		options.addOption("connectionintervallimit", true, "connection interval limit in seconds");
		options.addOption("port", true, "server port, an integer");
		options.addOption("secret", true, "secret");
		options.addOption("debug", false, "print debug information");
		options.addOption("exchangeinterval", true, "exchange interval in seconds");
		return options;
	}

	public JSONObject parse(String[] JSONString, Options options) throws URISyntaxException {
		Command command = new Command();
		CommandLineParser parser = new DefaultParser();
		// String lineString = "";
		try {
			CommandLine line = parser.parse(options, JSONString);
			if (line.hasOption("host")) {
				Client.setHost(line.getOptionValue("host"));
			}
			if (line.hasOption("port")) {
				Client.setPort(Integer.parseInt(line.getOptionValue("port")));
			}
			if (line.hasOption("publish") || line.hasOption("remove") || line.hasOption("share")) {
				if (line.hasOption("publish")) {
					command.setCommand("PUBLISH");
				}
				if (line.hasOption("remove")) {
					command.setCommand("REMOVE");
				}
				if (line.hasOption("share")) {
					command.setCommand("SHARE");
				}

				// resource
				if (line.hasOption("channel")) {
					command.getResource().setter("channel", line.getOptionValue("channel"));
				}
				if (line.hasOption("description")) {
					command.getResource().setter("description", line.getOptionValue("description"));
				}
				if (line.hasOption("name")) {
					command.getResource().setter("name", line.getOptionValue("name"));
				}
				if (line.hasOption("owner")) {
					command.getResource().setter("owner", line.getOptionValue("owner"));
				}
				if (line.hasOption("tags")) {
					command.getResource().setter("tags", line.getOptionValue("tags"));
				}
				if (line.hasOption("uri")) {
					command.getResource().setter("uri", line.getOptionValue("uri"));
				}
				if (line.hasOption("ezserver")) {
					command.getResource().setter("ezserver", line.getOptionValue("ezserver"));
				}

				if (line.hasOption("secret")) {
					command.setSecret(line.getOptionValue("secret"));
				}
			} else if (line.hasOption("query") || line.hasOption("fetch")) {
				if (line.hasOption("fetch")) {
					command.setCommand("FETCH");
				}
				if (line.hasOption("query")) {
					command.setCommand("QUERY");
				}

				// resourceTemplate
				if (line.hasOption("channel")) {
					command.resourceTemplate.setter("channel", line.getOptionValue("channel"));
				}
				if (line.hasOption("description")) {
					command.resourceTemplate.setter("description", line.getOptionValue("description"));
				}
				if (line.hasOption("name")) {
					command.resourceTemplate.setter("name", line.getOptionValue("name"));
				}
				if (line.hasOption("owner")) {
					command.resourceTemplate.setter("owner", line.getOptionValue("owner"));
				}
				if (line.hasOption("tags")) {
					command.resourceTemplate.setter("tags", line.getOptionValue("tags"));
				}
				if (line.hasOption("uri")) {
					command.resourceTemplate.setter("uri", line.getOptionValue("uri"));
				}
				if (line.hasOption("ezserver")) {
					command.resourceTemplate.setter("ezserver", line.getOptionValue("ezserver"));
				}
				if (line.hasOption("relay")) {
					command.setRelay(Boolean.parseBoolean(line.getOptionValue("relay")));
				}

			} else if (line.hasOption("exchange")) {
				command.setCommand("EXCHANGE");
				// if (line.hasOption("serverList"))
				// {command.resource.setter("ezserver",
				// line.getOptionValue("ezserver"));}
				if (line.hasOption("servers")) {
					command.addServer(line.getOptionValue("servers"));
				} // to be improved
			}
			// else if(){ }
			else
				System.out.println("invalid command");
		}

		catch (MissingArgumentException e) {
			System.out.println("missing argument!");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("cannot parse command");
		}
		// return result.toString();
		// System.out.println(command.toJSON().toJSONString());
		return command.toJSON();
	}

	public void parseServerCmd(String[] cmdString, Options options) {
		CommandLineParser parser = new DefaultParser();
		try {
			/*
			 * options.addOption("advertisedhostname", true,
			 * "advertised hostname");
			 * options.addOption("connectionintervallimit", true,
			 * "connection interval limit in seconds");
			 * options.addOption("port", true, "server port, an integer");
			 * options.addOption("secret", true, "secret");
			 * options.addOption("debug", false, "print debug information");
			 */
			CommandLine line = parser.parse(options, cmdString);
			if (line.hasOption("advertisedhostname"))
				Server.setHostName(line.getOptionValue("advertisedhostname"));
			if (line.hasOption("connectionintervallimit"))
				Server.setConnectionIntervallimit(Integer.parseInt(line.getOptionValue("connectionintervallimit")));
			if (line.hasOption("port"))
				Server.setPort(Integer.parseInt(line.getOptionValue("port")));
			if (line.hasOption("secret"))
				Server.setSecret(line.getOptionValue("secret"));
			if (line.hasOption("exchangeinterval"))
				Server.setExchangeinterval(Integer.parseInt(line.getOptionValue("exchangeinterval")));
		} catch (MissingArgumentException e) {
			System.out.println("missing argument!");
		} catch (ParseException e) {
			System.out.println("cannot parse command");
		}
	}

	public boolean debug(String[] cmdString, Options options) {
		CommandLineParser parser = new DefaultParser();
		CommandLine line;
		try {
			line = parser.parse(options, cmdString);
			return line.hasOption("debug");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return false;
	}

}
