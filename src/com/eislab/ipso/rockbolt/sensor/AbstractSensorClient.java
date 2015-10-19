package com.eislab.ipso.rockbolt.sensor;

import java.util.Observable;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public abstract class AbstractSensorClient extends Observable {
	public String targetURI = null, oldtargetURI = null;
	boolean observe = false, loop = false;
	public boolean runThread = true;

	CoapObserveRelation observeRelation ;
//	private Request request;

	/*
	 * Application entry point.
	 */
	public AbstractSensorClient(String uri) {
		targetURI = new String(uri);
		oldtargetURI = new String(uri);
		setupObserve();
	}

	public abstract String forwardOutput(String uri, String payload);

	public abstract int processInput(CoapResponse response); 

	public void cancelObserve() {
		observeRelation.proactiveCancel();
	}
	
	public void setupObserve() {
		System.out.println("Observe URI: " + targetURI);

		CoapClient client = new CoapClient(targetURI);
		client.setTimeout(3000);
		observeRelation = client.observe(new CoapHandler() {
			
			@Override
			public void onLoad(CoapResponse arg0) {
				processInput(arg0);
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				System.out.println("error. What was the damned cause?!?!?!?!??!?!");
				
			}
			
		});

	}
	
	public boolean setTargetURI(String uri) {
		targetURI = new String(uri);
		System.out.println("URI set ('" + uri + "')");
		return true;
	}

}
