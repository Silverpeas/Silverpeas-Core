package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class InfoImagePK extends WAPrimaryKey implements Serializable{
  
  public InfoImagePK(String id) {
    super(id);
  }
  
  public InfoImagePK(String id,String space,String componentName) {
    super(id, space, componentName);
  }
  
  public InfoImagePK(String id,WAPrimaryKey pk) {
    super(id, pk);
  }
  
  public String getRootTableName()
  {
    return "InfoImage";
  }
  
  public String getTableName(){
    return "SB_Publication_InfoImage";
  }
  
  public boolean equals(Object other)
  {
    if (!(other instanceof InfoImagePK)) return false;
    return (id.equals( ((InfoImagePK) other).getId()) ) &&
       (space.equals(((InfoImagePK) other).getSpace()) ) &&
       (componentName.equals(((InfoImagePK) other).getComponentName()) );
  }
  
  public String toString() {
	return "InfoImagePK(id = " + getId() + ", space = " + getSpace() + 
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