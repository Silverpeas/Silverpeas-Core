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
 * FLOSS exception.  You should have received a copy of the text describing
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

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.Map;

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

  @Override
  public void ejbCreate() {
    ResourceLocator mailerSettings = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.smtp.smtpSettings", "");
    m_Host = mailerSettings.getString("SMTPServer");
    m_SmtpAuthentication = mailerSettings.getBoolean("SMTPAuthentication", false);
    if (m_SmtpAuthentication) {
      m_Port = Integer.parseInt(mailerSettings.getString("SMTPPort"));
      m_User = mailerSettings.getString("SMTPUser");
      m_Pwd = mailerSettings.getString("SMTPPwd");
    }
    m_SmtpDebug = mailerSettings.getBoolean("SMTPDebug", false);
    isSmtpSecure = mailerSettings.getBoolean("SMTPSecure", false);
  }

  /**
   * listener of NotificationServer JMS message
   * @param msg the message recieved
   */
  @Override
  public void onMessage(javax.jms.Message msg) {
    try {
      SilverTrace.info("smtp", "SMTPListner.onMessage()", "root.MSG_GEN_PARAM_VALUE",
          "JMS Message = " + msg.toString());
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverTrace.error("smtp", "SMTPListner.onMessage()",
          "smtp.EX_CANT_PROCESS_MSG", "JMS Message = " + msg.toString()
          + ", Payload = " + m_payload == null ? "" : m_payload, e);
    }
  }

  @Override
  public void send(NotificationData p_Message) throws NotificationServerException {
    // process the target param string, containing the FROM and the SUBJECT
    // email fields.
    Map<String, Object> keyValue = p_Message.getTargetParam();
    String tmpFromString = (String) keyValue.get("FROM");
    String tmpSubjectString = (String) keyValue.get("SUBJECT");
    String tmpUrlString = (String) keyValue.get("URL");
    String tmpLanguageString = (String) keyValue.get("LANGUAGE");
    String tmpAttachmentIdString = (String) keyValue.get("ATTACHMENTID");
    String tmpSourceString = (String) keyValue.get("SOURCE");

    if (tmpSourceString != null && tmpSourceString.length() > 0) {
      tmpSubjectString = tmpSourceString + " : " + tmpSubjectString;
    }

    if (tmpLanguageString == null) {
      tmpLanguageString = I18NHelper.defaultLanguage;
    }

    ResourceLocator messages = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.smtp.multilang.smtpBundle",
        tmpLanguageString);
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
    properties.put("mail.smtp.auth", String.valueOf(m_SmtpAuthentication));
    javax.mail.Session session = javax.mail.Session.getInstance(properties, null);
    session.setDebug(m_SmtpDebug); // print on the console all SMTP messages.
    try {
      InternetAddress fromAddress = new InternetAddress(pFrom);
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
      email.setHeader("Precedence", "list");
      email.setHeader("List-ID", pFrom);
      String subject = pSubject;
      if (subject == null) {
        subject = "";
      }
      String content = pMessage;
      if (content == null) {
        content = "";
      }
      email.setSubject(subject, "UTF-8");
      if (content.toLowerCase().indexOf("<html>") != -1 || htmlFormat) {
        email.setContent(content, "text/html; charset=\"UTF-8\"");
      } else {
        email.setText(content, "UTF-8");
      }
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
      } else {
        transport.connect();
      }

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
}
