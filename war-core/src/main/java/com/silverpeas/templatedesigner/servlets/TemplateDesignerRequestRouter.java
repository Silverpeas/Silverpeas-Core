/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.templatedesigner.servlets;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.Label;
import com.silverpeas.form.record.Parameter;
import com.silverpeas.form.record.ParameterValue;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.templatedesigner.control.TemplateDesignerSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class TemplateDesignerRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1117593114737219878L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "TemplateDesigner";
  }

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new TemplateDesignerSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialized.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    String destination = "";
    String root = "/templateDesigner/jsp/";
    TemplateDesignerSessionController templateDesignerSC = (TemplateDesignerSessionController) componentSC;
    SilverTrace.info("templateDesigner", "TemplateDesignerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);
    try {
      if (function.startsWith("Main")) {
        List<PublicationTemplate> templates = templateDesignerSC.getTemplates();
        request.setAttribute("Templates", templates);
        destination = root + "welcome.jsp";
      } else if (function.equals("ViewTemplate")) {
        String fileName = request.getParameter("Template");
        PublicationTemplate template = null;
        if (!isDefined(fileName)) {
          template = templateDesignerSC.reloadCurrentTemplate();
        } else {
          template = templateDesignerSC.setTemplate(fileName);
        }
        Form formUpdate = template.getUpdateForm();
        DataRecord data = template.getRecordSet().getEmptyRecord();
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        PagesContext context = new PagesContext("myForm", "2", templateDesignerSC.getLanguage(),
            false, "useless", templateDesignerSC.getUserId());
        context.setBorderPrinted(false);
        request.setAttribute("context", context);
        destination = root + "template.jsp";
      } else if (function.equals("NewTemplate")) {
        destination = root + "templateHeader.jsp";
      } else if (function.equals("EditTemplate")) {
        String fileName = request.getParameter("FileName");
        PublicationTemplate template;
        if (isDefined(fileName)) {
          template = templateDesignerSC.setTemplate(fileName);
        } else {
          template = templateDesignerSC.getCurrentTemplate();
        }
        request.setAttribute("Template", template);

        destination = root + "templateHeader.jsp";
      } else if (function.equals("AddTemplate")) {
        PublicationTemplate template = request2Template(request);
        templateDesignerSC.createTemplate(template);
        destination = getDestination("ViewFields", componentSC, request);
      } else if (function.equals("UpdateTemplate")) {
        PublicationTemplate template = request2Template(request);
        templateDesignerSC.updateTemplate((PublicationTemplateImpl) template);
        destination = getDestination("Main", componentSC, request);
      } else if (function.equals("ViewFields")) {
        request.setAttribute("Fields", templateDesignerSC.getFields());
        request.setAttribute("UpdateInProgress", templateDesignerSC.isUpdateInProgress());

        destination = root + "fields.jsp";
      } else if (function.equals("NewField")) {
        String displayer = request.getParameter("Displayer");

        request.setAttribute("Languages", templateDesignerSC.getLanguages());
        request.setAttribute("Displayer", displayer);

        destination = root + getDestinationFromDisplayer(displayer);
      } else if (function.equals("BackToFormField")) {
        GenericFieldTemplate field = (GenericFieldTemplate) request.getAttribute("field");
        request.setAttribute("Field", field);

        String actionForm = (String) request.getAttribute("actionForm");
        request.setAttribute("actionForm", actionForm);

        request.setAttribute("Languages", templateDesignerSC.getLanguages());
        request.setAttribute("Displayer", field.getDisplayerName());

        destination = root + getDestinationFromDisplayer(field.getDisplayerName());
      } else if (function.equals("AddField")) {
        GenericFieldTemplate field = request2Field(request);

        templateDesignerSC.addField(field);
        
        request.setAttribute("UrlToReload", "ViewFields");
        destination = root + "closeWindow.jsp";
      } else if (function.equals("EditField")) {
        String fieldName = request.getParameter("FieldName");

        FieldTemplate field = templateDesignerSC.getField(fieldName);
        request.setAttribute("Field", field);

        request.setAttribute("Languages", templateDesignerSC.getLanguages());
        request.setAttribute("Displayer", field.getDisplayerName());

        destination = root
            + getDestinationFromDisplayer(field.getDisplayerName());
      } else if (function.equals("UpdateField")) {
        GenericFieldTemplate field = request2Field(request);

        templateDesignerSC.updateField(field);
        
        request.setAttribute("UrlToReload", "ViewFields");
        destination = root + "closeWindow.jsp";
      } else if (function.equals("DeleteField")) {
        String fieldName = request.getParameter("FieldName");

        templateDesignerSC.removeField(fieldName);

        destination = getDestination("ViewFields", componentSC, request);
      } else if (function.equals("MoveField")) {
        String fieldName = request.getParameter("FieldName");
        int direction = Integer.parseInt(request.getParameter("Direction"));

        templateDesignerSC.moveField(fieldName, direction);

        destination = getDestination("ViewFields", componentSC, request);
      } else if (function.equals("SaveTemplate")) {
        templateDesignerSC.saveTemplate();

        destination = getDestination("ViewTemplate", componentSC, request);
      } else {
        destination = root + "welcome.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("templateDesigner",
        "TemplateDesignerRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  private String getDestinationFromDisplayer(String displayer) {
    if (displayer.equals("wysiwyg")) {
      return "fieldWysiwyg.jsp";
    }  if (displayer.equals("textarea")) {
      return "fieldTextarea.jsp";
    }  if (displayer.equals("listbox")) {
      return "fieldMultivalues.jsp";
    }  if (displayer.equals("checkbox")) {
      return "fieldMultivalues.jsp";
    } if (displayer.equals("radio")) {
      return "fieldMultivalues.jsp";
    } if (displayer.equals("url")) {
      return "fieldURL.jsp";
    } if (displayer.equals("date")) {
      return "fieldDate.jsp";
    } if (displayer.equals("file")) {
      return "fieldFile.jsp";
    } if (displayer.equals("image")) {
      return "fieldImage.jsp";
    } if (displayer.equals("video")) {
      return "fieldVideo.jsp";
    } if (displayer.equals("user")) {
      return "fieldUser.jsp";
    } if (displayer.equals("multipleUser")) {
      return "fieldMultipleUser.jsp";
    } if (displayer.equals("ldap")) {
      return "fieldLdap.jsp";
    } if (displayer.equals("accessPath")) {
      return "fieldAccessPath.jsp";
    } if (displayer.equals("jdbc")) {
      return "fieldJdbc.jsp";
    } if (displayer.equals("pdc")) {
      return "fieldPdc.jsp";
    } if (displayer.equals("group")) {
      return "fieldGroup.jsp";
    } if (displayer.equals("sequence")) {
      return "fieldSequence.jsp";
    } else {
      return "fieldText.jsp";
    }
  }

  private PublicationTemplate request2Template(HttpServletRequest request) {
    String name = request.getParameter("Name");
    String description = request.getParameter("Description");
    boolean visible = "true".equalsIgnoreCase(request.getParameter("Visible"));
    String thumbnail = request.getParameter("Thumbnail");
    boolean searchable = "true".equalsIgnoreCase(request.getParameter("Searchable"));

    PublicationTemplateImpl template = new PublicationTemplateImpl();
    template.setName(name);
    template.setDescription(description);
    template.setThumbnail(thumbnail);
    template.setVisible(visible);

    if (searchable) {
      template.setSearchFileName("dummy");
    } else {
      template.setSearchFileName(null);
    }

    return template;
  }

  public static GenericFieldTemplate request2Field(HttpServletRequest request)
      throws FormException {
    String displayer = request.getParameter("Displayer");
    String fieldName = request.getParameter("FieldName");
    boolean mandatory = "true".equalsIgnoreCase(request.getParameter("Mandatory"));
    boolean readOnly = "true".equalsIgnoreCase(request.getParameter("ReadOnly"));
    boolean hidden = "true".equalsIgnoreCase(request.getParameter("Hidden"));
    boolean disabled = "true".equalsIgnoreCase(request.getParameter("Disabled"));
    boolean searchable = "true".equalsIgnoreCase(request.getParameter("Searchable"));

    String fieldType = "text";
    if (displayer.equals("user")) {
      fieldType = "user";
    } else if (displayer.equals("multipleUser")) {
      fieldType = "multipleUser";
    } else if (displayer.equals("date")) {
      fieldType = "date";
    } else if (displayer.equals("image") || displayer.equals("file") || displayer.equals("video")) {
      fieldType = "file";
    } else if (displayer.equals("ldap")) {
      fieldType = "ldap";
    } else if (displayer.equals("accessPath")) {
      fieldType = "accessPath";
    } else if (displayer.equals("jdbc")) {
      fieldType = "jdbc";
    } else if (displayer.equals("pdc")) {
      fieldType = "pdc";
    } else if (displayer.equals("group")) {
      fieldType = "group";
    } else if (displayer.equals("sequence")) {
      fieldType = "sequence";
    }

    GenericFieldTemplate field = new GenericFieldTemplate();
    field.setDisplayerName(displayer);
    field.setFieldName(fieldName);
    field.setDisabled(disabled);
    field.setHidden(hidden);
    field.setMandatory(mandatory);
    field.setReadOnly(readOnly);
    field.setTypeName(fieldType);
    field.setSearchable(searchable);

    @SuppressWarnings("unchecked")
    Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String paramName = paramNames.nextElement();
      if (paramName.startsWith("Param_")) {
        String xmlParameterName = paramName.substring(6);
        String xmlParameterValue = request.getParameter(paramName);
        if (isDefined(xmlParameterValue)) {
          Parameter parameter = new Parameter(xmlParameterName, "dummy");
          parameter.getParameterValuesObj().add(new ParameterValue("fr", xmlParameterValue));
          field.getParametersObj().add(parameter);
        }
      } else if (paramName.startsWith("Label_")) {
        String lang = paramName.substring(6);
        String sLabel = request.getParameter(paramName);
        if (isDefined(sLabel)) {
          Label label = new Label(sLabel, lang);
          field.getLabelsObj().add(label);
        }
      }
    }
    return field;
  }

  private static boolean isDefined(String parameter) {
    return (parameter != null && parameter.length() > 0 && !parameter.equals("null"));
  }
}
