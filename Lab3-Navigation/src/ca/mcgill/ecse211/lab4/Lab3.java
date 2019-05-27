package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.lab4.Odometer;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 * 
 * @author Remi Carriere
 *
 */
public class Lab3 {
	// Enables printing to the remote console.
	public static boolean debug_mode = false;

	// Overrides the path choice and uses the reportPath (for data gathering for the
	// lab report)
	public static boolean report_path = false;

	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	private static final EV3MediumRegulatedMotor sensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));

	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 15.59;

	public static void main(String[] args) throws OdometerExceptions {
		int buttonChoice = -1;

		final TextLCD lcd = LocalEV3.get().getTextLCD();

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		Navigation navigation = new Navigation(odometer, leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
		UltrasonicPoller usPoller = new UltrasonicPoller(sensorMotor);
		Navigator navigator = new Navigator(navigation, odometer, usPoller);
		Display odometryDisplay = new Display(lcd);

		do {
			// clear the display
			lcd.clear();

			// ask the user whether the motors should drive in a square or float
			lcd.drawString("Left: Track 1", 0, 0);
			lcd.drawString("Right: Track 2", 0, 1);
			lcd.drawString("Up: Track 3", 0, 2);
			lcd.drawString("Down: Track 4", 0, 3);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_UP
				&& buttonChoice != Button.ID_DOWN);

		if (buttonChoice != -1) {
			switch (buttonChoice) {
			case Button.ID_LEFT:
				navigator.setPath(1);
				break;
			case Button.ID_RIGHT:
				navigator.setPath(2);
				break;
			case Button.ID_UP:
				navigator.setPath(3);
				break;
			case Button.ID_DOWN:
				navigator.setPath(4);
				break;
			case Button.ID_ESCAPE:
				System.exit(0);
				break;
			}
			Thread odoThread = new Thread(odometer);
			odoThread.start();

			Thread odoDisplayThread = new Thread(odometryDisplay);
			odoDisplayThread.start();
			navigator.start();
			usPoller.start();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}
