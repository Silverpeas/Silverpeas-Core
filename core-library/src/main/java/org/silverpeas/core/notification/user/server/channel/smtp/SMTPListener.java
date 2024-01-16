/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.server.channel.smtp;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.mail.MailAddress;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.notification.user.AttachmentLink;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameterNames;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.notification.user.server.channel.AbstractListener;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.MessageListener;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.mail.MailAddress.eMail;
import static org.silverpeas.core.notification.user.client.NotificationTemplateKey.*;
import static org.silverpeas.core.util.MailUtil.isForceReplyToSenderField;


@MessageDriven(activationConfig = {
  @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
  @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
  @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "CHANNEL='SMTP'"),
  @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue
      = "jms/queue/notificationsQueue")},
    description = "Message driven bean to send notifications by email")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SMTPListener extends AbstractListener implements MessageListener {

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
    String tmpSubjectString = WebEncodeHelper.htmlStringToJavaString((String) keyValue.get(NotificationParameterNames.SUBJECT));
    String serverUrl = (String) keyValue.get(NotificationParameterNames.SERVERURL);
    String baseServerUrl = (String) keyValue.get(NotificationParameterNames.SERVER_BASEURL);
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
      throw new NotificationServerException("Missing sender email address!");
    }

    StringBuilder body = new StringBuilder();
    SilverpeasTemplate templateHeaderFooter =
        SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("notification");

    templateHeaderFooter.setAttribute(NOTIFICATION_BASE_SERVER_URL.toString(), baseServerUrl);
    templateHeaderFooter.setAttribute(NOTIFICATION_SERVER_URL.toString(), serverUrl);
    templateHeaderFooter.setAttribute(NOTIFICATION_SENDER_NAME.toString(),
        notification.getSenderName());
    templateHeaderFooter.setAttribute(NOTIFICATION_SENDER_EMAIL.toString(), tmpFromString);

    if (hideSmtpHeaderFooter == null) {
      // Header Message
      String smtpMessageheader =
          templateHeaderFooter.applyFileTemplate("SMTPmessageHeader" + '_' + tmpLanguageString);
      body.append(smtpMessageheader);
    }

    // Body Message
    String messageBody = notification.getMessage();
    // Transform text to html format
    messageBody = WebEncodeHelper.convertWhiteSpacesForHTMLDisplay(messageBody + "\n\n");
    body.append(messageBody);

    if (tmpUrlString != null) {

      templateHeaderFooter.setAttribute(NOTIFICATION_LINK.toString(), tmpUrlString);

      if (StringUtil.isDefined(linkLabel)) {
        templateHeaderFooter.setAttribute(NOTIFICATION_LINK_LABEL.toString(), linkLabel);
      } else {//link name by default
        templateHeaderFooter.setAttribute(NOTIFICATION_LINK_LABEL.toString(),
            messages.getString("GoToContribution"));
      }

    }

    final List<AttachmentLink> attachments = getAttachmentLinks(keyValue, tmpLanguageString);
    if (!attachments.isEmpty()) {
      templateHeaderFooter.setAttribute(NOTIFICATION_ATTACHMENTS.toString(), attachments);
    }

    // The next treatments use String replacement mechanism
    StringBuilder beforeFooterMessage = new StringBuilder();
    StringBuilder afterFooterMessage = new StringBuilder();

    if (hideSmtpHeaderFooter == null) {
      applyFooterTemplate(templateHeaderFooter, tmpLanguageString, beforeFooterMessage,
          afterFooterMessage);
    }

    String bodyAsString = body.toString();
    bodyAsString = bodyAsString.replace(NotificationMetaData.BEFORE_MESSAGE_FOOTER_TAG,
        beforeFooterMessage.toString().replaceAll("[\\n\\r]", ""));
    bodyAsString = bodyAsString.replace(NotificationMetaData.AFTER_MESSAGE_FOOTER_TAG,
        afterFooterMessage.toString().replaceAll("[\\n\\r]", ""));
    boolean isHtml = tmpAttachmentIdString == null;
    sendEmail(tmpFromString, notification.getSenderName(), notification.getTargetReceipt(),
        tmpSubjectString, bodyAsString, isHtml);
  }

  private void applyFooterTemplate(final SilverpeasTemplate templateHeaderFooter,
      final String tmpLanguageString, final StringBuilder beforeFooterMessage,
      final StringBuilder afterFooterMessage) {
    // Before Footer Message
    String smtpMessageBeforeFooter = templateHeaderFooter.applyFileTemplate(
        "SMTPmessageFooter_before" + '_' + tmpLanguageString);
    beforeFooterMessage.append(smtpMessageBeforeFooter);
    // After Footer Message
    String smtpMessageAfterFooter =
        templateHeaderFooter.applyFileTemplate("SMTPmessageFooter_after" + '_' + tmpLanguageString);
    afterFooterMessage.append(smtpMessageAfterFooter);
  }

  private List<AttachmentLink> getAttachmentLinks(final Map<String, Object> keyValues,
      final String language) {
    final String componentId = (String) keyValues.get(NotificationParameterNames.COMPONENTID);
    final String contributionId =
        (String) keyValues.get(NotificationParameterNames.ATTACHMENT_TARGETID);
    final List<AttachmentLink> links;
    if (StringUtil.isDefined(componentId) && StringUtil.isDefined(contributionId)) {
      final ResourceReference ref = new ResourceReference(contributionId, componentId);
      links = AttachmentLink.getForContribution(ref, language);
    } else {
      links = Collections.emptyList();
    }
    return links;
  }

  /**
   * send email to destination using SMTP protocol and JavaMail 1.3 API (compliant with MIME
   * format).
   * @param from : from field that will appear in the email header.
   * @param fromName :
   * @param to : the email target destination.
   * @param subject : the subject of the email.
   * @param content : the message or payload of the email.
   * @see InternetAddress
   */
  private void sendEmail(String from, String fromName, String to, String subject,
      String content, boolean isHtml) throws NotificationServerException {
    final boolean isSilverpeasEmail = Administration.get().getSilverpeasEmail().equals(from);
    final MailAddress fromMailAddress = eMail(from).withName(ofNullable(fromName)
        .filter(StringUtil::isDefined)
        .orElseGet(() -> isSilverpeasEmail ? Administration.get().getSilverpeasName() : StringUtil.EMPTY));
    final MailSending mail = MailSending.from(fromMailAddress).to(eMail(to)).withSubject(subject);
    try {
      final InternetAddress fromAddress = fromMailAddress.getAuthorizedInternetAddress();
      if (!isSilverpeasEmail &&
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
      throw new NotificationServerException(e);
    }
  }
}
