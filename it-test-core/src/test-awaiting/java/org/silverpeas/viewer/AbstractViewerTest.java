/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.viewer;

import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
public class AbstractViewerTest {

  protected File getDocumentNamed(final String name) throws Exception {
    final URL documentLocation = ViewServiceCacheDemonstrationTestAfter.class.getResource(name);
    return new File(documentLocation.toURI());
  }

  protected SimpleDocument getSimpleDocumentNamed(final String name) throws Exception {
    final File document = getDocumentNamed(name);
    return new SimpleDocument(new SimpleDocumentPK("simple_doc_UUID_" + name, "instanceId"),
        "foreignId", 0, false,
        new SimpleAttachment(name, "fr", "title", "description", document.length(), "contentType",
            "me", new Date(), null)) {
      private static final long serialVersionUID = 4437882040649114634L;

      @Override
      public String getAttachmentPath() {
        return document.getPath();
      }
    };
  }

  protected boolean canPerformViewConversionTest() {
    if (SwfToolManager.isActivated()) {
      return true;
    }
    Logger.getAnonymousLogger().severe("SwfTools are not available, test is skipped.");
    Logger.getAnonymousLogger().severe("Please install pdf2swf and swfrender tools.");
    return false;
  }
}
