package org.silverpeas.file;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.silverpeas.image.ImageTool;
import org.silverpeas.image.ImageToolDirective;
import org.silverpeas.image.option.DimensionOption;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;

/**
 * Unit tests on the image resizing.
 *
 * @author mmoquillon
 */
@Named("imageResizingProcessor")
public class ImageResizingProcessor implements SilverpeasFileProcessor {

  protected static final String IMAGE_CACHE_PATH = FileRepositoryManager.getAbsolutePath("cache");

  @Inject
  private ImageTool imageTool;

  @PostConstruct
  public void registerItself() {
    SilverpeasFileProvider fileFactory = SilverpeasFileProvider.getInstance();
    fileFactory.addProcessor(this);
  }

  @Override
  public String processBefore(final String path) {
    String imagePath = path;
    if (FileUtil.isImage(imagePath)) {
      File sourceImage = new File(imagePath);
      if (!sourceImage.exists()) {
        ResizingParameters parameters = computeResizingParameters(sourceImage);
        if (parameters.isDefined()) {
          sourceImage = parameters.getSourceImage();
          File resizedImage = parameters.getDestinationImage();
          imagePath = resizedImage.getPath();
          if (!resizedImage.exists() && sourceImage.exists()) {
            if (!resizedImage.getParentFile().exists()) {
              resizedImage.getParentFile().mkdirs();
            }
            DimensionOption dimension =
                DimensionOption.widthAndHeight(parameters.getWidth(), parameters.getHeight());
            imageTool
                .convert(sourceImage, resizedImage, dimension, ImageToolDirective.GEOMETRY_SHRINK);
          }
        }
      }
    }
    return imagePath;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file) {
    return file;
  }

  private ResizingParameters computeResizingParameters(File image) {
    ResizingParameters parameters = ResizingParameters.NO_RESIZING;
    String parent = image.getParentFile().getName();
    if (parent.contains("x")) {
      String[] size = parent.split("x");
      int width = -1;
      int height = -1;
      try {
        if (StringUtil.isDefined(size[0])) {
          width = Integer.valueOf(size[0]);
        }
        if (size.length == 2) {
          height = Integer.valueOf(size[1]);
        }
        File imageSource =
            new File(image.getParentFile().getParent() + File.separator + image.getName());
        if (imageSource.exists() && (width > -1 || height > -1)) {
          File imageDestination =
              new File(IMAGE_CACHE_PATH + parent + File.separator + imageSource.getName());
          parameters = new ResizingParameters(imageSource, imageDestination, width, height);
        }
      } catch (NumberFormatException ex) {
      }
    }
    return parameters;
  }

  private static class ResizingParameters {

    private static ResizingParameters NO_RESIZING = new ResizingParameters(null, null, -1, -1);

    private int width;
    private int height;
    private File sourceImage;
    private File destinationImage;

    public ResizingParameters(File sourceImage, File destinationImage, int width, int height) {
      this.height = height;
      this.width = width;
      this.sourceImage = sourceImage;
      this.destinationImage = destinationImage;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
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
