package utils.data;

import models.DataSet;

/**
 * Read data from various sources
 * @author excelsior
 *
 */
public interface Reader 
{
	/**
	 * 
	 * @param datasetName
	 * @return
	 */
	public DataSet createDataSet(String datasetName);
}
