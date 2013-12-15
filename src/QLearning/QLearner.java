import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Implements the Q learning algorithm
 * @author Filip
 *
 */
public class QLearner implements Constants {
	
	// The Q structure
	TreeMap<Long, Double> Q;
	// The name of the file containing the Q
	String fileName;
	
	
	/**
	 * Constructor
	 * @param fileName the name of the file containing Q
	 */
	public QLearner(String fileName) {
		this.fileName = fileName;
		Q = readQ(fileName);
	}
	
	
	/**
	 * Read Q from a file
	 * @param fileName the name of the file
	 * @return the Q structure
	 */
	public TreeMap<Long, Double> readQ(String fileName) {
		String[] row;
		Long state;
		Double value;
		TreeMap<Long, Double> Q = new TreeMap<Long, Double>();
		
		try {
			File file = new File(fileName);
			Scanner sc = new Scanner(file);
			
			while ( sc.hasNextLine() ) {
				row = sc.nextLine().split(" ");
				state = Long.parseLong(row[0]);
				value = Double.parseDouble(row[1]);
				
				Q.put(state, value);
			}
			
		} catch (FileNotFoundException e) {
		}		
		
		return Q;
	}
	
	
	/**
	 * Write the Q to a file
	 * @param fileName the name of the file
	 */
	public void writeQ(String fileName) {

		try {
			PrintWriter pr = new PrintWriter(new File(fileName));
			
			Set<Entry<Long, Double>> set = Q.entrySet();
			Iterator<Entry<Long, Double>> iterator = set.iterator();
			Entry<Long, Double> entry;
			
			while ( iterator.hasNext() ) {
				entry = iterator.next();
				
				pr.println(entry.getKey() + " " + entry.getValue());
			}
			
			pr.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Generate an initial state for the algorithm
	 * @param x1 OX coordinate for the first objective
	 * @param y1 OY coordinate for the first objective
	 * @param x2 OX coordinate for the second objective
	 * @param y2 OY coordinate for the second objective
	 * @return the generated state
	 */
	public State generateInitialState(int x1, int y1, int x2, int y2) {
		Position firstObjective = new Position(x1, y1);
		Position secondObjective = new Position(x2, y2);
		
		State initialState = new State(firstObjective, secondObjective);
		
		return initialState;
	}
	
	
	/**
	 * Inner class, used for making a pair between an action and its score
	 * @author Filip
	 *
	 */
	private class SelectionResult {
		private long action;
		private double score;
		
		public SelectionResult(long action, double score) {
			this.setAction(action);
			this.setScore(score);
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		public long getAction() {
			return action;
		}

		public void setAction(long action) {
			this.action = action;
		}
	}
	
	
	/**
	 * Get the maximum score of the next possible combinations of actions and states
	 * @param stateActions the next possible state-actions
	 * @return the maximum score
	 */
	public double getMaxScore(ArrayList<Long> statesActions) {
		double maxScore, score;
		
		maxScore = MIN_SCORE;
			
		for (Long stateActions:statesActions) { 
			if ( Q.containsKey(stateActions) ) {
				score = Q.get(stateActions);
			}
			else {
				// If the state-action has not been explored, its score is 0
				score = 0;
			}
			
			if ( score > maxScore ) {
				maxScore = score;
			}
		}
		
		return maxScore;
	}
	
	
	/**
	 * Select an new action
	 * @param stateActions the next possible state-actions 
	 * @return the action and its score
	 */
	public SelectionResult selectAction(ArrayList<Long> statesActions) {
		double maxScore, score;
		long chosenStateAction = 0;
		Random r = new Random();
		ArrayList<Long> unusedAction = new ArrayList<Long>();
		
		// Pick a random action with a certain probability
		if ( r.nextInt(100) < RANDOM_ACTION_PERCENT ) {
			// Pick random action
			chosenStateAction = statesActions.get(r.nextInt(statesActions.size()));
			if ( Q.containsKey(chosenStateAction) )
				maxScore = Q.get(chosenStateAction);
			else
				// If it has not yet been explored, its score is 0
				maxScore = 0;
		}
		
		else {
			maxScore = MIN_SCORE;
			
			for (Long stateAction:statesActions) { 
				if ( Q.containsKey(stateAction) ) {
					score = Q.get(stateAction);
				}
				else {
					// If it has not yet been explored, its score is 0
					unusedAction.add(stateAction);
					score = 0;
				}
				
				if ( score > maxScore ) {
					maxScore = score;
					chosenStateAction = stateAction;
				}
			}
			
			// If the maximum score is 0, which is the score for unexplored states, randomly pick an unexplored
			// state.
			if ( maxScore == 0 && unusedAction.size() > 0 ) {
				chosenStateAction = unusedAction.get(r.nextInt(unusedAction.size()));
			}
		}
		
		return new SelectionResult(chosenStateAction, maxScore);
	}
	
	
	/**
	 * Learn a path using the Q-learning algorithm
	 * @param x1 OX coordinate for the first objective
	 * @param y1 OY coordinate for the first objective
	 * @param x2 OX coordinate for the second objective
	 * @param y2 OY coordinate for the second objective
	 * @return the number of states used for the path
	 */
	public int learn(int x1, int y1, int x2, int y2) {
		Long chosenStateAction;
		SelectionResult result;
		int steps = 0;
		State state = generateInitialState(x1, y1, x2, y2);
		double oldScore, newScore, maxScore;
		ArrayList<Long> actions;
		
		// While the goal has not been reached
		while ( !state.checkGoal() ) {	

			// Select action
			result = selectAction(state.getActions());
			oldScore = result.getScore();
			chosenStateAction = result.getAction();
						
			// Change the state accordingly
			state.changeState(chosenStateAction);
			
			// Update Q
			if ( !state.checkGoal() ) { 
				actions = state.getActions();
				maxScore = getMaxScore(actions);
			}
			else 
				// If the goal has been reached, do not calculate the maximum score for new actions
				maxScore = 0;
			
			// The Q-learning formula
			newScore = oldScore + ALPHA * (state.getReward() + GAMMA * maxScore - oldScore);
			Q.put(chosenStateAction, newScore);
			
			// Increase the number of steps
			steps++;			
		}
		
		// Write the new Q to a file
		writeQ(fileName);
		
		return steps;
	}
	
	
	/**
	 * Main method, used to launch different scenrios
	 * @param args
	 */
	public static void main(String[] args) {
		QLearner learner = new QLearner("q.txt");
		int y1, y2, x1, x2, k;
		double distance;
		
		/*i1 = (SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE) * SQUARE_SIZE + SQUARE_SIZE / 2; 
		j1 = (SECOND_SQUARES_UP) * SQUARE_SIZE + SQUARE_SIZE / 2;
		i2 = (SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE) * SQUARE_SIZE + SQUARE_SIZE / 2 + 4;
		j2 = 0;
		
		for (i = 0; i < 100; i++) {
			steps = learner.learn(i1, j1, i2, j2);
			if ( steps == -1 ) {
				System.out.println("Break!");
				break;
			}
			System.out.println(i + " " + steps + "\n--------------------------");
			totalSteps += steps;
		}
		
		System.out.println("Average steps: " + (double)totalSteps / 100);*/
		
				
		// For every possible position for the first objective
		for (y1 = SECOND_SQUARES_UP; y1 < SECOND_SQUARES_UP + FIRST_SQUARES_UP + 1 + FIRST_SQUARES_DOWN; y1++) {
			for (x1 = SECOND_SQUARES_SIDE; x1 < SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE + 1; x1++) {
				
				// For every possible position of the second objective
				for (y2 = 0; y2 < SECOND_SQUARES_UP + FIRST_SQUARES_UP + 1 + FIRST_SQUARES_DOWN + SECOND_SQUARES_DOWN; y2++) {
					for (x2 = 0; x2 < 2 * SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE + 1; x2++) {
						
						// Calculate the distance (in squares) between the two objectives
						distance = Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
						
						// Check if the distance is within the boundaries and if the second objective is where it
						// should be.
						if ( distance >= SECOND_SQUARES_SIDE - 2 && distance <= SECOND_SQUARES_SIDE + 2 
								&& !( x2 >= SECOND_SQUARES_SIDE && x2 <= SECOND_SQUARES_SIDE + 2 * FIRST_SQUARES_SIDE
										&& y2 >= SECOND_SQUARES_UP && y2 <= SECOND_SQUARES_UP + FIRST_SQUARES_UP ) ) {
							
							// For each combination of objectives run the algorithm NR_RUNS times
							for (k = 0; k < NR_RUNS; k++) {
								
								learner.learn(x1 * SQUARE_SIZE + SQUARE_SIZE / 2, 
										y1 * SQUARE_SIZE + SQUARE_SIZE / 2, 
										x2 * SQUARE_SIZE + SQUARE_SIZE / 2, 
										y2 * SQUARE_SIZE + SQUARE_SIZE / 2);
							}
							
							System.out.println("Combination:  First("+x1+","+y1+") Second("+x2+","+y2+")");
						}
					}
				}
			}
		}
	}
}
