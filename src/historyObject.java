/******************************************************************************
* The following class combines a String with a boolean to be used to store linear history.
* Due to the implementation of branched history the linear history for the back
* and forward buttons needs to know if the URL was entered manually (a child of the root)
* or a hyperlink (a child of another object) so the back button knows how to adjust 
* the variable currentNode in the browserWindow class.
 ******************************************************************************/

public class historyObject 
{
	private boolean wasEntered;
	private String url;
	
	/**
	 * This method serves as the constructor setting the variables wasEntered and URl
	 * equal to the given parameters
	 * @param wasEntered	A boolean representing if the URL was manually entered by the user
	 * or clicked on from another website.
	 * @param url	A String representing the URL of the item in history
	 */
	public historyObject(boolean wasEntered, String url)
	{
		this.wasEntered = wasEntered;
		this.url = url;
	}
	
	/**
	 * A getter that returns whether or not the URL was entered manually
	 * @return	True is URL was entered manually, false if not
	 */
	public Boolean wasEntered()
	{
		return wasEntered;
	}
	
	/**
	 * A getter that returns the URL in the history
	 * @return	The URL in the history
	 */
	public String getUrl()
	{
		return url;
	}
	
}
