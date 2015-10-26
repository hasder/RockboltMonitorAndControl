package com.eislab.ipso.rockbolt;

import java.util.Observable;

import com.eislab.ipso.rockbolt.sensor.RockboltAccRmsSensor;
import com.eislab.ipso.rockbolt.sensor.RockboltStrainSensor;

public class Rockbolt extends Observable {

	// ThingspeakAgent thingspeakAgent;
	HistorianAgent historianAgent;

	public HistorianAgent getHistorianAgent() {
		return historianAgent;
	}

	// public ThingspeakAgent getThingspeakAgent() {
	// return thingspeakAgent;
	// }

	AlarmAgent alarmAgent;
	RockboltStrainSensor strainSensor;
	RockboltAccRmsSensor accRmsSensor;
	public String device = "";
	Thread historianThread;
	Thread strainThread;
	Thread accRmsThread;
	RockboltLwm2mMonitor monitor;

	public Rockbolt(String device, String historianURI,
			RockboltLwm2mMonitor monitor) {

		this.monitor = monitor;
		this.device = device;

		alarmAgent = AlarmAgent.getInstance();
		alarmAgent.initAlarms(this);
		alarmAgent.registerRockbolt(this);

		// thingspeakAgent = new
		// ThingspeakAgent("https://api.thingspeak.com/update/");
		// new Thread(thingspeakAgent).start();

		historianAgent = new HistorianAgent(historianURI + this.getURN(), this);
		historianThread = new Thread(historianAgent);
		historianThread.start();

		strainSensor = new RockboltStrainSensor("coap://" + device + "/strain", this);//fake resource until real one available
//		strainSensor = new RockboltStrainSensor("coap://" + device + "/3200/0/5500", this);//real strain resource (not yet implemented in mulle)
		strainSensor.addObserver(alarmAgent);
		// strainThread = new Thread(strainSensor);
		// strainThread.start();

		accRmsSensor = new RockboltAccRmsSensor("coap://" + device + "/acc/rms", this);
		accRmsSensor.addObserver(alarmAgent);
		// accRmsThread = new Thread(accRmsSensor);
		// accRmsThread.start();

	}

	public void endObject() {

		alarmAgent.deregisterRockbolt(this);

		if (historianThread != null) {
			try {
				historianAgent.terminate();
				historianThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// if (strainSensor != null) {
		// strainSensor.terminate();
		// }

		// if (accRmsSensor != null) {
		// accRmsSensor.terminate();
		// }

		this.monitor.deregisterRockbolt(this);
	}

	public String getURN() {
		String ipParts[] = this.device
				.substring(this.device.indexOf("["), this.device.indexOf("]"))
				.replace("[", "").replace("]", "").split(":");

		String urn = "unknown";

		if (ipParts.length > 4) {
			urn = "urn:dev:mac:" + ipParts[ipParts.length - 4]
					+ ipParts[ipParts.length - 3] + ipParts[ipParts.length - 2]
					+ ipParts[ipParts.length - 1];
		}
		return urn;
	}

}
