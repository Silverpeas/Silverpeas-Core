package org.silverpeas.sharing.web;

import javax.inject.Named;

import com.silverpeas.web.TestResources;

@Named(TestResources.TEST_RESOURCES_NAME)
public class TicketTestRessources extends TestResources {

  public static final String JAVA_PACKAGE = "org.silverpeas.sharing.web";
  public static final String SPRING_CONTEXT = "spring-sharing-webservice.xml";

  public static final String A_URI= "mytickets/";
  public static final String UNEXISTING_URI = "mytickets/kmelia99";

  public static final String INSTANCE_ID = "kmelia12";

}
