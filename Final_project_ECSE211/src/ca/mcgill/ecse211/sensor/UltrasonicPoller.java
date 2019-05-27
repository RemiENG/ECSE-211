package ca.mcgill.ecse211.sensor;

import lejos.robotics.SampleProvider;
/**
 * Provides filtered data to us localizer
 * 
 *
 */
public class UltrasonicPoller extends Thread {

	private UltrasonicLocalizer ul;
	private SampleProvider sample;
	private float[] usData;
	private float distance;
	

	public UltrasonicPoller(SampleProvider filteredUS, float[] filteredUsData, UltrasonicLocalizer ul) {
		this.sample = filteredUS;
		this.usData = filteredUsData;
		this.ul = ul;
	}

	public void run() {
		//fetch filtered samples until us localization is finished
		while (true) {
			sample.fetchSample(usData, 0);
			ul.setDist(usData[0] * 100f);
			distance = usData[0];
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			} // Poor man's timed sampling
		}
	}

	/*
	 * returns the distance as a float.
	 */
	public synchronized float getDistance() {;
		return this.distance * 100f;
	}
}