package org.silverpeas.util.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.silverpeas.util.PathTestUtil;

public class MailExtractorTest {

  private static final String dir = PathTestUtil.TARGET_DIR + File.separatorChar + "test-classes" +
      PathTestUtil.SEPARATOR + "org" + File.separatorChar + "silverpeas" + File.separatorChar +
      "util" + File.separatorChar + "mail" + File.separatorChar;

  @Test
  public void testEMLBrut() throws Exception {
    String fileName = "Silverpeas test archivage 1_3 (brut sans pieces jointes).eml";

    testBrut(fileName);
  }

  @Test
  public void testMSGBrut() throws Exception {
    String fileName = "Silverpeas test archivage 1_3 (brut sans pieces jointes).msg";

    testBrut(fileName);
  }

  private void testBrut(String filename) throws Exception {
    File file = new File(dir + filename);

    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();

    // check basic attributes
    assertThat(mail.getSubject(), is("Silverpeas : test archivage 1/3 (brut)"));

    // check date of transmission
    Calendar calendar = Calendar.getInstance();
    calendar.set(2013, 0, 21, 10, 10);

    assertThat(DateUtils.truncate(mail.getDate(), Calendar.MINUTE),
        is(DateUtils.truncate(calendar.getTime(), Calendar.MINUTE)));

    // check from
    assertThat(mail.getFrom().getAddress(), is("nicolas.eysseric@silverpeas.com"));

    // check to
    assertThat(mail.getTo().length, is(2));
    assertThat(mail.getCc().length, is(1));
    assertThat(mail.getAllRecipients().length, is(3));

    // check body
    assertThat(mail.getBody(), is(notNullValue()));

    // check attachments
    List<MailAttachment> attachments = extractor.getAttachments();
    assertThat(attachments.size(), is(0));
  }

  @Test
  public void testEMLHTMLWithoutFiles() throws Exception {
    String fileName = "Silverpeas test archivage 2_3 (html sans pieces jointes).eml";

    testHTMLWithoutFiles(fileName);
  }

  @Test
  public void testMSGHTMLWithoutFiles() throws Exception {
    String fileName = "Silverpeas test archivage 2_3 (html sans pieces jointes).msg";

    testHTMLWithoutFiles(fileName);
  }

  private void testHTMLWithoutFiles(String filename) throws Exception {
    File file = new File(dir + filename);

    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();

    // check basic attributes
    assertThat(mail.getSubject(), is("Silverpeas : test archivage 2/3 (hml sans pièces jointes)"));

    // check date of transmission
    Calendar calendar = Calendar.getInstance();
    calendar.set(2013, 0, 21, 10, 11);
    assertThat(DateUtils.truncate(mail.getDate(), Calendar.MINUTE),
        is(DateUtils.truncate(calendar.getTime(), Calendar.MINUTE)));
    
    // check from
    assertThat(mail.getFrom().getAddress(), is("nicolas.eysseric@silverpeas.com"));

    // check to
    assertThat(mail.getTo().length, is(2));
    assertThat(mail.getCc(), is(nullValue()));
    assertThat(mail.getAllRecipients().length, is(2));

    // check body
    assertThat(mail.getBody(), is(notNullValue()));
    assertThat(mail.getBody().startsWith("Bonjour"), is(true));
    assertThat(mail.getBody().lastIndexOf("Bonjour"), is(0));

    // check attachments
    List<MailAttachment> attachments = extractor.getAttachments();
    assertThat(attachments.size(), is(0));
  }

  @Test
  public void testEMLHTMLWithFiles() throws Exception {
    String fileName = "Silverpeas test archivage 3_3 (html avec pieces jointes).eml";

    testHTMLWithFiles(fileName);
  }

  @Test
  public void testMSGHTMLWithFiles() throws Exception {
    String fileName = "Silverpeas test archivage 3_3 (html avec pieces jointes).msg";

    testHTMLWithFiles(fileName);
  }

  private void testHTMLWithFiles(String filename) throws Exception {
    File file = new File(dir + filename);

    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();

    // check basic attributes
    assertThat(mail.getSubject(), is("Silverpeas : test archivage 3/3 (hml avec pièces jointes)"));

    // check date of transmission
    Calendar calendar = Calendar.getInstance();
    calendar.set(2013, 0, 21, 10, 11);
    assertThat(DateUtils.truncate(mail.getDate(), Calendar.MINUTE),
        is(DateUtils.truncate(calendar.getTime(), Calendar.MINUTE)));
    
    // check from
    assertThat(mail.getFrom().getAddress(), is("nicolas.eysseric@silverpeas.com"));

    // check to
    assertThat(mail.getTo().length, is(1));
    assertThat(mail.getCc().length, is(1));
    assertThat(mail.getAllRecipients().length, is(2));

    // check body
    assertThat(mail.getBody(), is(notNullValue()));

    // check attachments
    List<MailAttachment> attachments = extractor.getAttachments();
    assertThat(attachments.size(), is(2));

    MailAttachment attachment = attachments.get(0);
    assertThat(attachment.getName(), is("Liste des applications-V1.0.pdf"));
    File file1 = new File(dir + "temp" + PathTestUtil.SEPARATOR + attachment.getName());
    FileUtils.copyInputStreamToFile(attachment.getFile(), file1);
    assertThat(attachment.getName(), is(file1.getName()));
    assertThat(file1.length(), is(149463L));
    IOUtils.closeQuietly(attachment.getFile());
    FileUtils.deleteQuietly(file1);

    attachment = attachments.get(1);
    assertThat(attachment.getName(), is("Silverpeas-SearchEngine.odt"));
    IOUtils.closeQuietly(attachment.getFile());
  }
}
