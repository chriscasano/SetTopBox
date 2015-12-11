package com.hortonworks.settopbox;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class MessageSender {
	
	private static final String BROKER_LIST = "settopboxdemo.kafka.broker.list";
	private static final String	MILLISECOND_DELAY =	"settopboxdemo.kafka.events.milliseconds_delay";
	private static final String	SERIALIZER_CLASS =	"settopboxdemo.kafka.serializer.class";
	private static final String	REQUEST_REQUIRED_ACKS =	"settopboxdemo.kafka.request.required.acks";
	private static final String MESSAGE_TOPIC = "settopboxdemo.kafka.topic";
	private static final String INPUT_FILE = "settopboxdemo.kafka.input_file";
	
	private String broker_list = "";
	private int millisecond_delay = 0;
	private String serializer_class = "";
	private String required_acks = "1";
	private String message_topic="";
	private String input_file = "";

	public MessageSender(Properties props) {
		System.out.println("Loading properties...");
		
		broker_list = props.getProperty(BROKER_LIST);
		millisecond_delay = Integer.parseInt(props.getProperty(MILLISECOND_DELAY,"0"));
		serializer_class = props.getProperty(SERIALIZER_CLASS, "kafka.serializer.StringEncoder");
		required_acks = props.getProperty(REQUEST_REQUIRED_ACKS,"1");
		message_topic = props.getProperty(MESSAGE_TOPIC, "settop_box_events");
		input_file = props.getProperty(INPUT_FILE);
		
		System.out.println("Input File = " + input_file);
		System.out.println("Broker List = " + broker_list);
		System.out.println("Millisecond Delay = " + millisecond_delay);
		System.out.println("Required Acks = " + required_acks);
		System.out.println("Message Topic = " + message_topic);
		System.out.println("Serializer Class = " + serializer_class);
		
		sendEvents();
	}
	
	private void sendEvents() {
		
		try{
			// create a producer
	
			Properties props = new Properties();

	
			props.put("metadata.broker.list", broker_list);
			props.put("serializer.class", serializer_class);
			props.put("request.required.acks", required_acks);
	
			ProducerConfig config = new ProducerConfig(props);
			Producer producer = new Producer<String, String>(config);

			//sending...
			String topic = message_topic;
			String sFile = input_file;
			
			// Open the file
			FileInputStream fstream = new FileInputStream(sFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	
			String strLine;
			
			System.out.println("Processing input file...");
	
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().getTime());
				System.out.println(strLine + timeStamp + "\"}");
				KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(topic, strLine + timeStamp + "\"}");
				producer.send(keyedMessage);
				if (this.millisecond_delay > 0) {
					Thread.sleep(millisecond_delay);
				}
			}
	
			//Close the input stream
			br.close();
			
	} catch (Exception e) {
		System.out.println("Error...");
		e.printStackTrace();
		System.out.println(e.getMessage());
	}

	System.out.println("Message sending complete.");		
		
	}

	public static void main(String[] args) {

		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(args[0]);
			prop.load(input);
			MessageSender ms = new MessageSender(prop);

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
