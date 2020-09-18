/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.file;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.io.media.image.option.OrientationOption;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static org.silverpeas.core.io.media.image.ImageToolDirective.GEOMETRY_SHRINK;
import static org.silverpeas.core.util.CollectionUtil.asSet;

/**
 * A processor dedicated to resize an image on the demand. If the image is already resized, then
 * nothing the resized image is just returned.
 *
 * @author mmoquillon
 */
@Service
public class ImageResizingProcessor extends AbstractSilverpeasFileProcessor {

  protected static final String IMAGE_CACHE_PATH = FileRepositoryManager.getAbsolutePath("cache");

  @Inject
  private ImageTool imageTool;

  @Override
  public String processBefore(final String path, ProcessingContext context) {
    String imagePath = path;
    if (context == ProcessingContext.GETTING) {
      imagePath = resizeImage(path);
    }
    return imagePath;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file, ProcessingContext context) {
    if (context == ProcessingContext.DELETION || context == ProcessingContext.MOVING) {
      removeResizedImagesOf(file);
    }
    return file;
  }

  private ResizingParameters computeResizingParameters(File image) {
    ResizingParameters parameters = ResizingParameters.NO_RESIZING;
    String parent = image.getParentFile().getName();
    if (parent.contains("x")) {
      String[] size = parent.split("x");
      Integer width = null;
      Integer height = null;
      try {
        if (StringUtil.isDefined(size[0])) {
          width = Integer.valueOf(size[0]);
        }
        if (size.length == 2) {
          height = Integer.valueOf(size[1]);
        }
        final File imageSource = new File(image.getParentFile().getParent(), image.getName());
        if (imageSource.exists() && (width != null || height != null)) {
          final String fileName = imageSource.lastModified() + "_" + imageSource.getName();
          final File imageDestination = new File(IMAGE_CACHE_PATH + parent, fileName);
          parameters = new ResizingParameters(imageSource, imageDestination, width, height);
        }
      } catch (NumberFormatException ignore) {
      }
    }
    return parameters;
  }

  private void removeResizedImagesOf(final File originaImage) {
    List<String> resizedImagePaths = ImageCache.getImages(originaImage.getAbsolutePath());
    for (String resizedImage : resizedImagePaths) {
      if (!(new File(resizedImage)).delete()) {
        SilverLogger.getLogger(this).warn(
            "The resized image {0} for the in deletion original image {1} cannot be deleted!",
            resizedImage, originaImage.getAbsolutePath());
      }
    }
  }

  private String resizeImage(final String path) {
    String imagePath = path;
    if (FileUtil.isImage(imagePath)) {
      File sourceImage = new File(imagePath);
      if (!sourceImage.exists()) {
        ResizingParameters parameters = computeResizingParameters(sourceImage);
        if (parameters.isDefined()) {
          sourceImage = parameters.getSourceImage();
          File resizedImage = parameters.getDestinationImage();
          imagePath = resizedImage.getPath();
          if (sourceImage.exists() && (!resizedImage.exists() ||
              sourceImage.lastModified() >= resizedImage.lastModified())) {
            if (!resizedImage.getParentFile().exists()) {
              resizedImage.getParentFile().mkdirs();
            }
            final DimensionOption dimension =
                DimensionOption.widthAndHeight(parameters.getWidth(), parameters.getHeight());
            final OrientationOption auto = OrientationOption.auto();
            imageTool.convert(sourceImage, resizedImage, asSet(dimension, auto), GEOMETRY_SHRINK);
            ImageCache.putImage(sourceImage.getAbsolutePath(), resizedImage.getAbsolutePath());
          }
        }
      }
    }
    return imagePath;
  }

  private static class ResizingParameters {

    private static ResizingParameters NO_RESIZING = new ResizingParameters(null, null, -1, -1);

    private Integer width;
    private Integer height;
    private File sourceImage;
    private File destinationImage;

    public ResizingParameters(File sourceImage, File destinationImage, Integer width,
        Integer height) {
      this.height = height;
      this.width = width;
      this.sourceImage = sourceImage;
      this.destinationImage = destinationImage;
    }

    public Integer getWidth() {
      return width;
    }

    public Integer getHeight() {
      return height;
    }

    public File getSourceImage() {
      return sourceImage;
    }

    public File getDestinationImage() {
      return destinationImage;
    }

    public boolean isDefined() {
      return this != NO_RESIZING;
    }
  }
}
