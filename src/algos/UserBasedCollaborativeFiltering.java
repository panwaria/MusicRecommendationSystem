package algos;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import models.DataSet;
import models.Song;
import models.SongScore;

import org.apache.log4j.Logger;

import utils.AlgoUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

/**
 * Implements memory-based user-based collaborative filtering for making best recommendations.
 * 
 * The idea is that users who have listened to similar songs in the past tend to listen to similar
 * songs in the future too. Thus, if we know a user Y is similar to user X, we can recommend Y's 
 * listened songs to user X.
 * 
 * @author excelsior
 * 
 *
 */
public class UserBasedCollaborativeFiltering implements Algorithm
{
	private Logger LOG = Logger.getLogger(UserBasedCollaborativeFiltering.class);
	
	private int numSongsToRecommend = 0;
	private DataSet trainDataset = null;
	
	// Assigns weights to each user based on the number of songs he/she has listened. A user who
	// listens to every other song should not add much to the recommendation score.
	private double weightCoefficient = 0.8;
	
	// Maximizes the impact of high weights and minimizes the impact of low weights;
	private double normalizationCoefficient = 8.0;
	
	public UserBasedCollaborativeFiltering(int numSongsToRecommend)
	{
		this.numSongsToRecommend = numSongsToRecommend;
	}
	
	private DataSet getTrainDataset()
	{
		return trainDataset;
	}
	
	public double getWeightCoefficient()
	{
		return weightCoefficient;
	}
	
	public void setWeightCoefficient(double weightCoefficient)
	{
		this.weightCoefficient = weightCoefficient;
	}
	
	public double getNormalizationCoefficient()
	{
		return normalizationCoefficient;
	}
	
	public void setNormalizationCoefficient(double normalizationCoefficient)
	{
		this.normalizationCoefficient = normalizationCoefficient;
	}
	
	public void generateModel(DataSet trainSet) {
		this.trainDataset = trainSet;
	}

	public Map<String, List<Song>> recommend(DataSet testVisibleDataset) {
		LOG.info("Weight coefficient : " + getWeightCoefficient() + 
				", Normalization coefficient : " + getNormalizationCoefficient());
		LOG.info("TRAIN users : " + trainDataset.getListOfUsers().size() + 
				", TEST users : " + testVisibleDataset.getListOfUsers().size());
		
		Map<String, List<Song>> recommendations = Maps.newHashMap();
		
		Table<String, String, Double> userSimMatrix = getUserSimilarityMatrix(testVisibleDataset);
		Set<String> allTrainSongs = trainDataset.getSongMap().keySet();
		
		for(String testUser : testVisibleDataset.getListOfUsers()) {
			Set<String> allTestUserSongs = Sets.newHashSet(testVisibleDataset.getSongsForUser(testUser));
			
			// Get the list of all the songs which the test user has currently not listened.
			Set<String> songsToEvaluate = AlgoUtils.getUnexploredSongs(allTestUserSongs, allTrainSongs); 

			/**
			 * If test user has listened to all the songs by train users, just recommend the top
			 * N overall popular songs.
			 */
			if(songsToEvaluate.isEmpty()) {
				recommendations.put(testUser, trainDataset.getOverallNPopularSongs(numSongsToRecommend));
				LOG.info("No songs to evaluate for test user");
				continue;
			}
			
			List<Song> topNSongsList = Lists.newArrayList();
			PriorityQueue<SongScore> topNSongScores = new PriorityQueue<SongScore>(numSongsToRecommend);
			for(String song : songsToEvaluate) {
				// Which training set users have listened to this song ? Only these users would
				// contribute to the overall score of this song
				List<String> trainUsersForSong = trainDataset.getUsersForSong(song);
				double songWeight = getSongWeight(testUser, trainUsersForSong, userSimMatrix);
				AlgoUtils.updateTopNSongs(numSongsToRecommend, topNSongScores, song, songWeight);
			}
			
			// Add the best N recommendations for this user
			topNSongsList.addAll(AlgoUtils.getTopNSongs(topNSongScores, trainDataset));
			topNSongsList = AlgoUtils.checkAndUpdateTopNSongs(topNSongsList, numSongsToRecommend, 
					trainDataset.getOverallNPopularSongs(numSongsToRecommend));			
			recommendations.put(testUser, topNSongsList);
		}
		
		return recommendations;
	}

	/**
	 * Gets the overall weight of a song for a test user, calculated by adding the individual
	 * similarity scores contributed by each of the training set users who has listened to this song.
	 * 
	 * weight(song) = SUM [ pow(sim(u,v), gamma) for all v in TRAINING] where gamma is the normalization
	 * constant used to minimize the impact of low weights and maximize the weights of high weights.
	 * @param testUser
	 * @param trainUsers
	 * @param userSimMatrix
	 * @return
	 */
	private double getSongWeight(String testUser, List<String> trainUsers, Table<String, String, Double> userSimMatrix)
	{
		double weight = 0.0;
		for(String trainUser : trainUsers) {
			double simScoreBwUsers = 0.0;
			if(userSimMatrix.contains(testUser, trainUser)) {
				simScoreBwUsers = userSimMatrix.get(testUser, trainUser);
			}
			weight += Math.pow(simScoreBwUsers, getNormalizationCoefficient());
		}

		if(Double.compare(weight, 0.0) > 0) {
			LOG.debug("Sim score for testuser " + testUser + " is " + weight);			
		}
		
		return weight;
	}
	
	/**
	 * Computes the similarity between all the users in the test dataset against the users in the
	 * train dataset.
	 * 
	 * Row - Test user
	 * Column - Train user
	 * Cell - similarity between test and train user calculated as follows :
	 * 
	 * sim(r, c) = common items (r,c)/items(r)pow(aplha) * items(c) pow(1-aplha)
	 * @param testVisibleDataset
	 */
	private Table<String, String, Double> getUserSimilarityMatrix(DataSet testVisibleDataset)
	{
		Table<String, String, Double> userSimMatrix = HashBasedTable.create();
		Map<String, Map<String, Integer>> trainListeningHistory = trainDataset.getUserListeningHistory();
		Map<String, Map<String, Integer>> testVisibleListeningHistory = testVisibleDataset.getUserListeningHistory();
		
		Set<String> testVisibleListeners = testVisibleListeningHistory.keySet();
		Set<String> trainListeners = trainListeningHistory.keySet();
		for(String testUser : testVisibleListeners) {
			Set<String> testUserSongs = testVisibleListeningHistory.get(testUser).keySet();
			for(String trainUser : trainListeners) {
				Set<String> trainUserSongs = trainListeningHistory.get(trainUser).keySet();
				int commonSongs = getCommonSongs(testUserSongs, trainUserSongs);
				
				// Optimization : If no common songs, there is no similarity between these users.
				// Don't add it to the matrix as it does not have any significance.
				if(commonSongs == 0) {
					continue;
				}
				int testUserTotalSongs = testUserSongs.size();
				int trainUserTotalSongs = trainUserSongs.size();
				
				/**
				 * User similarity matrix contains testusers as row headers, train users as column
				 * headers and the cell values contain the similarity score between a testuser
				 * and a train user.
				 */
				double simScore = getSimScoreBwUsers(commonSongs, testUserTotalSongs, trainUserTotalSongs);
				userSimMatrix.put(testUser, trainUser, simScore);
			}
		}
		
		LOG.info(" Matrix => Rows : " + userSimMatrix.rowKeySet().size() + 
				 ", Columns : " + userSimMatrix.columnKeySet().size());
		return userSimMatrix;
	}
	
	/**
	 * Get the number of common items between two sets.
	 * @param setA
	 * @param setB
	 * @return
	 */
	private int getCommonSongs(Set<String> setA, Set<String> setB)
	{
		if(setA == null || setB == null || setA.isEmpty() || setB.isEmpty()) {
			return 0;
		}
		
		return Sets.intersection(setA, setB).size();
	}
	
	/**
	 * Gets the similarity score between two song listeners.
	 * 
	 * @param commonSongs
	 * @param testUserSongs
	 * @param trainUserSongs
	 * @return
	 */
	private double getSimScoreBwUsers(int commonSongs, int testUserSongs, int trainUserSongs)
	{
		double weightCoeff = getWeightCoefficient();
		double score = (double)(commonSongs)/
				(double)((Math.pow(testUserSongs, weightCoeff))*(Math.pow(trainUserSongs, 1-weightCoeff)));
		
		LOG.debug("Sim score for (" + commonSongs + ", " + testUserSongs + ", " + trainUserSongs + ") is " + score);
		return score;
	}
}
