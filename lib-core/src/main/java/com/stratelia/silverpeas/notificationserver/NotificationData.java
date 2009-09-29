/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver;

import java.io.Serializable;
import java.util.*;

import com.stratelia.silverpeas.silvertrace.*;

/**
 * Title: Notification Server Description: contains a Notification structure
 * Copyright: Copyright (c) 2000 Company: Stratelia
 * 
 * @author : eDurand
 * @version 1.0
 */

public class NotificationData implements Serializable {
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
  private Hashtable mTargetParam = null;
  private String mPrioritySpeed;
  private String mReportToSenderStatus;
  private String mReportToSenderTargetChannel;
  private String mReportToSenderTargetReceipt;
  private String mReportToSenderTargetParam;
  private String mReportToLogStatus;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public NotificationData() {
  }

  /**
   * getters
   * 
   * @return mNotificationId
   */
  public long getNotificationId() {
    return mNotificationId;
  }

  /**
   * getters
   * 
   * @return mLoginUser
   */
  public String getLoginUser() {
    return mLoginUser;
  }

  /**
   * getters
   * 
   * @return mLoginPassword
   */
  public String getLoginPassword() {
    return mLoginPassword;
  }

  /**
   * getters
   * 
   * @return mMessage
   */
  public String getMessage() {
    return mMessage;
  }

  /**
   * getters
   * 
   * @return mComment
   */
  public String getComment() {
    return mComment;
  }

  /**
   * getters
   * 
   * @return mSenderName
   */
  public String getSenderName() {
    return mSenderName;
  }

  /**
   * getters
   * 
   * @return mSenderId
   */
  public String getSenderId() {
    return mSenderId;
  }

  /**
   * getters
   * 
   * @return mTargetChannel
   */
  public String getTargetChannel() {
    return mTargetChannel;
  }

  /**
   * getters
   * 
   * @return mTargetReceipt
   */
  public String getTargetReceipt() {
    return mTargetReceipt;
  }

  /**
   * getters
   * 
   * @return mTargetName
   */
  public String getTargetName() {
    return mTargetName;
  }

  /**
   * getters
   * 
   * @return mTargetParam
   */
  public Hashtable getTargetParam() {
    return mTargetParam;
  }

  /**
   * getters
   * 
   * @return mPrioritySpeed
   */
  public String getPrioritySpeed() {
    return mPrioritySpeed;
  }

  /**
   * getters
   * 
   * @return mReportToSenderStatus
   */
  public String getReportToSenderStatus() {
    return mReportToSenderStatus;
  }

  /**
   * getters
   * 
   * @return mReportToSenderTargetChannel
   */
  public String getReportToSenderTargetChannel() {
    return mReportToSenderTargetChannel;
  }

  /**
   * getters
   * 
   * @return mReportToSenderTargetReceipt
   */
  public String getReportToSenderTargetReceipt() {
    return mReportToSenderTargetReceipt;
  }

  /**
   * getters
   * 
   * @return mReportToSenderTargetParam
   */
  public String getReportToSenderTargetParam() {
    return mReportToSenderTargetParam;
  }

  /**
   * getters
   * 
   * @return mReportToLogStatus
   */
  public String getReportToLogStatus() {
    return mReportToLogStatus;
  }

  /**
   * getters
   * 
   * @return mAnswerAllowed
   */
  public boolean isAnswerAllowed() {
    return mAnswerAllowed;
  }

  /**
   * setters
   * 
   * @param pNotificationId
   */
  public void setNotificationId(long pNotificationId) {
    mNotificationId = pNotificationId;
  }

  /**
   * setters
   * 
   * @param pUser
   */
  public void setLoginUser(String pUser) {
    mLoginUser = pUser;
  }

  /**
   * setters
   * 
   * @param pPassword
   */
  public void setLoginPassword(String pPassword) {
    mLoginPassword = pPassword;
  }

  /**
   * setters
   * 
   * @param pMessage
   */
  public void setMessage(String pMessage) {
    mMessage = pMessage;
  }

  /**
   * setters
   * 
   * @param pComment
   */
  public void setComment(String pComment) {
    mComment = pComment;
  }

  /**
   * setters
   * 
   * @param pSenderName
   */
  public void setSenderName(String pSenderName) {
    mSenderName = pSenderName;
  }

  /**
   * setters
   * 
   * @param pSenderId
   */
  public void setSenderId(String pSenderId) {
    mSenderId = pSenderId;
  }

  /**
   * setters
   * 
   * @param pTargetChannel
   */
  public void setTargetChannel(String pTargetChannel) {
    mTargetChannel = pTargetChannel;
  }

  /**
   * setters
   * 
   * @param pTargetChannel
   */
  public void setTargetReceipt(String pTargetReceipt) {
    mTargetReceipt = pTargetReceipt;
  }

  /**
   * setters
   * 
   * @param pTargetName
   */
  public void setTargetName(String pTargetName) {
    mTargetName = pTargetName;
  }

  /**
   * setters
   * 
   * @param pTargetParam
   */
  public void setTargetParam(Hashtable pTargetParam) {
    mTargetParam = pTargetParam;
  }

  /**
   * setters
   * 
   * @param pPrioritySpeed
   */
  public void setPrioritySpeed(String pPrioritySpeed) {
    mPrioritySpeed = pPrioritySpeed;
  }

  /**
   * setters
   * 
   * @param pReportToSenderStatus
   */
  public void setReportToSenderStatus(String pReportToSenderStatus) {
    mReportToSenderStatus = pReportToSenderStatus;
  }

  /**
   * setters
   * 
   * @param pReportToSenderTargetChannel
   */
  public void setReportToSenderTargetChannel(String pReportToSenderTargetChannel) {
    mReportToSenderTargetChannel = pReportToSenderTargetChannel;
  }

  /**
   * setters
   * 
   * @param pReportToSenderTargetReceipt
   */
  public void setReportToSenderTargetReceipt(String pReportToSenderTargetReceipt) {
    mReportToSenderTargetReceipt = pReportToSenderTargetReceipt;
  }

  /**
   * setters
   * 
   * @param pReportToSenderTargetParam
   */
  public void setReportToSenderTargetParam(String pReportToSenderTargetParam) {
    mReportToSenderTargetParam = pReportToSenderTargetParam;
  }

  /**
   * setters
   * 
   * @param pReportToLogStatus
   */
  public void setReportToLogStatus(String pReportToLogStatus) {
    mReportToLogStatus = pReportToLogStatus;
  }

  /**
   * setters
   * 
   * @param mAnswerAllowed
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

}
