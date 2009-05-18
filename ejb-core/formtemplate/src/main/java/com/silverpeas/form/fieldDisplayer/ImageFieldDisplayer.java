package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * A ImageFieldDisplayer is an object which can display an image in HTML
 * and can retrieve via HTTP any file.
 * 
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class ImageFieldDisplayer implements FieldDisplayer
{

   /**
    * Returns the name of the managed types.
    */
   public String[] getManagedTypes()
   {
      String[] s = new String[0];

      s[0] = FileField.TYPE;
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

      if (!template.getTypeName().equals(FileField.TYPE))
      {
         SilverTrace.info("form", "ImageFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE", FileField.TYPE);
      }
      if (template.isMandatory() && PagesContext.useMandatory())
      {
         out.println("	if (isWhitespace(stripInitialWhitespace(document.forms['"+ PagesContext.getFormName()+"'].elements['"+fieldName+"'].value))) {");
         out.println("		var "+fieldName+"Value = document.forms['"+PagesContext.getFormName()+"'].elements['"+fieldName+FileField.PARAM_NAME_SUFFIX+"'].value;");
         out.println("		if ("+fieldName+"Value=='' || "+fieldName+"Value.substring(0,7)==\"remove_\") {");
         out.println("			errorMsg+=\"  - '"+EncodeHelper.javaStringToJsString(template.getLabel(language))+"' "+Util.getString("GML.MustBeFilled", language)+"\\n \";");
         out.println("			errorNb++;");
         out.println("		}");
         out.println("	}");
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
                       PagesContext pagesContext) throws FormException
   {
		SilverTrace.info("form", "ImageFieldDisplayer.display", "root.MSG_GEN_ENTER_METHOD", "fieldName = "+template.getFieldName()+", value = "+field.getValue()+", fieldType = "+field.getTypeName());
		
      	String mandatoryImg = Util.getIcon("mandatoryField");

      	String html = "";

      	String fieldName = template.getFieldName();

      	if (!field.getTypeName().equals(FileField.TYPE))
      	{
        	SilverTrace.info("form", "ImageFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE", FileField.TYPE);
      	}
      	
      	String attachmentId = field.getValue();
		String componentId	= pagesContext.getComponentId();
		AttachmentDetail attachment = null;
		if (attachmentId != null && attachmentId.length() > 0)
		{
			attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentId, componentId));
		} else {
			attachmentId = "";
		}
      	
		if (template.isReadOnly() && !template.isHidden()) {
			if (attachment != null)
			{
				html = "<IMG alt=\"\" src=\""+attachment.getAttachmentURL()+"\">";
				//html += "<A href=\""+attachment.getAttachmentURL()+"\" target=\"_blank\">"+attachment.getLogicalName()+"</A>";
			}
		}
		else if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly())
      	{
			html += "<INPUT type=\"file\" size=\"50\" id=\""+fieldName+"\" name=\""+fieldName+"\">";
			html += "<INPUT type=\"hidden\" name=\""+fieldName+FileField.PARAM_NAME_SUFFIX+"\" value=\"" + attachmentId + "\">";
			
			if (attachment != null)
			{
				String deleteImg = Util.getIcon("delete");
				String deleteLab = Util.getString("removeImage", pagesContext.getLanguage());
				
				html += "&nbsp;<span id=\"div"+fieldName+"\">";
				html += "<IMG alt=\"\" align=\"absmiddle\" src=\""+attachment.getAttachmentIcon()+"\" width=20>&nbsp;";
				html += "<A href=\""+attachment.getAttachmentURL()+"\" target=\"_blank\">"+attachment.getLogicalName()+"</A>";
				
				html += "&nbsp;<a href=\"#\" onclick=\"javascript:"
					  + "document.getElementById('div"+fieldName+"').style.display='none';"
		        	  + "document."+pagesContext.getFormName()+"."+fieldName+FileField.PARAM_NAME_SUFFIX+".value='remove_"+attachmentId+"';"
					  +  "\">";
		        html += "<img src=\"" 
		             + deleteImg 
		             + "\" width=\"15\" height=\"15\" border=\"0\" alt=\"" 
		             + deleteLab + "\" align=\"absmiddle\" title=\"" 
		             + deleteLab + "\"></a>";
		        html += "</span>";
		    }

         	if (template.isMandatory() && pagesContext.useMandatory())
         	{
            	html += "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">";
         	}
      	}
      	out.println(html);
   }
   
   public void update(String attachmentId, Field field, 
						 FieldTemplate template, 
						 PagesContext pagesContext) throws FormException
	  {
		   if (field.getTypeName().equals(FileField.TYPE))
		   {
			   if (attachmentId == null || attachmentId.trim().equals(""))
			   {
				   field.setNull();
			   }
			   else
			   {
				   ((FileField) field).setAttachmentId(attachmentId);
			   }
		   }
		   else
		   {
			   throw new FormException("ImageFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", FileField.TYPE);
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
