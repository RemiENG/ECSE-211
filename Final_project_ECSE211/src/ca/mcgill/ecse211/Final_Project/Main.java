package ca.mcgill.ecse211.Final_Project;

import java.util.HashMap;
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
import ca.mcgill.ecse211.tests.Display;
import ca.mcgill.ecse211.tests.LightLocalizer;
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

public class Main {

	private static final String SERVER_IP = "192.168.2.34";
	private static final int TEAM_NUMBER = 2;
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = false;

	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

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
	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 14.8;
	public static final double[] SR_UR = new double[2];
	public static final double[] SR_LL = new double[2]; 

	// Possible Starting Corners
	public static double startCorner;

	// Starting corner for DEMO
	public static double[] startingCorner = new double[2];
	// Bridge and tunnel coodrdinates
	public static final double[] bridgeLocation_UR = new double[2];
	public static final double[] bridgeLocation_LL = new double[2];
	public static final double[] tunnelLocation_LL = new double[2];
	public static final double[] tunnelLocation_UR = new double[2];
	// Zone coordinates
	public static final double[] red_UR = new double[2];
	public static final double[] red_LL = new double[2];
	public static final double[] green_UR = new double[2];
	public static final double[] green_LL = new double[2];

	// Starting zone (Team)
	public static enum Start_Zone {
		Green_Zone, Red_Zone
	};

	// Color of the zone
	static HashMap<Integer, String> colors = new HashMap<Integer, String>();

	// Set starting zone (for testing)
	public static Start_Zone startZone;
	// public static Start_Zone startZone = Start_Zone.Red_zone;

	// Target Block
	public static BlockColor tb;
	public static BlockColor none = BlockColor.NONE;

	// Flag for odometry Correction
	public static boolean correctionON = false;

	public static void main(String[] args) throws OdometerExceptions {
		// System.out.println("Running");
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
		float[] test = new float[100];

		// set the color mapping
		setColorMapping();
		// initialize all threads
		final TextLCD lcd = LocalEV3.get().getTextLCD();
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		Navigation navigation = new Navigation(odometer, leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
		OdometryCorrection odometryCorrection = new OdometryCorrection();
		LightLocalizer lightLocalizer = new LightLocalizer(navigation, odometer, odometryCorrection);
		LineDetector1 lineDetector1 = new LineDetector1(meanFilter, RGBData, odometryCorrection, odometer);
		LineDetector2 lineDetector2 = new LineDetector2(meanFilter2, RGBData2, odometryCorrection, odometer);
		Thread odoThread = new Thread(odometer);
		UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(State.FALLING_EDGE_STATE, navigation, odometer);
		UltrasonicPoller usPoller = new UltrasonicPoller(medianFilter, usData, usLocalizer);
		Display odometryDisplay = new Display(lcd, usLocalizer, odometryCorrection);
		Thread odoDisplayThread = new Thread(odometryDisplay);
		Thread lineDeterctor1Thread = new Thread(lineDetector1);
		BridgeTunnel bridge = new BridgeTunnel(navigation, odometer, lightLocalizer);
		Thread lineDeterctor2Thread = new Thread(lineDetector2);
		Thread odoCorrectionThread = new Thread(odometryCorrection);
		ColorIdentifier blockColorSensor = new ColorIdentifier(test, tb);
		blockColorSensor.start();

		// Exit early by pressing button
		(new Thread() {
			public void run() {
				while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
				}
				System.exit(0);
			}
		}).start();

		// initialize four threads before receiving parameters from wifi
		odoThread.start();
		odoDisplayThread.start();
		usPoller.start();
		BlockScanner scan = new BlockScanner(navigation, usPoller, odometer);
		lineDeterctor1Thread.start();
		lineDeterctor2Thread.start();
		getWifiParameter();
		lcd.clear();
		usLocalizer.start();
		odoCorrectionThread.start();
		while (!usLocalizer.finished) {
		}
		// start light localization
		lightLocalizer.Localize(true);
		// wait for light localizer to finish
		while (!lightLocalizer.finished) {
		}

		// Go to bridge
		travelBaseOnStartingPosition(bridge, navigation,scan,blockColorSensor);

	}

	private static void assignBlockColor(String color) {
		if (color == "Red") {
			tb = BlockColor.RED;
		} else if (color == "Blue") {
			tb = BlockColor.BLUE;
		} else if (color == "Yellow") {
			tb = BlockColor.YELLOW;
		} else if (color == "White") {
			tb = BlockColor.WHITE;
		}

	}

	private static void travelBaseOnStartingPosition(BridgeTunnel bridge, Navigation nav, BlockScanner scan, ColorIdentifier colorIdentifier) throws OdometerExceptions {
		
		//Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		//Navigation navigation = new Navigation(odometer, leftMotor, rightMotor, WHEEL_RAD, WHEEL_RAD, TRACK);
		//UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(State.FALLING_EDGE_STATE, nav, odometer);
		//UltrasonicPoller usPoller = new UltrasonicPoller(medianFilter, usData, usLocalizer);
		//BlockScanner scan = new BlockScanner(nav, usPoller, odometer);
		Search search = new Search(nav,colorIdentifier, scan);
		if (startZone == Start_Zone.Red_Zone) {
			bridge.travelToBridge();	
			search.start();
			while(!search.isFinished());
			nav.travelToTile(Main.SR_UR[0], Main.SR_UR[1]);
			bridge.travelToTunnel();
		} else {
			bridge.travelToTunnel();
			search.start();
			while(!search.isFinished());
			nav.travelToTile(Main.SR_UR[0], Main.SR_UR[1]);
			bridge.travelToBridge();
		}
		if (startCorner == 0) {
			nav.travelByTileSteps(startingCorner[0], startingCorner[1]);
		} else if (startCorner == 1) {
			nav.travelByTileSteps(startingCorner[0]-1, startingCorner[1]);
		}else if (startCorner == 2) {
			nav.travelByTileSteps(startingCorner[0]-1, startingCorner[1]-1);
		}else if (startCorner == 3) {
			nav.travelByTileSteps(startingCorner[0], startingCorner[1]-1);
		}
	}

	private static void assignStartingCoordinate(double corner) {
		if (corner == 0) {
			startingCorner[0] = 0;
			startingCorner[1] = 0;
		} else if (corner == 1) {
			startingCorner[0] = 8;
			startingCorner[1] = 0;
		} else if (corner == 2) {
			startingCorner[0] = 8;
			startingCorner[1] = 8;
		} else if (corner == 3) {
			startingCorner[0] = 0;
			startingCorner[1] = 8;
		}
	}

	private static void getWifiParameter() {
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		try {
			Map data = conn.getData();
			int redTeam = ((Long) data.get("RedTeam")).intValue();
			int greenTeam = ((Long) data.get("GreenTeam")).intValue();
			int greenOponentFlag = ((Long) data.get("OG")).intValue();
			int redOponentFlag = ((Long) data.get("OR")).intValue();
			// SET THE STARTZONE OF TEAM 2
			// RedCorner
			if (redTeam == 2) {
				startZone = Start_Zone.Red_Zone;
				assignBlockColor(colors.get(redOponentFlag));
				startCorner = ((Long) data.get("RedCorner")).intValue();
				SR_LL[0] = ((Long) data.get("SG_LL_x")).intValue();
				SR_LL[1] = ((Long) data.get("SG_LL_y")).intValue();
				SR_UR[0] = ((Long) data.get("SG_UR_x")).intValue();
				SR_UR[1] = ((Long) data.get("SG_UR_y")).intValue();
			} else if (greenTeam == 2) {
				startZone = Start_Zone.Green_Zone;
				assignBlockColor(colors.get(greenOponentFlag));
				startCorner = ((Long) data.get("GreenCorner")).intValue();
				SR_LL[0] = ((Long) data.get("SR_LL_x")).intValue();
				SR_LL[1] = ((Long) data.get("SR_LL_y")).intValue();
				SR_UR[0] = ((Long) data.get("SR_UR_x")).intValue();
				SR_UR[1] = ((Long) data.get("SR_UR_y")).intValue();
			}

			// assign the starting coordinate
			assignStartingCoordinate(startCorner);

			// set the bridge coordinate
			// TODO check if all the coordinates are within the range of the board
			// for now I am assuming they are going to pass the right inputs
			bridgeLocation_UR[0] = ((Long) data.get("BR_UR_x")).intValue();
			bridgeLocation_UR[1] = ((Long) data.get("BR_UR_y")).intValue();
			bridgeLocation_LL[0] = ((Long) data.get("BR_LL_x")).intValue();
			bridgeLocation_LL[1] = ((Long) data.get("BR_LL_y")).intValue();
			tunnelLocation_LL[0] = ((Long) data.get("TN_LL_x")).intValue();
			tunnelLocation_LL[1] = ((Long) data.get("TN_LL_y")).intValue();
			tunnelLocation_UR[0] = ((Long) data.get("TN_UR_x")).intValue();
			tunnelLocation_UR[1] = ((Long) data.get("TN_UR_y")).intValue();

		} catch (Exception e) {
			// System.err.println("Error: " + e.getMessage());
		}
	}

	private static void setColorMapping() {
		colors.put(1, "Red");
		colors.put(2, "Blue");
		colors.put(3, "Yellow");
		colors.put(4, "White");
	}

}