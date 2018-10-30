/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.directory.servlets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.test.extention.SilverTestEnv;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@ExtendWith(SilverTestEnv.class)
public class ImageProfilTest {

  @AfterEach
  public void prepareDir() throws Exception {
    FileUtils.deleteQuietly(new File(FileRepositoryManager.getAvatarPath()));
  }

  @Test
  public void testIsImage() {
    ImageProfil imageProfil = new ImageProfil("nidale.jpg");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.bmp");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.jpeg");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.gif");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.txt");
    assertEquals(false, imageProfil.isImage());
  }

  @Test
  public void testExtractImage() throws IOException {
    ImageProfil imageProfil = new ImageProfil("SilverAdmin.jpg");
    try (InputStream fis = this.getClass().getResourceAsStream("/SilverAdmin.jpg")) {
      imageProfil.saveImage(fis);
    }
    try (InputStream image = imageProfil.getImage();
         InputStream fis = this.getClass().getResourceAsStream("/SilverAdmin.jpg")) {
      IOUtils.contentEquals(fis, image);
    }
  }

}
