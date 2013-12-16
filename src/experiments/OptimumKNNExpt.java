package experiments;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import algos.KNN;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import models.Constants;
import models.DataSet;
import utils.DBReader;
import utils.Utility;
import utils.data.CrossValidationFactory;

/**
 * This experiment explores the test accuracy as K (number of nearest neighbors) is varied.
 * 
 * @author excelsior
 *
 */
public class OptimumKNNExpt 
{

	private static Logger LOG = Logger.getLogger(OptimumKNNExpt.class);
	
	private static int JOB_RUNS = 5;
	private static int CROSS_VALIDATION_FOLDS = 10;
	private static String DB_TABLE_NAME = "msd_test";
	private static int NUM_SONGS_TO_RECOMMEND = 10;
	
	private static List<Integer> kValues = Lists.newArrayList(1, 2, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
	
	public static void main(String[] args)
	{
		DBReader reader = new DBReader();
		DataSet fullDataset = reader.createDataSet(DB_TABLE_NAME);
		
		CrossValidationFactory datasetFactory = 
			new CrossValidationFactory(fullDataset, CROSS_VALIDATION_FOLDS, true);
		
		Map<Integer, Double> minAccuracyMap = Maps.newHashMap();
		Map<Integer, Double> avgAccuracyMap = Maps.newHashMap();
		Map<Integer, Double> maxAccuracyMap = Maps.newHashMap();
		
		Stopwatch timer = Stopwatch.createStarted();
		for(Integer kvalue : kValues) {
			LOG.info("Running experiment for K=" + kvalue);
			double maxAccuracy = 0.0;
			double sumAccuracy = 0.0;
			double minAccuracy = 0.0;
			for(int runId = 0; runId < JOB_RUNS; runId++)
			 {
				Map<String, DataSet> foldDatasets = datasetFactory.getDatasets(runId);
				DataSet trainDataset = foldDatasets.get(Constants.TRAIN_DATASET);
				DataSet testVisibleDataset = foldDatasets.get(Constants.TEST_VISIBLE_DATASET);
				DataSet testHiddenDataset = foldDatasets.get(Constants.TEST_HIDDEN_DATASET);
				
				KNN knnAlgo = new KNN(NUM_SONGS_TO_RECOMMEND);
				knnAlgo.setNumNeighbours(kvalue);
				
				double accuracy = Utility.runAlgorithm(knnAlgo, trainDataset, testVisibleDataset, 
						testHiddenDataset);
				sumAccuracy += accuracy;
				if(accuracy > maxAccuracy) {
					maxAccuracy = accuracy;
				}
				if(accuracy < minAccuracy) {
					minAccuracy = accuracy;
				}
			 }
			
			double avgAccuracy = sumAccuracy/JOB_RUNS;
			
			minAccuracyMap.put(kvalue, minAccuracy);
			avgAccuracyMap.put(kvalue, avgAccuracy);
			maxAccuracyMap.put(kvalue, maxAccuracy);
		}
		
		
		// Display the aggregated results
		for(Integer kvalue : kValues) {
			LOG.info("K : " + kvalue + " => {" + 
					minAccuracyMap.get(kvalue) + ", " + 
					avgAccuracyMap.get(kvalue) + ", " + 
					maxAccuracyMap.get(kvalue) + 
					"}"
				);
		}
		
		LOG.info("Time to run the experiment : " + timer.elapsed(TimeUnit.SECONDS) + " seconds.");
		
	}
}
