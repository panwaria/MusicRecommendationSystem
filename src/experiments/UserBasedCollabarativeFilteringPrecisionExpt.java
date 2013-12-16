package experiments;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import utils.DBReader;
import utils.Utility;
import utils.data.CrossValidationFactory;
import models.Constants;
import models.DataSet;
import models.Song;
import algos.Algorithm;
import algos.UserBasedCollaborativeFiltering;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * This experiment involves running user-based collaborative filtering experiment for various
 * parameter values for weight and normalization co-efficients.
 * 
 * This experiment can be useful to determine the optimal weight and normalizaiton co-efficient
 * value to be used for user-based collaborative filtering.
 * @author excelsior
 *
 */
public class UserBasedCollabarativeFilteringPrecisionExpt 
{
	private static Logger LOG = Logger.getLogger(UserBasedCollabarativeFilteringPrecisionExpt.class);
	
	private static List<Double> weightCoefficientValues = Lists.newArrayList(0.2, 0.4, 0.6, 0.8);
	private static List<Double> normalizationCoefficientValues = Lists.newArrayList(1.0,2.0,3.0,5.0,6.0,8.0);
	
	private static int JOB_RUNS = 5;
	private static int CROSS_VALIDATION_FOLDS = 10;
	private static String DB_TABLE_NAME = "msd_test";
	private static int NUM_SONGS_TO_RECOMMEND = 10;
	
	public static void main(String[] args)
	{
		DBReader reader = new DBReader();
		DataSet fullDataset = reader.createDataSet(DB_TABLE_NAME);
		
		CrossValidationFactory datasetFactory = 
			new CrossValidationFactory(fullDataset, CROSS_VALIDATION_FOLDS, true);
		
		Table<Double, Double, Double> avgAccuracyTbl = HashBasedTable.create();
		Table<Double, Double, Double> minAccuracyTbl = HashBasedTable.create();
		Table<Double, Double, Double> maxAccuracyTbl = HashBasedTable.create();
		
		for(Double weightCoeff : weightCoefficientValues) {
			for(Double normalizeCoeff : normalizationCoefficientValues) {
				double maxAccuracy = 0.0;
				double sumAccuracy = 0.0;
				double minAccuracy = 0.0;
				for(int runId = 0; runId < JOB_RUNS; runId++)
				 {
					Map<String, DataSet> foldDatasets = datasetFactory.getDatasets(runId);
					DataSet trainDataset = foldDatasets.get(Constants.TRAIN_DATASET);
					DataSet testVisibleDataset = foldDatasets.get(Constants.TEST_VISIBLE_DATASET);
					DataSet testHiddenDataset = foldDatasets.get(Constants.TEST_HIDDEN_DATASET);
					
					UserBasedCollaborativeFiltering  userBasedCollabFilter = 
						new UserBasedCollaborativeFiltering(NUM_SONGS_TO_RECOMMEND);
					userBasedCollabFilter.setWeightCoefficient(weightCoeff);
					userBasedCollabFilter.setNormalizationCoefficient(normalizeCoeff);
					
					double accuracy = 
						runAlgorithm(userBasedCollabFilter, trainDataset, testVisibleDataset, testHiddenDataset);
					sumAccuracy += accuracy;
					if(accuracy > maxAccuracy) {
						maxAccuracy = accuracy;
					}
					if(accuracy < minAccuracy) {
						minAccuracy = accuracy;
					}
				 }
				
				double avgAccuracy = sumAccuracy/JOB_RUNS;
				
				avgAccuracyTbl.put(weightCoeff, normalizeCoeff, avgAccuracy);
				minAccuracyTbl.put(weightCoeff, normalizeCoeff, minAccuracy);
				maxAccuracyTbl.put(weightCoeff, normalizeCoeff, maxAccuracy);
			}
		}
		
		for(Double weightCoeff : weightCoefficientValues) {
			for(Double normalizeCoeff : normalizationCoefficientValues) {
				double avgAcc = avgAccuracyTbl.get(weightCoeff, normalizeCoeff);
				double minAcc = minAccuracyTbl.get(weightCoeff, normalizeCoeff);
				double maxAcc = maxAccuracyTbl.get(weightCoeff, normalizeCoeff);
				
				LOG.info("Coefficients : {" + weightCoeff + "," + normalizeCoeff + "} = {" + 
						minAcc + ", " + avgAcc + ", " + maxAcc + "}");
			}
		}

	}
	
	/**
	 * Method to run any algorithm with a given trainDataset and testVisibleDataset. testHiddenDataset is 
	 * used to test the accuracy of the recommendations made by the generated model of that algorithm.
	 * 
	 * @param algo					Learner Method
	 * @param trainDataset			TrainDataset
	 * @param testVisibleDataset	Test Visible Dataset (part of training dataset)
	 * @param testHiddenDataset		Actual Test Dataset
	 * @return						Accuracy of the generated model
	 */
	private static double runAlgorithm(Algorithm algo, DataSet trainDataset, 
									  DataSet testVisibleDataset, DataSet testHiddenDataset)
	{
		// Generate Model
		algo.generateModel(trainDataset);
		
		// Get Recommendations using generated model
		Map<String, List<Song>> recommendations = algo.recommend(testVisibleDataset);
		
		// Test Accuracy of generated model
		return Utility.getAccuracy(recommendations, testHiddenDataset);
	}	
}
