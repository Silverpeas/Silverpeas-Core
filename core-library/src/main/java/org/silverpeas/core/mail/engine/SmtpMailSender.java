/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.mail.engine;

import org.silverpeas.core.mail.MailAddress;
import org.silverpeas.core.mail.MailToSend;
import org.silverpeas.core.mail.ReceiverMailAddressSet;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.util.MailUtil;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This is the SMTP implementation of the {@link MailSender} interface.
 * This implementation uses the parameters provided by {@link MailUtil}.
 * @author Yohann Chastagnier
 */
public class SmtpMailSender implements MailSender {

  /**
   * Retrieves the system properties and configure a mail session.
   * @return an initialized session.
   * @see <code>RFC1891</code>
   */
  private Session getMailSession(SmtpConfiguration smtpConfiguration) {
    Properties properties = System.getProperties();
    properties.put("mail.smtp.host", smtpConfiguration.getServer());
    properties.put("mail.smtp.auth", String.valueOf(smtpConfiguration.isAuthenticate()));
    Session session = Session.getInstance(properties, null);
    // print on the console all SMTP messages.
    session.setDebug(smtpConfiguration.isDebug());
    // Returning the session.
    return session;
  }

  @Override
  public void send(final MailToSend mail) {
    SmtpConfiguration smtpConfiguration = SmtpConfiguration.fromDefaultSettings();

    MailAddress fromMailAddress = mail.getFrom();
    Session session = getMailSession(smtpConfiguration);
    try {
      InternetAddress fromAddress = fromMailAddress.getAuthorizedInternetAddress();
      InternetAddress replyToAddress = null;
      List<InternetAddress[]> toAddresses = new ArrayList<>();

      // Parsing destination address for compliance with RFC822.
      final Collection<ReceiverMailAddressSet> addressBatches =
          mail.getTo().getBatchedReceiversList();
      for (ReceiverMailAddressSet addressBatch : addressBatches) {
        try {
          toAddresses.add(InternetAddress.parse(addressBatch.getEmailsSeparatedByComma(), false));
        } catch (AddressException e) {
          SilverTrace.warn("mail", "MailSender.send()", "root.MSG_GEN_PARAM_VALUE",
              "From = " + fromMailAddress + ", To = " + addressBatch.getEmailsSeparatedByComma(),
              e);
        }
      }
      try {
        if (mail.isReplyToRequired()) {
          replyToAddress = new InternetAddress(fromMailAddress.getEmail(), false);
          if (StringUtil.isDefined(fromMailAddress.getName())) {
            replyToAddress.setPersonal(fromMailAddress.getName(), Charsets.UTF_8.name());
          }
        }
      } catch (AddressException e) {
        SilverTrace.warn("mail", "MailSender.send()", "root.MSG_GEN_PARAM_VALUE",
            "ReplyTo = " + fromMailAddress + " is malformed.", e);
      }
      MimeMessage email = new MimeMessage(session);
      email.setFrom(fromAddress);
      if (replyToAddress != null) {
        email.setReplyTo(new InternetAddress[]{replyToAddress});
      }
      email.setHeader("Precedence", "list");
      email.setHeader("List-ID", fromAddress.getAddress());
      email.setSentDate(new Date());
      email.setSubject(mail.getSubject(), CharEncoding.UTF_8);
      mail.getContent().applyOn(email);

      // Sending.
      performSend(mail, smtpConfiguration, session, email, toAddresses);

    } catch (MessagingException | UnsupportedEncodingException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method performs the treatment of the technical send:
   * <ul>
   * <li>connection to the SMTP server</li>
   * <li>sending</li>
   * <li>closing the connection</li>
   * </ul>
   * @param mail the original data from which the given {@link MimeMessage} has been initialized.
   * @param smtpConfiguration the SMTP configuration.
   * @param session the current mail session.
   * @param messageToSend the technical message to send.
   * @param batchedToAddresses the receivers of the message.
   * @throws MessagingException
   */
  private void performSend(final MailToSend mail, final SmtpConfiguration smtpConfiguration,
      Session session, MimeMessage messageToSend, List<InternetAddress[]> batchedToAddresses)
      throws MessagingException {

    // Creating a Transport connection (TCP)
    final Transport transport;
    if (smtpConfiguration.isSecure()) {
      transport = session.getTransport(SmtpConfiguration.SECURE_TRANSPORT);
    } else {
      transport = session.getTransport(SmtpConfiguration.SIMPLE_TRANSPORT);
    }

    // Adding send reporting listener
    transport.addTransportListener(new SmtpMailSendReportListener(mail));

    try {
      if (smtpConfiguration.isAuthenticate()) {
        transport.connect(smtpConfiguration.getServer(), smtpConfiguration.getPort(),
            smtpConfiguration.getUsername(), smtpConfiguration.getPassword());
      } else {
        transport.connect(smtpConfiguration.getServer(), smtpConfiguration.getPort(), null, null);
      }

      for (InternetAddress[] toAddressBatch : batchedToAddresses) {
        messageToSend
            .setRecipients(mail.getTo().getRecipientType().getTechnicalType(), toAddressBatch);
        transport.sendMessage(messageToSend, toAddressBatch);
      }

    } finally {
      try {
        transport.close();
      } catch (Exception e) {
        SilverTrace.
            error("mail", "SmtpMailSender.send()", "root.EX_IGNORED", "ClosingTransport", e);
      }
    }
  }
}
