/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.image;

import static java.util.Collections.singleton;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.silverpeas.image.option.AbstractImageToolOption;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractImageTool implements ImageTool {

  /**
   * Convert an image with dimensions and options directives
   * @param source mandatory (if it not exists, no exception is generated and the process stops)
   * @param destination if destination is not specified, the destination file is the same as the
   * source
   * @param options permits specifying multiple additional working options :
   * - DIMENSION : resizing the image
   * - BACKGROUND : setting a background color
   * @param directives it is possible to specify some additional directives :
   * - PREVIEW_WORK : the conversion concerns an preview result
   * - GEOMETRY_SHRINK : shrinks images with dimension(s) larger than the corresponding width and/or
   * height dimension(s).
   */
  protected abstract void convert(File source, File destination,
      Map<Class<AbstractImageToolOption>, AbstractImageToolOption> options,
      Set<ImageToolDirective> directives) throws Exception;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.image.ImageTool#convert(java.io.File, java.io.File,
   * org.silverpeas.image.ImageToolDirective[])
   */
  @Override
  public void convert(final File source, final File destination,
      final ImageToolDirective... directives) {
    centralizedConvert(source, destination, null, directives);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.image.ImageTool#convert(java.io.File, java.io.File,
   * org.silverpeas.image.option.AbstractImageToolOption, org.silverpeas.image.ImageToolDirective[])
   */
  @Override
  public void convert(final File source, final File destination,
      final AbstractImageToolOption option, final ImageToolDirective... directives) {
    centralizedConvert(source, destination, singleton(option), directives);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.image.ImageTool#convert(java.io.File, java.io.File, java.util.Set,
   * org.silverpeas.image.ImageToolDirective[])
   */
  @Override
  public void convert(final File source, final File destination,
      final Set<AbstractImageToolOption> options, final ImageToolDirective... directives) {
    centralizedConvert(source, destination, options, directives);
  }

  /**
   * Centralizes convert calling
   * @param source
   * @param destination
   * @param options
   * @param directives
   */
  private void centralizedConvert(final File source, File destination,
      final Set<AbstractImageToolOption> options, final ImageToolDirective... directives) {
    if (source.exists()) {
      if (destination == null) {
        destination = source;
      }
      try {
        convert(source, destination, toMap(options), toSet(directives));
      } catch (final Exception e) {
        throw new ImageToolException(e);
      }
    }
  }

  /**
   * Option getter tool
   * @param options
   * @param key
   * @return
   */
  protected <T extends AbstractImageToolOption> T getOption(final Map<?, ?> options,
      final Class<T> key) {
    return (T) options.get(key);
  }

  /**
   * Internal tool
   * @param options
   * @return
   */
  private <T extends AbstractImageToolOption> Map<Class<T>, AbstractImageToolOption> toMap(
      final Set<AbstractImageToolOption> options) {
    Map<Class<T>, AbstractImageToolOption> mappedOptions;
    if (options != null) {
      mappedOptions = new HashMap<Class<T>, AbstractImageToolOption>();
      for (final AbstractImageToolOption option : options) {
        mappedOptions.put((Class<T>) option.getClass(), option);
      }
    } else {
      mappedOptions = new HashMap<Class<T>, AbstractImageToolOption>(0);
    }
    return mappedOptions;
  }

  /**
   * Internal tool
   * @param items
   * @return
   */
  private <T> Set<T> toSet(final T... items) {
    if (items == null) {
      return new HashSet<T>(0);
    }
    return new HashSet<T>(Arrays.asList(items));
  }
}
