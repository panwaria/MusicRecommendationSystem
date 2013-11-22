package algos;

import java.util.List;
import java.util.Map;

import models.DataSet;
import models.Song;

/**
 * Interface to represent basic methods required for an Algorithm to recommend songs to a user.
 */
public interface Algorithm
{
	/**
	 * Method to generate a model.
	 * @param d
	 */
	public void generateModel(DataSet trainSet);
	
	/**
	 * Method to give recommendations.
	 * @return	Map of each user with recommended N popular songs
	 */
	public Map<String, List<Song>> recommend(DataSet testSet);

}
