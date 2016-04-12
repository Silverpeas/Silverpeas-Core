/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.smtp;

import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameterNames;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.channel.AbstractListener;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.mail.MailAddress;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.MessageListener;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.silverpeas.core.notification.user.client.NotificationTemplateKey.*;
import static org.silverpeas.core.mail.MailAddress.eMail;
import static org.silverpeas.core.util.MailUtil.isForceReplyToSenderField;


@MessageDriven(activationConfig = {
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
  @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "AutoAcknowledge"),
  @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CHANNEL='SMTP'"),
  @ActivationConfigProperty(propertyName = "destination", propertyValue
      = "java:/queue/notificationsQueue")},
    description = "Message driven bean to send notifications by email")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SMTPListener extends AbstractListener implements MessageListener {

  public SMTPListener() {
  }

  /**
   * listener of NotificationServer JMS message
   *
   * @param msg the message recieved
   */
  @Override
  public void onMessage(javax.jms.Message msg) {
    try {
      processMessage(msg);
    } catch (NotificationServerException e) {
      SilverLogger.getLogger(this).error("JMS message processing error: message = {0}, Payload = {1}",
          new String[]{msg.toString(), payLoad}, e);
    }
  }

  @Override
  public void send(NotificationData notification) throws NotificationServerException {
    // process the target param string, containing the FROM and the SUBJECT email fields.
    Map<String, Object> keyValue = notification.getTargetParam();
    String tmpFromString = (String) keyValue.get(NotificationParameterNames.FROM);
    String tmpSubjectString = EncodeHelper.htmlStringToJavaString((String) keyValue.get(NotificationParameterNames.SUBJECT));
    String serverUrl = (String) keyValue.get(NotificationParameterNames.SERVERURL);
    String tmpUrlString = (String) keyValue.get(NotificationParameterNames.URL);
    String linkLabel = (String) keyValue.get(NotificationParameterNames.LINKLABEL);
    String tmpLanguageString = (String) keyValue.get(NotificationParameterNames.LANGUAGE);
    String tmpAttachmentIdString = (String) keyValue.get(NotificationParameterNames.ATTACHMENTID);
    String tmpSourceString = (String) keyValue.get(NotificationParameterNames.SOURCE);
    Boolean hideSmtpHeaderFooter = (Boolean) keyValue.get(NotificationParameterNames.HIDESMTPHEADERFOOTER);

    if (StringUtil.isDefined(tmpSourceString)) {
      tmpSubjectString = tmpSourceString + " : " + tmpSubjectString;
    }

    if (tmpLanguageString == null) {
      tmpLanguageString = I18NHelper.defaultLanguage;
    }

    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.notificationserver.channel.smtp.multilang.smtpBundle", tmpLanguageString);
    if (tmpFromString == null) {
      throw new NotificationServerException("SMTPListener.send()", SilverpeasException.ERROR,
          "smtp.EX_MISSING_FROM");
    } else {
      StringBuilder body = new StringBuilder();
      SilverpeasTemplate templateHeaderFooter =
          SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("notification");

      templateHeaderFooter.setAttribute(notification_serverurl.toString(), serverUrl);

      if (hideSmtpHeaderFooter == null) {
        // Header Message
        String SMTPmessageHeader =
            templateHeaderFooter.applyFileTemplate("SMTPmessageHeader" + '_' + tmpLanguageString);
        body.append(SMTPmessageHeader);
      }

      // Body Message
      String messageBody = notification.getMessage();
        // Transform text to html format
      messageBody = EncodeHelper.convertWhiteSpacesForHTMLDisplay(messageBody + "\n\n");
      body.append(messageBody);

      if(tmpUrlString != null) {

        templateHeaderFooter.setAttribute(notification_link.toString(), tmpUrlString);

        if(StringUtil.isDefined(linkLabel)) {
          templateHeaderFooter.setAttribute(notification_linkLabel.toString(), linkLabel);
        } else {//link name by default
          templateHeaderFooter.setAttribute(notification_linkLabel.toString(), messages.getString("GoToContribution"));
        }

      }

      // The next treatments use String replacement mechanism
      StringBuilder beforeFooterMessage = new StringBuilder();
      StringBuilder afterFooterMessage = new StringBuilder();

      if (hideSmtpHeaderFooter == null) {
        // Before Footer Message
        String smtpMessageBeforeFooter = templateHeaderFooter
            .applyFileTemplate("SMTPmessageFooter_before" + '_' + tmpLanguageString);
        beforeFooterMessage.append(smtpMessageBeforeFooter);
        // After Footer Message
        String smtpMessageAfterFooter = templateHeaderFooter
            .applyFileTemplate("SMTPmessageFooter_after" + '_' + tmpLanguageString);
        afterFooterMessage.append(smtpMessageAfterFooter);
      }


      String bodyAsString = body.toString();
      bodyAsString = bodyAsString.replace(NotificationMetaData.BEFORE_MESSAGE_FOOTER_TAG,
          beforeFooterMessage.toString().replaceAll("[\\n\\r]", ""));
      bodyAsString = bodyAsString.replace(NotificationMetaData.AFTER_MESSAGE_FOOTER_TAG,
          afterFooterMessage.toString().replaceAll("[\\n\\r]", ""));
      if (tmpAttachmentIdString == null) {
        sendEmail(tmpFromString, notification.getSenderName(), notification.getTargetReceipt(),
            tmpSubjectString, bodyAsString, true);
      } else {
        // For the moment, send the email without attachment
        sendEmail(tmpFromString, notification.getSenderName(), notification.getTargetReceipt(),
            tmpSubjectString, bodyAsString, false);
      }
    }
  }

  /**
   * send email to destination using SMTP protocol and JavaMail 1.3 API (compliant with MIME
   * format).
   *
   * @param from : from field that will appear in the email header.
   * @param fromName :
   * @see {@link InternetAddress}
   * @param to : the email target destination.
   * @param subject : the subject of the email.
   * @param content : the message or payload of the email.
   */
  private void sendEmail(String from, String fromName, String to, String subject,
      String content, boolean isHtml) throws NotificationServerException {
    MailAddress fromMailAddress = eMail(from).withName(fromName);
    MailSending mail = MailSending.from(fromMailAddress).to(eMail(to)).withSubject(subject);
    try {
      InternetAddress fromAddress = fromMailAddress.getAuthorizedInternetAddress();
      if (!Administration.get().getAdministratorEmail().equals(from) &&
          (!fromAddress.getAddress().equals(from) || isForceReplyToSenderField())) {
        mail.setReplyToRequired();
      }

      if (isHtml) {
        mail.withContent(content);
      } else {
        mail.withTextContent(content);
      }

      mail.sendSynchronously();

    } catch (MessagingException | UnsupportedEncodingException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    } catch (Exception e) {
      throw new NotificationServerException("SMTPListener.sendEmail()", SilverpeasException.ERROR,
          "smtp.EX_CANT_SEND_SMTP_MESSAGE", e);
    }
  }
}
