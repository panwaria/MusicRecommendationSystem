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
	
	// Columns in msd_train and msd_test tables
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
	public static final String USER_BASED_COLLABORATIVE_FILTERING = "User based collaborative filtering algorithm";
	public static final String ITEM_BASED_COLLABORATIVE_FILTERING = "Item based collaborative filtering algorithm";
	public static final String BAGGING_NAIVE_BAYES 	= "Bagging (Naive Bayes) algorithm";
	public static final String BAGGING_ITEM_BASED 	= "Bagging (Item-Based CollabFiltering) algorithm";
	public static final String BAGGING_KNN 			= "Bagging (KNN) algorithm";
	public static final String BAGGING_USER_BASED	= "Bagging (User-Based CollabFiltering) algorithm";
	
	// Dataset
	public static final String TRAIN_DATASET 			= "train";
	public static final String TEST_VISIBLE_DATASET		= "test_visible";
	public static final String TEST_HIDDEN_DATASET		= "test_hidden";	
}
