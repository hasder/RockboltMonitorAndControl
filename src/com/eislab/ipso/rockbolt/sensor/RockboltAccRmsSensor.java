package com.eislab.ipso.rockbolt.sensor;

import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonParser;

import org.eclipse.californium.core.CoapResponse;
import com.eislab.ipso.rockbolt.HistorianAgent;
import com.eislab.ipso.rockbolt.Rockbolt;

public class RockboltAccRmsSensor extends AbstractSensorClient {

	private HistorianAgent historianAgent;
	
	Double averageAcc_Y = 0.0;
	Calendar timestamp = Calendar.getInstance();
	Date saved = timestamp.getTime();
	int prevAlarm = 0;
//	Rockbolt parentRockbolt;
	
	
	public RockboltAccRmsSensor(String RockboltURI, Rockbolt rockbolt) {
		super(RockboltURI);
//		this.parentRockbolt = rockbolt;
		historianAgent = rockbolt.getHistorianAgent();

	}

	@Override
	public int processInput(CoapResponse response) {

		String resp = response.getResponseText();
		if (resp != null) {
			if (!resp.equals("")) {
				
				Double acc_X = null;
				Double acc_Y = null;
				Double acc_Z = null;
				int alarm = 0;

				try {
				
				
					JsonParser parser = Json.createParser(new StringReader(resp));
					JsonParser.Event event;	
					
					while (parser.hasNext()) {
						event = parser.next();
						if (event.equals(JsonParser.Event.START_ARRAY)) {
							
							while (parser.hasNext()) {
								event = parser.next();
								if (event.equals(JsonParser.Event.END_ARRAY)) {
									//break out of this array walking while loop
									break;
								}
								
								if (event.equals(JsonParser.Event.START_OBJECT)) {
									Map<String, String> objectMap = new HashMap<String, String>();
									//start reading in all key/value pairs
									while (parser.hasNext()) {
			
										event = parser.next();
										//breakout clause
										if (event.equals(JsonParser.Event.END_OBJECT)) {
											
											//get the values gathered in the map.
											if (objectMap.get("n").equals("X") )
												acc_X = Double.parseDouble(objectMap.get("v"));
											else if (objectMap.get("n").equals("Y"))
												acc_Y = Double.parseDouble(objectMap.get("v"));
											else if (objectMap.get("n").equals("Z"))
												acc_Z = Double.parseDouble(objectMap.get("v"));
											else if (objectMap.get("n").equals("a"))
												alarm = Integer.parseInt(objectMap.get("v"));
											break;
										}
										
										//catch a key value pair and add to our memory dictionary
										if (event.equals(JsonParser.Event.KEY_NAME)) {
											String keyName = parser.getString();
											event = parser.next();
											String keyValue = parser.getString();
											objectMap.put(keyName, keyValue);
										}								
									}
								}
							}
							break;	
						}
					}
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if ((acc_Y > 0.4) || (acc_Y < -0.4)) {
					alarm = 2;
				}
							
				if(prevAlarm != alarm) {
					prevAlarm = alarm;
					historianAgent.setAcc_rms_alarm(alarm);
					System.out.println("getting ready to notify acc alarm");
					setChanged();
					notifyObservers("acc_alarm=" + alarm);
					System.out.println("Set acc_alarm and notify");
				}
				
				historianAgent.setAcc_rms(acc_Y);
		
				
//				this.forwardOutput(historianURI, resp);
			}
		}

		return resp.length();

	}
	@Override
	public String forwardOutput(String uri, String payload) {

//		System.out.println(payload);
		
		return null;


	}




}
