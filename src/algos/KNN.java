package algos;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import models.DataSet;
import models.Song;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Class to represent k-Nearest-Neighbor Algorithm
 */
public class KNN implements Algorithm
{
	// Number of songs to recommend for a user
	private static int mSongsCount = 0;

	private static DataSet trainDataset = null;
	
	private static int K = 1;
	
	public KNN(int numSongsToRecommend)
	{
		this.mSongsCount = mSongsCount;
	}
	
	public void generateModel(DataSet trainDataset)
	{
		this.trainDataset = trainDataset;
	}

	public Map<String, List<Song>> recommend(DataSet tuneDataset)
	{
		List<String> tuneUsers = tuneDataset.getListOfUsers();
		if(tuneUsers == null || tuneUsers.isEmpty()) {
			return null;
		}
		
		Map<String, List<Song>> songRecommendationsForUserMap = Maps.newHashMap();
		for(String user : tuneUsers) {
			List<Song> recommendations = getSongRecommendations(user, tuneDataset);
			songRecommendationsForUserMap.put(user, recommendations);
		}
		
		return songRecommendationsForUserMap;
	}
	
	/**
	 * Get all the song recommendations for the specified user.
	 */
	private List<Song> getSongRecommendations(String user, DataSet tuneDataset)
	{
		Set<String> allSongs = Sets.newHashSet();
		allSongs.addAll(trainDataset.getmSongMap().keySet());
		allSongs.addAll(tuneDataset.getmSongMap().keySet());
		List<String> allSongsList = Lists.newArrayList(allSongs);
		
		PriorityQueue<SimilarUser> kNNUsers = getKNNForUser(user, tuneDataset, allSongsList);
		List<Song> recommendations = getSongsBasedOnKNN(kNNUsers);
		return recommendations;
	}
	
	/**
	 * Get top N most similar songs based on the K nearest neighbors
	 */
	private List<Song> getSongsBasedOnKNN(PriorityQueue<SimilarUser> kNNUsers)
	{
		List<Song> recommendations = Lists.newArrayList();
		Map<String, Integer> allSongsBwKUsers = Maps.newHashMap();
		for(SimilarUser user : kNNUsers) {
			String userName = user.userId;
			Map<String, Integer> listeningHistory = trainDataset.getmUserListeningHistory().get(userName);
			for(Map.Entry<String, Integer> entry : listeningHistory.entrySet()) {
				String songName = entry.getKey();
				int songCount = 0;
				if(allSongsBwKUsers.containsKey(songName)) {
					songCount = allSongsBwKUsers.get(songName);
				}
				songCount += 1 + user.simScore;
				allSongsBwKUsers.put(songName, songCount);
			}
		}

		PriorityQueue<SongScore> topSongs = new PriorityQueue<KNN.SongScore>(mSongsCount);
		for(Map.Entry<String, Integer> entry : allSongsBwKUsers.entrySet()) {
			if(topSongs.size() < mSongsCount) {
				topSongs.add(new SongScore(entry.getKey(), entry.getValue()));
			}
			else {
				SongScore head = topSongs.peek();
				if(head.popularityScoreAmongKNN < entry.getValue()) {
					topSongs.remove();
					topSongs.add(new SongScore(entry.getKey(), entry.getValue()));
				}
			}
		}
		
		Map<String, Song> trainSongMap = trainDataset.getmSongMap();
		for(SongScore songScore : topSongs) {
			recommendations.add(trainSongMap.get(songScore.songId));
		}
		
		return recommendations;
	}
	
	/**
	 * Return the K nearest neighbors for a listener.
	 * 
	 * This can be done by calculating the cosine distance between two users where the feature
	 * vector is the weight of all the songs.
	 */
	private PriorityQueue<SimilarUser> getKNNForUser(String user, DataSet tuneDataset, List<String> allSongs)
	{
		List<String> trainUsers = trainDataset.getListOfUsers();
		List<Integer> tuneFeature = getFeatureVector(user, allSongs, tuneDataset);
		PriorityQueue<SimilarUser> kNNUsers = new PriorityQueue<KNN.SimilarUser>(K);
		for(String trainUser : trainUsers) {
			List<Integer> trainFeature = getFeatureVector(trainUser, allSongs, trainDataset);
			Double simScore = getCosineSimilarityScore(tuneFeature, trainFeature);
			if(kNNUsers.size() < K) {
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
		Map<String, Integer> userListeningHistory = dataset.getmUserListeningHistory().get(user);
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
	 * @param tuneFeature
	 * @param trainFeature
	 * @return
	 */
	private Double getCosineSimilarityScore(List<Integer> tuneFeature, List<Integer> trainFeature)
	{
		int cosineSim = 0;
		for(int i=0; i < tuneFeature.size(); i++){
			int tuneFeatValue = tuneFeature.get(i);
			int trainFeatValue = trainFeature.get(i);
			int diff = tuneFeatValue - trainFeatValue;
			cosineSim += diff*diff;
		}
		
		return Math.sqrt(cosineSim);
	}
	
	public class SongScore implements Comparable
	{
		String songId;
		int popularityScoreAmongKNN;
		
		public SongScore(String songId, int score)
		{
			this.songId = songId;
			this.popularityScoreAmongKNN = score;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof SongScore) {
				SongScore that = (SongScore)obj;
				return Objects.equal(this.songId, that.songId) &&
						Objects.equal(this.popularityScoreAmongKNN, that.popularityScoreAmongKNN);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hashCode(this.songId, this.songId);
		}
		
		public int compareTo(Object obj)
		{
			SongScore that = (SongScore)obj;
			return this.popularityScoreAmongKNN - that.popularityScoreAmongKNN;
		}		
	}
	
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
