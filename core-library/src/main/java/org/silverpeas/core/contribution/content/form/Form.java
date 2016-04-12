/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.contribution.content.form;

import org.apache.commons.fileupload.FileItem;

import javax.servlet.jsp.JspWriter;
import java.util.List;

/**
 * A Form is an object which can display in HTML the content of a DataRecord to a end user and can
 * retrieve via HTTP any updated values.
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public interface Form {
  /**
   * Prints the javascripts which will be used to control the new values given to the data record
   * fields. The error messages may be adapted to a local language. The RecordTemplate gives the
   * field type and constraints. The RecordTemplate gives the local label too. Never throws an
   * Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>a field is unknown by the template.</li>
   * <li>a field has not the required type.</li>
   * </ul>
   */
  public void displayScripts(JspWriter out, PagesContext pagesContext);

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate to extract labels and extra
   * informations. The value formats may be adapted to a local language. Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>a field is unknown by the template.</li>
   * <li>a field has not the required type.</li>
   * </ul>
   */
  public void display(JspWriter out, PagesContext pagesContext,
      DataRecord record) throws FormException;

  public void display(JspWriter out, PagesContext pagesContext) throws FormException;

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request. this method treats only wysiwyg fields.
   * @throws FormException if the field type is not a managed type or if the field doesn't accept
   * the new value.
   */
  public List<String> updateWysiwyg(List<FileItem> items,
      DataRecord record, PagesContext pagesContext)
      throws FormException;

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throws FormException if the field type is not a managed type or if the field doesn't accept
   * the new value.
   */
  public List<String> update(List<FileItem> items,
      DataRecord record, PagesContext pagesContext)
      throws FormException;

  /**
   * Updates the values of the dataRecord using the RecordTemplate to extra control information
   * (readOnly or mandatory status). The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throws FormException if the field type is not a managed type or  if the field doesn't
   * accept the new value.
   */
  public List<String> update(List<FileItem> items,
      DataRecord record, PagesContext pagesContext, boolean updateWysiwyg)
      throws FormException;

  /**
   * Get the form title
   */
  public String getTitle();

  public String toString(PagesContext pagesContext, DataRecord record);

  public boolean isEmpty(List<FileItem> items, DataRecord record, PagesContext pagesContext);

  public void setFormName(String name);

  public void setData(DataRecord data);

  /**
   * Defines if this form is a 'view' form (opposite to an 'update' form)
   * A 'view' form may have some specific behaviors like to not display empty fields
   * @param viewForm true if this form is a 'view' form
   */
  public void setViewForm(boolean viewForm);
}
