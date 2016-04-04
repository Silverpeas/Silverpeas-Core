/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/**
 * Titre : Silverpeas<p>
 * Description : This object provides the function of files attached<p>
 * Copyright : Copyright (c) Jean-Claude Groccia<p>
 * Société : Stratelia<p>
 * @author author Publication and Jean-Claude Groccia
 * @version 1.0
 */
package org.silverpeas.core.importexport.attachment;

import org.silverpeas.core.WAPrimaryKey;

import java.io.Serializable;

/*
 * CVS Informations
 *
 * $Id: AttachmentPK.java,v 1.3 2008/05/20 13:19:47 neysseri Exp $
 *
 * $Log: AttachmentPK.java,v $
 * Revision 1.3  2008/05/20 13:19:47  neysseri
 * no message
 *
 * Revision 1.2.20.1  2008/05/06 09:28:00  ehugonnet
 * Gestion via webdav des attachments pour l'edition en ligne
 *
 * Revision 1.2  2006/04/13 13:24:16  neysseri
 * no message
 *
 * Revision 1.1  2003/09/17 09:18:21  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:47:40  nchaix
 * no message
 *
 * Revision 1.3  2001/12/31 15:43:44  groccia
 * stabilisation
 *
 */

/**
 * Class declaration
 * @author
 */
public class AttachmentPK extends WAPrimaryKey implements Serializable {
  /**
   * Constructor declaration
   * @param id
   * @see
   */
  public AttachmentPK(String id) {
    super(id);
  }

  /**
   * Constructor declaration
   * @param id
   * @param spaceId
   * @param componentId
   * @see
   */
  public AttachmentPK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  public AttachmentPK(String id, String componentId) {
    super(id, null, componentId);
  }

  /**
   * Constructor declaration
   * @param id
   * @param pk
   * @see
   */
  public AttachmentPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * **********
   */

  public String getRootTableName() {
    return "Attachment";
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTableName() {
    return "SB_Attachment_Attachment";
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof AttachmentPK)) {
      return false;
    }
    return (id.equals(((AttachmentPK) other).getId()))
        && (componentName.equals(((AttachmentPK) other).getComponentName()));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(id = " + getId() + ", componentName = " + getComponentName() + ")";
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }

}