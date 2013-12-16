package algos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import models.DataSet;
import models.Song;

/**
 * Class to represent Naive Bayes Algorithm
 */
public class NaiveBayes  implements Algorithm
{
	int mSongsCount = 0;
	private static DataSet mTrainDataset = null;

	public NaiveBayes(int numSongsToRecommend)
	{
		super();
		this.mSongsCount = numSongsToRecommend;
	}

	public void generateModel(DataSet trainDataset) 
	{
		// TODO Auto-generated method stub
		this.mTrainDataset = trainDataset;

	}

	public Map<String, List<Song>> recommend(DataSet testVisibleDataset)
	{
		List<String> testUsers = testVisibleDataset.getListOfUsers();
		if(testUsers == null || testUsers.isEmpty()) 
		{
			return null;
		}
		Map<String, List<Song>> songRecommendationsForUserMap = Maps.newHashMap();
		for(String user : testUsers) 
		{
			List<Song> recommendations = getSongRecommendations(user, testVisibleDataset);
			songRecommendationsForUserMap.put(user, recommendations);
		}
		return songRecommendationsForUserMap;		
	}

	private List<Song> getSongRecommendations(String user, DataSet testVisibleDataset) 
	{
		Set<String> allSongs = Sets.newHashSet();
		allSongs.addAll(mTrainDataset.getSongMap().keySet());
		List<String> allSongsList = Lists.newArrayList(allSongs);
		PriorityQueue<recoSong> pq = new PriorityQueue<recoSong>();
		for(String songItem: allSongsList)
		{
			if(!testVisibleDataset.getUserListeningHistory().get(user).containsKey(songItem))
			{
				Double logProb = 0.0;
				Double alpha = 0.0;
				Set<String> listenedSongs = testVisibleDataset.getUserListeningHistory().get(user).keySet();
				
				Set<String> tempSet = Sets.newHashSet();
				for(String listener: mTrainDataset.getSongMap().get(songItem).getListenersList())
				{
					tempSet.add(listener);
				}

				for(String listenedSong: listenedSongs)
				{
					Integer countListenedSong = 0, countJointListenedAndNotListenedSong = 0, countNotListenedSong=0;
					if(mTrainDataset.getSongMap().containsKey(listenedSong))
					{
						countListenedSong = mTrainDataset.getSongMap().get(listenedSong).getListenersList().size();
					}
					if(mTrainDataset.getSongMap().containsKey(songItem))
					{
						countNotListenedSong=mTrainDataset.getSongMap().get(songItem).getListenersList().size();
					}
					Set<String> commonUsers = Sets.newHashSet();
					if(mTrainDataset.getSongMap().containsKey(listenedSong))
					for(String listener: mTrainDataset.getSongMap().get(listenedSong).getListenersList())
					{
						if(tempSet.contains(listener))
						{
							commonUsers.add(listener);
						}
					}
					countJointListenedAndNotListenedSong = commonUsers.size();
					if(countListenedSong>0 && countNotListenedSong>0)
					{
						logProb += Math.log((double)countJointListenedAndNotListenedSong/(double)countListenedSong/Math.pow(countNotListenedSong, alpha));
					}

				}
				if(logProb < 0)
				{
					pq.offer(new recoSong(songItem,Math.pow(Math.E,logProb)));
					while(pq.size()>mSongsCount)
						pq.remove();
				}
			}
		}
		List<Song> toReturn = new ArrayList<Song>();
		while(pq.size()>0)
		{
			String temp = pq.poll().SongName;
			toReturn.add(mTrainDataset.getSongMap().get(temp));
		}
		/*
		if(toReturn.size()<mSongsCount)
			System.out.println("less");*/
		int i = 30;
		while(toReturn.size() < mSongsCount)
			toReturn.add(mTrainDataset.getSongMap().get(allSongsList.get(i++)));
		return toReturn;
	}



}
class recoSong implements Comparable<recoSong>
{
	String SongName;
	Double Prob;
	public recoSong(String name, Double prob)
	{
		SongName = name;
		Prob = prob;
	}
	
	public int compareTo(recoSong other) {
		// TODO Auto-generated method stub
		if(this.Prob <other.Prob)
			return -1;
		else if(this.Prob > other.Prob)
			return 1;
		else
			return 0;
	}

}
