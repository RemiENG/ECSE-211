package ca.mcgill.ecse211.sensor;

import ca.mcgill.ecse211.Final_Project.Main;
import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import lejos.hardware.Sound;

/**
 * This class uses the ultrasonic sensor to find the robots heading wrt to +y
 * axis
 * 
 * 
 */
public class UltrasonicLocalizer extends Thread {

	/**
	 *  Enum of all localization states
	 *
	 */
	public enum State {
		FALLING_EDGE_STATE, RISING_EDGE_STATE, INIT
	};

	private State state = State.INIT; // default value

	private Navigation nav;
	private Odometer odo;

	// Localization Constants (distance) (we may need to adjust these for better
	// performance)
	private final float RISING_EDGE_THRESHHOLD = 60f;
	private final float FALLING_EDGE_THRESHHOLD = 30f;
	private final float WALL_THRESHHOLD = 30f;

	// Constants to compute correct heading (we may need to adjust these for better
	// performance)
	private final double RISING_EDGE_CONST = 45 + 9;
	private final double FALLING_EDGE_CONST = 225 + 5;

	private final double TILE_SIZE = 30.48;

	// angles used to compute correct heading
	private double theta1 = -1.0;
	private double theta2 = -1.0;

	// filtered distance from us poller
	private float distance = -1f;

	public boolean finished = false; // To know when we're finished with us localization

	public UltrasonicLocalizer(State state, Navigation nav, Odometer odo) {
		this.state = state;
		this.nav = nav;
		this.odo = odo;
	}

	/**
	 * FSM with falling edge and rising edge states
	 */
	public void run() {
		switch (state) {
		case FALLING_EDGE_STATE:
			// // we started towards wall, turn away
			// while (getDist() < WALL_THRESHHOLD) {
			// nav.spin(true);
			// }
			nav.rotate(360.0, true);
			// switch to rising egde if we started towards wall
			if (getDist() < WALL_THRESHHOLD) {
				state = State.RISING_EDGE_STATE; 
				risingEdge();
			} else {
				fallingEdge();
			}
			break;
		case RISING_EDGE_STATE:
			// we started away from wall, turn toward a wall
			while (getDist() > WALL_THRESHHOLD) {
				nav.spin(true);
			}
			nav.rotate(360.0, true);
			risingEdge();
			break;
		case INIT:
			// do nothing
			break;
		}
	}

	/**
	 * Starts rising edge localization
	 */

	private void risingEdge() {
		// wait to find a rising edge
		waitForEdge(state);
		// Record odometer theta.
		theta1 = odo.getXYT()[2];
		// Rotate in the other direction.
		nav.rotate(-360, true);
		// wait for robot to face towards wall again
		sleepThread(3f);
		// wait to find a second rising edge
		waitForEdge(state);
		nav.stop(true);
		// Record odometer theta.
		theta2 = odo.getXYT()[2];

		findHeading();
	}

	/**
	 * Starts falling edge localization
	 */
	private void fallingEdge() {

		// wait to find falling edge
		waitForEdge(state);
		// Record odometer theta.
		theta1 = odo.getXYT()[2];
		// Rotate in the other direction.
		nav.rotate(-360.0, true);
		// wait for robot to face away from wall again
		sleepThread(3f);
		// wait to find a second falling edge
		waitForEdge(state);
		nav.stop(true);
		// Record odometer theta.
		theta2 = odo.getXYT()[2];

		findHeading();
	}

	/**
	 * Computes heading of robot based on state and detected angles. Then waits for
	 * user input to rotate to 0 deg
	 */
	private void findHeading() {
		double heading = -1;
		// compute estimated heading
		switch (state) {
		case FALLING_EDGE_STATE:
			heading = (FALLING_EDGE_CONST - (theta1 + theta2) / 2) % 360; // wrap from 0 to 360
			break;
		case RISING_EDGE_STATE:
			heading = (RISING_EDGE_CONST - (theta1 + theta2) / 2) % 360; // wrap from 0 to 360
			break;
		case INIT:
			break;
		}
		// set x and y to starting point
		if (Main.startingCorner[1] < 1) {
			odo.setY(TILE_SIZE * Main.startingCorner[1] + TILE_SIZE / 2);
		} else if (Main.startingCorner[1] > 1) {
			odo.setY(TILE_SIZE * Main.startingCorner[1] - TILE_SIZE / 2);
		}
		if (Main.startingCorner[0] < 1) {
			odo.setX(TILE_SIZE * Main.startingCorner[0] + TILE_SIZE / 2);
		} else if (Main.startingCorner[0] > 1) {
			odo.setX(TILE_SIZE * Main.startingCorner[0] - TILE_SIZE / 2);
		}

		// update odometer's heading
		odo.update(0, 0, heading);
		sleepThread(1f);
		// turn to 0 deg
		nav.turnTo(0);
		nav.stop(false);
		// correct starting position angle (due to different starting corners)
//		if (Tests.startingCorner[0] != Tests.startingCorner[1]) {
//			nav.rotate(90, false);
//			odo.setTheta(0);
//		}
		if(Main.startCorner == 1) {
			odo.setTheta(270);
		}
		if(Main.startCorner == 3) {
			odo.setTheta(90);
		}

		if (Main.startCorner == 2) {
			odo.setTheta(180);
		}
		finished = true;
	}

	/**
	 * Waits to find rising or falling edge
	 * 
	 * @param state1
	 */
	private void waitForEdge(State state1) {
		if (state1 == State.FALLING_EDGE_STATE) {
			// wait to find falling edge
			while (getDist() > FALLING_EDGE_THRESHHOLD) {
			}
			// falling edge found
			Sound.beep();
			return;
			// Rising Edge
		}
		if (state1 == State.RISING_EDGE_STATE) {
			// wait to find falling edge
			while (getDist() < RISING_EDGE_THRESHHOLD) {
			}
			// rising edge found
			Sound.beep();
			return;
		}
	}

	/**
	 * Sync distance with distance from usPoller
	 * 
	 * @param dist
	 * 
	 */
	public synchronized void setDist(float dist) {
		this.distance = dist;
	}

	/**
	 * Filtered distance from uspoller
	 * 
	 * @return distance (cm)
	 */
	public synchronized float getDist() {
		return distance;
	}

	/**
	 * For display/debugging
	 * 
	 * @return value of theta 1
	 */
	public double getTheta1() {
		return theta1;

	}

	/**
	 * for display/debugging
	 * 
	 * @return value of theta 2
	 */
	public double getTheta2() {
		return theta2;

	}

	/**
	 * puts thread to sleep (!!) careful, time is in seconds
	 * 
	 * @param time in  (seconds)
	 */
	private void sleepThread(float time) {
		try {
			Thread.sleep((long) (time * 1000));
		} catch (Exception e) {
		}
	}
}
