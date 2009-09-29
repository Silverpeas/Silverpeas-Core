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
package com.silverpeas.form;

import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.fileupload.FileItem;

/**
 * A FieldDisplayer is an object which can display in HTML
 * the content of a field to a end user
 * and can retrieve via HTTP any updated value.
 * 
 * A FieldDisplayer can only manage fields of a specific type.
 * The links between Fields and FieldDisplayers are managed by a FormField.
 * 
 * FieldDisplayers are grouped in Form in order to display each fields of
 * a DataRecord.
 *
 * @see Field
 * @see FormField
 * @see Form
 */
public interface FieldDisplayer {

  /**
   * Prints the javascripts which will be used to control
   * the new value given to the named field.
   *
   * The error messages may be adapted to a local language.
   * The FormField gives the field type and constraints.
   * The FormField gives the local labeld too.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> the fieldName is unknown by the template.
   * <LI> the field type is not a managed type.
   * </UL>
   */
  public void displayScripts(PrintWriter out,
      FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException;

  /**
   * Prints the HTML value of the field.
   *
   * The value format may be adapted to a local language.
   * The fieldName must be used to name the html form input.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> the field type is not a managed type.
   * </UL>
   */
  public void display(PrintWriter out,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext) throws FormException;

  /**
   * Updates the value of the field.
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public List<String> update(List<FileItem> items,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext)
      throws FormException;

  /**
   * Updates the value of the field.
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public List<String> update(String value,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext)
      throws FormException;

  /*
   * Indique si le champ affiché autorise l'affichage de la notion d'obligation de saisie du champs
   */
  public boolean isDisplayedMandatory();

  /**
   * retourne le nombre d'objets html affiché par le displayer
   */
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext);

  /**
   * 
   * Add the content of the field to the index entry
   * 
   * @param indexEntry
   * @param key
   * @param fieldName 
   * @param field
   * @param language
   */
  public void index(FullIndexEntry indexEntry, String key, String fieldName, Field field, String language);
}
