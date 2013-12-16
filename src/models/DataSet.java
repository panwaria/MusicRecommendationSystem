package models;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class to represent a dataset.
 */
public class DataSet 
{
	private static Logger LOG = Logger.getLogger(DataSet.class);

	// User Listening History : Map<UserID, Map<SongID, PlayCount>>
	private Map<String, Map<String, Integer>> mUserListeningHistory = Maps.newHashMap();
	
	// Mapping of Song ID to Song Object: Map<SongID, Song>
	private Map<String, Song> mSongMap = Maps.newHashMap();
	
	public DataSet(Map<String, Map<String, Integer>> mUserListeningHistory,
			Map<String, Song> mSongMap)
	{
		super();
		this.mUserListeningHistory = mUserListeningHistory;
		this.mSongMap = mSongMap;
	}

	/**
	 * Get the summary of the dataset
	 * @return
	 */
	public String getDatasetStats()
	{
		StringBuilder stats = new StringBuilder();
		stats.append("Users: ").append(mUserListeningHistory.keySet().size()).append("\t");
		stats.append("Songs: ").append(mSongMap.keySet().size());
		
		return stats.toString();
	}
	
	public Map<String, Map<String, Integer>> getUserListeningHistory()
	{
		return mUserListeningHistory; 
	}

	public void setUserListeningHistory(Map<String, Map<String, Integer>> mUserListeningHistory) 
	{
		this.mUserListeningHistory = mUserListeningHistory;
	}

	public Map<String, Song> getSongMap() 
	{
		return mSongMap;
	}

	public void setSongMap(Map<String, Song> mSongMap) 
	{
		this.mSongMap = mSongMap;
	}

	/**
	 * Return list of all unique UserIDs
	 * @return	List of UserIDs
	 */
	public List<String> getListOfUsers()
	{
		LOG.debug("No of users in dataset : " + mUserListeningHistory.keySet().size());
		return Lists.newArrayList(mUserListeningHistory.keySet());
	}
	
	/**
	 * Method to get overall N popular songs in the data set. We define popularity
	 * by the number if unique listeners of this song.
	 * @param N		Number of popular songs
	 * @return		N most popular songs
	 */
	public List<Song> getOverallNPopularSongs(int N)
	{
		LOG.debug("Calculating the " + N + " most popular songs in the dataset ..");
		PriorityQueue<SongFrequency> topSongs = new PriorityQueue<DataSet.SongFrequency>(N);
		for(Map.Entry<String, Song> entry : mSongMap.entrySet()) {
			String songId = entry.getKey();
			int numUsersListened = entry.getValue().getmListenersList().size();
			
			// If the priority queue is at its max capacity, we need to evaluate if we should
			// add the latest object or not.
			if(topSongs.size() < N) {
				topSongs.add(new SongFrequency(songId, numUsersListened));
			}
			else {
				SongFrequency head = topSongs.peek();
				if(head.numUsersListened < numUsersListened) {
					topSongs.remove(head);
					topSongs.add(new SongFrequency(songId, numUsersListened));
				}
			}
		}

		List<Song> overallNPopularSongs = Lists.newArrayList();
		for(SongFrequency songFreq : topSongs) {
			overallNPopularSongs.add(mSongMap.get(songFreq.songId));
		}
		
		return overallNPopularSongs;
	}
	
	/**
	 * Get all the songs listened by a user.
	 * @param user
	 * @return
	 */
	public List<String> getSongsForUser(String user)
	{
		if(mUserListeningHistory.containsKey(user)) {
			return Lists.newArrayList(mUserListeningHistory.get(user).keySet());
		}
		return Lists.newArrayList();
	}
	
	/**
	 * Get all the users who have listened to a song
	 * @return
	 */
	public List<String> getUsersForSong(String song)
	{
		if(mSongMap.containsKey(song)) {
			return mSongMap.get(song).mListenersList;
		}
		return Lists.newArrayList();
	}
	
	private class SongFrequency implements Comparable
	{
		String songId;
		int numUsersListened;
		
		public SongFrequency(String songId, int numUsersListened)
		{
			this.songId = songId;
			this.numUsersListened = numUsersListened;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof SongFrequency) {
				SongFrequency that = (SongFrequency)obj;
				return Objects.equal(this.songId, that.songId) &&
						Objects.equal(this.numUsersListened, that.numUsersListened);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hashCode(this.songId, this.numUsersListened);
		}
		
		public int compareTo(Object obj)
		{
			SongFrequency that = (SongFrequency)obj;
			return this.numUsersListened - that.numUsersListened;
		}
		
	}
	
}
