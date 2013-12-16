package algos;

import java.lang.annotation.Inherited;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;

import utils.AlgoUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import models.DataSet;
import models.Song;
import models.SongScore;

/**
 * Implements in-memory item-based collaborative filtering for making best recommendations.
 * 
 * The idea is that if songs X and Y are frequently listened together and user X has already listened
 * to song X, he/she would most likely also like song Y.
 * @author excelsior
 *
 */
public class ItemBasedCollaborativeFiltering implements Algorithm
{

	private Logger LOG = Logger.getLogger(ItemBasedCollaborativeFiltering.class);

	private int numSongsToRecommend = 0;
	private DataSet trainDataset;
	
	public ItemBasedCollaborativeFiltering(int numSongsToRecommend)
	{
		this.numSongsToRecommend = numSongsToRecommend;
	}
	
	public void generateModel(DataSet trainSet) {
		this.trainDataset = trainSet;
	}

	public Map<String, List<Song>> recommend(DataSet testVisibleDataset) 
	{
		LOG.info("TRAIN songs : " + trainDataset.getSongMap().keySet().size() + 
				", TEST users : " + testVisibleDataset.getSongMap().keySet().size());
		
		Map<String, List<Song>> recommendations = Maps.newHashMap();
		
		// Song-to-song similarity matrix
		Table<String, String, Double> songSimMatrix = getSongSimilarityMatrix(testVisibleDataset);
		
		Set<String> allTrainSongs = trainDataset.getSongMap().keySet();
		PriorityQueue<SongScore> topNSongScores = new PriorityQueue<SongScore>(numSongsToRecommend);
		
		for(String testUser : testVisibleDataset.getListOfUsers()) {
			/**
			 * Calculate the score for each training song to be picked among the top N recommended songs.
			 * Sum the score of train song column in the matrix across all the test songs rows for this
			 * user.
			 */
			for(String trainSong : allTrainSongs) {
				double weightTrainSong = 0.0;
				List<String> userTestSongs = testVisibleDataset.getSongsForUser(testUser);
				for(String testSong : userTestSongs) {
					if(songSimMatrix.contains(testSong, trainSong)) {
						weightTrainSong += songSimMatrix.get(testSong, trainSong);					
					}
				}
				
				AlgoUtils.updateTopNSongs(numSongsToRecommend, topNSongScores, trainSong, weightTrainSong);
			}
			
			List<Song> topNSongs = AlgoUtils.getTopNSongs(topNSongScores, trainDataset);
			recommendations.put(testUser, topNSongs);
		}

		return recommendations;
	}

	/**
	 * Returns a song-to-song similarity matrix.
	 * 
	 * Row header consists of a song listened by a test user and column header consists of all
	 * songs in the train dataset. The cell values consist of the similarity scores for a pair of
	 * songs. If this score is high, it implies that these songs are highly co-related and should
	 * probably be recommended together.
	 * 
	 * This matrix can become really huge so need to optimize it a bit. Only store 10 best similar
	 * train songs for every test sons, instead of storing the similarity with every train song.
	 * 
	 * @param testVisibleDataset
	 * @return
	 */
	private Table<String, String, Double> getSongSimilarityMatrix(DataSet testVisibleDataset)
	{
		Table<String, String, Double> itemSimMatrix = HashBasedTable.create();
		
		Map<String, Song> testSongMap = testVisibleDataset.getSongMap();
		Map<String, Song> trainSongMap = trainDataset.getSongMap();
		
		Set<String> testSongs = testSongMap.keySet();
		Set<String> trainSongs = trainSongMap.keySet();
		
		for(String testSong : testSongs) {
			Set<String> testSongUsers = Sets.newHashSet(testSongMap.get(testSong).getListenersList());
			for(String trainSong : trainSongs) {
				Set<String> trainSongUsers = Sets.newHashSet(trainSongMap.get(trainSong).getListenersList());
				int commonUsers = getCommonUsers(testSongUsers, trainSongUsers);
				
				double simScore = getSimScoreBwSongs(commonUsers, testSongUsers.size(), trainSongUsers.size());
				itemSimMatrix.put(testSong, trainSong, simScore);
			}
		}
		
		return itemSimMatrix;
	}
	
	/**
	 * Get the number of common users for two set of listeners for two different songs.
	 */
	private int getCommonUsers(Set<String> setA, Set<String> setB)
	{
		if(setA == null || setB == null || setA.isEmpty() || setB.isEmpty()) {
			return 0;
		}
		
		return Sets.intersection(setA, setB).size();
	}
	
	/**
	 * Gets the similarity score between two songs.
	 * 
	 * @param commonUsers			Number of common listeners for both the songs.	
	 * @param testSongUsers			Number of listeners for the test song.
	 * @param trainSongUsers		Number of listeners for the train song.
	 * @return	Similarity score between a test and train song.
	 */
	private double getSimScoreBwSongs(int commonUsers, int testSongUsers, int trainSongUsers)
	{
		double score = (double)(commonUsers)/
				(double)((Math.pow(testSongUsers, 0.5))*(Math.pow(trainSongUsers, 0.5)));
		
		LOG.debug("Sim score for (" + commonUsers + ", " + testSongUsers + ", " + trainSongUsers + ") is " + score);
		return score;
	}	
}
