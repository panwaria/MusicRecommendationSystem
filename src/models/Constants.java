package models;

/**
 * Contains all the application constants.
 * 
 * @author excelsior
 *
 */
public class Constants {
	// Database connection constants
	public static final String DB_URL = "jdbc:mysql://localhost:3306";
	public static final String DB_NAME = "msdchallenge";
	public static final String DB_USER = "msd_user";
	public static final String DB_PWD  = "msd_user";
	
	// Database table names
	public static final String MSD_TRAIN_DATA_TABLE = "msd_train";
	public static final String MSD_TEST_DATA_TABLE 	= "msd_test";
	
	// Algorithm names
	public static final String TOP_N_POPULAR 		= 	"Overall N-Popular Songs Algorithm";
	public static final String K_NEAREST_NEIGHBOUR 	= 			"K-Nearest Neighbours Algorithm";
	public static final String NAIVE_BAYES 			= 	"Naive Bayes Algorithm";
	
	// Dataset
	public static final String TRAIN_DATASET 			= "train";
	public static final String TEST_VISIBLE_DATASET		= "test_visible";
	public static final String TEST_HIDDEN_DATASET		= "test_hidden";	
}
