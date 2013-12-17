package utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.google.common.collect.Lists;

import models.DataSet;
import models.Song;
import models.SongScore;

/**
 * Utility functions used across algorithms.
 * 
 * @author excelsior
 *
 */
public class AlgoUtils {

	/**
	 * Updates the priority queue to ensure that the top N songs are only retained based on their
	 * scores.
	 * 
	 * @param topNSongs
	 * @param song
	 * @param score
	 */
	public static void updateTopNSongs(int numSongsToRecommend, PriorityQueue<SongScore> topNSongs, 
			String song, double score)
	{
		if(topNSongs.size() < numSongsToRecommend) {
			topNSongs.add(new SongScore(song, score));
		}
		else {
			SongScore head = topNSongs.peek();
			if(Double.compare(head.getScore(),score) < 0 ) {
				topNSongs.remove();
				topNSongs.add(new SongScore(song, score));
			}
		}
	}
	
	/**
	 * Extracts the final songs to be recommended from the intermediate list of top N song scores.
	 * @param topSongScores
	 * @param trainDataset
	 * @return
	 */
	public static List<Song> getTopNSongs(PriorityQueue<SongScore> topSongScores, DataSet trainDataset)
	{
		List<Song> topNSongs = Lists.newArrayList();
		Map<String, Song> trainSongMap = trainDataset.getSongMap();
		for(SongScore songScore : topSongScores) {
			topNSongs.add(trainSongMap.get(songScore.getSong()));
		}
		
		return topNSongs;
	}
	
	/**
	 * Checks if the number of songs recommended by the algorithm is equal to required number of
	 * recommendation songs. If it is slightly less, it picks the best songs from overall popular
	 * songs list and appends to the set of recommendations.
	 * @return
	 */
	public static List<Song> checkAndUpdateTopNSongs(List<Song> recommendations, int numSongsToRecommend, 
													 List<Song> overallPopularSongs)
	{
		if(recommendations != null && recommendations.size() == numSongsToRecommend) {
			return recommendations;
		}
		
		if(recommendations == null) {
			recommendations = Lists.newArrayList();
		}
		int numSongsToAdd = numSongsToRecommend - recommendations.size();
		
		// Start fetching the most popular songs, which have been sorted based on their overall
		// listeners frequency.
		for(Song song : overallPopularSongs) {
			if(numSongsToAdd == 0) {
				break;
			}
			
			recommendations.add(song);
			--numSongsToAdd;
		}
		
		return recommendations;
	}
}
