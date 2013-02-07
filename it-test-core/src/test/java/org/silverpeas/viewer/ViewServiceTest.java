package org.silverpeas.viewer;

import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-viewer.xml")
public class ViewServiceTest {

  @Inject
  private ViewService viewService;

  @Before
  public void setup() throws Exception {
    (new File(FileRepositoryManager.getTemporaryPath())).mkdirs();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(new File(FileRepositoryManager.getTemporaryPath()));
  }

  @Test
  public void testPdfFileView() throws Exception {
    if (SwfToolManager.isActivated()) {
      final DocumentView view =
          viewService.getDocumentView("file.pdf", getDocumentNamed("file.pdf"));
      assertThat(view, notNullValue());
      assertThat(view.getPhysicalFile().getName().length(), greaterThan(10));
      assertThat(view.getWidth(), is("595"));
      assertThat(view.getHeight(), is("842"));
    }
  }

  private File getDocumentNamed(final String name) throws Exception {
    final URL documentLocation = getClass().getResource(name);
    return new File(documentLocation.toURI());
  }
}
