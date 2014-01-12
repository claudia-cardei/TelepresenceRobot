package telepresence.pathfinding;

import java.util.List;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import telepresence.gui.ImagePanel;
import telepresence.gui.MainFrame;
import telepresence.gui.MapMarker;
import telepresence.map.FloorMap;
import telepresence.qlearning.Action;
import telepresence.qlearning.Constants;
import telepresence.qlearning.Position;
import telepresence.qlearning.QLearner;
import telepresence.qlearning.StateInfo;


/**
 * Reads a map of the environment, builds a path for the robot using BFS and uses the results of Qlearning to 
 * determine the robot's moves.
 * 
 * @author Filip
 *
 */
public class PathFinder extends Thread implements Constants {

	// The Q structure
	public static QLearner learner;
	
	// The robot's position
	Position robot;
	
	// The robot's moves
	ArrayList<String> moves;
	
	// The map
	int n, m;	
	int[][] map;
	
	// The destination
	Position destination;
	
	// The list of markers for the ideal path
	List<MapMarker> markers;
	
	MainFrame mainFrame;
	
    private boolean running = true;
    
    public static final String defaultQFile = "q-old.txt";
	
	
	public PathFinder(String qFile, String mapFile) {
		readQ(qFile);
		readMap(mapFile);
	}
	
	
	/**
	 * Get all the parameters from the graphical interface
	 * @param map
	 * @param robot
	 * @param destination
	 * @param markers
	 * @param mainFrame
	 */
	public PathFinder(FloorMap map, ImagePanel robot, MapMarker destination, List<MapMarker> markers, MainFrame mainFrame) {
    	boolean[][] bitmap = map.getBitmap();
    	int i, j;
    	
    	this.mainFrame = mainFrame;
    	
    	// Initialize map
    	n = bitmap.length;
    	m = bitmap[1].length;
    	this.map = new int[n][m];
    	
		for (i = 0; i < bitmap.length; i++) {
			for (j = 0; j < bitmap[i].length;j++) {
				if ( bitmap[i][j] == false ) 
					this.map[i][j] = 1;
				else
					this.map[i][j] = 0;
			}
		}
		
		// Set robot position
		Point robotPos = robot.getLocation();
		setRobotPosition((int)robotPos.getX(), (int)robotPos.getY(), 270);
		
		// Set  destination
		this.destination = new Position(destination.getCenterX() - IMAGE_SIZE/2, destination.getCenterY() - IMAGE_SIZE/2); 
		System.out.println("Destination: " + destination.getCenterX() + " " + destination.getCenterY());
		
		// Set marker list
		this.markers = markers; 
	}
	
	
	/**
	 * Read Q from a file. This might take a while, as the Q file is very big.
	 * @param fileName the name of the file
	 */
	public void readQ(String fileName) {		
		learner = new QLearner(fileName);		
		
		System.out.println("Q read.");
	}
	
	
	/**
	 * Read the map from a file
	 * @param fileName the file name
	 */
	public void readMap(String fileName) {
		int i, j;
		String line;
		
		try {
			Scanner sc = new Scanner(new File(fileName));
			
			n = sc.nextInt();
			m = sc.nextInt();
			line = sc.nextLine();
			
			map = new int[n][m];
			
			for (i = 0; i < n; i++) {
				line = sc.nextLine();
				
				for (j = 0; j < m; j++)
					// 0 is for a free cell, 1 is for an obstacle
					if ( line.charAt(j) == '0' )
						map[i][j] = 0;
					else
						map[i][j] = 1;
			}
			
			sc.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Set the position of the robot
	 * @param x the OX coordinate
	 * @param y the OY coordinate
	 * @param angle the initial angle
	 */
	public void setRobotPosition(int x, int y, double angle) {
		robot = new Position(x, y, angle);
	}
	
	
	/**
	 * Check if valid. Used for the BFS path finding algorithm. It checks if there are any obstacles in the
	 * window used by the Qlearning algorithm.
	 * 
	 * @param current the current position
	 * @param next the next position
	 * @return true/false
	 */
	public boolean checkMove(Position current, Position next) {
		double dX, dY;
		int i, j, minI, maxI, minJ, maxJ;
		
		// Get the OX and OY difference between the two
		dX = next.getX() - current.getX();
		dY = next.getY() - current.getY();
		
		// Move up
		if ( (dX == 0 && dY == -1) || (dX == 1 && dY == -1) ) {
			// Boundaries for the window
			minI = (int) (current.getY() - (FIRST_SQUARES_UP + SQUARES_RADIUS));
			minJ = (int) (current.getX() - (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			maxJ = (int) (current.getX() + (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			
			// Check if window is within the map
			if ( minI < 0 || minJ < 0 || maxJ >= m )
				return false;

			// Look for obstacles in the window
			for (i = minI; i <= current.getY(); i++)
				for (j = minJ; j <= maxJ; j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		// Move to the right
		else if ( (dX == 1 && dY == 0) || (dX == 1 && dY == 1) ) {
			// Boundaries for the window
			minI = (int) (current.getY() - (FIRST_SQUARES_UP + SQUARES_RADIUS));
			maxI = (int) (current.getY() + (FIRST_SQUARES_UP + SQUARES_RADIUS));
			maxJ = (int) (current.getX() + (FIRST_SQUARES_SIDE + SQUARES_RADIUS));

			// Check if window is within the map
			if ( minI < 0 || maxI >= n || maxJ >= m )
				return false;
			
			// Look for obstacles in the window
			for (i = minI; i <= maxI; i++)
				for (j = (int) current.getX(); j <= maxJ; j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		// Move down
		else if ( (dX == 0 && dY == 1) || (dX == -1 && dY == 1) ) {
			// Boundaries for the window
			maxI = (int) (current.getY() + (FIRST_SQUARES_UP + SQUARES_RADIUS));
			minJ = (int) (current.getX() - (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			maxJ = (int) (current.getX() + (FIRST_SQUARES_SIDE + SQUARES_RADIUS));

			// Check if window is within the map
			if ( maxI >= n || minJ < 0 || maxJ >= m )
				return false;

			// Look for obstacles in the window
			for (i = (int) current.getY(); i <= maxI; i++)
				for (j = minJ; j <= maxJ; j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		// Move to the left
		else if ( (dX == -1 && dY == 0) || (dX == -1 && dY == -1) ) {
			// Boundaries for the window
			minI = (int) (current.getY() - (FIRST_SQUARES_UP + SQUARES_RADIUS));
			maxI = (int) (current.getY() + (FIRST_SQUARES_UP + SQUARES_RADIUS));
			minJ = (int) (current.getX() - (FIRST_SQUARES_SIDE + SQUARES_RADIUS));

			// Check if window is within the map
			if ( minI < 0 || maxI >= n || minJ < 0 )
				return false;

			// Look for obstacles in the window
			for (i = minI; i <= maxI; i++)
				for (j = minJ; j <= (int) current.getX(); j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		/*if ( dX == 0 ) {
			min = (int) (next.getX() - (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2));
			max = (int) (next.getX() + (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2));
			if ( min < 0 || max >= m )
				return false;
			
			for (i = min; i <= max; i++)
				if ( map[(int) next.getY()][i] == 1 )
					return false;
		}
		else if ( dY == 0 ) {
			min = (int) (next.getY() - (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2));
			max = (int) (next.getY() + (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2));
			if ( min < 0 || max >= n )
				return false;
			
			for (i = min; i <= max; i++) {
				if ( map[i][(int) next.getX()] == 1 )
					return false;
			}	
		} 
		else {
			if ( dX == dY ) {
				min = - (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2);
				if ( next.getY() + min < 0 || next.getX() - min >= m )
					return false;
				
				max = (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2);
				if ( next.getY() + max >= n || next.getX() - max < 0 )
					return false;
				
				for (i = min; i <= max; i++) {
					if ( map[(int) next.getY() + i][(int) next.getX() - i] == 1 )
						return false;
				}
			}
			else {
				min = - (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2);
				if ( next.getY() + min < 0 || next.getX() + min < 0 )
					return false;
				
				max = (FIRST_SQUARES_SIDE + FIRST_SQUARES_SIDE / 2);
				if ( next.getY() + max >= n || next.getX() + max >= m )
					return false;
				
				for (i = min; i <= max; i++) {
					if ( map[(int) next.getY() + i][(int) next.getX() + i] == 1 )
						return false;
				}
				
			}			
		}*/
		
		return true;
	}
	
	
	/**
	 * The BFS algorithm that searches for a path between two cells.
	 * @param start the starting position
	 * @param goal the goal position
	 * @return the list of cells on the path
	 */
	public ArrayList<Position> bfs(Position start, Position goal) {
		ArrayList<Position> pathCells = new ArrayList<Position>();
		ArrayList<Position> queue = new ArrayList<Position>();
		ArrayList<Integer> parent = new ArrayList<Integer>();
		boolean[][] visited = new boolean[n][m];
		
		// The available moves
		int[] dx = {0, 1, 0, -1, 1, 1, -1, -1};
		int[] dy = {-1, 0, 1, 0, -1, 1, 1, -1};
		Position neighbour, current;
		boolean goalReached = false;
		int i, j;
		
		queue.add(start);
		parent.add(-1);
		
		System.out.println(start.getX() + " " + start.getY());
		System.out.println(destination.getX() + " " + destination.getY());
		
		i = 0;
		// Use a queue, in which all valid neighbors are added
		while ( i < queue.size() && !goalReached ) {
			current = queue.get(i);
			
			// Add valid neighbors
			for (j = 0; j < dx.length; j++) {
					
				// Get the next move
				neighbour = new Position(current.getX() + dx[j], current.getY() + dy[j]);
				// Check if the position is valid
				if ( neighbour.getX() > -1 && neighbour.getX() < m 
						&& neighbour.getY() > -1 && neighbour.getY() < n
						&& !visited[(int) neighbour.getY()][(int) neighbour.getX()] && checkMove(current, neighbour) ) {
					
					queue.add(neighbour);
					visited[(int) neighbour.getY()][(int) neighbour.getX()] = true;
					parent.add(i);
										
					// Check if it is the goal
					if ( neighbour.equals(goal) ) {
						goalReached = true;
						break;
					}
				}
									
			}
			if ( !goalReached )
				i++;	
		}
		
		// If the goal has been reached, add all the cells from the pfound path
		if ( goalReached ) {
			pathCells.add(goal);
			while ( i != -1 ) {
				pathCells.add(queue.get(i));				
				i = parent.get(i);
			}
		}
		
		return pathCells;
	}
	
	
	/**
	 * Get the orientation for the window, considering the current and next position
	 * @param current the current position
	 * @param next the next position
	 * @return one of four possible orientations
	 */
	public int windowOrientation(Position current, Position next) {
		double dX, dY;

		// Get the OX and OY difference between the two positions
		dX = next.getX() - current.getX();
		dY = next.getY() - current.getY();
		
		if ( dX >= dY && dX > 0 )
				return RIGHT;
		if ( dX > dY &&  dY < 0 )
				return UP;
		if ( dY >= dX && dX < 0 )
			return LEFT;
		else 
			return DOWN;		
	}
	
	
	/**
	 * Get the moves the robot makes in order to reach the first objective, according to the Qlearning results
	 * @param state the current state
	 * @param firstObjective the position of the first objective
	 * @param secondObjective the position of the second objective
	 * @return the final real position of the robot
	 */
	public Position getRobotMoves(StateInfo state, Position firstObjective, Position secondObjective) {
		Long chosenStateAction;
		ArrayList<Action> actions = new ArrayList<Action>();
		Action action;
		String moveText;
		int i = 0;
		
		//System.out.print("New state: " + state.getRobotReal().getAngle() + " " + firstObjective.getX() + ", " + firstObjective.getY() + " " + secondObjective.getX() + ", " + secondObjective.getY() + "\n");

		// While the goal has not been reached
		while ( i < 10 && !state.checkGoal() ) {	
			
			// Select action
			chosenStateAction = learner.selectLearnedAction(state.getActions());
						
			// Change the state accordingly
			action = state.changeState(chosenStateAction);
			actions.add(action);
			
			// Print the action
			if ( action.getType() == ROTATE ) {
				moveText = "ROTATE " + action.getAngle() + " " + action.getTime();
				System.out.println(moveText);
				moves.add(moveText);
			}
			else {
				moveText = "MOVE " + action.getSpeedLeft() + " " + action.getSpeedRight() + " " + action.getTime();
				System.out.println(moveText);
				moves.add(moveText);
			}
			
			if ( i >= 3 && action.equals(actions.get(i - 2)) && actions.get(i - 1).equals(actions.get(i - 3)) )
				break;
			
			i++;
		}
		
		return state.getRobotReal();
	}
	
		
	/**
	 * Move the robot from its initial position to the goal position
	 * @param goal the goal position
	 * @return true if there exists a path, false otherwise
	 */
	public boolean move(Position goal) {
		ArrayList<Position> path;
		Position resultPosition, last, current, firstCell, secondCell, robotReal, firstObjective = null, secondObjective = null;
		int i, windowType, dX, dY;
		double angle;
		StateInfo state = null;
		moves = new ArrayList<String>();
		
		// Set the real position of the robot
		robotReal = new Position(robot.getX() * SQUARE_SIZE + SQUARE_SIZE / 2, robot.getY() * SQUARE_SIZE + SQUARE_SIZE / 2, robot.getAngle());
		
		// Get BFS path
		path = bfs(robot, goal);
		
		// If no path was found
		if ( path.isEmpty() )
			return false;
		
		// Remove non-important cells from the path, but keep the first and last cells
		i = 1;
		last = path.get(0);
		while ( i < path.size() - 1 ) {
			current = path.get(i);
			
			if ( last.distance(current) >= FIRST_SQUARES_UP - 1 || last.distance(current) >= FIRST_SQUARES_SIDE - 1 ) {
				last = current;
				i++;
			}
			else
				path.remove(i);
		}
		
		// Mark the positions of the path cells
		for (i = path.size() - 1; i >= 0; i--) {
			//System.out.println(path.get(i).getX() + " " + path.get(i).getY());
			map[(int) path.get(i).getY()][(int) path.get(i).getX()] = 7;
		}
		
				
		// Apply the learned Q to each window of the path
		for (i = path.size() - 1; i >= 1; i--) {
			current = path.get(i);
			firstCell = path.get(i - 1);
			if ( i - 2 >= 0 )
				secondCell = path.get(i - 2);
			else {
				secondCell = new Position(firstCell.getX() + (firstCell.getX() - robot.getX()), firstCell.getY() + (firstCell.getY() - robot.getY()));
			}
			
			// Get the window orientation
			windowType = windowOrientation(current, firstCell);

			System.out.print("\nTransition: " + current.getX() + " " + current.getY() + "-> " + firstCell.getX() + " " + firstCell.getY() + "-> " + secondCell.getX() + " " + secondCell.getY() + ", window type: " + windowType + "\n");
									
			// Apply different position transformations for each possible orientation of the window
			switch (windowType) {
			
				// Right
				case 0: {
					
					// If the first angle is not appropriate for the movement, make a rotation
					if ( i == path.size() - 1 && !(robot.getAngle() >= 180 && robot.getAngle() < 360) ) {
						robot.addAngle(180);
						robotReal.addAngle(180);
						System.out.println("ROTATE 180");
						moves.add("ROTATE 180");
					}
					
					// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
					angle = robot.getAngle() - 270;
					if ( angle < 0 )
						angle = 360 + angle;
					
					// Get the OX and OY difference between the robot's position and the first objective
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());
					
					// Translate the first objective according to the window orientation
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dX) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Get the OX and OY difference between the robot's position and the second objective
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());

					// Translate the second objective according to the window orientation
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					// Set the window for the Qlearning algorithm
					state = new StateInfo(firstObjective, secondObjective, angle);

					// Use the Qlearning results and get the robot's resulting position
					resultPosition = getRobotMoves(state, firstObjective, secondObjective);

					// Update the robot's real position and cell position 
					robotReal.addX(- (resultPosition.getY() - ROBOT_Y));
					robotReal.addY(+ (resultPosition.getX() - ROBOT_X));
					robotReal.addAngle(resultPosition.getAngle());					
					robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
					robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
					robot.setAngle(robotReal.getAngle());

					System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
					
					break;
				}
				
				// Down
				case 1: {
					
					// If the first angle is not appropriate for the movement, make a rotation
					if ( i == path.size() - 1 && !(robot.getAngle() >= 90 && robot.getAngle() < 270) ) {
						robot.addAngle(180);
						robotReal.addAngle(180);
						System.out.println("ROTATE 180");
						moves.add("ROTATE 180");
					}

					// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
					angle = robot.getAngle() - 180;
					if ( angle < 0 )
						angle = 360 + angle;					

					// Get the OX and OY difference between the robot's position and the first objective
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());

					// Translate the first objective according to the window orientation
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dY) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Get the OX and OY difference between the robot's position and the second objective
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());

					// Translate the second objective according to the window orientation
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dY) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Set the window for the Qlearning algorithm
					state = new StateInfo(firstObjective, secondObjective, angle);

					// Use the Qlearning results and get the robot's resulting position
					resultPosition = getRobotMoves(state, firstObjective, secondObjective);

					// Update the robot's real position and cell position
					robotReal.addX(- (resultPosition.getX() - ROBOT_X));
					robotReal.addY(- (resultPosition.getY() - ROBOT_Y));
					robotReal.addAngle(resultPosition.getAngle());					
					robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
					robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
					robot.setAngle(robotReal.getAngle());

					System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
					
					break;
				}
				
				// Left
				case 2: {	
					
					// If the first angle is not appropriate for the movement, make a rotation
					if ( i == path.size() - 1 && !(robot.getAngle() >= 0 && robot.getAngle() < 180) ) {
						robot.addAngle(180);
						robotReal.addAngle(180);
						System.out.println("ROTATE 180");
						moves.add("ROTATE 180");
					}

					// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
					angle = robot.getAngle() - 90;
					if ( angle < 0 )
						angle = 360 + angle;	

					// Get the OX and OY difference between the robot's position and the first objective
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());

					// Translate the first objective according to the window orientation
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dX) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Get the OX and OY difference between the robot's position and the second objective
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());

					// Translate the second objective according to the window orientation
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dX) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Set the window for the Qlearning algorithm
					state = new StateInfo(firstObjective, secondObjective, angle);

					// Use the Qlearning results and get the robot's resulting position
					resultPosition = getRobotMoves(state, firstObjective, secondObjective);

					// Update the robot's real position and cell position
					robotReal.addX(+ (resultPosition.getY() - ROBOT_Y));
					robotReal.addY(- (resultPosition.getX() - ROBOT_X));
					robotReal.addAngle(resultPosition.getAngle());					
					robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
					robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
					robot.setAngle(robotReal.getAngle());

					System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
					
					break;
				}
				
				// Up
				case 3: {	
					
					// If the first angle is not appropriate for the movement, make a rotation
					if ( i == path.size() - 1 && !(robot.getAngle() >= 270 || robot.getAngle() < 90) ) {
						robot.addAngle(180);
						robotReal.addAngle(180);
						System.out.println("ROTATE 180");
						moves.add("ROTATE 180");
					}

					// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
					angle = robot.getAngle();

					// Get the OX and OY difference between the robot's position and the first objective
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());

					// Translate the first objective according to the window orientation
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dY) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Get the OX and OY difference between the robot's position and the second objective
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());

					// Translate the second objective according to the window orientation
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dY) * SQUARE_SIZE + SQUARE_SIZE / 2);

					// Set the window for the Qlearning algorithm
					state = new StateInfo(firstObjective, secondObjective, angle);
					
					// Use the Qlearning results and get the robot's resulting position
					resultPosition = getRobotMoves(state, firstObjective, secondObjective);

					// Update the robot's real position and cell position
					robotReal.addX(+ (resultPosition.getX() - ROBOT_X));
					robotReal.addY(+ (resultPosition.getY() - ROBOT_Y));
					robotReal.addAngle(resultPosition.getAngle());					
					robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
					robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
					robot.setAngle(robotReal.getAngle());
					
					System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
					
					break;
				}
			}
		}
		
		// Mark the final position of the robot
		map[(int) (robotReal.getY() / SQUARE_SIZE)][(int) (robotReal.getX() / SQUARE_SIZE)] = 5;
		
		// Print the map
		System.out.println();
		for (i = 0; i < n; i++) {
			for (int j = 0; j < m; j++)
				System.out.print(map[i][j]);
			System.out.println();
		}
		
		return true;
	}
	
	
	/**
	 * Get an ideal path using bfs
	 * @return
	 */
	public ArrayList<Position> getPath() {
		ArrayList<Position> path;
		Position last, current;
		int i;
		moves = new ArrayList<String>();
		
		// Get BFS path
		path = bfs(robot, destination);
		
		// If no path was found
		if ( path.isEmpty() ) {
			System.out.println("empty");
			return null;
		}
		
		// Remove non-important cells from the path, but keep the first and last cells
		i = 1;
		last = path.get(0);
		while ( i < path.size() - 1 ) {
			current = path.get(i);
			
			if ( last.distance(current) >= FIRST_SQUARES_UP - 1 || last.distance(current) >= FIRST_SQUARES_SIDE - 1 ) {
				last = current;
				i++;
			}
			else
				path.remove(i);
		}
		
		// Mark the positions of the path cells
		for (i = path.size() - 1; i >= 0; i--) {
			//System.out.println(path.get(i).getX() + " " + path.get(i).getY());
			map[(int) path.get(i).getY()][(int) path.get(i).getX()] = 7;
		}
		
		return path;
	}
	
	
	@Override
    public void run() {

		while ( learner == null ) {
		}

		// Get the BFS path
		ArrayList<Position> path = getPath();
		
		MapMarker marker;
		StateInfo state;
		Position robotReal, current, firstCell, secondCell, firstObjective = null, secondObjective = null, resultPosition;
		int i, windowType, dX, dY;
		double angle;
		
		if ( path != null && !path.isEmpty() ) {
			
			// Set the real position of the robot
			robotReal = new Position(robot.getX() * SQUARE_SIZE + SQUARE_SIZE / 2, robot.getY() * SQUARE_SIZE + SQUARE_SIZE / 2, robot.getAngle());
					
			// Add markers
			for (Position pos:path) {
				marker = new MapMarker((int)pos.getX(), (int)pos.getY(), Color.GREEN);
				markers.add(marker);
				mainFrame.addMarker((int)pos.getX(), (int)pos.getY());
			}
                        mainFrame.repaint();
			System.out.println("Markers added.");
			
			i = path.size() - 1;
			
	        while (running && i >= 1) {
	        	
				current = path.get(i);
				firstCell = path.get(i - 1);
				if ( i - 2 >= 0 )
					secondCell = path.get(i - 2);
				else {
					secondCell = new Position(firstCell.getX() + (firstCell.getX() - robot.getX()), firstCell.getY() + (firstCell.getY() - robot.getY()));
				}
				
				// Get the window orientation
				windowType = windowOrientation(current, firstCell);
	
				System.out.print("\nTransition: " + current.getX() + " " + current.getY() + "-> " + firstCell.getX() + " " + firstCell.getY() + "-> " + secondCell.getX() + " " + secondCell.getY() + ", window type: " + windowType + "\n");
										
				// Apply different position transformations for each possible orientation of the window
				switch (windowType) {
				
					// Right
					case 0: {
						
						// If the first angle is not appropriate for the movement, make a rotation
						if ( i == path.size() - 1 && !(robot.getAngle() >= 180 && robot.getAngle() < 360) ) {
							robot.addAngle(180);
							robotReal.addAngle(180);
							System.out.println("ROTATE 180");
							moves.add("ROTATE 180");
						}
						
						// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
						angle = robot.getAngle() - 270;
						if ( angle < 0 )
							angle = 360 + angle;
						
						// Get the OX and OY difference between the robot's position and the first objective
						dX = (int) (firstCell.getX() - robot.getX());
						dY = (int) (firstCell.getY() - robot.getY());
						
						// Translate the first objective according to the window orientation
						firstObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Get the OX and OY difference between the robot's position and the second objective
						dX = (int) (secondCell.getX() - robot.getX());
						dY = (int) (secondCell.getY() - robot.getY());
	
						// Translate the second objective according to the window orientation
						secondObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
						
						// Set the window for the Qlearning algorithm
						state = new StateInfo(firstObjective, secondObjective, angle);
	
						// Use the Qlearning results and get the robot's resulting position
						resultPosition = getRobotMoves(state, firstObjective, secondObjective);
	
						// Update the robot's real position and cell position 
						robotReal.addX(- (resultPosition.getY() - ROBOT_Y));
						robotReal.addY(+ (resultPosition.getX() - ROBOT_X));
						robotReal.addAngle(resultPosition.getAngle());					
						robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
						robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
						robot.setAngle(robotReal.getAngle());
	
						System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
						
						break;
					}
					
					// Down
					case 1: {
						
						// If the first angle is not appropriate for the movement, make a rotation
						if ( i == path.size() - 1 && !(robot.getAngle() >= 90 && robot.getAngle() < 270) ) {
							robot.addAngle(180);
							robotReal.addAngle(180);
							System.out.println("ROTATE 180");
							moves.add("ROTATE 180");
						}
	
						// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
						angle = robot.getAngle() - 180;
						if ( angle < 0 )
							angle = 360 + angle;					
	
						// Get the OX and OY difference between the robot's position and the first objective
						dX = (int) (firstCell.getX() - robot.getX());
						dY = (int) (firstCell.getY() - robot.getY());
	
						// Translate the first objective according to the window orientation
						firstObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Get the OX and OY difference between the robot's position and the second objective
						dX = (int) (secondCell.getX() - robot.getX());
						dY = (int) (secondCell.getY() - robot.getY());
	
						// Translate the second objective according to the window orientation
						secondObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Set the window for the Qlearning algorithm
						state = new StateInfo(firstObjective, secondObjective, angle);
	
						// Use the Qlearning results and get the robot's resulting position
						resultPosition = getRobotMoves(state, firstObjective, secondObjective);
	
						// Update the robot's real position and cell position
						robotReal.addX(- (resultPosition.getX() - ROBOT_X));
						robotReal.addY(- (resultPosition.getY() - ROBOT_Y));
						robotReal.addAngle(resultPosition.getAngle());					
						robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
						robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
						robot.setAngle(robotReal.getAngle());
	
						System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
						
						break;
					}
					
					// Left
					case 2: {	
						
						// If the first angle is not appropriate for the movement, make a rotation
						if ( i == path.size() - 1 && !(robot.getAngle() >= 0 && robot.getAngle() < 180) ) {
							robot.addAngle(180);
							robotReal.addAngle(180);
							System.out.println("ROTATE 180");
							moves.add("ROTATE 180");
						}
	
						// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
						angle = robot.getAngle() - 90;
						if ( angle < 0 )
							angle = 360 + angle;	
	
						// Get the OX and OY difference between the robot's position and the first objective
						dX = (int) (firstCell.getX() - robot.getX());
						dY = (int) (firstCell.getY() - robot.getY());
	
						// Translate the first objective according to the window orientation
						firstObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Get the OX and OY difference between the robot's position and the second objective
						dX = (int) (secondCell.getX() - robot.getX());
						dY = (int) (secondCell.getY() - robot.getY());
	
						// Translate the second objective according to the window orientation
						secondObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Set the window for the Qlearning algorithm
						state = new StateInfo(firstObjective, secondObjective, angle);
	
						// Use the Qlearning results and get the robot's resulting position
						resultPosition = getRobotMoves(state, firstObjective, secondObjective);
	
						// Update the robot's real position and cell position
						robotReal.addX(+ (resultPosition.getY() - ROBOT_Y));
						robotReal.addY(- (resultPosition.getX() - ROBOT_X));
						robotReal.addAngle(resultPosition.getAngle());					
						robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
						robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
						robot.setAngle(robotReal.getAngle());
	
						System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
						
						break;
					}
					
					// Up
					case 3: {	
						
						// If the first angle is not appropriate for the movement, make a rotation
						if ( i == path.size() - 1 && !(robot.getAngle() >= 270 || robot.getAngle() < 90) ) {
							robot.addAngle(180);
							robotReal.addAngle(180);
							System.out.println("ROTATE 180");
							moves.add("ROTATE 180");
						}
	
						// Change the angle in order to put it in the [-90,90] interval, which was used for Qlearning
						angle = robot.getAngle();
	
						// Get the OX and OY difference between the robot's position and the first objective
						dX = (int) (firstCell.getX() - robot.getX());
						dY = (int) (firstCell.getY() - robot.getY());
	
						// Translate the first objective according to the window orientation
						firstObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Get the OX and OY difference between the robot's position and the second objective
						dX = (int) (secondCell.getX() - robot.getX());
						dY = (int) (secondCell.getY() - robot.getY());
	
						// Translate the second objective according to the window orientation
						secondObjective = new Position(
								(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
								(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
	
						// Set the window for the Qlearning algorithm
						state = new StateInfo(firstObjective, secondObjective, angle);
						
						// Use the Qlearning results and get the robot's resulting position
						resultPosition = getRobotMoves(state, firstObjective, secondObjective);
	
						// Update the robot's real position and cell position
						robotReal.addX(+ (resultPosition.getX() - ROBOT_X));
						robotReal.addY(+ (resultPosition.getY() - ROBOT_Y));
						robotReal.addAngle(resultPosition.getAngle());					
						robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
						robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
						robot.setAngle(robotReal.getAngle());
						
						System.out.println("Result: " + robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle() + "\n");
						
						break;
					}
				}
				
				// Set the robot's new position
				mainFrame.setRobotPosition((int)(robotReal.getX() / SQUARE_SIZE), (int)(robotReal.getY() / SQUARE_SIZE));
				mainFrame.repaint();
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
	        	i--;
	        	
	        	
			}
	               	
	        	
	        
	        if (running) {
	        	System.out.println("Thread closed.");
	        	close();
	        }
		}
		
		else {
			System.out.println("No solution found.");
			close();
		}
    }

    public void close() {
        running = false;
    }
	
	
	/*public static void main(String[] args) {
		PathFinder finder = new PathFinder("q.txt", "map.txt");
		
		// Set the robot's initial position
		finder.setRobotPosition(8, 19, 280);
		
		// Move the robot to a goal position 
		finder.move(new Position(10, 5));
	}*/
}
