package org.silverpeas.util.mail;

import static com.silverpeas.converter.DocumentFormat.html;
import static com.silverpeas.converter.DocumentFormat.inFormat;
import static com.silverpeas.converter.DocumentFormat.rtf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import com.silverpeas.converter.DocumentFormatConverterFactory;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MSGExtractor implements MailExtractor {

  private MAPIMessage message;
  private static final DateFormat DATE_MAIL_FORMAT =
    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

  public MSGExtractor(File file) throws Exception {
    message = new MAPIMessage(file.getPath());
    message.setReturnNullOnMissingChunk(true);
  }

  public MSGExtractor(InputStream file) throws Exception {
    init(file);
  }

  private void init(InputStream file) throws Exception {
    message = new MAPIMessage(file);
    message.setReturnNullOnMissingChunk(true);
  }

  public Mail getMail() throws Exception {

    Mail mail = new Mail();
    try {
      Date date = null;
      if (message.getMessageDate() != null) {
        date = message.getMessageDate().getTime();
      } else {
        date = extractDateOfReception();
      }
      mail.setDate(date);
    } catch (ChunkNotFoundException e) {
      SilverTrace.warn("info", "MSGExtractor.getMail()", "", e);
    }
    try {
      mail.setSubject(message.getSubject());
    } catch (ChunkNotFoundException e) {
      SilverTrace.warn("info", "MSGExtractor.getMail()", "", e);
    }

    Chunks mainChunks = message.getMainChunks();
    InternetAddress from =
        new InternetAddress(mainChunks.emailFromChunk.getValue(),
            mainChunks.displayFromChunk.getValue());
    mail.setFrom(from);
    
    String[] toNames = StringUtils.split(message.getDisplayTo(), ";");
    mail.setTo(getInChunks(toNames).toArray(new Address[0]));
    
    String[] ccNames = StringUtils.split(message.getDisplayCC(), ";");
    List<InternetAddress> ccAddresses = getInChunks(ccNames);
    if (ccAddresses != null && !ccAddresses.isEmpty()) {
      mail.setCc(getInChunks(ccNames).toArray(new Address[0]));
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
      SilverTrace.warn("info", "MSGExtractor.getMail()", "", e);
    }
    return mail;
  }
  
  private List<InternetAddress> getInChunks(String[] names) throws UnsupportedEncodingException {
    List<InternetAddress> result = new ArrayList<InternetAddress>();
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
        chunkContent = chunkContent.substring(dateIdx + 6, chunkContent.indexOf("\n", dateIdx))
            .replaceAll("[\r\n]", "");
        return DATE_MAIL_FORMAT.parse(chunkContent);
      }
    }
    return null;
  }
  
  /**
   * Gets readable string from RTF text.
   * @param rtfText
   * @return
   * @throws Exception
   */
  private String getRtfText(String rtfText) {
    try {
      ByteArrayOutputStream htmlText = new ByteArrayOutputStream();
      DocumentFormatConverterFactory.getFactory().getToHTMLConverter()
          .convert(new ByteArrayInputStream(rtfText.getBytes()), inFormat(rtf), htmlText,
              inFormat(html));
      return htmlText.toString();
    } catch (Exception e) {
      SilverTrace.warn("util", "MSGExtractor.getRtfText()", "CANT_CONVERT_RTF_TO_HMTL_BODY", e);
    }
    return null;
  }

  @Override
  public List<MailAttachment> getAttachments() throws Exception {
    List<MailAttachment> mailAttachments = new ArrayList<MailAttachment>();
    AttachmentChunks[] attachmentChunks = message.getAttachmentFiles();
    for (AttachmentChunks attachment : attachmentChunks) {
      byte[] data = attachment.attachData.getValue();
      
      MailAttachment mailAttachment = new MailAttachment(attachment.attachLongFileName.getValue());
      mailAttachment.setFile(new ByteArrayInputStream(data));
      
      mailAttachments.add(mailAttachment);
    }
    return mailAttachments;
  }

}
