import java.util.ArrayList;
/******************************************************************************
* The following class handles a tree with an infinite number of children per node.
* The tree stores a String to be its contents, its parent so it is possible to navigate
*  through the tree and an ArrayList of its children. This class can be used by storing
*  just one pointer to any node in the tree. In this program it is used to store the
*  browser history.
 ******************************************************************************/

public class treeNode 
{
	private ArrayList<treeNode> children;
	private String contents;
	private treeNode parent;
	
	/**
	 * This method serves as this class' constructor. It initializes the ArrayList
	 * of children, sets the contents equal to the string provided and sets the parent
	 * equal to the parent provided.
	 * @param parent	The parent of the node
	 * @param contents	The String stored in the node.
	 */
	public treeNode(treeNode parent, String contents)
	{
		this.contents = contents;
		this.parent = parent;
		children = new ArrayList<treeNode>();
	}
	
	/**
	 * This method is a getter, it just returns the contents
	 * @return	The string stored in the node
	 */
	public String getContents()
	{
		return contents;
	}
	
	/**
	 * This method adds a child to the node by adding a new node with a parent of itself
	 * to the ArrayList of children
	 * @param stringContents	The contents of the new node as a String
	 */
	public void addChild (String stringContents)
	{
		children.add(new treeNode(this, stringContents));
	}
	
	
	
	/**
	 * This method simply returns the last child of the ArrayList of children
	 * @return	The last child in the ArrayList
	 */
	public treeNode getLastChild()
	{
		if(children == null)
		{
			return null;
		}
		else
		{
			return children.get(children.size() - 1);
		}
	}
	
	/**
	 * This method returns the child at a given index in the ArrayList.
	 * @param index		The index of the ArrayList containing the child requested
	 * @return	The child at the given index as a treeNode
	 */
	public treeNode getChildAtIndex(int index)
	{
		if(children == null || index > children.size() - 1)
		{
			return null;
		}
		else
		{
			return children.get(index);
		}
	}
	
	/**
	 * This method returns all of the children as a String
	 * @return	The contents of ArrayList children as a String
	 */
	public String childrenToString()
	{
		String childrenAsString = "";
		for (treeNode t: children)
		{
			childrenAsString += t.getContents() + "\n";
		}
		if(childrenAsString.endsWith("\n"))
		{
			childrenAsString = childrenAsString.substring(0, childrenAsString.length()-2);
		}
		return childrenAsString;
	}
	
	/**
	 * This method checks if the node has children
	 * @return	Returns true if the ArrayList children is empty, false if it is not
	 */
	public boolean hasChildren()
	{
		if(children.size() >= 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * This method is a getter that just returns the parent
	 * @return	The parent of the node
	 */
	public treeNode getParent()
	{
		return parent;
	}
	
	/**
	 * This method returns the number of children the node has
	 * @return	Number of children the node has
	 */
	public int getNumOfChildren()
	{
		return children.size();
	}
	
	/**
	 * This method returns the contents of the node as a String along with its distance
	 * from the root.
	 */
	public String toString()
	{
		int level = 0;
		treeNode node = this;
		while(node.getParent() !=null)
		{
			node = node.getParent();
			level++;
		}
		
		return "Contents: " + contents + "\n" + "Level: " + level; 
	}
	
	/**
	 * This method returns the first child with a given contents. It iterates
	 * through the list of children then returns the first child with the same
	 * contents as the parameter contentsOfNode. If nothing is found, null is returned
	 * @param contentsOfNode	A string that the childrens contents are being compared to
	 * @return	The first node with contents matching parameter contentsOfNode or null
	 * if nothing is found
	 */
	
	public treeNode nodeWith(String url)
	{
		for (treeNode t: children)
		{
			
			if(t.contents.equals(url))
			{
				return t;
			}
		}
		return null;
	}
	
	/**
	 * This method cycles up the tree until it finds the tree then returns the root
	 * @return	The root of the tree
	 */
	public treeNode getRoot()
	{
		treeNode t = this;
		while(t.getParent() != null)
		{
			t = t.getParent();
		}
		return t;
	}
	
	

	
}
