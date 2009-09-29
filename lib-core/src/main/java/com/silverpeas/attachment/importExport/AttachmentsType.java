/*
 * Created on 24 janv. 2005
 */
package com.silverpeas.attachment.importExport;

import java.util.List;

/**
 * Classe utilisée pour le (un)marshalling Castor
 * 
 * @author sdevolder
 */
public class AttachmentsType {

  private List listAttachmentDetail;// AttachmentDetail

  /**
   * @return
   */
  public List getListAttachmentDetail() {
    return listAttachmentDetail;
  }

  /**
   * @param list
   */
  public void setListAttachmentDetail(List list) {
    listAttachmentDetail = list;
  }

}
