package com.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.common.EventData;
import com.google.gson.Gson;

/**
* This class processes log event data.
* 
* @author  Mandar Pardeshi
* @version 1.0
* @since   2022-09-01 
*/
public class EventDataProcessor {
	
	private static final Logger logger = LogManager.getLogger(EventDataProcessor.class);
	
	public static void main(String[] args) {
		
		System.out.println("Please enter windows file path (e.g. D:\\logfile.txt) : ");
		String filepath = new Scanner(System.in).nextLine();
						
		EventDataProcessor pData = new EventDataProcessor();		
		Stream<String> evDataStream = pData.getLogData(filepath);		
		if(null != evDataStream){
			logger.debug("Log data not present for further processing");
			Map<String, EventData> eventTableMap = pData.processLogData(evDataStream);
			evDataStream.close();
			pData.storeDataInDB(eventTableMap);
		}		
	}
	
	/**
	 * method to get log data from file
	 * @param filepath
	 * @return
	 */
	public Stream<String> getLogData(String filepath){
		Stream<String> fstream = null;
		try {
			fstream = Files.lines(Paths.get(filepath));
		} catch (IOException e) {
			logger.error("File not found.");
		}
		return fstream;
	}
	
	/**
	 * method to process log data
	 * 
	 * @param fstream
	 * @return
	 */
	public Map<String, EventData> processLogData(Stream<String> fstream){
			
			Map<String, EventData> eventTableMap = new HashMap<String, EventData>(); 
			fstream.forEach(line -> {
				EventData logentry = new Gson().fromJson(line, EventData.class);
				if(eventTableMap.containsKey(logentry.getId())){
					logger.debug("Log entry already presents in Hashmap");
					EventData storedLogentry = eventTableMap.get(logentry.getId());
					long duration = 0;
					if("FINISHED".equals(storedLogentry.getState())){
						duration = storedLogentry.getTimestamp() - logentry.getTimestamp();
					}else if ("STARTED".equals(storedLogentry.getState())){
						duration = logentry.getTimestamp() - storedLogentry.getTimestamp();
					}	
					storedLogentry.setDuration(duration);
					if(duration>4){
						storedLogentry.setAlert(true);
						logger.info("Alert : ID \"" + logentry.getId()+"\" took more than 4 ms");
						System.out.println("Alert : ID \"" + logentry.getId()+"\" took more than 4 ms");
					}
					eventTableMap.computeIfPresent(logentry.getId(), (key, val) -> storedLogentry);
				}else{
					eventTableMap.put(logentry.getId(), logentry);
					logger.debug("Log entry added in Hashmap");
				}
			});
			return eventTableMap;
	
	}

	/**
	 * method to display table entries & store into DB 
	 * @param eventTableMap
	 */
	public boolean storeDataInDB(Map<String, EventData> eventTableMap){
		
		boolean stored= false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("Displying all entries which are going to get stored in DB:");
		
		eventTableMap.entrySet().stream().forEach(eventEntry -> {EventData eventData = eventEntry.getValue();
		sb.append("\nfEvent Id: ");
		sb.append(eventData.getId());
		sb.append(", Event Duration: ");
		sb.append(eventData.getDuration());
		sb.append(" ms, Type: ");
		sb.append(eventData.getType());
		sb.append(", Host: ");
		sb.append(eventData.getHost());
		sb.append(", Alert: ");
		sb.append(eventData.isAlert());
		});
		
		logger.info(sb.toString());	
		System.out.println(sb.toString());
		
		//TODO: Store map (which includes duration & alert flag) in database. 
		return stored;
	}
	
}
