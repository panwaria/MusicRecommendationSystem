package models;

import java.util.List;
import java.util.Map;

/**
 * Class to describe more details about a song
 */
public class SongDetail extends Song
{
	/* Member Variables */
	public List<String> mTrackIDList;			// Indicates a mapping of this song to these trackIDs
	public Map<String, Integer> mLyricsMap;	//Map<String word, Integer frequency>

	
	/* Methods */
}
