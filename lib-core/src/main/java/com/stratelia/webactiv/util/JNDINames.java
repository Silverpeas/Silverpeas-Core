/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.util;

/**
 * This class is the central location to store the internal JNDI names of various entities. Any
 * change here should also be reflected in the deployment descriptors.
 */
public class JNDINames {

  /********************************** JNDI names for EJBs Home ************************************/
  public static final String CLIPBOARD_EJBHOME;
  public static final String NODEBM_EJBHOME;
  public static final String NODE_EJBHOME;
  public static final String COMMENT_EJBHOME;
  public static final String KMELIABM_EJBHOME;
  public static final String YELLOWPAGESBM_EJBHOME;
  public static final String PUBLICATION_EJBHOME;
  public static final String PUBLICATIONBM_EJBHOME;
  public static final String CONTACT_EJBHOME;
  public static final String CONTACTBM_EJBHOME;
  public static final String FAVORITBM_EJBHOME;
  public static final String SUBSCRIBEBM_EJBHOME;
  public static final String STATISTICBM_EJBHOME;
  public static final String NEWSBM_EJBHOME;
  public static final String QUICKINFOBM_EJBHOME;
  public static final String ALMANACHBM_EJBHOME;
  public static final String AGENDABM_EJBHOME;
  public static final String CALENDARBM_EJBHOME;
  public static final String POLLBM_EJBHOME;
  public static final String QUESTIONBM_EJBHOME;
  public static final String QUESTIONRESULTBM_EJBHOME;
  public static final String ANSWERBM_EJBHOME;
  public static final String TOOLBOXBM_EJBHOME;
  public static final String SURVEYBM_EJBHOME;
  public static final String PERSONALIZATIONBM_EJBHOME;
  public static final String QUIZZBM_EJBHOME;
  public static final String SCOREBM_EJBHOME;
  public static final String QUESTIONCONTAINERBM_EJBHOME;
  public static final String ATTACHMENT_EJBHOME;
  public static final String VERSIONING_EJBHOME;
  public static final String TASKMBM_EJBHOME;
  public static final String COORDINATESBM_EJBHOME;
  public static final String CONNECTEURJDBC_EJBHOME;
  public static final String SILVERSTATISTICS_EJBHOME;
  public static final String INTEREST_CENTER_EJBHOME;
  public static final String PDC_SUBSCRIPTION_EJBHOME;
  public static final String FORUMSBM_EJBHOME;
  public static final String INDEXBM_EJBHOME;
  public static final String SEARCHBM_EJBHOME;
  public static final String BOOKMARKBM_EJBHOME;
  public static final String WEBSITESBM_EJBHOME;
  public static final String FORMDESIGNERBM_EJBHOME;
  public static final String FORMMANAGERBM_EJBHOME;
  public static final String KMAXBM_EJBHOME;
  public static final String ADBM_EJBHOME;
  public static final String AD_EJBHOME;
  public static final String DOCUMENTBM_EJBHOME;
  public static final String BUSREMOTEACCESSBM_EJBHOME;
  public static final String ADMINBM_EJBHOME;
  public static final String PDCBM_EJBHOME;
  public static final String PLANACTIONSBM_EJBHOME;
  public static final String PROJECTMANAGERBM_EJBHOME;
  public static final String THESAURUSBM_EJBHOME;
  public static final String AUTHENTICATIONBM_EJBHOME;
  public static final String GALLERYBM_EJBHOME;
  public static final String FORMTEMPLATEBM_EJBHOME;
  public static final String MYLINKSBM_EJBHOME;
  public static final String BLOGBM_EJBHOME;
  public static final String MYDBBM_EJBHOME;
  public static final String TAGCLOUDBM_EJBHOME;
  public static final String NOTATIONBM_EJBHOME;
  public static final String CLASSIFIEDSBM_EJBHOME;
  public static final String WEBPUBMANAGER_EJBHOME;
  /********************************** JNDI names for JMS QUEUE ************************************/
  public static final String SILVERSTATISTICS_JMS_QUEUE;
  public static final String SILVERSTATISTICS_JMS_FACTORY;
  /********************************** JNDI names for Data Sources ************************************/
  public static final String SILVERPEAS_DATASOURCE;
  public static final String PUBLICATION_DATASOURCE;
  public static final String INTEREST_CENTER_DATASOURCE;
  public static final String VERSIONING_DATASOURCE;
  public static final String CONTACT_DATASOURCE;
  public static final String NODE_DATASOURCE;
  public static final String FAVORIT_DATASOURCE;
  public static final String SUBSCRIBE_DATASOURCE;
  public static final String STATISTIC_DATASOURCE;
  public static final String CALENDAR_DATASOURCE;
  public static final String ALMANACH_DATASOURCE;
  public static final String FORUMS_DATASOURCE;
  public static final String QUESTION_DATASOURCE;
  public static final String ANSWER_DATASOURCE;
  public static final String SURVEY_DATASOURCE;
  public static final String PERSONALIZATION_DATASOURCE;
  public static final String SCORE_DATASOURCE;
  public static final String QUESTIONCONTAINER_DATASOURCE;
  public static final String BOOKMARK_DATASOURCE;
  public static final String WEBSITES_DATASOURCE;
  public static final String ATTACHMENT_DATASOURCE;
  public static final String THUMBNAIL_DATASOURCE;
  public static final String TASKM_DATASOURCE;
  public static final String FORMDESIGNER_DATASOURCE;
  public static final String PDC_SUBSCRIPTION_DATASOURCE;
  public static final String PUZZLE_DATASOURCE;
  public static final String SILVERSTATISTICS_DATASOURCE;
  public static final String CRM_DATASOURCE;
  public static final String THESAURUS_DATASOURCE;
  public static final String PDC_BUSIHM_DATASOURCE;
  public static final String FILEBOXPLUS_DATASOURCE;
  public static final String QUESTIONREPLY_DATASOURCE;
  public static final String WHITEPAGES_DATASOURCE;
  public static final String PERSISTENCE_EJB_DATASOURCE;
  public static final String WEBPUBMANAGER_DATASOURCE;
  public static final String CONTAINERMANAGER_DATASOURCE;
  public static final String CONTENTMANAGER_DATASOURCE;
  public static final String CLASSIFYENGINE_DATASOURCE;
  public static final String PDC_DATASOURCE;
  public static final String ADMIN_DATASOURCE;
  public static final String FORMTEMPLATE_DATASOURCE;
  public static final String WORKFLOW_DATASOURCE;
  public static final String CHAT_DATASOURCE;
  public static final String INFOLETTER_DATASOURCE;
  public static final String OUTLOOK_DATASOURCE;
  public static final String GENERATOR_DATASOURCE;
  public static final String FORMDESIGNER_DB_DATASOURCE;
  public static final String DATABASE_DATASOURCE;
  public static final String PERSISTENCE_DB_DATASOURCE;
  public static final String DATAWARNING_DATASOURCE;
  public static final String WIKI_DATASOURCE;
  public static final String DIRECT_DATASOURCE;
  /********************************** JNDI names for other resources ************************************/
  public static final String MAIL_SESSION;
  /******************************* JNDI names of application properties ************************************/
  public static final String USE_CATALOG_EJB;
  public static final String SECURITY_ADAPTER_CLASSNAME;
  public static final String SEND_CONFIRMATION_MAIL;
  public static final String JMS_FACTORY;
  public static final String JMS_QUEUE;
  public static final String JMS_HEADER_CHANNEL;
  public static final String NOTIF_API_JMS;

  static {
    ResourceLocator rs = new ResourceLocator("com.stratelia.webactiv.util.jndi", "");

    CLIPBOARD_EJBHOME = rs.getString("CLIPBOARD_EJBHOME", "ejb/ClipboardBm");
    NODEBM_EJBHOME = rs.getString("NODEBM_EJBHOME", "ejb/NodeBm");
    NODE_EJBHOME = rs.getString("NODE_EJBHOME", "ejb/Node");
    COMMENT_EJBHOME = rs.getString("COMMENT_EJBHOME", "ejb/Comment");
    KMELIABM_EJBHOME = rs.getString("KMELIABM_EJBHOME", "ejb/KmeliaBm");
    YELLOWPAGESBM_EJBHOME = rs.getString("YELLOWPAGESBM_EJBHOME", "ejb/YellowpagesBm");
    PUBLICATION_EJBHOME = rs.getString("PUBLICATION_EJBHOME", "ejb/Publication");
    PUBLICATIONBM_EJBHOME = rs.getString("PUBLICATIONBM_EJBHOME", "ejb/PublicationBm");
    CONTACT_EJBHOME = rs.getString("CONTACT_EJBHOME", "ejb/Contact");
    CONTACTBM_EJBHOME = rs.getString("CONTACTBM_EJBHOME", "ejb/ContactBm");
    FAVORITBM_EJBHOME = rs.getString("FAVORITBM_EJBHOME", "ejb/FavoritBm");
    SUBSCRIBEBM_EJBHOME = rs.getString("SUBSCRIBEBM_EJBHOME", "ejb/SubscribeBm");
    STATISTICBM_EJBHOME = rs.getString("STATISTICBM_EJBHOME", "ejb/StatisticBm");
    NEWSBM_EJBHOME = rs.getString("NEWSBM_EJBHOME", "ejb/NewsBm");
    QUICKINFOBM_EJBHOME = rs.getString("QUICKINFOBM_EJBHOME", "ejb/QuickInfoBm");
    ALMANACHBM_EJBHOME = rs.getString("ALMANACHBM_EJBHOME", "ejb/AlmanachBm");
    AGENDABM_EJBHOME = rs.getString("AGENDABM_EJBHOME", "ejb/AgendaBm");
    CALENDARBM_EJBHOME = rs.getString("CALENDARBM_EJBHOME", "ejb/CalendarBm");
    POLLBM_EJBHOME = rs.getString("POLLBM_EJBHOME", "ejb/PollingStationBm");
    QUESTIONBM_EJBHOME = rs.getString("QUESTIONBM_EJBHOME", "ejb/QuestionBm");
    QUESTIONRESULTBM_EJBHOME = rs.getString("QUESTIONRESULTBM_EJBHOME", "ejb/QuestionResultBm");
    ANSWERBM_EJBHOME = rs.getString("ANSWERBM_EJBHOME", "ejb/AnswerBm");
    TOOLBOXBM_EJBHOME = rs.getString("TOOLBOXBM_EJBHOME", "ejb/ToolBoxBm");
    SURVEYBM_EJBHOME = rs.getString("SURVEYBM_EJBHOME", "ejb/SurveyBm");
    PERSONALIZATIONBM_EJBHOME = rs.getString("PERSONALIZATIONBM_EJBHOME", "ejb/PersonalizationBm");
    QUIZZBM_EJBHOME = rs.getString("QUIZZBM_EJBHOME", "ejb/QuizzBm");
    SCOREBM_EJBHOME = rs.getString("SCOREBM_EJBHOME", "ejb/ScoreBm");
    QUESTIONCONTAINERBM_EJBHOME = rs.getString("QUESTIONCONTAINERBM_EJBHOME",
            "ejb/QuestionContainerBm");
    ATTACHMENT_EJBHOME = rs.getString("ATTACHMENT_EJBHOME", "ejb/Attachment");
    VERSIONING_EJBHOME = rs.getString("VERSIONING_EJBHOME", "ejb/Versioning");
    TASKMBM_EJBHOME = rs.getString("TASKMBM_EJBHOME", "ejb/TaskmBm");
    COORDINATESBM_EJBHOME = rs.getString("COORDINATESBM_EJBHOME", "ejb/CoordinatesBm");
    GALLERYBM_EJBHOME = rs.getString("GALLERYBM_EJBHOME", "ejb/GalleryBm");
    CONNECTEURJDBC_EJBHOME = rs.getString("CONNECTEURJDBC_EJBHOME", "ejb/ConnecteurJDBCBm");
    SILVERSTATISTICS_EJBHOME = rs.getString("SILVERSTATISTICS_EJBHOME", "ejb/SilverStatistics");
    INTEREST_CENTER_EJBHOME = rs.getString("INTEREST_CENTER_EJBHOME", "ejb/InterestCenter");
    PDC_SUBSCRIPTION_EJBHOME = rs.getString("PDC_SUBSCRIPTION_EJBHOME", "ejb/pdcSubscription");
    FORUMSBM_EJBHOME = rs.getString("FORUMSBM_EJBHOME", "ejb/ForumsBM");
    INDEXBM_EJBHOME = rs.getString("INDEXBM_EJBHOME", "ejb/IndexEngineBm");
    SEARCHBM_EJBHOME = rs.getString("SEARCHBM_EJBHOME", "ejb/SearchEngineBm");
    BOOKMARKBM_EJBHOME = rs.getString("BOOKMARKBM_EJBHOME", "ejb/BookmarkBm");
    WEBSITESBM_EJBHOME = rs.getString("WEBSITESBM_EJBHOME", "ejb/webSitesBm");
    FORMDESIGNERBM_EJBHOME = rs.getString("FORMDESIGNERBM_EJBHOME", "ejb/FormDesignerBm");
    FORMMANAGERBM_EJBHOME = rs.getString("FORMMANAGERBM_EJBHOME", "ejb/FormManagerBm");
    KMAXBM_EJBHOME = rs.getString("KMAXBM_EJBHOME", "ejb/KmaxBm");
    ADBM_EJBHOME = rs.getString("ADBM_EJBHOME", "ejb/AdBm");
    AD_EJBHOME = rs.getString("AD_EJBHOME", "ejb/Ad");
    DOCUMENTBM_EJBHOME = rs.getString("DOCUMENTBM_EJBHOME", "ejb/DocumentBm");
    BUSREMOTEACCESSBM_EJBHOME = rs.getString("BUSREMOTEACCESSBM_EJBHOME", "ejb/BusRemoteAccessBm");
    ADMINBM_EJBHOME = rs.getString("ADMINBM_EJBHOME", "ejb/AdminBm");
    PDCBM_EJBHOME = rs.getString("PDCBM_EJBHOME", "ejb/PdcBm");
    PLANACTIONSBM_EJBHOME = rs.getString("PLANACTIONSBM_EJBHOME", "ejb/PlanActionsBm");
    PROJECTMANAGERBM_EJBHOME = rs.getString("PROJECTMANAGERBM_EJBHOME", "ejb/ProjectManagerBm");
    THESAURUSBM_EJBHOME = rs.getString("THESAURUSBM_EJBHOME", "ejb/ThesaurusBm");
    AUTHENTICATIONBM_EJBHOME = rs.getString("AUTHENTICATIONBM_EJBHOME", "ejb/AuthenticationBm");
    MYDBBM_EJBHOME = rs.getString("MYDBBM_EJBHOME", "ejb/MyDBBm");
    FORMTEMPLATEBM_EJBHOME = rs.getString("FORMTEMPLATEBM_EJBHOME", "ejb/FormTemplateBm");
    MYLINKSBM_EJBHOME = rs.getString("MYLINKSBM_EJBHOME", "ejb/MyLinks");
    WEBPUBMANAGER_EJBHOME = rs.getString("WEBPUBMANAGER_EJBHOME", "ejb/WebPublicationManagerBm");
    BLOGBM_EJBHOME = rs.getString("BLOGBM_EJBHOME", "ejb/BlogBm");
    TAGCLOUDBM_EJBHOME = rs.getString("TAGCLOUDBM_EJBHOME", "ejb/TagCloudBm");
    NOTATIONBM_EJBHOME = rs.getString("NOTATIONBM_EJBHOME", "ejb/NotationBm");
    CLASSIFIEDSBM_EJBHOME = rs.getString("CLASSIFIEDSBM_EJBHOME", "ejb/ClassifiedsBm");
    SILVERSTATISTICS_JMS_QUEUE = rs.getString("SILVERSTATISTICS_JMS_QUEUE",
            "com.stratelia.silverpeas.silverstatistics.jms.SilverStatisticsJMSQUEUE");
    SILVERSTATISTICS_JMS_FACTORY = rs.getString("SILVERSTATISTICS_JMS_FACTORY",
            "com.stratelia.silverpeas.silverstatistics.jms.QueueConnectionFactory");
    SILVERPEAS_DATASOURCE = rs.getString("SILVERPEAS_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PUBLICATION_DATASOURCE = rs.getString("PUBLICATION_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    INTEREST_CENTER_DATASOURCE = rs.getString("INTEREST_CENTER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    VERSIONING_DATASOURCE = rs.getString("VERSIONING_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    CONTACT_DATASOURCE = rs.getString("CONTACT_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    NODE_DATASOURCE = rs.getString("NODE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    FAVORIT_DATASOURCE = rs.getString("FAVORIT_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    SUBSCRIBE_DATASOURCE = rs.getString("SUBSCRIBE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    STATISTIC_DATASOURCE = rs.getString("STATISTIC_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    CALENDAR_DATASOURCE = rs.getString("CALENDAR_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    ALMANACH_DATASOURCE = rs.getString("ALMANACH_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    FORUMS_DATASOURCE = rs.getString("FORUMS_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    QUESTION_DATASOURCE = rs.getString("QUESTION_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    ANSWER_DATASOURCE = rs.getString("ANSWER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    SURVEY_DATASOURCE = rs.getString("SURVEY_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PERSONALIZATION_DATASOURCE = rs.getString("PERSONALIZATION_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    SCORE_DATASOURCE = rs.getString("SCORE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    QUESTIONCONTAINER_DATASOURCE = rs.getString("QUESTIONCONTAINER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    BOOKMARK_DATASOURCE = rs.getString("BOOKMARK_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    WEBSITES_DATASOURCE = rs.getString("WEBSITES_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    ATTACHMENT_DATASOURCE = rs.getString("ATTACHMENT_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    TASKM_DATASOURCE = rs.getString("TASKM_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    FORMDESIGNER_DATASOURCE = rs.getString("FORMDESIGNER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PDC_SUBSCRIPTION_DATASOURCE = rs.getString("PDC_SUBSCRIPTION_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PUZZLE_DATASOURCE = rs.getString("PUZZLE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    SILVERSTATISTICS_DATASOURCE = rs.getString("SILVERSTATISTICS_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    CRM_DATASOURCE = rs.getString("CRM_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    DATAWARNING_DATASOURCE = rs.getString("DATAWARNING_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    THESAURUS_DATASOURCE = rs.getString("THESAURUS_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PDC_BUSIHM_DATASOURCE = rs.getString("PDC_BUSIHM_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    FILEBOXPLUS_DATASOURCE = rs.getString("FILEBOXPLUS_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    QUESTIONREPLY_DATASOURCE = rs.getString("QUESTIONREPLY_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    WHITEPAGES_DATASOURCE = rs.getString("WHITEPAGES_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PERSISTENCE_EJB_DATASOURCE = rs.getString("PERSISTENCE_EJB_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    WEBPUBMANAGER_DATASOURCE = rs.getString("WEBPUBMANAGER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    CONTAINERMANAGER_DATASOURCE = rs.getString("CONTAINERMANAGER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    CONTENTMANAGER_DATASOURCE = rs.getString("CONTENTMANAGER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    CLASSIFYENGINE_DATASOURCE = rs.getString("CLASSIFYENGINE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PDC_DATASOURCE = rs.getString("PDC_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    ADMIN_DATASOURCE = rs.getString("ADMIN_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    FORMTEMPLATE_DATASOURCE = rs.getString("FORMTEMPLATE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    WORKFLOW_DATASOURCE = rs.getString("WORKFLOW_DATASOURCE", "jdbc/Silverpeas");
    CHAT_DATASOURCE = rs.getString("CHAT_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    INFOLETTER_DATASOURCE = rs.getString("INFOLETTER_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    OUTLOOK_DATASOURCE = rs.getString("OUTLOOK_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    GENERATOR_DATASOURCE = rs.getString("GENERATOR_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    FORMDESIGNER_DB_DATASOURCE = rs.getString("FORMDESIGNER_DB_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    DATABASE_DATASOURCE = rs.getString("DATABASE_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    PERSISTENCE_DB_DATASOURCE = rs.getString("PERSISTENCE_DB_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    WIKI_DATASOURCE = rs.getString("WIKI_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    THUMBNAIL_DATASOURCE = rs.getString("THUMBNAIL_DATASOURCE", "java:/datasources/silverpeas-jdbc");
    DIRECT_DATASOURCE = rs.getString("DIRECT_DATASOURCE", "serial://datasources/Silverpeas");
    MAIL_SESSION = rs.getString("MAIL_SESSION", "java:comp/env/MailSession");
    USE_CATALOG_EJB = rs.getString("USE_CATALOG_EJB", "java:comp/env/useCatalogEJB");
    SECURITY_ADAPTER_CLASSNAME = rs.getString("SECURITY_ADAPTER_CLASSNAME",
            "java:comp/env/securityAdapterClassName");
    SEND_CONFIRMATION_MAIL = rs.getString("SEND_CONFIRMATION_MAIL",
            "java:comp/env/sendConfirmationMail");
    JMS_FACTORY = rs.getString("JMS_FACTORY",
            "com.stratelia.silverpeas.notificationserver.jms.QueueConnectionFactory");
    NOTIF_API_JMS = rs.getString("NOTIF_API_JMS", JMS_FACTORY);
    JMS_QUEUE = rs.getString("JMS_QUEUE", "com.stratelia.silverpeas.notificationserver.jms.Queue");
    JMS_HEADER_CHANNEL = rs.getString("JMS_HEADER_CHANNEL", "CHANNEL");
  }
}