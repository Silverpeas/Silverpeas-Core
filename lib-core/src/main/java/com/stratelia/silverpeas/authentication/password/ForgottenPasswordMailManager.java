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

package com.stratelia.silverpeas.authentication.password;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.silverpeas.util.MimeTypes;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.util.ResourceLocator;

public class ForgottenPasswordMailManager {

  private static final String TRANSPORT_SMTP = "smtp";
  private static final String TRANSPORT_SMTPS = "smtps";
  private static final String SUBJECT_ENCODING = "ISO-8859-1";

  private static final String PREFIX_RESET_PASSWORD_REQUEST = "resetPasswordRequest";
  private static final String PREFIX_NEW_PASSWORD = "newPassword";
  private static final String PREFIX_ERROR = "error";
  private static final String PREFIX_ADMIN = "admin";
  private static final String CONTENT = "content";
  private static final String SUBJECT = "subject";

  private ResourceLocator resource = new ResourceLocator(
      "com.silverpeas.authentication.multilang.forgottenPasswordMail", "");

  // SMTP parameters
  private String smtpHost;
  private boolean smtpAuthentication;
  private boolean smtpSecure;
  private boolean smtpDebug;
  private int smtpPort;
  private String smtpUser;
  private String smtpPwd;
  private Session session;
  private String fromAddress;
  private String fromName;
  private String adminEmail;

  public ForgottenPasswordMailManager(Admin admin) {
    initSmtpParameters();
    initFromAddress(admin);
  }

  private void initSmtpParameters() {
    ResourceLocator smtpSettings = new ResourceLocator(
        "com.stratelia.silverpeas.notificationserver.channel.smtp.smtpSettings", "");
    smtpHost = smtpSettings.getString("SMTPServer");
    smtpAuthentication = smtpSettings.getBoolean("SMTPAuthentication", false);
    smtpDebug = smtpSettings.getBoolean("SMTPDebug", false);
    smtpPort = Integer.parseInt(smtpSettings.getString("SMTPPort"));
    smtpUser = smtpSettings.getString("SMTPUser");
    smtpPwd = smtpSettings.getString("SMTPPwd");
    smtpSecure = smtpSettings.getBoolean("SMTPSecure", false);

    Properties props = System.getProperties();
    props.put("mail.smtp.host", smtpHost);
    props.put("mail.smtp.auth", String.valueOf(smtpAuthentication));
    session = Session.getInstance(props, null);
    session.setDebug(smtpDebug); // print on the console all SMTP messages.
  }

  private void initFromAddress(Admin admin) {
    ResourceLocator mailSettings = new ResourceLocator(
        "com.silverpeas.authentication.settings.forgottenPasswordMail", "");
    adminEmail = mailSettings.getString("admin.mail", admin.getAdministratorEmail());
    fromAddress = mailSettings.getString("fromAddress", adminEmail);
    fromName = mailSettings.getString("fromName", "Silverpeas");
  }

  public void sendResetPasswordRequestMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    sendMail(parameters, PREFIX_RESET_PASSWORD_REQUEST);
  }

  public void sendNewPasswordMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    sendMail(parameters, PREFIX_NEW_PASSWORD);
  }

  public void sendErrorMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    parameters.setToAddress(adminEmail);
    sendMail(parameters, PREFIX_ERROR);
  }

  public void sendAdminMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    parameters.setToAddress(adminEmail);
    sendMail(parameters, PREFIX_ADMIN);
  }

  private void sendMail(ForgottenPasswordMailParameters parameters, String resourcePrefix)
      throws MessagingException {
    parameters.setSubject(resource.getString(resourcePrefix + "." + SUBJECT));
    parameters.setContent(resource.getString(resourcePrefix + "." + CONTENT));
    sendMail(parameters);
  }

  private void sendMail(ForgottenPasswordMailParameters parameters) throws MessagingException {
    Transport transport = null;
    try {
      MimeMessage msg = new MimeMessage(session);
      try {
        msg.setFrom(new InternetAddress(fromAddress, fromName, "UTF-8"));
      } catch (UnsupportedEncodingException e1) {
        msg.setFrom(new InternetAddress(fromAddress));
      }
      msg.setSubject(parameters.getSubject(), SUBJECT_ENCODING);
      msg.setContent(parameters.getFilledContent(), MimeTypes.HTML_MIME_TYPE);
      msg.setSentDate(new Date());

      // create a Transport connection
      if (smtpSecure) {
        transport = session.getTransport(TRANSPORT_SMTPS);
      } else {
        transport = session.getTransport(TRANSPORT_SMTP);
      }

      // redefine the TransportListener interface.
      TransportListener transportListener = new TransportListener() {
        public void messageDelivered(TransportEvent e) {
        }

        public void messageNotDelivered(TransportEvent e) {
        }

        public void messagePartiallyDelivered(TransportEvent e) {
        }
      };

      transport.addTransportListener(transportListener);

      InternetAddress[] addresses = { new InternetAddress(parameters.getToAddress()) };
      msg.setRecipients(Message.RecipientType.TO, addresses);
      // add Transport Listener to the transport connection.
      if (smtpAuthentication) {
        SilverTrace.info("Bus - peasCore", "ForgottenPasswordMailManager.sendMail()",
            "root.MSG_GEN_PARAM_VALUE", "host = " + smtpHost + " m_Port=" + smtpPort
            + " m_User=" + smtpUser);
        transport.connect(smtpHost, smtpPort, smtpUser, smtpPwd);
        msg.saveChanges();
      } else {
        transport.connect();
      }
      transport.sendMessage(msg, addresses);
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (Exception e) {
          SilverTrace.error("Bus - peasCore", "ForgottenPasswordMailManager.sendMail()",
              "root.EX_IGNORED", "ClosingTransport", e);
        }
      }
    }
  }

}
