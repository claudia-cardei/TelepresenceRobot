package telepresence.qlearning;
import java.util.ArrayList;

/**
 * Implements a state of the Q-learning algorithm
 * @author Filip
 *
 */
public class State implements Constants {

	Position robot;
	Position robotReal;
	Position firstObjective;
	Position secondObjective;

	// The time for the last action
	double actionTime;
	
	// The list of all possible actions
	ArrayList<Action> possibleActions;
	
	// The State encoder
	StateActionEncoding encoder;
	
	
	public State(Position firstObjective, Position secondObjective, double angle) {
		
		actionTime = 0;
		// Initialize the positions of the robot and the first and second objective
		robot = new Position(ROBOT_X, ROBOT_Y, angle);
		robotReal = new Position(ROBOT_X, ROBOT_Y, angle);
		this.firstObjective = firstObjective;
		this.secondObjective = secondObjective;
		
		// Generate all possible actions
		generatePossibleActions();
		// Get a new State encoder
		encoder = new StateActionEncoding(possibleActions.size());
	}
	
	
	/**
	 * Generate the list of all possible actions
	 */
	public void generatePossibleActions() {
		double angle, stepLeft, stepRight, speedLeft, speedRight;
		int i, j;
		
		possibleActions = new ArrayList<Action>();
		
		// Add all possible rotations
		angle = FIRST_ANGLE;
		possibleActions.add(new Action(angle, ROTATE_PERIOD));
		
		for (i = 0; i < NR_ROTATIONS; i++) {
			angle += ANGLE_STEP;
			possibleActions.add(new Action(angle, Math.abs(angle * ROTATE_PERIOD / FIRST_ANGLE)));
		}
		
		// Add possible movements with different speed for the wheels
		stepLeft = 0;		
		for (i = 0; i <= 1 / SPEED_STEP; i++) {

			speedLeft = stepLeft * 10 * MAX_SPEED / 10;
			stepRight = 0;
			for (j = 0; j <= 1 / SPEED_STEP; j++) { 
				speedRight = stepRight * 10 * MAX_SPEED / 10;
				
				if ( speedLeft != speedRight && speedLeft + speedRight >= MIN_SPEED_SUM )
					possibleActions.add(new Action(speedLeft, speedRight, MOVE_PERIOD));
												
				stepRight += SPEED_STEP;
			}
			stepLeft += SPEED_STEP;
		}
		
		// Add all possible straight forward movements
		//possibleActions.add(new Action(MAX_SPEED, MAX_SPEED, MOVE_PERIOD * SPEED_STEP));
		for (i = 1; i <= 1 / SPEED_STEP; i++) {
			possibleActions.add(new Action(MAX_SPEED, MAX_SPEED, MOVE_PERIOD * SPEED_STEP * i));
		}
	}
	
	
	/**
	 * Check if an actions is valid
	 * @param action the action
	 * @return true/false
	 */
	public boolean checkAction(Action action) {
		Position pos = action.getMovement(robot.angle);
		double dx = pos.getX(), dy = pos.getY();
		double firstX, firstY, secondX, secondY;
		
		// Check movement
		firstX = firstObjective.getX() - dx;
		firstY = firstObjective.getY() + dy;
		secondX = secondObjective.getX() - dx;
		secondY = secondObjective.getY() + dy;
			
		if ( firstX < SECOND_SQUARES_SIDE * SQUARE_SIZE + SQUARE_SIZE / 2 
				|| firstX > (2 * FIRST_SQUARES_SIDE + SECOND_SQUARES_SIDE) * SQUARE_SIZE + SQUARE_SIZE / 2
				|| firstY < SECOND_SQUARES_UP * SQUARE_SIZE + SQUARE_SIZE / 2
				|| firstY > (SECOND_SQUARES_UP + FIRST_SQUARES_UP + FIRST_SQUARES_DOWN) * SQUARE_SIZE + SQUARE_SIZE / 2 
				|| secondX < SQUARE_SIZE / 2
				|| secondX > (2 * SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE) * SQUARE_SIZE + SQUARE_SIZE
				|| secondY < SQUARE_SIZE / 2
				|| secondY > (SECOND_SQUARES_UP + FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + SECOND_SQUARES_DOWN) * SQUARE_SIZE + SQUARE_SIZE / 2 )
			return false;
				
		// Check angle
		/*double newAngle = robot.angle + pos.angle;
		if ( newAngle < 0 )
			newAngle += 360;
		if ( newAngle > 360 )
			newAngle -= 360;*/
		
		return true;
	}
	
	
	/**
	 * Get a list of all valid actions for the current state
	 * @return the list of state-action encodings
	 */
	public ArrayList<Long> getActions() {
		int i;
		long encoding;
		Action action;
		ArrayList<Long> validActions = new ArrayList<Long>();
				
		// For all possible actions
		for (i = 0; i < possibleActions.size(); i++) {
			action = possibleActions.get(i);
			
			// If the actions is valid
			if ( checkAction(action) ) {
				// Get the state-action encoding
				encoding = encoder.encode(this, i);
				// Add the action to the valid actions list
				validActions.add(encoding);
			}
		}
		
		return validActions;
	}
	
	
	/**
	 * Change the current state according to an action
	 * @param actionId the state-action encoding
	 * @return the action
	 */
	public Action changeState(Long actionId) {
		Action action = possibleActions.get(encoder.decode(actionId));
		Position pos = action.getMovement(robot.angle);
		
		actionTime = action.time;
			
		/*if ( action.type == ROTATE )
			System.out.println("ROTATE " + action.angle + " " + action.time);
		else
			System.out.println("MOVE " + pos.x + ", " + pos.y + " " + action.speedLeft + " " + action.speedRight + " " + action.time);
		*/
		
		// Change the angle
		robot.angle += pos.angle;
		if ( robot.angle < 0 )
			robot.angle += 360;
		if ( robot.angle >= 360 )
			robot.angle -= 360;
		
		// Change the position of the objectives according to the robots movement
		firstObjective.x -= pos.x;
		secondObjective.x -= pos.x;
		
		firstObjective.y += pos.y;
		secondObjective.y += pos.y;
		
		// Also keep the real position of the robot
		robotReal.x += pos.x;
		robotReal.y -= pos.y;
		robotReal.angle = robot.angle;
		
		//System.out.println("\tAngle = " + robot.angle + ", x = " + robotReal.x + ", y = " + robotReal.y + ", Distance = " + robot.distance(firstObjective) + ", D2 =  " + robot.distance(secondObjective));
		
		return action;
	}
	
	
	/**
	 * Check if the goal state has been reached
	 * @return
	 */
	public boolean checkGoal() {
		
		// If the distance between the robot and the first objective is small enough
		if ( robot.distance(firstObjective) < 1.5 * SQUARE_SIZE )
			return true;
		else
			return false;
	}
	
		
	/**
	 * Get the reward for the current state
	 * @return
	 */
	public double getReward() {
		double reward = 0;
		
		// Penalize time
		reward = -actionTime;
		
		if ( robot.distance(firstObjective) < 1.5 * SQUARE_SIZE )
			reward += (OBJECTVE_REWARD - robot.distance(secondObjective));
		
		return reward;
	}
	
	
	public void setRobotReal(Position robot) {
		robotReal.x = robot.x;
		robotReal.y = robot.y;
	}
	
	
	public Position getRobotReal() {
		return robotReal;
	}
}
