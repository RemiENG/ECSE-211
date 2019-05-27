
package ca.mcgill.ecse211.lab4;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

// Taken from the last lab.

/**
 * 
 * Class that handles moving the robot when moving to a waypoint. Delegates
 * control to the PController when avoiding an obstacle.
 * 
 * @author Remi Carriere
 *
 */

public class Navigation {
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 75;
	private static final double TILE_SIZE = 30.48;
	PController pCont;
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	double leftRadius, rightRadius;
	double width;
	Odometer odo;
	boolean rotating = false;
	boolean moving_forward = false;

	public Navigation(Odometer odo, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double leftRadius, double rightRadius, double width) {
		this.odo = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
		pCont = new PController(leftMotor, rightMotor, 30, 2);
		leftMotor.setAcceleration(600);
		rightMotor.setAcceleration(600);
	}

	public void rotate(double theta, boolean returnEarly) {
		// set motor speeds and acceleration
		leftMotor.setAcceleration(350);
		rightMotor.setAcceleration(350);
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		if (theta > 180.0) {
			theta = theta - 360.0;
		} else if (theta < -180.0) {
			theta = theta + 360.0;
		}

		// rotate
		leftMotor.rotate(convertAngle(leftRadius, width, theta), true);
		rightMotor.rotate(-convertAngle(rightRadius, width, theta), false || returnEarly);

	}

	public void moveTo(double dist, boolean returnEarly) {
		leftMotor.setAcceleration(600);
		rightMotor.setAcceleration(600);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
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
		// limit to the minimum angle
		return theta;
	}

	public void stop() {
		rightMotor.stop();
		leftMotor.stop();
	}

	public void avoidObstacle(float dist) {
		pCont.processUSData(dist);
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	public boolean isRotating() {
		return rotating;
	}

	public boolean isGoingForward() {
		return moving_forward;
	}
}
