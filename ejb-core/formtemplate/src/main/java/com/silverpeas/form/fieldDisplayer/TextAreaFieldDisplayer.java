package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.util.Map;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A TextAreaFieldDisplayer is an object which can display a TextFiel in HTML
 * the content of a TextFiel to a end user
 * and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TextAreaFieldDisplayer implements FieldDisplayer 
{
	
	/**
   * Constructeur
   */  
   public TextAreaFieldDisplayer() {
   }
   
  
  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
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
                             PagesContext PagesContext)  throws java.io.IOException { 
 		
		String language = PagesContext.getLanguage();
		
		if (! template.getTypeName().equals(TextField.TYPE))
			SilverTrace.info("form", "TextAreaFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);

	 	if (template.isMandatory() && PagesContext.useMandatory()) {
			out.println("	if (isWhitespace(stripInitialWhitespace(document.forms["+PagesContext.getFormIndex()+"].elements["+PagesContext.getCurrentFieldIndex()+"].value))) {");
			out.println("		errorMsg+=\"  - '"+template.getLabel(language)+"' "+Util.getString("GML.MustBeFilled", language)+"\\n \";");
			out.println("		errorNb++;");
			out.println("	}");
    	} 		
    	
    	out.println("	if (! isValidText(document.forms["+PagesContext.getFormIndex()+"].elements["+PagesContext.getCurrentFieldIndex()+"], "+Util.getSetting("nbMaxCar")+")) {");
    	out.println("		errorMsg+=\"  - '"+template.getLabel(language)+"' "+Util.getString("ContainsTooLargeText", language)+Util.getSetting("nbMaxCar")+" "+Util.getString("Characters", language)+"\\n \";");
       	out.println("		errorNb++;");
     	out.println("	}");
     	
     	Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
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
                      PagesContext PagesContext) throws FormException {
				
			String value = "";	
			String rows = "6";
			String cols = "48";
			String html = "";
			
			String mandatoryImg = Util.getIcon("mandatoryField");
			
			String fieldName = template.getFieldName();
			Map parameters = template.getParameters(PagesContext.getLanguage());
			
			if (! field.getTypeName().equals(TextField.TYPE))
				SilverTrace.info("form", "TextAreaFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
				
			if (! field.isNull())
				value = field.getValue(PagesContext.getLanguage());
						
			
			html += "<textarea id=\""+fieldName+"\" name=\""+fieldName+"\"";
				
			if (parameters.containsKey("rows")) {
				rows = (String) parameters.get("rows");
			}
			html += " rows="+rows;	
			
			if (parameters.containsKey("cols")) {
				cols = (String) parameters.get("cols");
			}
			html += " cols="+cols;	
			
			if (template.isDisabled()) {
				html += " disabled";
			}
			else if (template.isReadOnly()) {
				html += " readOnly";
			}
			
			html += " >"+EncodeHelper.javaStringToHtmlString(value)+"</TEXTAREA>"; 
			
			if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() && !template.isHidden() && PagesContext.useMandatory()) {
				html += "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\"/>";
			}

			out.println(html);
  }
   
   public void update(String newValue,
						Field field,
						FieldTemplate template,
						PagesContext PagesContext)
		throws FormException {
     	
		   if (! field.getTypeName().equals(TextField.TYPE))
				   throw new FormException("TextAreaFieldDisplayer.update","form.EX_NOT_CORRECT_TYPE",TextField.TYPE);
		
		   if (field.acceptValue(newValue, PagesContext.getLanguage()))
			   field.setValue(newValue, PagesContext.getLanguage());
		   else throw new FormException("TextAreaFieldDisplayer.update","form.EX_NOT_CORRECT_VALUE",TextField.TYPE);
		
	  }
   
   public boolean isDisplayedMandatory() {
		return true;
   }   

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext)
  {
		return 1;
  }

}
