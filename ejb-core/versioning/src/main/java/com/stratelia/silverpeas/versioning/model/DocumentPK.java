package com.stratelia.silverpeas.versioning.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This object contains the info about PrimaryKey of document
 * @author Georgy Shakirin
 * @version 1.0
 */

public class DocumentPK extends WAPrimaryKey implements Serializable, Cloneable {

    /**
     * Constructor declaration
     *
     *
     * @param id
     *
     * @see
     */
    public DocumentPK(int id) {
        super(String.valueOf(id));
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
    public DocumentPK(int id, String spaceId, String componentId) {
        super(String.valueOf(id), spaceId, componentId);
    }

    /**
     * Constructor declaration
     *
     *
     * @param id
     * @param componentId
     *
     * @see
     */
    public DocumentPK(int id, String componentId) {
        super(String.valueOf(id), componentId);
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
    public DocumentPK(int id, WAPrimaryKey pk) {
        super(String.valueOf(id), pk);
    }

    /**
     * **********
     */


    public String getRootTableName() {
        return "Version";
    }


    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getTableName() {
        return "SB_Version_Document";
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
    public boolean equals(Object other) {
        if (!(other instanceof DocumentPK)) {
            return false;
        }
        return (id.equals(((DocumentPK) other).getId())) && (space.equals(((DocumentPK) other).getSpace())) && (componentName.equals(((DocumentPK) other).getComponentName()));
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String toString() {
        return "(id = " + getId() + ", space = " + getSpace() + ", componentName = " + getComponentName() + ")";
    }

    /**
     *
     * Returns a hash code for the key
     * @return A hash code for this object
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Support Cloneable Interface
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // this should never happened
        }
    }


}