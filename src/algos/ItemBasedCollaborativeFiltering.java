package algos;

import java.util.List;
import java.util.Map;

import models.DataSet;
import models.Song;

/**
 * Implements in-memory item-based collaborative filtering for making best recommendations.
 * 
 * The idea is that if songs X and Y are frequently listened together and user X has already listened
 * to song X, he/she would most likely also like song Y.
 * @author excelsior
 *
 */
public class ItemBasedCollaborativeFiltering implements Algorithm{

	public void generateModel(DataSet trainSet) {
		// TODO Auto-generated method stub
		
	}

	public Map<String, List<Song>> recommend(DataSet testSet) {
		// TODO Auto-generated method stub
		return null;
	}

}
