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

	private DataSet fullDataset = null;
	private List<DataSet> datasetFolds = Lists.newArrayList();
	
	private DBReader reader = new DBReader();
	
	/**
	 * Initialize with the base dataset. This dataset would then be split into various chunks to
	 * generate various permutations of dataset based on individual folds during the cross validation
	 * process.
	 * 
	 * @param dbTableName
	 */
	public CrossValidationFactory(String dbTableName, int numFolds)
	{
		fullDataset = reader.createDataSet(dbTableName);
		createDatasetFolds(numFolds);
	}
	
	/**
	 * Create dataset folds using the original dataset.
	 * @param numFolds
	 */
	private void createDatasetFolds(int numFolds)
	{
		Map<String, Map<String, Integer>> fullListeningHistory = fullDataset.getmUserListeningHistory();
		Map<String, Song> fullSongMap = fullDataset.getmSongMap();
		
		List<String> allListeners = Lists.newArrayList(fullListeningHistory.keySet());
		int foldSize = allListeners.size()/numFolds;
		
		for(int numUser=0; numUser < allListeners.size(); numUser++) {
			datasetFolds.add(createDatasetFold(numUser, numUser+foldSize, allListeners, fullListeningHistory, fullSongMap));
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
		return fullDataset;
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
		datasets.put(Constants.TEST_DATASET, datasetFolds.get(runId));
		
		Map<String, Map<String, Integer>> trainListeningHistory = Maps.newHashMap();
		Map<String, Song> trainSongMap = Maps.newHashMap();
		
		for(int foldId=0; foldId < datasetFolds.size(); foldId++) {
			// Don't include the test datafold
			if(foldId == runId) {
				continue;
			}
			
			DataSet fold = datasetFolds.get(foldId);
			trainListeningHistory.putAll(fold.getmUserListeningHistory());
			trainSongMap.putAll(fold.getmSongMap());
		}
		
		DataSet trainDataset = new DataSet(trainListeningHistory, trainSongMap);
		datasets.put(Constants.TRAIN_DATASET, trainDataset);

		return datasets;
	}
}
