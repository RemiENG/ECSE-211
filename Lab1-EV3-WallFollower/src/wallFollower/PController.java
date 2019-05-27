package wallFollower;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	private final int bandCenter, bandwidth;
	private final int motorStraight = 175, FILTER_OUT = 40;
	private final int maxMotor = 300;
	private final int minMotor = 100;
	private final double propConst = 3.0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;

	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter,
			int bandwidth) {
		// Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorStraight); // Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}

	private int calcProp(int diff) {
		diff = Math.abs(diff);
		int correction = (int) (propConst * (double) diff);
		return correction;
	}

	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}

		int diff = bandCenter - this.distance;
		int delta = calcProp(diff);
		if (Math.abs(diff) < bandwidth) {
			leftMotor.setSpeed(motorStraight);
			leftMotor.forward();
			rightMotor.setSpeed(motorStraight);
			rightMotor.forward();
		} else if (diff > 0) { // turn away from wall, small distance
			if (motorStraight + delta > maxMotor) { // check to see if delta is within max speed constraint
				leftMotor.setSpeed(maxMotor); // delta too large, speed set to max
			} else {
				leftMotor.setSpeed(motorStraight + delta); // delta within constraints, set appropriate speed
			}
			if (motorStraight - delta < minMotor) { // check to see if delta is within min speed constraint
				rightMotor.setSpeed(minMotor); // delta too large, speed set to min
			} else {
				rightMotor.setSpeed(motorStraight - delta); // delta within constraints, set appropriate speed
			}
			if (this.distance >= 10) { // if more than 10cm away from wall, stay in forward
				leftMotor.forward();
				rightMotor.forward();
			} else { // if less than 10cm away, turn away quickly by reversing right wheel
				leftMotor.forward();
				rightMotor.backward();
			}
		} else if (diff < 0) { // turn towards wall, large distance
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			} // keep going straight for 0.5sec if far from wall
			if (motorStraight + delta > maxMotor) {
				rightMotor.setSpeed(maxMotor); // delta too large, speed set to max
			} else {
				rightMotor.setSpeed(motorStraight + delta);
			}
			if (motorStraight - delta < minMotor) {
				leftMotor.setSpeed(minMotor); // delta too large, speed set to min
			} else {
				leftMotor.setSpeed(motorStraight - delta);
			}
			leftMotor.forward();
			rightMotor.forward();
		}
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
