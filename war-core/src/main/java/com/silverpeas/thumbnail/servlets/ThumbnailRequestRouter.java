/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.thumbnail.servlets;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.thumbnail.ThumbnailRuntimeException;
import com.silverpeas.thumbnail.ThumbnailSessionController;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class ThumbnailRequestRouter extends ComponentRequestRouter {

  private static final ResourceLocator publicationSettings = new ResourceLocator(
          "com.stratelia.webactiv.util.publication.publicationSettings", "fr");
  private static final long serialVersionUID = -2685660972761271210L;

  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {

    String destination = "";
    if (!function.startsWith("images")) {

      ThumbnailSessionController thumbnailSC = (ThumbnailSessionController) componentSC;

      String action = getAction(request);

      List<FileItem> parameters = null;
      if (action == null) {
        try {
          parameters = FileUploadUtil.parseRequest(request);
        } catch (UtilException e) {
          SilverTrace.error("Thumbnail",
              "ThumbnailRequestRouter.getAction",
              "root.MSG_GEN_PARAM_VALUE", e);
        }
        action = FileUploadUtil.getParameter(parameters, "Action");
      }
      String result = null;
      String backUrl = request.getParameter("BackUrl");
      destination = backUrl;

      if ("Delete".equals(action)) {
        result = deleteThumbnail(request, thumbnailSC);
      } else if ("Add".equals(action)) {
        // Open the Add form
        request.setAttribute("action", "add");
        destination = "/thumbnail/jsp/thumbnailManager.jsp";
      } else if ("UpdateFile".equals(action)) {
        // Open the Update File form
        ThumbnailDetail thumbnailToUpdate = getThumbnail(request);
        request.setAttribute("thumbnaildetail", thumbnailToUpdate);
        if (thumbnailToUpdate == null) {
          result = "error";
        }
        request.setAttribute("action", "update");
        destination = "/thumbnail/jsp/thumbnailManager.jsp";
      } else if ("Save".equals(action)) {
        if (destination == null) {
          destination = FileUploadUtil.getParameter(parameters,
              "BackUrl");
        }
        result = createThumbnail(request, parameters, thumbnailSC);
      } else if ("SaveUpdateFile".equals(action)) {
        if (destination == null) {
          destination = FileUploadUtil.getParameter(parameters,
              "BackUrl");
        }
        result = updateFile(request, parameters, thumbnailSC);
      } else if ("Update".equals(action)) {
        ThumbnailDetail thumbnailToUpdate = getThumbnail(request);
        request.setAttribute("thumbnaildetail", thumbnailToUpdate);
        if (thumbnailToUpdate == null) {
          result = "error";
        }
        destination = "/thumbnail/jsp/thumbnailManager.jsp";
      } else if ("SaveUpdate".equals(action)) {
        result = updateThumbnail(request, thumbnailSC);
      } else if ("Crop".equals(action)) {
        result = cropThumbnail(request, thumbnailSC);
      }

      if (destination != null && result != null) {
        if (destination.indexOf("?") != -1) {
          destination = destination + "&resultThumbnail=" + result;
        } else {
          destination = destination + "?resultThumbnail=" + result;
        }
      }
    }

    return destination;

  }

  private String updateFile(HttpServletRequest req,
      List<FileItem> parameters, ThumbnailSessionController thumbnailSC) {

    // make some control before delete
    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    boolean runOnUnix = settings.getBoolean("runOnSolaris", false);
    FileItem item = FileUploadUtil.getFile(parameters, "OriginalFile");

    String type = null;
    String fullFileName = null;
    if (!item.isFormField()) {

      fullFileName = item.getName();
      if (fullFileName != null && runOnUnix) {
        fullFileName = fullFileName.replace('\\',
            File.separatorChar);
        SilverTrace.info("thumbnail",
            "ThumbnailRequestRouter.createAttachment",
            "root.MSG_GEN_PARAM_VALUE",
            "fullFileName on Unix = " + fullFileName);
      }

      String fileName = fullFileName
          .substring(
              fullFileName.lastIndexOf(File.separator) + 1,
              fullFileName.length());

      if (fileName.lastIndexOf(".") != -1) {
        type = fileName.substring(fileName.lastIndexOf(".") + 1,
            fileName.length());
      }

      // is there a type?
      if (type == null || type.length() == 0) {
        return "EX_MSG_NO_TYPE_ERROR";
      }

      // file type is correct?
      if (!type.equalsIgnoreCase("gif") && !type.equalsIgnoreCase("jpg")
          && !type.equalsIgnoreCase("jpeg") && !type.equalsIgnoreCase("png")) {
        return "EX_MSG_WRONG_TYPE_ERROR";
      }

    } else {
      return "error";
    }

    // parameters seems to be correct -> delete the old thumbnail and create the new one
    String objectId = FileUploadUtil.getParameter(parameters, "ObjectId");
    String componentId = FileUploadUtil.getParameter(parameters, "ComponentId");
    String objectType = FileUploadUtil.getParameter(parameters, "ObjectType");
    ThumbnailDetail thumbToDelete = new ThumbnailDetail(componentId,
        Integer.parseInt(objectId), Integer.parseInt(objectType));
    try {
      ThumbnailController.deleteThumbnail(thumbToDelete);
    } catch (Exception e) {
      return "failed";
    }
    return createThumbnail(req, parameters, thumbnailSC);
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private ThumbnailDetail getThumbnail(HttpServletRequest req) {
    String objectId = req.getParameter("ObjectId");
    String componentId = req.getParameter("ComponentId");
    String objectType = req.getParameter("ObjectType");
    ThumbnailDetail thumbToUpdate = new ThumbnailDetail(componentId,
        Integer.parseInt(objectId), Integer.parseInt(objectType));

    ThumbnailDetail result = null;

    try {
      result = ThumbnailController.getCompleteThumbnail(thumbToUpdate);
    } catch (ThumbnailRuntimeException e) {
      SilverTrace.error("Thumbnail",
          "ThumbnailRequestRouter.updateThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return result;
  }

  private String updateThumbnail(HttpServletRequest req,
      ThumbnailSessionController thumbnailSC) {
    String objectId = req.getParameter("ObjectId");
    String componentId = req.getParameter("ComponentId");
    String objectType = req.getParameter("ObjectType");
    ThumbnailDetail thumbToUpdate = new ThumbnailDetail(componentId,
        Integer.parseInt(objectId), Integer.parseInt(objectType));
    // ici les seul champ a updater
    String xStart = req.getParameter("XStart");
    thumbToUpdate.setXStart(Integer.parseInt(xStart));
    String yStart = req.getParameter("YStart");
    thumbToUpdate.setYStart(Integer.parseInt(yStart));
    String xLength = req.getParameter("XLength");
    thumbToUpdate.setXLength(Integer.parseInt(xLength));
    String yLength = req.getParameter("YLength");
    thumbToUpdate.setYLength(Integer.parseInt(yLength));

    String thumbnailHeight = req.getParameter("ThumbnailHeight");
    String thumbnailWidth = req.getParameter("ThumbnailWidth");

    try {
      ThumbnailController.updateThumbnail(thumbToUpdate, Integer
          .parseInt(thumbnailWidth), Integer
          .parseInt(thumbnailHeight));
      return null;
    } catch (ThumbnailRuntimeException e) {
      SilverTrace.error("Thumbnail",
          "ThumbnailRequestRouter.updateThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return "error";

  }

  private String cropThumbnail(HttpServletRequest req, ThumbnailSessionController thumbnailSC) {
    String objectId = req.getParameter("ObjectId");
    String componentId = req.getParameter("ComponentId");
    String objectType = req.getParameter("ObjectType");
    ThumbnailDetail thumbToUpdate = new ThumbnailDetail(componentId,
        Integer.parseInt(objectId), Integer.parseInt(objectType));
    // ici les seul champ a updater
    String xStart = req.getParameter("XStart");
    thumbToUpdate.setXStart(Integer.parseInt(xStart));
    String yStart = req.getParameter("YStart");
    thumbToUpdate.setYStart(Integer.parseInt(yStart));
    String xLength = req.getParameter("XLength");
    thumbToUpdate.setXLength(Integer.parseInt(xLength));
    String yLength = req.getParameter("YLength");
    thumbToUpdate.setYLength(Integer.parseInt(yLength));

    String thumbnailHeight = req.getParameter("ThumbnailHeight");
    String thumbnailWidth = req.getParameter("ThumbnailWidth");

    try {
      ThumbnailController.cropThumbnail(thumbToUpdate, Integer
          .parseInt(thumbnailWidth), Integer
          .parseInt(thumbnailHeight));
      return null;
    } catch (ThumbnailRuntimeException e) {
      SilverTrace.error("Thumbnail",
          "ThumbnailRequestRouter.updateThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return "error";

  }

  private String createThumbnail(HttpServletRequest req,
      List<FileItem> parameters, ThumbnailSessionController thumbnailSC) {
    // save file on disk
    ThumbnailDetail thumb = null;
    try {
      thumb = saveFile(req, parameters, thumbnailSC);
    } catch (ThumbnailRuntimeException e) {
      // only one case -> no .type for the file
      SilverTrace.info("Thumbnail",
          "ThumbnailRequestRouter.addThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
      return "EX_MSG_NO_TYPE_ERROR";
    } catch (Exception exp) {
      SilverTrace.info("Thumbnail",
          "ThumbnailRequestRouter.addThumbnail",
          "root.MSG_GEN_PARAM_VALUE", exp);
      return "EX_MSG_SAVE_FILE_ERROR";
    }

    String thumbnailHeight = FileUploadUtil.getParameter(parameters,
        "ThumbnailHeight");
    String thumbnailWidth = FileUploadUtil.getParameter(parameters,
        "ThumbnailWidth");
    int intThumbnailHeight = -1;
    if (thumbnailHeight != null) {
      intThumbnailHeight = Integer.parseInt(thumbnailHeight);
    }

    int intThumbnailWidth = -1;
    if (thumbnailWidth != null) {
      intThumbnailWidth = Integer.parseInt(thumbnailWidth);
    }

    // create line in db
    try {

      if (ThumbnailController.createThumbnail(thumb,
          intThumbnailWidth,
          intThumbnailHeight) != null) {
        return null;
      } else {
        return "error";
      }

    } catch (ThumbnailRuntimeException e) {
      SilverTrace.error("Thumbnail",
          "ThumbnailRequestRouter.addThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
      // need remove the file on disk
      try {
        ThumbnailController.deleteThumbnail(thumb);
      } catch (Exception exp) {
        SilverTrace.info("Thumbnail",
            "ThumbnailRequestRouter.addThumbnail - remove after error",
            "root.MSG_GEN_PARAM_VALUE", exp);
      }
      return "EX_MSG_NOT_AN_IMAGE";
    }
  }

  private ThumbnailDetail saveFile(HttpServletRequest req,
      List<FileItem> parameters, ThumbnailSessionController thumbnailSC) throws Exception {
    SilverTrace.info("thumbnail",
        "ThumbnailRequestRouter.createAttachment",
        "root.MSG_GEN_ENTER_METHOD");

    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    boolean runOnUnix = settings.getBoolean("runOnSolaris", false);

    SilverTrace.info("thumbnail",
        "ThumbnailRequestRouter.createAttachment",
        "root.MSG_GEN_PARAM_VALUE", "runOnUnix = " + runOnUnix);

    String componentId = FileUploadUtil.getParameter(parameters,
        "ComponentId");
    SilverTrace.info("thumbnail",
        "ThumbnailRequestRouter.createAttachment",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    String id = FileUploadUtil.getParameter(parameters, "ObjectId");
    SilverTrace.info("thumbnail",
        "ThumbnailRequestRouter.createAttachment",
        "root.MSG_GEN_PARAM_VALUE", "id = " + id);

    FileItem item = FileUploadUtil.getFile(parameters, "OriginalFile");

    String fullFileName = null;
    if (!item.isFormField()) {

      fullFileName = item.getName();
      if (fullFileName != null && runOnUnix) {
        fullFileName = fullFileName.replace('\\',
            File.separatorChar);
        SilverTrace.info("thumbnail",
            "ThumbnailRequestRouter.createAttachment",
            "root.MSG_GEN_PARAM_VALUE",
            "fullFileName on Unix = " + fullFileName);
      }

      String fileName = fullFileName
          .substring(
              fullFileName.lastIndexOf(File.separator) + 1,
              fullFileName.length());
      SilverTrace.info("thumbnail",
          "ThumbnailRequestRouter.createAttachment",
          "root.MSG_GEN_PARAM_VALUE", "file = " + fileName);

      long size = item.getSize();
      SilverTrace.info("thumbnail",
          "ThumbnailRequestRouter.createAttachment",
          "root.MSG_GEN_PARAM_VALUE", "size = " + size);

      String type = null;
      if (fileName.lastIndexOf(".") != -1) {
        type = fileName.substring(fileName.lastIndexOf(".") + 1,
            fileName.length());
      }

      if (type == null || type.length() == 0) {
        throw new ThumbnailRuntimeException(
                  "ThumbnailRequestRouter.saveFile()",
                  SilverpeasRuntimeException.ERROR, "thumbnail_MSG_TYPE_KO");
      }

      String physicalName = new Long(new Date().getTime()).toString()
          + "." + type;

      String filePath = FileRepositoryManager
          .getAbsolutePath(componentId)
          + publicationSettings.getString("imagesSubDirectory") + File.separator + physicalName;
      File file = new File(filePath);

      if (!file.exists()) {
        FileFolderManager.createFolder(file.getParentFile());
        file.createNewFile();
      }

      item.write(file);
      String mimeType = AttachmentController.getMimeType(fileName);

      String objectType = FileUploadUtil.getParameter(parameters, "ObjectType");
      ThumbnailDetail thumbToAdd = new ThumbnailDetail(componentId,
          Integer.valueOf(id), Integer.valueOf(objectType));
      thumbToAdd.setOriginalFileName(physicalName);
      thumbToAdd.setMimeType(mimeType);

      return thumbToAdd;
    }
    return null;
  }

  private String deleteThumbnail(HttpServletRequest req,
      ThumbnailSessionController thumbnailSC) {
    String objectId = req.getParameter("ObjectId");
    String componentId = req.getParameter("ComponentId");
    String objectType = req.getParameter("ObjectType");
    ThumbnailDetail thumbToDelete = new ThumbnailDetail(componentId,
        Integer.parseInt(objectId), Integer.parseInt(objectType));
    try {
      ThumbnailController.deleteThumbnail(thumbToDelete);
      return null;
    } catch (Exception e) {
      return "failed";
    }
  }

  @Override
  public ComponentSessionController createComponentSessionController(
          MainSessionController mainSessionCtrl, ComponentContext context) {
    return new ThumbnailSessionController(mainSessionCtrl, context);
  }

  @Override
  public String getSessionControlBeanName() {
    return "thumbnail";
  }

}