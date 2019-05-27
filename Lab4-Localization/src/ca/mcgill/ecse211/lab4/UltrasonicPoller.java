package ca.mcgill.ecse211.lab4;

import lejos.robotics.SampleProvider;

public class UltrasonicPoller extends Thread {

	private UltrasonicLocalizer ul;
	private SampleProvider sample;
	private float[] usData;
	private float distance;

	public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicLocalizer ul) {
		this.sample = us;
		this.usData = usData;
		this.ul = ul;
	}

	public void run() {
		// Terminate whenever the ultrasonic localizer is done to spare system
		// resources.
		while (!ul.finished) {
			sample.fetchSample(usData, 0);
			ul.setDist(usData[0] * 100.f);
			distance = usData[0];
			try {
				Thread.sleep(30);
			} catch (Exception e) {
			} // Poor man's timed sampling
		}
	}

	public float getDistance() {
		return this.distance;
	}
}