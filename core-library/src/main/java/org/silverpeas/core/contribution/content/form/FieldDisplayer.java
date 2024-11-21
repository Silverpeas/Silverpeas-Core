/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A FieldDisplayer is an object which can display in HTML the content of a field to a end user and
 * can retrieve via HTTP any updated value. A FieldDisplayer can only manage fields of a specific
 * type. The links between Fields and FieldDisplayers are managed by a Field. FieldDisplayers are
 * grouped in Form in order to display each fields of a DataRecord.
 *
 * @param <T> the type of field.
 * @see Field
 * @see Form
 */
public interface FieldDisplayer<T extends Field> {

  /**
   * Prints the javascript which will be used to control the new value given to the named field. The
   * error messages may be adapted to a local language. The Field gives the field type and
   * constraints. The Field gives the local label too. Never throws an Exception but log a
   * message and writes an empty string when the fieldName is unknown by the
   * template or when the field type is not a managed type.
   *
   * @throws IOException if the rendering fails
   */
  void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException;

  /**
   * Prints the HTML value of the field. The value format may be adapted to a local language. The
   * fieldName must be used to name the html form input. Never throws an Exception but log the error
   * and writes an empty string when the field type is not of a managed type.
   *
   * @throws FormException if the rendering fails.
   */
  void display(PrintWriter out, T field, FieldTemplate template, PagesContext pagesContext)
      throws FormException;

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request. @throw FormException if the field type is not a managed type. @throw FormException
   * if the field doesn't accept the new value.
   *
   * @throws FormException if the rendering fails.
   */
  List<String> update(List<FileItem> items, T field, FieldTemplate template,
      PagesContext pagesContext) throws FormException;

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request. @throw FormException if the field type is not a managed type. @throw FormException
   * if the field doesn't accept the new value.
   *
   * @throws FormException if the rendering fails.
   */
  List<String> update(String value, T field, FieldTemplate template,
      PagesContext pagesContext) throws FormException;

  /**
   * Is the field mandatory? In this case, and for a field edition, the mandatory icon should be
   * displayed for the field.
   * @return true if the field is mandatory in the form.
   */
  boolean isDisplayedMandatory();

  /**
   * Return the number of HTML Objects displayed by the displayer.
   *
   * @return the number of HTML Objects displayed by the displayer.
   */
  int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext);

  /**
   * Add the content of the field to the index entry
   */
  void index(FullIndexEntry indexEntry, String key, String fieldName, T field,
      String language, boolean stored);
}
