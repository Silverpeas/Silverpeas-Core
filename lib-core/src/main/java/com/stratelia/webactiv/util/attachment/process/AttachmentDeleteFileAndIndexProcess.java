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
package com.stratelia.webactiv.util.attachment.process;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.management.AbstractFileProcess;
import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.process.session.ProcessSession;

import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * Silverpeas process which takes in charge the attachment deletion of a file with its indexes.
 *
 * @author Yohann Chastagnier
 */
public class AttachmentDeleteFileAndIndexProcess extends AbstractFileProcess<ProcessExecutionContext> {

  private final SimpleDocument document;
  private final AttachmentDetail attachment;

  /**
   * Gets an instance
   *
   * @param attachment
   * @return
   */
  public static AttachmentDeleteFileAndIndexProcess getInstance(final SimpleDocument attachment) {
    return new AttachmentDeleteFileAndIndexProcess(attachment);
  }

  public static AttachmentDeleteFileAndIndexProcess getInstance(final AttachmentDetail attachment) {
    return new AttachmentDeleteFileAndIndexProcess(attachment);
  }

  /**
   * Default hidden constructor
   *
   * @param attachment
   */
  private AttachmentDeleteFileAndIndexProcess(final AttachmentDetail attachment) {
    this.document = null;
    this.attachment = attachment;
  }

  /**
   * Default hidden constructor
   *
   * @param document
   */
  private AttachmentDeleteFileAndIndexProcess(final SimpleDocument document) {
    this.document = document;
    this.attachment = null;
  }

  @Override
  public void processFiles(final ProcessExecutionContext processExecutionProcess,
      final ProcessSession session, final FileHandler fileHandler) throws Exception {
    if (attachment != null) {
      AttachmentController.deleteFileAndIndex(attachment, fileHandler);
    } else {
      AttachmentController.deleteFileAndIndex(document, fileHandler);
    }
  }
}
