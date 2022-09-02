package com.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.common.EventData;
import com.core.EventDataProcessor;

/**
 * @author Mandar Pardeshi
 *
 */
public class TestEventData {

	EventDataProcessor pData;
	
	@Before
	public void init() {
		pData = new EventDataProcessor();
    }
	
	@Test
	public void testLogDataFound() {
		
		String filepath = "F:\\logfile.txt";		
		assertNotNull(pData.getLogData(filepath));
	}
	
	@Test
	public void testLogDataNotFound() {
		
		String filepath = "F:\\LogsFile.txt";
		assertNull(pData.getLogData(filepath));	
	}
	
	@Test
	public void testProcessLogData() throws IOException {
		Stream<String> fstream = Files.lines(Paths.get("F:\\logfile.txt"));
		Map<String, EventData> eventTableMap = pData.processLogData(fstream);
		assertFalse(eventTableMap.size()==0);
	}
	
	@Test
	public void testStoreDataInDB() {
		
		EventData eData = new EventData();
		eData.setId("123");
		eData.setDuration(12);
		eData.setHost("98022");
		eData.setType("APPLICATION_LOGS");
		
		Map<String, EventData> eventTableMap = new HashMap<String, EventData>();
		eventTableMap.put("123", eData);
		eventTableMap.put("456", eData);
		
		assertFalse(pData.storeDataInDB(eventTableMap));
	}

}
