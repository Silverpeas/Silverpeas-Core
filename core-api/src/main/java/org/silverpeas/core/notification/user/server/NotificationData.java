/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.server;

import org.silverpeas.core.util.logging.SilverLogger;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Data on the notifications to the user. The data are used to store all the information required to
 * send a message from a sender to its recipient through a given channel. Such an instance is sent
 * from the notification client to the server responsible to transmit it to the recipients.
 */
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
  private transient Map<String, Object> mTargetParam = null;
  private String mPrioritySpeed;
  private String mReportToSenderStatus;
  private String mReportToSenderTargetChannel;
  private String mReportToSenderTargetReceipt;
  private String mReportToSenderTargetParam;
  private String mReportToLogStatus;

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

  public void setNotificationId(long pNotificationId) {
    mNotificationId = pNotificationId;
  }

  public void setLoginUser(String pUser) {
    mLoginUser = pUser;
  }

  public void setLoginPassword(String pPassword) {
    mLoginPassword = pPassword;
  }

  public void setMessage(String pMessage) {
    mMessage = pMessage;
  }

  public void setComment(String pComment) {
    mComment = pComment;
  }

  public void setSenderName(String pSenderName) {
    mSenderName = pSenderName;
  }

  public void setSenderId(String pSenderId) {
    mSenderId = pSenderId;
  }

  public void setTargetChannel(String pTargetChannel) {
    mTargetChannel = pTargetChannel;
  }

  public void setTargetReceipt(String pTargetReceipt) {
    mTargetReceipt = pTargetReceipt;
  }

  public void setTargetName(String pTargetName) {
    mTargetName = pTargetName;
  }

  public void setTargetParam(Map<String, Object> pTargetParam) {
    mTargetParam = pTargetParam;
  }

  public void setPrioritySpeed(String pPrioritySpeed) {
    mPrioritySpeed = pPrioritySpeed;
  }

  public void setReportToSenderStatus(String pReportToSenderStatus) {
    mReportToSenderStatus = pReportToSenderStatus;
  }

  public void setReportToSenderTargetChannel(String pReportToSenderTargetChannel) {
    mReportToSenderTargetChannel = pReportToSenderTargetChannel;
  }

  public void setReportToSenderTargetReceipt(String pReportToSenderTargetReceipt) {
    mReportToSenderTargetReceipt = pReportToSenderTargetReceipt;
  }

  public void setReportToSenderTargetParam(String pReportToSenderTargetParam) {
    mReportToSenderTargetParam = pReportToSenderTargetParam;
  }

  public void setReportToLogStatus(String pReportToLogStatus) {
    mReportToLogStatus = pReportToLogStatus;
  }

  public void setAnswerAllowed(boolean answerAllowed) {
    mAnswerAllowed = answerAllowed;
  }

  public void traceObject() {
    SilverLogger.getLogger(this).debug("Notification Data Dump: {" +
            "NotificationId: {0}, ReportToSenderStatus: {1}, ReportToSenderTargetChannel: {2}," +
            "ReportToSenderTargetReceipt: {3}, ReportToSenderTargetParam: {4}," +
            "ReportToLogStatus: {5}", mNotificationId, mReportToSenderStatus,
        mReportToSenderTargetChannel, mReportToSenderTargetReceipt, mReportToSenderTargetParam,
        mReportToLogStatus);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NotificationData that = (NotificationData) o;
    return mNotificationId == that.mNotificationId && mAnswerAllowed == that.mAnswerAllowed &&
        Objects.equals(mLoginUser, that.mLoginUser) &&
        Objects.equals(mLoginPassword, that.mLoginPassword) &&
        Objects.equals(mMessage, that.mMessage) && Objects.equals(mSenderId, that.mSenderId) &&
        Objects.equals(mSenderName, that.mSenderName) && Objects.equals(mComment, that.mComment) &&
        Objects.equals(mTargetChannel, that.mTargetChannel) &&
        Objects.equals(mTargetReceipt, that.mTargetReceipt) &&
        Objects.equals(mTargetName, that.mTargetName) &&
        Objects.equals(mTargetParam, that.mTargetParam) &&
        Objects.equals(mPrioritySpeed, that.mPrioritySpeed) &&
        Objects.equals(mReportToSenderStatus, that.mReportToSenderStatus) &&
        Objects.equals(mReportToSenderTargetChannel, that.mReportToSenderTargetChannel) &&
        Objects.equals(mReportToSenderTargetReceipt, that.mReportToSenderTargetReceipt) &&
        Objects.equals(mReportToSenderTargetParam, that.mReportToSenderTargetParam) &&
        Objects.equals(mReportToLogStatus, that.mReportToLogStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mLoginUser, mLoginPassword, mMessage, mNotificationId, mSenderId,
        mSenderName, mAnswerAllowed, mComment, mTargetChannel, mTargetReceipt, mTargetName,
        mTargetParam, mPrioritySpeed, mReportToSenderStatus, mReportToSenderTargetChannel,
        mReportToSenderTargetReceipt, mReportToSenderTargetParam, mReportToLogStatus);
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
