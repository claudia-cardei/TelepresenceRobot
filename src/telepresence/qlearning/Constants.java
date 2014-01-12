package telepresence.qlearning;

public interface Constants {

	// Robot
	public static final double MAX_SPEED = 40;
	public static final double DIAMETER = 45.72;	
	public static final int SQUARES_RADIUS = 0;
	public static final int IMAGE_SIZE = 40;
	
	// Q-learning
	public static final double ALPHA = 0.4;
	public static final double GAMMA = 1;
	public static final int MIN_SCORE = -Integer.MIN_VALUE;
	public static final int OBJECTVE_REWARD = 100;
	public static final double RANDOM_ACTION_PERCENT = 35;
	public static final double NR_RUNS = 300;
	
	// Action types
	public static final int ROTATE = 0;
	public static final int MOVE = 1;
	
	// Actions
	public static final int NR_ROTATIONS = 9;
	public static final int ANGLE_STEP = 20;
	public static final double FIRST_ANGLE = -90;
	public static final double SPEED_STEP = 0.25;
	public static final double MOVE_PERIOD = 1;
	public static final double ROTATE_PERIOD = 1;
	public static final int ANGLE_PRECISION = 10;
	public static final int MIN_SPEED_SUM = 30;
	
	// Map
	public static int N = 5;
	public static int SQUARE_SIZE = 10;
	public static int FIRST_SQUARES_UP = 5;
	public static int FIRST_SQUARES_DOWN = 0;
	public static int FIRST_SQUARES_SIDE = 5;
	public static int SECOND_SQUARES_UP = 5;
	public static int SECOND_SQUARES_DOWN = 0;
	public static int SECOND_SQUARES_SIDE = 5;
	public static double ROBOT_X = (SECOND_SQUARES_SIDE + FIRST_SQUARES_SIDE) * SQUARE_SIZE + SQUARE_SIZE / 2;
	public static double ROBOT_Y = (SECOND_SQUARES_UP + FIRST_SQUARES_UP) * SQUARE_SIZE + SQUARE_SIZE / 2;
	
	// Window
	public static int RIGHT = 0;
	public static int DOWN = 1;
	public static int LEFT = 2;
	public static int UP = 3;
}
