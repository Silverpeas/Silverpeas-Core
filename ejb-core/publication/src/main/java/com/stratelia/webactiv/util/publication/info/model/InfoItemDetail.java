/** 
 *
 * @author  akhadrou
 * @version 
 */
package com.stratelia.webactiv.util.publication.info.model;
 
import java.io.Serializable;

public abstract class InfoItemDetail implements Serializable {
 
  private InfoPK pk;
  private String order = null;
  private String id = null;
  
  public InfoItemDetail(InfoPK pk, String order, String id) {
	    setPK(pk);
            setOrder(order);
            setId(id);
    }

  // set get on attributes        
  //id
  public InfoPK getPK() {
        return pk;
  }
		
  public void setPK(InfoPK pk) {
        this.pk = pk;
  }
  
  public String getOrder() {
    return order;
  }
  
  public void setOrder(String order) {
    this.order = order;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  
  public String toString() {
	return "(pk = " + getPK() + ", order = " + getOrder() + ")";
  }
  
}