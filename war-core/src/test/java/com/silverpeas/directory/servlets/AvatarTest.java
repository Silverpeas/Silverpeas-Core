/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.directory.servlets;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import com.silverpeas.personalization.Avatar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.silverpeas.util.PathTestUtil;

  public class AvatarTest {
  private static final String TEMP_DIRECTORY = PathTestUtil.TARGET_DIR + File.separatorChar + "temp" + File.separatorChar + "read";
  private static final String SOURCE_FILE = PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes" + File.separatorChar + "SilverAdmin.jpg";
  @BeforeClass
  public static void prepareDir() throws IOException {
    FileUtils.forceMkdir(new File(TEMP_DIRECTORY));
    FileUtils.copyFileToDirectory(new File(SOURCE_FILE), new File(TEMP_DIRECTORY) );
  }

  @AfterClass
  public static void cleanDir() throws IOException {
   // FileUtils.forceDelete(new File(TEMP_DIRECTORY));
  }
	@Test
	public void testIsImage() {
	  Avatar avatar = new Avatar("nidale.jpg");
	  assertEquals(true, avatar.isImage());
	  avatar = new Avatar("nidale.bmp");
    assertEquals(true, avatar.isImage());
    avatar = new Avatar("nidale.jpeg");
    assertEquals(true, avatar.isImage());
    avatar = new Avatar("nidale.gif");
    assertEquals(true, avatar.isImage());
    avatar = new Avatar("nidale.txt");
    assertEquals(false, avatar.isImage());
	}

	@Test
	public void testExtractImage() throws IOException {
    Avatar avatar = new Avatar("SilverAdmin.jpg");
    avatar.saveImage(this.getClass().getResourceAsStream("SilverAdmin.jpg"));
    IOUtils.contentEquals(this.getClass().getResourceAsStream("SilverAdmin.jpg"), avatar.getImage());
	}

}
