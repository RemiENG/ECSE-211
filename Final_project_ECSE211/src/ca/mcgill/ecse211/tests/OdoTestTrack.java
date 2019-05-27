/*
 * SquareDriver.java
 */
package ca.mcgill.ecse211.tests;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is used to drive the robot on the demo floor.
 */
public class OdoTestTrack {

  private static final int ROTATE_SPEED = 300;


  /**
   * This method is meant to drive the robot in a square of size 2x2 Tiles. It is to run in parallel
   * with the odometer and Odometer correcton classes allow testing their functionality.
   * 
   * @param leftMotor
   * @param rightMotor
   * @param leftRadius
   * @param rightRadius
   * @param width
   */
  public static void drive(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      double leftRadius, double rightRadius, double track) {
    // reset the motors
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
      motor.stop();
      motor.setAcceleration(400); //SLowed acceleration to prevent wheel slip
    }

    // Sleep for 2 seconds
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // There is nothing to be done here
    }

    for (int i = 0; i < 1; i++) {

      // turn 1080 degrees clockwise
      leftMotor.setSpeed(ROTATE_SPEED);
      rightMotor.setSpeed(ROTATE_SPEED);

      leftMotor.rotate(convertAngle(leftRadius, track, 1080), true);
      rightMotor.rotate(-convertAngle(rightRadius, track, 1080), false);
      
    }
  }

  /**
   * This method allows the conversion of a distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius		value of the radius
   * @param distance		value of the distance  
   * @return
   */
  private static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  /**
   * 
   * @param radius
   * @param width
   * @param angle
   * @return an int of the converted angle
   */
  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }
}
