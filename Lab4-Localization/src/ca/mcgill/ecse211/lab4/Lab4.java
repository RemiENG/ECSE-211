package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.lab4.Navigation;
import ca.mcgill.ecse211.lab4.Odometer;
import ca.mcgill.ecse211.lab4.Display;
import ca.mcgill.ecse211.lab4.UltrasonicLocalizer.Mode;
import ca.mcgill.ecse211.lab4.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.filter.MedianFilter;

public class Lab4 {

	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	// Ultrasonic sensor port.
	private static final Port usPort = LocalEV3.get().getPort("S2");
	// Color sensor port.
	private static final Port colorPort = LocalEV3.get().getPort("S1");

	private static SampleProvider usSample;
	private static SampleProvider meanFilter;
	private static float[] usData;

	// private static EV3ColorSensor colorSensor;
	private static SampleProvider rgbSample;
	private static SampleProvider medianFilter;
	private static float[] RGBData;

	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 9.545;

	private static Mode option;

	public static void main(String[] args) throws OdometerExceptions {
		int buttonChoice = -1;
		final TextLCD lcd = LocalEV3.get().getTextLCD();

		// Set up the ultrasonic sensor.
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		usSample = usSensor.getMode("Distance");
		meanFilter = new MeanFilter(usSample, 40);
		usData = new float[meanFilter.sampleSize()];

		// Set up the color sensor.
		@SuppressWarnings("resource")
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		rgbSample = colorSensor.getMode("Red");
		medianFilter = new MedianFilter(rgbSample, rgbSample.sampleSize());
		RGBData = new float[medianFilter.sampleSize()];

		// menu display.
		do {
			// clear the display
			lcd.clear();
			// ask the user whether the motors should drive in a square or float
			lcd.drawString("Left: Rising Edge", 0, 0);
			lcd.drawString("Right: Falling Edge", 0, 1);
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		switch (buttonChoice) {
		case Button.ID_LEFT:
			option = Mode.RISING_EDGE;
			break;
		case Button.ID_RIGHT:
			option = Mode.FALLING_EDGE;
			break;
		default:
			System.exit(0);
			break;
		}

		if (buttonChoice == Button.ID_LEFT || buttonChoice == Button.ID_RIGHT) {
			
			Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
			Navigation navigation = new Navigation(odometer, leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
			UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(option, navigation, odometer);
			UltrasonicPoller usPoller = new UltrasonicPoller(meanFilter, usData, usLocalizer);
			LightLocalizer lightLocalizer = new LightLocalizer(navigation, odometer);
			ColorPoller rgbPoller = new ColorPoller(medianFilter, RGBData, lightLocalizer);
			Display odometryDisplay = new Display(lcd);

			/*
			 * Thread to detect early exits.
			 */
			(new Thread() {
				public void run() {
					while (Button.waitForAnyPress() != Button.ID_ESCAPE)
						;
					System.exit(0);
				}
			}).start();

			Thread odoThread = new Thread(odometer);
			odoThread.start();

			Thread odoDisplayThread = new Thread(odometryDisplay);
			odoDisplayThread.start();
			usPoller.start();
			usLocalizer.start();
			

			while (!usLocalizer.finished)
				;
			navigation.rotate(45, false);
			navigation.move(5.0, false);
			rgbPoller.start();
			while (!rgbPoller.isAlive())
				; // Make sure the color poller thread is alive before starting the localization.
			lightLocalizer.start();
			while (!lightLocalizer.done)
				;
//			navigator.start();
//			navigator.setNavigating(true);
//			navigator.setMap(new MapPoint[] { new MapPoint(0, 0) });
//
//			while (navigator.isNavigating())
				;
			navigation.rotate(-odometer.getXYT()[2], false);
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}
