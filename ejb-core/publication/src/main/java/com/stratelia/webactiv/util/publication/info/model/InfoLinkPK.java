package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class InfoLinkPK extends WAPrimaryKey implements Serializable{
  
  public InfoLinkPK(String id) {
    super(id);
  }
  
  public InfoLinkPK(String id,String space,String componentName) {
    super(id, space, componentName);
  }
  
  public InfoLinkPK(String id,WAPrimaryKey pk) {
    super(id, pk);
  }
  
  public String getRootTableName()
  {
    return "InfoLink";
  }
  
  public String getTableName(){
    return "SB_Publication_InfoLink";
  }
  
  public boolean equals(Object other)
  {
    if (!(other instanceof InfoLinkPK)) return false;
    return (id.equals( ((InfoLinkPK) other).getId()) ) &&
       (space.equals(((InfoLinkPK) other).getSpace()) ) &&
       (componentName.equals(((InfoLinkPK) other).getComponentName()) );
  }
  
  public String toString() {
	return "(id = " + getId() + ", space = " + getSpace() + 
            ", componentName = " + getComponentName() + ")";
  }
  
  /**
  *
  * Returns a hash code for the key
  *
  * @return A hash code for this object
  */
  public int hashCode() {
	return toString().hashCode();
}
  
}