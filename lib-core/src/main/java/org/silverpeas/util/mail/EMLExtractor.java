package org.silverpeas.util.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class EMLExtractor implements MailExtractor {

  public static final CharSequenceTranslator ESCAPE_ISO8859_1 = new LookupTranslator(
      EntityArrays.ISO8859_1_ESCAPE());
  private MimeMessage message;

  public EMLExtractor(File file) throws FileNotFoundException, MessagingException {
    init(new FileInputStream(file));
  }
  
  public EMLExtractor(InputStream file) throws MessagingException {
    init(file);
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
    
    String content = null;
    Object messageContent = message.getContent();
    if (messageContent instanceof Multipart) {
      content = processMultipart((Multipart) messageContent, null);
    } else if (messageContent instanceof String) {
      content = (String) messageContent;
    }
    
    if (message.getFrom() != null) {
      mail.setFrom((InternetAddress) message.getFrom()[0]);
    }
    mail.setTo(message.getRecipients(Message.RecipientType.TO));
    mail.setCc(message.getRecipients(Message.RecipientType.CC));
    
    mail.setBody(ESCAPE_ISO8859_1.translate(content));
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
        return EncodeHelper.javaStringToHtmlParagraphe(part.getContentType());
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
