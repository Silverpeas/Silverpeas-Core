package org.silverpeas.util.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.silverpeas.util.PathTestUtil;

public class MailExtractorTest {
  
  private static final String dir =
      PathTestUtil.TARGET_DIR + "test-classes" + PathTestUtil.SEPARATOR +
          "org/silverpeas/util/mail/";

  @Test
  public void testEML() throws Exception {
    String fileName = "ci-joint 3 commandes.eml";

    File file = new File(dir + fileName);
    test1(file);
  }
  
  /*@Test
  public void testMSG() throws Exception {
    String fileName = "ci-joint 3 commandes.msg";
    
    File file = new File(dir + fileName);
    test1(file);
  }*/
  
  private void test1(File file) throws Exception {
    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();

    // check basic attributes
    assertThat(mail.getSubject(), is("ci-joint 3 commandes"));

    Calendar calendar = Calendar.getInstance();
    calendar.set(2012, 11, 11, 14, 38, 51);
    assertThat(DateUtils.truncate(mail.getDate(), Calendar.SECOND),
        is(DateUtils.truncate(calendar.getTime(), Calendar.SECOND)));
    
    assertThat(mail.getFrom().getAddress(), is("michele.gougaud@cg11.fr"));

    // check body
    assertThat(mail.getBody(), is(notNullValue()));

    // check attachments
    List<MailAttachment> attachments = extractor.getAttachments();
    assertThat(attachments.size(), is(5));
    for (MailAttachment attachment : attachments) {
      IOUtils.closeQuietly(attachment.getFile());
    }
  }
}
