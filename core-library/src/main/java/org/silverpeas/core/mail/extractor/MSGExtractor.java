/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.silverpeas.core.contribution.converter.DocumentFormatConverterProvider;
import org.silverpeas.core.mail.MailException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.silverpeas.core.contribution.converter.DocumentFormat.*;

public class MSGExtractor implements MailExtractor {

  private MAPIMessage message;

  public MSGExtractor(File file) throws ExtractorException {
    try {
      message = new MAPIMessage(file.getPath());
    } catch (IOException e) {
      throw new ExtractorException(e);
    }
    message.setReturnNullOnMissingChunk(true);
  }

  public MSGExtractor(InputStream file) throws ExtractorException {
    init(file);
  }

  private void init(InputStream file) throws ExtractorException {
    try {
      message = new MAPIMessage(file);
    } catch (IOException e) {
      throw new ExtractorException(e);
    }
    message.setReturnNullOnMissingChunk(true);
  }

  @Override
  public Mail getMail() throws MailException {
    try {
      Mail mail = new Mail();
      setMailDate(mail);
      setMailSubject(mail);

      Chunks mainChunks = message.getMainChunks();
      InternetAddress from = new InternetAddress(mainChunks.getEmailFromChunk().getValue(),
          mainChunks.getDisplayFromChunk().getValue());
      mail.setFrom(from);

      String[] toNames = StringUtils.split(message.getDisplayTo(), ';');
      mail.setTo(getInChunks(toNames).toArray(new Address[toNames.length]));

      String[] ccNames = StringUtils.split(message.getDisplayCC(), ';');
      List<InternetAddress> ccAddresses = getInChunks(ccNames);
      if (!ccAddresses.isEmpty()) {
        mail.setCc(getInChunks(ccNames).toArray(new Address[ccNames.length]));
      }

      setMailBody(mail);
      return mail;
    } catch (ChunkNotFoundException | ParseException | UnsupportedEncodingException e) {
      throw new MailException(e);
    }
  }

  private void setMailDate(final Mail mail) throws ParseException {
    try {
      if (message.getMessageDate() != null) {
        mail.setDate(message.getMessageDate().getTime());
      } else {
        mail.setDate(extractDateOfReception());
      }
    } catch (ChunkNotFoundException e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }

  private void setMailSubject(final Mail mail) {
    try {
      mail.setSubject(message.getSubject());
    } catch (ChunkNotFoundException e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }

  private void setMailBody(final Mail mail) {
    try {
      String body = message.getHtmlBody();
      if (body == null) {
        body = getRtfText(message.getRtfBody());
        if (body == null) {
          body = WebEncodeHelper.javaStringToHtmlParagraphe(message.getTextBody());
        }
      }
      mail.setBody(body);
    } catch (ChunkNotFoundException e) {
      SilverLogger.getLogger(this).warn(e);
    }
  }

  private List<InternetAddress> getInChunks(String[] names) throws UnsupportedEncodingException {
    List<InternetAddress> result = new ArrayList<>(names.length);
    for (String name : names) {
      InternetAddress address = getInChunks(name.trim());
      if (address != null) {
        result.add(address);
      }
    }
    return result;
  }

  private InternetAddress getInChunks(String name) throws UnsupportedEncodingException {
    RecipientChunks[] recipientChunks = message.getRecipientDetailsChunks();
    for (RecipientChunks recipient : recipientChunks) {
      if (name.equals(recipient.getRecipientName())) {
        return
            new InternetAddress(recipient.getRecipientEmailAddress(), recipient.getRecipientName());
      }
    }
    return null;
  }

  private Date extractDateOfReception() throws ParseException {
    if (message.getMainChunks().getMessageHeaders() != null) {
      String chunkContent = message.getMainChunks().getMessageHeaders().getValue();
      int dateIdx = chunkContent.indexOf("Date: ");
      if (dateIdx >= 0) {
        final int dateValueIdx = 6;
        chunkContent =
            chunkContent.substring(dateIdx + dateValueIdx, chunkContent.indexOf('\n', dateIdx))
            .replaceAll("[\r\n]", "");
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(
            chunkContent);
      }
    }
    return null;
  }

  /**
   * Gets readable string from RTF text.
   *
   * @param rtfText
   * @return
   * @throws Exception
   */
  private String getRtfText(String rtfText) {
    try {
      ByteArrayOutputStream htmlText = new ByteArrayOutputStream();
      DocumentFormatConverterProvider.getToHTMLConverter().convert(
          new ByteArrayInputStream(rtfText.getBytes(Charsets.UTF_8)), inFormat(rtf), htmlText,
          inFormat(html));
      return htmlText.toString(StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Cannot convert RTF to HTML", e);
    }
    return null;
  }

  @Override
  public List<MailAttachment> getAttachments() throws MailException {
    try {
      AttachmentChunks[] attachmentChunks = message.getAttachmentFiles();
      List<MailAttachment> mailAttachments = new ArrayList<>(attachmentChunks.length);
      for (AttachmentChunks attachment : attachmentChunks) {
        byte[] data = attachment.getAttachData().getValue();
        String fileName = attachment.getAttachLongFileName().getValue();
        MailAttachment mailAttachment = new MailAttachment(fileName);
        String dir = FileRepositoryManager.getTemporaryPath() + "mail" + Calendar.getInstance().
            getTimeInMillis();
        File file = new File(dir, fileName);
        FileUtils.writeByteArrayToFile(file, data);
        mailAttachment.setPath(file.getAbsolutePath());
        mailAttachment.setSize(file.length());
        mailAttachments.add(mailAttachment);
      }
      return mailAttachments;
    } catch (IOException e) {
      throw new MailException(e);
    }
  }
}
