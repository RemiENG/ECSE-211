package ca.mcgill.ecse211.lab4;

import lejos.hardware.Button;
import lejos.hardware.Sound;

/**
 * This class uses the ultrasonic sensor to find the robots heading wrt to +y
 * axis
 * 
 */
public class UltrasonicLocalizer extends Thread {
	private Navigation nav;
	private Odometer odo;

	// States for the localization mode
	public enum Mode {
		FALLING_EDGE, RISING_EDGE, INVALID
	};

	private Mode mode = Mode.INVALID; // default value

	// Localization Constants
	private final float RISING_EDGE_DIST_THRESHOLD = 30.f;
	private final float FALLING_EDGE_DIST_THRESHOLD = 70.f;

	private float distance = -1.f; // distance from ultrasonic sensor
	// angles used to compute correct heading
	private double theta1 = -1.0;
	private double theta2 = -1.0;
	// Constants to compute correct heading (we may need to adjust these for better
	// performance)
	private final double RISING_EDGE_CONST = 45;
	private final double FALLING_EDGE_CONST = 225;

	public boolean finished = false; // To know when were finished here

	
	public UltrasonicLocalizer(Mode mode, Navigation nav, Odometer odo) {
		this.mode = mode;
		this.nav = nav;
		this.odo = odo;
	}

	/**
	 * FSM with falling edge and rising edge localization states
	 */
	public void run() {
		nav.rotate(360, true);
		switch (mode) {
		case FALLING_EDGE:
			fallingEdge();
			break;
		case RISING_EDGE:
			risingEdge();
			break;
		case INVALID:
			System.out.print("Invalid");
			break;
		}
	}

	private void fallingEdge() {
		wait(mode);
		theta1 = odo.getXYT()[2]; // Record current odometer theta
		nav.rotate(-360, true); // rotate the robot 360 deg

		sleepThread(3); // Wait for a bit.

		wait(mode);
		// nav.rotate(0, false);
		theta2 = odo.getXYT()[2];
		;

		computeOrientation();
	}

	private void risingEdge() {
		wait(mode);
		theta1 = odo.getXYT()[2];
		; // Record the current theta.

		// Rotate in the other direction.
		nav.rotate(-360, true);

		sleepThread(3); // Wait for a bit.

		wait(mode);
		nav.rotate(0, false);
		theta2 = odo.getXYT()[2];
		;

		computeOrientation();
	}

	/**
	 * Computes the orientation of the robot using the recorded angles.
	 */
	private void computeOrientation() {
		double new_theta = -1;
		switch (mode) {
		case FALLING_EDGE:
			new_theta = FALLING_EDGE_CONST - (theta1 + theta2) / 2;
			break;
		case RISING_EDGE:
			new_theta = RISING_EDGE_CONST - (theta1 + theta2) / 2;
			break;
		case INVALID:
			break;
		}

		odo.update(0, 0, new_theta);

		Button.waitForAnyPress();
		nav.turnTo(-odo.getXYT()[2]);
		Button.waitForAnyPress();
		finished = true;
	}

	/**
	 * Used by the ultrasonic poller to pass the distance.
	 * 
	 * @param dist
	 *            the distace read by the ultrasonic poller
	 */
	public synchronized void setDist(float dist) {
		this.distance = dist;
	}

	/**
	 * distance read by the ultrasonic sensor
	 * 
	 * @return distance (cm)
	 */
	public synchronized float getDist() {
		return distance;
	}

	/**
	 * Waits to find rising or falling edge
	 * 
	 * @param m
	 */
	private void wait(Mode m) {
		if (m == Mode.FALLING_EDGE) {
			while (getDist() > FALLING_EDGE_DIST_THRESHOLD) {
			}
			; // Wait until we capture a falling edge.
			Sound.beep();
			return;
		} else {
			while (getDist() < RISING_EDGE_DIST_THRESHOLD) {
			}
			; // Wait until we capture a rising edge.
			Sound.beep();
			return;
		}
	}

	/**
	 * puts thread to sleep (!!) careful, time is in seconds
	 * 
	 * @param seconds
	 */
	private void sleepThread(float seconds) {
		try {
			Thread.sleep((long) (seconds * 1000));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
