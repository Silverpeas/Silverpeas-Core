/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.formTemplate.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormTemplateSessionController;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormTemplateRequestRouter extends
    ComponentRequestRouter<FormTemplateSessionController> {

  private static final long serialVersionUID = 1L;

  @Override
  public FormTemplateSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new FormTemplateSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "formTemplate";
  }

  /**
   * This method has to be implemented by the component request Router it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param controller The component Session Control, build and initialised.
   * @param request The entering request. The request Router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, FormTemplateSessionController controller,
      HttpServletRequest request) {
    SilverTrace.info("form", "FormTemplateRequestRouter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD", "function = " + function);
    String destination = "";

    PublicationTemplateManager publicationTemplateManager =
        PublicationTemplateManager.getInstance();

    try {
      if ("Edit".equals(function)) {
        // display editing form with data

        String componentId = request.getParameter("ComponentId");
        String objectId = request.getParameter("ObjectId");
        String objectType = request.getParameter("ObjectType");
        String objectLanguage = request.getParameter("ObjectLanguage");
        String xmlFormName = request.getParameter("XMLFormName");
        String reloadOpener = request.getParameter("ReloadOpener");
        String urlToReload = request.getParameter("UrlToReload");

        controller.setComponentId(componentId);
        controller.setObjectId(objectId);
        controller.setObjectType(objectType);
        controller.setObjectLanguage(objectLanguage);
        controller.setXmlFormName(xmlFormName);
        controller.setReloadOpener(reloadOpener);
        controller.setUrlToReload(urlToReload);

        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

        // register xmlForm to object
        publicationTemplateManager.addDynamicPublicationTemplate(componentId + ":" + objectType +
            ":" + xmlFormShortName, xmlFormName);

        PublicationTemplateImpl pubTemplate =
            (PublicationTemplateImpl) publicationTemplateManager.getPublicationTemplate(
            componentId + ":" + objectType + ":" + xmlFormShortName, xmlFormName);
        Form formUpdate = pubTemplate.getUpdateForm();
        RecordSet recordSet = pubTemplate.getRecordSet();

        DataRecord data = recordSet.getRecord(objectId, objectLanguage);
        if (data == null) {
          data = recordSet.getEmptyRecord();
          data.setId(objectId);
        }

        PagesContext pageContext =
            new PagesContext("myForm", "2", controller.getLanguage(), false, componentId,
            controller.getUserId());
        pageContext.setObjectId(objectId);

        request.setAttribute("XMLForm", formUpdate);
        request.setAttribute("XMLData", data);
        request.setAttribute("XMLFormName", xmlFormName);
        request.setAttribute("PagesContext", pageContext);

        destination = "/form/jsp/edit.jsp";
      } else if ("Update".equals(function)) {
        // Save changes
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String xmlFormName = controller.getXmlFormName();
        String xmlFormShortName =
            xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

        String objectId = controller.getObjectId();
        String objectType = controller.getObjectType();
        String componentId = controller.getComponentId();
        String objectLanguage = controller.getObjectLanguage();

        PublicationTemplate pub =
            publicationTemplateManager.getPublicationTemplate(componentId + ":" + objectType + ":" +
            xmlFormShortName);

        RecordSet set = pub.getRecordSet();
        Form form = pub.getUpdateForm();

        int callbackAction = CallBackManager.ACTION_XMLCONTENT_UPDATE;

        DataRecord data = set.getRecord(objectId, objectLanguage);
        if (data == null) {
          data = set.getEmptyRecord();
          data.setId(objectId);
          data.setLanguage(objectLanguage);
          callbackAction = CallBackManager.ACTION_XMLCONTENT_CREATE;
        }

        PagesContext context =
            new PagesContext("myForm", "3", controller.getLanguage(), false, componentId,
            controller.getUserId());
        context.setObjectId(objectId);
        context.setContentLanguage(objectLanguage);

        form.update(items, data, context);
        set.save(data);

        Map<String, String> params = new HashMap<String, String>();
        params.put("ObjectId", objectId);
        params.put("ObjectType", objectType);
        params.put("ObjectLanguage", objectLanguage);
        params.put("XMLFormName", xmlFormShortName);

        // launch event
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(callbackAction, Integer.parseInt(controller.getUserId()),
            componentId, params);
        request.setAttribute("ReloadOpener", controller.getReloadOpener());
        request.setAttribute("urlToReload", controller.getUrlToReload());

        destination = "/form/jsp/close.jsp";
      } else if (function.equals("View")) {
        String componentId = request.getParameter("ComponentId");
        String objectId = request.getParameter("ObjectId");
        String objectType = request.getParameter("ObjectType");
        String objectLanguage = request.getParameter("ObjectLanguage");
        String xmlFormName = request.getParameter("XMLFormName");

        if (xmlFormName.indexOf(".") != -1)
          xmlFormName = xmlFormName.substring(0, xmlFormName.indexOf("."));

        if (StringUtil.isDefined(xmlFormName) && StringUtil.isDefined(objectId)) {
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) publicationTemplateManager
              .getPublicationTemplate(componentId + ":" + objectType + ":" + xmlFormName);

          Form formView = pubTemplate.getViewForm();

          RecordSet recordSet = pubTemplate.getRecordSet();
          DataRecord data = recordSet.getRecord(objectId, objectLanguage);
          if (data == null) {
            data = recordSet.getEmptyRecord();
            data.setId(objectId);
          }

          PagesContext pageContext =
              new PagesContext("myForm", "2", controller.getLanguage(), false, componentId,
              controller.getUserId());
          pageContext.setObjectId(objectId);

          request.setAttribute("XMLForm", formView);
          request.setAttribute("XMLData", data);
          request.setAttribute("PagesContext", pageContext);
        }

        destination = "/form/jsp/view.jsp";
      } else {
        destination = "/form/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("form", "FormTemplateRequestRouter.getDestination()",
        "root.MSG_GEN_EXIT_METHOD", "destination = " + destination);
    return destination;
  }

}