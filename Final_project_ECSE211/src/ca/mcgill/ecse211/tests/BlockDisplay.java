package ca.mcgill.ecse211.tests;

import ca.mcgill.ecse211.Final_Project.ColorIdentifier;
import lejos.hardware.lcd.TextLCD;

/**
 * 
 * This class is responsible for displaying the color of block being detected.
 *
 */

public class BlockDisplay implements Runnable {

	private TextLCD lcd;
	ColorIdentifier rgb;
	private final long DISPLAY_PERIOD = 25;
	private long timeout = Long.MAX_VALUE;

	/**
	 * Constructor for BlockDisplay.
	 * 
	 * @param lcd
	 * @param rgb
	 */
	public BlockDisplay(TextLCD lcd, ColorIdentifier rgb) {
		this.rgb = rgb;
		this.lcd = lcd;
	}

	/**
	 * @param lcd
	 * @param timeout
	 */
	public BlockDisplay(TextLCD lcd, long timeout) {
		this.timeout = timeout;
		this.lcd = lcd;
	}
	/**
	 *Running Block Display.
	 */
	public void run() {

		lcd.clear();

		long updateStart, updateEnd;

		long tStart = System.currentTimeMillis();
		do {
			updateStart = System.currentTimeMillis();
			lcd.clear();
			lcd.drawString("Block: " + rgb.blockDetected, 0, 4);
			lcd.drawString("Color: " + rgb.blockColor.toString(), 0, 5);
			lcd.drawString("Location: " + "to do", 0, 6);

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
