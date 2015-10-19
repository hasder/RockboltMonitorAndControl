package com.eislab.ipso.rockbolt;

import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapClient;

public class AlarmAgent implements Observer, Runnable {

//	private int strainAlarm = 0;
//	private int acc_rmsAlarm = 0;
//	private ArrayList<String> deviceList = new ArrayList<String>();
//	private ArrayList<Rockbolt> rockboltList = new ArrayList<Rockbolt>();
	private Map<Rockbolt, Integer> rockboltStrainAlarm = new HashMap<Rockbolt, Integer>();
	private Map<Rockbolt, Integer> rockboltAccAlarm = new HashMap<Rockbolt, Integer>();

	private static AlarmAgent alarmAgent = null;

	private AlarmAgent() {
		
	}
	
	public static AlarmAgent getInstance() {
		if (alarmAgent == null) {
			alarmAgent = new AlarmAgent();
			new Thread(alarmAgent).start();
		}
		return alarmAgent;
	}
	
	public void registerRockbolt(Rockbolt rockboltDevice) {
//		deviceList.add(rockboltDevice.device);
//		rockboltList.add(rockboltDevice);
		rockboltStrainAlarm.put(rockboltDevice, 0);//false
		rockboltAccAlarm.put(rockboltDevice, 0);
	}
	
	public void deregisterRockbolt(Rockbolt rockboltDevice) {
//		deviceList.add(rockboltDevice.device);
//		rockboltList.add(rockboltDevice);
		rockboltStrainAlarm.remove(rockboltDevice);//false
		rockboltAccAlarm.remove(rockboltDevice);
	}
	
	public void initAlarms(Rockbolt rockboltDevice) {
		CoapClient turnOffAllAlarms = new CoapClient("coap://" + rockboltDevice.device + "/3311/1/5850");
		turnOffAllAlarms.put("false", 0);
		turnOffAllAlarms = new CoapClient("coap://" + rockboltDevice.device + "/3311/2/5850");
		turnOffAllAlarms.put("false", 0);
		turnOffAllAlarms = new CoapClient("coap://" + rockboltDevice.device + "/3311/3/5850");
		turnOffAllAlarms.put("false", 0);
		turnOffAllAlarms = new CoapClient("coap://" + rockboltDevice.device + "/3311/4/5850");
		turnOffAllAlarms.put("false", 0);
	}
	
	
	public int getStrainAlarm() {
		return strainAlarm;
	}


	public void setStrainAlarm(int strainAlarm) {
		this.strainAlarm = strainAlarm;
	}


//	public int getAcc_rmsAlarm() {
//		return acc_rmsAlarm;
//	}
//
//
//	public void setAcc_rmsAlarm(int acc_rmsAlarm) {
//		this.acc_rmsAlarm = acc_rmsAlarm;			
//	}

//	public void setDeviceListMonitor(RockboltLwm2mMonitor deviceMonitor) {
//		deviceMonitor.addObserver(this);
//	}
	
	private void spreadAlarm(String url, int value) {
		Date timenow = new Date();
		CoapClient spreadingAlarm = new CoapClient(url);
		spreadingAlarm.put(value > 0 ? "true" : "false", 0);
		System.out.println("Took " + ((new Date()).getTime() - timenow.getTime())  + " ms to send alarm to " + url);
	}

	Timer timedFunc;

	@Override
	public void update(Observable o, Object arg1) {
		timedFunc = new Timer();
		timedFunc.schedule( new TimerTask() {

			@Override
			public void run() {
				doAlarmRoutine(o, arg1);
			}},
			10);
		
	}
	

	int accAlarm = 0;
	int strainAlarm = 0;
	
	private Object doAlarmLock = new Object();
	 
	private void doAlarmRoutine(Observable o, Object arg1) {
		
		
		synchronized(doAlarmLock) {
			
			String arg1String = (String)arg1;
			System.out.println(arg1);
			
			//check what the notification is
			if(arg1String.startsWith("acc_alarm=")) {
				accAlarm = 0;
				Iterator<Entry<Rockbolt, Integer>> it = rockboltAccAlarm.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Rockbolt, Integer> rbIterator = it.next();
					Rockbolt rockboltTemp = rbIterator.getKey();
					if(rockboltTemp.accRmsSensor.equals(o)) {
						rockboltAccAlarm.put(rockboltTemp, Integer.parseInt(arg1String.substring(arg1String.indexOf("=")+1)));
						//TODO: do a flash here!!
//						this.spreadAlarm("coap://" + rockboltTemp.device + "/3311/2/5850", 0); 
					}
					
					if (rbIterator.getValue() > accAlarm) {
						accAlarm = rbIterator.getValue();
					}
				}
			} else if(arg1String.startsWith("strain_alarm=")) {
				strainAlarm = 0;
				Iterator<Entry<Rockbolt, Integer>> it = rockboltStrainAlarm.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Rockbolt, Integer> rbIterator = it.next();
					Rockbolt rockboltTemp = rbIterator.getKey();
					if(rockboltTemp.strainSensor.equals(o)) {
						rockboltStrainAlarm.put(rockboltTemp, Integer.parseInt(arg1String.substring(arg1String.indexOf("=")+1)));
					}
	
					if (rbIterator.getValue() > strainAlarm) {
						strainAlarm = rbIterator.getValue();
					}
				}
			} else {
				return;
			}
			
			Rockbolt rockbolt ;
			Iterator<Entry<Rockbolt, Integer>> it = rockboltStrainAlarm.entrySet().iterator();
			
			it = rockboltStrainAlarm.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Rockbolt, Integer> entry = it.next();
				rockbolt = entry.getKey();
				
				if(arg1String.startsWith("strain_alarm="))
					this.spreadAlarm("coap://" + rockbolt.device + "/3311/1/5850", strainAlarm);

				if(arg1String.startsWith("acc_alarm="))
					this.spreadAlarm("coap://" + rockbolt.device + "/3311/2/5850", accAlarm);
	//			this.spreadAlarm("coap://" + rockbolt.device + "/3311/2/5850", Integer.parseInt(arg1String.substring(arg1String.indexOf("=")+1)));
			}
		
		}
		
			
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
//		while(true) {
//			doAlarmRoutine(null, "");
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
//	@Override
//	public void update(Observable arg0, Object arg1) {
//			deviceList.add((String) arg1);
//			 setAcc_rmsAlarm(0);
//	}
}
