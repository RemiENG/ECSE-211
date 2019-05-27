/*
 * SquareDriver.java
 */
package ca.mcgill.ecse211.tests;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class OdoTestSquare {
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 100;
	private static final double TILE_SIZE = 30.48;

	private static final double NUM_Tile = 3; // added this to make it less "hard coded"
	// "don't hard code your odometry correction" - hard codes square driver...SMH

	/**
	 * This method is meant to drive the robot in a square of size 2x2 Tiles. It is
	 * to run in parallel with the odometer and Odometer correcton classes allow
	 * testing their functionality.
	 * 
	 * @param leftMotor		An EV3 regulated motor
	 * @param rightMotor		An EV3 regulated motor
	 * @param leftRadius		value of the left radius
	 * @param rightRadius	value of the right radius
	 * @param width			value of the width
	 */
	public static void drive(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double leftRadius,
			double rightRadius, double track) {
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(400); // SLowed acceleration to prevent wheel slip
		}

		// Sleep for 2 seconds
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// There is nothing to be done here
		}

		for (int i = 0; i < 4; i++) {
			// drive forward two tiles
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.setSpeed(FORWARD_SPEED);

			for (int j = 0; j < NUM_Tile; j++) {
				leftMotor.rotate(convertDistance(leftRadius, 1 * TILE_SIZE), true);
				rightMotor.rotate(convertDistance(rightRadius, 1 * TILE_SIZE), false);
				Button.waitForAnyPress();
			}
			// turn 90 degrees clockwise
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			leftMotor.rotate(convertAngle(leftRadius, track, 90), true);
			rightMotor.rotate(-convertAngle(rightRadius, track, 90), false);

			Button.waitForAnyPress();

		}
	}

	/**
	 * This method allows the conversion of a distance to the total rotation of each
	 * wheel need to cover that distance.
	 * 
	 * @param radius		value of radius
	 * @param distance	value of the distance
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * 
	 * 
	 * @param radius
	 * @param width
	 * @param angle
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
