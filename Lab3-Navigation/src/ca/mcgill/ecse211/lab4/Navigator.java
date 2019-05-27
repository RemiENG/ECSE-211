package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;

/**
 * This class Navigates the robot through a selected map while avoiding
 * obstacles
 * 
 * @author Remi Carriere
 *
 */
public class Navigator extends Thread {

	Odometer odo;
	UltrasonicPoller usPoller;
	Navigation nav;
	private final double ANGLE_THRESHOLD = 1; // Allowed angle error before correcting
	private final double DISTANCE_THRESHOLD = 2; //// Allowed distance error before changing state
	// Navigation
	private boolean navigating = true;
	MapPoint[] map;
	MapPoint target = null;
	int mapProgress = -1; // keep track of progress through map
	boolean finished = false; // Set to true when map is completed
	// Obstacle avoidance
	private boolean obstacleDetected = false;
	private boolean obstacleAvoided = true;
	// All maps described in lab doc
	static final MapPoint[] map1 = { new MapPoint(0, 2), new MapPoint(1, 1), new MapPoint(2, 2), new MapPoint(2, 1),
			new MapPoint(1, 0) };
	static final MapPoint[] map2 = { new MapPoint(1, 1), new MapPoint(0, 2), new MapPoint(2, 2), new MapPoint(2, 1),
			new MapPoint(1, 0) };
	static final MapPoint[] map3 = { new MapPoint(1, 0), new MapPoint(2, 1), new MapPoint(2, 2), new MapPoint(0, 2),
			new MapPoint(1, 1) };
	static final MapPoint[] map4 = { new MapPoint(0, 1), new MapPoint(1, 2), new MapPoint(1, 0), new MapPoint(2, 1),
			new MapPoint(2, 2) };

	public static final double TILE_SIZE = 30.48;

	// States of Navigator
	public enum state {
		IDLING, CALCULATING, ROTATING, MOVING, AVOIDING, AT_MAP_POINT
	}

	private state cur_state = state.IDLING; // Starts the Navigator at IDLE.

	public Navigator(Navigation driver, Odometer odo, UltrasonicPoller uPoll) {
		this.nav = driver;
		this.odo = odo;
		this.usPoller = uPoll;
		uPoll.setNav(this);
	}

	/**
	 * FSM for navigating through map while avoiding obstacles
	 */
	public void run() {
		while (true) {

			switch (cur_state) {
			case IDLING:
				cur_state = idling();
				break;
			case CALCULATING:
				cur_state = process_Calculating();
				break;
			case ROTATING:
				cur_state = process_rotating();
				break;
			case MOVING:
				cur_state = process_moving();
				break;
			case AT_MAP_POINT:
				cur_state = process_atMapPoint();
				break;
			// default case
			default:
				break;
			}

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Idling state is when we check for the first map point. if the map point
	 * exists, change state to calculating if the map point does not exist, stay
	 * idling
	 * 
	 * @return
	 */
	private state idling() {
		target = getNextMapPoint();
		if (target != null) {
			return state.CALCULATING;
		}
		// Just incase
		return state.IDLING;
	}

	/**
	 * Calculating state is when we compute the path the robot needs to travel.
	 * changes state to rotating or moving based on which correction is needed
	 * 
	 * @return
	 */
	private state process_Calculating() {
		if (Math.abs(currentTargetTheta()) > 2) {
			return state.ROTATING;
		} else if (currentTargetHypot() > 2) {
			return state.MOVING;
		}
		// Just incase
		return state.IDLING;
	}

	/**
	 * Rotating state is when we turn the robot to the correct heading. if heading
	 * is correct, set state to moving, otherwise, stay in rotating state. if map
	 * point is reached, set state to at map point.
	 * 
	 * @return
	 */

	private state process_rotating() {
			// 
			nav.rotate(currentTargetTheta(), false);
		
			// correct angle, switch to moving state
			return state.MOVING;
		
	}

	/**
	 * Moving state is when we move the robot. set to rotating state if too much
	 * error. if map point is reached, set state to at map point.
	 * 
	 * @return
	 */
	private state process_moving() {
			nav.moveTo(currentTargetHypot(), false);
			return state.AT_MAP_POINT;
	}

	/**
	 * at map point state is when we have reached a certain map point
	 * 
	 * @return
	 */
	private state process_atMapPoint() {
		if (currentTargetHypot() < DISTANCE_THRESHOLD) {
			target = getNextMapPoint();
			if (target != null) {
				return state.CALCULATING;
			} else {
				navigating = false;
				Sound.beep();
				return state.IDLING;
			}
		} else {
			return state.MOVING;
		}
	}

	/**
	 * calculates distance to map point
	 * 
	 * @return
	 */
	private double currentTargetHypot() {
		return nav.computeTargetHypot(target.x, target.y, true);
	}

	/**
	 * calculates angle to map point
	 * 
	 * @return
	 */
	private double currentTargetTheta() {
		return nav.computeTargetTheta(target.x, target.y, true);

	}

	/**
	 * Gets the next map point in the map
	 * 
	 * @return
	 */
	private MapPoint getNextMapPoint() {
		if (map != null) {
			if (mapProgress + 1 >= map.length) {
				finished = true;
				return null;
			}
		}
		return map[++mapProgress];
	}

	/**
	 * Gets the target, as a map point.
	 * 
	 * @return
	 */
	public MapPoint getTargetPos() {
		return target;
	}

	/**
	 * Get the current state of the navigator.
	 * 
	 * @return
	 */
	public state getCurrentState() {
		return cur_state;
	}

	/**
	 * sets the desired map i
	 * 
	 * @param i
	 */
	public void setPath(int i) {
		switch (i) {
		case 1:
			this.map = map1;
			break;
		case 2:
			this.map = map2;
			break;
		case 3:
			this.map = map3;
			break;
		case 4:
			this.map = map4;
			break;
		default:
			this.map = map1;
			break;
		}

	}

	/**
	 * returns if an obstacle has been detected
	 * 
	 * @return
	 */
	public synchronized boolean getObstacleDetected() {
		return obstacleDetected;
	}

	/**
	 * Sets the value of obstacleDetected.
	 * 
	 * @param obstacle
	 */
	public synchronized void setObstacleDetected(boolean obstacle) {
		obstacleDetected = obstacle;
	}

	public synchronized boolean isNavigating() {
		return navigating;
	}

	public synchronized void setNavigating(boolean navigating) {
		this.navigating = navigating;
	}

	public void setMap(MapPoint[] mapPoints) {
		map = mapPoints;
	}
}
