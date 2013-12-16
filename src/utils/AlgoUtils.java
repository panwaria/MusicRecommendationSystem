package utils;

import java.util.PriorityQueue;

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
}
