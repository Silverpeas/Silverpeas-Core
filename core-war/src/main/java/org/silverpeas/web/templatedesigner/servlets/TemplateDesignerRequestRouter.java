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
package org.silverpeas.web.templatedesigner.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.field.PublicationsPickerField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.Label;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.contribution.content.form.record.ParameterValue;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.pdc.form.fieldtype.PdcField;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.AdminComponentRequestRouter;
import org.silverpeas.web.templatedesigner.control.TemplateDesignerSessionController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class TemplateDesignerRequestRouter extends
    AdminComponentRequestRouter<TemplateDesignerSessionController> {

  private static final long serialVersionUID = 1117593114737219878L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "TemplateDesigner";
  }

  @Override
  public TemplateDesignerSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new TemplateDesignerSessionController(mainSessionCtrl,
        componentContext);
  }

  @Override
  public String getAdminDestination(String function,
      TemplateDesignerSessionController templateDesignerSC,
      HttpRequest request) {
    String destination = "";
    String root = "/templateDesigner/jsp/";
    try {
      if (function.startsWith("Main")) {
        List<PublicationTemplate> templates = templateDesignerSC.getTemplates();
        request.setAttribute("Templates", templates);
        destination = root + "welcome.jsp";
      } else if (function.equals("ViewTemplate")) {
        String fileName = request.getParameter("Template");
        PublicationTemplate template;
        if (!StringUtil.isDefined(fileName)) {
          template = templateDesignerSC.reloadCurrentTemplate();
        } else {
          template = templateDesignerSC.setTemplate(fileName);
        }
        Form formUpdate = template.getUpdateFormAsXMLOne();
        DataRecord data = template.getRecordSet().getEmptyRecord();
        request.setAttribute("Form", formUpdate);
        request.setAttribute("Data", data);
        PagesContext context = new PagesContext("myForm", "2", templateDesignerSC.getLanguage(),
            false, "useless", templateDesignerSC.getUserId());
        context.setBorderPrinted(false);
        if (!template.isLocked()) {
          context.setDesignMode(true);
        }
        request.setAttribute("context", context);
        destination = root + "template.jsp";
      } else if (function.equals("NewTemplate")) {
        destination = getDestination("GoToTemplateHeader", templateDesignerSC, request);
      } else if (function.equals("EditTemplate")) {
        String fileName = request.getParameter("FileName");
        PublicationTemplate template;
        if (StringUtil.isDefined(fileName)) {
          template = templateDesignerSC.setTemplate(fileName);
        } else {
          template = templateDesignerSC.getCurrentTemplate();
        }
        request.setAttribute("Template", template);

        destination = getDestination("GoToTemplateHeader", templateDesignerSC, request);
      } else if ("GoToTemplateHeader".equals(function)) {
        request.setAttribute("ComponentsUsingForms", templateDesignerSC.getComponentsUsingForms());
        request.setAttribute("EncryptionAvailable", templateDesignerSC.isEncryptionAvailable());
        request.setAttribute("Utilization", templateDesignerSC.getNumberOfRecordsByTemplateAndComponents());
        destination = root + "templateHeader.jsp";
      } else if (function.equals("AddTemplate")) {
        PublicationTemplate template = request2Template(request);
        if (templateDesignerSC.createTemplate(template)) {
          destination = getDestination("ViewTemplate", templateDesignerSC, request);
        } else {
          destination = getDestination("Main", templateDesignerSC, request);
        }
      } else if ("UpdateTemplate".equals(function)) {
        PublicationTemplate template = request2Template(request);
        try {
          templateDesignerSC.updateTemplate((PublicationTemplateImpl) template);
          destination = getDestination("Main", templateDesignerSC, request);
        } catch (CryptoException e) {
          request.setAttribute("CryptoException", e);
          destination = getDestination("EditTemplate", templateDesignerSC, request);
        }
      } else if (function.equals("NewField")) {
        String displayer = request.getParameter("Displayer");

        setCommonAttributesOfFieldDisplayers(displayer, templateDesignerSC, request);

        destination = root + getDestinationFromDisplayer(displayer);
      } else if (function.equals("BackToFormField")) {
        GenericFieldTemplate field = (GenericFieldTemplate) request.getAttribute("field");
        request.setAttribute("Field", field);

        String actionForm = (String) request.getAttribute("actionForm");
        request.setAttribute("actionForm", actionForm);

        setCommonAttributesOfFieldDisplayers(field.getDisplayerName(), templateDesignerSC, request);

        destination = root + getDestinationFromDisplayer(field.getDisplayerName());
      } else if (function.equals("AddField")) {
        GenericFieldTemplate field = request2Field(request);

        templateDesignerSC.addField(field);

        if (PdcField.TYPE.equals(field.getTypeName())) {
          request.setAttribute("UrlToReload", "ViewTemplate");
          destination = root + "closeWindow.jsp";
        } else {
          destination = getDestination("ViewTemplate", templateDesignerSC, request);
        }
      } else if (function.equals("EditField")) {
        String fieldName = request.getParameter("FieldName");

        FieldTemplate field = templateDesignerSC.getField(fieldName);
        request.setAttribute("Field", field);

        setCommonAttributesOfFieldDisplayers(field.getDisplayerName(), templateDesignerSC, request);

        destination = root + getDestinationFromDisplayer(field.getDisplayerName());
      } else if (function.equals("UpdateField")) {
        GenericFieldTemplate field = request2Field(request);

        templateDesignerSC.updateField(field);

        if (PdcField.TYPE.equals(field.getTypeName())) {
          request.setAttribute("UrlToReload", "ViewTemplate");
          destination = root + "closeWindow.jsp";
        } else {
          destination = getDestination("ViewTemplate", templateDesignerSC, request);
        }
      } else if (function.equals("DeleteField")) {
        String fieldName = request.getParameter("FieldName");

        templateDesignerSC.removeField(fieldName);

        destination = getDestination("ViewTemplate", templateDesignerSC, request);
      } else if (function.equals("SaveTemplate")) {
        templateDesignerSC.saveTemplate();

        destination = getDestination("ViewTemplate", templateDesignerSC, request);
      } else if ("DuplicateForm".equals(function)) {
        templateDesignerSC.duplicateTemplate(request.getParameter("DuplicatedFormName"));
        destination = getDestination("Main", templateDesignerSC, request);
      } else if ("RemoveTemplate".equals(function)) {
        templateDesignerSC.deleteTemplate();
        destination = getDestination("Main", templateDesignerSC, request);
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String getDestinationFromDisplayer(String displayer) {
    switch (displayer) {
      case "wysiwyg":
        return "fieldWysiwyg.jsp";
      case "textarea":
        return "fieldTextarea.jsp";
      case "listbox":
      case "checkbox":
      case "radio":
        return "fieldMultivalues.jsp";
      case "url":
        return "fieldURL.jsp";
      case "date":
        return "fieldDate.jsp";
      case "file":
        return "fieldFile.jsp";
      case "image":
        return "fieldImage.jsp";
      case "video":
        return "fieldVideo.jsp";
      case "user":
        return "fieldUser.jsp";
      case "multipleUser":
        return "fieldMultipleUser.jsp";
      case "ldap":
        return "fieldLdap.jsp";
      case "accessPath":
        return "fieldAccessPath.jsp";
      case "jdbc":
        return "fieldJdbc.jsp";
      case "pdc":
        return "fieldPdc.jsp";
      case "group":
        return "fieldGroup.jsp";
      case "sequence":
        return "fieldSequence.jsp";
      case "time":
        return "fieldTime.jsp";
      case "explorer":
        return "fieldExplorer.jsp";
      case "map":
        return "fieldMap.jsp";
      case "email":
        return "fieldEmail.jsp";
      case PublicationsPickerField.TYPE:
        return "fieldPublicationsPicker.jsp";
    }
    return "fieldText.jsp";
  }

  private PublicationTemplate request2Template(HttpRequest request) throws IOException {
    List<FileItem> parameters = request.getFileItems();
    String name = FileUploadUtil.getParameter(parameters, "Name");
    String description = FileUploadUtil.getParameter(parameters, "Description");
    boolean visible = StringUtil.getBooleanValue(FileUploadUtil.getParameter(parameters, "Visible"));
    String thumbnail = FileUploadUtil.getParameter(parameters, "Thumbnail");
    boolean searchable = StringUtil.getBooleanValue(FileUploadUtil.getParameter(parameters, "Searchable"));
    boolean encrypted = StringUtil.getBooleanValue(FileUploadUtil.getParameter(parameters, "Encrypted"));
    boolean directoryUsage = request.getParameterAsBoolean("DirectoryUsage");

    PublicationTemplateImpl template = new PublicationTemplateImpl();
    template.setName(name);
    template.setDescription(description);
    template.setThumbnail(thumbnail);
    template.setVisible(visible);
    template.setDataEncrypted(encrypted);
    template.setDirectoryUsage(directoryUsage);

    if (searchable) {
      template.setSearchFileName("dummy");
    } else {
      template.setSearchFileName(null);
    }

    boolean deleteViewLayer = StringUtil.getBooleanValue(FileUploadUtil.getParameter(parameters, "DeleteViewLayer"));
    FileItem viewLayer = FileUploadUtil.getFile(parameters, "ViewLayer");
    if (viewLayer != null && StringUtil.isDefined(viewLayer.getName())) {
      File file = new File(FileRepositoryManager.getTemporaryPath()+System.currentTimeMillis(), "view.html");
      FileUploadUtil.saveToFile(file, viewLayer);
      template.setViewLayerFileName(file.getAbsolutePath());
      template.setViewLayerAction(PublicationTemplateImpl.LAYER_ACTION_ADD);
    } else if (deleteViewLayer) {
      template.setViewLayerAction(PublicationTemplateImpl.LAYER_ACTION_REMOVE);
    }

    boolean deleteUpdateLayer = StringUtil.getBooleanValue(FileUploadUtil.getParameter(parameters, "DeleteUpdateLayer"));
    FileItem updateLayer = FileUploadUtil.getFile(parameters, "UpdateLayer");
    if (updateLayer != null && StringUtil.isDefined(updateLayer.getName())) {
      File file = new File(FileRepositoryManager.getTemporaryPath()+System.currentTimeMillis(), "update.html");
      FileUploadUtil.saveToFile(file, updateLayer);
      template.setUpdateLayerFileName(file.getAbsolutePath());
      template.setUpdateLayerAction(PublicationTemplateImpl.LAYER_ACTION_ADD);
    } else if (deleteUpdateLayer) {
      template.setUpdateLayerAction(PublicationTemplateImpl.LAYER_ACTION_REMOVE);
    }

    String paramSpaceIds = request.getParameter("Visibility_Spaces");
    if (StringUtil.isDefined(paramSpaceIds)) {
      template.setSpaces(Arrays.asList(paramSpaceIds.split(" ")));
    }

    String[] applications = request.getParameterValues("Visibility_Applications");
    if (applications != null) {
      template.setApplications(Arrays.asList(applications));
    }

    String paramInstances = request.getParameter("Visibility_Instances");
    if (StringUtil.isDefined(paramInstances)) {
      template.setInstances(Arrays.asList(paramInstances.split(" ")));
    }

    String paramDomainIds = request.getParameter("Visibility_Domains");
    if (StringUtil.isDefined(paramDomainIds)) {
      template.setDomains(Arrays.asList(paramDomainIds.split(" ")));
    }

    String paramGroupIds = request.getParameter("Visibility_Groups");
    if (StringUtil.isDefined(paramGroupIds)) {
      template.setGroups(Arrays.asList(paramGroupIds.split(" ")));
    }

    return template;
  }

  public static GenericFieldTemplate request2Field(HttpServletRequest request)
      throws FormException {
    String displayer = request.getParameter("Displayer");
    String fieldName = request.getParameter("FieldName");
    boolean mandatory = StringUtil.getBooleanValue(request.getParameter("Mandatory"));
    boolean readOnly = StringUtil.getBooleanValue(request.getParameter("ReadOnly"));
    boolean hidden = StringUtil.getBooleanValue(request.getParameter("Hidden"));
    boolean disabled = StringUtil.getBooleanValue(request.getParameter("Disabled"));
    boolean searchable = StringUtil.getBooleanValue(request.getParameter("Searchable"));
    boolean usedAsFacet = StringUtil.getBooleanValue(request.getParameter("UsedAsFacet"));

    String fieldType = "text";
    switch (displayer) {
      case "user":
        fieldType = "user";
        break;
      case "multipleUser":
        fieldType = "multipleUser";
        break;
      case "date":
        fieldType = "date";
        break;
      case "image":
      case "file":
      case "video":
        fieldType = "file";
        break;
      case "ldap":
        fieldType = "ldap";
        break;
      case "accessPath":
        fieldType = "accessPath";
        break;
      case "jdbc":
        fieldType = "jdbc";
        break;
      case "pdc":
        fieldType = PdcField.TYPE;
        break;
      case "group":
        fieldType = "group";
        break;
      case "sequence":
        fieldType = "sequence";
        break;
      case "explorer":
        fieldType = "explorer";
        break;
      case "publicationsPicker":
        fieldType = PublicationsPickerField.TYPE;
        break;
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
    field.setUsedAsFacet(usedAsFacet);

    String nbMaxValues = request.getParameter("NbMaxValues");
    if (StringUtil.isInteger(nbMaxValues)) {
      field.setMaximumNumberOfOccurrences(Integer.parseInt(nbMaxValues));
    }

    Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String paramName = paramNames.nextElement();
      if (paramName.startsWith("Param_")) {
        String xmlParameterName = paramName.substring(6);
        String xmlParameterValue = request.getParameter(paramName);
        if (StringUtil.isDefined(xmlParameterValue)) {
          Parameter parameter = new Parameter(xmlParameterName);
          parameter.getParameterValues().add(new ParameterValue("fr", xmlParameterValue));
          field.getParameters().add(parameter);
        }
      } else if (paramName.startsWith("Label_")) {
        String lang = paramName.substring(6);
        String sLabel = request.getParameter(paramName);
        if (StringUtil.isDefined(sLabel)) {
          Label label = new Label(sLabel, lang);
          field.getLabelsObj().add(label);
        }
      }
    }
    return field;
  }

  private void setCommonAttributesOfFieldDisplayers(String displayerName,
      TemplateDesignerSessionController sc, HttpServletRequest request) {
    request.setAttribute("Languages", sc.getLanguages());
    request.setAttribute("Displayer", displayerName);
    request.setAttribute("Fields", sc.getFields());
  }
}
