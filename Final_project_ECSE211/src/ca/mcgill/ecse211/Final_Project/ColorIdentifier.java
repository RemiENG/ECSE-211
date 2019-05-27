package ca.mcgill.ecse211.Final_Project;

import java.util.Arrays;
import java.io.*;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;

/**
 * This class is responsible for providing filtered light data to light localizer
 *
 *
 */
public class ColorIdentifier extends Thread {

	private float[] lightData;
	private float lightLevel = -1f;
	private static final Port blockColorPort = LocalEV3.get().getPort("S4");
	EV3ColorSensor colorSensor = new EV3ColorSensor(blockColorPort);
	SampleProvider sample = colorSensor.getColorIDMode();
	public boolean blockDetected = false; // true if a colored block is detected
	// possible colors of blocks

	/**
	 * Enum of all possible colours for the Block
	 */
	public static enum BlockColor {
		RED, BLUE, YELLOW, WHITE, NONE
	};

	private BlockColor tb;
	public boolean tbDetected = false;
	public BlockColor blockColor = BlockColor.NONE;
	/**
	 * Constructor for ColorIdentifier 
	 * @param filteredLightData
	 * @param tb
	 */
	public ColorIdentifier(float[] filteredLightData, BlockColor tb) {
		this.lightData = filteredLightData;
		this.tb = tb;
	}
/**
 *Running ColorIdentifier to filter light data to the light localizer.
 */
	public void run() {
		while (true) {
			sample.fetchSample(lightData, 0);
			this.lightLevel = lightData[0];
			if (lightData[0] == 2) {
				blockDetected = true;
				blockColor = BlockColor.BLUE;
			}
			if (lightData[0] == 0) {
				blockDetected = true;
				blockColor = BlockColor.RED;
			}
			if (lightData[0] == 3) {
				blockDetected = true;
				blockColor = BlockColor.YELLOW;
			}
			if (lightData[0] == 6) {
				blockDetected = true;
				blockColor = BlockColor.WHITE;
			}
			// if we are not detecting a colored block
			else if ((lightData[0] != 0) && (lightData[0] != 2) && (lightData[0] != 3) && (lightData[0] != 6)) {
				blockDetected = false;
				blockColor = BlockColor.NONE;
			}

			sleepThread(0.050f); // Poor man's timed sampling
		}
	}

	/**
	 * Responsible for making a thread sleep for specific amount of time
	 * @param seconds
	 */
	
	public void sleepThread(float seconds) {
		try {
			Thread.sleep((long) (seconds * 1000f));
		} catch (Exception e) {
		}
	}
	/**
	 * 
	 * @return boolean is true if the block is detected 
	 */
	public synchronized boolean getBlockDetected() {
		return this.blockDetected;
	}
	/**
	 * 
	 * @return block color
	 */
	public synchronized BlockColor getBlockColor() {
		return this.blockColor;
	}
}
