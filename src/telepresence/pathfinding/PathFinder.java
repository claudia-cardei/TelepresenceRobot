package telepresence.pathfinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import telepresence.qlearning.Action;
import telepresence.qlearning.Constants;
import telepresence.qlearning.Position;
import telepresence.qlearning.QLearner;
import telepresence.qlearning.State;


public class PathFinder implements Constants {

	// The Q structure
	QLearner learner;
	
	Position robot;
	
	int n, m;
	
	int[][] map;
	
	public PathFinder(String qFile, String mapFile) {
		readQ(qFile);
		readMap(mapFile);
	}
	
	
	/**
	 * Read Q from a file
	 * @param fileName the name of the file
	 */
	public void readQ(String fileName) {		
		learner = new QLearner(fileName);		
		
		System.out.println("Q read.");
	}
	
	
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
	
	
	public void setRobotPosition(int x, int y, double angle) {
		robot = new Position(x, y, angle);
	}
	
	
	public boolean checkMove(Position current, Position next) {
		double dX, dY;
		int i, j, minI, maxI, minJ, maxJ;
		
		dX = next.getX() - current.getX();
		dY = next.getY() - current.getY();
		
		if ( (dX == 0 && dY == -1) || (dX == 1 && dY == -1) ) {
			minI = (int) (current.getY() - (FIRST_SQUARES_UP + SQUARES_RADIUS));
			minJ = (int) (current.getX() - (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			maxJ = (int) (current.getX() + (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			
			if ( minI < 0 || minJ < 0 || maxJ >= m )
				return false;
			
			for (i = minI; i <= current.getY(); i++)
				for (j = minJ; j <= maxJ; j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		else if ( (dX == 1 && dY == 0) || (dX == 1 && dY == 1) ) {
			minI = (int) (current.getY() - (FIRST_SQUARES_UP + SQUARES_RADIUS));
			maxI = (int) (current.getY() + (FIRST_SQUARES_UP + SQUARES_RADIUS));
			maxJ = (int) (current.getX() + (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			
			if ( minI < 0 || maxI >= n || maxJ >= m )
				return false;
			
			for (i = minI; i <= maxI; i++)
				for (j = (int) current.getX(); j <= maxJ; j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		else if ( (dX == 0 && dY == 1) || (dX == -1 && dY == 1) ) {
			maxI = (int) (current.getY() + (FIRST_SQUARES_UP + SQUARES_RADIUS));
			minJ = (int) (current.getX() - (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			maxJ = (int) (current.getX() + (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			
			if ( maxI >= n || minJ < 0 || maxJ >= m )
				return false;
			
			for (i = (int) current.getY(); i <= maxI; i++)
				for (j = minJ; j <= maxJ; j++)
					if ( map[i][j] == 1 )
						return false;
		}
		
		else if ( (dX == -1 && dY == 0) || (dX == -1 && dY == -1) ) {
			minI = (int) (current.getY() - (FIRST_SQUARES_UP + SQUARES_RADIUS));
			maxI = (int) (current.getY() + (FIRST_SQUARES_UP + SQUARES_RADIUS));
			minJ = (int) (current.getX() - (FIRST_SQUARES_SIDE + SQUARES_RADIUS));
			
			if ( minI < 0 || maxI >= n || minJ < 0 )
				return false;
			
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
	
	
	public ArrayList<Position> bfs(Position start, Position goal) {
		ArrayList<Position> pathCells = new ArrayList<Position>();
		ArrayList<Position> queue = new ArrayList<Position>();
		ArrayList<Integer> parent = new ArrayList<Integer>();
		int[] dx = {0, 1, 0, -1, 1, 1, -1, -1};
		int[] dy = {-1, 0, 1, 0, -1, 1, 1, -1};
		Position neighbour, current;
		boolean goalReached = false;
		int i, j;
		
		queue.add(start);
		parent.add(-1);
		
		i = 0;
		while ( i < queue.size() && !goalReached ) {
			current = queue.get(i);
			
			// Add valid neighbours
			for (j = 0; j < dx.length; j++)
					
				if ( dx[j] != 0 || dy[j] != 0 ) {
					neighbour = new Position(current.getX() + dx[j], current.getY() + dy[j]);
					if ( neighbour.getX() > -1 && neighbour.getX() < m 
							&& neighbour.getY() > -1 && neighbour.getY() < n
							&& !queue.contains(neighbour) && checkMove(current, neighbour) ) { 
						queue.add(neighbour);
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
		System.out.println();
		
		if ( goalReached ) {
			pathCells.add(goal);
			while ( i != -1 ) {
				pathCells.add(queue.get(i));				
				i = parent.get(i);
			}
			
			/*for (i = 0; i < pathCells.size(); i++) {
				System.out.println(pathCells.get(i).getX() + " " + pathCells.get(i).getY());
				map[(int) pathCells.get(i).getY()][(int) pathCells.get(i).getX()] = 5;
			}*/
			
			/*for (i = 0; i < n; i++) {
				for (j = 0; j < m; j++)
					System.out.print(map[i][j]);
				System.out.println();
			}*/
		}
		
		return pathCells;
	}
	
	
	public int windowOrientation(Position current, Position next) {
		double dX, dY;
		
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
	
	
	public Position solve(State state, Position firstObjective, Position secondObjective) {
		Long chosenStateAction;
		ArrayList<Action> actions = new ArrayList<Action>();
		Action action;

		// While the goal has not been reached
		while ( !state.checkGoal() ) {	

			// Select action
			chosenStateAction = learner.selectLearnedAction(state.getActions());
						
			// Change the state accordingly
			action = state.changeState(chosenStateAction);
			actions.add(action);
			
			if ( action.getType() == ROTATE )
				System.out.println("ROTATE " + action.getAngle() + " " + action.getTime());
			else
				System.out.println("MOVE " + action.getSpeedLeft() + " " + action.getSpeedRight() + " " + action.getTime());
			
		}
		
		return state.getRobotReal();
	}
	
		
	public boolean getMoves(Position robot, Position goal) {
		ArrayList<Position> path;
		Position resultPosition, last, current, firstCell, secondCell, robotReal, firstObjective = null, secondObjective = null;
		int i, windowType, dX, dY;
		double angle;
		State state = null;
		
		robotReal = new Position(robot.getX() * SQUARE_SIZE + SQUARE_SIZE / 2, robot.getY() * SQUARE_SIZE + SQUARE_SIZE / 2, robot.getAngle());
		
		// Get BFS path
		path = bfs(robot, goal);
		
		if ( path.isEmpty() )
			return false;
		
		// Remove non-important cells from the path
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
		
				
		// Apply the learned Q to each window of the path
		for (i = path.size() - 1; i >= 7; i--) {
			current = path.get(i);
			firstCell = path.get(i - 1);
			if ( i - 2 < 0 )
				secondCell = path.get(i - 1);
			else
				secondCell = path.get(i - 2);
			
			windowType = windowOrientation(current, firstCell);
			
			System.out.println(current.getX() + " " + current.getY());
			System.out.println(firstCell.getX() + " " + firstCell.getY());
			System.out.println(secondCell.getX() + " " + secondCell.getY());
			
			// AAAAAAAAAAAA
			robot = current;
						
			switch (windowType) {
			
				// Right
				case 0: {
					angle = robot.getAngle() - 90;
					if ( angle < 0 )
						angle = 360 + angle;
					
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());
					
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());
					
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					state = new State(firstObjective, secondObjective, angle);
					state.setRobotReal(robotReal);
					
					resultPosition = solve(state, firstObjective, secondObjective);

					robotReal.addX(- (resultPosition.getX() - robotReal.getX()));
					robotReal.addY(+ (resultPosition.getY() - robotReal.getY()));
					robotReal.addAngle(resultPosition.getAngle() + 90);
					
					break;
				}
				
				// Down
				case 1: {			
					angle = robot.getAngle() - 180;
					if ( angle < 0 )
						angle = 360 + angle;					
					
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());
					
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());
					
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP - dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					state = new State(firstObjective, secondObjective, angle);
					state.setRobotReal(robotReal);
					
					/*resultPosition = solve(state, firstObjective, secondObjective);
					
					robotReal.addX(- (resultPosition.getX() - robotReal.getX()));
					robotReal.addY(- (resultPosition.getY() - robotReal.getY()));
					robotReal.addAngle(resultPosition.getAngle() + 180);
					
					robot.setX((int)(robotReal.getX() / SQUARE_SIZE));
					robot.setY((int)(robotReal.getY() / SQUARE_SIZE));
					robot.setAngle(robotReal.getAngle());
					
					System.out.println(robotReal.getX() + " " + robotReal.getY() + " "+ robotReal.getAngle());
					System.out.println(robot.getX() + " " + robot.getY() + " "+ robot.getAngle());
					*/
					break;
				}
				
				// Left
				case 2: {		
					angle = robot.getAngle() - 270;
					if ( angle < 0 )
						angle = 360 + angle;					
					
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());
					
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());
					
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE - dY) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dX) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					state = new State(firstObjective, secondObjective, angle);
					state.setRobotReal(robotReal);
					
					resultPosition = solve(state, firstObjective, secondObjective);

					robotReal.addX(+ (resultPosition.getX() - robotReal.getX()));
					robotReal.addY(- (resultPosition.getY() - robotReal.getY()));
					robotReal.addAngle(resultPosition.getAngle() + 270);
					
					break;
				}
				
				// Up
				case 3: {	
					angle = robot.getAngle();
					
					dX = (int) (firstCell.getX() - robot.getX());
					dY = (int) (firstCell.getY() - robot.getY());
					
					firstObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					dX = (int) (secondCell.getX() - robot.getX());
					dY = (int) (secondCell.getY() - robot.getY());
					
					secondObjective = new Position(
							(SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE + dX) * SQUARE_SIZE + SQUARE_SIZE / 2, 
							(SECOND_SQUARES_UP + FIRST_SQUARES_UP + dY) * SQUARE_SIZE + SQUARE_SIZE / 2);
					
					state = new State(firstObjective, secondObjective, angle);
					state.setRobotReal(robotReal);
					
					resultPosition = solve(state, firstObjective, secondObjective);

					robotReal.addX(+ (resultPosition.getX() - robotReal.getX()));
					robotReal.addY(+ (resultPosition.getY() - robotReal.getY()));
					robotReal.addAngle(resultPosition.getAngle());
					
					break;
				}
			}
			
			map[(int) path.get(i).getY()][(int) path.get(i).getX()] = 6;
		}
		
		for (i = 0; i < n; i++) {
			for (int j = 0; j < m; j++)
				System.out.print(map[i][j]);
			System.out.println();
		}
		
		return true;
	}
	
	
	public static void main(String[] args) {
		PathFinder finder = new PathFinder("q.txt", "map.txt");
		
		finder.setRobotPosition(8, 15, 0);
		
		finder.getMoves(finder.robot, new Position(25, 6));
	}
}
