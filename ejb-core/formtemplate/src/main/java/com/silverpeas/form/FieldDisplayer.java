package com.silverpeas.form;

import java.io.PrintWriter;

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
public interface FieldDisplayer
{
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
  public void display(PrintWriter   out,
                      Field         field,
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
  /*public void update(HttpServletRequest request,
                     Field              field,
				     FieldTemplate      template,
				     PagesContext PagesContext)
     throws FormException;*/
	public void update(String value,
						 Field              field,
						 FieldTemplate      template,
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
}
