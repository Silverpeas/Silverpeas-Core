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
package org.silverpeas.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.silverpeas.converter.DocumentFormatConverterFactory;
import com.silverpeas.util.StringUtil;

import com.stratelia.webactiv.util.DateUtil;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.silverpeas.converter.DocumentFormat.*;

/**
 * User: Yohann Chastagnier
 * Date: 21/01/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-mail-extractor.xml")
public class MsgMailExtractorTest {
  private static String FILENAME_MAIL_WITH_ATTACHMENTS = "mailWithAttachments.msg";

  private static DateFormat DATE_MAIL_FORMAT =
      new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

  /**
   * This unit test is support for MSG exploration solution testing.
   * @throws Exception
   */
  @Test
  public void readMailWithAttachmentsMSG() throws Exception {
    MAPIMessage msg =
        new MAPIMessage(getDocumentFromName(FILENAME_MAIL_WITH_ATTACHMENTS).getPath());
    msg.setReturnNullOnMissingChunk(true);

    System.out.println("FROM : " + msg.getDisplayFrom());

    System.out.println("TO : ");
    for (RecipientChunks recipientChunks : msg.getRecipientDetailsChunks()) {
      System.out.println("\t" + recipientChunks.getRecipientName() +
          " " + recipientChunks.getRecipientEmailAddress());
    }

    System.out.print("ATTACHMENTS : ");
    AttachmentChunks[] attachments = msg.getAttachmentFiles();
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
   * @param rtfText
   * @return
   * @throws Exception
   */
  protected String getRtfText(String rtfText) throws Exception {
    ByteArrayOutputStream htmlText = new ByteArrayOutputStream();
    DocumentFormatConverterFactory.getFactory().getToHTMLConverter()
        .convert(new ByteArrayInputStream(rtfText.getBytes()), inFormat(rtf), htmlText,
            inFormat(html));
    return htmlText.toString();
  }

  /**
   * Gets the reception date of email.
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
   * @param name
   * @return
   * @throws Exception
   */
  private File getDocumentFromName(final String name) throws Exception {
    final URL documentLocation = getClass().getResource(name);
    return new File(documentLocation.toURI());
  }
}
