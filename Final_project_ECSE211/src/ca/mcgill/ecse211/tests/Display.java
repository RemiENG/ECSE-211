package ca.mcgill.ecse211.tests;

import java.text.DecimalFormat;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import ca.mcgill.ecse211.sensor.UltrasonicLocalizer;
import lejos.hardware.lcd.TextLCD;

/**
 * This class is used to display the content of the odometer variables (x, y,
 * Theta)
 */
public class Display implements Runnable {

	private Odometer odo;
	private TextLCD lcd;
	private UltrasonicLocalizer ul;
	private OdometryCorrection oc;
	private double[] position;
	private final long DISPLAY_PERIOD = 25;
	private long timeout = Long.MAX_VALUE;

	/**
	 * This is the class constructor
	 * 
	 * @param odoData
	 * @throws OdometerExceptions
	 */
	public Display(TextLCD lcd, UltrasonicLocalizer ul,  OdometryCorrection oc) throws OdometerExceptions {
		odo = Odometer.getOdometer();
		this.lcd = lcd;
		this.ul = ul;
		this.oc = oc;
	}

	/**
	 * 
	 * @param lcd
	 * @param timeout
	 * @throws OdometerExceptions
	 */
	public Display(TextLCD lcd, long timeout) throws OdometerExceptions {
		odo = Odometer.getOdometer();
		this.timeout = timeout;
		this.lcd = lcd;
	}

	public void run() {

		lcd.clear();

		long updateStart, updateEnd;

		long tStart = System.currentTimeMillis();
		do {
			updateStart = System.currentTimeMillis();

			// Retrieve x, y and Theta information
			position = odo.getXYT();

			// Print x,y, and theta information
			DecimalFormat numberFormat = new DecimalFormat("######0.00");
			lcd.drawString("X: " + numberFormat.format(position[0]), 0, 0);
			lcd.drawString("Y: " + numberFormat.format(position[1]), 0, 1);
			lcd.drawString("T: " + numberFormat.format(position[2]), 0, 2);
			lcd.drawString("Dist: " + (ul.getDist()), 0, 3);
//			lcd.drawString("Light1(r): " + (oc.getLightLevel1()), 0, 3);
//			lcd.drawString("Light2(l): " + (oc.getLightLevel2()), 0, 4);
//			lcd.drawString("x1: " + (oc.x1), 0, 3);
//			lcd.drawString("y1: "+(oc.y1), 0, 5);
//			lcd.drawString("x2: " + (oc.x2), 0, 5);
//			lcd.drawString("y2: "+(oc.y2), 0, 6);
//			lcd.drawString("Theta1: " + numberFormat.format(ul.getTheta1()), 0, 4);
//			lcd.drawString("Theta2: " + numberFormat.format(ul.getTheta2()), 0, 5);
//			lcd.drawString("color: " + numberFormat.format(ll.getLightLevel()), 0, 6);

			// this ensures that the data is updated only once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while ((updateEnd - tStart) <= timeout);

	}

}
