
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;



/**
 * The class that manages the configuration constants
 * used in the server. The configuration file is automatically
 * generated if there no existing file. The file is then
 * filled with the default values.
 * 
 * Thread safe as no configurations can be written here.
 * @author Kevin
 *
 */
public class Config{

	//private static final int MIN_PORT = 1000;
	//private static final int MAX_PORT = 65535;



	private static final String CONFIG_FILE_NAME = "proxy_config";
	private static final String DELIM = "=";

	
	private static final String REDIRECT_URL = "redirect-URL";
	private static final String SERVER_PORT_FIELD = "server-port";
	
	
	private static HashMap<String, String> properties = new HashMap<String, String>();




	private static Config instance = new Config();

	public static Config getInstance(){
		return instance;
	}

	private Config(){
		//find the user database and read values by comma separation
		File dbfile = new File(CONFIG_FILE_NAME);
		if (dbfile.exists() == false){
			//create the file
			try {
				dbfile.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Can not create configuration file. Quitting...");
				System.exit(-1);
			}
		}
		
		//fill properties with defaults
		fillDefaults();	

		//database file found, read the file
		try {
			FileReader fr = new FileReader(dbfile);
			BufferedReader br = new BufferedReader(fr);


			//now read each line
			String nextLine = br.readLine();
			String[] tokens;
			while (nextLine != null){

				tokens = nextLine.split(DELIM, 2);

				if (tokens.length == 2){
					//exactly two values, then 
					tokens[0] = tokens[0].trim();		//field name
					//tokens[1] = tokens[1].trim();		//value

					if ((tokens[0] != null) && (tokens[1] != null) && (tokens[0].length() >= 1) && (tokens[1].length() >= 1)){


						//add to master list
						if (properties.containsKey(tokens[0])){
							//has key, take the value
							properties.put(tokens[0], tokens[1]);
						}


					}

				}


				//otherwise skip to next line
				nextLine = br.readLine();
			}

			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();

		}

		
		saveAll();
	}

	/**
	 * Saves all current user values to the file.
	 */
	private synchronized static void saveAll(){
		File dbfile = new File(CONFIG_FILE_NAME);

		if (dbfile.exists() == false){
			//cannot file file, cannot save
			System.err.println("Error! Cannot save user file, cannot be found");
		}
		else{

			String warning = "## Server Configuration file ##";

			//open file for overwriting
			try {
				BufferedWriter userFile = new BufferedWriter(new FileWriter(dbfile, false));

				userFile.write(warning + "\n");

				for (Entry<String,String> pair : properties.entrySet()){
					userFile.write(pair.getKey() + DELIM + pair.getValue() + "\n");
				}

				userFile.flush();
				userFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Fills the properties with the current defaults
	 */
	private static void fillDefaults(){
		properties.put(REDIRECT_URL, "REDIRECT URL HERE");
		properties.put(SERVER_PORT_FIELD, "48567");
	}

	public static int getCount(){
		return properties.size();
	}

	public static String getRedirectURL() {
		return properties.get(REDIRECT_URL);
	}

	
	public static int getPort(){
		return Integer.parseInt(properties.get(SERVER_PORT_FIELD));
	}
}
