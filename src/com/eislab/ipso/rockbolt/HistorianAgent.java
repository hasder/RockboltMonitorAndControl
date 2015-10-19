package com.eislab.ipso.rockbolt;

import java.util.Date;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class HistorianAgent implements Runnable {

//	private final String USER_AGENT = "Mozilla/5.0";

	private String uri;
	private Rockbolt rockbolt;
	private volatile boolean running = true;

	private Double strain = 0.0;
	private int strain_alarm = 0;
	private Double acc_rms = 0.0;
	private int acc_rms_alarm = 0;

	public HistorianAgent(String agentUri, Rockbolt rockbolt) {
		this.uri = agentUri;
		this.rockbolt = rockbolt;
		
		System.out.println("SensorClient initialized ('" + rockbolt.device + "') -> " + agentUri);
		try {
			CoapClient testHistorian = new CoapClient(this.uri);
			CoapResponse response = testHistorian.get();
			if (response == null || !response.isSuccess()) {
				String historianResponse = doRockboltHistorianPuT(this.uri, (new Date()).getTime()/1000, this.rockbolt.getURN(), strain, strain_alarm, acc_rms, acc_rms_alarm);
				if (historianResponse == null) {
					running = false;
				}
			}
		} catch (Exception e) {
			running = false;
			System.err.println(e.getMessage());
		}

	}

	public Double getStrain() {
		return strain;
	}

	public void setStrain(Double strain) {
		this.strain = strain;
	}

	public int getStrain_alarm() {
		return strain_alarm;
	}

	public void setStrain_alarm(int strain_alarm) {
		this.strain_alarm = strain_alarm;
	}

	public Double getAcc_rms() {
		return acc_rms;
	}

	public void setAcc_rms(Double acc_rms) {
		this.acc_rms = acc_rms;
	}

	public int getAcc_rms_alarm() {
		return acc_rms_alarm;
	}

	public void setAcc_rms_alarm(int acc_rms_alarm) {
		this.acc_rms_alarm = acc_rms_alarm;
	}

	public void terminate() {
		running = false;
	}

	@Override
	public void run() {
		int backoff = 1;
		// TODO Auto-generated method stub
		while (running == true) {
 
			try {

				String historianResponse = doRockboltHistorianPuT(this.uri, (new Date()).getTime()/1000,this.rockbolt.getURN(), strain, strain_alarm, acc_rms, acc_rms_alarm);
				
				if (historianResponse == null) {
					System.out.println("No response or error response from Historian: increase back off to:" + ++backoff);
					if (backoff > 30) {
						backoff = 30;
					}
				} else {
					backoff = 1;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				// sleep for up to 15 seconds
				Thread.sleep(1000 * backoff);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String doRockboltHistorianPuT(String coapServerUri, Long time, String urn, double strain, int strain_alarm, double acc_rms, int acc_rms_alarm) {

		CoapClient coapClient = new CoapClient(coapServerUri);
		
		
		String histPayload = "{\n"
				+ "\"bt\":\"" + time + "\",\n"
				+ "\"bn\":\"" + urn + "\",\n"
				+ "\"e\":[\n" + "{\"n\":\"strain\", \"v\":\"" + strain + "\"},\n"
				+ "{\"n\":\"strain_alarm\", \"v\":\"" + strain_alarm + "\"},\n"
				+ "{\"n\":\"acc_y\", \"v\":\"" + acc_rms + "\"},\n"
				+ "{\"n\":\"acc_alarm\", \"v\":\"" + acc_rms_alarm + "\"}\n"
				+ "]}";

		System.out.println(uri);
		System.out.println(histPayload);
		coapClient.setTimeout(3000);
		CoapResponse coapResponse = coapClient.put(histPayload, MediaTypeRegistry.APPLICATION_JSON);
		return (coapResponse != null) ? coapResponse.getResponseText() : null;
	}
}
