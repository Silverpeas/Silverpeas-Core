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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.notification.user.server;

import java.util.Date;
import java.util.HashMap;
import com.silverpeas.jcrutil.RandomGenerator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class NotificationServerUtilTest {

  public NotificationServerUtilTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of convertNotificationDataToXML method, of class NotificationServerUtil.
   * @throws Exception
   */
  @Test
  public void testConvertNotificationDataToXML() throws Exception {
    NotificationData p_Data = new NotificationData();
    Map<String, Object> params = new HashMap<String, Object>(2);
    Date dateParam = RandomGenerator.getRandomCalendar().getTime();
    params.put("date", dateParam);
    params.put("string", "bonjour le monde; 0 + 0 = la tête à toto");
    p_Data.setAnswerAllowed(true);
    p_Data.setComment("comment");
    p_Data.setLoginPassword("password");
    p_Data.setLoginUser("user");
    p_Data.setMessage("message");
    p_Data.setNotificationId(RandomGenerator.getRandomLong());
    p_Data.setPrioritySpeed("fast");
    p_Data.setReportToLogStatus("logStatus");
    p_Data.setReportToSenderStatus("senderStatus");
    p_Data.setReportToSenderTargetChannel("POPUP");
    p_Data.setReportToSenderTargetParam("MyParms");
    p_Data.setReportToSenderTargetReceipt("SenderReceipt");
    p_Data.setSenderId("bart.simpson@silverpeas.com");
    p_Data.setSenderName("Bart Simpson");
    p_Data.setTargetChannel("SMTP");
    p_Data.setTargetName("Home Simpson");
    p_Data.setTargetParam(params);
    p_Data.setTargetReceipt("receipt");
    String result = NotificationServerUtil.convertNotificationDataToXML(p_Data);
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<NOTIFY>	<LOGIN>		<USER>"
        + "<![CDATA[user]]></USER>		<PASSWORD><![CDATA[password]]></PASSWORD>	</LOGIN>	"
        + "<MESSAGE><![CDATA[message]]></MESSAGE>	<SENDER>		<ID><![CDATA[bart.simpson@silverpeas"
        + ".com]]></ID>		<NAME><![CDATA[Bart Simpson]]></NAME>		<ANSWERALLOWED>true"
        + "</ANSWERALLOWED>	</SENDER>	<COMMENT><![CDATA[comment]]></COMMENT>	<TARGET CHANNEL=\"SMTP\">"
        + "		<NAME><![CDATA[Home Simpson]]></NAME>		<RECEIPT><![CDATA[receipt]]></RECEIPT>		"
        + "<PARAM><![CDATA[date=#DATE#" + dateParam.getTime() + ";string=bonjour le monde;; 0 + 0 == la tête à "
        + "toto]]></PARAM>	</TARGET>	<PRIORITY SPEED=\"fast\"/>	<REPORT>	</REPORT></NOTIFY>", result);
  }

  /**
   * Test of convertXMLToNotificationData method, of class NotificationServerUtil.
   * @throws Exception
   */
  @Test
  public void testConvertXMLToNotificationData() throws Exception {
    Date dateParam = RandomGenerator.getRandomCalendar().getTime();
    String p_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<NOTIFY>	<LOGIN>		<USER>"
        + "<![CDATA[user]]></USER>		<PASSWORD><![CDATA[password]]></PASSWORD>	</LOGIN>	"
        + "<MESSAGE><![CDATA[message]]></MESSAGE>	<SENDER>		<ID><![CDATA[bart.simpson@silverpeas"
        + ".com]]></ID>		<NAME><![CDATA[Bart Simpson]]></NAME>		<ANSWERALLOWED>true"
        + "</ANSWERALLOWED>	</SENDER>	<COMMENT><![CDATA[comment]]></COMMENT>	<TARGET CHANNEL=\"SMTP\">"
        + "		<NAME><![CDATA[Home Simpson]]></NAME>		<RECEIPT><![CDATA[receipt]]></RECEIPT>		"
        + "<PARAM><![CDATA[date=#DATE#" + dateParam.getTime() + ";string=bonjour le monde;; 0 + 0 == la tête à "
        + "toto]]></PARAM>	</TARGET>	<PRIORITY SPEED=\"fast\"/>	<REPORT>	</REPORT></NOTIFY>";
    NotificationData expResult = new NotificationData();
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("string", "bonjour le monde; 0 + 0 = la tête à toto");
    params.put("date", dateParam);
    expResult.setAnswerAllowed(true);
    expResult.setComment("comment");
    expResult.setLoginPassword("password");
    expResult.setLoginUser("user");
    expResult.setMessage("message");
    expResult.setNotificationId(0L);
    expResult.setPrioritySpeed("fast");
    expResult.setSenderId("bart.simpson@silverpeas.com");
    expResult.setSenderName("Bart Simpson");
    expResult.setTargetChannel("SMTP");
    expResult.setTargetName("Home Simpson");
    expResult.setTargetParam(params);
    expResult.setTargetReceipt("receipt");
    NotificationData result = NotificationServerUtil.convertXMLToNotificationData(p_XML);
    assertEquals(expResult, result);
  }

  /**
   * Test of unpackKeyValues method, of class NotificationServerUtil.
   */
  @Test
  public void testUnpackKeyValues() {
    String keyvaluestring = "date=#DATE#1358963160000;string=bonjour le monde;; 0 + 0 == la tête à toto;title=Titre d'œuvre";
    Map<String, Object> expResult = new HashMap<String, Object>(3);
    expResult.put("date", new Date(1358963160000l));
    expResult.put("string", "bonjour le monde; 0 + 0 = la tête à toto");
    expResult.put("title", "Titre d'œuvre");
    Map<String, Object> result = NotificationServerUtil.unpackKeyValues(keyvaluestring);
    assertEquals(expResult, result);
  }

  /**
   * Test of doubleSeparators method, of class NotificationServerUtil.
   */
  @Test
  public void testDoubleSeparators() {
    String simpleValue = "Le petit chat est mort";
    String expResult = "Le petit chat est mort";
    String result = NotificationServerUtil.doubleSeparators(simpleValue);
    assertEquals(expResult, result);
    String stringWithEqual = "0+0 = la tête à toto";
    expResult = "0+0 == la tête à toto";
    result = NotificationServerUtil.doubleSeparators(stringWithEqual);
    assertEquals(expResult, result);

    String stringWithComa = "Oh, Oh, oh; comment vas tu ?";
    expResult = "Oh, Oh, oh;; comment vas tu ?";
    result = NotificationServerUtil.doubleSeparators(stringWithComa);
    assertEquals(expResult, result);

  }

  /**
   * Test of packKeyValues method, of class NotificationServerUtil.
   */
  @Test
  public void testPackKeyValues() {
    Map<String, Object> params = new LinkedHashMap<String, Object>(2);
    params.put("date", new Date(1358963160000l));
    params.put("string", "bonjour le monde; 0 + 0 = la tête à toto");
    params.put("title", "Titre d'œuvre");
    String expResult = "date=#DATE#1358963160000;string=bonjour le monde;; 0 + 0 == la tête à toto;title=Titre d'œuvre";
    String result = NotificationServerUtil.packKeyValues(params);
    assertEquals(expResult, result);
  }
}
