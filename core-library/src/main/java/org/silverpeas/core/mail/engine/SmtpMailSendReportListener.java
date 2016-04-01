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

import org.silverpeas.core.mail.MailToSend;
import org.silverpeas.core.silvertrace.SilverTrace;

import javax.mail.Address;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import java.text.MessageFormat;

/**
 * This listener permits to get some reporting about a send of an mail.
 * @author Yohann Chastagnier
 */
public class SmtpMailSendReportListener implements TransportListener {

  private final MailToSend mailToSend;

  public SmtpMailSendReportListener(final MailToSend mailToSend) {
    this.mailToSend = mailToSend;
  }

  @Override
  public void messageDelivered(final TransportEvent e) {
    String logMessage = MessageFormat
        .format("Mail with subject ''{0}'' has been successfully delivered.",
            mailToSend.getSubject());
    report(e);
  }

  @Override
  public void messageNotDelivered(final TransportEvent e) {
    String message = MessageFormat
        .format("Mail with subject ''{0}'' has not been delivered.", mailToSend.getSubject());
    SilverTrace
        .error("mail", "SmtpMailSendReportListener.messageDelivered()", "root.MSG_GEN_PARAM_VALUE",
            message);
    report(e);
  }

  @Override
  public void messagePartiallyDelivered(final TransportEvent e) {
    String message = MessageFormat
        .format("Mail with subject ''{0}'' has been partially delivered.", mailToSend.getSubject());
    SilverTrace
        .error("mail", "SmtpMailSendReportListener.messageDelivered()", "root.MSG_GEN_PARAM_VALUE",
            message);
    report(e);
  }

  /**
   * Performs the report of the send.
   * @param e
   */
  private void report(TransportEvent e) {

    /*
    VALID
     */
    if (e.getValidSentAddresses() != null && e.getValidSentAddresses().length > 0) {
      StringBuilder logMessage = new StringBuilder(1000).append(MessageFormat
          .format("Mail with subject ''{0}'' - delivered successfully to emails:\n",
              mailToSend.getSubject()));
      for (Address address : e.getValidSentAddresses()) {
        logMessage.append("\t").append(address).append("\n");
      }
    }

    /*
    NOT DELIVERED ON SEND
     */
    if (e.getValidUnsentAddresses() != null && e.getValidUnsentAddresses().length > 0) {
      StringBuilder logMessage = new StringBuilder(1000).append(MessageFormat
          .format("Mail with subject ''{0}'' - has not been delivered to emails:\n",
              mailToSend.getSubject()));
      for (Address address : e.getValidUnsentAddresses()) {
        logMessage.append("\t").append(address).append("\n");
      }
      SilverTrace.error("mail", "SmtpMailSendReportListener.messageDelivered()",
          "root.MSG_GEN_PARAM_VALUE", logMessage.toString());
    }

    /*
    NOT DELIVERED BECAUSE BAD EMAILS
     */
    if (e.getInvalidAddresses() != null && e.getInvalidAddresses().length > 0) {
      StringBuilder logMessage = new StringBuilder(1000).append(MessageFormat
          .format("Mail with subject ''{0}'' - has not been delivered to emails:\n",
              mailToSend.getSubject()));
      for (Address address : e.getInvalidAddresses()) {
        logMessage.append("\t").append(address).append("\n");
      }
      SilverTrace.error("mail", "SmtpMailSendReportListener.messageDelivered()",
          "root.MSG_GEN_PARAM_VALUE", logMessage.toString());
    }
  }
}
