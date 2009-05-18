package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A WysiwygFieldDisplayer is an object which can display a TextFiel in HTML
 * the content of a TextFiel to a end user
 * and can retrieve via HTTP any updated value.
 * 
 * 
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class WysiwygFieldDisplayer implements FieldDisplayer
{
	/**
   * Constructeur
   */  
   public WysiwygFieldDisplayer() {
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
							 	
 		
		String fieldName = template.getFieldName();
		String language = PagesContext.getLanguage();

		if (! template.getTypeName().equals(TextField.TYPE))
			SilverTrace.info("form", "WysiwygFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
			
		out.println("var thecode = document.all."+fieldName+"editBox.testHTML(false, new Array('IMG'));");
		out.println("document.forms["+PagesContext.getFormIndex()+"].elements["+PagesContext.getCurrentFieldIndex()+"].value = thecode;");

	 	if (template.isMandatory() && PagesContext.useMandatory()) {
			out.println("	if (isWhitespace(stripInitialWhitespace(thecode)) || thecode == \"<P>&nbsp;</P>\") {");
			out.println("		errorMsg+=\"  - '"+template.getLabel(language)+"' "+Util.getString("GML.MustBeFilled", language)+"\\n \";");
			out.println("		errorNb++;");
			out.println("	}");
    	} 		

		out.println("	if (! isValidText(document.forms["+PagesContext.getFormIndex()+"].elements["+PagesContext.getCurrentFieldIndex()+"], "+Util.getSetting("nbMaxCar")+")) {");
    	out.println("		errorMsg+=\"  - '"+template.getLabel(language)+"' "+Util.getString("ContainsTooLargeText", language)+Util.getSetting("nbMaxCar")+" "+Util.getString("Characters", language)+"\\n \";");
       	out.println("		errorNb++;");
     	out.println("	}");    	
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
				
			String code = "";	
			String mandatoryImg = Util.getIcon("mandatoryField");
			
			String fieldName = template.getFieldName();
			if (! field.getTypeName().equals(TextField.TYPE))
				SilverTrace.info("form", "WysiwygFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
				
			if (! field.isNull())
				code = field.getValue(PagesContext.getLanguage());
											
			if (template.isDisabled() || template.isReadOnly()) {
				out.println(code);
			}
			
			else {
				
out.println("<INPUT TYPE=\"hidden\" name=\""+fieldName+"\">");
			
out.println("<TABLE>");

out.println("<TR>");

out.println("<TD valign=top>");


out.println("<SCRIPT FOR="+fieldName+"editBox EVENT=onreadystatechange>");
out.println("if (document.all."+fieldName+"editBox.readyState==4)");
out.println("  bLoad=true");
out.println("</SCRIPT>");

out.println("</TD>");

out.println("<TD>");

try {
	out.println("<OBJECT ID="+fieldName+"editBox WIDTH=380 HEIGHT=250 DATA=\""+Util.getPath()+"/form/jsp/editor.jsp?Code="+URLEncoder.encode(code, "ISO-8859-1")+"\" TYPE=\"text/x-scriptlet\">");
} catch (UnsupportedEncodingException e) {
	throw new FormException("WysiwygFieldDisplayer.display", e.getMessage(), e);
}	
out.println("</OBJECT>");

if (template.isMandatory() && PagesContext.useMandatory()) {
	out.println("&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">");
}			
out.println("</TD>");

out.println("</TR>");
out.println("</TABLE>");

}
							
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
				throw new FormException("WysiwygFieldDisplayer.update","form.EX_NOT_CORRECT_TYPE",TextField.TYPE);
				
		
		String newValue = request.getParameter(template.getFieldName());
		
		if (field.acceptValue(newValue, PagesContext.getLanguage()))
			field.setValue(newValue, PagesContext.getLanguage());
		else throw new FormException("WysiwygFieldDisplayer.update","form.EX_NOT_CORRECT_VALUE",TextField.TYPE);
		
   }*/
   
   public void update(String newValue,
						Field field,
						FieldTemplate template,
						PagesContext PagesContext)
		throws FormException {
     	
		   if (! field.getTypeName().equals(TextField.TYPE))
				   throw new FormException("WysiwygFieldDisplayer.update","form.EX_NOT_CORRECT_TYPE",TextField.TYPE);
				
		   if (field.acceptValue(newValue, PagesContext.getLanguage()))
			   field.setValue(newValue, PagesContext.getLanguage());
		   else throw new FormException("WysiwygFieldDisplayer.update","form.EX_NOT_CORRECT_VALUE",TextField.TYPE);
		
	  }
   
   public boolean isDisplayedMandatory() {
		return true;
   }   

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext)
  {
		return 2;
  }

}
