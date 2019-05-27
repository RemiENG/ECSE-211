package ca.mcgill.ecse211.sensor;

import java.util.HashMap;

import ca.mcgill.ecse211.Final_Project.Main;
import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import lejos.hardware.Sound;

/**
 * This class is responsible for scanning for a block once the robot is in a
 * search zone
 * 
 * @author remi
 *
 */
public class BlockScanner extends Thread {

	Navigation nav;
	UltrasonicPoller us;
	Odometer odo;
	public boolean scannedBlock = false;
	HashMap<Float, Double> map = new HashMap<Float, Double>();
	private float smallestDistance = Float.MAX_VALUE;
	private double smallestAngle = Double.MAX_VALUE;

	/**
	 * Constructor
	 * 
	 * @param nav
	 * @param us
	 * @param odo
	 */
	public BlockScanner(Navigation nav, UltrasonicPoller us, Odometer odo) {
		this.nav = nav;
		this.us = us;
		this.odo = odo;
	}

	/**
	 * Scans search zone to find a block
	 * 
	 * @return true if block was found, false otherwise
	 */
	public synchronized boolean Scan() {
		// a timer to scan 90 degrees forward and backwards
		// put all the distances , and angles measured in the hashMap
		// reset values

		// Scan 90 deg
		spin(90);
		// if we scanned a block
		if (smallestDistance < 60f && withinSearchZone()) {
			Sound.beep();
			goToBlock();
			return true;
		}
		return false;
	}

	/**
	 * Spins robot back and forth by angle and finds distance and angle of nearest
	 * block
	 * 
	 * @param angle
	 */
	public void spin(int angle) {
		// reset values
		smallestDistance = Float.MAX_VALUE;
		smallestAngle = Double.MAX_VALUE;
		// timer to get data while rotating
		int timer = angle == 90 ? 2000 : 4000;
		rotate(timer, true);
		// spin 90 counter-clockwise
		rotate(timer, false);

		// spin 90 anti-clockwise
		smallestAngle = map.get(smallestDistance);
	}

	private void rotate(int timer, boolean clockwise) {
		long endTimeMillisCW = System.currentTimeMillis() + timer;
		while (true) {
			nav.spin(clockwise);
			float temp = us.getDistance();
			if (temp > 0) {
				smallestDistance = temp < smallestDistance ? temp : smallestDistance;
				map.put(temp, odo.getXYT()[2]);
			}
			if (System.currentTimeMillis() > endTimeMillisCW) {
				break;
			}
		}
	}

	public float getDistance() {
		return this.smallestDistance;
	}

	public double getAngle() {
		return this.smallestAngle;
	}

	private void goToBlock() {
		Sound.beep();
		nav.turnTo(smallestAngle);
		// if the block is further than 35cm, approach block and scan again
		if (this.smallestDistance > 35) { 
			nav.move(this.smallestDistance - 15, false);
			nav.turn(-30);
			spin(90);
			nav.turnTo(smallestAngle);
			if (smallestDistance > 4) {
				nav.move(smallestDistance - 4, false);
			} else {
				goCloser();
			}
		} 
		// Block is near by, go directly to it
		else {
			if (smallestDistance > 4) {
				nav.move(smallestDistance - 4, false);
			} else {
				goCloser();
			}
		}

	}

	private void goCloser() {
		while (us.getDistance() > 3) {
			nav.moveForward(100);
		}
	}

	private boolean withinSearchZone() {
		boolean withInX = false;
		boolean withInY = false;

		double targetX = (smallestDistance * Math.sin(Math.toRadians(smallestAngle)));
		targetX += odo.getXYT()[0];
		double targetY = (smallestDistance * Math.cos(Math.toRadians(smallestAngle)));
		targetY += odo.getXYT()[1];

		// if x value is within zone
		if ((targetX > Main.SR_LL[0] * 30.48) && (targetX < Main.SR_UR[0] * 30.48)) {
			withInX = true;
		}
		// if y value is within zone
		if ((targetY > Main.SR_LL[1] * 30.48) && (targetY < Main.SR_UR[1] * 30.48)) {
			withInY = true;
		}

		return withInY && withInX;
	}
	public void sleepThread(float seconds) {
		try {
			Thread.sleep((long) (seconds * 1000f));
		} catch (Exception e) {
		}
	}

}
