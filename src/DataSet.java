import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to represent a dataset.
 */
public class DataSet 
{
	/* Member Variables */

	// User Listening History : Map<UserID, Map<SongID, PlayCount>>
	public Map<String, Map<String, Integer>> mUserListeningHistory = new HashMap<String, Map<String, Integer>>();
	
	// Mapping of Song ID to Song Object: Map<SongID, Song>
	public Map<String, Song> mSongMap = new HashMap<String, Song>();
	
	
	/* Methods */
	
	/**
	 * Return list of all unique UserIDs
	 * @return	List of UserIDs
	 */
	List<String> getListOfUsers()
	{
		// TODO: Implement.
		return null;
	}
	
	/**
	 * Method to get overall N popular songs in the data set. We define popularity
	 * by the number if unique listeners of this song.
	 * @param N		Number of popular songs
	 * @return		N most popular songs
	 */
	List<Song> getOverallNPopularSongs(int N)
	{
		// TODO: Implement.
		
		
		return null;
	}
	
	/**
	 * Method to generate Stratified DataSets.
	 * @return List of Stratified DataSets
	 */
	List<DataSet> generateStratifiedSamples()
	{
		// TODO: Implement.
		return null;
	}
	
	/**
	 * Method to get top songs for a given user.
	 * @param userID	User Identifier
	 * @param N			Number of songs to return
	 * @return			List of top songs listened by a given user.
	 */
	List<Song> getNTopSongsForUser(String userID, int N)
	{
		// TODO: Implement.
		return null;
	}
	
	/**
	 * Method to get the list of top users who have listened to a given song.
	 * @param songID	Song Identifier
	 * @param N			Number of users to return
	 * @return			List of top users who have listened to a given song
	 */
	List<Integer> getNTopUsersForSong(String songID, int N)
	{
		// TODO: Implement.
		return null;
	}

	
	
	

}
