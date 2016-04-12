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

package org.silverpeas.core.io.media.image.thumbnail.control;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailException;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailRuntimeException;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailService;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailServiceProvider;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.file.FileFolderManager;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileDescriptor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.util.file.FileUploadUtil;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ThumbnailController implements ComponentInstanceDeletion {

  private static final SettingBundle publicationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.publication.publicationSettings");

  /**
   * the constructor.
   */
  public ThumbnailController() {
  }

  private static ThumbnailService getThumbnailService() {
    return ThumbnailServiceProvider.getThumbnailService();
  }

  @Transactional
  @Override
  public void delete(final String componentInstanceId) {

    // 1 - delete data in database
    try {
      getThumbnailService().deleteAllThumbnail(componentInstanceId);
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailServiceImpl.delete()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED", e);
    }

    // 2 - delete directory where files are stored
    try {
      FileFolderManager.deleteFolder(getImageDirectory(componentInstanceId));
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailServiceImpl.delete()",
          SilverpeasException.ERROR, "root.DELETING_DATA_DIRECTORY_FAILED", e);
    }
  }

  public static boolean processThumbnail(ForeignPK pk, String objectType, List<FileItem> parameters)
      throws Exception {
    boolean thumbnailChanged = false;
    String mimeType = null;
    String physicalName = null;
    FileItem uploadedFile = FileUploadUtil.getFile(parameters, "WAIMGVAR0");
    if (uploadedFile != null) {
      String logicalName = uploadedFile.getName().replace('\\', '/');
      if (StringUtil.isDefined(logicalName)) {
        logicalName = FilenameUtils.getName(logicalName);
        mimeType = FileUtil.getMimeType(logicalName);
        String type = FileRepositoryManager.getFileExtension(logicalName);
        if (FileUtil.isImage(logicalName)) {
          physicalName = String.valueOf(System.currentTimeMillis()) + '.' + type;
          SilverpeasFileDescriptor descriptor = new SilverpeasFileDescriptor(pk.getInstanceId())
              .mimeType(mimeType)
              .parentDirectory(publicationSettings.getString("imagesSubDirectory"))
              .fileName(physicalName);
          SilverpeasFile target = SilverpeasFileProvider.newFile(descriptor);
          target.writeFrom(uploadedFile.getInputStream());
        } else {
          throw new ThumbnailRuntimeException("ThumbnailController.processThumbnail()",
              SilverpeasRuntimeException.ERROR, "thumbnail_EX_MSG_WRONG_TYPE_ERROR");
        }
      }
    }

    // If no image have been uploaded, check if one have been picked up from a gallery
    if (physicalName == null) {
      // on a pas d'image, regarder s'il y a une provenant de la galerie
      String nameImageFromGallery = FileUploadUtil.getParameter(parameters, "valueImageGallery");
      if (StringUtil.isDefined(nameImageFromGallery)) {
        physicalName = nameImageFromGallery;
        mimeType = "image/jpeg";
      }
    }

    // If one image is defined, save it through Thumbnail service
    if (StringUtil.isDefined(physicalName)) {
      ThumbnailDetail detail = new ThumbnailDetail(pk.getInstanceId(),
          Integer.parseInt(pk.getId()),
          ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      detail.setOriginalFileName(physicalName);
      detail.setMimeType(mimeType);
      try {
        ThumbnailController.updateThumbnail(detail);
        thumbnailChanged = true;
      } catch (ThumbnailRuntimeException e) {
        SilverTrace.error("thumbnail", "KmeliaRequestRouter.processVignette",
            "thumbnail_MSG_UPDATE_THUMBNAIL_KO", e);
        try {
          ThumbnailController.deleteThumbnail(detail);
        } catch (Exception exp) {

        }
      }
    }
    return thumbnailChanged;
  }

  /**
   * To update thumbnails files informations.
   *
   * @param thumbDetail :ThumbnailDetail.
   * @author Sebastien ROCHET
   */
  public static void updateThumbnail(ThumbnailDetail thumbDetail) {

    try {
      ThumbnailDetail completeThumbnail = getThumbnailService().getCompleteThumbnail(thumbDetail);
      // first, delete former thumbnail
      if (completeThumbnail != null) {
        if (completeThumbnail.getCropFileName() != null) {
          deleteThumbnailFileOnServer(completeThumbnail.getInstanceId(), completeThumbnail
              .getCropFileName());
        }
        getThumbnailService().deleteThumbnail(thumbDetail);
      }
      thumbDetail.setCropFileName(null);
      thumbDetail.setXLength(-1);
      thumbDetail.setXStart(-1);
      thumbDetail.setYLength(-1);
      thumbDetail.setYStart(-1);
      getThumbnailService().createThumbnail(thumbDetail);
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailController.updateThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_UPDATE_THUMBNAIL_KO", e);
    }
  }

  public static void deleteThumbnail(ThumbnailDetail thumbDetail) {

    try {
      // delete the file on server
      ThumbnailDetail completeThumbnail = getThumbnailService().getCompleteThumbnail(thumbDetail);
      if (completeThumbnail != null) {
        if (completeThumbnail.getOriginalFileName() != null) {
          deleteThumbnailFileOnServer(completeThumbnail.getInstanceId(), completeThumbnail
              .getOriginalFileName());
        }
        if (completeThumbnail.getCropFileName() != null) {
          deleteThumbnailFileOnServer(completeThumbnail.getInstanceId(), completeThumbnail
              .getCropFileName());
        }
        getThumbnailService().deleteThumbnail(thumbDetail);
      }
    } catch (Exception fe) {
      throw new ThumbnailRuntimeException(
          "ThumbnailController.deleteThumbnail(ThumbnailDetail thumbDetail)",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_DELETE_THUMBNAIL_KO", fe);
    }
  }

  public static ThumbnailDetail createThumbnail(ThumbnailDetail thumbDetail, int thumbnailWidth,
      int thumbnailHeight) {
    try {
      // create line in db
      ThumbnailDetail thumdAdded = getThumbnailService().createThumbnail(thumbDetail);
      // create crop thumbnail
      if (thumdAdded.getCropFileName() == null && thumdAdded.isCropable()) {
        createCropFile(thumbnailWidth, thumbnailHeight, thumdAdded);
      }
      return thumdAdded;
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailController.createThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_CREATE_THUMBNAIL_KO", e);
    }
  }

  public static ThumbnailDetail getCompleteThumbnail(ThumbnailDetail thumbDetail) {
    try {
      // get thumbnail
      return getThumbnailService().getCompleteThumbnail(thumbDetail);
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailController.getCompleteThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_GET_COMPLETE_THUMBNAIL_KO", e);
    }
  }

  public static void copyThumbnail(ForeignPK fromPK, ForeignPK toPK) {
    ThumbnailDetail vignette =
        ThumbnailController.getCompleteThumbnail(new ThumbnailDetail(
            fromPK.getInstanceId(), Integer.parseInt(fromPK.getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE));
    try {
      if (vignette != null) {
        ThumbnailDetail thumbDetail =
            new ThumbnailDetail(toPK.getInstanceId(),
                Integer.valueOf(toPK.getId()),
                ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
        if (vignette.getOriginalFileName().startsWith("/")) {
          thumbDetail.setOriginalFileName(vignette.getOriginalFileName());
          thumbDetail.setMimeType(vignette.getMimeType());
        } else {
          String from = getImageDirectory(fromPK.getInstanceId()) + vignette.getOriginalFileName();

          String type = FilenameUtils.getExtension(vignette.getOriginalFileName());
          String newOriginalImage = String.valueOf(System.currentTimeMillis()) + "." + type;

          String to = getImageDirectory(toPK.getInstanceId()) + newOriginalImage;
          FileRepositoryManager.copyFile(from, to);
          thumbDetail.setOriginalFileName(newOriginalImage);

          // then copy thumbnail image if exists
          if (vignette.getCropFileName() != null) {
            from = getImageDirectory(fromPK.getInstanceId()) + vignette.getCropFileName();
            type = FilenameUtils.getExtension(vignette.getCropFileName());
            String newThumbnailImage = String.valueOf(System.currentTimeMillis()) + "." + type;
            to = getImageDirectory(toPK.getInstanceId()) + newThumbnailImage;
            FileRepositoryManager.copyFile(from, to);
            thumbDetail.setCropFileName(newThumbnailImage);
          }
          thumbDetail.setMimeType(vignette.getMimeType());
          thumbDetail.setXLength(vignette.getXLength());
          thumbDetail.setYLength(vignette.getYLength());
          thumbDetail.setXStart(vignette.getXStart());
          thumbDetail.setYStart(vignette.getYStart());
        }
        getThumbnailService().createThumbnail(thumbDetail);
      }
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailController.copyThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_CANT_COPY_THUMBNAIL", e);
    }
  }

  public static void moveThumbnail(ForeignPK fromPK, ForeignPK toPK) {
    ThumbnailDetail thumbnail =
      ThumbnailController.getCompleteThumbnail(new ThumbnailDetail(
          fromPK.getInstanceId(), Integer.parseInt(fromPK.getId()),
          ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE));
    try {
      if (thumbnail != null) {
        // move thumbnail on disk
        if (!thumbnail.getOriginalFileName().startsWith("/")) {
          String path = getImageDirectory(fromPK.getInstanceId()) + File.separatorChar +
              thumbnail.getOriginalFileName();
          String destinationPath = getImageDirectory(toPK.getInstanceId());
          SilverpeasFile image = SilverpeasFileProvider.getFile(path);
          image.moveInto(destinationPath);

          // move cropped thumbnail
          if (thumbnail.getCropFileName() != null) {
            path = getImageDirectory(fromPK.getInstanceId()) + File.separatorChar +
                thumbnail.getCropFileName();
            image = SilverpeasFileProvider.getFile(path);
            image.moveInto(destinationPath);
          }
        }

        // move thumbnail in DB
        getThumbnailService().moveThumbnail(thumbnail, toPK.getInstanceId());
      }
    } catch (Exception e) {
      throw new ThumbnailRuntimeException("ThumbnailController.moveThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_CANT_MOVE_THUMBNAIL", e);
    }
  }

  protected static void createCropThumbnailFileOnServer(String pathOriginalFile,
      String pathCropdir,
      String pathCropFile, ThumbnailDetail thumbnail, int thumbnailWidth, int thumbnailHeight) {
    try {
      // Creates folder if not exists
      File dir = new File(pathCropdir);
      if (!dir.exists()) {
        FileFolderManager.createFolder(pathCropdir);
      }
      // create empty file
      File cropFile = new File(pathCropFile);
      if (!cropFile.exists()) {
        cropFile.createNewFile();
      }

      File originalFile = new File(pathOriginalFile);
      BufferedImage bufferOriginal = ImageIO.read(originalFile);
      // crop image
      BufferedImage cropPicture = bufferOriginal.getSubimage(thumbnail.getXStart(),
          thumbnail.getYStart(), thumbnail.getXLength(), thumbnail.getYLength());
      BufferedImage cropPictureFinal = new BufferedImage(thumbnailWidth, thumbnailHeight,
          BufferedImage.TYPE_INT_RGB);
      // Redimensionnement de l'image
      Graphics2D g2 = cropPictureFinal.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.drawImage(cropPicture, 0, 0, thumbnailWidth, thumbnailHeight, null);
      g2.dispose();

      // save crop image
      String extension = FilenameUtils.getExtension(originalFile.getName());
      ImageIO.write(cropPictureFinal, extension, cropFile);
    } catch (Exception e) {
      SilverTrace.warn("thumbnail", "ThumbnailController.createThumbnailFileOnServer()",
          "thumbnail_MSG_CREATE_CROP_FILE_KO", "originalFileName=" +
          thumbnail.getOriginalFileName()
          + " cropFileName = " + thumbnail.getCropFileName(), e);
    }
  }

  private static void deleteThumbnailFileOnServer(String componentId, String fileName) {
    String path = getImageDirectory(componentId) + fileName;
    try {
      SilverpeasFile image = SilverpeasFileProvider.getFile(path);
      image.delete();
    } catch (Exception e) {
      SilverTrace.warn("thumbnail",
          "ThumbnailController.deleteThumbnailFileOnServer(String componentId, String fileName)",
          "thumbnail_MSG_NOT_DELETE_FILE", "filePath=" + path, e);
    }
  }

  public static String getImage(String instanceId, int objectId, int objectType) {
    ThumbnailDetail thumbDetail = new ThumbnailDetail(instanceId, objectId, objectType);
    // default size if creation
    String[] imageProps = getImageAndMimeType(thumbDetail);
    return imageProps[0];
  }

  public static String getImageMimeType(String instanceId, int objectId, int objectType) {
    ThumbnailDetail thumbDetail = new ThumbnailDetail(instanceId, objectId, objectType);

    // default size if creation
    String[] imageProps = getImageAndMimeType(thumbDetail);
    return imageProps[1];
  }

  /**
   *
   * @param thumbDetail
   * @return
   */
  public static String[] getImageAndMimeType(ThumbnailDetail thumbDetail) {
    try {
      ThumbnailDetail thumbDetailComplete = getThumbnailService().getCompleteThumbnail(thumbDetail);
      if (thumbDetailComplete != null) {
        if (thumbDetailComplete.getCropFileName() != null) {
          return new String[] { thumbDetailComplete.getCropFileName(),
              thumbDetailComplete.getMimeType() };
        } else {
          return new String[] { thumbDetailComplete.getOriginalFileName(),
              thumbDetailComplete.getMimeType() };
        }
      } else {
        // case no thumbnail define
        return new String[] { null, null };
      }
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(
          "ThumbnailController.getCompleteThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_GET_IMAGE_KO",
          e);
    }
  }

  public static ThumbnailDetail cropThumbnail(ThumbnailDetail thumbnail, int thumbnailWidth,
      int thumbnailHeight) {
    try {
      ThumbnailDetail thumbDetailComplete = getThumbnailService().getCompleteThumbnail(thumbnail);
      if (thumbDetailComplete.getCropFileName() != null) {
        // on garde toujours le meme nom de fichier par contre on le supprime
        // puis le recreer avec les nouvelles coordonnees
        deleteThumbnailFileOnServer(thumbnail.getInstanceId(), thumbDetailComplete
            .getCropFileName());
      } else {
        // case creation
        String extension = FilenameUtils.getExtension(thumbDetailComplete.getOriginalFileName());
        String cropFileName = String.valueOf(new Date().getTime()) + '.' + extension;
        thumbDetailComplete.setCropFileName(cropFileName);
      }
      String pathCropdir = getImageDirectory(thumbnail.getInstanceId());
      String pathOriginalFile = pathCropdir + thumbDetailComplete.getOriginalFileName();
      String pathCropFile = pathCropdir + thumbDetailComplete.getCropFileName();
      createCropThumbnailFileOnServer(pathOriginalFile, pathCropdir, pathCropFile,
          thumbnail, thumbnailWidth, thumbnailHeight);
      thumbDetailComplete.setXStart(thumbnail.getXStart());
      thumbDetailComplete.setXLength(thumbnail.getXLength());
      thumbDetailComplete.setYStart(thumbnail.getYStart());
      thumbDetailComplete.setYLength(thumbnail.getYLength());
      getThumbnailService().updateThumbnail(thumbDetailComplete);
      return thumbDetailComplete;
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(
          "ThumbnailController.cropThumbnail()",
          SilverpeasRuntimeException.ERROR, "thumbnail_MSG_GET_IMAGE_KO",
          e);
    }
  }

  private static void createCropFile(int thumbnailWidth, int thumbnailHeight,
      ThumbnailDetail thumbDetailComplete) throws IOException, ThumbnailException {

    String pathOriginalFile = getImageDirectory(thumbDetailComplete.getInstanceId())
        + thumbDetailComplete.getOriginalFileName();

    if (thumbnailWidth == -1 && thumbnailHeight != -1) {
      // crop with fix height
      String[] result =
          ImageUtil.getWidthAndHeightByHeight(new File(pathOriginalFile), thumbnailHeight);
      thumbnailWidth = Integer.valueOf(result[0]);
      thumbnailHeight = Integer.valueOf(result[1]);
    } else if (thumbnailHeight == -1 && thumbnailWidth != -1) {
      // crop with fix width
      String[] result =
          ImageUtil.getWidthAndHeightByWidth(new File(pathOriginalFile), thumbnailWidth);
      thumbnailWidth = Integer.valueOf(result[0]);
      thumbnailHeight = Integer.valueOf(result[1]);
    } else if (thumbnailHeight == -1) {
      // crop full file
      String[] result = ImageUtil.getWidthAndHeight(new File(pathOriginalFile));
      thumbnailWidth = Integer.valueOf(result[0]);
      thumbnailHeight = Integer.valueOf(result[1]);
    }

    String extension = FilenameUtils.getExtension(thumbDetailComplete.getOriginalFileName());
    // add 2 to be sure cropfilename is different from original filename
    String cropFileName = String.valueOf(new Date().getTime() + 2) + '.' + extension;
    thumbDetailComplete.setCropFileName(cropFileName);
    // crop sur l image entiere
    cropFromPath(pathOriginalFile, thumbDetailComplete, thumbnailHeight, thumbnailWidth);
  }

  protected static void cropFromPath(String pathOriginalFile, ThumbnailDetail thumbDetailComplete,
      int thumbnailHeight, int thumbnailWidth) throws IOException, ThumbnailException {
    File originalFile = new File(pathOriginalFile);
    BufferedImage bufferOriginal = ImageIO.read(originalFile);
    if (bufferOriginal == null) {
      SilverTrace.error("thumbnail", "ThumbnailController.cropFromPath(int thumbnailWidth, "
          + "int thumbnailHeight,ThumbnailDetail thumbDetailComplete)",
          "thumbnail.EX_MSG_NOT_AN_IMAGE", "pathOriginalFile=" + pathOriginalFile);
      throw new ThumbnailException("ThumbnailBmImpl.cropFromPath()",
          SilverpeasException.ERROR, "thumbnail.EX_MSG_NOT_AN_IMAGE");
    } else {
      thumbDetailComplete.setXStart(0);
      thumbDetailComplete.setYStart(0);
      thumbDetailComplete.setXLength(bufferOriginal.getWidth());
      thumbDetailComplete.setYLength(bufferOriginal.getHeight());

      String pathCropFile = getImageDirectory(thumbDetailComplete.getInstanceId())
          + thumbDetailComplete.getCropFileName();
      createCropThumbnailFileOnServer(pathOriginalFile,
          getImageDirectory(thumbDetailComplete.getInstanceId()), pathCropFile,
          thumbDetailComplete,
          thumbnailWidth, thumbnailHeight);
      getThumbnailService().updateThumbnail(thumbDetailComplete);
    }
  }

  protected static String getImageDirectory(String instanceId) {
    return FileRepositoryManager.getAbsolutePath(instanceId) + publicationSettings.getString(
        "imagesSubDirectory") + File.separatorChar;
  }
}