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
package org.silverpeas.core.viewer.service;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.io.media.image.imagemagick.Im4javaManager;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.viewer.test.WarBuilder4Viewer;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.test.util.SilverProperties;
import org.silverpeas.core.util.ServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractViewerTest {

  @Deployment
  public static Archive<?> createTestArchive() throws IOException {
    return WarBuilder4Viewer.onWarForTestClass(AbstractViewerTest.class).build();
  }

  private static File tempPath;
  private static File resourceTestDir;

  protected static void init() {
    if (tempPath == null) {
      SilverProperties properties =
          MavenTargetDirectoryRule.loadPropertiesForTestClass(AbstractViewerTest.class);
      properties.load("org/silverpeas/general.properties");
      tempPath = new File(properties.getProperty("tempPath"));
      resourceTestDir = MavenTargetDirectoryRule.getResourceTestDirFile(properties);
    }
  }

  protected static File getTemporaryPath() {
    return tempPath;
  }

  private static File getResourceTestDirFile() {
    return resourceTestDir;
  }

  @Before
  public void setupCommonServices() throws Exception {
    init();
    for (Initialization serviceToInitialize : new Initialization[]{
        ServiceProvider.getService(Im4javaManager.class),
        ServiceProvider.getService(SwfToolManager.class),
        ServiceProvider.getService(JsonPdfToolManager.class)}) {
      serviceToInitialize.init();
    }
  }

  @SuppressWarnings("ConstantConditions")
  protected File getDocumentNamed(final String name) throws Exception {
    return FileUtils.getFile(getResourceTestDirFile(), "org/silverpeas/viewer", name);
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

  protected void saveInTemporaryPath(String simpleFileName, String value) throws Exception {
    FileUtils.writeStringToFile(new File(getTemporaryPath(), simpleFileName), value);
  }

  protected String readAndRemoveFromTemporaryPath(String simpleFileName) throws Exception {
    File fileToReadAndRemove = new File(getTemporaryPath(), simpleFileName);
    try {
      return FileUtils.readFileToString(fileToReadAndRemove);
    } finally {
      FileUtils.deleteQuietly(fileToReadAndRemove);
    }
  }

  protected Long readAndRemoveFromTemporaryPathAsLong(String simpleFileName) throws Exception {
    return Long.valueOf(readAndRemoveFromTemporaryPath(simpleFileName));
  }
}
