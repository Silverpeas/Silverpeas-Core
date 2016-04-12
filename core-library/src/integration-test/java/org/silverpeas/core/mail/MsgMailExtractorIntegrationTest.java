/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.mail;

import org.silverpeas.core.contribution.converter.DocumentFormatConverterProvider;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.silverpeas.core.contribution.converter.DocumentFormat.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * User: Yohann Chastagnier Date: 21/01/13
 */
@RunWith(Arquillian.class)
public class MsgMailExtractorIntegrationTest {

  private final static String FILENAME_MAIL_WITH_ATTACHMENTS = "mailWithAttachments.msg";

  private final static DateFormat DATE_MAIL_FORMAT = new SimpleDateFormat(
      "EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(MsgMailExtractorIntegrationTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addMavenDependencies("org.apache.tika:tika-core", "org.apache.tika:tika-parsers",
            "org.apache.commons:commons-exec", "com.artofsolving:jodconverter")
        .addPackages(true, "org.silverpeas.core.contribution.converter")
        .addAsResource("org/silverpeas/converter")
        .testFocusedOn(warBuilder -> {
          warBuilder
              .addMavenDependencies("com.icegreen:greenmail")
              .addPackages(true, "org.silverpeas.core.mail")
              .addAsResource("org/silverpeas/core/mail/mailWithAttachments.msg");
        }).build();
  }

  /**
   * This unit test is support for MSG exploration solution testing.
   *
   * @throws Exception
   */
  @Test
  public void readMailWithAttachmentsMSG() throws Exception {
    MAPIMessage msg = new MAPIMessage(getDocumentFromName(FILENAME_MAIL_WITH_ATTACHMENTS).getPath());
    msg.setReturnNullOnMissingChunk(true);

    assertThat(msg.getDisplayFrom(), is("Nicolas Eysseric"));
    assertThat(msg.getRecipientDetailsChunks(), is(notNullValue()));
    assertThat(msg.getRecipientDetailsChunks().length, is(2));
    assertThat(msg.getRecipientDetailsChunks()[0].getRecipientName(), is("Aurore ADR. DELISSNYDER"));
    assertThat(msg.getRecipientDetailsChunks()[0].getRecipientEmailAddress(), is(
        "Aurore.DELISSNYDER@hydrostadium.fr"));
    assertThat(msg.getRecipientDetailsChunks()[1].getRecipientName(), is("Ludovic BERTIN"));
    assertThat(msg.getRecipientDetailsChunks()[1].getRecipientEmailAddress(), is(
        "ludovic.bertin@oosphere.com"));
    AttachmentChunks[] attachments = msg.getAttachmentFiles();
    assertThat(attachments, is(notNullValue()));
    assertThat(attachments.length, is(2));
    if (attachments != null && attachments.length > 0) {
      System.out.print("\n");
      for (AttachmentChunks attachmentChunks : attachments) {
        System.out.println(attachmentChunks.attachFileName.getValue());
      }
    } else {
      System.out.println("None.");
    }

    System.out.println("DATE : " + DateUtil.getOutputDateAndHour(getMessageDate(msg), "fr"));

    System.out.println("BODY : ");
    System.out.println(">>>");
    System.out.println(getMessageBody(msg));
    System.out.println("<<<");
  }

  /**
   * Gets the message from email.
   *
   * @param msg
   * @return
   * @throws Exception
   */
  protected String getMessageBody(MAPIMessage msg) throws Exception {
    String messageBody = msg.getHtmlBody();
    if (!StringUtil.isDefined(messageBody)) {
      messageBody = getRtfText(msg.getRtfBody());
      if (!StringUtil.isDefined(messageBody)) {
        messageBody = msg.getTextBody();
      }
    }
    return messageBody;
  }

  /**
   * Gets readable string from RTF text.
   *
   * @param rtfText
   * @return
   * @throws Exception
   */
  protected String getRtfText(String rtfText) throws Exception {
    ByteArrayOutputStream htmlText = new ByteArrayOutputStream();
    DocumentFormatConverterProvider.getToHTMLConverter()
        .convert(new ByteArrayInputStream(rtfText.getBytes()), inFormat(rtf), htmlText,
        inFormat(html));
    return htmlText.toString();
  }

  /**
   * Gets the reception date of email.
   *
   * @param msg
   * @return
   * @throws Exception
   */
  protected static Date getMessageDate(MAPIMessage msg) throws Exception {
    Calendar messageDate = msg.getMessageDate();
    if (messageDate == null) {
      return extractDateOfReception(msg);
    }
    return messageDate.getTime();
  }

  /**
   * Extracts the reception date from an email.
   *
   * @param msg
   * @return
   * @throws Exception
   */
  protected static Date extractDateOfReception(MAPIMessage msg) throws Exception {
    if (msg.getMainChunks().messageHeaders != null) {
      String chunkContent = msg.getMainChunks().messageHeaders.getValue();
      int dateIdx = chunkContent.indexOf("Date: ");
      if (dateIdx >= 0) {
        chunkContent = chunkContent.substring(dateIdx + 6, chunkContent.indexOf("\n", dateIdx))
            .replaceAll("[\r\n]", "");
        return DATE_MAIL_FORMAT.parse(chunkContent);
      }
    }
    return null;
  }

  /**
   * Gets the file from its name.
   *
   * @param name
   * @return
   * @throws Exception
   */
  private File getDocumentFromName(final String name) throws Exception {
    return FileUtils
        .getFile(mavenTargetDirectoryRule.getResourceTestDirFile(), "org/silverpeas/core/mail", name);
  }
}
