package telepresence.qlearning;

/**
 * The encoding for a state-action pair
 * @author Filip
 *
 */
public class StateActionEncoding implements Constants {

	// The number of possible actions
	int possibleActions;

	public StateActionEncoding(int possibleActions) {
		this.possibleActions = possibleActions;
	}
	
	
	/**
	 * Transform a coordinate into state coordinates
	 * @param c the coordinate
	 * @return the state coordinate
	 */
	public int stateCoord(double c) {
		return (int) (c / SQUARE_SIZE);
	}
	
	
	/**
	 * Encode a state-action pair
	 * @param state the state
	 * @param action the action
	 * @return the encoding
	 */
	public long encode(State state, int action) {
		int n, encoding;
		encoding = 0;
		Position pos;
		
		// Action
		encoding += action;
		n = possibleActions;
		
		// First objective
		pos = state.firstObjective;
		// x
		encoding += n * (stateCoord(pos.getX()) - SECOND_SQUARES_SIDE); 
		n = n * (2 * FIRST_SQUARES_SIDE + 1);
		// y
		encoding += n * (stateCoord(pos.getY()) - SECOND_SQUARES_UP); 
		n = n * (FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + 1);

		// Second objective
		pos = state.secondObjective;
		// x
		encoding += n * stateCoord(pos.getX()); 
		n = n * (2 * SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE + 1);
		// y
		encoding += n * stateCoord(pos.getY()); 
		n = n * (SECOND_SQUARES_UP + FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + SECOND_SQUARES_DOWN + 1);
		
		// Robot angle
		pos = state.robot;
		encoding += n * ((int)(pos.angle + (ANGLE_PRECISION / 2)) / ANGLE_PRECISION);
		
		return encoding;
	}
	
	
	/**
	 * Decode an encoded state-action pair
	 * @param encoding the encoding
	 * @return the action
	 */
	public int decode(long encoding) {
		int action, n = possibleActions 
				* (2 * FIRST_SQUARES_SIDE + 1) 
				* (FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + 1)
				* (2 * SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE + 1) 
				* (SECOND_SQUARES_UP + FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + SECOND_SQUARES_DOWN + 1);
		
		// Angle
		encoding = encoding % n;
		n =  n / (SECOND_SQUARES_UP + FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + SECOND_SQUARES_DOWN + 1);
		
		// Second objective
		// y
		encoding = encoding % n;
		n = n / (2 * SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE + 1);
		// x
		encoding = encoding % n;
		n = n / (FIRST_SQUARES_UP + FIRST_SQUARES_DOWN + 1);
		
		// First objective
		// y
		encoding = encoding % n;
		n = n / (2 * FIRST_SQUARES_SIDE + 1);
		
		// x
		encoding = encoding % n;
		n = n / possibleActions;
		
		// Action
		action = (int) (encoding / n);
		
		return action;
	}
}
