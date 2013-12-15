package algos;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import models.DataSet;
import models.Song;

import com.google.common.collect.Maps;

/**
 * Class to represent TopNPopularSongs algorithm. 
 * 
 * The recommendations from this algorithm will be used as a baseline to compare the accuracy and
 * performance of other recommendation algorithms.
 */
public class TopNPopularSongs  implements Algorithm
{
	private Logger LOG = Logger.getLogger(TopNPopularSongs.class);
	
	int mSongsCount = 0; 						// Number of songs to be returned for every user.
	List<Song> mOverallNPopularSongs = null;	// List of overall popular songs in the training dataset.
	
	
	/* Methods */
	
	public TopNPopularSongs(int numSongsToRecommend)
	{
		this.mSongsCount = numSongsToRecommend;
	}
	
	public void generateModel(DataSet trainDataSet) 
	{
		// No model to generate here as such. We just get N popular songs from the dataset.
		mOverallNPopularSongs = trainDataSet.getOverallNPopularSongs(mSongsCount);
		
		LOG.info("Most popular songs in the dataset ..");
		for(Song s: mOverallNPopularSongs) {
			LOG.info("Song : " + s.mSongID + " with user count " + s.getmListenersList().size());
		}
	}

	/**
	 * Recommend the top songs based on the overall popularity of songs in the dataset.
	 */
	public Map<String, List<Song>> recommend(DataSet testSet) 
	{
		List<String> allTestSetUsers = testSet.getListOfUsers();
		if(allTestSetUsers == null || allTestSetUsers.isEmpty() || mOverallNPopularSongs == null)
			return null;
		
		Map<String, List<Song>> recommendations = Maps.newHashMap();
		// Recommending same set of popular songs to every user.
		for (String userID : allTestSetUsers) 
			recommendations.put(userID, mOverallNPopularSongs);
		
		return recommendations;
	}

}