package org.silverpeas.file;

import org.silverpeas.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.silverpeas.file.ImageResizingProcessor.IMAGE_CACHE_PATH;
import static org.silverpeas.file.SilverpeasFileProcessor.ProcessingContext.GETTING;

public class ImageResizingProcessorTest {

  private static final String IMAGE_NAME = "image-test.jpg";
  private static final int IMAGE_WIDTH = 550;
  private static final int IMAGE_HEIGHT = 413;

  private static final int IMAGE_SIDE_SIZE = 128;
  private static final String NEW_SIZE = IMAGE_SIDE_SIZE + "x" + IMAGE_SIDE_SIZE;
  private static final String NEW_SIZE_WIDTH = IMAGE_SIDE_SIZE + "x";
  private static final String NEW_SIZE_HEIGHT = "x" + IMAGE_SIDE_SIZE;
  private static final String NO_SIZE = String.valueOf(IMAGE_SIDE_SIZE);
  private static final String INVALID_SIZE = "trx128";

  private ConfigurableApplicationContext ctx;
  private File originalImage;
  ImageResizingProcessor processor;

  @Before
  public void setUp() throws Exception {
    // get the original path
    originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));

    // bootstrap the Spring context
    ctx = new ClassPathXmlApplicationContext("classpath:/spring-image.xml");
    ctx.start();
    processor = ctx.getBean(ImageResizingProcessor.class);
  }

  @After
  public void tearDown() throws Exception {
    ctx.close();
    File cache = new File(IMAGE_CACHE_PATH);
    if (cache.exists()) {
      FileUtil.forceDeletion(new File(IMAGE_CACHE_PATH));
    }
  }

  /**
   * No resizing occurs when the targeted image exists.
   * @throws Exception if an error occurs.
   */
  @Test
  public void noResizingOccursIfTheImageAlreadyExists() throws Exception {
    String actualPath = processor.processBefore(originalImage.getCanonicalPath(), GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  /**
   * An already resized image (that is an existing image in the directory corresponding to the
   * image
   * size) is not any more resized.
   * @throws Exception if an error occurs.
   */
  @Test
  public void noResizingOccursIfTheImageIsAlreadyResized() throws Exception {
    String expectedPath = copyOriginalImageInto(IMAGE_CACHE_PATH + File.separator + NEW_SIZE);

    String actualPath = processor.processBefore(expectedPath, GETTING);
    assertThat(actualPath, is(expectedPath));

    BufferedImage image = ImageIO.read(new File(actualPath));
    assertThat(image.getWidth(), is(IMAGE_WIDTH));
    assertThat(image.getHeight(), is(IMAGE_HEIGHT));
  }

  @Test
  public void resizeAtCorrectDimension() throws Exception {
    String askedPath = pathForOriginalImageSize(NEW_SIZE);
    String actualPath = processor.processBefore(askedPath, GETTING);
    assertThat(actualPath, not(is(askedPath)));

    BufferedImage image = ImageIO.read(new File(actualPath));
    assertThat(image.getWidth(), is(IMAGE_SIDE_SIZE));
    assertThat(image.getHeight(), is(IMAGE_SIDE_SIZE * IMAGE_HEIGHT / IMAGE_WIDTH));
  }

  @Test
  public void resizeAtCorrectWidth() throws Exception {
    String askedPath = pathForOriginalImageSize(NEW_SIZE_WIDTH);
    String actualPath = processor.processBefore(askedPath, GETTING);
    assertThat(actualPath, not(is(askedPath)));

    BufferedImage image = ImageIO.read(new File(actualPath));
    assertThat(image.getWidth(), is(IMAGE_SIDE_SIZE));
    assertThat(image.getHeight(), is(IMAGE_SIDE_SIZE * IMAGE_HEIGHT / IMAGE_WIDTH));
  }

  @Test
  public void resizeAtCorrectHeight() throws Exception {
    String askedPath = pathForOriginalImageSize(NEW_SIZE_HEIGHT);
    String actualPath = processor.processBefore(askedPath, GETTING);
    assertThat(actualPath, not(is(askedPath)));

    BufferedImage image = ImageIO.read(new File(actualPath));
    assertThat(image.getWidth(), is(IMAGE_SIDE_SIZE * IMAGE_WIDTH / IMAGE_HEIGHT));
    assertThat(image.getHeight(), is(IMAGE_SIDE_SIZE));
  }

  @Test
  public void noResizingIfNoSizeInThePath() throws Exception {
    String askedPath = pathForOriginalImageSize(NO_SIZE);
    String actualPath = processor.processBefore(askedPath, GETTING);

    assertThat(actualPath, is(askedPath));
  }

  @Test
  public void noResizingIfInvalidSize() throws Exception {
    String askedPath = pathForOriginalImageSize(INVALID_SIZE);
    String actualPath = processor.processBefore(askedPath, GETTING);

    assertThat(actualPath, is(askedPath));
  }

  /**
   * Copy the image used in the tests into the specified location.
   * @param path the absolute path of the directory into which the original image will be copied.
   * @return the path of the copied image.
   * @throws IOException if the copy fails.
   */
  private String copyOriginalImageInto(String path) throws IOException {
    File destination = new File(path + File.separator + originalImage.getName());
    destination.getParentFile().mkdirs();
    FileUtil.copyFile(originalImage, destination);
    return destination.getCanonicalPath();
  }

  /**
   * Computes the path of the original image for an image size as specified in argument. The
   * resizing is performed according to the size specified in the path (the name of the last
   * directory in the path). This method then builds the path according to this rule.
   * @param size the size to which the image should be resized.
   * @return the computed path.
   */
  private String pathForOriginalImageSize(String size) {
    return originalImage.getParent() + File.separator + size + File.separator + IMAGE_NAME;
  }
}