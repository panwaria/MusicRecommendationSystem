import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import models.Constants;
import models.DataSet;
import models.SongScore;

import org.apache.log4j.Logger;

import utils.Utility;
import utils.data.CrossValidationFactory;
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
	
	private static Logger LOG = Logger.getLogger(MusicRecommender.class);
	
	private static DecimalFormat df = new DecimalFormat("#.00"); 
	
	private static Map<String, Algorithm> getAlgorithms(int recommendationCount)
	{
		// Algorithms
		Algorithm overallTopNSongsAlgo = new TopNPopularSongs(recommendationCount);
		Algorithm kNNAlgo = new KNN(recommendationCount) ;
		Algorithm naiveBayesAlgo = new NaiveBayes(recommendationCount);

		Map<String, Algorithm> algosMap = Maps.newHashMap();
		algosMap.put(Constants.TOP_N_POPULAR, 				overallTopNSongsAlgo);
		//algosMap.put(Constants.K_NEAREST_NEIGHBOUR, 		kNNAlgo);
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
	public static void main(String[] args) {
		// Parse the command line arguments
		if(args == null || (args.length != 4)) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("Please run the program with correct arguments !!").append("\n");
			errorMsg.append("Usage : MusicRecommender <table name> <num songs to recommend> <num cross-validation folds> <num runs>");
			throw new IllegalArgumentException("Please run the program with correct arguments !!");
		}

		String datasetTable = args[0];
		int numSongRecommendationPerUser = Integer.parseInt(args[1]);
		int numCrossValidationFolds = Integer.parseInt(args[2]);
		int runs = Integer.parseInt(args[3]);
		LOG.info("Dataset Table : " + datasetTable + ", Song recommendations per user : " + 
				numSongRecommendationPerUser + ", Cross validation folds : " + numCrossValidationFolds + 
				", Job runs : " + runs);
		
		// Run algorithms multiple times to get average accuracy results for different datasets
		// using cross-validation approach.
		Map<String, Map<Integer, Double>> algosAccuracy = Maps.newHashMap();
		CrossValidationFactory datasetFactory = new CrossValidationFactory(datasetTable, numCrossValidationFolds);
		for(int runId = 0; runId < runs; runId++) {
			Map<String, Algorithm> algosToRun = getAlgorithms(numSongRecommendationPerUser);
			
			Map<String, DataSet> foldDatasets = datasetFactory.getDatasets();
			DataSet trainDataset = foldDatasets.get(Constants.TRAIN_DATASET);
			DataSet testVisibleDataset = foldDatasets.get(Constants.TEST_VISIBLE_DATASET);
			DataSet testHiddenDataset = foldDatasets.get(Constants.TEST_HIDDEN_DATASET);
			
			LOG.info("\n\n");
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
			for(Map.Entry<String, Algorithm> entry : algosToRun.entrySet()) {
				String algoName = entry.getKey();
				Algorithm algo = entry.getValue();
				LOG.info("Running '" + algoName + "' recommendation algorithm for run " + runId);
				
				algo.generateModel(trainDataset);
				Map<String, List<SongScore>> recommendations = algo.recommend(testVisibleDataset);
				double algoAccuracy = Utility.getAccuracy(recommendations, testHiddenDataset);
				LOG.info("Accuracy of algo '" + algoName + "' for run " + runId + " is " + df.format(algoAccuracy) + " % ");
				
				Map<Integer, Double> algoRunsResult = null;
				if(algosAccuracy.containsKey(algoName)) {
					algoRunsResult = algosAccuracy.get(algoName);
				}
				else {
					algoRunsResult = Maps.newHashMap();
				}
				
				algoRunsResult.put(runId + 1, algoAccuracy);
				algosAccuracy.put(algoName, algoRunsResult);
			}
		}
		
		// Display the aggregated results for all algorithms
		LOG.info("\n\n");
		LOG.info("Overall accuracy of all algorithms for recommending top " + numSongRecommendationPerUser + 
				" songs with " + numCrossValidationFolds + "-fold cross validations approach ..");
		for(Map.Entry<String, Map<Integer, Double>> entry : algosAccuracy.entrySet()) {
			String algoName = entry.getKey();
			Map<Integer, Double> algoRunsResult = algosAccuracy.get(algoName);
			
			double sumAccuracies = 0.0;
			for(Map.Entry<Integer, Double> entry2: algoRunsResult.entrySet()) {
				sumAccuracies += entry2.getValue();
			}
			
			double avgAccuarcy = sumAccuracies/algoRunsResult.size();
			LOG.info("Average accuracy for algorithm '" + algoName + "' is " + df.format(avgAccuarcy) + " % ");
		}

	}

}
