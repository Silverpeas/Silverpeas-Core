package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A CheckBoxDisplayer is an object which can display a checkbox in HTML
 * the content of a checkbox to a end user
 * and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class CheckBoxDisplayer implements FieldDisplayer
{

	/**
	 * Constructeur
	 */  
	public CheckBoxDisplayer() 
	{
	}
   
  
	/**
	 * Returns the name of the managed types.
	 */
	public String[] getManagedTypes() 
	{
		String [] s = new String[0];
		s[0] = TextField.TYPE;
		return s;
	}

	/**
	 * Prints the javascripts which will be used to control
	 * the new value given to the named field.
	 *
	 * The error messages may be adapted to a local language.
	 * The FieldTemplate gives the field type and constraints.
	 * The FieldTemplate gives the local labeld too.
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
								PagesContext pagesContext)  throws java.io.IOException 
	{ 
		String language = pagesContext.getLanguage();
		
		if (! template.getTypeName().equals(TextField.TYPE))
			SilverTrace.info("form", "CheckBoxDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
		
		if (template.isMandatory() && pagesContext.useMandatory()) 
		{
	    	int currentIndex = new Integer(pagesContext.getCurrentFieldIndex()).intValue();
	 		int fin = currentIndex + getNbHtmlObjectsDisplayed(template, pagesContext);
	    	out.println("	var checked = false;\n");
	    	out.println("	for (var i = "+currentIndex+"; i < "+fin+"; i++) {\n"); 
	 		out.println("		if (document.forms["+pagesContext.getFormIndex()+"].elements[i].checked) {\n");
	 		out.println("			checked = true;\n");
	 		out.println("		}\n");
	 		out.println("	}\n");
	 		out.println("	if(checked == false) {\n");
	 		out.println("		errorMsg+=\"  - '"+template.getLabel(language)+"' "+Util.getString("GML.MustBeFilled", language)+"\\n \";");
			out.println("		errorNb++;");
			out.println("	}");
		}
		
		Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
	}

	/**
	 * Prints the HTML value of the field.
	 * The displayed value must be updatable by the end user.
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
                      PagesContext PagesContext) throws FormException 
	{
		String selectedValues = "";
		ArrayList valuesFromDB = new ArrayList();
		String keys = "";
		String values = "";
		String html = "";
		int cols = 1;
		String language = PagesContext.getLanguage();

		String mandatoryImg = Util.getIcon("mandatoryField");

		String fieldName = template.getFieldName();
		Map parameters = template.getParameters(language);
			
		if (! field.getTypeName().equals(TextField.TYPE))
			SilverTrace.info("form", "CheckBoxDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
			
		if (! field.isNull())
			selectedValues = field.getValue(language);
			
		StringTokenizer st = new StringTokenizer(selectedValues,"##");
		while (st.hasMoreTokens()){
			valuesFromDB.add(st.nextToken());
		}
			
		if (parameters.containsKey("keys")) 
				keys = (String) parameters.get("keys");
			
		if (parameters.containsKey("values")) 
				values = (String) parameters.get("values");

		try
		{
			if (parameters.containsKey("cols")) 
				cols = ( Integer.valueOf( (String) parameters.get("cols") ) ).intValue();
		}
		catch (NumberFormatException nfe)
		{
			SilverTrace.error("form", "CheckBoxDisplayer.display", "form.EX_ERR_ILLEGAL_PARAMETER_COL", (String) parameters.get("cols"));
			cols = 1;
		}

		// if either keys or values is not filled
		// take the same for keys and values
		if (keys.equals("") && !values.equals(""))
				keys = values;
		if (values.equals("") && !keys.equals(""))
				values = keys;

		StringTokenizer stKeys = new StringTokenizer(keys, "##");
		StringTokenizer stValues = new StringTokenizer(values, "##");
		String optKey = "";
		String optValue = "";
		int nbTokens = getNbHtmlObjectsDisplayed(template, PagesContext);

		if (stKeys.countTokens() != stValues.countTokens())
		{
			SilverTrace.error("form", 
							"CheckBoxDisplayer.display", 
							"form.EX_ERR_ILLEGAL_PARAMETERS", 
							"Nb keys=" + stKeys.countTokens() + " & Nb values=" + stValues.countTokens());
		}
		else
		{
			html += "<table border=0>";	
			int col = 0;
			for (int i=0; i<nbTokens; i++)
			{
				if (col==0)
						html += "<tr>";
				
				col++;
				html += "<td>";
				optKey = stKeys.nextToken();
				optValue = stValues.nextToken();

				html += "<INPUT type=\"checkbox\" id=\""+fieldName+"\" name=\""+fieldName+"\" value=\"" + optKey + "\" ";

				if (template.isDisabled() || template.isReadOnly()) 
						html += " disabled ";
					
				if (valuesFromDB.contains(optKey))
						html += " checked ";

				html += ">&nbsp;" +  optValue;
				
				//last checkBox
				if (i==nbTokens-1)
				{
					if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() && !template.isHidden() && PagesContext.useMandatory()) 
					{
						html += "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">";
					}
				}
				
				html += "\n";

				if (col==cols)
				{
					html += "<tr>";
					col=0;
				}
			}

			if (col != 0)
				html += "</tr>";

			html += "</table>";			
		}			
		out.println(html);
  }
	
  /**
   * Updates the value of the field.
   *
   * The fieldName must be used to retrieve the HTTP parameter from the request.
   *
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  /*public void update(HttpServletRequest request,
                     Field field,
				     FieldTemplate template,
				     PagesContext PagesContext)
     throws FormException {
     	
     	if (! field.getTypeName().equals(TextField.TYPE))
			throw new FormException("CheckBoxDisplayer.update","form.EX_NOT_CORRECT_TYPE",TextField.TYPE);
			
		//14/07/2004 - NEY - several values can be entry
	    String[] newValues = request.getParameterValues(template.getFieldName());
	   	String valuesToInsert = "";
	   	
	   	if (newValues != null)
	   	{
	   		for (int v=0; v<newValues.length; v++)
	   		{
	   			valuesToInsert += newValues[v];
	   			if (v<newValues.length-1) {
	   				valuesToInsert += "##";
	   			}
	   		}
	   	}
		
		if (field.acceptValue(valuesToInsert, PagesContext.getLanguage()))
			field.setValue(valuesToInsert, PagesContext.getLanguage());
		else 
			throw new FormException("CheckBoxDisplayer.update","form.EX_NOT_CORRECT_VALUE",TextField.TYPE);
		
   }*/
   
   public void update(String values,
						Field field,
						FieldTemplate template,
						PagesContext PagesContext)
		throws FormException {
     	
		   if (! field.getTypeName().equals(TextField.TYPE))
			   throw new FormException("CheckBoxDisplayer.update","form.EX_NOT_CORRECT_TYPE",TextField.TYPE);
			
		   String valuesToInsert = values;
		
		   if (field.acceptValue(valuesToInsert, PagesContext.getLanguage()))
			   field.setValue(valuesToInsert, PagesContext.getLanguage());
		   else 
			   throw new FormException("CheckBoxDisplayer.update","form.EX_NOT_CORRECT_VALUE",TextField.TYPE);
		
	  }
   
   public boolean isDisplayedMandatory() {
		return true;
   }   

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext)
  {
	  String keys = "";
	  String values = "";
	  Map parameters = template.getParameters(pagesContext.getLanguage());
	  if (parameters.containsKey("keys")) keys = (String) parameters.get("keys");
	  if (parameters.containsKey("values")) values = (String) parameters.get("values");

	  // if either keys or values is not filled
	  // take the same for keys and values
	  if (keys.equals("") && !values.equals("")) keys = values;
	  if (values.equals("") && !keys.equals("")) values = keys;

	  // Calculate numbers of html elements
	  StringTokenizer stKeys = new StringTokenizer(keys, "##");
	  return stKeys.countTokens();
		
  }

}
