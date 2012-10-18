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
package org.silverpeas.viewer;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.silverpeas.viewer.exception.PreviewException;

import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractViewerService {

  // Extension of pdf document file
  public static final String PDF_DOCUMENT_EXTENSION = "pdf";
  protected final ResourceLocator settings =
      new ResourceLocator("org.silverpeas.viewer.viewer", "");

  /**
   * Generate a tmp file
   * @param fileType
   * @return
   */
  protected File generateTmpFile(final String fileExtension) {
    return new File(FileRepositoryManager.getTemporaryPath() + System.nanoTime() + "." +
        fileExtension);
  }

  /**
   * Changes the extension of a file
   * @param fileExtension
   * @return
   */
  protected File changeTmpFileExtension(final File file, final String fileExtension) {
    return new File(getFullPath(file.getPath()) + getBaseName(file.getPath()) + "." + fileExtension);
  }

  /**
   * Centralizing command exececution code
   * @param command
   * @return
   */
  protected List<String> exec(final String command) {
    final List<String> result = new ArrayList<String>();
    final Process process;
    try {
      process = Runtime.getRuntime().exec(command);
      final Thread errEater = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            IOUtils.readLines(process.getErrorStream());
          } catch (final IOException e) {
            throw new PreviewException(e);
          }
        }
      });
      errEater.start();
      final Thread outEater = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            result.addAll(IOUtils.readLines(process.getInputStream()));
          } catch (final IOException e) {
            throw new PreviewException(e);
          }
        }
      });
      outEater.start();
      process.waitFor();
    } catch (final Exception e) {
      throw new PreviewException(e);
    }
    return result;
  }
}
