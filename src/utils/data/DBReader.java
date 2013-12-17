package utils.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import models.Constants;
import models.DataSet;
import models.Song;

/**
 * Helper Class to create a dataset from the underlying database.
 */
public class DBReader implements Reader{
	
	// Connect to the database
	public DBReader() 
	{	}
	
	private Connection getDBConnection()
	{
		// JDBC connection to the database
		Connection dbConn = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load mysql jdbc connector ..");
		}
		
		try {
			dbConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/msdchallenge","msd_user", "msd_user");
		} catch (SQLException e) {
			e.printStackTrace();			
			throw new RuntimeException("Connection Failed! Check output console");
		}
		
		if(dbConn != null) {
			//System.out.println("Connected to the MySQL database ..");
		}
		else {
			throw new RuntimeException("Failed to connect to the MySQL database ..");
		}
		
		return dbConn;
	}
	
	/**
	 * Important to close the database connection, so that database resources are not tied up.
	 * @param dbConn	JDBC connection to the database
	 */
	private void closeDBConnection(Connection dbConn)
	{
		try {
			dbConn.close();
			//System.out.println("Closed db connection ..");
		} catch (SQLException e) {
			System.err.println("Failed to close connection to the MySQL database ..");
			e.printStackTrace();
		}
	}

	/**
	 * Method to create a dataset given the location of the data in the
	 * database.
	 * 
	 * @param datasetTableName
	 * @return Created Dataset
	 */
	public DataSet createDataSet(String datasetTableName) 
	{
		String selectSQL = 	"SELECT " + Constants.COLUMN_USER_ID + ", " + Constants.COLUMN_SONG_ID + ", " + Constants.COLUMN_PLAY_COUNT + " " + 
							"FROM " + datasetTableName + " " +
							"ORDER by " + Constants.COLUMN_USER_ID;
		System.out.println("Querying db for SQL query : " + selectSQL);
		
		Map<String, Map<String, Integer>> userListeningMap = Maps.newHashMap();
		Map<String, Song> songIdToObjMap = Maps.newHashMap();
		
		long startTime = System.currentTimeMillis();
		PreparedStatement preparedStatement = null;
		Connection dbConn = getDBConnection();
		try 
		{
			preparedStatement = dbConn.prepareStatement(selectSQL);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()) 
			{
				String userId = rs.getString(Constants.COLUMN_USER_ID);
				String songId = rs.getString(Constants.COLUMN_SONG_ID);
				Integer playCount = rs.getInt(Constants.COLUMN_PLAY_COUNT);
				
				// Update listening history for a user
				Map<String, Integer> userIdListenedSongsMap = null;
				if(userListeningMap.containsKey(userId)) 
					userIdListenedSongsMap = userListeningMap.get(userId);
				else
					userIdListenedSongsMap = Maps.newHashMap();
				
				userIdListenedSongsMap.put(songId, playCount);
				userListeningMap.put(userId, userIdListenedSongsMap);
				
				// Update the list of users who have listened to a particular song
				Song song = null;
				List<String> listenersList = null;
				if(songIdToObjMap.containsKey(songId)) 
				{
					song = songIdToObjMap.get(songId);
					listenersList = song.getListenersList();
				}
				else 
				{
					song = new Song();
					listenersList = Lists.newArrayList();
				}
				
				listenersList.add(userId);
				
				song.setSongID(songId);
				song.setListenersList(listenersList);
				
				songIdToObjMap.put(songId, song);
			}			
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally 
		{
			if (preparedStatement != null) 
			{
				try
				{
					preparedStatement.close();
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
 
			if (dbConn != null) 
				closeDBConnection(dbConn);
		}		
		
		long endTime = System.currentTimeMillis();
		int numSongs = songIdToObjMap.keySet().size();
		int numUsers = userListeningMap.keySet().size();
		System.out.println("Found " + numUsers + " users, " + numSongs + " songs .");
		System.out.println("Executed query in " + (endTime - startTime)/1000 + " seconds.");
		
		return new DataSet(userListeningMap, songIdToObjMap);
	}
	
}