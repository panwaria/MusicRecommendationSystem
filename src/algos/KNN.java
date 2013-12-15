package algos;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import models.DataSet;
import models.Song;

/**
 * Class to represent k-Nearest-Neighbor Algorithm
 */
public class KNN implements Algorithm
{
	// Number of songs to recommend for a user
	int mSongsCount = 0;
	DataSet trainSet = null;
	
	public KNN(int numSongsToRecommend)
	{
		this.mSongsCount = mSongsCount;
	}
	
	public void generateModel(DataSet trainSet)
	{
		this.trainSet = trainSet;
	}

	public Map<String, List<Song>> recommend(DataSet testSet)
	{
		List<String> testUsers = testSet.getListOfUsers();
		if(testUsers == null || testUsers.isEmpty()) {
			return null;
		}
		
		Map<String, List<Song>> songRecommendationsForUserMap = Maps.newHashMap();
		for(String user : testUsers) {
			List<Song> recommendations = getSongRecommendations(user);
			songRecommendationsForUserMap.put(user, recommendations);
		}
		
		return songRecommendationsForUserMap;
	}
	
	/**
	 * Get all the song recommendations for the specified user.
	 */
	private static List<Song> getSongRecommendations(String user)
	{
		List<Song> recommendations = Lists.newArrayList();
		return recommendations;
	}

}
