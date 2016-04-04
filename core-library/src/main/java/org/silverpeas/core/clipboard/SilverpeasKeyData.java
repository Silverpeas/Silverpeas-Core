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

package org.silverpeas.core.clipboard;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

/**
 * Class declaration
 * @author
 */
public class SilverpeasKeyData implements Serializable {

  private static final long serialVersionUID = 6975015837634530711L;
  final static String kTitleKEY = "TITLE";
  final static String kAuthorKEY = "AUTHOR";
  final static String kDescKEY = "DESC";
  final static String kHTMLKEY = "HTML"; // rendu HTML de l'object
  final static String kTextKEY = "TEXT"; // rendu HTML de l'object

  private Date m_CreationDate = null;
  private Properties m_KeyData = null;

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public SilverpeasKeyData() {
    m_KeyData = new Properties();
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setTitle(String Title) {
    m_KeyData.setProperty(kTitleKEY, Title);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setAuthor(String Author) {
    m_KeyData.setProperty(kAuthorKEY, Author);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setCreationDate(Date date) {
    m_CreationDate = date;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setDesc(String Desc) {
    m_KeyData.setProperty(kDescKEY, Desc);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setText(String Text) {
    m_KeyData.setProperty(kTextKEY, Text);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void setProperty(String key, String value) throws SKDException {
    if (m_KeyData.containsKey(key)) {
      throw new SKDException("SilverpeasKeyData.setProperty()",
          SKDException.ERROR, "root.EX_CLIPBOARD_COPY_FAILED",
          "Key still used (" + key + ")");
    } else {
      m_KeyData.setProperty(key, value);
    }
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public String getTitle() {
    return m_KeyData.getProperty(kTitleKEY);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public String getAuthor() {
    return m_KeyData.getProperty(kAuthorKEY);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public Date getCreationDate() {
    return m_CreationDate;
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public String getDesc() {
    return m_KeyData.getProperty(kDescKEY);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public String getText() {
    return m_KeyData.getProperty(kTextKEY);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public String getProperty(String key) {
    return m_KeyData.getProperty(key);
  }

  /**
   * --------------------------------------------------------------------------
   * ------------------------------
   */
  public void toDOM() {
  }

}
