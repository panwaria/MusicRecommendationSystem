package models;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Class to represent basic information about a song.
 */
public class Song 
{
	/* Member Variables */
	public String mSongID;
	public List<String> mListenersList;		// List of users who listened to this song.
	
	public String getmSongID() {
		return mSongID;
	}

	public void setmSongID(String mSongID) {
		this.mSongID = mSongID;
	}

	public List<String> getmListenersList() {
		return mListenersList;
	}

	public void setmListenersList(List<String> mListenersList) {
		this.mListenersList = mListenersList;
	}

	public Song()
	{
		
	}
	
	public Song(String mSongID, List<String> mListenersList) {
		super();
		this.mSongID = mSongID;
		this.mListenersList = mListenersList;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.mSongID);
	}
	
	@Override
	public boolean equals(Object obj)
	{
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    final Song other = (Song) obj;
	    return 	Objects.equal(this.mSongID, other.mSongID);		
	}

}
