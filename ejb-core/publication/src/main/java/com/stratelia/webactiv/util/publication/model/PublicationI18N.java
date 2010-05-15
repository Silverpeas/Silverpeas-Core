/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.publication.model;

import java.io.Serializable;

import com.silverpeas.util.i18n.Translation;

public class PublicationI18N extends Translation implements Serializable {

  private static final long serialVersionUID = -3608883875752659027L;

  private String name = null;
  private String description = null;
  private String keywords = null;

  public PublicationI18N() {
  }

  public PublicationI18N(PublicationDetail publi) {
    if (publi.getLanguage() != null)
      super.setLanguage(publi.getLanguage());

    this.name = publi.getName();
    this.description = publi.getDescription();
    this.keywords = publi.getKeywords();

    if (publi.getTranslationId() != null)
      super.setId(Integer.parseInt(publi.getTranslationId()));
    super.setObjectId(publi.getPK().getId());
  }

  public PublicationI18N(String lang, String name, String description,
      String keywords) {
    if (lang != null)
      super.setLanguage(lang);
    this.name = name;
    this.description = description;
    this.keywords = keywords;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

}
