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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.util;

import com.stratelia.silverpeas.util.SilverpeasSettings;

/**
 * This class is the central location to store the internal JNDI names of various entities. Any
 * change here should also be reflected in the deployment descriptors.
 */
public class JNDINames extends SilverpeasSettings {

  /********************************** JNDI names for EJBs Home ************************************/
  public static String CLIPBOARD_EJBHOME = "ejb/ClipboardBm";
  public static String NODEBM_EJBHOME = "ejb/NodeBm";
  public static String NODE_EJBHOME = "ejb/Node";
  public static String COMMENT_EJBHOME = "ejb/Comment";
  public static String KMELIABM_EJBHOME = "ejb/KmeliaBm";
  public static String YELLOWPAGESBM_EJBHOME = "ejb/YellowpagesBm";
  public static String PUBLICATION_EJBHOME = "ejb/Publication";
  public static String PUBLICATIONBM_EJBHOME = "ejb/PublicationBm";
  public static String CONTACT_EJBHOME = "ejb/Contact";
  public static String CONTACTBM_EJBHOME = "ejb/ContactBm";
  public static String FAVORITBM_EJBHOME = "ejb/FavoritBm";
  public static String SUBSCRIBEBM_EJBHOME = "ejb/SubscribeBm";
  public static String STATISTICBM_EJBHOME = "ejb/StatisticBm";
  public static String READINGCONTROLBM_EJBHOME = "ejb/ReadingControlBm";
  public static String NEWSBM_EJBHOME = "ejb/NewsBm";
  public static String QUICKINFOBM_EJBHOME = "ejb/QuickInfoBm";
  public static String ALMANACHBM_EJBHOME = "ejb/AlmanachBm";
  public static String AGENDABM_EJBHOME = "ejb/AgendaBm";
  public static String CALENDARBM_EJBHOME = "ejb/CalendarBm";
  public static String POLLBM_EJBHOME = "ejb/PollingStationBm";
  public static String QUESTIONBM_EJBHOME = "ejb/QuestionBm";
  public static String QUESTIONRESULTBM_EJBHOME = "ejb/QuestionResultBm";
  public static String ANSWERBM_EJBHOME = "ejb/AnswerBm";
  public static String TOOLBOXBM_EJBHOME = "ejb/ToolBoxBm";
  public static String SURVEYBM_EJBHOME = "ejb/SurveyBm";
  public static String PERSONALIZATIONBM_EJBHOME = "ejb/PersonalizationBm";
  public static String QUIZZBM_EJBHOME = "ejb/QuizzBm";
  public static String SCOREBM_EJBHOME = "ejb/ScoreBm";
  public static String QUESTIONCONTAINERBM_EJBHOME = "ejb/QuestionContainerBm";
  public static String ATTACHMENT_EJBHOME = "ejb/Attachment";
  public static String VERSIONING_EJBHOME = "ejb/Versioning";
  public static String TASKMBM_EJBHOME = "ejb/TaskmBm";
  public static String TRAININGBM_EJBHOME = "ejb/TrainingBm";
  public static String COORDINATESBM_EJBHOME = "ejb/CoordinatesBm";
  public static String CONNECTEURJDBC_EJBHOME = "ejb/ConnecteurJDBCBm";
  public static String SILVERSTATISTICS_EJBHOME = "ejb/SilverStatistics";
  public static String INTEREST_CENTER_EJBHOME = "ejb/InterestCenter";
  public static String PDC_SUBSCRIPTION_EJBHOME = "ejb/pdcSubscription";
  public static String FORUMSBM_EJBHOME = "ejb/ForumsBM";
  public static String INDEXBM_EJBHOME = "ejb/IndexEngineBm";
  public static String SEARCHBM_EJBHOME = "ejb/SearchEngineBm";
  public static String BOOKMARKBM_EJBHOME = "ejb/BookmarkBm";
  public static String WEBSITESBM_EJBHOME = "ejb/webSitesBm";
  public static String FORMDESIGNERBM_EJBHOME = "ejb/FormDesignerBm";
  public static String FORMMANAGERBM_EJBHOME = "ejb/FormManagerBm";
  public static String KMAXBM_EJBHOME = "ejb/KmaxBm";
  public static String SMALLADSBM_EJBHOME = "ejb/SmalladsBm";
  public static String ADBM_EJBHOME = "ejb/AdBm";
  public static String AD_EJBHOME = "ejb/Ad";
  public static String DOCUMENTBM_EJBHOME = "ejb/DocumentBm";
  public static String BUSREMOTEACCESSBM_EJBHOME = "ejb/BusRemoteAccessBm";
  public static String ADMINBM_EJBHOME = "ejb/AdminBm";
  public static String PDCBM_EJBHOME = "ejb/PdcBm";
  public static String PLANACTIONSBM_EJBHOME = "ejb/PlanActionsBm";
  public static String PROJECTMANAGERBM_EJBHOME = "ejb/ProjectManagerBm";
  public static String THESAURUSBM_EJBHOME = "ejb/ThesaurusBm";
  public static String AUTHENTICATIONBM_EJBHOME = "ejb/AuthenticationBm";
  public static String GALLERYBM_EJBHOME = "ejb/GalleryBm";
  public static String FORMTEMPLATEBM_EJBHOME = "ejb/FormTemplateBm";
  public static String MYLINKSBM_EJBHOME = "ejb/MyLinks";
  public static String BLOGBM_EJBHOME = "ejb/BlogBm";
  public static String MYDBBM_EJBHOME = "ejb/MyDBBm";
  public static String TAGCLOUDBM_EJBHOME = "ejb/TagCloudBm";
  public static String NOTATIONBM_EJBHOME = "ejb/NotationBm";
  public static String CLASSIFIEDSBM_EJBHOME = "ejb/ClassifiedsBm";

  public static String WEBPUBMANAGER_EJBHOME = "ejb/WebPublicationManagerBm";

  /********************************** JNDI names for JMS QUEUE ************************************/
  public static String SILVERSTATISTICS_JMS_QUEUE =
      "com.stratelia.silverpeas.silverstatistics.jms.SilverStatisticsJMSQUEUE";
  public static String SILVERSTATISTICS_JMS_FACTORY =
      "com.stratelia.silverpeas.silverstatistics.jms.QueueConnectionFactory";

  /********************************** JNDI names for Data Sources ************************************/
  public static String SILVERPEAS_DATASOURCE = "java:comp/env/jdbc/Silverpeas";

  public static String PUBLICATION_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String INTEREST_CENTER_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String VERSIONING_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String CONTACT_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String NODE_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String FAVORIT_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String SUBSCRIBE_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String STATISTIC_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String CALENDAR_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String ALMANACH_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String FORUMS_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String QUESTION_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String ANSWER_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String SURVEY_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String PERSONALIZATION_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String SCORE_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String QUESTIONCONTAINER_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String BOOKMARK_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String WEBSITES_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String ATTACHMENT_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String TASKM_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String FORMDESIGNER_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String PDC_SUBSCRIPTION_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String PUZZLE_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String SILVERSTATISTICS_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String CRM_DATASOURCE = "java:comp/env/jdbc/Silverpeas";

  public static String THESAURUS_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String PDC_BUSIHM_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String FILEBOXPLUS_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String QUESTIONREPLY_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String WHITEPAGES_DATASOURCE = "java:comp/env/jdbc/Silverpeas";
  public static String PERSISTENCE_EJB_DATASOURCE = "java:comp/env/jdbc/Silverpeas";

  public static String WEBPUBMANAGER_DATASOURCE = "java:comp/env/jdbc/Silverpeas";

  public static String CONTAINERMANAGER_DATASOURCE = "jdbc/Silverpeas";
  public static String CONTENTMANAGER_DATASOURCE = "jdbc/Silverpeas";
  public static String CLASSIFYENGINE_DATASOURCE = "jdbc/Silverpeas";
  public static String PDC_DATASOURCE = "jdbc/Silverpeas";
  public static String ADMIN_DATASOURCE = "jdbc/Silverpeas";
  public static String FORMTEMPLATE_DATASOURCE = "jdbc/Silverpeas";
  public static String WORKFLOW_DATASOURCE = "jdbc/Silverpeas";
  public static String CHAT_DATASOURCE = "jdbc/Silverpeas";
  public static String INFOLETTER_DATASOURCE = "jdbc/Silverpeas";
  public static String OUTLOOK_DATASOURCE = "jdbc/Silverpeas";
  public static String GENERATOR_DATASOURCE = "jdbc/Silverpeas";
  public static String FORMDESIGNER_DB_DATASOURCE = "jdbc/Silverpeas";
  public static String DATABASE_DATASOURCE = "jdbc/Silverpeas";
  public static String PERSISTENCE_DB_DATASOURCE = "jdbc/Silverpeas";
  public static String DATAWARNING_DATASOURCE = "jdbc/Silverpeas";
  public static String WIKI_DATASOURCE = "jdbc/Silverpeas";

  public static String DIRECT_DATASOURCE = "serial://datasources/Silverpeas";

  /********************************** JNDI names for other resources ************************************/
  public static String MAIL_SESSION = "java:comp/env/MailSession";

  /******************************* JNDI names of application properties ************************************/
  public static String USE_CATALOG_EJB = "java:comp/env/useCatalogEJB";
  public static String SECURITY_ADAPTER_CLASSNAME = "java:comp/env/securityAdapterClassName";
  public static String SEND_CONFIRMATION_MAIL = "java:comp/env/sendConfirmationMail";

  public static String JMS_FACTORY =
      "com.stratelia.silverpeas.notificationserver.jms.QueueConnectionFactory";
  public static String JMS_QUEUE = "com.stratelia.silverpeas.notificationserver.jms.Queue";
  public static String JMS_HEADER_CHANNEL = "CHANNEL";

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.stratelia.webactiv.util.jndi", "");

    CLIPBOARD_EJBHOME = readString(rs, "CLIPBOARD_EJBHOME", "ejb/ClipboardBm");
    NODEBM_EJBHOME = readString(rs, "NODEBM_EJBHOME", "ejb/NodeBm");
    NODE_EJBHOME = readString(rs, "NODE_EJBHOME", "ejb/Node");
    COMMENT_EJBHOME = readString(rs, "COMMENT_EJBHOME", "ejb/Comment");
    KMELIABM_EJBHOME = readString(rs, "KMELIABM_EJBHOME", "ejb/KmeliaBm");
    YELLOWPAGESBM_EJBHOME = readString(rs, "YELLOWPAGESBM_EJBHOME",
        "ejb/YellowpagesBm");
    PUBLICATION_EJBHOME = readString(rs, "PUBLICATION_EJBHOME",
        "ejb/Publication");
    PUBLICATIONBM_EJBHOME = readString(rs, "PUBLICATIONBM_EJBHOME",
        "ejb/PublicationBm");
    CONTACT_EJBHOME = readString(rs, "CONTACT_EJBHOME", "ejb/Contact");
    CONTACTBM_EJBHOME = readString(rs, "CONTACTBM_EJBHOME", "ejb/ContactBm");
    FAVORITBM_EJBHOME = readString(rs, "FAVORITBM_EJBHOME", "ejb/FavoritBm");
    SUBSCRIBEBM_EJBHOME = readString(rs, "SUBSCRIBEBM_EJBHOME",
        "ejb/SubscribeBm");
    STATISTICBM_EJBHOME = readString(rs, "STATISTICBM_EJBHOME",
        "ejb/StatisticBm");
    READINGCONTROLBM_EJBHOME = readString(rs, "READINGCONTROLBM_EJBHOME",
        "ejb/ReadingControlBm");
    NEWSBM_EJBHOME = readString(rs, "NEWSBM_EJBHOME", "ejb/NewsBm");
    QUICKINFOBM_EJBHOME = readString(rs, "QUICKINFOBM_EJBHOME",
        "ejb/QuickInfoBm");
    ALMANACHBM_EJBHOME = readString(rs, "ALMANACHBM_EJBHOME", "ejb/AlmanachBm");
    AGENDABM_EJBHOME = readString(rs, "AGENDABM_EJBHOME", "ejb/AgendaBm");
    CALENDARBM_EJBHOME = readString(rs, "CALENDARBM_EJBHOME", "ejb/CalendarBm");
    POLLBM_EJBHOME = readString(rs, "POLLBM_EJBHOME", "ejb/PollingStationBm");
    QUESTIONBM_EJBHOME = readString(rs, "QUESTIONBM_EJBHOME", "ejb/QuestionBm");
    QUESTIONRESULTBM_EJBHOME = readString(rs, "QUESTIONRESULTBM_EJBHOME",
        "ejb/QuestionResultBm");
    ANSWERBM_EJBHOME = readString(rs, "ANSWERBM_EJBHOME", "ejb/AnswerBm");
    TOOLBOXBM_EJBHOME = readString(rs, "TOOLBOXBM_EJBHOME", "ejb/ToolBoxBm");
    SURVEYBM_EJBHOME = readString(rs, "SURVEYBM_EJBHOME", "ejb/SurveyBm");
    PERSONALIZATIONBM_EJBHOME = readString(rs, "PERSONALIZATIONBM_EJBHOME",
        "ejb/PersonalizationBm");
    QUIZZBM_EJBHOME = readString(rs, "QUIZZBM_EJBHOME", "ejb/QuizzBm");
    SCOREBM_EJBHOME = readString(rs, "SCOREBM_EJBHOME", "ejb/ScoreBm");
    QUESTIONCONTAINERBM_EJBHOME = readString(rs, "QUESTIONCONTAINERBM_EJBHOME",
        "ejb/QuestionContainerBm");
    ATTACHMENT_EJBHOME = readString(rs, "ATTACHMENT_EJBHOME", "ejb/Attachment");
    VERSIONING_EJBHOME = readString(rs, "VERSIONING_EJBHOME", "ejb/Versioning");
    TASKMBM_EJBHOME = readString(rs, "TASKMBM_EJBHOME", "ejb/TaskmBm");
    COORDINATESBM_EJBHOME = readString(rs, "COORDINATESBM_EJBHOME",
        "ejb/CoordinatesBm");
    CONNECTEURJDBC_EJBHOME = readString(rs, "CONNECTEURJDBC_EJBHOME",
        "ejb/ConnecteurJDBCBm");
    SILVERSTATISTICS_EJBHOME = readString(rs, "SILVERSTATISTICS_EJBHOME",
        "ejb/SilverStatistics");
    INTEREST_CENTER_EJBHOME = readString(rs, "INTEREST_CENTER_EJBHOME",
        "ejb/InterestCenter");
    PDC_SUBSCRIPTION_EJBHOME = readString(rs, "PDC_SUBSCRIPTION_EJBHOME",
        "ejb/pdcSubscription");
    FORUMSBM_EJBHOME = readString(rs, "FORUMSBM_EJBHOME", "ejb/ForumsBM");
    INDEXBM_EJBHOME = readString(rs, "INDEXBM_EJBHOME", "ejb/IndexEngineBm");
    SEARCHBM_EJBHOME = readString(rs, "SEARCHBM_EJBHOME", "ejb/SearchEngineBm");
    BOOKMARKBM_EJBHOME = readString(rs, "BOOKMARKBM_EJBHOME", "ejb/BookmarkBm");
    WEBSITESBM_EJBHOME = readString(rs, "WEBSITESBM_EJBHOME", "ejb/webSitesBm");
    FORMDESIGNERBM_EJBHOME = readString(rs, "FORMDESIGNERBM_EJBHOME",
        "ejb/FormDesignerBm");
    FORMMANAGERBM_EJBHOME = readString(rs, "FORMMANAGERBM_EJBHOME",
        "ejb/FormManagerBm");
    KMAXBM_EJBHOME = readString(rs, "KMAXBM_EJBHOME", "ejb/KmaxBm");
    ADBM_EJBHOME = readString(rs, "ADBM_EJBHOME", "ejb/AdBm");
    AD_EJBHOME = readString(rs, "AD_EJBHOME", "ejb/Ad");
    DOCUMENTBM_EJBHOME = readString(rs, "DOCUMENTBM_EJBHOME", "ejb/DocumentBm");
    BUSREMOTEACCESSBM_EJBHOME = readString(rs, "BUSREMOTEACCESSBM_EJBHOME",
        "ejb/BusRemoteAccessBm");
    ADMINBM_EJBHOME = readString(rs, "ADMINBM_EJBHOME", "ejb/AdminBm");
    PDCBM_EJBHOME = readString(rs, "PDCBM_EJBHOME", "ejb/PdcBm");
    PLANACTIONSBM_EJBHOME = readString(rs, "PLANACTIONSBM_EJBHOME",
        "ejb/PlanActionsBm");
    PROJECTMANAGERBM_EJBHOME = readString(rs, "PROJECTMANAGERBM_EJBHOME",
        "ejb/ProjectManagerBm");
    THESAURUSBM_EJBHOME = readString(rs, "THESAURUSBM_EJBHOME",
        "ejb/ThesaurusBm");
    AUTHENTICATIONBM_EJBHOME = readString(rs, "AUTHENTICATIONBM_EJBHOME",
        "ejb/AuthenticationBm");
    MYDBBM_EJBHOME = readString(rs, "MYDBBM_EJBHOME", "ejb/MyDBBm");

    WEBPUBMANAGER_EJBHOME = readString(rs, "WEBPUBMANAGER_EJBHOME",
        "ejb/WebPublicationManagerBm");

    SILVERSTATISTICS_JMS_QUEUE = readString(rs, "SILVERSTATISTICS_JMS_QUEUE",
        "com.stratelia.silverpeas.silverstatistics.jms.SilverStatisticsJMSQUEUE");
    SILVERSTATISTICS_JMS_FACTORY = readString(rs,
        "SILVERSTATISTICS_JMS_FACTORY",
        "com.stratelia.silverpeas.silverstatistics.jms.QueueConnectionFactory");

    SILVERPEAS_DATASOURCE = readString(rs, "SILVERPEAS_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");

    PUBLICATION_DATASOURCE = readString(rs, "PUBLICATION_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    INTEREST_CENTER_DATASOURCE = readString(rs, "INTEREST_CENTER_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    VERSIONING_DATASOURCE = readString(rs, "VERSIONING_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    CONTACT_DATASOURCE = readString(rs, "CONTACT_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    NODE_DATASOURCE = readString(rs, "NODE_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    FAVORIT_DATASOURCE = readString(rs, "FAVORIT_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    SUBSCRIBE_DATASOURCE = readString(rs, "SUBSCRIBE_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    STATISTIC_DATASOURCE = readString(rs, "STATISTIC_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    CALENDAR_DATASOURCE = readString(rs, "CALENDAR_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    ALMANACH_DATASOURCE = readString(rs, "ALMANACH_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    FORUMS_DATASOURCE = readString(rs, "FORUMS_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    QUESTION_DATASOURCE = readString(rs, "QUESTION_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    ANSWER_DATASOURCE = readString(rs, "ANSWER_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    SURVEY_DATASOURCE = readString(rs, "SURVEY_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    PERSONALIZATION_DATASOURCE = readString(rs, "PERSONALIZATION_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    SCORE_DATASOURCE = readString(rs, "SCORE_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    QUESTIONCONTAINER_DATASOURCE = readString(rs,
        "QUESTIONCONTAINER_DATASOURCE", "java:comp/env/jdbc/Silverpeas");
    BOOKMARK_DATASOURCE = readString(rs, "BOOKMARK_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    WEBSITES_DATASOURCE = readString(rs, "WEBSITES_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    ATTACHMENT_DATASOURCE = readString(rs, "ATTACHMENT_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    TASKM_DATASOURCE = readString(rs, "TASKM_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    FORMDESIGNER_DATASOURCE = readString(rs, "FORMDESIGNER_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    PDC_SUBSCRIPTION_DATASOURCE = readString(rs, "PDC_SUBSCRIPTION_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    PUZZLE_DATASOURCE = readString(rs, "PUZZLE_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    SILVERSTATISTICS_DATASOURCE = readString(rs, "SILVERSTATISTICS_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    CRM_DATASOURCE = readString(rs, "CRM_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");

    THESAURUS_DATASOURCE = readString(rs, "THESAURUS_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    PDC_BUSIHM_DATASOURCE = readString(rs, "PDC_BUSIHM_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    FILEBOXPLUS_DATASOURCE = readString(rs, "FILEBOXPLUS_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    QUESTIONREPLY_DATASOURCE = readString(rs, "QUESTIONREPLY_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    WHITEPAGES_DATASOURCE = readString(rs, "WHITEPAGES_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");
    PERSISTENCE_EJB_DATASOURCE = readString(rs, "PERSISTENCE_EJB_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");

    WEBPUBMANAGER_DATASOURCE = readString(rs, "WEBPUBMANAGER_DATASOURCE",
        "java:comp/env/jdbc/Silverpeas");

    CONTAINERMANAGER_DATASOURCE = readString(rs, "CONTAINERMANAGER_DATASOURCE",
        "jdbc/Silverpeas");
    CONTENTMANAGER_DATASOURCE = readString(rs, "CONTENTMANAGER_DATASOURCE",
        "jdbc/Silverpeas");
    CLASSIFYENGINE_DATASOURCE = readString(rs, "CLASSIFYENGINE_DATASOURCE",
        "jdbc/Silverpeas");
    PDC_DATASOURCE = readString(rs, "PDC_DATASOURCE", "jdbc/Silverpeas");
    ADMIN_DATASOURCE = readString(rs, "ADMIN_DATASOURCE", "jdbc/Silverpeas");
    FORMTEMPLATE_DATASOURCE = readString(rs, "FORMTEMPLATE_DATASOURCE",
        "jdbc/Silverpeas");
    WORKFLOW_DATASOURCE = readString(rs, "WORKFLOW_DATASOURCE",
        "jdbc/Silverpeas");
    CHAT_DATASOURCE = readString(rs, "CHAT_DATASOURCE", "jdbc/Silverpeas");
    INFOLETTER_DATASOURCE = readString(rs, "INFOLETTER_DATASOURCE",
        "jdbc/Silverpeas");
    OUTLOOK_DATASOURCE = readString(rs, "OUTLOOK_DATASOURCE", "jdbc/Silverpeas");
    GENERATOR_DATASOURCE = readString(rs, "GENERATOR_DATASOURCE",
        "jdbc/Silverpeas");
    FORMDESIGNER_DB_DATASOURCE = readString(rs, "FORMDESIGNER_DB_DATASOURCE",
        "jdbc/Silverpeas");
    DATABASE_DATASOURCE = readString(rs, "DATABASE_DATASOURCE",
        "jdbc/Silverpeas");
    PERSISTENCE_DB_DATASOURCE = readString(rs, "PERSISTENCE_DB_DATASOURCE",
        "jdbc/Silverpeas");
    WIKI_DATASOURCE = readString(rs, "WIKI_DATASOURCE", "jdbc/Silverpeas");

    DIRECT_DATASOURCE = readString(rs, "DIRECT_DATASOURCE",
        "serial://datasources/Silverpeas");

    MAIL_SESSION = readString(rs, "MAIL_SESSION", "java:comp/env/MailSession");

    USE_CATALOG_EJB = readString(rs, "USE_CATALOG_EJB",
        "java:comp/env/useCatalogEJB");
    SECURITY_ADAPTER_CLASSNAME = readString(rs, "SECURITY_ADAPTER_CLASSNAME",
        "java:comp/env/securityAdapterClassName");
    SEND_CONFIRMATION_MAIL = readString(rs, "SEND_CONFIRMATION_MAIL",
        "java:comp/env/sendConfirmationMail");

    JMS_FACTORY = readString(rs, "JMS_FACTORY",
        "com.stratelia.silverpeas.notificationserver.jms.QueueConnectionFactory");
    JMS_QUEUE = readString(rs, "JMS_QUEUE",
        "com.stratelia.silverpeas.notificationserver.jms.Queue");
    JMS_HEADER_CHANNEL = readString(rs, "JMS_HEADER_CHANNEL", "CHANNEL");
  }

}