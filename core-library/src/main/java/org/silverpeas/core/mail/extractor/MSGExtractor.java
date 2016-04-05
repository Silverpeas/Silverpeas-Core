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
package org.silverpeas.core.mail.extractor;

import org.silverpeas.core.contribution.converter.DocumentFormatConverterProvider;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.exception.SilverpeasException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.silverpeas.core.util.Charsets;

import static org.silverpeas.core.contribution.converter.DocumentFormat.*;

public class MSGExtractor implements MailExtractor {

  private MAPIMessage message;

  public MSGExtractor(File file) throws ExtractorException {
    try {
      message = new MAPIMessage(file.getPath());
    } catch (IOException e) {
      throw new ExtractorException("MSGExtractor.constructor", SilverpeasException.ERROR, "", e);
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
      throw new ExtractorException("MSGExtractor.init", SilverpeasException.ERROR, "", e);
    }
    message.setReturnNullOnMissingChunk(true);
  }

  @Override
  public Mail getMail() throws Exception {
    Mail mail = new Mail();
    try {
      if (message.getMessageDate() != null) {
        mail.setDate(message.getMessageDate().getTime());
      } else {
        mail.setDate(extractDateOfReception());
      }
    } catch (ChunkNotFoundException e) {
      SilverTrace.warn("util", "MSGExtractor.getMail()", "", e);
    }
    try {
      mail.setSubject(message.getSubject());
    } catch (ChunkNotFoundException e) {
      SilverTrace.warn("util", "MSGExtractor.getMail()", "", e);
    }

    Chunks mainChunks = message.getMainChunks();
    InternetAddress from = new InternetAddress(mainChunks.emailFromChunk.getValue(),
        mainChunks.displayFromChunk.getValue());
    mail.setFrom(from);

    String[] toNames = StringUtils.split(message.getDisplayTo(), ';');
    mail.setTo(getInChunks(toNames).toArray(new Address[toNames.length]));

    String[] ccNames = StringUtils.split(message.getDisplayCC(), ';');
    List<InternetAddress> ccAddresses = getInChunks(ccNames);
    if (ccAddresses != null && !ccAddresses.isEmpty()) {
      mail.setCc(getInChunks(ccNames).toArray(new Address[ccNames.length]));
    }

    try {
      String body = message.getHtmlBody();
      if (body == null) {
        body = getRtfText(message.getRtfBody());
        if (body == null) {
          body = EncodeHelper.javaStringToHtmlParagraphe(message.getTextBody());
        }
      }
      mail.setBody(body);
    } catch (ChunkNotFoundException e) {
      SilverTrace.warn("util", "MSGExtractor.getMail()", "", e);
    }
    return mail;
  }

  private List<InternetAddress> getInChunks(String[] names) throws UnsupportedEncodingException {
    List<InternetAddress> result = new ArrayList<InternetAddress>(names.length);
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
        InternetAddress address =
            new InternetAddress(recipient.getRecipientEmailAddress(), recipient.getRecipientName());
        return address;
      }
    }
    return null;
  }

  private Date extractDateOfReception() throws ParseException {
    if (message.getMainChunks().messageHeaders != null) {
      String chunkContent = message.getMainChunks().messageHeaders.getValue();
      int dateIdx = chunkContent.indexOf("Date: ");
      if (dateIdx >= 0) {
        chunkContent = chunkContent.substring(dateIdx + 6, chunkContent.indexOf('\n', dateIdx))
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
      return htmlText.toString(CharEncoding.UTF_8);
    } catch (Exception e) {
      SilverTrace.warn("util", "MSGExtractor.getRtfText()", "CANT_CONVERT_RTF_TO_HMTL_BODY", e);
    }
    return null;
  }

  @Override
  public List<MailAttachment> getAttachments() throws Exception {
    AttachmentChunks[] attachmentChunks = message.getAttachmentFiles();
    List<MailAttachment> mailAttachments = new ArrayList<MailAttachment>(attachmentChunks.length);
    for (AttachmentChunks attachment : attachmentChunks) {
      byte[] data = attachment.attachData.getValue();
      String fileName = attachment.attachLongFileName.getValue();
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
  }
}
