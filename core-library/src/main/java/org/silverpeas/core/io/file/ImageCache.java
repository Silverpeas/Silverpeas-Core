package org.silverpeas.core.io.file;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * It represents a cache of the images that were resized by the
 * {@link ImageResizingProcessor} processor. In fact, it doesn't contain
 * the images themselves but their location in the file system. It is used by the image resizing
 * processor as a table of resized images in order to keep a trace of them for management purpose.
 * @author mmoquillon
 */
class ImageCache {

  private static final String IMAGE_CACHE_TABLE =
      ImageResizingProcessor.IMAGE_CACHE_PATH + File.separatorChar + ".data";

  /**
   * Puts an entry into the cache for the specified resized image of the specified original image.
   * @param originalImagePath the absolute path of the original image from which the resized image
   * was made.
   * @param resizedImagePath the absolute path of the resized image for which an entry will be
   * created into the cache.
   */
  protected static void putImage(final String originalImagePath, final String resizedImagePath) {
    String entryName = hash(originalImagePath);
    File entry = new File(IMAGE_CACHE_TABLE, entryName);
    try {
      List<String> lines = new ArrayList<String>();
      if (!entry.exists()) {
        lines.add(originalImagePath);
      }
      lines.add(resizedImagePath);
      FileUtils.writeLines(entry, lines, true);
    } catch (IOException ex) {
      SilverLogger.getLogger(ImageCache.class)
          .error("Cannot write the cache entry {0} with value {1}. Cause: {2}",
              new String[]{entry.getAbsolutePath(), resizedImagePath, ex.getMessage()});
    }
  }

  /**
   * Removes all the resized images of the specified original ones from the cache. If no resized
   * images exist for the specified one, then nothing is performed.
   * @param originalImagePaths the path of one or more original images for which the resized images
   * has to be removed from the cache.
   */
  protected static void removeImages(String... originalImagePaths) {
    for (String anOriginalImage : originalImagePaths) {
      String entryName = hash(anOriginalImage);
      File entry = new File(IMAGE_CACHE_TABLE, entryName);
      if (entry.exists()) {
        try {
          List<String> lines = FileUtils.readLines(entry);
          for (String resizedImagePath : lines.subList(1, lines.size())) {
            File resizedImage = new File(resizedImagePath);
            if (resizedImage.exists()) {
              if (!resizedImage.delete()) {
                SilverLogger.getLogger(ImageCache.class)
                    .warn("Cannot remove {0} from the cache entry {1}",
                        resizedImage.getAbsolutePath(), entry.getAbsolutePath());
              }
            }
          }
          if (!entry.delete()) {
            SilverLogger.getLogger(ImageCache.class)
                .warn("Cannot delete the cache entry {0}", entry.getAbsolutePath());
          }
        } catch (IOException ex) {
          SilverLogger.getLogger(ImageCache.class).error(ex.getMessage(), ex.getMessage());
        }
      }
    }
  }

  /**
   * Gets all the resized images of the specified original image.
   * @param originalImagePath the absolute path of the original image.
   * @return an array with the path of the images resized from the specified one.
   */
  protected static List<String> getImages(String originalImagePath) {
    String entryName = hash(originalImagePath);
    File entry = new File(IMAGE_CACHE_TABLE, entryName);
    if (entry.exists()) {
      try {
        List<String> lines = FileUtils.readLines(entry);
        if (!lines.isEmpty()) {
          return lines.subList(1, lines.size());
        }
      } catch (IOException ex) {
        SilverLogger.getLogger(ImageCache.class).error(ex.getMessage());
      }
    }
    return Collections.EMPTY_LIST;
  }

  /**
   * Gets all the entries in this cache. Each entry is defined by the absolute path of the original
   * image from which resized ones were generated and referred by this cache.
   * @return a list of absolute path of the original images from which a resized image was made.
   */
  protected static List<String> getAllImageEntries() {
    List<String> originalImagePaths = new ArrayList<String>();
    File entries = new File(IMAGE_CACHE_TABLE);
    for (File anEntry : entries.listFiles()) {
      try {
        List<String> resizedImagePaths = FileUtils.readLines(anEntry);
        originalImagePaths.add(resizedImagePaths.get(0));
      } catch (IOException ex) {
        SilverLogger.getLogger(ImageCache.class).error(ex.getMessage());
      }
    }
    return originalImagePaths;
  }

  private static String hash(String name) {
    MessageDigest m;
    try {
      m = MessageDigest.getInstance("MD5");
      m.update(name.getBytes(), 0, name.length());
      return new BigInteger(1, m.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      SilverLogger.getLogger(ImageCache.class).warn(e.getMessage());
      return String.valueOf(name.hashCode());
    }
  }
}
