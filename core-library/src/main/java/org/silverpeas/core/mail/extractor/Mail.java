/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.mail.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class Mail {

  public static final String EML_MAIL_EXTENSION = "eml";
  public static final String MSG_MAIL_EXTENSION = "msg";
  public static final String[] MAIL_EXTENTIONS = new String[]{EML_MAIL_EXTENSION,
    MSG_MAIL_EXTENSION};
  private String subject;
  private InternetAddress from;
  private Address[] to;
  private Address[] cc;
  private Date date;
  private String body;

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setDate(Calendar date) {
    if (date != null) {
      this.date = date.getTime();
    }
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public InternetAddress getFrom() {
    return from;
  }

  public void setFrom(InternetAddress from) {
    this.from = from;
  }

  public Address[] getTo() {
    return to;
  }

  public void setTo(Address[] to) {
    this.to = (to != null ? to.clone() : null);

  }

  public Address[] getCc() {
    return cc;
  }

  public void setCc(Address[] cc) {
    this.cc = (cc != null ? cc.clone() : null);
  }

  public Address[] getAllRecipients() {
    List<Address> recipients = new ArrayList<Address>();
    if (getTo() != null && getTo().length > 0) {
      recipients.addAll(Arrays.asList(getTo()));
    }
    if (getCc() != null && getCc().length > 0) {
      recipients.addAll(Arrays.asList(getCc()));
    }
    return recipients.toArray(new Address[recipients.size()]);
  }
}
