/**
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.notificationserver;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.MapUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class NotificationData implements Serializable {

  private static final long serialVersionUID = -3772511721152323046L;
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
   */
  public NotificationData() {
  }

  /**
   * @return mNotificationId
   */
  public long getNotificationId() {
    return mNotificationId;
  }

  /**
   * @return mLoginUser
   */
  public String getLoginUser() {
    return mLoginUser;
  }

  /**
   * @return mLoginPassword
   */
  public String getLoginPassword() {
    return mLoginPassword;
  }

  /**
   * @return mMessage
   */
  public String getMessage() {
    return mMessage;
  }

  /**
   * @return mComment
   */
  public String getComment() {
    return mComment;
  }

  /**
   * @return mSenderName
   */
  public String getSenderName() {
    return mSenderName;
  }

  /**
   * @return mSenderId
   */
  public String getSenderId() {
    return mSenderId;
  }

  /**
   * @return mTargetChannel
   */
  public String getTargetChannel() {
    return mTargetChannel;
  }

  /**
   * @return mTargetReceipt
   */
  public String getTargetReceipt() {
    return mTargetReceipt;
  }

  /**
   * @return mTargetName
   */
  public String getTargetName() {
    return mTargetName;
  }

  /**
   * @return mTargetParam
   */
  public Map<String, Object> getTargetParam() {
    return mTargetParam;
  }

  /**
   * @return mPrioritySpeed
   */
  public String getPrioritySpeed() {
    return mPrioritySpeed;
  }

  /**
   * @return mReportToSenderStatus
   */
  public String getReportToSenderStatus() {
    return mReportToSenderStatus;
  }

  /**
   * @return mReportToSenderTargetChannel
   */
  public String getReportToSenderTargetChannel() {
    return mReportToSenderTargetChannel;
  }

  /**
   * @return mReportToSenderTargetReceipt
   */
  public String getReportToSenderTargetReceipt() {
    return mReportToSenderTargetReceipt;
  }

  /**
   * @return mReportToSenderTargetParam
   */
  public String getReportToSenderTargetParam() {
    return mReportToSenderTargetParam;
  }

  /**
   * @return mReportToLogStatus
   */
  public String getReportToLogStatus() {
    return mReportToLogStatus;
  }

  /**
   * @return mAnswerAllowed
   */
  public boolean isAnswerAllowed() {
    return mAnswerAllowed;
  }

  /**
   * @param pNotificationId
   */
  public void setNotificationId(long pNotificationId) {
    mNotificationId = pNotificationId;
  }

  /**
   * @param pUser
   */
  public void setLoginUser(String pUser) {
    mLoginUser = pUser;
  }

  /**
   * @param pPassword
   */
  public void setLoginPassword(String pPassword) {
    mLoginPassword = pPassword;
  }

  /**
   * @param pMessage
   */
  public void setMessage(String pMessage) {
    mMessage = pMessage;
  }

  /**
   * @param pComment
   */
  public void setComment(String pComment) {
    mComment = pComment;
  }

  /**
   * @param pSenderName
   */
  public void setSenderName(String pSenderName) {
    mSenderName = pSenderName;
  }

  /**
   * @param pSenderId
   */
  public void setSenderId(String pSenderId) {
    mSenderId = pSenderId;
  }

  /**
   * @param pTargetChannel
   */
  public void setTargetChannel(String pTargetChannel) {
    mTargetChannel = pTargetChannel;
  }

  /**
   * @param pTargetReceipt
   */
  public void setTargetReceipt(String pTargetReceipt) {
    mTargetReceipt = pTargetReceipt;
  }

  /**
   * @param pTargetName
   */
  public void setTargetName(String pTargetName) {
    mTargetName = pTargetName;
  }

  /**
   * @param pTargetParam
   */
  public void setTargetParam(Map<String, Object> pTargetParam) {
    mTargetParam = pTargetParam;
  }

  /**
   * @param pPrioritySpeed
   */
  public void setPrioritySpeed(String pPrioritySpeed) {
    mPrioritySpeed = pPrioritySpeed;
  }

  /**
   * @param pReportToSenderStatus
   */
  public void setReportToSenderStatus(String pReportToSenderStatus) {
    mReportToSenderStatus = pReportToSenderStatus;
  }

  /**
   * @param pReportToSenderTargetChannel
   */
  public void setReportToSenderTargetChannel(String pReportToSenderTargetChannel) {
    mReportToSenderTargetChannel = pReportToSenderTargetChannel;
  }

  /**
   * @param pReportToSenderTargetReceipt
   */
  public void setReportToSenderTargetReceipt(String pReportToSenderTargetReceipt) {
    mReportToSenderTargetReceipt = pReportToSenderTargetReceipt;
  }

  /**
   * @param pReportToSenderTargetParam
   */
  public void setReportToSenderTargetParam(String pReportToSenderTargetParam) {
    mReportToSenderTargetParam = pReportToSenderTargetParam;
  }

  /**
   * @param pReportToLogStatus
   */
  public void setReportToLogStatus(String pReportToLogStatus) {
    mReportToLogStatus = pReportToLogStatus;
  }

  /**
   * @param answerAllowed
   */
  public void setAnswerAllowed(boolean answerAllowed) {
    mAnswerAllowed = answerAllowed;
  }

  public void traceObject() {
    if (SilverTrace.getTraceLevel("notificationServer", true) <= SilverTrace.TRACE_LEVEL_INFO) {
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "LoginUser : " + mLoginUser);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "Message : " + mMessage);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA",
          "NotificationId : " + Long.toString(mNotificationId));
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "SenderId : " + mSenderId);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "Answer allowed : " + mAnswerAllowed);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "SenderName : " + mSenderName);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "Comment : " + mComment);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "TargetChannel : " + mTargetChannel);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "TargetReceipt : " + mTargetReceipt);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "TargetName : " + mTargetName);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA", "PrioritySpeed : " + mPrioritySpeed);
      SilverTrace.info("notificationServer", "NotificationData.traceObject",
          "notificationServer.INFO_DUMP_NOTIF_DATA",
          "ReportToSenderStatus : " + mReportToSenderStatus);
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
          "notificationServer.INFO_DUMP_NOTIF_DATA", "ReportToLogStatus : " + mReportToLogStatus);
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
    if (!mAnswerAllowed == that.mAnswerAllowed) {
      return false;
    }
    if (!Objects.equals(mComment, that.mComment)) {
      return false;
    }
    if (!Objects.equals(mLoginPassword, that.mLoginPassword)) {
      return false;
    }
    if (!Objects.equals(mLoginUser, that.mLoginUser)) {
      return false;
    }
    if (!Objects.equals(mMessage, that.mMessage)) {
      return false;
    }
    if (!Objects.equals(mPrioritySpeed, that.mPrioritySpeed)) {
      return false;
    }
    if (!Objects.equals(mReportToLogStatus, that.mReportToLogStatus)) {
      return false;
    }
    if (!Objects.equals(mReportToSenderStatus, that.mReportToSenderStatus)) {
      return false;
    }
    if (!Objects.equals(mReportToSenderTargetChannel, that.mReportToSenderTargetChannel)) {
      return false;
    }
    if (!Objects.equals(mReportToSenderTargetParam, that.mReportToSenderTargetParam)) {
      return false;
    }
    if (!Objects.equals(mReportToSenderTargetReceipt, that.mReportToSenderTargetReceipt)) {
      return false;
    }
    if (!Objects.equals(mSenderId, that.mSenderId)) {
      return false;
    }
    if (!Objects.equals(mSenderName, that.mSenderName)) {
      return false;
    }
    if (!Objects.equals(mTargetChannel, that.mTargetChannel)) {
      return false;
    }
    if (!Objects.equals(mTargetName, that.mTargetName)) {
      return false;
    }
    if (!MapUtil.equals(mTargetParam, that.mTargetParam)) {
      return false;
    }
    if (!Objects.equals(mTargetReceipt, that.mTargetReceipt)) {
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
    hash = 83 * hash +
        (this.mReportToSenderStatus != null ? this.mReportToSenderStatus.hashCode() : 0);
    hash = 83 * hash +
        (this.mReportToSenderTargetChannel != null ? this.mReportToSenderTargetChannel.hashCode() :
            0);
    hash = 83 * hash +
        (this.mReportToSenderTargetReceipt != null ? this.mReportToSenderTargetReceipt.hashCode() :
            0);
    hash = 83 * hash +
        (this.mReportToSenderTargetParam != null ? this.mReportToSenderTargetParam.hashCode() : 0);
    hash = 83 * hash + (this.mReportToLogStatus != null ? this.mReportToLogStatus.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "NotificationData{" + "mLoginUser=" + mLoginUser + ", mLoginPassword=" + mLoginPassword +
        ", mMessage=" + mMessage + ", mNotificationId=" + mNotificationId + ", mSenderId=" +
        mSenderId + ", mSenderName=" + mSenderName + ", mAnswerAllowed=" + mAnswerAllowed +
        ", mComment=" + mComment + ", mTargetChannel=" + mTargetChannel + ", mTargetReceipt=" +
        mTargetReceipt + ", mTargetName=" + mTargetName + ", mTargetParam=" + mTargetParam +
        ", mPrioritySpeed=" + mPrioritySpeed + ", mReportToSenderStatus=" + mReportToSenderStatus +
        ", mReportToSenderTargetChannel=" + mReportToSenderTargetChannel +
        ", mReportToSenderTargetReceipt=" + mReportToSenderTargetReceipt +
        ", mReportToSenderTargetParam=" + mReportToSenderTargetParam + ", mReportToLogStatus=" +
        mReportToLogStatus + '}';
  }
}
