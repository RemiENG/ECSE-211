package wallFollower;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter,
			int bandwidth, int motorLow, int motorHigh) {
		// Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorHigh); // Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}

	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		int error = bandCenter - distance;

		if (Math.abs(error) < bandwidth) {
			leftMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.setSpeed(motorHigh);
			rightMotor.forward();
		} else if (error < 0) { // turn towards wall
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			rightMotor.setSpeed(motorHigh);
			rightMotor.forward();
			leftMotor.setSpeed(motorLow);
			leftMotor.forward();
		} else if (error > 0) { // turn away from wall
			if (distance < 10) {
				rightMotor.setSpeed(motorLow);
				leftMotor.setSpeed(motorHigh);
				rightMotor.backward();
				leftMotor.forward();
			} else {
				rightMotor.setSpeed(motorLow);
				rightMotor.forward();
				leftMotor.setSpeed(motorHigh);
				leftMotor.forward();
			}
		}
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
