/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.publication.importExport;

import com.silverpeas.wysiwyg.importExport.WysiwygContentType;

/**
 * @author tleroi
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PublicationContentType {

  private DBModelContentType dBModelContentType;
  private WysiwygContentType wysiwygContentType;
  private XMLModelContentType xmlModelContentType;

  public XMLModelContentType getXMLModelContentType() {
    return xmlModelContentType;
  }

  public void setXMLModelContentType(XMLModelContentType xmlModelContentType) {
    this.xmlModelContentType = xmlModelContentType;
  }

  /**
   * @return
   */
  public DBModelContentType getDBModelContentType() {
    return dBModelContentType;
  }

  /**
   * @param type
   */
  public void setDBModelContentType(DBModelContentType type) {
    dBModelContentType = type;
  }

  /**
   * @return Returns the wysiwygContentType.
   */
  public WysiwygContentType getWysiwygContentType() {
    return wysiwygContentType;
  }

  /**
   * @param wysiwygContentType
   *          The wysiwygContentType to set.
   */
  public void setWysiwygContentType(WysiwygContentType wysiwygContentType) {
    this.wysiwygContentType = wysiwygContentType;
  }
}