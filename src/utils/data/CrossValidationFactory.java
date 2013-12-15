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
public class CrossValidationFactory {

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
		Map<String, Map<String, Integer>> fullListeningHistory = mFullDataset.getmUserListeningHistory();
		Map<String, Song> fullSongMap = mFullDataset.getmSongMap();
		
		List<String> allListeners = Lists.newArrayList(fullListeningHistory.keySet());
		int foldSize = allListeners.size()/numFolds;
		
		for(int numUser=0; numUser < allListeners.size(); numUser++) {
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
		if(endId > allListeners.size()) {
			endId = allListeners.size();
		}
		
		List<String> listeners = allListeners.subList(startId, endId);
		Map<String, Map<String, Integer>> foldListeningHistory = Maps.newHashMap();
		Map<String, Song> foldSongMap = Maps.newHashMap();
		for(String listener : listeners ) {
			Map<String, Integer> listenerSongsCount = fullListeningHistory.get(listener);
			foldListeningHistory.put(listener, listenerSongsCount);
			for(Map.Entry<String, Integer> entry2: listenerSongsCount.entrySet()) {
				String songName = entry2.getKey();
				if(fullSongMap.containsKey(songName)) {
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
	public Map<String, DataSet> getDatasets(int runId)
	{
		Map<String, DataSet> datasets = Maps.newHashMap();
		DataSet dataset = mDatasetFolds.get(runId);
		datasets.putAll(getTestTuneDataset(dataset));
		
		Map<String, Map<String, Integer>> trainListeningHistory = Maps.newHashMap();
		Map<String, Song> trainSongMap = Maps.newHashMap();
		
		for(int foldId=0; foldId < mDatasetFolds.size(); foldId++) {
			// Don't include the test datafold
			if(foldId == runId) {
				continue;
			}
			
			DataSet fold = mDatasetFolds.get(foldId);
			trainListeningHistory.putAll(fold.getmUserListeningHistory());
			trainSongMap.putAll(fold.getmSongMap());
		}
		
		DataSet trainDataset = new DataSet(trainListeningHistory, trainSongMap);
		datasets.put(Constants.TRAIN_DATASET, trainDataset);

		return datasets;
	}
	
	/**
	 * The basic idea is to divide the dataset into two parts - one which would be used for tuning
	 * (contains half listening history of a user) and the other would be used for testing the
	 * efficiency of the algorithm.
	 * 
	 * For a user X, if total number of songs is N :
	 * Tune dataset would contain : (N/2)+1 songs
	 * Test dataset would contain : (N/2) songs
	 */
	private static Map<String, DataSet> getTestTuneDataset(DataSet dataset)
	{
		Map<String, Map<String, Integer>> listeningHistory = dataset.getmUserListeningHistory();
		Map<String, Song> songMap = dataset.getmSongMap();
		
		Map<String, Map<String, Integer>> testListeningHistory = Maps.newHashMap();
		Map<String, Song> testSongMap = Maps.newHashMap();
		
		Map<String, Map<String, Integer>> tuneListeningHistory = Maps.newHashMap();
		Map<String, Song> tuneSongMap = Maps.newHashMap();
		
		for(Map.Entry<String, Map<String, Integer>> entry : listeningHistory.entrySet()) {
			Map<String, Integer> testUserPlayCountMap = Maps.newHashMap();
			Map<String, Integer> tuneUserPlayCountMap = Maps.newHashMap();
			
			String user = entry.getKey();
			Map<String, Integer> songsPlayCountMap = entry.getValue();
			int numSongs = songsPlayCountMap.size();
			List<String> songs = Lists.newArrayList(songsPlayCountMap.keySet());
			for(int i=0; i <= numSongs/2; i++) {
				String song = songs.get(i);
				tuneUserPlayCountMap.put(song, songsPlayCountMap.get(song));
				tuneSongMap.put(song, songMap.get(song));
			}
			for(int i=(numSongs/2)+1; i < numSongs; i++) {
				String song = songs.get(i);
				testUserPlayCountMap.put(song, songsPlayCountMap.get(song));
				testSongMap.put(song, songMap.get(song));
			}
			
			tuneListeningHistory.put(user, tuneUserPlayCountMap);
			testListeningHistory.put(user, testUserPlayCountMap);
		}
		
		DataSet tuneDataset = new DataSet(tuneListeningHistory, tuneSongMap);
		DataSet testDataset = new DataSet(testListeningHistory, testSongMap);
		
		Map<String, DataSet> datasetsMap = Maps.newHashMap();
		datasetsMap.put(Constants.TUNE_DATASET, tuneDataset);
		datasetsMap.put(Constants.TEST_DATASET, testDataset);
		return datasetsMap;
	}
	
}
