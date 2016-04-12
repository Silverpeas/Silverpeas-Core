/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.importexport.servlets;

import org.silverpeas.core.importexport.report.ExportPDFReport;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.web.importexport.control.ImportExportSessionController;

import org.silverpeas.core.util.file.FileUploadUtil;

import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.core.node.model.NodePK;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.web.http.HttpRequest;

import java.io.File;
import java.util.List;

public class ImportExportRequestRouter extends
    ComponentRequestRouter<ImportExportSessionController> {

  private static final long serialVersionUID = 1L;

  @Override
  public ImportExportSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ImportExportSessionController(mainSessionCtrl, componentContext,
        "org.silverpeas.importExportPeas.multilang.importExportPeasBundle",
        "org.silverpeas.importExportPeas.settings.importExportPeasIcons");
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  @Override
  public String getSessionControlBeanName() {
    return "importExportPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param importExportSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ImportExportSessionController importExportSC,
      HttpRequest request) {
    String destination = "";
    try {
      if (function.startsWith("Main")) {
        destination = "/importExportPeas/jsp/welcome.jsp";
      } else if ("Import".equals(function)) {
        File file = null;
        List<FileItem> items = request.getFileItems();
        for (FileItem item : items) {
          if (!item.isFormField()) {
            String fileName = FileUploadUtil.getFileName(item);
            file = new File(FileRepositoryManager.getTemporaryPath(null, null) + fileName);
            FileUploadUtil.saveToFile(file, item);
          }
        }
        ImportReport importReport =
            importExportSC.processImport(file.getAbsolutePath(), (MultiSilverpeasBundle) request.
            getAttribute("resources"));
        request.setAttribute("importReport", importReport);
        destination = "/importExportPeas/jsp/viewSPExchange.jsp";
      } else if ("SelectExportMode".equals(function)) {
        @SuppressWarnings("unchecked")
        List<WAAttributeValuePair> itemPKs =
            (List<WAAttributeValuePair>) request.getAttribute("selectedResultsWa");
        NodePK rootPK = (NodePK) request.getAttribute("RootPK");
        importExportSC.saveItems(itemPKs, rootPK);
        destination = "/importExportPeas/jsp/selectExportMode.jsp";
      } else if ("ExportSavedItems".equals(function)) {
        String mode = request.getParameter("ExportMode");
        importExportSC.processExportOfSavedItems(mode);
        destination = "/importExportPeas/jsp/pingExport.jsp";
      } else if ("ExportItems".equals(function)) {
        @SuppressWarnings("unchecked")
        List<WAAttributeValuePair> itemPKs = (List<WAAttributeValuePair>) request.getAttribute(
            "selectedResultsWa");
        NodePK rootPK = (NodePK) request.getAttribute("RootPK");
        if (itemPKs != null && !itemPKs.isEmpty()) {
          importExportSC.processExport(itemPKs, rootPK);
          destination = "/importExportPeas/jsp/pingExport.jsp";
        } else {
          destination = "/importExportPeas/jsp/nothingToExport.jsp";
        }
      } else if ("ExportItemsPing".equals(function)) {
        if (importExportSC.isExportInProgress()) {
          destination = "/importExportPeas/jsp/pingExport.jsp";
        } else {
          ExportReport report = importExportSC.getExportReport();
          request.setAttribute("ExportReport", report);
          destination = "/importExportPeas/jsp/downloadZip.jsp";
        }
      } else if ("ExportPDF".equals(function)) {
        @SuppressWarnings("unchecked")
        List<WAAttributeValuePair> itemPKs =
            (List<WAAttributeValuePair>) request.getAttribute("selectedResultsWa");

        if (itemPKs != null && !itemPKs.isEmpty()) {
          ExportPDFReport report = importExportSC.processExportPDF(itemPKs);

          if (report != null) {
            request.setAttribute("ExportPDFReport", report);
            destination = "/importExportPeas/jsp/downloadPdf.jsp";
          } else {
            destination = "/importExportPeas/jsp/nothingToExport.jsp";
          }

        } else {
          destination = "/importExportPeas/jsp/nothingToExport.jsp";
        }
      } else if (function.equals("KmaxExportComponent")) {
        @SuppressWarnings("unchecked")
        List<WAAttributeValuePair> itemPKs =
            (List<WAAttributeValuePair>) request.getAttribute("selectedResultsWa");
        if (itemPKs != null && !itemPKs.isEmpty()) {
          ExportReport report =
              importExportSC.processExportKmax(importExportSC.getLanguage(), itemPKs, null, null);
          request.setAttribute("ExportReport", report);
          destination = "/importExportPeas/jsp/downloadZip.jsp";
        } else {
          destination = "/importExportPeas/jsp/nothingToExport.jsp";
        }
      } else if (function.equals("KmaxExportPublications")) {
        @SuppressWarnings("unchecked")
        List<WAAttributeValuePair> itemPKs =
            (List<WAAttributeValuePair>) request.getAttribute("selectedResultsWa");
        List combination = (List) request.getAttribute("Combination");
        String timeCriteria = (String) request.getAttribute("TimeCriteria");

        if (itemPKs != null && !itemPKs.isEmpty()) {
          ExportReport report = importExportSC.processExportKmax(importExportSC.getLanguage(),
              itemPKs, combination, timeCriteria);
          request.setAttribute("ExportReport", report);
          destination = "/importExportPeas/jsp/downloadZip.jsp";
        } else {
          destination = "/importExportPeas/jsp/nothingToExport.jsp";
        }
      } else {
        destination = "/importExportPeas/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }
}
