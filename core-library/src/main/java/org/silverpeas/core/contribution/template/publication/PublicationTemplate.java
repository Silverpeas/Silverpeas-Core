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

package org.silverpeas.core.contribution.template.publication;

import java.util.List;

import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.form.XmlForm;

/**
 * A PublicationTemplate describes a set of publication records built on a same template. A
 * PublicationTemplate groups :
 * <ol>
 * <li>a RecordTemplate which describes the built records.</li>
 * <li>a RecordSet of records built on this template,</li>
 * <li>an update Form used to create and update the publication items</li>
 * <li>a view Form used to show the publications.</li>
 * <li>a search form used to search the publication records.</li>
 * <li>a result search view form to add publication records inside search result display</li>
 * </ol>
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
   * Returns the XML Form used to create and update the records built from this template.
   * Even if an HTML layer exists (HTMLForm), the XML one is returned.
   * @return the XMLForm
   * @throws PublicationTemplateException
   */
  public XmlForm getUpdateFormAsXMLOne() throws PublicationTemplateException;

  /**
   * Returns the Form used to view the records built from this template.
   */
  public Form getViewForm() throws PublicationTemplateException;

  /**
   * Returns the Form used to search the records built from this template.
   */
  public Form getSearchForm() throws PublicationTemplateException;

  public void setExternalId(String externalId);

  public String getExternalId();

  public String getName();

  public String getDescription();

  public String getThumbnail();

  public String getFileName();

  /**
   * Is this publication template is visible to others?
   * @return true if it is visible, false otherwise.
   */
  public boolean isVisible();

  /**
   * Is the publication records described by this template can be searchable?
   * @return true if the records are searchable, false othersise.
   */
  public boolean isSearchable();

  /**
   * Returns the Form used to view the search result records built from this template.
   */
  public Form getSearchResultForm() throws PublicationTemplateException;

  /**
   * Returns all field names which can generate a search facet
   * @return a List of field name
   */
  public List<String> getFieldsForFacets();

  public boolean isRestrictedVisibility();

  public List<String> getSpaces();

  public boolean isRestrictedVisibilityToSpace();

  public List<String> getApplications();

  public boolean isRestrictedVisibilityToApplication();

  public List<String> getInstances();

  public boolean isRestrictedVisibilityToInstance();

  public boolean isDataEncrypted();

  public boolean isViewLayerExist();

  public boolean isUpdateLayerExist();

}
