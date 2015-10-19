package com.eislab.ipso.rockbolt;


public class RockboltMonitorAndControlApp {

	public static void main(String[] args) {

		String rdIP = "fdfd:55::80fe";
		String rdPort = "5683";
		String rdURL = "coap://[IP]:PORT/rd";
		
		if(args.length > 0) {
			rdIP = new String( args[0]);
			
		}
		if(args.length > 1) {
			rdPort = new String( args[1]);
		}

		rdURL = rdURL.replace("IP", rdIP).replace("PORT", rdPort);
			
//		RockboltLwm2mMonitor mon = new RockboltLwm2mMonitor("coap://[fcfc::01]:5683/rd");
		RockboltLwm2mMonitor mon = new RockboltLwm2mMonitor(rdURL);
		
		

					
		while(true)
		{
			try {
				mon.run();
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	
}
