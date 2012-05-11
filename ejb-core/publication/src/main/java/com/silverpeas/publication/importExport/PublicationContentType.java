/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.publication.importExport;

import com.silverpeas.wysiwyg.importExport.WysiwygContentType;

/**
 * @author tleroi To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
   * @param wysiwygContentType The wysiwygContentType to set.
   */
  public void setWysiwygContentType(WysiwygContentType wysiwygContentType) {
    this.wysiwygContentType = wysiwygContentType;
  }
}