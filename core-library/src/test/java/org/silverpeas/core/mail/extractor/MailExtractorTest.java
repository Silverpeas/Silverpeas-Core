/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.mail.extractor;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@EnableSilverTestEnv
public class MailExtractorTest {

  @Test
  public void testEMLBrut() throws Exception {
    String fileName = "Silverpeas test archivage 1_3 (brut sans pieces jointes).eml";
    OffsetDateTime mailDateTime = OffsetDateTime.parse("2013-01-21T10:10:48+01:00");
    testBrut(fileName, mailDateTime);
  }

  @Test
  public void testMSGBrut() throws Exception {
    String fileName = "Silverpeas test archivage 1_3 (brut sans pieces jointes).msg";
    OffsetDateTime mailDateTime = OffsetDateTime.parse("2013-01-21T10:10:48+01:00");
    testBrut(fileName, mailDateTime);
  }

  private void testBrut(String filename, OffsetDateTime mailDateTime) throws Exception {
    File file = getDocumentNamed(filename);
    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();
    // check basic attributes
    assertThat(mail.getSubject(), is("Silverpeas : test archivage 1/3 (brut)"));
    // check date of transmission
    assertThat(mail.getDate().toInstant(), is(mailDateTime.toInstant()));    // check from
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
    OffsetDateTime mailDateTime = OffsetDateTime.parse("2013-01-21T10:11:03+01:00");
    testHTMLWithoutFiles(fileName, mailDateTime);
  }

  @Test
  public void testMSGHTMLWithoutFiles() throws Exception {
    String fileName = "Silverpeas test archivage 2_3 (html sans pieces jointes).msg";
    OffsetDateTime mailDateTime = OffsetDateTime.parse("2013-01-21T10:11:03+01:00");
    testHTMLWithoutFiles(fileName, mailDateTime);
  }

  private void testHTMLWithoutFiles(String filename, OffsetDateTime mailDateTime) throws Exception {
    File file = getDocumentNamed(filename);
    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();
    // check basic attributes
    assertThat(mail.getSubject(), is("Silverpeas : test archivage 2/3 (hml sans pièces jointes)"));
    // check date of transmission
    assertThat(mail.getDate().toInstant(), is(mailDateTime.toInstant()));
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
    OffsetDateTime mailDateTime = OffsetDateTime.parse("2013-01-21T10:11:25+01:00");
    String body = "Bonjour,<div><br></div><div>Ce mail est au format html est permet&nbsp;<u>"
        + "quelques fantaisies</u>.</div><div>Le glisser/d&eacute;poser des mails dans la GED est "
        + "une&nbsp;<b>nouvelle fonction tr&egrave;s int&eacute;ressante</b>.</div><div><br></div>"
        + "<div>\n\nR&eacute;alis&eacute;e &agrave; la demande d&#39;"
        + "<a href=\"http://www.hydrostadium.com/\" target=\"_blank\">Hydrostadium</a>, elle va "
        + "&ecirc;tre certainement utilis&eacute;e par d&#39;autres clients de la communaut&eacute;."
        + "</div><div><br></div>-- <br>Nicolas EYSSERIC<br>\n\n<span style=\"color:rgb(102,102,102)\">"
        + "Directeur produit</span><br style=\"color:rgb(102,102,102)\">"
        + "<span style=\"color:rgb(102,102,102)\">06 59 55 18 25 <b>(Attention nouveau "
        + "num&eacute;ro)</b></span><br>\n-----------------------------<br>Silverpeas<br>1, "
        + "place Firmin Gautier<br>38000 Grenoble<br>\n";
    testHTMLWithFiles(fileName, mailDateTime, body);
  }

  @Test
  public void testMSGHTMLWithFiles() throws Exception {
    String fileName = "Silverpeas test archivage 3_3 (html avec pieces jointes).msg";
    OffsetDateTime mailDateTime = OffsetDateTime.parse("2013-01-21T10:11:25+01:00");
    String body = "Bonjour,<br/><br/>Ce mail est au format html est permet quelques fantaisies."
        + "<br/>Le glisser/d&eacute;poser des mails dans la GED est une nouvelle fonction "
        + "tr&egrave;s<br/>int&eacute;ressante.<br/><br/>R&eacute;alis&eacute;e &agrave; "
        + "la demande d'Hydrostadium &lt;http://www.hydrostadium.com/&gt; ,<br/>elle va &ecirc;tre "
        + "certainement utilis&eacute;e par d'autres clients de la<br/>communaut&eacute;.<br/><br/>"
        + "-- <br/>Nicolas EYSSERIC<br/>Directeur produit<br/>06 59 55 18 25 (Attention nouveau "
        + "num&eacute;ro)<br/>-----------------------------<br/>Silverpeas<br/>1, place Firmin "
        + "Gautier<br/>38000 Grenoble<br/><br/>";
    testHTMLWithFiles(fileName, mailDateTime, body);

  }

  private void testHTMLWithFiles(String filename, OffsetDateTime mailDateTime, String body)
      throws Exception {
    File file = getDocumentNamed(filename);
    MailExtractor extractor = Extractor.getExtractor(file);
    Mail mail = extractor.getMail();
    // check basic attributes
    assertThat(mail.getSubject(), is("Silverpeas : test archivage 3/3 (hml avec pièces jointes)"));
    // check date of transmission
    assertThat(mail.getDate().toInstant(), is(mailDateTime.toInstant()));
    // check from
    assertThat(mail.getFrom().getAddress(), is("nicolas.eysseric@silverpeas.com"));
    // check to
    assertThat(mail.getTo().length, is(1));
    assertThat(mail.getCc().length, is(1));
    assertThat(mail.getAllRecipients().length, is(2));
    // check body
    assertThat(mail.getBody(), is(notNullValue()));
    assertThat(mail.getBody(), is(body));
    // check attachments
    List<MailAttachment> attachments = extractor.getAttachments();
    assertThat(attachments.size(), is(2));
    MailAttachment attachment = attachments.get(0);
    assertThat(attachment.getName(), is("Liste des applications-V1.0.pdf"));
    attachment = attachments.get(1);
    assertThat(attachment.getName(), is("Silverpeas-SearchEngine.odt"));
  }

  private static File getDocumentNamed(final String name) {
    final URL documentLocation = MailExtractorTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
