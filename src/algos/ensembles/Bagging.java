package algos.ensembles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.DataSet;
import models.Song;
import utils.Utility;
import algos.Algorithm;

/**
 * Bagging or Bootstrap Aggregation is an ensemble technique which use combination of hypotheses learned
 * over randomly chosen test instances (drawn with replacement) to predict the output of test instance.
 * 
 * @author prakhar
 *
 */

/*
 * Graphs: Bagging with k-NN and Normal k-NN (for a particular k)
 */

public class Bagging implements Algorithm
{
	/* Member Variables */
	public String mAlgoName = null;
	public DataSet mOriginalTrainDataSet = null;
	public List<Algorithm> mHypotheses = null;
	public int mNumSongsToRecommend = 0;
	
	public static final int NUM_ITERATIONS_BAGGING = 5;
	
	/* Methods */
	
	public Bagging(String algoName, int numSongsToRecommend)
	{
		mAlgoName = algoName;
		mNumSongsToRecommend = numSongsToRecommend;
		mHypotheses = new ArrayList<Algorithm>();
	}
	
	public void generateModel(DataSet trainDataset)
	{
		mOriginalTrainDataSet = trainDataset;
		
		for(int i = 0; i < NUM_ITERATIONS_BAGGING; i++)
		{
			// Change the training data set
			DataSet randomlyDrawnDataset = Utility.getDatasetDrawnWithReplacement(mOriginalTrainDataSet);
			
			// Generate Model
			Algorithm algo = Utility.getAlgorithmInstance(mAlgoName, mNumSongsToRecommend);
			algo.generateModel(randomlyDrawnDataset);
			
			// Add it to the list of models
			mHypotheses.add(algo);
		}
	}

	public Map<String, List<Song>> recommend(DataSet testVisibleDataset)
	{
		List<Map<String, List<Song>>> recommendationList = new ArrayList<Map<String, List<Song>>>();
		for(Algorithm hypo : mHypotheses)
		{
			recommendationList.add(hypo.recommend(testVisibleDataset));
		}
		
		// TODO: Get combined recommendations
		
		return null;
	}

	
}
