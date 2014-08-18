package org.silverpeas.media.video.ffmpeg;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-ffmpeg.xml" })
public class FFmpegThumbnailExtractorTest {

  private static final File mp4File = getDocumentNamed("/video.mp4");
  private static final File flvFile = getDocumentNamed("/video.flv");
  private static final File movFile = getDocumentNamed("/video.mov");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    cleanThumbnails();
  }

  @After
  public void tearDown() throws Exception {
    cleanThumbnails();
  }

  private static void cleanThumbnails() {
    for (int i = 0; i < 5; i++) {
      new File(mp4File.getParentFile(), "img" + i + ".jpg").delete();
    }
  }

  @Test
  public void testGenerateThumbnailsFromMp4() {
    generateThumbnailsFromFile(mp4File);
  }

  @Test
  public void testGenerateThumbnailsFromFlv() {
    generateThumbnailsFromFile(flvFile);
  }

  @Test
  public void testGenerateThumbnailsFromMov() {
    generateThumbnailsFromFile(movFile);
  }

  private void generateThumbnailsFromFile(File file) {
    FFmpegThumbnailExtractor extractor = FFmpegThumbnailExtractor.getInstance();
    if (extractor.isActivated()) {
      extractor.generateThumbnailsFrom(file);
      assertThat(new File(file.getParentFile(), "img0.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img1.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img2.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img3.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img4.jpg").exists(), equalTo(true));
    }
  }

  private static File getDocumentNamed(final String name) {
    final URL documentLocation = FFmpegThumbnailExtractorTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }

}
