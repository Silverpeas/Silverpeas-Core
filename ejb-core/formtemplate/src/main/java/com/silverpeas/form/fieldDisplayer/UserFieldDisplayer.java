package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.UserField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A UserFieldDisplayer is an object which can display a UserFiel in HTML
 * and can retrieve via HTTP any updated value.
 * 
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class UserFieldDisplayer implements FieldDisplayer
{

   /**
    * Returns the name of the managed types.
    */
   public String[] getManagedTypes()
   {
      String[] s = new String[0];

      s[0] = UserField.TYPE;
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
   public void displayScripts(PrintWriter out, FieldTemplate template, 
                              PagesContext PagesContext) throws java.io.IOException
   {
      String language = PagesContext.getLanguage();

      String fieldName = template.getFieldName();

      if (!template.getTypeName().equals(UserField.TYPE))
      {
         SilverTrace.info("form", "TextFieldDisplayer.displayScripts", 
                          "form.INFO_NOT_CORRECT_TYPE", UserField.TYPE);

      }
      if (template.isMandatory() && PagesContext.useMandatory())
      {
         out.println("   if (isWhitespace(stripInitialWhitespace(document.forms['" 
                     + PagesContext.getFormName() + "'].elements['" 
                     + fieldName + UserField.PARAM_NAME_SUFFIX+"'].value))) {");
         out.println("      errorMsg+=\"  - '" 
                     + EncodeHelper.javaStringToJsString(template.getLabel(language)) 
                     + "' " + Util.getString("GML.MustBeFilled", language) 
                     + "\\n \";");
         out.println("      errorNb++;");
         out.println("   }");
      }
      
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
   public void display(PrintWriter out, Field field, FieldTemplate template, 
                       PagesContext PagesContext) throws FormException
   {
		SilverTrace.info("form", "UserFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD", "fieldName = "+template.getFieldName()+", value = "+field.getValue()+", fieldType = "+field.getTypeName());
		
      String language = PagesContext.getLanguage();
      String mandatoryImg = Util.getIcon("mandatoryField");
      String selectUserImg = Util.getIcon("userPanel");
      String selectUserLab = Util.getString("userPanel", language);
      String deleteUserImg = Util.getIcon("delete");
      String deleteUserLab = Util.getString("clearUser", language);

      String userName = "";
      String userId = "";
      String html = "";

      String fieldName = template.getFieldName();

      if (!field.getTypeName().equals(UserField.TYPE))
      {
         SilverTrace.info("form", "UserFieldDisplayer.display", 
                          "form.INFO_NOT_CORRECT_TYPE", UserField.TYPE);
      }
		else
		{
			userId = ((UserField) field).getUserId();
		}
      if (!field.isNull())
      {
         userName = field.getValue();
      }
      html += "<INPUT type=\"hidden\""
           +  " name=\"" + fieldName+UserField.PARAM_NAME_SUFFIX+"\" value=\"" +  EncodeHelper.javaStringToHtmlString(userId) + "\" >";

      if (!template.isHidden())
		{
         html += "<INPUT type=\"text\" disabled size=\"50\" "
              +  " id=\""+fieldName+"$$name\" name=\""+fieldName+"$$name\" value=\"" +  EncodeHelper.javaStringToHtmlString(userName) + "\" >";
		}

      if (!template.isHidden() &&!template.isDisabled() 
          &&!template.isReadOnly())
      {
         html += "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('"+URLManager.getApplicationURL()+"/RselectionPeasWrapper/jsp/open"
			     +  "?formName="+PagesContext.getFormName()
				  +  "&elementId="+fieldName+UserField.PARAM_NAME_SUFFIX
				  +  "&elementName="+fieldName+"$$name"
				  +  "&selectedUser="+((userId==null)?"":userId)
				  +  "','selectUser',800,600,'');\" >";
         html += "<img src=\"" 
              + selectUserImg 
              + "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" 
              + selectUserLab + "\" align=\"absmiddle\" title=\"" 
              + selectUserLab + "\"></a>";
         html += "&nbsp;<a href=\"#\" onclick=\"javascript:"
        	  + "document."+PagesContext.getFormName()+"."+fieldName+UserField.PARAM_NAME_SUFFIX+".value='';"
        	  + "document."+PagesContext.getFormName()+"."+fieldName+"$$name"+".value='';"
			  +  "\">";
         html += "<img src=\"" 
             + deleteUserImg 
             + "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" 
             + deleteUserLab + "\" align=\"absmiddle\" title=\"" 
             + deleteUserLab + "\"></a>";

         if (template.isMandatory() && PagesContext.useMandatory())
         {
            html += "&nbsp;<img src=\"" + mandatoryImg 
                    + "\" width=\"5\" height=\"5\" border=\"0\">";
         }
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
   /*public void update(HttpServletRequest request, Field field, 
                      FieldTemplate template, 
                      PagesContext pagesContext) throws FormException
   {

      String newId = request.getParameter(template.getFieldName()+"_id");
      
      if (field.getTypeName().equals(UserField.TYPE))
      {
		   if (newId == null || newId.trim().equals(""))
			{
			   field.setNull();
			}
			else
			{
            ((UserField) field).setUserId(newId);
			}
      }
      else
      {
         throw new FormException("UserFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", UserField.TYPE);
      }
   }*/
   
   public void update(String newId, Field field, 
						 FieldTemplate template, 
						 PagesContext pagesContext) throws FormException
  {

	 //String newId = request.getParameter(template.getFieldName()+"_id");
  
	 if (field.getTypeName().equals(UserField.TYPE))
	 {
		  if (newId == null || newId.trim().equals(""))
		   {
			  field.setNull();
		   }
		   else
		   {
		   ((UserField) field).setUserId(newId);
		   }
	 }
	 else
	 {
		throw new FormException("UserFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", UserField.TYPE);
	 }
  }

   /**
    * Method declaration
    * 
    * 
    * @return
    * 
    */
   public boolean isDisplayedMandatory()
   {
      return true;
   }

   /**
    * Method declaration
    * 
    * 
    * @return
    * 
    */
   public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext)
   {
      return 2;
   }

}
