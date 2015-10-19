package com.eislab.ipso.rockbolt;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;


public class RockboltLwm2mMonitor/* extends Observable*/ implements Runnable{


	private ArrayList<String> connectedDevices = new ArrayList<String>();
	private ArrayList<Rockbolt> rockboltObjects = new ArrayList<Rockbolt>();
	private CoapClient leshanClient ;
//	private ThingspeakAgent thingspeakAgent = new ThingspeakAgent("https://api.thingspeak.com/update/");
	
	
	public RockboltLwm2mMonitor(String rdURL) {
		// TODO Auto-generated constructor stub

		if (rdURL.isEmpty())
			System.exit(1);

		leshanClient = new CoapClient(rdURL);
			
	}
	
	@Override
	public void run() {

		ArrayList<String> devices = new ArrayList<String>();
		
		CoapResponse response = leshanClient.get();
		
		if(response == null) {
			
		} else {
			String responseString = response.getResponseText();
			
						
			if(responseString.isEmpty()) {
				System.err.println("Empty Response string from the Leshan RD");
				System.exit(1);
			} else {
					
				JsonParser parser = Json.createParser(new StringReader(responseString));
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
										String address = objectMap.get("address").replace("/", "");
										String port = objectMap.get("port");
										devices.add("[" + address + "]:" + port);
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
			}
					
			
			//for (Iterator<String> iterator = devices.iterator(); iterator.hasNext();) {
			Iterator<String> iterator = devices.iterator(); 
			for (int ite = 0; ite < devices.size(); ite++) {
				
				String device = iterator.next();
				
				if(connectedDevices.contains(device)) {
					//skip this device as it is already registered as a connected device of this monitor
				} else {
							
					CoapClient deviceClient = new CoapClient("coap://" + device + "/.well-known/core");
					CoapResponse deviceGet = deviceClient.get();
					
					if (deviceGet == null) {
						// no response remove from iterator, can be added again on next attempt
	
						System.out.println("No /.well-known/core response from: coap://" + device);
//						devices.remove(device);
//						iterator.remove();
						
					} else {
						String deviceResponse = deviceGet.getResponseText();
						
						if (deviceResponse.toLowerCase().contains("strain")) {
							
							System.out.println("Unregistered strain device found: coap://" + device);
							System.out.println("Register new device");									
							connectedDevices.add(device);
							rockboltObjects.add(new Rockbolt(device, "coap://sm-pc777.sm.ltu.se:61616/lime/storage/", this ));
						} else {
							// Remove the current element from the iterator and the list.
//					        iterator.remove();
//							devices.remove(device);
						}
					}
				}
			}
			
			
		}
		
	}
	
	public void deregisterRockbolt(Rockbolt rockbolt) {
		rockboltObjects.remove(rockbolt);
		connectedDevices.remove(rockbolt.device);
	}

}
