package com.silverpeas.directory.servlets;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.silverpeas.util.PathTestUtil;

  public class ImageProfilTest {
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
	  ImageProfil imageProfil = new ImageProfil("nidale.jpg", "");
	  assertEquals(true, imageProfil.isImage());
	  imageProfil = new ImageProfil("nidale.bmp", "");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.jpeg", "");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.gif", "");
    assertEquals(true, imageProfil.isImage());
    imageProfil = new ImageProfil("nidale.txt", "");
    assertEquals(false, imageProfil.isImage());
	}

	@Test
	public void testExtractImage() throws IOException {
	  String subdirectory = "read";
    ImageProfil imageProfil = new ImageProfil("SilverAdmin.jpg", subdirectory);
    imageProfil.saveImage(this.getClass().getResourceAsStream("SilverAdmin.jpg"));
    IOUtils.contentEquals(this.getClass().getResourceAsStream("SilverAdmin.jpg"),imageProfil.getImage());
	}

}
