/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.util.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.MimeTypes;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class EMLExtractor implements MailExtractor {

  public static final CharSequenceTranslator ESCAPE_ISO8859_1 = new LookupTranslator(
      EntityArrays.ISO8859_1_ESCAPE());
  private MimeMessage message;

  public EMLExtractor(File file) throws ExtractorException {
    try {
      init(new FileInputStream(file));
    } catch (Exception e) {
      throw new ExtractorException("EMLExtractor.constructor", SilverpeasException.ERROR, "", e);
    }
  }

  public EMLExtractor(InputStream file) throws ExtractorException {
    try {
      init(file);
    } catch (MessagingException e) {
      throw new ExtractorException("EMLExtractor.constructor", SilverpeasException.ERROR, "", e);
    }
  }
  
  private void init(InputStream file) throws MessagingException {
    Properties props = System.getProperties();
    Session mailSession = Session.getDefaultInstance(props, null);
    
    message = new MimeMessage(mailSession, file);
  }

  public Mail getMail() throws Exception {
    
    Mail mail = new Mail();
    mail.setDate(message.getSentDate());
    mail.setSubject(message.getSubject());
    
    String body = null;
    Object messageContent = message.getContent();
    if (messageContent instanceof Multipart) {
      body = getBody((Multipart) messageContent);
    } else if (messageContent instanceof String) {
      body = (String) messageContent;
    }
    
    if (message.getFrom() != null) {
      mail.setFrom((InternetAddress) message.getFrom()[0]);
    }
    mail.setTo(message.getRecipients(Message.RecipientType.TO));
    mail.setCc(message.getRecipients(Message.RecipientType.CC));
    
    mail.setBody(ESCAPE_ISO8859_1.translate(body));
    return mail;
  }
  
  public List<MailAttachment> getAttachments() throws Exception {
    List<MailAttachment> attachments = new ArrayList<MailAttachment>();
    Object messageContent = message.getContent();
    if (messageContent instanceof Multipart) {
      processMultipart((Multipart) messageContent, attachments);
    }
    return attachments;
  }
  
  private String getBody(Multipart multipart) throws MessagingException, IOException {
    int partsNumber = multipart.getCount();
    String body = "";
    for (int i = 0; i < partsNumber; i++) {
      Part part = multipart.getBodyPart(i);
      if (part.getContentType().indexOf(MimeTypes.HTML_MIME_TYPE) >= 0) {
        // if present, return always HTML part
        return (String) part.getContent();
      } else if (part.getContentType().indexOf(MimeTypes.PLAIN_TEXT_MIME_TYPE) >= 0) {
        body = EncodeHelper.javaStringToHtmlParagraphe((String) part.getContent());
      }
    }
    return body;
  }

  private String processMultipart(Multipart multipart, List<MailAttachment> attachments) throws MessagingException, IOException {
    int partsNumber = multipart.getCount();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < partsNumber; i++) {
      Part part = multipart.getBodyPart(i);
      sb.append(processMailPart(part, attachments));
    }
    return sb.toString();
  }

  private String processMailPart(Part part, List<MailAttachment> attachments) throws MessagingException, IOException {
    if (!isTextPart(part)) {
      Object content = part.getContent();
      if (content instanceof Multipart) {
        Multipart mContent = (Multipart) content;
        return processMultipart(mContent, attachments);
      } else if (attachments != null) {
        String fileName = getFileName(part);
        if (fileName != null) {
          MailAttachment attachment = new MailAttachment(fileName);
          attachment.setFile(part.getInputStream());
          attachments.add(attachment);
        }
      }
    } else {
      if (part.getContentType().indexOf(MimeTypes.HTML_MIME_TYPE) >= 0) {
        return (String) part.getContent();
      } else if (part.getContentType().indexOf(MimeTypes.PLAIN_TEXT_MIME_TYPE) >= 0) {
        return EncodeHelper.javaStringToHtmlParagraphe((String) part.getContent());
      }
    }
    return "";
  }

  private boolean isTextPart(Part part) throws MessagingException {
    String disposition = part.getDisposition();
    if (!Part.ATTACHMENT.equals(disposition) &&
        !Part.INLINE.equals(disposition)) {
      try {
        ContentType type = new ContentType(part.getContentType());
        return "text".equalsIgnoreCase(type.getPrimaryType());
      } catch (ParseException e) {
        e.printStackTrace();
      }
    } else if (Part.INLINE.equals(disposition)) {
      try {
        ContentType type = new ContentType(part.getContentType());
        return "text".equalsIgnoreCase(type.getPrimaryType()) &&
            getFileName(part) == null;
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return false;
  }
  
  private static String getFileName(Part part) throws MessagingException {
    String fileName = part.getFileName();
    if (fileName == null) {
      try {
        ContentType type = new ContentType(part.getContentType());
        fileName = type.getParameter("name");
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return fileName;
  }

}
