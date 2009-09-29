package com.stratelia.webactiv.util.publication.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class PublicationPK extends WAPrimaryKey implements Serializable {

  // for flat pk design pattern
  public transient PublicationDetail pubDetail = null;

  /**
   * Constructor which set only the id
   * 
   * @since 1.0
   */
  public PublicationPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public PublicationPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public PublicationPK(String id, String componentId) {
    super(id, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public PublicationPK(String id, WAPrimaryKey pk) {
    super(id, pk.getSpace(), pk.getInstanceId());
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   */
  public String getRootTableName() {
    return "Publication";
  }

  /**
   * Return the object table name
   * 
   * @return the table name of the object
   * @since 1.0
   */
  public String getTableName() {
    return "SB_Publication_Publi";
  }

  /**
   * Check if an another object is equal to this object
   * 
   * @param other
   *          the object to compare to this PublicationPK
   * @return true if other is equals to this object
   */
  public boolean equals(Object other) {
    return ((other instanceof PublicationPK)
        && (id.equals(((PublicationPK) other).getId())) && (componentName
        .equals(((PublicationPK) other).getComponentName())));
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   */
  public int hashCode() {
    return this.id.hashCode() ^ this.componentName.hashCode();
    // return toString().hashCode();
  }
}