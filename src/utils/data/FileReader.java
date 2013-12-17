package utils.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import models.DataSet;
import models.Song;
import utils.Utility;

import com.google.common.collect.Maps;

/**
 * Reads data from filesystem.
 * 
 * @author excelsior
 *
 */
public class FileReader implements Reader 
{
	public DataSet createDataSet(String datasetName) {
		String csvFilePath = System.getProperty("user.dir") + "/data/" + datasetName;
		
		Map<String, Map<String, Integer>> listeningHistory = Maps.newHashMap();
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(csvFilePath));
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		scanner.useDelimiter("\n");
		while(scanner.hasNext()) {
			String line = scanner.next().trim();
			if(line.isEmpty()) {
				continue;
			}
		    String columns[] = line.split(","); 	
			String userId = columns[0];
			String songId = columns[1];
			int playCount = Integer.parseInt(columns[2]);
			
			Map<String, Integer> userListeningHistory = null;
			if(listeningHistory.containsKey(userId)) {
				userListeningHistory = listeningHistory.get(userId);
			}
			else {
				userListeningHistory = Maps.newHashMap();
			}
			
			userListeningHistory.put(songId, playCount);
			listeningHistory.put(userId, userListeningHistory);		    
		}
		
		Map<String, Song> songMap = Utility.getSongMapForListeningHistory(listeningHistory);
		
		return new DataSet(listeningHistory, songMap);
	}
	
}
