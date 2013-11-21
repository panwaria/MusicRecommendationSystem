import java.util.List;
import java.util.Map;

/**
 * This is the MAIN class of the project which aims at giving music recommendations to the users
 * in the test set according to data provided in the training set.
 */
public class MusicRecommender 
{
	/* Member Variables */
	
	public static int mRecommendationCount;	// Number of Songs to be recommended to each use
	
	// DataSets
	public static DataSet mTrainSet = null;
	public static DataSet mTestSet = null;
	
	// Algorithms
	public static Algorithm mAlgoTopNSongs = null;
	public static Algorithm mAlgoKNN = null;
	public static Algorithm mAlgoNaiveBayes = null;
	
	// DB Reader
	public static DBReader mDBReader = null;
	
	/* Methods */

	/**
	 * Main mehtod which will execute different Recommendation Algorithms and compare their results.
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// Initialize number of songs to recommend
		mRecommendationCount = 10;
		
		// Create a DBReader
		mDBReader = new DBReader();
		
		// Get Train and Test DataSets using DBReader
		String trainDatabasePath = "";
		mTrainSet = mDBReader.createDataSet(trainDatabasePath);
		String testDatabasePath = "";
		mTrainSet = mDBReader.createDataSet(testDatabasePath);

		// Create Algorithm instances
		mAlgoTopNSongs = new AlgorithmTopNPopularSongs(mRecommendationCount);
		
		// Generate Model for each algorithm for the given training set
		mAlgoTopNSongs.generateModel(mTrainSet);
		Map<String, List<Song>> recommendation = mAlgoTopNSongs.recommend(mTestSet);
		
		// Get Accuracy of the recommendations
		System.out.println("Accuracy of the algo is = " + Utility.getAccuracy(recommendation));
	}

}
