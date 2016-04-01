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

import org.silverpeas.core.util.StringUtil;

/**
 * @author Yohann Chastagnier
 */
public class MailToSend {

  private MailAddress from;
  private ReceiverMailAddressSet to;
  private String subject = "";
  private MailContent content = MailContent.EMPTY;
  private boolean replyToRequired = false;
  private boolean asynchronous = true;

  /**
   * Hidden contructor.
   */
  MailToSend() {
  }

  public MailAddress getFrom() {
    return from;
  }

  public void setFrom(final MailAddress from) {
    this.from = from;
  }

  public ReceiverMailAddressSet getTo() {
    return to;
  }

  public void setTo(final ReceiverMailAddressSet to) {
    this.to = to;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(final String subject) {
    this.subject = StringUtil.defaultStringIfNotDefined(subject);
  }

  public MailContent getContent() {
    return content;
  }

  public void setContent(final MailContent content) {
    this.content = content != null ? content : MailContent.EMPTY;
  }

  public boolean isReplyToRequired() {
    return replyToRequired;
  }

  public void setReplyToRequired() {
    this.replyToRequired = true;
  }

  public boolean isAsynchronous() {
    return asynchronous;
  }

  public void sendSynchronously() {
    this.asynchronous = false;
  }
}
