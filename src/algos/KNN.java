package algos;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import utils.AlgoUtils;
import models.DataSet;
import models.Song;
import models.SongScore;

import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Class to represent k-Nearest-Neighbor Algorithm
 */
public class KNN implements Algorithm
{
	private static Logger LOG = Logger.getLogger(KNN.class);
	
	// Number of songs to recommend for a user
	private static int mSongsCount = 0;

	private static DataSet mTrainDataset = null;
	
	// Number of neighbors to consider
	private int numNeighbours = 80;
	
	public KNN(int numSongsToRecommend)
	{
		this.mSongsCount = numSongsToRecommend;
	}
	
	public int getNumNeighbours()
	{
		return numNeighbours;
	}
	
	public void setNumNeighbours(int numNeighbours)
	{
		this.numNeighbours = numNeighbours;
	}
	
	public void generateModel(DataSet trainDataset)
	{
		// Do nothing but store the training dataset
		this.mTrainDataset = trainDataset;
	}

	public Map<String, List<Song>> recommend(DataSet testVisibleDataset)
	{
		Map<String, List<Song>> songRecommendationsForUserMap = Maps.newHashMap();
		
		List<String> testVisibleUsers = testVisibleDataset.getListOfUsers();
		if(testVisibleUsers == null || testVisibleUsers.isEmpty()) {
			return songRecommendationsForUserMap;
		}

		// Cache all the train dataset features, instead of computing for every single user
		Map<String, List<Integer>> trainDatasetFeaturesMap = getTrainDatasetFeaturesMap(testVisibleDataset);
		
		for(String user : testVisibleUsers) {
			List<Song> recommendations = getSongRecommendations(user, trainDatasetFeaturesMap, testVisibleDataset);
			recommendations = AlgoUtils.checkAndUpdateTopNSongs(recommendations, mSongsCount, 
					mTrainDataset.getOverallNPopularSongs(mSongsCount));
			songRecommendationsForUserMap.put(user, recommendations);
		}
		
		return songRecommendationsForUserMap;
	}
	
	/**
	 * Cache all the train dataset features
	 * @return
	 */
	private Map<String, List<Integer>> getTrainDatasetFeaturesMap(DataSet testVisibleDataset)
	{
		Stopwatch cacheBuildTimer = Stopwatch.createStarted();
		Map<String, List<Integer>> trainDatasetFeaturesMap = Maps.newHashMap();
		
		List<String> allSongs = getAllTrainTestSongs(testVisibleDataset);
		Set<String> trainUsers = mTrainDataset.getUserListeningHistory().keySet();
		for(String trainUser : trainUsers) {
			List<Integer> trainFeature = getFeatureVector(trainUser, allSongs, mTrainDataset);
			trainDatasetFeaturesMap.put(trainUser, trainFeature);
		}
		
		LOG.info("Built train feature vector cache in " + cacheBuildTimer.elapsed(TimeUnit.SECONDS) + " seconds.");
		return trainDatasetFeaturesMap;
	}
	
	private List<String> getAllTrainTestSongs(DataSet testVisibleDataset)
	{
		Set<String> allSongs = Sets.newHashSet();
		allSongs.addAll(mTrainDataset.getSongMap().keySet());
		allSongs.addAll(testVisibleDataset.getSongMap().keySet());
		return Lists.newArrayList(allSongs);
	}
	/**
	 * Get all the song recommendations for the specified user.
	 */
	private List<Song> getSongRecommendations(String user, Map<String, List<Integer>> trainDatasetFeaturesMap, 
											  DataSet testVisibleDataset)
	{
		List<String> allSongsList = getAllTrainTestSongs(testVisibleDataset);
		
		PriorityQueue<SimilarUser> kNNUsers = getKNNForUser(user, trainDatasetFeaturesMap, 
				testVisibleDataset, allSongsList);
		List<Song> recommendations = getSongsBasedOnKNN(kNNUsers);
		return recommendations;
	}
	
	/**
	 * Get top N most similar songs based on the K nearest neighbors
	 */
	private List<Song> getSongsBasedOnKNN(PriorityQueue<SimilarUser> kNNUsers)
	{
		List<Song> recommendations = Lists.newArrayList();
		
		// Accumulate all possible song recommendations from K-neighbours
		Map<String, Double> allSongsBwKUsers = Maps.newHashMap();
		for(SimilarUser user : kNNUsers) {
			String userName = user.userId;
			Map<String, Integer> listeningHistory = mTrainDataset.getUserListeningHistory().get(userName);
			for(Map.Entry<String, Integer> entry : listeningHistory.entrySet()) {
				String songName = entry.getKey();
				double songScore = 0.0;
				if(allSongsBwKUsers.containsKey(songName)) {
					songScore = allSongsBwKUsers.get(songName);
				}
				songScore += 1.0 + user.simScore;
				allSongsBwKUsers.put(songName, songScore);
			}
		}

		// Retain the top N songs with the best scores.
		PriorityQueue<SongScore> topNSongs = new PriorityQueue<SongScore>(mSongsCount);
		for(Map.Entry<String, Double> entry : allSongsBwKUsers.entrySet()) {
			if(topNSongs.size() < mSongsCount) {
				topNSongs.add(new SongScore(entry.getKey(), entry.getValue()));
			}
			else {
				SongScore head = topNSongs.peek();
				if(Double.compare(head.getScore(), entry.getValue()) < 0) {
					topNSongs.remove();
					topNSongs.add(new SongScore(entry.getKey(), entry.getValue()));
				}
			}
		}
		
		Map<String, Song> trainSongMap = mTrainDataset.getSongMap();
		for(SongScore songScore : topNSongs) {
			recommendations.add(trainSongMap.get(songScore.getSong()));
		}
		
		return recommendations;
	}
	
	/**
	 * Return the K nearest neighbors for a listener.
	 * 
	 * This can be done by calculating the cosine distance between two users where the feature
	 * vector is the weight of all the songs.
	 */
	private PriorityQueue<SimilarUser> getKNNForUser(String user, Map<String, List<Integer>> trainDatasetFeaturesMap, 
													 DataSet testVisibleDataset, List<String> allSongs)
	{
		List<Integer> testFeature = getFeatureVector(user, allSongs, testVisibleDataset);
		
		// Maintain a priority queue to ensure that only the top K neighbors are returned for
		// the test user.
		PriorityQueue<SimilarUser> kNNUsers = new PriorityQueue<KNN.SimilarUser>(getNumNeighbours());
		for(Map.Entry<String, List<Integer>> entry : trainDatasetFeaturesMap.entrySet()) {
			String trainUser = entry.getKey();
			List<Integer> trainFeature = entry.getValue();
			Double simScore = getCosineSimilarityScore(testFeature, trainFeature);
			if(kNNUsers.size() < getNumNeighbours()) {
				kNNUsers.add(new SimilarUser(trainUser, simScore));
			}
			else {
				SimilarUser head = kNNUsers.peek();
				if(Double.compare(head.simScore, simScore) < 0) {
					kNNUsers.remove(head);
					kNNUsers.add(new SimilarUser(trainUser, simScore));
				}
			}			
		}
		
		return kNNUsers;
	}

	/**
	 * Get the playcount feature vector for each song for this user.
	 */
	private List<Integer> getFeatureVector(String user, List<String> allSongs, DataSet dataset)
	{
		List<Integer> songPlaycountFeature = Lists.newArrayList();
		Map<String, Integer> userListeningHistory = dataset.getUserListeningHistory().get(user);
		for(String song: allSongs) {
			if(userListeningHistory.containsKey(song)) {
				songPlaycountFeature.add(userListeningHistory.get(song));
			}
			else {
				songPlaycountFeature.add(0);
			}
		}
		
		return songPlaycountFeature;
	}
	
	/**
	 * Calculate the cosine similarity between two feature vectors.
	 * @param testFeature
	 * @param trainFeature
	 * @return
	 */
	private Double getCosineSimilarityScore(List<Integer> testFeature, List<Integer> trainFeature)
	{
		int numerator = 0;
		int magnitudeTestValue = 0;
		int magintudeTrainValue = 0;
		for(int i=0; i < testFeature.size(); i++){
			int testFeatValue = testFeature.get(i);
			int trainFeatValue = trainFeature.get(i);
			numerator += testFeatValue*trainFeatValue;
			magnitudeTestValue += testFeatValue*testFeatValue;
			magintudeTrainValue += trainFeatValue*trainFeatValue;
		}
		
		return numerator/(double)(Math.sqrt(magnitudeTestValue)*Math.sqrt(magintudeTrainValue));
	}
	
	/**
	 * Models a similar user to the test user being investigated during K-NN processing.
	 * @author excelsior
	 *
	 */
	public class SimilarUser implements Comparable
	{
		String userId;
		Double simScore;
		
		public SimilarUser(String user, Double score)
		{
			this.userId = user;
			this.simScore = score;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof SimilarUser) {
				SimilarUser that = (SimilarUser)obj;
				return Objects.equal(this.userId, that.userId) &&
						Objects.equal(this.simScore, that.simScore);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hashCode(this.userId, this.simScore);
		}
		
		public int compareTo(Object obj)
		{
			SimilarUser that = (SimilarUser)obj;
			return Double.compare(this.simScore, that.simScore);
		}
	}
}
