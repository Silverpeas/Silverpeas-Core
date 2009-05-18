package com.stratelia.silverpeas.comment.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This object contains the info about PrimaryKey of document
 * @author Georgy Shakirin
 * @version 1.0
 */

public class CommentPK extends WAPrimaryKey implements Serializable{

  /**
 * Constructor declaration
 *
 *
 * @param id
 *
 * @see
 */
public CommentPK(String id)
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
public CommentPK(String id, String spaceId, String componentId)
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
public CommentPK(String id, WAPrimaryKey pk)
{
    super(id, pk);
}

/**
 * **********
 */


public String getRootTableName()
{
    return "Comment";
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
    return "SB_Comment_Comment";
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
    if (!(other instanceof CommentPK))
    {
        return false;
    }
    return (id.equals(((CommentPK) other).getId())) && (space.equals(((CommentPK) other).getSpace())) && (componentName.equals(((CommentPK) other).getComponentName()));
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
    return "(id = " + getId() + ", space = " + getSpace() + ", componentName = " + getComponentName() + ")";
}

/**
 *
 * Returns a hash code for the key
 * @return A hash code for this object
 */
public int hashCode()
{
    return toString().hashCode();
}

}