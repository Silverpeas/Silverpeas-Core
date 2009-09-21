package com.silverpeas.form;

import java.util.List;

import javax.servlet.jsp.JspWriter;

/**
 * A Form is an object which can display in HTML
 * the content of a DataRecord to a end user
 * and can retrieve via HTTP any updated values.
 * 
 * @see DataRecord
 * @see RecordTemplate
 * @see FieldDisplayer
 */
public interface Form
{
  /**
   * Prints the javascripts which will be used to control
   * the new values given to the data record fields.
   *
   * The error messages may be adapted to a local language.
   * The RecordTemplate gives the field type and constraints.
   * The RecordTemplate gives the local label too.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> a field is unknown by the template.
   * <LI> a field has not the required type.
   * </UL>
   */
  public void displayScripts(JspWriter out, PagesContext PagesContext);

  /**
   * Prints the HTML layout of the dataRecord using the RecordTemplate
   * to extract labels and extra informations.
   *
   * The value formats may be adapted to a local language.
   *
   * Never throws an Exception
   * but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI> a field is unknown by the template.
   * <LI> a field has not the required type.
   * </UL>
   */
  public void display(JspWriter out, PagesContext PagesContext, 
                      DataRecord record) throws FormException;

  /**
   * Updates the values of the dataRecord using the RecordTemplate
   * to extra control information (readOnly or mandatory status).
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public void update(List items,
                     DataRecord record, PagesContext PagesContext)
     throws FormException;

	/**
	 * Get the form title
	 */
	public String getTitle();
	
	public String toString(PagesContext pagesContext, DataRecord record);
	
	public boolean isEmpty(List items, DataRecord record, PagesContext pagesContext);
}
