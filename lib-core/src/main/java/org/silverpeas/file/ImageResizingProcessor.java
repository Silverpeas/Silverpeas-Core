package org.silverpeas.file;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.RestRequest;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.silverpeas.image.ImageTool;
import org.silverpeas.image.option.DimensionOption;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.silverpeas.image.ImageToolDirective.GEOMETRY_SHRINK;

/**
 * Unit tests on the image resizing. *
 * @author mmoquillon
 */
@Named("imageResizingProcessor")
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
        File imageSource =
            new File(image.getParentFile().getParent() + File.separator + image.getName());
        if (imageSource.exists() && (width != null || height != null)) {
          File imageDestination =
              new File(IMAGE_CACHE_PATH + parent + File.separator + imageSource.getName());
          parameters = new ResizingParameters(imageSource, imageDestination, width, height);
        }
      } catch (NumberFormatException ex) {
      }
    }
    return parameters;
  }

  private void removeResizedImagesOf(final File originaImage) {
    List<String> resizedImagePaths = ImageCache.getImages(originaImage.getAbsolutePath());
    for (String resizedImage : resizedImagePaths) {
      if (!(new File(resizedImage)).delete()) {
        Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING,
            "The resized image {0} for the in deletion original image {1} cannot be deleted!",
            new String[]{resizedImage, originaImage.getAbsolutePath()});
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
            DimensionOption dimension =
                DimensionOption.widthAndHeight(parameters.getWidth(), parameters.getHeight());
            imageTool
                .convert(sourceImage, resizedImage, dimension, GEOMETRY_SHRINK);
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
