package ca.mcgill.ecse211.sensor;


import ca.mcgill.ecse211.Final_Project.Main;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import lejos.robotics.SampleProvider;


/**
 * This class is responsible for provides filtered light data to light localizer
 *
 *
 */
public class LineDetector1 extends Thread {

	private SampleProvider sample;
	// private LightLocalizer ll;
	private OdometryCorrection oc;
	private float[] lightData;
	private float lightLevel = -1f;
	private Odometer odo;
	// adjust for better performance 
	private final float LIGHT_THRESHOLD = 0.35f;
	private final double sensorCorrection = 0.3; 

	/**
	 * 
	 * @param filteredSample
	 * @param filteredLightData a float array to contain the filtered light data.
	 * @param oc Odometry Correction
	 * @param odo Odometer
	 */
	public LineDetector1(SampleProvider filteredSample, float[] filteredLightData, OdometryCorrection oc,
			Odometer odo) {
		this.sample = filteredSample;
		this.lightData = filteredLightData;
		this.oc = oc;
		this.odo = odo;
		// this.ll = ll;
	}

	public void run() {
		while (true) {
			sample.fetchSample(lightData, 0);
			if (lightData[0] > 0f) {
				oc.setLightLevel1(lightData[0]);
				lightLevel = lightData[0];
			}
			// detect lines and record data
			if (lightLevel < LIGHT_THRESHOLD && Main.correctionON) {
				oc.x1 = odo.getXYT()[0] + sensorCorrection;
				oc.y1 = odo.getXYT()[1] + sensorCorrection;
			}
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			} // Poor man's timed sampling
		}
	}

	/*
	 * returns light level as a float.
	 */
	public float getLightLevel() {
		return lightLevel;
	}
}
