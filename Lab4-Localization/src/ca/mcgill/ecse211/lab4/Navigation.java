
package ca.mcgill.ecse211.lab4;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

// Taken from the last lab.

/**
 * 
 * 
 * 
 *
 */

public class Navigation {
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 75;
	private static final double TILE_SIZE = 30.48;
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	double leftRadius, rightRadius;
	double width;
	Odometer odo;
	boolean isNavigating = false;

	public Navigation(Odometer odo, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double leftRadius, double rightRadius, double width) {
		this.odo = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
		leftMotor.setAcceleration(600);
		rightMotor.setAcceleration(600);
	}
	

	public void travelTo(double x, double y, boolean tileMultiple) {
		isNavigating = true;
		// Calculate distance from current position
		double hypotenuse = computeTargetHypot(x, y, tileMultiple);

		// Calculate theta (deg) towards target wrt to +y axis
		double targetTheta = computeTargetTheta(x, y, tileMultiple);

		// turn towards destination
		turnTo(targetTheta);

		// travel to target
		leftMotor.setSpeed(FORWARD_SPEED - 1);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(leftRadius, hypotenuse), true);
		rightMotor.rotate(convertDistance(rightRadius, hypotenuse), false);

		isNavigating = false;

	}

	/**
	 * This method turns the robot angle theta (limits angle)
	 * 
	 * @param theta
	 */
	public void turnTo(double theta) {
		// limit to the minimum angle
		if (theta > 180.0) {
			theta = theta - 360.0;
		} else if (theta < -180.0) {
			theta = theta + 360.0;
		}
		// set motor speeds
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		// set amount to rotate
		leftMotor.rotate(convertAngle(leftRadius, width, theta), true);
		rightMotor.rotate(-convertAngle(rightRadius, width, theta), false);
	}

	public void rotate(double theta, boolean returnEarly) {
		// set motor speeds and acceleration
		leftMotor.setAcceleration(350);
		rightMotor.setAcceleration(350);
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		// rotate
		leftMotor.rotate(convertAngle(leftRadius, width, theta), true);
		rightMotor.rotate(-convertAngle(rightRadius, width, theta), false || returnEarly);

	}

	public void move(double dist, boolean returnEarly) {
		// set motor speeds and acceleration
		leftMotor.setAcceleration(600);
		rightMotor.setAcceleration(600);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		// move
		leftMotor.rotate(convertDistance(leftRadius, dist), true);
		rightMotor.rotate(convertDistance(rightRadius, dist), false || returnEarly);
	}

	public double computeTargetHypot(double x, double y, boolean tileMultiple) {
		if (tileMultiple) {
			x = x * TILE_SIZE;
			y = y * TILE_SIZE;
		}

		x = x - odo.getXYT()[0]; // x distance
		y = y - odo.getXYT()[1]; // y distance
		double hypotenuse = Math.hypot(x, y); // distance to target
		return hypotenuse;
	}

	public double computeTargetTheta(double x, double y, boolean tileMultiple) {
		if (tileMultiple) {
			x = x * TILE_SIZE;
			y = y * TILE_SIZE;
		}
		x = x - odo.getXYT()[0]; // x distance
		y = y - odo.getXYT()[1]; // y distance
		double theta = Math.toDegrees(Math.atan2(y, x)); // wrt to +x
		theta = 90 - theta; // wrt to +y
		theta = theta - odo.getXYT()[2];
		return theta;
	}

	public boolean isNavigating() {
		return isNavigating;
	}

	public void stop() {
		rightMotor.stop();
		leftMotor.stop();
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
