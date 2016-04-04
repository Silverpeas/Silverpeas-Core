/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.notification.user.server.channel.popup;

/**
 * @author dblot
 */
public class SilverMessage {

  private String mWhat = null;
  private String mContent = null;
  private String mID = null;
  private String mSenderId = null;
  private String mSenderName = null;
  private boolean mAnswerAllowed = false;

  /**
   * --------------------------------------------------------------------------
   * constructor
   */
  public SilverMessage(String what, String content) {
    this.mWhat = what;
    this.mContent = content;
  }

  /**
   * --------------------------------------------------------------------------
   * constructor
   */
  public SilverMessage(String what) {
    this.mWhat = what;
    this.mContent = "";
  }

  /**
   * getWhat
   * @return what
   */
  public String getWhat() {
    return mWhat;
  };

  /**
   * -------------------------------------------------------------------------- getContent return
   * content
   */
  public String getContent() {
    return mContent;
  };

  /**
   * -------------------------------------------------------------------------- setContent set the
   * content
   */
  public void setContent(String content) {
    mContent = content;
  };

  /**
   * -------------------------------------------------------------------------- setContent set the
   * ID
   */
  public void setID(String ID) {
    mID = ID;
  };

  /**
   * -------------------------------------------------------------------------- setContent get the
   * ID
   */
  public String getID() {
    return mID;
  }

  public String getSenderId() {
    return mSenderId;
  }

  public void setSenderId(String senderId) {
    mSenderId = senderId;
  }

  public String getSenderName() {
    return mSenderName;
  }

  public void setSenderName(String senderName) {
    mSenderName = senderName;
  }

  public boolean isAnswerAllowed() {
    return mAnswerAllowed;
  }

  public void setAnswerAllowed(boolean answerAllowed) {
    mAnswerAllowed = answerAllowed;
  }

  public void setDate(String date) {
    // m_Date = date;
  }

  public void setTime(String time) {
    // m_Time = time;
  }
}
