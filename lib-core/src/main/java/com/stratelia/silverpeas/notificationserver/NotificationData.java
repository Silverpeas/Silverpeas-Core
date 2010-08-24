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
package com.stratelia.silverpeas.notificationserver;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.Map;

/**
 * Title: Notification Server Description: contains a Notification structure Copyright: Copyright
 * (c) 2000 Company: Stratelia
 * @author : eDurand
 * @version 1.0
 */
public class NotificationData implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  // private fields
  private String mLoginUser;
  private String mLoginPassword;
  private String mMessage;
  private long mNotificationId;
  private String mSenderId;
  private String mSenderName;
  private boolean mAnswerAllowed;
  private String mComment;
  private String mTargetChannel;
  private String mTargetReceipt;
  private String mTargetName;
  private Map<String, Object> mTargetParam = null;
  private String mPrioritySpeed;
  private String mReportToSenderStatus;
  private String mReportToSenderTargetChannel;
  private String mReportToSenderTargetReceipt;
  private String mReportToSenderTargetParam;
  private String mReportToLogStatus;

  /**
   * Constructor declaration
   * @see
   */
  public NotificationData() {
  }

  /**
   * getters
   * @return mNotificationId
   */
  public long getNotificationId() {
    return mNotificationId;
  }

  /**
   * getters
   * @return mLoginUser
   */
  public String getLoginUser() {
    return mLoginUser;
  }

  /**
   * getters
   * @return mLoginPassword
   */
  public String getLoginPassword() {
    return mLoginPassword;
  }

  /**
   * getters
   * @return mMessage
   */
  public String getMessage() {
    return mMessage;
  }

  /**
   * getters
   * @return mComment
   */
  public String getComment() {
    return mComment;
  }

  /**
   * getters
   * @return mSenderName
   */
  public String getSenderName() {
    return mSenderName;
  }

  /**
   * getters
   * @return mSenderId
   */
  public String getSenderId() {
    return mSenderId;
  }

  /**
   * getters
   * @return mTargetChannel
   */
  public String getTargetChannel() {
    return mTargetChannel;
  }

  /**
   * getters
   * @return mTargetReceipt
   */
  public String getTargetReceipt() {
    return mTargetReceipt;
  }

  /**
   * getters
   * @return mTargetName
   */
  public String getTargetName() {
    return mTargetName;
  }

  /**
   * getters
   * @return mTargetParam
   */
  public Map<String, Object> getTargetParam() {
    return mTargetParam;
  }

  /**
   * getters
   * @return mPrioritySpeed
   */
  public String getPrioritySpeed() {
    return mPrioritySpeed;
  }

  /**
   * getters
   * @return mReportToSenderStatus
   */
  public String getReportToSenderStatus() {
    return mReportToSenderStatus;
  }

  /**
   * getters
   * @return mReportToSenderTargetChannel
   */
  public String getReportToSenderTargetChannel() {
    return mReportToSenderTargetChannel;
  }

  /**
   * getters
   * @return mReportToSenderTargetReceipt
   */
  public String getReportToSenderTargetReceipt() {
    return mReportToSenderTargetReceipt;
  }

  /**
   * getters
   * @return mReportToSenderTargetParam
   */
  public String getReportToSenderTargetParam() {
    return mReportToSenderTargetParam;
  }

  /**
   * getters
   * @return mReportToLogStatus
   */
  public String getReportToLogStatus() {
    return mReportToLogStatus;
  }

  /**
   * getters
   * @return mAnswerAllowed
   */
  public boolean isAnswerAllowed() {
    return mAnswerAllowed;
  }

  /**
   * setters
   * @param pNotificationId
   */
  public void setNotificationId(long pNotificationId) {
    mNotificationId = pNotificationId;
  }

  /**
   * setters
   * @param pUser
   */
  public void setLoginUser(String pUser) {
    mLoginUser = pUser;
  }

  /**
   * setters
   * @param pPassword
   */
  public void setLoginPassword(String pPassword) {
    mLoginPassword = pPassword;
  }

  /**
   * setters
   * @param pMessage
   */
  public void setMessage(String pMessage) {
    mMessage = pMessage;
  }

  /**
   * setters
   * @param pComment
   */
  public void setComment(String pComment) {
    mComment = pComment;
  }

  /**
   * setters
   * @param pSenderName
   */
  public void setSenderName(String pSenderName) {
    mSenderName = pSenderName;
  }

  /**
   * setters
   * @param pSenderId
   */
  public void setSenderId(String pSenderId) {
    mSenderId = pSenderId;
  }

  /**
   * setters
   * @param pTargetChannel
   */
  public void setTargetChannel(String pTargetChannel) {
    mTargetChannel = pTargetChannel;
  }

  /**
   * setters
   * @param pTargetReceipt
   */
  public void setTargetReceipt(String pTargetReceipt) {
    mTargetReceipt = pTargetReceipt;
  }

  /**
   * setters
   * @param pTargetName
   */
  public void setTargetName(String pTargetName) {
    mTargetName = pTargetName;
  }

  /**
   * setters
   * @param pTargetParam
   */
  public void setTargetParam(Map<String, Object> pTargetParam) {
    mTargetParam = pTargetParam;
  }

  /**
   * setters
   * @param pPrioritySpeed
   */
  public void setPrioritySpeed(String pPrioritySpeed) {
    mPrioritySpeed = pPrioritySpeed;
  }

  /**
   * setters
   * @param pReportToSenderStatus
   */
  public void setReportToSenderStatus(String pReportToSenderStatus) {
    mReportToSenderStatus = pReportToSenderStatus;
  }

  /**
   * setters
   * @param pReportToSenderTargetChannel
   */
  public void setReportToSenderTargetChannel(String pReportToSenderTargetChannel) {
    mReportToSenderTargetChannel = pReportToSenderTargetChannel;
  }

  /**
   * setters
   * @param pReportToSenderTargetReceipt
   */
  public void setReportToSenderTargetReceipt(String pReportToSenderTargetReceipt) {
    mReportToSenderTargetReceipt = pReportToSenderTargetReceipt;
  }

  /**
   * setters
   * @param pReportToSenderTargetParam
   */
  public void setReportToSenderTargetParam(String pReportToSenderTargetParam) {
    mReportToSenderTargetParam = pReportToSenderTargetParam;
  }

  /**
   * setters
   * @param pReportToLogStatus
   */
  public void setReportToLogStatus(String pReportToLogStatus) {
    mReportToLogStatus = pReportToLogStatus;
  }

  /**
   * setters
   * @param answerAllowed
   */
  public void setAnswerAllowed(boolean answerAllowed) {
    mAnswerAllowed = answerAllowed;
  }

  public void traceObject() {
    if (SilverTrace.getTraceLevel("notificationServer", true) <= SilverTrace.TRACE_LEVEL_INFO) {
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "LoginUser : "
          + mLoginUser);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "Message : " + mMessage);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "NotificationId : "
          + Long.toString(mNotificationId));
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "SenderId : " + mSenderId);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "Answer allowed : "
          + mAnswerAllowed);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "SenderName : "
          + mSenderName);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "Comment : " + mComment);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "TargetChannel : "
          + mTargetChannel);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "TargetReceipt : "
          + mTargetReceipt);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "TargetName : "
          + mTargetName);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "PrioritySpeed : "
          + mPrioritySpeed);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "ReportToSenderStatus : "
          + mReportToSenderStatus);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA",
          "ReportToSenderTargetChannel : " + mReportToSenderTargetChannel);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA",
          "ReportToSenderTargetReceipt : " + mReportToSenderTargetReceipt);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA",
          "ReportToSenderTargetParam : " + mReportToSenderTargetParam);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "ReportToLogStatus : "
          + mReportToLogStatus);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationData that = (NotificationData) o;
    if (!Objects.equal(mAnswerAllowed, that.mAnswerAllowed)) {
      return false;
    }
    if (!Objects.equal(mComment, that.mComment)) {
      return false;
    }
    if (!Objects.equal(mLoginPassword, that.mLoginPassword)) {
      return false;
    }
    if (!Objects.equal(mLoginUser, that.mLoginUser)) {
      return false;
    }
    if (!Objects.equal(mMessage, that.mMessage)) {
      return false;
    }
    if (!Objects.equal(mPrioritySpeed, that.mPrioritySpeed)) {
      return false;
    }
    if (!Objects.equal(mReportToLogStatus, that.mReportToLogStatus)) {
      return false;
    }
    if (!Objects.equal(mReportToSenderStatus, that.mReportToSenderStatus)) {
      return false;
    }
    if (!Objects.equal(mReportToSenderTargetChannel, that.mReportToSenderTargetChannel)) {
      return false;
    }
    if (!Objects.equal(mReportToSenderTargetParam, that.mReportToSenderTargetParam)) {
      return false;
    }
    if (!Objects.equal(mReportToSenderTargetReceipt, that.mReportToSenderTargetReceipt)) {
      return false;
    }
    if (!Objects.equal(mSenderId, that.mSenderId)) {
      return false;
    }
    if (!Objects.equal(mSenderName, that.mSenderName)) {
      return false;
    }
    if (!Objects.equal(mTargetChannel, that.mTargetChannel)) {
      return false;
    }
    if (!Objects.equal(mTargetName, that.mTargetName)) {
      return false;
    }
    if (Maps.difference(mTargetParam, that.mTargetParam).areEqual()) {
      return false;
    }
    if (!Objects.equal(mTargetReceipt, that.mTargetReceipt)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + (this.mLoginUser != null ? this.mLoginUser.hashCode() : 0);
    hash = 83 * hash + (this.mLoginPassword != null ? this.mLoginPassword.hashCode() : 0);
    hash = 83 * hash + (this.mMessage != null ? this.mMessage.hashCode() : 0);
    hash = 83 * hash + (this.mSenderId != null ? this.mSenderId.hashCode() : 0);
    hash = 83 * hash + (this.mSenderName != null ? this.mSenderName.hashCode() : 0);
    hash = 83 * hash + (this.mAnswerAllowed ? 1 : 0);
    hash = 83 * hash + (this.mComment != null ? this.mComment.hashCode() : 0);
    hash = 83 * hash + (this.mTargetChannel != null ? this.mTargetChannel.hashCode() : 0);
    hash = 83 * hash + (this.mTargetReceipt != null ? this.mTargetReceipt.hashCode() : 0);
    hash = 83 * hash + (this.mTargetName != null ? this.mTargetName.hashCode() : 0);
    hash = 83 * hash + (this.mTargetParam != null ? this.mTargetParam.hashCode() : 0);
    hash = 83 * hash + (this.mPrioritySpeed != null ? this.mPrioritySpeed.hashCode() : 0);
    hash =
        83 * hash +
            (this.mReportToSenderStatus != null ? this.mReportToSenderStatus.hashCode() : 0);
    hash =
        83 *
            hash +
            (this.mReportToSenderTargetChannel != null ? this.mReportToSenderTargetChannel
                .hashCode() : 0);
    hash =
        83 *
            hash +
            (this.mReportToSenderTargetReceipt != null ? this.mReportToSenderTargetReceipt
                .hashCode() : 0);
    hash =
        83 *
            hash +
            (this.mReportToSenderTargetParam != null ? this.mReportToSenderTargetParam.hashCode()
                : 0);
    hash = 83 * hash + (this.mReportToLogStatus != null ? this.mReportToLogStatus.hashCode() : 0);
    return hash;
  }
}
