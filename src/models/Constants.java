package models;

/**
 * Contains all the application constants.
 * 
 * @author excelsior
 *
 */
public class Constants {
	// Database connection constants
	public static final String DB_URL 	= "jdbc:mysql://localhost:3306";
	public static final String DB_NAME 	= "msdchallenge";
	public static final String DB_USER 	= "msd_user";
	public static final String DB_PWD  	= "msd_user";
	
	// Fields/Columns in msd_train and msd_test tables
	public static final String COLUMN_USER_ID 		= "user_id";
	public static final String COLUMN_SONG_ID 		= "song_id";
	public static final String COLUMN_PLAY_COUNT 	= "play_count";
	
	// Database table names
	public static final String MSD_TRAIN_DATA_TABLE = "msd_train";
	public static final String MSD_TEST_DATA_TABLE 	= "msd_test";
	
	// Algorithm names
	public static final String TOP_N_POPULAR 		= 	"Overall N-Popular Songs Algorithm";
	public static final String K_NEAREST_NEIGHBOUR 	= 	"K-Nearest Neighbours Algorithm";
	public static final String NAIVE_BAYES 			= 	"Naive Bayes Algorithm";
	
	// Dataset
	public static final String TRAIN_DATASET 	= "train";
	public static final String TUNE_DATASET		= "tune";
	public static final String TEST_DATASET		= "test";	
}
