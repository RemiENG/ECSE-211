package ca.mcgill.ecse211.sensor;


import ca.mcgill.ecse211.Final_Project.Main;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import lejos.robotics.SampleProvider;

/**
 * Provides filtered light data to light localizer
 *
 *
 */
public class LineDetector2 extends Thread {

	private SampleProvider sample;
	// private LightLocalizer ll;
	private OdometryCorrection oc;
	private float[] lightData2;
	private float lightLevel2 = -1f;
	private Odometer odo;
	private final float LIGHT_THRESHOLD = 0.35f;
	private final double sensorCorrection = 0.0;

	/**
	 * 
	 * @param filteredSample
	 * @param filteredLightData a float array of the filtered light data
	 * @param oc Odometry correction
	 * @param odo Odometer
	 */
	public LineDetector2(SampleProvider filteredSample, float[] filteredLightData, OdometryCorrection oc,
			Odometer odo) {
		this.sample = filteredSample;
		this.lightData2 = filteredLightData;
		this.oc = oc;
		this.odo = odo;
		// this.ll = ll;
	}

	public void run() {
		while (true) {
			sample.fetchSample(lightData2, 0);
			if (lightData2[0] > 0f) {
				oc.setLightLevel2(lightData2[0]);
				lightLevel2 = lightData2[0];
			}
			// detect lines and record data
			if (lightLevel2 < LIGHT_THRESHOLD && Main.correctionON) {
				oc.x2 = odo.getXYT()[0] + sensorCorrection;
				oc.y2 = odo.getXYT()[1] + sensorCorrection;
			}
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			} // Poor man's timed sampling
		}
	}

	/*
	 * return the second light level as a float
	 */
	public float getLightLevel2() {
		return this.lightLevel2;
	}

}
