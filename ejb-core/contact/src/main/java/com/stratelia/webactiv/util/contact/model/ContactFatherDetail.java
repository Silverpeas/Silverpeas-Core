package com.stratelia.webactiv.util.contact.model;

import java.io.Serializable;

/**
 * This object contains the description of a contact and a node (contact parameter, model detail, info)
 * @author SC
 * @version 1.0
 */
public class ContactFatherDetail implements Serializable {
  
  private ContactDetail pubDetail;
  private String nodeId;
  private String nodeName;
  
   /**
	* Create a new ContactFatherDetail
	* @param contactDetail
	* @param nodeId
	* @param nodeName
	*/
  public ContactFatherDetail(ContactDetail pubDetail, String nodeId, String nodeName) {
    this.pubDetail = pubDetail;
    this.nodeId = nodeId;
    this.nodeName = nodeName;
  }
    
    /**
	* Get the contact parameters
	* @return a ContactDetail - the contact parameters
	* @see com.stratelia.webactiv.util.contact.model.PulicationDetail
	* @since 1.0
	*/	
  public ContactDetail getContactDetail(){
    return pubDetail;
  }
  
  public String getNodeId() {
    return nodeId;
  }
  
  public String getNodeName() {
    return nodeName;
  }
}