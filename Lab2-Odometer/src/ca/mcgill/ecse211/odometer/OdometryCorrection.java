/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	// Set up color sensor
	private Port sensorPort = LocalEV3.get().getPort("S1");
	private static EV3ColorSensor RGBSensor;
	private static SampleProvider RGBSample;
	public float[] RGBData;

	private static double sensorOffset = 5.5; //actual 7.6 
	private static double lightLimit = 0.3; // less than this value means black line (may need adjustment)
	private static double tileSize = 30.48;
	private static int maxTiles = 4;

	/**
	 * This is the default class constructor. An existing instance of the odometer
	 * is used. This is to ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection() throws OdometerExceptions {

		odometer = Odometer.getOdometer();
		RGBSensor = new EV3ColorSensor(sensorPort);
		RGBSample = RGBSensor.getRedMode();// can switch color for different readings
		RGBData = new float[RGBSensor.sampleSize()]; // create a buffer for sensor data
	}

	/**
	 * Here is where the odometer correction code should be run.
	 * 
	 * @throws OdometerExceptions
	 */
	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO Trigger correction (When do I have information to correct?)
			// Get RGB sensor data
			RGBSample.fetchSample(RGBData, 0);
			double currentLight = (double) RGBData[0];
			// copy odometer data for simplicity
			double x = odometer.getXYT()[0];
			double y = odometer.getXYT()[1];
			double theta = odometer.getXYT()[2];

			// A correction is needed if a line is crossed
			boolean needsCorrection = false;
			if (currentLight < lightLimit) {
				Sound.beep();
				needsCorrection = true;
			}
			// TODO Calculate new (accurate) robot position

			// TODO Update odometer with new calculated (and more accurate) vales

			// Make correction if needed
			if (needsCorrection) {
				// if heading is +y
				if (theta < 10 || theta > 350) {
					//correct theta
					odometer.setTheta(0);
					//if first correction
					if (y < tileSize / 2 - sensorOffset) {
						odometer.setY(-sensorOffset);
					} else {
						//after first correction
						for (int i = 1; i <= maxTiles; i++) {
							if (y < (double) i * tileSize + sensorOffset) {
								odometer.setY((double) i * tileSize - sensorOffset);
								break;
							}
						}
					}
				}
				// if heading is -y
				else if (theta < 190 && theta > 170) {
					odometer.setTheta(180);
					if (y < tileSize / 2 + sensorOffset) {
						odometer.setY(sensorOffset+1.5);
					} else {
						for (int i = maxTiles; i >= 1; i--) {
							if (y > (double) i * tileSize - sensorOffset-5) {
								odometer.setY((double) i * tileSize + sensorOffset);
								break;
							}
						}
					}
				}

				// if heading is +x
				else if (theta < 100 && theta > 80) {
					odometer.setTheta(90);
					if (x < tileSize / 2 - sensorOffset) {
						odometer.setX(-sensorOffset);
					} else {
						for (int i = 1; i <= maxTiles; i++) {
							if (x < (double) i * tileSize + sensorOffset) {
								odometer.setX((double) i * tileSize - sensorOffset);
								break;
							}
						}
					}
				}
				// if heading is -x
				else if (theta < 280 && theta > 260) {
					odometer.setTheta(270);
					if (x < tileSize / 2 + sensorOffset) {
						odometer.setX(sensorOffset+1);
					} else {
						for (int i = maxTiles; i >= 1; i--) {
							if (x > (double) i * tileSize - sensorOffset) {
								odometer.setX((double) i * tileSize + sensorOffset);
								break;
							}
						}
					}
				}
				needsCorrection = false; // reset after correction is made
			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here
				}
			}
		}
	}
}
