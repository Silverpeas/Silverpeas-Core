/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationserver.channel.smtp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class SMTPListener extends AbstractListener {
  private String m_Host;
  private String m_User;
  private String m_Pwd;
  private int m_Port;
  private boolean m_SmtpAuthentication;
  private boolean m_SmtpDebug;
  private boolean isSmtpSecure = false;

  public SMTPListener() {
  }

  public void ejbCreate() {
    ResourceLocator mailerSettings = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.smtp.smtpSettings",
        "");

    m_Host = mailerSettings.getString("SMTPServer");
    m_SmtpAuthentication = mailerSettings.getBoolean("SMTPAuthentication",
        false);
    if (m_SmtpAuthentication) {
      m_Port = new Integer(mailerSettings.getString("SMTPPort")).intValue();
      m_User = mailerSettings.getString("SMTPUser");
      m_Pwd = mailerSettings.getString("SMTPPwd");
    }
    m_SmtpDebug = mailerSettings.getBoolean("SMTPDebug", false);
    isSmtpSecure = mailerSettings.getBoolean("SMTPSecure", false);
  }

  /**
   * listener of NotificationServer JMS message
   */
  @Override
  public void onMessage(javax.jms.Message msg) {
    try {
      SilverTrace.info("smtp", "SMTPListner.onMessage()",
          "root.MSG_GEN_PARAM_VALUE", "JMS Message = " + msg.toString());
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverTrace.error("smtp", "SMTPListner.onMessage()",
          "smtp.EX_CANT_PROCESS_MSG", "JMS Message = " + msg.toString()
          + ", Payload = " + m_payload == null ? "" : m_payload, e);
    }
  }

  @Override
  public void send(NotificationData p_Message)
      throws NotificationServerException {
    String tmpFromString = null;
    String tmpSubjectString = null;
    String tmpUrlString = null;
    String tmpLanguageString = null;
    String tmpAttachmentIdString = null;
    String tmpSourceString = null;
    // process the target param string, containing the FROM and the SUBJECT
    // email fields.
    try {
      Hashtable keyValue = p_Message.getTargetParam();

      tmpFromString = (String) keyValue.get("FROM"); // retrieves the FROM key
      // value.
      tmpSubjectString = (String) keyValue.get("SUBJECT"); // retrieves the
      // SUBJECT key value.
      tmpUrlString = (String) keyValue.get("URL"); // retrieves the URL key
      // value.
      tmpLanguageString = (String) keyValue.get("LANGUAGE"); // retrieves the
      // LANGUAGE key
      // value.
      tmpAttachmentIdString = (String) keyValue.get("ATTACHMENTID");
      tmpSourceString = (String) keyValue.get("SOURCE");

      if (tmpSourceString != null && tmpSourceString.length() > 0) {
        tmpSubjectString = tmpSourceString + " : " + tmpSubjectString;
      }
    } catch (IllegalArgumentException e) {
      throw new NotificationServerException("SMTPListner.send()",
          SilverpeasException.ERROR, "smtp.EX_NO_TARGET_AVAILABLE", e);
    } catch (NoSuchElementException e) {
      throw new NotificationServerException("SMTPListner.send()",
          SilverpeasException.ERROR, "smtp.EX_MISSING_PARAMETER", e);
    }

    // if no LANGUAGE field was entered, then take "fr"
    if (tmpLanguageString == null)
      tmpLanguageString = "fr";

    ResourceLocator messages = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.smtp.multilang.smtpBundle",
        tmpLanguageString);
    // if no FROM field was entered, then add an error. If not, send the email.
    if (tmpFromString == null) {
      throw new NotificationServerException("SMTPListner.send()",
          SilverpeasException.ERROR, "smtp.EX_MISSING_FROM");
    } else {
      String body = p_Message.getMessage();
      if (tmpUrlString != null) {
        // Transform text to html format
        body = EncodeHelper.javaStringToHtmlParagraphe(body + "\n\n");
        body += "<a href=\"" + tmpUrlString + "\" target=_blank>"
            + messages.getString("clickHere") + "</a> "
            + messages.getString("ToAccessDocument");
      }

      if (tmpAttachmentIdString == null) {
        sendEmail(tmpFromString, p_Message.getTargetReceipt(),
            tmpSubjectString, body, (tmpUrlString != null));
      } else {
        // For the moment, send the email without attachment
        sendEmail(tmpFromString, p_Message.getTargetReceipt(),
            tmpSubjectString, body, false);
      }
    }
  }

  /**
   * send email to destination using SMTP protocol and JavaMail 1.3 API (compliant with MIME
   * format).
   * @param pFrom : from field that will appear in the email header.
   * @param pTo : the email target destination.
   * @param pSubject : the subject of the email.
   * @param pMessage : the message or payload of the email.
   */
  private void sendEmail(String pFrom, String pTo, String pSubject,
      String pMessage, boolean htmlFormat) throws NotificationServerException {
    // retrieves system properties and set up Delivery Status Notification
    // @see RFC1891
    Transport transport = null;

    Properties properties = System.getProperties();
    properties.put("mail.smtp.host", m_Host);
    properties.put("mail.smtp.auth", new Boolean(m_SmtpAuthentication)
        .toString());
    javax.mail.Session session = javax.mail.Session.getInstance(properties, null);
    session.setDebug(m_SmtpDebug); // print on the console all SMTP messages.
    try {
      InternetAddress fromAddress = new InternetAddress(pFrom); // use InternetAddress
      // structure.
      InternetAddress[] toAddress = null;
      // parsing destination address for compliance with RFC822
      try {
        toAddress = InternetAddress.parse(pTo, false);
      } catch (AddressException e) {
        SilverTrace.warn("smtp", "SMTPListner.sendEmail()",
            "root.MSG_GEN_PARAM_VALUE", "From = " + pFrom + ", To = " + pTo);
      }
      MimeMessage email = new MimeMessage(session);
      email.setFrom(fromAddress);
      email.setRecipients(javax.mail.Message.RecipientType.TO, toAddress);
      email.setSubject(pSubject == null ? "" : pSubject, "ISO-8859-1");
      if (pMessage.toLowerCase().indexOf("<html>") != -1 || htmlFormat) {
        // create and fill the first message
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setDataHandler(new DataHandler(new ByteArrayDataSource(pMessage,
            "text/html; charset=\"iso-8859-1\"")));

        // create the Multipart and its parts to it
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);

        // add the Multipart to the message
        email.setContent(mp);
      } else
        email.setText(pMessage == null ? "" : pMessage, "ISO-8859-1");
      // set the Date: header
      email.setSentDate(new Date());

      // create a Transport connection (TCP)
      if (isSmtpSecure) {
        transport = session.getTransport(SMTPConstant.SECURE_TRANSPORT);
      } else {
        transport = session.getTransport(SMTPConstant.SIMPLE_TRANSPORT);
      }
      if (m_SmtpAuthentication) {
        SilverTrace.info("smtp", "SMTPListner.sendEmail()",
            "root.MSG_GEN_PARAM_VALUE", "m_Host = " + m_Host + " m_Port="
            + m_Port + " m_User=" + m_User);
        transport.connect(m_Host, m_Port, m_User, m_Pwd);
      } else
        transport.connect();

      transport.sendMessage(email, toAddress);
    } catch (Exception e) {
      throw new NotificationServerException("SMTPListner.sendEmail()",
          SilverpeasException.ERROR, "smtp.EX_CANT_SEND_SMTP_MESSAGE", e);
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (Exception e) {
          SilverTrace.error("smtp", "SMTPListner.sendEmail()",
              "root.EX_IGNORED", "ClosingTransport", e);
        }
      }
    }
  }

  class ByteArrayDataSource implements DataSource {
    private byte[] data; // data
    private String type; // content-type

    /* Create a DataSource from an input stream */
    public ByteArrayDataSource(InputStream is, String type) {
      this.type = type;
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int ch;

        while ((ch = is.read()) != -1)
          // XXX - must be made more efficient by
          // doing buffered reads, rather than one byte reads
          os.write(ch);
        data = os.toByteArray();

      } catch (IOException ioex) {
      }
    }

    /* Create a DataSource from a byte array */
    public ByteArrayDataSource(byte[] data, String type) {
      this.data = data;
      this.type = type;
    }

    /* Create a DataSource from a String */
    public ByteArrayDataSource(String data, String type) {
      try {
        // Assumption that the string contains only ASCII
        // characters! Otherwise just pass a charset into this
        // constructor and use it in getBytes()
        this.data = data.getBytes("iso-8859-1");
      } catch (UnsupportedEncodingException uex) {
      }
      this.type = type;
    }

    /**
     * Return an InputStream for the data. Note - a new stream must be returned each time.
     */
    @Override
    public InputStream getInputStream() throws IOException {
      if (data == null)
        throw new IOException("no data");
      return new ByteArrayInputStream(data);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new IOException("cannot do this");
    }

    @Override
    public String getContentType() {
      return type;
    }

    @Override
    public String getName() {
      return "dummy";
    }
  }
}