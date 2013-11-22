import java.util.List;
import java.util.Map;

import models.DataSet;
import models.Song;
import utils.DBReader;
import utils.Utility;
import algos.Algorithm;
import algos.KNN;
import algos.NaiveBayes;
import algos.TopNPopularSongs;

import com.google.common.collect.Maps;

/**
 * This is the MAIN class of the project which aims at giving music
 * recommendations to the users in the test set according to data provided in
 * the training set.
 */
public class MusicRecommender {
	/* Member Variables */

	public static int mRecommendationCount = 10; // Default number of songs to be recommended to each user

	// DataSets
	public static DataSet mTrainSet = null;
	public static DataSet mTestSet = null;

	// Algorithms
	public static Algorithm mOverallTopNSongsAlgo = new TopNPopularSongs(mRecommendationCount);
	public static Algorithm mKNNAlgo = new KNN(mRecommendationCount) ;
	public static Algorithm mNaiveBayesAlgo = new NaiveBayes(mRecommendationCount);

	private static Map<String, Algorithm> algosMap = Maps.newHashMap();
	static {
		algosMap.put("OverallN-Popular Songs Algorithm", 	mOverallTopNSongsAlgo);
		//algosMap.put("K-Nearest Neighbours Algorithm", 		mKNNAlgo);
		//algosMap.put("Naive Bayes Algorithm", 				mNaiveBayesAlgo);
	}
	
	// DB Reader
	public static DBReader mDBReader = null;

	/* Methods */

	/**
	 * Main mehtod which will execute different Recommendation Algorithms and
	 * compare their results.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Initialize number of songs to recommend
		mRecommendationCount = 10;

		// Create a DBReader
		mDBReader = new DBReader();

		// Get Train and Test DataSets using DBReader
		mTrainSet = mDBReader.createDataSet(Constants.MSD_TRAIN_DATA_TABLE);
		System.out.println("Loading training data set ..");
		
		mTestSet = mDBReader.createDataSet(Constants.MSD_TEST_DATA_TABLE);
		System.out.println("Loaded test data set ..");
		
		/**
		 * For each recommendation algorithm do the following :
		 * 
		 * 1) Build a learning model based on the algorithm.
		 * 2) Recommend top N songs based on the learned model.
		 * 3) Compare the predicted songs with the actual songs listened by a test data set user.
		 */
		for(Map.Entry<String, Algorithm> entry : algosMap.entrySet()) {
			String algoName = entry.getKey();
			Algorithm algo = entry.getValue();
			System.out.println("Running " + algoName + " recommendation algorithm ..");
			
			algo.generateModel(mTrainSet);
			Map<String, List<Song>> recommendations = algo.recommend(mTestSet);
			double algoAccuracy = Utility.getAccuracy(recommendations, mTestSet);
			System.out.println("Accuracy of algo " + algoName + " is " + algoAccuracy);
		}
	}

}
