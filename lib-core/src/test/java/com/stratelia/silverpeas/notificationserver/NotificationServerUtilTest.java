/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.notificationserver;

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
    params.put("date", RandomGenerator.getRandomCalendar().getTime());
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
        + "<PARAM><![CDATA[date=#DATE#1358963160000;string=bonjour le monde;; 0 + 0 == la tête à "
        + "toto]]></PARAM>	</TARGET>	<PRIORITY SPEED=\"fast\"/>	<REPORT>	</REPORT></NOTIFY>", result);
  }

  /**
   * Test of convertXMLToNotificationData method, of class NotificationServerUtil.
   * @throws Exception
   */
  @Test
  public void testConvertXMLToNotificationData() throws Exception {
    String p_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<NOTIFY>	<LOGIN>		<USER>"
        + "<![CDATA[user]]></USER>		<PASSWORD><![CDATA[password]]></PASSWORD>	</LOGIN>	"
        + "<MESSAGE><![CDATA[message]]></MESSAGE>	<SENDER>		<ID><![CDATA[bart.simpson@silverpeas"
        + ".com]]></ID>		<NAME><![CDATA[Bart Simpson]]></NAME>		<ANSWERALLOWED>true"
        + "</ANSWERALLOWED>	</SENDER>	<COMMENT><![CDATA[comment]]></COMMENT>	<TARGET CHANNEL=\"SMTP\">"
        + "		<NAME><![CDATA[Home Simpson]]></NAME>		<RECEIPT><![CDATA[receipt]]></RECEIPT>		"
        + "<PARAM><![CDATA[date=#DATE#1358963160000;string=bonjour le monde;; 0 + 0 == la tête à "
        + "toto]]></PARAM>	</TARGET>	<PRIORITY SPEED=\"fast\"/>	<REPORT>	</REPORT></NOTIFY>";
    NotificationData expResult = new NotificationData();
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("date", RandomGenerator.getRandomCalendar().getTime());
    params.put("string", "bonjour le monde; 0 + 0 = la tête à toto");
    expResult.setAnswerAllowed(true);
    expResult.setComment("comment");
    expResult.setLoginPassword("password");
    expResult.setLoginUser("user");
    expResult.setMessage("message");
    expResult.setNotificationId(RandomGenerator.getRandomLong());
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
