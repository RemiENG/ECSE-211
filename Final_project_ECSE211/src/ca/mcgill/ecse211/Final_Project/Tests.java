package ca.mcgill.ecse211.Final_Project;

import java.util.Map;

import ca.mcgill.ecse211.Final_Project.ColorIdentifier.BlockColor;
import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import ca.mcgill.ecse211.navigation.BridgeTunnel;
import ca.mcgill.ecse211.navigation.Navigation;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.odometer.OdometryCorrection;
import ca.mcgill.ecse211.sensor.BlockScanner;
import ca.mcgill.ecse211.sensor.LineDetector1;
import ca.mcgill.ecse211.sensor.LineDetector2;
import ca.mcgill.ecse211.sensor.UltrasonicLocalizer;
import ca.mcgill.ecse211.sensor.UltrasonicPoller;
import ca.mcgill.ecse211.sensor.UltrasonicLocalizer.State;
import ca.mcgill.ecse211.tests.OdoTestTrack;
import ca.mcgill.ecse211.tests.BlockDisplay;
import ca.mcgill.ecse211.tests.Display;
import ca.mcgill.ecse211.tests.LightLocalizer;
import ca.mcgill.ecse211.tests.OdoTestRadius;
import ca.mcgill.ecse211.tests.OdoTestSquare;
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

/**
 * 
 * this class starts the necessary threads. contains main method
 *
 */
public class Tests {
	// ** Set these as appropriate for your team and current situation **
	// wifi code already implemented!
	/*
	 * private static final String SERVER_IP = "192.168.2.16"; private static final
	 * int TEAM_NUMBER = 2; private static final boolean ENABLE_DEBUG_WIFI_PRINT =
	 * true;
	 */

	// Motors
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	// private static final EV3LargeRegulatedMotor sensorMotor = new
	// EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	// Us sensor port for localization
	private static final Port usPort = LocalEV3.get().getPort("S2");
	// Us data and filter for localization
	private static SampleProvider usSample;
	private static SampleProvider medianFilter;
	private static float[] usData;

	// RGB sensor port1
	private static final Port colorPort = LocalEV3.get().getPort("S1");
	// RGB data and filter for line1 detection
	private static SampleProvider rgbSample;
	private static SampleProvider meanFilter;
	private static float[] RGBData;

	// RGB sensor port2
	private static final Port colorPort2 = LocalEV3.get().getPort("S3");
	// RGB data and filter for line2 detection
	private static SampleProvider rgbSample2;
	private static SampleProvider meanFilter2;
	private static float[] RGBData2;

	// Constants (Adjust these for better performance)
	public static final double WHEEL_RAD = 2.1; // 1.7
	public static final double TRACK = 14.8; // 16.0

	// Positions
	// public static final double[] lowerCorner = { 3, 3 };
	// public static final double[] upperCorner = { 7, 7 };

	// Possible Starting Corners
	public static double startCorner = 0; // (0,0)
	// public static double startCorner = 1; //(8,0)
	// public static double startCorner = 2; //(8,8)
	// public static double startCorner = 3; //(0,8)

	// Starting corner for DEMO
	public static double[] startingCorner = new double[2];
	// Bridge and tunnel coodrdinates
	public static final double[] bridgeLocation_UR = { 6, 5 };
	public static final double[] bridgeLocation_LL = { 5, 3 };
	public static final double[] tunnelLocation_LL = { 2, 3 };
	public static final double[] tunnelLocation_UR = { 3, 5 };
	// Zone coordinates
	public static final double[] red_UR = new double[2];
	public static final double[] red_LL = new double[2];
	public static final double[] green_UR = new double[2];
	public static final double[] green_LL = new double[2];

	
	public static final double[] SR_UR = {3,3};
	public static final double[] SR_LL = {0,0}; 

	public static final double[] searchRegion_UR = {};
	public static final double[] searchRegion_LR = {};


	// Starting zone (Team)
	public static enum Start_Zone {
		Green_Zone, Red_zone
	};

	// Set starting zone (for testing)
	public static Start_Zone startZone = Start_Zone.Green_Zone;
	// public static Start_Zone startZone = Start_Zone.Red_zone;

	// Target Block
	public static BlockColor tb = BlockColor.BLUE;
	private static ColorIdentifier blockColorSensor;
	public static BlockColor none = BlockColor.NONE;
	// Flag for odometry Correction
	public static boolean correctionON = false;

	public static void main(String[] args) throws OdometerExceptions {

		// Set starting corner
		if (startCorner == 0) {
			startingCorner[0] = 0;
			startingCorner[1] = 0;
		} else if (startCorner == 1) {
			startingCorner[0] = 8;
			startingCorner[1] = 0;
		} else if (startCorner == 2) {
			startingCorner[0] = 8;
			startingCorner[1] = 8;
		} else if (startCorner == 3) {
			startingCorner[0] = 0;
			startingCorner[1] = 8;
		}

		/*
		 * WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER,
		 * ENABLE_DEBUG_WIFI_PRINT); try {
		 * 
		 * Map data = conn.getData();
		 * 
		 * // Example 1: Print out all received data System.out.println("Map:\n" +
		 * data);
		 * 
		 * // Example 2 : Print out specific values int redTeam = ((Long)
		 * data.get("RedTeam")).intValue(); System.out.println("Red Team: " + redTeam);
		 * 
		 * int og = ((Long) data.get("OG")).intValue();
		 * System.out.println("Green opponent flag: " + og);
		 * 
		 * // Example 3: Compare value int tn_ll_x = ((Long)
		 * data.get("TN_LL_x")).intValue(); if (tn_ll_x < 5) {
		 * System.out.println("Tunnel LL corner X < 5"); } else {
		 * System.out.println("Tunnel LL corner X >= 5"); }
		 * 
		 * } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
		 */
		int buttonChoice = -1;
		final TextLCD lcd = LocalEV3.get().getTextLCD();

		// Set up ultrasonic sensor and filter for localization
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		usSample = usSensor.getMode("Distance");
		medianFilter = new MedianFilter(usSample, usSample.sampleSize());
		usData = new float[medianFilter.sampleSize()];

		// Set up color sensor and filter for line1 detection
		@SuppressWarnings("resource")
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		rgbSample = colorSensor.getMode("Red");
		meanFilter = new MeanFilter(rgbSample, rgbSample.sampleSize());
		RGBData = new float[meanFilter.sampleSize()];

		// Set up color sensor and filter for line1 detection
		@SuppressWarnings("resource")
		SensorModes colorSensor2 = new EV3ColorSensor(colorPort2);
		rgbSample2 = colorSensor2.getMode("Red");
		meanFilter2 = new MeanFilter(rgbSample2, rgbSample2.sampleSize());
		RGBData2 = new float[meanFilter2.sampleSize()];

		// data for block detection
		float[] test = new float[100];

		// Start Menu
		do {
			// clear the display
			lcd.clear();

			lcd.drawString("< Left  | Right > ", 0, 0);
			lcd.drawString("        |         ", 0, 1);
			lcd.drawString("  Odo   |Search & ", 0, 2);
			lcd.drawString(" & Nav  | Localize", 0, 3);
			buttonChoice = Button.waitForAnyPress();
		}

		// wait for button press
		while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		lcd.clear();
		// Exit early by pressing button
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
				}
				System.exit(0);
			}
		}).start();

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		// // Navigation
		Navigation navigation = new Navigation(odometer, leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
		// // us localizer
		UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(State.FALLING_EDGE_STATE, navigation, odometer);
		UltrasonicPoller usPoller = new UltrasonicPoller(medianFilter, usData, usLocalizer);
		// odometry correction
		OdometryCorrection odometryCorrection = new OdometryCorrection();
		// // light localizer
		LightLocalizer lightLocalizer = new LightLocalizer(navigation, odometer, odometryCorrection);
		LineDetector1 lineDetector1 = new LineDetector1(meanFilter, RGBData, odometryCorrection, odometer);
		LineDetector2 lineDetector2 = new LineDetector2(meanFilter2, RGBData2, odometryCorrection, odometer);
		// odometer display
		Display odometryDisplay = new Display(lcd, usLocalizer, odometryCorrection);
		// Bridge crosser
		BridgeTunnel bridge = new BridgeTunnel(navigation, odometer, lightLocalizer);
		// Scanner
		BlockScanner scan = new BlockScanner(navigation, usPoller, odometer);

		// chose odo & Nav
		if (buttonChoice == Button.ID_LEFT) {
			lcd.clear();
			lcd.drawString("< Left  | Right > ", 0, 0);
			lcd.drawString("        |         ", 0, 1);
			lcd.drawString("  Odo   | Nav     ", 0, 2);
			lcd.drawString("        |         ", 0, 3);
			buttonChoice = Button.waitForAnyPress();

			if (buttonChoice == Button.ID_LEFT) {
				lcd.clear();
				lcd.drawString("< Left  | Right > ", 0, 0);
				lcd.drawString("        |         ", 0, 1);
				lcd.drawString("   No   | Correct ", 0, 2);
				lcd.drawString("Correct |         ", 0, 3);
				buttonChoice = Button.waitForAnyPress();
				// chose no correct
				if (buttonChoice == Button.ID_LEFT) {
					lcd.clear();
					lcd.drawString(" UP: Square drive ", 0, 0);
					lcd.drawString("< Left  | Right > ", 0, 1);
					lcd.drawString("Tune    | Tune    ", 0, 2);
					lcd.drawString("Track   | Radius  ", 0, 3);
					buttonChoice = Button.waitForAnyPress();
					// Track test (turns 360 deg)
					if (buttonChoice == Button.ID_LEFT) {
						Thread odoThread = new Thread(odometer);
						odoThread.start();
						Thread odoDisplayThread = new Thread(odometryDisplay);
						odoDisplayThread.start();
						(new Thread() {
							public void run() {
								OdoTestTrack.drive(leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
							}
						}).start();
						// Radius test (goes forward 3 tile sizes)
					} else if (buttonChoice == Button.ID_RIGHT) {
						Thread odoThread = new Thread(odometer);
						odoThread.start();
						Thread odoDisplayThread = new Thread(odometryDisplay);
						odoDisplayThread.start();
						(new Thread() {
							public void run() {
								OdoTestRadius.drive(leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
							}
						}).start();
						// square test
					} else if (buttonChoice == Button.ID_UP) {
						Thread odoThread = new Thread(odometer);
						odoThread.start();
						Thread odoDisplayThread = new Thread(odometryDisplay);
						odoDisplayThread.start();
						(new Thread() {
							public void run() {
								OdoTestSquare.drive(leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
							}
						}).start();

					}

					// Odometry correction test
				} else if (buttonChoice == Button.ID_RIGHT) {
					correctionON = true;
					Thread lineDetector1Thread = new Thread(lineDetector1);
					lineDetector1Thread.start();
					Thread lineDetector2Thread = new Thread(lineDetector2);
					lineDetector2Thread.start();
					Thread odoThread = new Thread(odometer);
					odoThread.start();
					Thread odoDisplayThread = new Thread(odometryDisplay);
					odoDisplayThread.start();
					Thread odoCorrectionThread = new Thread(odometryCorrection);
					odoCorrectionThread.start();

					(new Thread() {
						public void run() {
							OdoTestSquare.drive(leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
						}
					}).start();
				}
			}

			// chose Navigation
			else if (buttonChoice == Button.ID_RIGHT) {
				Thread odoThread = new Thread(odometer);
				odoThread.start();
				Thread odoDisplayThread = new Thread(odometryDisplay);
				odoDisplayThread.start();
				// start us localization
				usPoller.start();
				usLocalizer.start();
				// wait for us localizer to finish
				while (!usLocalizer.finished) {
				}
				// use odo corretction as light localization
				Thread lineDeterctor1Thread = new Thread(lineDetector1);
				lineDeterctor1Thread.start();
				Thread lineDeterctor2Thread = new Thread(lineDetector2);
				lineDeterctor2Thread.start();
				Thread odoCorrectionThread = new Thread(odometryCorrection);
				odoCorrectionThread.start();
				// start light localization
				lightLocalizer.Localize(true);
				// wait for light localizer to finish
				while (!lightLocalizer.finished) {
				}
				// Now we can Navigate...
				navigation.travelByTileSteps(3, 3);
				navigation.travelByTileSteps(0, 0);

			}

		}
		// chose localization and search
		else if (buttonChoice == Button.ID_RIGHT) {
			lcd.clear();
			lcd.drawString("< Left  | Right > ", 0, 0);
			lcd.drawString("        |         ", 0, 1);
			lcd.drawString("  local-| search  ", 0, 2);
			lcd.drawString("ization |   Color ", 0, 3);
			buttonChoice = Button.waitForAnyPress();

			// chose localisation
			if (buttonChoice == Button.ID_LEFT) {
				lcd.clear();

				// Start Odo and display
				Thread odoThread = new Thread(odometer);
				odoThread.start();
				Thread odoDisplayThread = new Thread(odometryDisplay);
				odoDisplayThread.start();
				// start us localization
				usPoller.start();
				usLocalizer.start();
				// wait for us localizer to finish
				while (!usLocalizer.finished) {
				}
				// use odo corretction as light localization
				Thread lineDeterctor1Thread = new Thread(lineDetector1);
				lineDeterctor1Thread.start();
				Thread lineDeterctor2Thread = new Thread(lineDetector2);
				lineDeterctor2Thread.start();
				Thread odoCorrectionThread = new Thread(odometryCorrection);
				odoCorrectionThread.start();
				// start light localization
				lightLocalizer.Localize(true);

			}
			// chose search and color
			else if (buttonChoice == Button.ID_RIGHT) {
				lcd.clear();
				lcd.drawString("^Up^: Green Team  ", 0, 0);
				lcd.drawString("< Left  | Right > ", 0, 1);
				lcd.drawString(" color  | search  ", 0, 2);
				lcd.drawString(" detect |         ", 0, 3);
				lcd.drawString(" Down: red team   ", 0, 4);
				buttonChoice = Button.waitForAnyPress();

				// color test
				if (buttonChoice == Button.ID_LEFT) {
					// still needs to be changed
					// // Start rgb block poller
					ColorIdentifier blockColorSensor = new ColorIdentifier(test, tb);
					blockColorSensor.start();
					// // start display
					BlockDisplay blockDisplay = new BlockDisplay(lcd, blockColorSensor);
					Thread blockDisplayThread = new Thread(blockDisplay);
					blockDisplayThread.start();
				}
				// chose search
				else if (buttonChoice == Button.ID_RIGHT) {
					lcd.clear();
					usPoller.start();
					Thread odoThread = new Thread(odometer);
					odoThread.start();
					Thread odoDisplayThread = new Thread(odometryDisplay);
					odoDisplayThread.start();
					Thread lineDeterctor1Thread = new Thread(lineDetector1);
					lineDeterctor1Thread.start();
					Thread lineDeterctor2Thread = new Thread(lineDetector2);
					lineDeterctor2Thread.start();
					Thread odoCorrectionThread = new Thread(odometryCorrection);
					odoCorrectionThread.start();
					ColorIdentifier blockColorSensor = new ColorIdentifier(test, tb);
					blockColorSensor.start();
					Search search = new Search(navigation, blockColorSensor, scan);
					search.start();


					// chose green team
				} else if (buttonChoice == Button.ID_UP) {
					Thread odoThread = new Thread(odometer);
					odoThread.start();
					Thread odoDisplayThread = new Thread(odometryDisplay);
					odoDisplayThread.start();
					// start us localization
					usPoller.start();
					usLocalizer.start();
					// wait for us localizer to finish
					while (!usLocalizer.finished) {
					}
					// use odo corretction as light localization
					Thread lineDeterctor1Thread = new Thread(lineDetector1);
					lineDeterctor1Thread.start();
					Thread lineDeterctor2Thread = new Thread(lineDetector2);
					lineDeterctor2Thread.start();
					Thread odoCorrectionThread = new Thread(odometryCorrection);
					odoCorrectionThread.start();
					// start light localization
					lightLocalizer.Localize(true);
					// wait for light localizer to finish
					while (!lightLocalizer.finished) {
					}

					// Go to tunnel
					bridge.travelToTunnel();

					// Go to bridge
					bridge.travelToBridge();

					navigation.travelByTileSteps(7, 0);

					// chose red team
				} else if (buttonChoice == Button.ID_DOWN) {
					Thread odoThread = new Thread(odometer);
					odoThread.start();
					Thread odoDisplayThread = new Thread(odometryDisplay);
					odoDisplayThread.start();
					// start us localization
					usPoller.start();
					usLocalizer.start();
					// wait for us localizer to finish
					while (!usLocalizer.finished) {
					}
					// use odo corretction as light localization
					Thread lineDeterctor1Thread = new Thread(lineDetector1);
					lineDeterctor1Thread.start();
					Thread lineDeterctor2Thread = new Thread(lineDetector2);
					lineDeterctor2Thread.start();
					Thread odoCorrectionThread = new Thread(odometryCorrection);
					odoCorrectionThread.start();
					// start light localization
					lightLocalizer.Localize(true);
					// wait for light localizer to finish
					while (!lightLocalizer.finished) {
					}

					// Go to bridge
					bridge.travelToBridge();

					// Go to tunnel
					bridge.travelToTunnel();
					// go to starting point
					navigation.travelByTileSteps(0, 7);
				}

			}
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
		}
		System.exit(0);
	}
}
