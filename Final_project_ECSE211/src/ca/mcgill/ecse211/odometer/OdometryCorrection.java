package ca.mcgill.ecse211.odometer;

import ca.mcgill.ecse211.Final_Project.Main;
import lejos.hardware.Sound;

/**
 * 
 * This class is responsible for performing Odometery Correction.
 *
 */

public class OdometryCorrection implements Runnable {

	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;

	private double sensorOffset = 2.5; // actual 7.6
	private final double sensorSeperation = 16.5;
	private double tileSize = 30.48;
	private final int maxTiles = 14;
	private final double tolerance = 5; 
	public boolean needsCorrection = false;
	// position of detected lines
	public double x1 = -1; // sensor 1 (right)
	public double y1 = -1;
	public double x2 = -1; // sensor 2 (left)
	public double y2 = -1;

	private float light_level;
	private float light_level2;

	/**
	 * This is the default class constructor. An existing instance of the odometer
	 * is used. This is to ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection() throws OdometerExceptions {

		odometer = Odometer.getOdometer();

	}

	/**
	 * Here is where the odometer correction code should be run.
	 * 
	 * @throws OdometerExceptions
	 */
	public void run() {
		long correctionStart, correctionEnd;
		sleepThread(1f);
		while (true) {
			correctionStart = System.currentTimeMillis();
			Sound.setVolume(15);
			double x = odometer.getXYT()[0];
			double y = odometer.getXYT()[1];
			double theta = odometer.getXYT()[2];

			// Make correction if needed
			if (needsCorrection() && Main.correctionON) {
				Sound.beepSequenceUp();
				verifyInputs(); //check if sensor data makes sense
				double deltaY = y1 - y2;
				double deltaX = x1 - x2;

				// if heading is +y
				if (theta < 20 || theta > 340) {
					// correct theta
					double correctTheta = Math.toDegrees(Math.atan(((deltaY) / sensorSeperation)));
					odometer.setTheta(correctTheta);
					for (int i = 1; i <= maxTiles; i++) {
						if (y < (double) i * tileSize + sensorOffset + tolerance) {
							double yOffset = 0.5 * sensorSeperation * Math.sin(Math.toRadians(correctTheta));
							odometer.setY((double) i * tileSize - sensorOffset - Math.abs(yOffset));
							break;
						}
					}
				}

				// if heading is -y
				else if (theta < 190 && theta > 170) {
					double correctTheta = Math.toDegrees(Math.atan(((deltaY) / sensorSeperation)));
					odometer.setTheta(-correctTheta + 180);
					for (int i = maxTiles; i >= 1; i--) {
						if (y > (double) i * tileSize - sensorOffset - tolerance) {
							double yOffset = 0.5 * sensorSeperation * Math.sin(Math.toRadians(correctTheta));
							odometer.setY((double) i * tileSize + sensorOffset + Math.abs(yOffset));
							break;
						}
					}
				}

				// if heading is +x
				else if (theta < 100 && theta > 80) {
					double correctTheta = Math.toDegrees(Math.atan(((deltaX) / sensorSeperation)));
					odometer.setTheta(correctTheta + 90);
					for (int i = 1; i <= maxTiles; i++) {
						if (x < (double) i * tileSize + sensorOffset + tolerance) {
							double yOffset = 0.5 * sensorSeperation * Math.sin(Math.toRadians(correctTheta));
							odometer.setX((double) i * tileSize - sensorOffset - Math.abs(yOffset));
							break;
						}
					}
				}

				// if heading is -x
				else if (theta < 280 && theta > 260) {
					double correctTheta = Math.toDegrees(Math.atan(((deltaX) / sensorSeperation)));
					odometer.setTheta(-correctTheta + 270);
					for (int i = maxTiles; i >= 1; i--) {
						if (x > (double) i * tileSize - sensorOffset - tolerance) {
							double yOffset = 0.5 * sensorSeperation * Math.sin(Math.toRadians(correctTheta));
							odometer.setX((double) i * tileSize + sensorOffset + Math.abs(yOffset));
							break;
						}
					}
				}
				
				// reset x1 y1 x2 y2
				x1 = -1;
				y1 = -1;
				x2 = -1;
				y2 = -1;
				needsCorrection = false;
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

	public boolean needsCorrection() {
		// A correction is needed if a line is crossed
		//boolean needsCorrection = false;
		if (x1 != -1 && x2 != -1 && y1 != -1 && y2 != -1) {
			needsCorrection = true;
		}
		return needsCorrection;
	}

	/**
	 * checks if inputs (x1,x2,y1,y2) are valid. Sets them to 0 if they are not
	 */
	private void verifyInputs() {
		if (Math.abs(x1 - x2) > sensorSeperation) {
			x1 = 0;
			x2 = 0;
			Sound.buzz();
		}
		if (Math.abs(y1 - y2) > sensorSeperation) {
			y1 = 0;
			y2 = 0;
			Sound.buzz();
		}
	}


	public synchronized void setPos1(double x, double y) {
		x1 = x;
		y1 = y;
	}

	public synchronized double[] getPos1() {
		double[] pos = { x1, y1 };
		return pos;
	}

	/**
	 * Synced with RGB Poller
	 * 
	 * @return light level
	 */

	public synchronized float getLightLevel1() {
		return light_level;
	}

	/**
	 * Synced with RGB Poller
	 * 
	 * @param level value of the level as float
	 */

	public synchronized void setLightLevel1(float level) {
		light_level = level;
	}

	public synchronized float getLightLevel2() {
		return light_level2;
	}

	/**
	 * Synced with RGB Poller
	 * 
	 * @param level
	 */

	public synchronized void setLightLevel2(float level) {
		light_level2 = level;
	}

	private void sleepThread(float seconds) {
		try {
			Thread.sleep((long) (seconds * 1000f));
		} catch (Exception e) {
		}
	}
}
