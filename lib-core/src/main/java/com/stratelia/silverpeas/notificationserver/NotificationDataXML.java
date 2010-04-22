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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package com.stratelia.silverpeas.notificationserver;

import java.io.CharArrayWriter;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.silverpeas.util.EncodeHelper;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Titre : Description : Copyright : Copyright (c) 2001 Société :
 * @author eDurand
 * @version 1.0
 */

public class NotificationDataXML implements DocumentHandler {
  private static final String PARAM = "PARAM";
  private static final String RECEIPT = "RECEIPT";
  private static final String ANSWERALLOWED = "ANSWERALLOWED";
  private static final String ID = "ID";
  private static final String SENDER = "SENDER";
  private static final String PASSWORD = "PASSWORD";
  private static final String USER = "USER";
  private static final String LOGIN = "LOGIN";
  private static final String COMMENT = "COMMENT";
  private static final String MESSAGE = "MESSAGE";
  private static final String TOLOG = "TOLOG";
  private static final String NAME = "NAME";
  private static final String STATUS = "STATUS";
  private static final String TOSENDER = "TOSENDER";
  private static final String REPORT = "REPORT";
  private static final String CHANNEL = "CHANNEL";
  private static final String TARGET = "TARGET";
  private static final String SPEED = "SPEED";
  private static final String PRIORITY = "PRIORITY";
  private NotificationData mData;

  /**
   * Method declaration
   * @return
   * @see
   */
  public NotificationData getNotificationData() {
    return mData;
  }

  private Parser mParser;
  private ArrayList mTagPath = new ArrayList(10);
  private int mTagDepth = -1;
  private CharArrayWriter mContents = new CharArrayWriter();
  private AttributeList mCurrentAttributeList;

  /**
	 *
	 */
  public NotificationDataXML() throws NotificationServerException {
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      // spf.setValidating (true);

      SAXParser sp = spf.newSAXParser();

      mParser = sp.getParser();

      mParser.setDocumentHandler(this);
      mParser.setErrorHandler(new NotificationDataXMLErrorHandler());
    } catch (Exception e) {
      mParser = null;
      throw new NotificationServerException("NotificationDataXML()",
          SilverpeasException.ERROR,
          "notificationServer.EX_ERROR_IN_XML_PARSING", e);
    }
  }

  /**
   * Method declaration
   * @param pFileName
   * @throws NotificationServerException
   * @see
   */
  public void ParseXML(String pFileName) throws NotificationServerException {
    try {
      mParser.parse(pFileName);
    } catch (Exception e) {
      throw new NotificationServerException("NotificationDataXML.ParseXML()",
          SilverpeasException.ERROR,
          "notificationServer.EX_ERROR_IN_XML_PARSING",
          "FileName=" + pFileName, e);
    }
  }

  /**
   * Method declaration
   * @param pInputSource
   * @throws NotificationServerException
   * @see
   */
  public void ParseXML(InputSource pInputSource)
      throws NotificationServerException {
    try {
      mParser.parse(pInputSource);
    } catch (Exception e) {
      throw new NotificationServerException("NotificationDataXML.ParseXML()",
          SilverpeasException.ERROR,
          "notificationServer.EX_ERROR_IN_XML_PARSING", "inputSource="
          + pInputSource.toString(), e);
    }
  }

  /**
   * Class declaration
   * @author
   * @version %I%, %G%
   */
  static class NotificationDataXMLErrorHandler extends HandlerBase {

    /**
     * Method declaration
     * @param e
     * @throws SAXParseException
     * @see
     */
    public void error(SAXParseException e) throws SAXParseException {
      throw e; // treat validation errors as fatal
    }

    /**
     * Method declaration
     * @param err
     * @throws SAXParseException
     * @see
     */
    public void warning(SAXParseException err) throws SAXParseException {
    }

  }

  /**
   * Method declaration
   * @param l
   * @see
   */
  public void setDocumentLocator(Locator l) {
  }

  /**
   * Method declaration
   * @throws SAXException
   * @see
   */
  public void startDocument() throws SAXException {
    mData = null;
    mData = new NotificationData();

    mTagPath.clear();
    mTagDepth = -1;

    mData.setNotificationId(-1);
    mData.setMessage("");
  }

  /**
   * Method declaration
   * @throws SAXException
   * @see
   */
  public void endDocument() throws SAXException {
    mTagPath.clear();
    mTagDepth = 0;
  }

  /**
   * Method declaration
   * @param tag
   * @param attrs
   * @throws SAXException
   * @see
   */
  public void startElement(String tag, AttributeList attrs) throws SAXException {
    int i;

    if (mTagPath.size() > 0) {
      if (mTagPath.get(mTagPath.size() - 1).equals(tag) == false) {
        mTagDepth++;
        mTagPath.add(mTagDepth, tag);
      }
    } else {
      mTagDepth++;
      mTagPath.add(mTagDepth, tag);
    }

    mContents.reset();
    mCurrentAttributeList = attrs;

    switch (mTagPath.size()) {
      case 2:
        if (PRIORITY.equals(mTagPath.get(1))) {
          if (mCurrentAttributeList != null) {
            for (i = 0; i < mCurrentAttributeList.getLength(); i++) {
              if (SPEED.equals(mCurrentAttributeList.getName(i))) {
                mData.setPrioritySpeed(EncodeHelper
                    .htmlStringToJavaString(mCurrentAttributeList.getValue(i)));
              }
            }
          }
        } else if (TARGET.equals(mTagPath.get(1))) {
          if (mCurrentAttributeList != null) {
            for (i = 0; i < mCurrentAttributeList.getLength(); i++) {
              if (CHANNEL.equals(mCurrentAttributeList.getName(i))) {
                mData.setTargetChannel(EncodeHelper
                    .htmlStringToJavaString(mCurrentAttributeList.getValue(i)));
              }
            }
          }
        }
        break;
      case 4:
        if (REPORT.equals(mTagPath.get(1))) {
          if (TOSENDER.equals(mTagPath.get(2))) {
            if (STATUS.equals(mTagPath.get(3))) {
              if (mCurrentAttributeList != null) {
                for (i = 0; i < mCurrentAttributeList.getLength(); i++) {
                  if (NAME.equals(mCurrentAttributeList.getName(i))) {
                    mData.setReportToSenderStatus((mData
                        .getReportToSenderStatus() != null ? (mData
                        .getReportToSenderStatus() + ";") : "")
                        + EncodeHelper
                        .htmlStringToJavaString(mCurrentAttributeList
                        .getValue(i)));
                  }
                }
              }
            } else if (TARGET.equals(mTagPath.get(3))) {
              if (mCurrentAttributeList != null) {
                for (i = 0; i < mCurrentAttributeList.getLength(); i++) {
                  if (CHANNEL.equals(mCurrentAttributeList.getName(i))) {
                    mData.setReportToSenderTargetChannel(EncodeHelper
                        .htmlStringToJavaString(mCurrentAttributeList
                        .getValue(i)));
                  }
                }
              }
            }
          } else if (TOLOG.equals(mTagPath.get(2))) {
            if (STATUS.equals(mTagPath.get(3))) {
              if (mCurrentAttributeList != null) {
                for (i = 0; i < mCurrentAttributeList.getLength(); i++) {
                  if (NAME.equals(mCurrentAttributeList.getName(i))) {
                    mData
                        .setReportToLogStatus((mData.getReportToLogStatus() != null ? (mData
                        .getReportToLogStatus() + ";")
                        : "")
                        + EncodeHelper
                        .htmlStringToJavaString(mCurrentAttributeList
                        .getValue(i)));
                  }
                }
              }
            }
          }
        }
        break;
    }
  }

  /**
   * Method declaration
   * @param name
   * @throws SAXException
   * @see
   */
  public void endElement(String name) throws SAXException {
    String theContents = EncodeHelper.htmlStringToJavaString(mContents
        .toString());

    switch (mTagPath.size()) {
      case 2:
        if (MESSAGE.equals(mTagPath.get(1))) {
          mData.setMessage(theContents);
        } else if (COMMENT.equals(mTagPath.get(1))) {
          mData.setComment(theContents);
        }
        break;
      case 3:
        if (LOGIN.equals(mTagPath.get(1))) {
          if (USER.equals(mTagPath.get(2))) {
            mData.setLoginUser(theContents);
          } else if (PASSWORD.equals(mTagPath.get(2))) {
            mData.setLoginPassword(theContents);
          }
        } else if (SENDER.equals(mTagPath.get(1))) {
          if (NAME.equals(mTagPath.get(2))) {
            mData.setSenderName(theContents);
          } else if (ID.equals(mTagPath.get(2))) {
            mData.setSenderId(theContents);
          } else if (ANSWERALLOWED.equals(mTagPath.get(2))) {
            mData.setAnswerAllowed(new Boolean(theContents).booleanValue());
          }
        } else if (TARGET.equals(mTagPath.get(1))) {
          if (NAME.equals(mTagPath.get(2))) {
            mData.setTargetName(theContents);
          } else if (RECEIPT.equals(mTagPath.get(2))) {
            mData.setTargetReceipt(theContents);
          } else if (PARAM.equals(mTagPath.get(2))) {
            mData.setTargetParam(NotificationServerUtil
                .unpackKeyValues(theContents));
          }
        }
        break;
      case 5:
        if (REPORT.equals(mTagPath.get(1))) {
          if (TOSENDER.equals(mTagPath.get(2))) {
            if (TARGET.equals(mTagPath.get(3))) {
              if (RECEIPT.equals(mTagPath.get(4))) {
                mData.setReportToSenderTargetReceipt(theContents);
              } else if (PARAM.equals(mTagPath.get(4))) {
                mData.setReportToSenderTargetParam(theContents);
              }
            }
          } else if (PASSWORD.equals(mTagPath.get(2))) {
            mData.setLoginPassword(theContents);
          }
        }
        break;
    }

    mTagPath.remove(mTagDepth);
    mTagDepth--;
  }

  /**
   * Method declaration
   * @param buf
   * @param offset
   * @param len
   * @throws SAXException
   * @see
   */
  public void characters(char buf[], int offset, int len) throws SAXException {
    mContents.write(buf, offset, len);
  }

  /**
   * Method declaration
   * @param buf
   * @param offset
   * @param len
   * @throws SAXException
   * @see
   */
  public void ignorableWhitespace(char buf[], int offset, int len)
      throws SAXException {
  }

  /**
   * Method declaration
   * @param target
   * @param data
   * @throws SAXException
   * @see
   */
  public void processingInstruction(String target, String data)
      throws SAXException {
  }

}