/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.io.media.image.thumbnail.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileDescriptor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.option.OrientationOption;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailException;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailRuntimeException;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailService;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailServiceProvider;
import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.io.media.image.ImageToolDirective.GEOMETRY_SHRINK;
import static org.silverpeas.core.io.media.image.ImageToolDirective.PREVIEW_WORK;
import static org.silverpeas.core.io.media.image.option.CropOption.crop;
import static org.silverpeas.core.io.media.image.option.DimensionOption.widthAndHeight;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.file.FileUploadUtil.getParameter;

@Service
public class ThumbnailController implements ComponentInstanceDeletion {

  private static final SettingBundle publicationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.publication.publicationSettings");

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
      throw new ThumbnailRuntimeException(e);
    }

    // 2 - delete directory where files are stored
    try {
      FileFolderManager.deleteFolder(getImageDirectory(componentInstanceId));
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(e);
    }
  }

  public static boolean processThumbnail(ResourceReference pk, List<FileItem> parameters)
      throws IOException {
    ThumbnailDetail detail = new ThumbnailDetail(pk.getInstanceId(),
        Integer.parseInt(pk.getId()),
        ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
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
          SilverpeasFileDescriptor descriptor = new SilverpeasFileDescriptor(detail.getInstanceId())
              .mimeType(mimeType)
              .parentDirectory(publicationSettings.getString("imagesSubDirectory"))
              .fileName(physicalName);
          SilverpeasFile target = SilverpeasFileProvider.newFile(descriptor);
          target.writeFrom(uploadedFile.getInputStream());
        } else {
          throw new ThumbnailRuntimeException("Not an image");
        }
      }
    }
    // If no image have been uploaded, check if one have been picked up from a gallery
    if (physicalName == null) {
      // on a pas d'image, regarder s'il y a une provenant de la galerie
      String nameImageFromGallery = getParameter(parameters, "valueImageGallery");
      if (StringUtil.isDefined(nameImageFromGallery)) {
        physicalName = nameImageFromGallery;
        mimeType = "image/jpeg";
      }
    }
    // If one image is defined, save it through Thumbnail service
    final boolean thumbnailChanged;
    if (StringUtil.isDefined(physicalName)) {
      detail.setOriginalFileName(physicalName);
      detail.setMimeType(mimeType);
      thumbnailChanged = changeThumbnail(detail);
    } else if (getBooleanValue(getParameter(parameters, "ThumbnailDeletion"))) {
      deleteThumbnail(detail);
      thumbnailChanged = true;
    } else {
      thumbnailChanged = false;
    }
    // cropping if requested
    final Integer xStart = getParameterAsInteger(parameters, "XStart");
    if (xStart != null) {
      detail.setXStart(xStart);
      detail.setYStart(getParameterAsInteger(parameters, "YStart"));
      detail.setXLength(getParameterAsInteger(parameters, "XLength"));
      detail.setYLength(getParameterAsInteger(parameters, "YLength"));
      cropThumbnail(detail, getParameterAsInteger(parameters, "ThumbnailWidth"),
          getParameterAsInteger(parameters, "ThumbnailHeight"));
    }
    return thumbnailChanged;
  }

  private static Integer getParameterAsInteger(final List<FileItem> parameters,
      final String parameterName) {
    return ofNullable(getParameter(parameters, parameterName)).map(Integer::parseInt).orElse(null);
  }

  private static boolean changeThumbnail(final ThumbnailDetail detail) {
    try {
      ThumbnailController.updateThumbnail(detail);
      return true;
    } catch (ThumbnailRuntimeException e) {
      SilverLogger.getLogger(ThumbnailController.class).error(e);
      try {
        ThumbnailController.deleteThumbnail(detail);
      } catch (Exception exp) {
        SilverLogger.getLogger(ThumbnailController.class).silent(e);
      }
    }
    return false;
  }

  /**
   * To update thumbnails files informations.
   *
   * @param toUpdate contains the data to update.
   * @author Sebastien ROCHET
   */
  public static void updateThumbnail(final ThumbnailDetail toUpdate) {
    try {
      // first deleting previous
      deleteThumbnail(toUpdate);
      // then creating a new entry
      toUpdate.setCropFileName(null);
      toUpdate.setXLength(-1);
      toUpdate.setXStart(-1);
      toUpdate.setYLength(-1);
      toUpdate.setYStart(-1);
      getThumbnailService().createThumbnail(toUpdate);
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(e);
    }
  }

  /**
   * Deletes the given thumbnail.
   * @param toDelete the instance representing the thumbnail to delete.
   */
  public static void deleteThumbnail(final ThumbnailDetail toDelete) {
    try {
      // delete the file on server
      final ThumbnailDetail current = getThumbnailService().getCompleteThumbnail(toDelete);
      if (current != null) {
        if (current.getOriginalFileName() != null) {
          deleteThumbnailFileOnServer(current.getInstanceId(), current
              .getOriginalFileName());
        }
        if (current.getCropFileName() != null) {
          deleteThumbnailFileOnServer(current.getInstanceId(), current
              .getCropFileName());
        }
        getThumbnailService().deleteThumbnail(toDelete);
      }
    } catch (Exception fe) {
      throw new ThumbnailRuntimeException(fe);
    }
  }

  public static ThumbnailDetail createThumbnail(ThumbnailDetail thumbDetail, int thumbnailWidth,
      int thumbnailHeight) {
    try {
      // create line in db
      ThumbnailDetail thumdAdded = getThumbnailService().createThumbnail(thumbDetail);
      // create crop thumbnail
      if (thumdAdded.getCropFileName() == null && thumdAdded.canBeCropped()) {
        createCropFile(thumbnailWidth, thumbnailHeight, thumdAdded);
      }
      return thumdAdded;
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(e);
    }
  }

  public static ThumbnailDetail getCompleteThumbnail(ThumbnailDetail thumbDetail) {
    try {
      // get thumbnail
      return getThumbnailService().getCompleteThumbnail(thumbDetail);
    } catch (Exception e) {
      throw new ThumbnailRuntimeException(e);
    }
  }

  public static void copyThumbnail(ResourceReference fromPK, ResourceReference toPK) {
    ThumbnailDetail vignette =
        ThumbnailController.getCompleteThumbnail(new ThumbnailDetail(
            fromPK.getInstanceId(), Integer.parseInt(fromPK.getId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE));
    try {
      if (vignette != null) {
        ThumbnailDetail thumbDetail =
            new ThumbnailDetail(toPK.getInstanceId(),
                Integer.parseInt(toPK.getId()),
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
      throw new ThumbnailRuntimeException(e);
    }
  }

  public static void moveThumbnail(ResourceReference fromPK, ResourceReference toPK) {
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
      throw new ThumbnailRuntimeException(e);
    }
  }

  protected static void createCropThumbnailFileOnServer(String pathOriginalFile, String pathCropdir,
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
        Files.createFile(cropFile.toPath());
      }
      File originalFile = new File(pathOriginalFile);
      if (!croppingWithImageTool(thumbnail, originalFile, cropFile, thumbnailWidth, thumbnailHeight)) {
        croppingWithImageIO(thumbnail, originalFile, cropFile, thumbnailWidth, thumbnailHeight);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(ThumbnailController.class).warn(e);
    }
  }

  /**
   * Crops the original image of a thumbnail using the given parameters and the {@link ImageIO}
   * API.
   * @param thumbnail the thumbnail details.
   * @param originalFile the physical original image file.
   * @param cropFile the physical image file of the cropping result.
   * @param thumbnailWidth the width of the thumbnail.
   * @param thumbnailHeight the height of the thumbnail.
   */
  private static void croppingWithImageIO(final ThumbnailDetail thumbnail, final File originalFile,
      final File cropFile, final int thumbnailWidth, final int thumbnailHeight) throws IOException {
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
  }

  /**
   * Crops the original image of a thumbnail using the given parameters and the {@link ImageTool}
   * API.
   * @param thumbnail the thumbnail details.
   * @param originalFile the physical original image file.
   * @param cropFile the physical image file of the cropping result.
   * @param thumbnailWidth the width of the thumbnail.
   * @param thumbnailHeight the height of the thumbnail.
   * @return a boolean true to indicate a successful processing, false otherwise.
   */
  private static boolean croppingWithImageTool(final ThumbnailDetail thumbnail,
      final File originalFile, final File cropFile, final int thumbnailWidth,
      final int thumbnailHeight) {
    final ImageTool imageTool = ImageTool.get();
    if (imageTool.isActivated()) {
      try {
        imageTool.convert(originalFile, cropFile,
            crop(thumbnail.getXLength(), thumbnail.getYLength())
                .withOffset(thumbnail.getXStart(), thumbnail.getYStart()));
        imageTool.convert(cropFile, cropFile,
            Set.of(OrientationOption.auto(), widthAndHeight(thumbnailWidth, thumbnailHeight)),
            PREVIEW_WORK, GEOMETRY_SHRINK);
      } catch (Exception e) {
        SilverLogger.getLogger(ThumbnailController.class).warn(e);
      }
    }
    return cropFile.length() != 0;
  }

  private static void deleteThumbnailFileOnServer(String componentId, String fileName) {
    String path = getImageDirectory(componentId) + fileName;
    try {
      SilverpeasFile image = SilverpeasFileProvider.getFile(path);
      image.delete();
    } catch (Exception e) {
      SilverLogger.getLogger(ThumbnailController.class).warn(e);
    }
  }

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
      throw new ThumbnailRuntimeException(e);
    }
  }

  private static ThumbnailDetail cropThumbnail(ThumbnailDetail thumbnail, int thumbnailWidth,
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
      throw new ThumbnailRuntimeException(e);
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
      thumbnailWidth = Integer.parseInt(result[0]);
      thumbnailHeight = Integer.parseInt(result[1]);
    } else if (thumbnailHeight == -1 && thumbnailWidth != -1) {
      // crop with fix width
      String[] result =
          ImageUtil.getWidthAndHeightByWidth(new File(pathOriginalFile), thumbnailWidth);
      thumbnailWidth = Integer.parseInt(result[0]);
      thumbnailHeight = Integer.parseInt(result[1]);
    } else if (thumbnailHeight == -1) {
      // crop full file
      String[] result = ImageUtil.getWidthAndHeight(new File(pathOriginalFile));
      thumbnailWidth = Integer.parseInt(result[0]);
      thumbnailHeight = Integer.parseInt(result[1]);
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
      throw new ThumbnailException("Not an image");
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