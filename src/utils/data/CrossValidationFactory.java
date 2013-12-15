package utils.data;

import java.util.List;
import java.util.Map;

import utils.DBReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import models.DataSet;
import models.Song;
import models.Constants;

/**
 * Utility class that generated cross-validation folds with a base dataset.
 * 
 * Cross-validation is used to generate multiple train and test datasets from the same original
 * dataset. This allows to test an algorithm over much diverse datasets.
 * 
 * Each fold would contain equal number of listeners, though the total songs across each fold may 
 * vary.
 * 
 * @author excelsior
 *
 */
public class CrossValidationFactory
{
	private DataSet mFullDataset = null;
	private List<DataSet> mDatasetFolds = Lists.newArrayList();
	
	private DBReader mReader = new DBReader();
	
	/**
	 * Initialize with the base dataset. This dataset would then be split into various chunks to
	 * generate various permutations of dataset based on individual folds during the cross validation
	 * process.
	 * 
	 * @param dbTableName
	 */
	public CrossValidationFactory(String dbTableName, int numFolds)
	{
		mFullDataset = mReader.createDataSet(dbTableName);
		createDatasetFolds(numFolds);
	}
	
	/**
	 * Create dataset folds using the original dataset.
	 * @param numFolds
	 */
	private void createDatasetFolds(int numFolds)
	{
		Map<String, Map<String, Integer>> fullListeningHistory = mFullDataset.getUserListeningHistory();
		Map<String, Song> fullSongMap = mFullDataset.getSongMap();
		
		List<String> allListeners = Lists.newArrayList(fullListeningHistory.keySet());
		int allListenersSize = allListeners.size();
		int foldSize = allListenersSize/numFolds;
		
		for(int numUser = 0; numUser < allListenersSize; numUser++) 
		{
			mDatasetFolds.add(createDatasetFold(numUser, numUser+foldSize, allListeners, fullListeningHistory, fullSongMap));
			numUser += foldSize;
		}
	}
	
	/**
	 * Create a single dataset fold
	 */
	private DataSet createDatasetFold(int startId, int endId, List<String> allListeners, 
			Map<String, Map<String, Integer>> fullListeningHistory, Map<String, Song> fullSongMap)
	{
		if (endId > allListeners.size())
			endId = allListeners.size();
		
		List<String> listeners = allListeners.subList(startId, endId);
		Map<String, Map<String, Integer>> foldListeningHistory = Maps.newHashMap();
		Map<String, Song> foldSongMap = Maps.newHashMap();
		for (String listener : listeners)
		{
			Map<String, Integer> listenerSongsCount = fullListeningHistory.get(listener);
			foldListeningHistory.put(listener, listenerSongsCount);
			for (Map.Entry<String, Integer> entry2 : listenerSongsCount.entrySet())
			{
				String songName = entry2.getKey();
				if (fullSongMap.containsKey(songName))
				{
					foldSongMap.put(songName, fullSongMap.get(songName));
				}
			}
		}
		
		return new DataSet(foldListeningHistory, foldSongMap);
	}
	
	public DataSet getFullDataset()
	{
		return mFullDataset;
	}
	
	/**
	 * Returns a new train and test dataset from the base dataset.
	 * 
	 * Choose the specified fold id as the test dataset and all other fold ids as the train dataset.
	 * @param foldId
	 * @return
	 */
	public Map<String, DataSet> getDatasets()
	{
		// Randomly generate a fold that would be used as the test fold.
		int testFoldId = 1 + (int)(Math.random() * ((mDatasetFolds.size() - 1) + 1));
		
		Map<String, DataSet> datasets = Maps.newHashMap();

		DataSet testDataset = mDatasetFolds.get(testFoldId);
		datasets.putAll(getHiddenAndVisibleTestDataset(testDataset));
		
		Map<String, Map<String, Integer>> trainListeningHistory = Maps.newHashMap();
		Map<String, Song> trainSongMap = Maps.newHashMap();
		
		for(int foldId=0; foldId < mDatasetFolds.size(); foldId++) 
		{
			// Don't include the test datafold
			if(foldId == testFoldId) 
				continue;
			
			DataSet fold = mDatasetFolds.get(foldId);
			trainListeningHistory.putAll(fold.getUserListeningHistory());
			trainSongMap.putAll(fold.getSongMap());
		}
		
		DataSet trainDataset = new DataSet(trainListeningHistory, trainSongMap);
		datasets.put(Constants.TRAIN_DATASET, trainDataset);

		return datasets;
	}
	
	/**
	 * The basic idea is to divide the test dataset into two parts - one which would be used with 
	 * training dataset for learning (contains half listening history of a user) and the other would
	 * be used for testing the efficiency of the algorithm.
	 * 
	 * For a user X, if total number of songs is N :
	 * Test(Visible) dataset would contain : (N/2)+1 songs
	 * Test(Hidden) dataset would contain : (N/2) songs
	 */
	private static Map<String, DataSet> getHiddenAndVisibleTestDataset(DataSet dataset)
	{
		Map<String, Map<String, Integer>> listeningHistory = dataset.getUserListeningHistory();
		Map<String, Song> songMap = dataset.getSongMap();
		
		Map<String, Map<String, Integer>> testHiddenListeningHistory = Maps.newHashMap();
		Map<String, Song> testHiddenSongMap = Maps.newHashMap();
		
		Map<String, Map<String, Integer>> testVisibleListeningHistory = Maps.newHashMap();
		Map<String, Song> testVisibleSongMap = Maps.newHashMap();
		
		for (Map.Entry<String, Map<String, Integer>> entry : listeningHistory.entrySet())
		{
			Map<String, Integer> testHiddenUserPlayCountMap = Maps.newHashMap();
			Map<String, Integer> testVisibleUserPlayCountMap = Maps.newHashMap();

			String user = entry.getKey();
			Map<String, Integer> songsPlayCountMap = entry.getValue();
			int numSongs = songsPlayCountMap.size();
			List<String> songs = Lists.newArrayList(songsPlayCountMap.keySet());
			for (int i = 0; i <= numSongs / 2; i++)
			{
				String song = songs.get(i);
				testVisibleUserPlayCountMap.put(song, songsPlayCountMap.get(song));
				testVisibleSongMap.put(song, songMap.get(song));
			}
			for (int i = (numSongs / 2) + 1; i < numSongs; i++)
			{
				String song = songs.get(i);
				testHiddenUserPlayCountMap.put(song, songsPlayCountMap.get(song));
				testHiddenSongMap.put(song, songMap.get(song));
			}
		
			testVisibleListeningHistory.put(user, testVisibleUserPlayCountMap);
			testHiddenListeningHistory.put(user, testHiddenUserPlayCountMap);
		}
		
		DataSet testVisibleDataset = new DataSet(testVisibleListeningHistory, testVisibleSongMap);
		DataSet testHiddenDataset = new DataSet(testHiddenListeningHistory, testHiddenSongMap);
		
		Map<String, DataSet> datasetsMap = Maps.newHashMap();
		datasetsMap.put(Constants.TEST_VISIBLE_DATASET, testVisibleDataset);
		datasetsMap.put(Constants.TEST_HIDDEN_DATASET, testHiddenDataset);
		return datasetsMap;
	}
	
}
