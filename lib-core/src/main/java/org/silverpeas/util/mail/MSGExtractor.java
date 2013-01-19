package org.silverpeas.util.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MSGExtractor implements MailExtractor {

  MAPIMessage message;

  public MSGExtractor(File file) throws Exception {
    init(new FileInputStream(file));
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
      mail.setDate(message.getMessageDate());
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

    RecipientChunks[] recipientChunks = message.getRecipientDetailsChunks();
    List<Address> addresses = new ArrayList<Address>();
    for (RecipientChunks recipient : recipientChunks) {
      InternetAddress address =
          new InternetAddress(recipient.getRecipientEmailAddress(), recipient.getRecipientName());
      addresses.add(address);
    }
    mail.setTo(addresses.toArray(new Address[0]));

    try {
      String body = message.getHtmlBody();
      if (body == null) {
        body = message.getRtfBody();
        if (body == null) {
          body = message.getTextBody();
        }
      }
      mail.setBody(body);
    } catch (ChunkNotFoundException e) {
      SilverTrace.warn("info", "MSGExtractor.getMail()", "", e);
    }
    return mail;
  }

  @Override
  public List<MailAttachment> getAttachments() throws Exception {
    List<MailAttachment> mailAttachments = new ArrayList<MailAttachment>();
    AttachmentChunks[] attachmentChunks = message.getAttachmentFiles();
    for (AttachmentChunks attachment : attachmentChunks) {
      byte[] data = attachment.attachData.getValue();
      
      MailAttachment mailAttachment = new MailAttachment(attachment.attachFileName.getValue());
      mailAttachment.setFile(new ByteArrayInputStream(data));
      
      mailAttachments.add(mailAttachment);
    }
    return mailAttachments;
  }

}
