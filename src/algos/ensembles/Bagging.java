package algos.ensembles;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	/**
	 * Method to generate model using the given training dataset.
	 */
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

	/**
	 * Method to give recommendations for the users in visible test dataset.
	 */
	public Map<String, List<Song>> recommend(DataSet testVisibleDataset)
	{
		Map<String, List<Song>> overallRecommendations = new HashMap<String, List<Song>>();
		
		// Map <UserID, Map<SongID, recommendCount>
		Map<String, Map<String, Integer>> userSongRecoMap = new HashMap<String, Map<String, Integer>>();
		
		for(Algorithm hypo : mHypotheses)
		{
			// Main Function of Algorithm being called
			Map<String, List<Song>> recommendations = hypo.recommend(testVisibleDataset);
			
			// For every user
			for(Map.Entry<String, List<Song>> perUserEntry: recommendations.entrySet())
			{
				String userID = perUserEntry.getKey();
				List<Song> recommendedSongs = perUserEntry.getValue();
				
				// For every recommended song for this user
				for(Song song : recommendedSongs)
				{
					String songID = song.getSongID();
					
					if(userSongRecoMap.containsKey(userID))
					{
						Map<String, Integer> songCountMap = userSongRecoMap.get(userID);
						if(songCountMap.containsKey(songID))
							songCountMap.put(songID, songCountMap.get(songID) + 1); // TODO: Replace 1 with song score
						else
							songCountMap.put(songID, 1);	// TODO: Replace 1 with song score
					}
					else
					{
						Map<String, Integer> songCountMap = new HashMap<String, Integer>();
						songCountMap.put(songID, 1);	// TODO: Replace 1 with song score
						userSongRecoMap.put(userID, songCountMap);
					}
				}
			}
		}
		
		// TODO: Get combined recommendations
		for(Map.Entry<String, Map<String, Integer>> perUserEntry: userSongRecoMap.entrySet())
		{
			String userID = perUserEntry.getKey();
			Map<String, Integer> songCountMap = perUserEntry.getValue();
			
			// Get song IDs with top scores
			List<String> recommendedSongIDs = Utility.sortHashMapByValues(songCountMap);
			
			// Get songIDs for these selected songs
			List<Song> recommendedSongs = new ArrayList<Song>();
			for(String songID : recommendedSongIDs)
				recommendedSongs.add(mOriginalTrainDataSet.getSongMap().get(songID));
			
			overallRecommendations.put(userID, recommendedSongs);
		}
		
		return overallRecommendations;
	}

	
}
