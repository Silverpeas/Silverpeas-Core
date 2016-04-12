/*
 * Copyright (C) 2000-2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.apache.commons.fileupload.disk;

import org.silverpeas.core.util.file.FileRepositoryManager;
import java.io.File;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileCleaningTracker;

/**
 * This factory is to replace the one provided by the FileUpload Apache Commons as a workaround of a
 * no yet fixed bug in the Apache library.
 *
 * The DiskFileItemFactory class in FileUpload Apache Commons has a bug when used in conjonction
 * with a tracker of non-long used temporary files (see
 * http://blog.novoj.net/2012/09/19/commons-file-upload-contains-a-severe-memory-leak/ for a
 * description of the bug and https://issues.apache.org/jira/browse/FILEUPLOAD-189 for a look at the
 * bug fix status). This implementation aims to replace the original DiskFileItemFactory class by
 * fixing the bug (in the hope it will be fix in a next release of the FileUpload Apache Commons
 * library).
 *
 * Beside this bug, some default parameters are also set for the particular use of Silverpeas:
 * <ul>
 * <li>the path of the temporary directory in use in Silverpeas,</li>
 * <li>the thresold size above which the uploaded files are temporarly stored in disk.</li>
 * </ul>
 *
 * @author mmoquillon
 */
public class SilverpeasDiskFileItemFactory extends DiskFileItemFactory {

  private static final int THRESHOLD_SIZE = 2097152;

  /**
   * Constructs a new SilverpeasDiskFileItemFactory by setting by default the following
   * parameters:
   * <ul>
   * <li>the size threshold is set at 2Mo: about this size, the file are temporarly stored in disk;</li>
   * <li>the temporary directory is the default one used in Silverpeas
   * (@see org.silverpeas.core.util.file.FileRepositoryManager#getTemporaryPath());</li>
   * <li>a temporary file cleaner is set: all stored file in disks are deleted once no more unused
   * (@see org.apache.commons.fileupload.disk.DiskFileItemFactory).<li>
   * </ul>
   */
  public SilverpeasDiskFileItemFactory() {
    super();
    this.setSizeThreshold(THRESHOLD_SIZE);
    this.setRepository(new File(FileRepositoryManager.getTemporaryPath()));
    this.setFileCleaningTracker(new FileCleaningTracker());
  }

  @Override
  public FileItem createItem(String fieldName, String contentType, boolean isFormField,
      String fileName) {
    DiskFileItem result = new DiskFileItem(fieldName, contentType,
        isFormField, fileName, getSizeThreshold(), getRepository());
    FileCleaningTracker tracker = getFileCleaningTracker();
    if (tracker != null) {
      tracker.track(result.getTempFile(), result);
    }
    return result;
  }
}
