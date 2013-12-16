package models;

import com.google.common.base.Objects;

/**
 * A model class that encapsulates the score assigned to a song during various algorithmic runs.
 * 
 * @author excelsior
 *
 */
public class SongScore implements Comparable<SongScore>
{
	private String song;
	private double score;
	
	public SongScore(String song, double score) 
	{
		super();
		this.song = song;
		this.score = score;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SongScore) {
			SongScore that = (SongScore)obj;
			return Objects.equal(this.song, that.song) &&
					Objects.equal(this.score, that.score);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.song, this.song);
	}
	
	public int compareTo(SongScore that) {
		return Double.compare(this.score, that.score);
	}

	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}		
	
}
