/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

// Source file: d:\\webactiv\\util\\com\\stratelia\\webactiv\\util\\score\\model\\ScorePK.java

package com.stratelia.webactiv.util.score.model;

import com.stratelia.webactiv.util.*;
import java.io.Serializable;

/*
 * CVS Informations
 * 
 * $Id: ScorePK.java,v 1.1.1.1 2002/08/06 14:47:53 nchaix Exp $
 * 
 * $Log: ScorePK.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:53  nchaix
 * no message
 *
 * Revision 1.4  2001/12/21 13:51:47  scotte
 * no message
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class ScorePK extends WAPrimaryKey implements Serializable
{

    /**
     * Constructor which set only the id
     * @since 1.0
     * @roseuid 3AB7343503E1
     */
    public ScorePK(String id)
    {
        super(id);
    }

    /**
     * Constructor which set the id
     * The WAPrimaryKey provides space and component name
     * @since 1.0
     * @roseuid 3AB734360003
     */
    public ScorePK(String id, String spaceId, String componentId)
    {
        super(id, spaceId, componentId);
    }

    /**
     * Constructor which set the id
     * The WAPrimaryKey provides space and component name
     * @since 1.0
     * @roseuid 3AB734360018
     */
    public ScorePK(String id, WAPrimaryKey pk)
    {
        super(id, pk);
    }

    /**
     * Return the object root table name
     * @return the root table name of the object
     * @since 1.0
     * @roseuid 3AB73436002B
     */
    public String getRootTableName()
    {
        return "Score";
    }

    /**
     * Return the object table name
     * @return the table name of the object
     * @since 1.0
     * @roseuid 3AB73436002B
     */
    public String getTableName()
    {
        return "SB_Question_Score";
    }

    /**
     * Check if an another object is equal to this object
     * @return true if other is equals to this object
     * @param other the object to compare to this PollPK
     * @since 1.0
     * @roseuid 3AB73436002C
     */
    public boolean equals(Object other)
    {
        if (!(other instanceof ScorePK))
        {
            return false;
        }
        return (id.equals(((ScorePK) other).getId())) && (space.equals(((ScorePK) other).getSpace())) && (componentName.equals(((ScorePK) other).getComponentName()));
    }

    /**
     * Returns a hash code for the key
     * @return A hash code for this object
     * @roseuid 3AB734360036
     */
    public int hashCode()
    {
        return toString().hashCode();
    }

}


