/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.coordinates.model;

import java.io.Serializable;

/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class CoordinatePoint implements Serializable
{

	private int		coordinateId;
	private int		nodeId;
	private boolean leaf;
	private String  name;
	private int		level;
	private int		displayOrder = 0;
	private String	path;

	/**
	 * Empty Constructor needed for mapping Castor
	 *
	 */
	public CoordinatePoint()
	{}

	/**
	 * Constructor declaration
	 *
	 *
	 * @param coordinateId
	 * @param nodeId
	 * @param leaf
	 *
	 * @see
	 */
	public CoordinatePoint(int coordinateId, int nodeId, boolean leaf)
	{
		this.coordinateId = coordinateId;
		this.nodeId = nodeId;
		this.leaf = leaf;
		this.level = 0;
	}

	/**
	 * Constructor declaration
	 *
	 *
	 * @param coordinateId
	 * @param nodeId
	 * @param leaf
	 * @param level
	 *
	 * @see
	 */
	public CoordinatePoint(int coordinateId, int nodeId, boolean leaf, int level)
	{
		this.coordinateId = coordinateId;
		this.nodeId = nodeId;
		this.leaf = leaf;
		this.level = level;
	}

	/**
	 * Constructor declaration
	 *
	 *
	 * @param coordinateId
	 * @param nodeId
	 * @param leaf
	 * @param level
	 * @param displayOrder
	 *
	 * @see
	 */
	public CoordinatePoint(int coordinateId, int nodeId, boolean leaf, int level, int displayOrder)
	{
		this.coordinateId = coordinateId;
		this.nodeId = nodeId;
		this.leaf = leaf;
		this.level = level;
		this.displayOrder = displayOrder;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public int getCoordinateId()
	{
		return this.coordinateId;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public int getNodeId()
	{
		return this.nodeId;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public boolean isLeaf()
	{
		return this.leaf;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public int getLevel()
	{
		return this.level;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public int getOrder()
	{
		return this.displayOrder;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param coordinateId
	 *
	 * @see
	 */
	public void setCoordinateId(int coordinateId)
	{
		this.coordinateId = coordinateId;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param nodeId
	 *
	 * @see
	 */
	public void setNodeId(int nodeId)
	{
		this.nodeId = nodeId;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param leaf
	 *
	 * @see
	 */
	public void setLeaf(boolean leaf)
	{
		this.leaf = leaf;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param name
	 *
	 * @see
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param level
	 *
	 * @see
	 */
	public void setLevel(int level)
	{
		this.level = level;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param order
	 *
	 * @see
	 */
	public void setOrder(int order)
	{
		this.displayOrder = order;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String toString()
	{
		String result = "CoordinatePoint {" + "\n";

		result = result + "  getCoordinateId() = " + getCoordinateId() + "\n";
		result = result + "  getNodeId() = " + getNodeId() + "\n";
		result = result + "  isLeaf() = " + isLeaf() + "\n";
		result = result + "  getName() = " + getName() + "\n";
		result = result + "  getLevel() = " + getLevel() + "\n";
		result = result + "}";
		return result;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}