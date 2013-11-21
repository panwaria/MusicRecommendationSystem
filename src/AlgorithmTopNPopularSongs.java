import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to represent TopNPopularSongs. The recommendations from this algorithm will be 
 * used as a baseline as compared to our other algorithms.
 */
public class AlgorithmTopNPopularSongs  implements Algorithm
{
	/**
	 * TODO: Let's describe the algorithm used and the features considered here.
	 * 
	 */
	
	/* Member Variables */
	
	int mSongsCount = 0; 						// Number of songs to be returned for every user.
	List<Song> mOverallNPopularSongs = null;	// List of overall popular songs in the training dataset.
	
	
	/* Methods */
	
	AlgorithmTopNPopularSongs(int N)
	{
		mSongsCount = N;
	}
	
	@Override
	public Map<String, List<Song>> getTopNPopularSongs(int N) 
	{
		// TODO: Not sure if we need this method.
		return null;
	}

	@Override
	public void generateModel(DataSet trainDataSet) 
	{
		// No model to generate here as such. We just get N popular songs from the dataset.
		mOverallNPopularSongs = trainDataSet.getOverallNPopularSongs(mSongsCount);
	}

	@Override
	public Map<String, List<Song>> recommend(DataSet testSet) 
	{
		List<String> allTestSetUsers = testSet.getListOfUsers();
		if(allTestSetUsers == null || allTestSetUsers.isEmpty() || mOverallNPopularSongs == null)
			return null;
		
		Map<String, List<Song>> recommendation = new HashMap<String, List<Song>>();
		
		// Recommending same set of popular songs to every user.
		for (String userID : allTestSetUsers) 
			recommendation.put(userID, mOverallNPopularSongs);
		
		return recommendation;
	}

}