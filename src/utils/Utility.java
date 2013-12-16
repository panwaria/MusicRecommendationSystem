package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Constants;
import models.DataSet;
import models.Song;
import org.apache.log4j.Logger;
import algos.Algorithm;
import algos.ItemBasedCollaborativeFiltering;
import algos.KNN;
import algos.NaiveBayes;
import algos.TopNPopularSongs;
import algos.UserBasedCollaborativeFiltering;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class with basic utility methods.
 * 
 */
public class Utility {
	private static Logger LOG = Logger.getLogger(Utility.class);

	/**
	 * Method to get the accuracy of the recommendations returned by different
	 * algorithms.
	 * 
	 * @param recommendation
	 *            Map of UserID to the List of recommended songs
	 * @return Accuracy of the algorithm
	 */
	public static Double getAccuracy(Map<String, List<Song>> recommendations, DataSet testDataset) 
	{
		double overallAccuracy = 0.0;
		for (Map.Entry<String, List<Song>> entry : recommendations.entrySet()) {
			String userId = entry.getKey();
			List<Song> predictedSongs = entry.getValue();
			Map<String, Integer> actualSongs = testDataset.getUserListeningHistory().get(userId);

			int totalRecommendations = predictedSongs.size();
			int matchedSongs = 0;
			for (Song s : predictedSongs) {
				if (actualSongs.containsKey(s.getSongID())) {
					++matchedSongs;
				}

				double accuracyForUser = (matchedSongs)/ (double) totalRecommendations;
				LOG.debug("Accuracy for user " + userId + " is "
						+ accuracyForUser + " with " + matchedSongs
						+ " matched songs ");

				overallAccuracy += accuracyForUser;
			}

		}
		
		int numUsers = recommendations.keySet().size();
		return (overallAccuracy * 100) / numUsers;
	}
	
	public static Algorithm getAlgorithmInstance(String algoName, int numSongsToRecommend)
	{
		Algorithm algo = null;
		
		if(algoName.endsWith(Constants.TOP_N_POPULAR))	
			return new TopNPopularSongs(numSongsToRecommend);
		
		if(algoName.endsWith(Constants.NAIVE_BAYES))	
			return new NaiveBayes(numSongsToRecommend);
		
		if(algoName.endsWith(Constants.K_NEAREST_NEIGHBOUR))	
			return new KNN(numSongsToRecommend);
		
		if(algoName.endsWith(Constants.ITEM_BASED_COLLABORATIVE_FILTERING))	
			return new ItemBasedCollaborativeFiltering(numSongsToRecommend);
		
		if(algoName.endsWith(Constants.USER_BASED_COLLABORATIVE_FILTERING))	
			return new UserBasedCollaborativeFiltering(numSongsToRecommend);
		
		return algo;
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
	public static double runAlgorithm(Algorithm algo, DataSet trainDataset, 
									  DataSet testVisibleDataset, DataSet testHiddenDataset)
	{
		// Generate Model
		algo.generateModel(trainDataset);
		
		// Get Recommendations using generated model
		Map<String, List<Song>> recommendations = algo.recommend(testVisibleDataset);
		
		// Test Accuracy of generated model
		return Utility.getAccuracy(recommendations, testHiddenDataset);
	}
	
	/**
	 * Method to get a dataset randomly drawn with replacement.
	 * 
	 * @param dataset	Base dataset
	 * @return			Randomly drawn dataset
	 */
	public static DataSet getDatasetDrawnWithReplacement(DataSet dataset)
	{
		Map<String, Map<String, Integer>> userListeningHistory = dataset.getUserListeningHistory();

		int datasetSize = dataset.getDataSetSize();
		List<String> usersList = dataset.getListOfUsers();
		int numUsers = usersList.size();
		
		Map<String, List<String>> userSongsListMap = new HashMap<String, List<String>>();
		Map<String, Map<String, Integer>> trainListeningHistory = Maps.newHashMap();
		
		// Iterate datasetSize number of times.
		for (int i = 0; i < datasetSize; i++)
		{
			int randomVal = (int)Math.random() * datasetSize;
			
			// Pick a User
			String pickedUser = usersList.get(randomVal % numUsers);
			
			// Get a list of the songs this user listened to
			List<String> userSongsList = null;
			Map<String, Integer> userListeningHistoryListMap = userListeningHistory.get(pickedUser);
			if(userSongsListMap.containsKey(pickedUser))
				userSongsList = userSongsListMap.get(pickedUser);
			else
				userSongsList = Lists.newArrayList(userListeningHistoryListMap.keySet());

			// Now, pick a song from this list
			String pickedSong = userSongsList.get(randomVal * userSongsList.size());
			
			// Find its play count
			int pickedSongPlayCount = userListeningHistoryListMap.get(pickedSong);
			
			// Push this data to trainListeningHistory
			if(trainListeningHistory.containsKey(pickedUser))
			{
				Map<String, Integer> songsPlayCountMap = trainListeningHistory.get(pickedUser);
				if(!songsPlayCountMap.containsKey(pickedSong))
					songsPlayCountMap.put(pickedSong, pickedSongPlayCount);
			}
			else
			{
				Map<String, Integer> songsPlayCountMap = new HashMap<String, Integer>();
				songsPlayCountMap.put(pickedSong, pickedSongPlayCount);
				trainListeningHistory.put(pickedUser,songsPlayCountMap);
			}
		}
		
		return  new DataSet(trainListeningHistory, dataset.getSongMap());
	}

}
