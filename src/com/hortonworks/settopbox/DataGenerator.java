package com.hortonworks.settopbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;

public class DataGenerator {
	private static final String START_EVENT_ID = "settopboxdemo.events.starting_event_id";
	private static final String START_DEVICE_ID = "settopboxdemo.users.starting_device_id";
	
	private static final String AUTO_GENERATE_USERS = "settopboxdemo.users.auto_generate";
	private static final String MANUAL_USER_COUNT = "settopboxdemo.users.manual_user_count";
	private static final String ENFORCE_UNIQUE_EVENTS = "settopboxdemo.events.enforce_unique";
	
	private static final String MARKET_NAMES = "settopboxdemo.data.markets.cities";
	private static final String MARKET_ZIP_CODES = "settopboxdemo.data.markets.zip_codes";
	private static final String MARKET_LATLONG = "settopboxdemo.data.markets.latlong";
    private static final String MARKET_STATE = "settopboxdemo.data.markets.state";
	private static final String MARKET_GMT_OFFSETS = "settopboxdemo.data.markets.gmt_offsets";
	private static final String PROGRAM_GUIDE_FILE = "settopboxdemo.data.input.program_guide";
	private static final String EVENT_OUTPUT_FILE = "settopboxdemo.data.output.events";
	private static final String TOTAL_EVENT_COUNT = "settopboxdemo.events.count";
	private static final String USE_GMT_OFFSETS =  "settopboxdemo.data.use_gmt_offsets";
	//static String currentDir = System.getProperty("user.dir");

	private int starting_event_id = 0;
	private int starting_device_id = 0;
	private int total_events = 0;
	private String program_guide = "";
	private String event_file = "";
	private String cities = "";
    private String states = "";
	private String zip_codes = "";
	private String latlong = "";
	private String gmt_offsets = "";
	private boolean use_gmt_offsets = false;
	
	private boolean auto_generate_users = true;
	private int manual_user_count = -1;
	private boolean enforce_unique_events = true;
	
	private ArrayList<String> alEvents;
	private HashMap<Integer, String[]> hmUsers;
	private HashMap<Integer, String[]> hmProgramGuide;
	private HashMap<Integer, String[]> hmMarkets;
	private HashSet<String> hsDays;
	private HashSet<String> hsExcludeList;

	public DataGenerator(Properties props) {

		total_events = Integer.parseInt(props.getProperty(TOTAL_EVENT_COUNT));
		starting_event_id = Integer.parseInt(props.getProperty(START_EVENT_ID, "0"));
		starting_device_id = Integer.parseInt(props.getProperty(START_DEVICE_ID,"0"));
		program_guide = props.getProperty(PROGRAM_GUIDE_FILE);
		event_file = props.getProperty(EVENT_OUTPUT_FILE);
		cities = props.getProperty(MARKET_NAMES);
        states = props.getProperty(MARKET_STATE);
		zip_codes = props.getProperty(MARKET_ZIP_CODES);
		latlong = props.getProperty(MARKET_LATLONG);
		gmt_offsets = props.getProperty(MARKET_GMT_OFFSETS);
		if ("true".equals(props.getProperty(USE_GMT_OFFSETS,"false").toLowerCase())){
			use_gmt_offsets = true;
		}
		if ("false".equals(props.getProperty(AUTO_GENERATE_USERS,"true").toLowerCase())){
			auto_generate_users = false;
		} 
		if ("false".equals(props.getProperty(ENFORCE_UNIQUE_EVENTS,"true").toLowerCase())){
			enforce_unique_events = false;
		} 
		manual_user_count = Integer.parseInt(props.getProperty(MANUAL_USER_COUNT, "-1"));

		
		hmUsers = new HashMap<Integer, String[]>();
		hmProgramGuide = new HashMap<Integer, String[]>();
		hmMarkets = new HashMap<Integer, String[]>();
		hsDays = new HashSet<String>();
		hsExcludeList = new HashSet<String>();
		alEvents = new ArrayList<String>();

		buildPrograms();
		buildMarkets();
		buildUsers();
		generateEvents();
	}

	private void writeOutputFile() {

		try {
			File fout = new File(event_file);
			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for (int x = 0; x < alEvents.size(); x++) {
				bw.write(alEvents.get(x));
				bw.newLine();
			}
			bw.close();
			System.out.println("Output written to " + event_file);
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	private void generateEvents() {

		String[] user;
		String[] program;

		int id = starting_event_id;
		long long_timestamp = 0;
		boolean bContinue = true;

		while (alEvents.size() < total_events) {
			
			bContinue = true;
			user = getUser();
			program = getProgram();
			String check = user[0] + ":" + program[2];

			if (enforce_unique_events) {
				check = user[0] + ":" + program[2];
				if (hsExcludeList.contains(check)) {
					bContinue = false;
				}
			}
			
			if (bContinue) {
				id++;
				
				if (use_gmt_offsets) {
					long_timestamp = Long.parseLong(program[2]) + Long.parseLong(user[3]);
				} else {
					long_timestamp = Long.parseLong(program[2]);
				}
				
				String event_string = "{\"event_id_s\":\"" + id
                        + "\",\"channel_s\":\"" + program[3]
                        + "\",\"gmt_offset_s\":\"" + user[3]
						+ "\",\"src_tl\":\"" + long_timestamp
						+ "\",\"market_s\":\"" + user[1]
                        + "\",\"program_s\":\"" + program[5]
                        + "\",\"device_id_s\":\"" + user[0]
						+ "\",\"zip_code_s\":\"" + user[2]
                        + "\",\"network_s\":\"" + program[4]
                        + "\",\"state_s\":\"" + user[5]
                        + "\",\"latlong_p\":\"" + user[4]  + "\"}";
				System.out.println(event_string);
				alEvents.add(event_string);
				hsExcludeList.add(check);
			}
		}

	}

	private String[] getUser() {
		String[] user = null;
		int random = getRandomNumber(hmUsers.size());
		if (starting_device_id > 0) {
			random = random + starting_device_id;
		}
		user = hmUsers.get(new Integer(random));
		user = new String[] { String.valueOf(random), user[0], user[1], user[2], user[3], user[4] }; // user_id,
																					// market_name,
																					// zip_code,
																					// gmt_offset
																					// latlong
                                                                                    // states
		return user;
	}

	private void buildUsers() {

		int user_count = -1;
		
		if (!auto_generate_users && manual_user_count > 0) {
			user_count = manual_user_count;
		} else {
			user_count = total_events / hsDays.size();
		}
		
		System.out.println("User Count = " + user_count);

		for (int x = starting_device_id; x < (user_count + starting_device_id); x++) {
			hmUsers.put(new Integer(x), getMarket());
		}

		System.out.println("buildUsers complete.");
	}

	private String[] getMarket() {
		String[] market = null;
		int random = getRandomNumber(hmMarkets.size());
		market = hmMarkets.get(new Integer(random));
		return market;
	}

	private void buildMarkets() {
		String[] market_names = cities.split(",");
		String[] market_zips = zip_codes.split(",");
		String[] market_gmt_offsets = gmt_offsets.split(",");
		String[] market_latlong = latlong.split(";");
        String[] market_states = states.split(",");
		for (int x = 0; x < market_names.length; x++) {
			hmMarkets.put(new Integer(x), new String[] { market_names[x],
					market_zips[x], market_gmt_offsets[x], market_latlong[x], market_states[x] });
		}
		System.out.println("buildMarkets complete.");
	}

	private String[] getProgram() {
		String[] program = null;
		int random = getRandomNumber(hmProgramGuide.size());
		program = hmProgramGuide.get(new Integer(random));
		return program;
	}

	private void buildPrograms() {

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		int x = 0;

		try {
			br = new BufferedReader(new FileReader(program_guide));
			while ((line = br.readLine()) != null) {
				String[] program = line.split(cvsSplitBy);
				this.hsDays.add(program[0]);
				this.hmProgramGuide.put(new Integer(x), program);
				x++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("buildPrograms complete.");
	}



	private static int getRandomNumber(int range) {
		int random_num = 0;
		Random randomGenerator = new Random();
		for (int idx = 1; idx <= 10; ++idx) {
			random_num = randomGenerator.nextInt(range);
		}
		return random_num;
	}
	
	public static void main(String[] args) {

		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(args[0]);
			prop.load(input);
			DataGenerator dg = new DataGenerator(prop);
			dg.writeOutputFile();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
