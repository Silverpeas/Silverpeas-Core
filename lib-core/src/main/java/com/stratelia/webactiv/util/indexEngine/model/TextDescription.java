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

package com.stratelia.webactiv.util.indexEngine.model;

import com.silverpeas.util.i18n.I18NHelper;

import java.io.Serializable;

/**
 * A ContentDescription pack all the needed information to parse and index a content. We need :
 * <UL>
 * <LI>the content itself</LI>
 * <LI>the language of the file</LI>
 * </UL>
 */
public class TextDescription implements Serializable {

  private static final long serialVersionUID = -6937724257200011564L;

  public TextDescription(String content, String lang) {
    this.content = content;
    this.lang = I18NHelper.checkLanguage(lang);
  }

  /**
   * Return the content itself
   */
  public String getContent() {
    return content;
  }

  /**
   * Return the content language
   */
  public String getLang() {
    return lang;
  }

  /**
   * All the attributes are private and final.
   */
  private final String content;
  private final String lang;
}
