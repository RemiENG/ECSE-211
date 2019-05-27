package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;

/**
 * 
 * 
 *
 */
public class LightLocalizer extends Thread {
	private Navigation nav;
	private Odometer odo;

	private final double SENSOR_OFFSET = 16.9; // The actual length won't give good results.
	private final float LIGHT_THRESHOLD = 0.37f;

	private int line_count = 0; // We will detect 4 lines in this lab
	private double[] angles = new double[4];

	private float light_level;

	public boolean done = false;

	public LightLocalizer(Navigation nav, Odometer odo) {
		this.nav = nav;
		this.odo = odo;
	}

	public void run() {
		nav.rotate(360, true);
		localize();
	}

	/**
	 * Where the magic happens. Get the heading (angle from 0 to 359.999) at the 4
	 * lines before computing the robot's position.
	 */
	private void localize() {
		// Start by finding all the lines
		sleepThread(1); // sleep the thread for a second to avoid false positives right off the bat.
		while (line_count != 4) {
			waitForLine();

			angles[line_count++] = odo.getXYT()[2]; // Record the angle at which we detected the line.

			sleepThread(0.5f); // wait for a second to avoid multiple detections of the same line.
		}

		nav.rotate(0, false);
		// We found all the lines, compute the position.
		computePosition();
	}

	/**
	 * Computes the position of the robot using the angles found in the localize()
	 * method.
	 */
	private void computePosition() {
		/*
		 * Here we know that we are always rotating in the same direction
		 * (counter-clockwise) so we know that the first and third lines will be for the
		 * x position and the second and last will be for the y position.
		 * 
		 * We also assume that both coordinates of the robot will always be negative.
		 */

		double x_pos = -SENSOR_OFFSET * Math.cos((angles[2] - angles[0]) / 2);
		double y_pos = -SENSOR_OFFSET * Math.cos((angles[3] - angles[1]) / 2);

		// Both negative.
		if (x_pos > 0) {
			x_pos *= -1;
		}

		if (y_pos > 0) {
			y_pos *= -1;
		}

		odo.setX(x_pos);
		odo.setY(y_pos);

		// Notify the main method that we are done.
		done = true;
	}

	/**
	 * This method stops the localizer until the light level becomes lower that the
	 * threshold level, meaning we detected a line.
	 */
	private void waitForLine() {
		while (getLightLevel() > LIGHT_THRESHOLD && getLightLevel() > 0.1f) {
		}
		;
		Sound.beep();
		return;
	}

	/**
	 * Not really necessary, this is just to make the risingEdge and fallingEdge
	 * methods more readable.
	 */
	private void sleepThread(float seconds) {
		try {
			Thread.sleep((long) (seconds * 1000));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/*
	 * Getters and Setters for the light_level, used by colorPoller
	 */
	public synchronized float getLightLevel() {
		return light_level;
	}

	public synchronized void setLightLevel(float new_level) {
		light_level = new_level;
	}
}
