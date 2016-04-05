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
package org.silverpeas.core.mail;

import org.silverpeas.core.mail.engine.MailSenderTask;
import org.silverpeas.core.util.MailUtil;

import javax.mail.Multipart;

/**
 * Handles easily the send of an email.
 * It deals with centralized mechanism that ensures a synchronized use of mail services (SMTP
 * server connexion for example).
 * @author Yohann Chastagnier
 */
public class MailSending {

  private MailToSend mailToSend;

  /**
   * Gets a new instance of {@link MailSending} by specifying the email of the sender.
   * @param senderEmail the email of the sender. This email can be changed just before the send
   * in case it is a not authorized one
   * (see {@link MailUtil#getAuthorizedEmailAddress(String, String)}).
   * @return the new instance of {@link MailSending}.
   */
  public static MailSending from(MailAddress senderEmail) {
    MailSending mailSending = new MailSending();
    mailSending.mailToSend = new MailToSend();
    mailSending.mailToSend.setFrom(senderEmail);
    return mailSending;
  }

  /**
   * Hidden constructor.
   */
  private MailSending() {
  }

  /**
   * Permits to specify the receiver of the mail.
   * @param receiverMailAddress the email of the receiver.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending to(MailAddress receiverMailAddress) {
    mailToSend.setTo(ReceiverMailAddressSet.with(receiverMailAddress));
    return this;
  }

  /**
   * Permits to specify the receiver of the mail.
   * @param receiverMailAddressSet the emails of the receivers.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending to(ReceiverMailAddressSet receiverMailAddressSet) {
    mailToSend.setTo(receiverMailAddressSet);
    return this;
  }

  /**
   * Permits to specify the subject of the mail.
   * @param subject the subject.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending withSubject(String subject) {
    mailToSend.setSubject(subject);
    return this;
  }

  /**
   * Permits to specify the HTML content as String of the mail.
   * @param content the content as String.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending withContent(String content) {
    mailToSend.setContent(MailContent.of(content));
    return this;
  }

  /**
   * Permits to specify the text content as String of the mail.
   * If the string content contains {@code <html>} TAG, then it will be considered as an HTML
   * one, in all cases.
   * @param content the content as String.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending withTextContent(String content) {
    mailToSend.setContent(MailContent.of(content).notHtml());
    return this;
  }

  /**
   * Permits to specify the content as {@link Multipart} of the mail.
   * @param content the content as {@link Multipart}.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending withContent(Multipart content) {
    mailToSend.setContent(MailContent.of(content));
    return this;
  }

  /**
   * Permits to specify the content as {@link MailContent} of the mail.
   * @param content the content as {@link MailContent}.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending withContent(MailContent content) {
    mailToSend.setContent(content);
    return this;
  }

  /**
   * Indicates that the reply to field of the mail must be set.
   * @return the completed instance of {@link MailSending}.
   */
  public MailSending setReplyToRequired() {
    mailToSend.setReplyToRequired();
    return this;
  }

  /**
   * Gets the mail to send.
   * @return the mail to send.
   */
  MailToSend getMailToSend() {
    return mailToSend;
  }

  /**
   * Performs the send of the mail.
   * This will be executed into a Threaded mechanism.
   */
  public void send() {
    MailSenderTask.addMailToSend(mailToSend);
  }

  /**
   * Performs the send of the mail synchronously.
   * So the caller wait for the end of the sending treatment before to continue its processing.
   */
  public void sendSynchronously() {
    mailToSend.sendSynchronously();
    send();
  }
}
