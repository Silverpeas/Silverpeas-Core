/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.thumbnail.servlets;

import org.silverpeas.core.io.media.image.thumbnail.ThumbnailRuntimeException;
import org.silverpeas.web.thumbnail.ThumbnailSessionController;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

public class ThumbnailRequestRouter extends ComponentRequestRouter<ThumbnailSessionController> {

  private static final SettingBundle publicationSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.publication.publicationSettings");
  private static final long serialVersionUID = -2685660972761271210L;

  @Override
  public String getDestination(String function, ThumbnailSessionController thumbnailSC,
      HttpRequest request) {
    String destination = "";
    if (!function.startsWith("images")) {
      String action = getAction(request);

      List<FileItem> parameters = null;
      if (action == null) {
        try {
          parameters = request.getFileItems();
        } catch (UtilException e) {
          SilverTrace.error("thumbnail", "ThumbnailRequestRouter.getAction",
              "root.MSG_GEN_PARAM_VALUE", e);
        }
        action = FileUploadUtil.getParameter(parameters, "Action");
      }
      String result = null;
      destination = request.getParameter("BackUrl");

      if ("Delete".equals(action)) {
        result = deleteThumbnail(request);
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
          destination = FileUploadUtil.getParameter(parameters, "BackUrl");
        }
        result = createThumbnail(parameters);
      } else if ("SaveUpdateFile".equals(action)) {
        if (destination == null) {
          destination = FileUploadUtil.getParameter(parameters, "BackUrl");
        }
        result = updateFile(parameters);
      } else if ("Update".equals(action)) {
        ThumbnailDetail thumbnailToUpdate = getThumbnail(request);
        request.setAttribute("thumbnaildetail", thumbnailToUpdate);
        if (thumbnailToUpdate == null) {
          result = "error";
        }
        destination = "/thumbnail/jsp/thumbnailManager.jsp";
      } else if ("SaveUpdate".equals(action)) {
        result = updateThumbnail(request);
      } else if ("Crop".equals(action)) {
        result = cropThumbnail(request);
      }
      if (destination != null && result != null) {
        if (destination.indexOf('?') != -1) {
          destination = destination + "&resultThumbnail=" + result;
        } else {
          destination = destination + "?resultThumbnail=" + result;
        }
      }
    }
    return destination;

  }

  private String updateFile(List<FileItem> parameters) {
    FileItem item = FileUploadUtil.getFile(parameters, "OriginalFile");
    if (!item.isFormField()) {
      String fileName = FileUtil.getFilename(item.getName());
      if (!FileUtil.isImage(fileName)) {
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
    return createThumbnail(parameters);
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
      SilverTrace.error("thumbnail",
          "ThumbnailRequestRouter.updateThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return result;
  }

  private String updateThumbnail(HttpServletRequest req) {
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

    try {
      ThumbnailController.updateThumbnail(thumbToUpdate);
      return null;
    } catch (ThumbnailRuntimeException e) {
      SilverTrace.error("thumbnail",
          "ThumbnailRequestRouter.updateThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return "error";

  }

  private String cropThumbnail(HttpServletRequest req) {
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
      SilverTrace.error("thumbnail",
          "ThumbnailRequestRouter.updateThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
    return "error";

  }

  private String createThumbnail(List<FileItem> parameters) {
    // save file on disk
    ThumbnailDetail thumb;
    try {
      thumb = saveFile(parameters);
    } catch (ThumbnailRuntimeException e) {
      // only one case -> no .type for the file

      return "EX_MSG_NO_TYPE_ERROR";
    } catch (Exception exp) {

      return "EX_MSG_SAVE_FILE_ERROR";
    }

    String thumbnailHeight = FileUploadUtil.getParameter(parameters, "ThumbnailHeight");
    String thumbnailWidth = FileUploadUtil.getParameter(parameters, "ThumbnailWidth");
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
      if (ThumbnailController.createThumbnail(thumb, intThumbnailWidth, intThumbnailHeight) != null) {
        return null;
      }
      return "error";
    } catch (ThumbnailRuntimeException e) {
      SilverTrace.error("thumbnail", "ThumbnailRequestRouter.addThumbnail",
          "root.MSG_GEN_PARAM_VALUE", e);
      // need remove the file on disk
      try {
        ThumbnailController.deleteThumbnail(thumb);
      } catch (Exception exp) {

      }
      return "EX_MSG_NOT_AN_IMAGE";
    }
  }

  private ThumbnailDetail saveFile(List<FileItem> parameters) throws Exception {


    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Attachment");
    boolean runOnUnix = settings.getBoolean("runOnSolaris", false);



    String componentId = FileUploadUtil.getParameter(parameters,
        "ComponentId");

    String id = FileUploadUtil.getParameter(parameters, "ObjectId");


    FileItem item = FileUploadUtil.getFile(parameters, "OriginalFile");

    String fullFileName;
    if (!item.isFormField()) {

      fullFileName = item.getName();
      if (fullFileName != null && runOnUnix) {
        fullFileName = fullFileName.replace('\\',
            File.separatorChar);
      }

      assert fullFileName != null;
      String fileName = fullFileName
          .substring(
          fullFileName.lastIndexOf(File.separator) + 1,
          fullFileName.length());

      long size = item.getSize();

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

      String physicalName = System.currentTimeMillis() + "." + type;

      String filePath = FileRepositoryManager.getAbsolutePath(componentId)
          + publicationSettings.getString("imagesSubDirectory") + File.separator + physicalName;
      File file = new File(filePath);

      if (!file.exists()) {
        FileFolderManager.createFolder(file.getParentFile());
        file.createNewFile();
      }

      item.write(file);
      String mimeType = FileUtil.getMimeType(fileName);

      String objectType = FileUploadUtil.getParameter(parameters, "ObjectType");
      ThumbnailDetail thumbToAdd = new ThumbnailDetail(componentId,
          Integer.valueOf(id), Integer.valueOf(objectType));
      thumbToAdd.setOriginalFileName(physicalName);
      thumbToAdd.setMimeType(mimeType);

      return thumbToAdd;
    }
    return null;
  }

  private String deleteThumbnail(HttpServletRequest req) {
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
  public ThumbnailSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new ThumbnailSessionController(mainSessionCtrl, context);
  }

  @Override
  public String getSessionControlBeanName() {
    return "thumbnail";
  }

}
