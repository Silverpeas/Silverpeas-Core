/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.io.file;

import org.apache.commons.io.FileUtils;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.Charset.defaultCharset;

/**
 * It represents a cache of the images that were resized by the
 * {@link ImageResizingProcessor} processor. In fact, it doesn't contain
 * the images themselves but their location in the file system. It is used by the image resizing
 * processor as a table of resized images in order to keep a trace of them for management purpose.
 * @author mmoquillon
 */
class ImageCache {

  private ImageCache() {
    // Not instantiatable
  }

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
      List<String> lines = new ArrayList<>();
      if (!entry.exists()) {
        lines.add(originalImagePath);
      }
      lines.add(resizedImagePath);
      FileUtils.writeLines(entry, lines, true);
    } catch (IOException ex) {
      SilverLogger.getLogger(ImageCache.class)
          .error("Cannot write the cache entry {0} with value {1}. Cause: {2}",
              entry.getAbsolutePath(), resizedImagePath, ex.getMessage());
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
        removeEachImageIn(entry);
      }
    }
  }

  private static void removeEachImageIn(final File entry) {
    try {
      List<String> lines = FileUtils.readLines(entry, defaultCharset());
      for (String resizedImagePath : lines.subList(1, lines.size())) {
        File resizedImage = new File(resizedImagePath);
        if (resizedImage.exists() && !Files.deleteIfExists(resizedImage.toPath())) {
          SilverLogger.getLogger(ImageCache.class)
              .warn("Cannot remove {0} from the cache entry {1}",
                  resizedImage.getAbsolutePath(), entry.getAbsolutePath());
        }
      }
      if (!Files.deleteIfExists(entry.toPath())) {
        SilverLogger.getLogger(ImageCache.class)
            .warn("Cannot delete the cache entry {0}", entry.getAbsolutePath());
      }
    } catch (IOException ex) {
      SilverLogger.getLogger(ImageCache.class).error(ex.getMessage(), ex.getMessage());
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
        List<String> lines = FileUtils.readLines(entry, defaultCharset());
        if (!lines.isEmpty()) {
          return lines.subList(1, lines.size());
        }
      } catch (IOException ex) {
        SilverLogger.getLogger(ImageCache.class).error(ex.getMessage());
      }
    }
    return Collections.emptyList();
  }

  /**
   * Gets all the entries in this cache. Each entry is defined by the absolute path of the original
   * image from which resized ones were generated and referred by this cache.
   * @return a list of absolute path of the original images from which a resized image was made.
   */
  protected static List<String> getAllImageEntries() {
    final List<String> originalImagePaths = new ArrayList<>();
    final File entries = new File(IMAGE_CACHE_TABLE);
    final File[] files = entries.listFiles();
    if (files != null) {
      for (final File anEntry : files) {
        try {
          List<String> resizedImagePaths = FileUtils.readLines(anEntry, defaultCharset());
          originalImagePaths.add(resizedImagePaths.get(0));
        } catch (IOException ex) {
          SilverLogger.getLogger(ImageCache.class).error(ex.getMessage());
        }
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
