import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import models.Constants;
import models.DataSet;

import org.apache.log4j.Logger;

import utils.Utility;
import utils.data.CrossValidationFactory;
import utils.data.DBReader;
import utils.data.FileReader;
import utils.data.Reader;
import algos.Algorithm;
import algos.ItemBasedCollaborativeFiltering;
import algos.KNN;
import algos.NaiveBayes;
import algos.TopNPopularSongs;
import algos.UserBasedCollaborativeFiltering;
import algos.ensembles.Bagging;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

/**
 * This is the MAIN class of the project which aims at giving music
 * recommendations to the users in the test set according to data provided in
 * the training set.
 */
public class MusicRecommender 
{
	private static Reader mReader = null;
	private static DataSet mFullDataset = null;	// Entire dataset read from the database
	
	private static Logger LOG = Logger.getLogger(MusicRecommender.class);
	
	private static DecimalFormat df = new DecimalFormat("#.00"); 
	
	private static Map<String, Algorithm> getOverallAlgorithmsMap(int recommendationCount)
	{
		// Algorithms
		Algorithm overallTopNSongsAlgo 	= new TopNPopularSongs(recommendationCount);
		
		// Optimum K-value was chosen by running an experiment
		KNN kNNAlgo 				= new KNN(recommendationCount) ;
		kNNAlgo.setNumNeighbours(80);
		
		Algorithm naiveBayesAlgo 		= new NaiveBayes(recommendationCount);
		
		// The parameters for algorithm were set by running it for different weight and normalization
		// coefficients in an experiment.
		UserBasedCollaborativeFiltering userBasedCollabFiltering = new UserBasedCollaborativeFiltering(recommendationCount);
		userBasedCollabFiltering.setWeightCoefficient(0.8);
		userBasedCollabFiltering.setNormalizationCoefficient(8.0);
		Algorithm itemBasedCollabFiltering = new ItemBasedCollaborativeFiltering(recommendationCount);
		
		Algorithm baggingWithNaiveBayes = new Bagging(Constants.NAIVE_BAYES, recommendationCount);
		
		Map<String, Algorithm> algosMap = Maps.newHashMap();
		algosMap.put(Constants.TOP_N_POPULAR, 				overallTopNSongsAlgo);
		algosMap.put(Constants.USER_BASED_COLLABORATIVE_FILTERING, userBasedCollabFiltering);
		algosMap.put(Constants.ITEM_BASED_COLLABORATIVE_FILTERING, itemBasedCollabFiltering);
		//algosMap.put(Constants.BAGGING_NAIVE_BAYES, baggingWithNaiveBayes);
		algosMap.put(Constants.K_NEAREST_NEIGHBOUR, 		kNNAlgo);
		//algosMap.put(Constants.NAIVE_BAYES, 				naiveBayesAlgo);
		
		return algosMap;
	
	}
	
	/**
	 * Main method which will execute different Recommendation Algorithms and
	 * compare their results.
	 * 
	 * Sample run :
	 * MusicRecommender msd_test 10 40 5
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// Parse the command line arguments

		if(args == null || (args.length != 5)) 
		{
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("Please run the program with correct arguments !!").append("\n");
			errorMsg.append("Usage : MusicRecommender <table name> <num songs to recommend> <num cross-validation folds> <num runs>");
			throw new IllegalArgumentException(errorMsg.toString());
		}

		String dbTableName = args[0];
		int numSongRecommendationPerUser = Integer.parseInt(args[1]);
		int numCrossValidationFolds = Integer.parseInt(args[2]);
		int runs = Integer.parseInt(args[3]);
		boolean isDataReadFromFile = (args[4].equals("filedata")) ? true : false;
		
		LOG.info("Dataset Table : " + dbTableName + ", Song recommendations per user : " + 
				numSongRecommendationPerUser + ", Cross validation folds : " + numCrossValidationFolds + 
				", Job runs : " + runs);
		
		// Read data from file
		if(isDataReadFromFile) {
			mReader = new FileReader();
			LOG.info("Reading data from file");
		}
		else {
			mReader = new DBReader();
			LOG.info("Reading data from database");
		}
		mFullDataset = mReader.createDataSet(dbTableName);
		
		// Run algorithms multiple times to get average accuracy results for different datasets
		// using cross-validation approach.
		boolean randomizeFolds = (runs == numCrossValidationFolds) ? false : true;
		CrossValidationFactory datasetFactory = 
				new CrossValidationFactory(mFullDataset, numCrossValidationFolds, randomizeFolds);

		Map<String, Algorithm> overallAlgosMap = getOverallAlgorithmsMap(numSongRecommendationPerUser);
		Map<String, Double> algosAccuracy = Maps.newHashMap();
		Map<String, Long> algosRunTimes = Maps.newHashMap();
		
		for(int runId = 0; runId < runs; runId++)
		 {
			Map<String, DataSet> foldDatasets = datasetFactory.getDatasets(runId);
			DataSet trainDataset = foldDatasets.get(Constants.TRAIN_DATASET);
			DataSet testVisibleDataset = foldDatasets.get(Constants.TEST_VISIBLE_DATASET);
			DataSet testHiddenDataset = foldDatasets.get(Constants.TEST_HIDDEN_DATASET);
			
			LOG.info("\n\n==========================================================================");
			LOG.info("Train dataset summary for run " + runId + " is " + trainDataset.getDatasetStats());
			LOG.info("Test visible dataset summary for run " + runId + " is " + testVisibleDataset.getDatasetStats());
			LOG.info("Test hidden dataset summary for run " + runId + " is " + testHiddenDataset.getDatasetStats());
			

			/**
			 * For each recommendation algorithm do the following :
			 * 
			 * 1) Build a learning model based on the algorithm.
			 * 2) Recommend top N songs based on the learned model.
			 * 3) Compare the predicted songs with the actual songs listened by a test data set user.
			 */			
			for(Map.Entry<String, Algorithm> perAlgorithmEntry : overallAlgosMap.entrySet()) 
			{
				// Getting the algorithm
				String algoName = perAlgorithmEntry.getKey();
				Algorithm algo = perAlgorithmEntry.getValue();
				LOG.info("Running '" + algoName + "' recommendation algorithm for run " + runId);
				
				// Main Step - Generating Model + Recommending + Testing Recommendation
				Stopwatch algoTimer = Stopwatch.createStarted();
				double currentAlgoAccuracy = Utility.runAlgorithm(algo, trainDataset, testVisibleDataset, testHiddenDataset);
				algoTimer.stop();
				LOG.info("Accuracy of algo '" + algoName + "' for run " + runId + " is " + 
						df.format(currentAlgoAccuracy) + " % ");
				
				// Logging algorithm's runtime
				long algoRuntime = 0;
				if(algosRunTimes.containsKey(algoName)) {
					algoRuntime = algosRunTimes.get(algoName); 
				}
				algosRunTimes.put(algoName, algoRuntime + algoTimer.elapsed(TimeUnit.SECONDS));
				
				// Summing up Algo Accuracy
				Double cumulativeAlgoAccuracy = 0.0;
				if(algosAccuracy.containsKey(algoName)) 
					cumulativeAlgoAccuracy = algosAccuracy.get(algoName);
				algosAccuracy.put(algoName, cumulativeAlgoAccuracy + currentAlgoAccuracy);
			}
		}
		
		// Display the aggregated results for all algorithms
		LOG.info("\n\n=============================================================================");
		LOG.info("----------------------------------------------");
		LOG.info("Overall Avg. Accuracy (NumOfRecommendations=" + numSongRecommendationPerUser + 
				", NumOfCVFolds=" + numCrossValidationFolds + ")");
		LOG.info("=====================\n");
		for(Map.Entry<String, Double> perAlgoEntry : algosAccuracy.entrySet())
		{
			String algoName = perAlgoEntry.getKey();
			Double sumAccuracies = algosAccuracy.get(algoName);
			
			double avgAccuarcy = sumAccuracies/runs;//algoRunsResult.size();
			LOG.info("'" + algoName + "' : Accuracy = " + df.format(avgAccuarcy) + " % , Time : " + 
					algosRunTimes.get(algoName) + " seconds.");
		}
		LOG.info("----------------------------------------------\n");

	}
	
}
