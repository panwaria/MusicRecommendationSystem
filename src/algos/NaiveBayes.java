package algos;

import java.util.List;
import java.util.Map;

import models.DataSet;
import models.Song;

/**
 * Class to represent Naive Bayes Algorithm
 */
public class NaiveBayes  implements Algorithm
{
	int mSongsCount = 0;
	
	public NaiveBayes(int mSongsCount) {
		super();
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
