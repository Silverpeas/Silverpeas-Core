/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.coordinates.model;

import com.stratelia.webactiv.util.*;
import java.io.Serializable;

/**
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CoordinatePK extends WAPrimaryKey implements Serializable
{

	/**
	 * Constructor declaration
	 *
	 *
	 * @param id
	 *
	 * @see
	 */
	public CoordinatePK(String id)
	{
		super(id);
	}

	/**
	 * Constructor declaration
	 *
	 *
	 * @param id
	 * @param spaceId
	 * @param componentId
	 *
	 * @see
	 */
	public CoordinatePK(String id, String spaceId, String componentId)
	{
		super(id, spaceId, componentId);
	}

	/**
	 * Constructor declaration
	 *
	 *
	 * @param id
	 * @param pk
	 *
	 * @see
	 */
	public CoordinatePK(String id, WAPrimaryKey pk)
	{
		super(id, pk);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getRootTableName()
	{
		return "Coordinates";
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getTableName()
	{
		return "SB_Coordinates_Coordinates";
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param other
	 *
	 * @return
	 *
	 * @see
	 */
	public boolean equals(Object other)
	{
		if (!(other instanceof CoordinatePK))
		{
			return false;
		}
		return (id.equals(((CoordinatePK) other).getId())) && (space.equals(((CoordinatePK) other).getSpace())) && (componentName.equals(((CoordinatePK) other).getComponentName()));
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public int hashCode()
	{
		return toString().hashCode();
	}

}
