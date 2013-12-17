package utils.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import utils.Utility;
import models.Constants;
import models.DataSet;
import models.Song;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
	private static Logger LOG = Logger.getLogger(CrossValidationFactory.class);
			
	private DataSet mFullDataset = null;
	private List<DataSet> mDatasetFolds = Lists.newArrayList();
	boolean mRandomizeFolds = false;
	
	/**
	 * Initialize with the base dataset. This dataset would then be split into various chunks to
	 * generate various permutations of dataset based on individual folds during the cross validation
	 * process.
	 * 
	 * @param dbTableName
	 */
	public CrossValidationFactory(DataSet fullDataset, int numFolds, boolean randomizeFolds)
	{
		mFullDataset = fullDataset;
		createDatasetFolds(numFolds);
		mRandomizeFolds = randomizeFolds;
		
		for(DataSet dataset : mDatasetFolds) {
			LOG.info("Dataset fold : " + dataset.getDatasetStats());
		}
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
		for (String listener : listeners) {
			Map<String, Integer> listenerSongsCount = fullListeningHistory.get(listener);
			foldListeningHistory.put(listener, listenerSongsCount);
		}

		/**
		 * Fixed a bug here. The songs in the song map should only contain listeners from the
		 * above partitioned listeners set, instead of all the listeners as done previously.
		 */
		Map<String, Song> foldSongMap = Utility.getSongMapForListeningHistory(foldListeningHistory);
		
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
	public Map<String, DataSet> getDatasets(int testFoldId)
	{
		if(mRandomizeFolds)
		{
			// Randomly generate a fold that would be used as the test fold.
			testFoldId = (int)(Math.random() * (mDatasetFolds.size() - 1));
		}		
		
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
			/**
			 * Listening history is partitioned across users, so we can simply add listening history
			 * of every fold together.
			 */
			trainListeningHistory.putAll(fold.getUserListeningHistory());
			
			/**
			 * Getting the listeners for each of the songs across the folds. Since a song might have
			 * been listened by users across folds, we need to merge the listeners list.
			 */
			for(Map.Entry<String, Song> song : fold.getSongMap().entrySet()) {
				String songName = song.getKey();
				Song songObj = song.getValue();
				// Add a new song and its listening history to the list
				if(!trainSongMap.containsKey(songName)) {
					trainSongMap.put(songName, songObj);
				}
				// Update listening history of an existing song in the map
				else {
					Song existingSongObj = trainSongMap.get(songName);
					List<String> existingListeners = existingSongObj.getListenersList();
					List<String> newListeners = songObj.getListenersList();
					
					Set<String> combinedListeners = Sets.newHashSet();
					combinedListeners.addAll(existingListeners);
					combinedListeners.addAll(newListeners);
					
					trainSongMap.put(songName, new Song(songName, Lists.newArrayList(combinedListeners)));
				}
			}
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
