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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.publicationTemplate;

import com.silverpeas.form.Form;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;

/**
 * A PublicationTemplate describes a set of publication records built on a same template. A
 * PublicationTemplate groups :
 * <OL>
 * <LI>a RecordTemplate which describes the built records.
 * <LI>a RecordSet of records built on this template,
 * <LI>an update Form used to create and update the publication items
 * <LI>a view Form used to show the publications.
 * </OL>
 */
public interface PublicationTemplate {

  public static final String DEFAULT_THUMBNAIL = "/weblib/xmlForms/model1.gif";

  /**
   * Returns the RecordTemplate of the publication data item.
   */
  public RecordTemplate getRecordTemplate() throws PublicationTemplateException;

  /**
   * Returns the RecordSet of all the records built from this template.
   */
  public RecordSet getRecordSet() throws PublicationTemplateException;

  /**
   * Returns the Form used to create and update the records built from this template.
   */
  public Form getUpdateForm() throws PublicationTemplateException;

  /**
   * Returns the Form used to view the records built from this template.
   */
  public Form getViewForm() throws PublicationTemplateException;

  /**
   * Returns the Form used to search the records built from this template.
   */
  public Form getSearchForm() throws PublicationTemplateException;

  public void setExternalId(String externalId);

  public Form getEditForm(String name) throws PublicationTemplateException;

  public String getExternalId();

  public String getName();

  public String getDescription();

  public String getThumbnail();

  public String getFileName();

  public boolean isVisible();

  public boolean isSearchable();
}
