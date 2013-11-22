package algos;

import java.util.List;
import java.util.Map;

import models.DataSet;
import models.Song;

/**
 * Class to represent k-Nearest-Neighbor Algorithm
 */
public class KNN implements Algorithm
{
	// Number of songs to recommend for a user
	int mSongsCount = 0;
	
	public KNN(int numSongsToRecommend)
	{
		this.mSongsCount = mSongsCount;
	}
	
	public void generateModel(DataSet trainSet)
	{
		// TODO Auto-generated method stub
		
	}

	public Map<String, List<Song>> recommend(DataSet testSet)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
