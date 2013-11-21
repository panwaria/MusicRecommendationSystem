import java.util.List;
import java.util.Map;

/**
 * Interface to represent basic methods required for an Algorithm to recommend songs to a user.
 */
public interface Algorithm
{
	/**
	 * Method to get N popular Songs from the DataSet.
	 * @param N		Number of popular songs to fetch
	 * @return		Map of each user with recommended N popular songs
	 */
	Map<String, List<Song>> getTopNPopularSongs(int N);
	
	/**
	 * Method to generate a model.
	 * @param d
	 */
	void generateModel(DataSet trainSet);
	
	/**
	 * Method to give recommendations.
	 * @return	Map of each user with recommended N popular songs
	 */
	Map<String, List<Song>> recommend(DataSet testSet);

}
