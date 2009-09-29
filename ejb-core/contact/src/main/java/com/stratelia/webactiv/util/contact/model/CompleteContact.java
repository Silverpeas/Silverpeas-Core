package com.stratelia.webactiv.util.contact.model;

import java.io.Serializable;

/**
 * This object contains the description of a complete contact (contact
 * parameter, model detail, info)
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CompleteContact implements Serializable {

  private ContactDetail pubDetail;
  private String modelId;

  /**
   * Create a new CompleteContact
   * 
   * @param pubDetail
   * @param modelId
   * @see com.stratelia.webactiv.util.contact.model.PulicationDetail
   * @since 1.0
   */
  public CompleteContact(ContactDetail pubDetail, String modelId) {
    this.pubDetail = pubDetail;
    this.modelId = modelId;
  }

  /**
   * Get the contact parameters
   * 
   * @return a ContactDetail - the contact parameters
   * @see com.stratelia.webactiv.util.contact.model.PulicationDetail
   * @since 1.0
   */
  public ContactDetail getContactDetail() {
    return pubDetail;
  }

  /**
   * @return
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * @param modelId
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

}