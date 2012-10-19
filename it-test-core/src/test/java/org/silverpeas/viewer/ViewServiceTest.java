package org.silverpeas.viewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.stratelia.webactiv.util.FileRepositoryManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-viewer.xml")
public class ViewServiceTest {

  @Inject
  private ViewService viewService;

  @After
  public void tearDown() throws Exception {
    FileUtils.cleanDirectory(new File(FileRepositoryManager.getTemporaryPath()));
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
