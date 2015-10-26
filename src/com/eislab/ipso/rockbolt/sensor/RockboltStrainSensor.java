package com.eislab.ipso.rockbolt.sensor;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonParser;

import org.eclipse.californium.core.CoapResponse;
import com.eislab.ipso.rockbolt.HistorianAgent;
import com.eislab.ipso.rockbolt.Rockbolt;


public class RockboltStrainSensor extends AbstractSensorClient {

//	private ThingspeakAgent thingspeakAgent;
	private HistorianAgent historianAgent;
	private int prevAlarm = 0; 
//	private Rockbolt parentRockbolt;
	
	
	public RockboltStrainSensor(String RockboltURI, Rockbolt rockbolt) {
		super(RockboltURI);
		this.historianAgent = rockbolt.getHistorianAgent();
//		this.parentRockbolt = rockbolt;
	}

	@Override
	public int processInput(CoapResponse response) {

		String resp = response.getResponseText();
//		System.out.println("CoAP Resp: " + resp);

		if (resp != null) {
			if (!resp.equals("")) {
//				System.out.println(payload);
				Double strain = null;
				int alarm = 0;

//				JSONObject obj;
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
											if (objectMap.get("n").equals("strain") )
												strain = Double.parseDouble(objectMap.get("v"));
											else if (objectMap.get("n").equals("alarm"))
												alarm = objectMap.get("v").equals("1") ? 1 : 0;
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
				
				if (strain > 7500) {
					alarm = 2;
				}
				
				if(prevAlarm != alarm) {
					prevAlarm = alarm;

					historianAgent.setStrain_alarm(alarm);
//					setChanged();
//					notifyObservers("strain_alarm=" + alarm);
				}
				
				historianAgent.setStrain(strain);

			}
		}

		return resp.length();

	}

	@Override
	public String forwardOutput(String uri, String payload) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
