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
package org.silverpeas.web.directory.servlets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableSilverTestEnv(context = JEETestContext.class)
class ImageProfileTest {

  @AfterEach
  public void prepareDir() throws Exception {
    FileUtils.deleteQuietly(new File(FileRepositoryManager.getAvatarPath()));
  }

  @Test
  void testIsImage() {
    ImageProfile imageProfile = new ImageProfile("nidale.jpg");
    assertEquals(true, imageProfile.isImage());
    imageProfile = new ImageProfile("nidale.bmp");
    assertEquals(true, imageProfile.isImage());
    imageProfile = new ImageProfile("nidale.jpeg");
    assertEquals(true, imageProfile.isImage());
    imageProfile = new ImageProfile("nidale.gif");
    assertEquals(true, imageProfile.isImage());
    imageProfile = new ImageProfile("nidale.txt");
    assertEquals(false, imageProfile.isImage());
  }

  @Test
  void testExtractImage() throws IOException {
    ImageProfile imageProfile = new ImageProfile("SilverAdmin.jpg");
    try (InputStream fis = this.getClass().getResourceAsStream("/SilverAdmin.jpg")) {
      imageProfile.saveImage(fis);
    }
    try (InputStream image = imageProfile.getImage();
         InputStream fis = this.getClass().getResourceAsStream("/SilverAdmin.jpg")) {
      assertTrue(IOUtils.contentEquals(fis, image));
    }
  }

}
