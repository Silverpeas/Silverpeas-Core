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
package com.stratelia.webactiv.util;

import com.stratelia.silverpeas.util.SilverpeasSettings;

/**
 * This class is the central location to store the internal JNDI names of various entities. Any
 * change here should also be reflected in the deployment descriptors.
 */
public class JNDINames extends SilverpeasSettings {

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

  static {
    ResourceLocator rs = new ResourceLocator("com.stratelia.webactiv.util.jndi", "");

    CLIPBOARD_EJBHOME = readString(rs, "CLIPBOARD_EJBHOME", "ejb/ClipboardBm");
    NODEBM_EJBHOME = readString(rs, "NODEBM_EJBHOME", "ejb/NodeBm");
    NODE_EJBHOME = readString(rs, "NODE_EJBHOME", "ejb/Node");
    COMMENT_EJBHOME = readString(rs, "COMMENT_EJBHOME", "ejb/Comment");
    KMELIABM_EJBHOME = readString(rs, "KMELIABM_EJBHOME", "ejb/KmeliaBm");
    YELLOWPAGESBM_EJBHOME = readString(rs, "YELLOWPAGESBM_EJBHOME", "ejb/YellowpagesBm");
    PUBLICATION_EJBHOME = readString(rs, "PUBLICATION_EJBHOME", "ejb/Publication");
    PUBLICATIONBM_EJBHOME = readString(rs, "PUBLICATIONBM_EJBHOME", "ejb/PublicationBm");
    CONTACT_EJBHOME = readString(rs, "CONTACT_EJBHOME", "ejb/Contact");
    CONTACTBM_EJBHOME = readString(rs, "CONTACTBM_EJBHOME", "ejb/ContactBm");
    FAVORITBM_EJBHOME = readString(rs, "FAVORITBM_EJBHOME", "ejb/FavoritBm");
    SUBSCRIBEBM_EJBHOME = readString(rs, "SUBSCRIBEBM_EJBHOME", "ejb/SubscribeBm");
    STATISTICBM_EJBHOME = readString(rs, "STATISTICBM_EJBHOME", "ejb/StatisticBm");
    NEWSBM_EJBHOME = readString(rs, "NEWSBM_EJBHOME", "ejb/NewsBm");
    QUICKINFOBM_EJBHOME = readString(rs, "QUICKINFOBM_EJBHOME", "ejb/QuickInfoBm");
    ALMANACHBM_EJBHOME = readString(rs, "ALMANACHBM_EJBHOME", "ejb/AlmanachBm");
    AGENDABM_EJBHOME = readString(rs, "AGENDABM_EJBHOME", "ejb/AgendaBm");
    CALENDARBM_EJBHOME = readString(rs, "CALENDARBM_EJBHOME", "ejb/CalendarBm");
    POLLBM_EJBHOME = readString(rs, "POLLBM_EJBHOME", "ejb/PollingStationBm");
    QUESTIONBM_EJBHOME = readString(rs, "QUESTIONBM_EJBHOME", "ejb/QuestionBm");
    QUESTIONRESULTBM_EJBHOME = readString(rs, "QUESTIONRESULTBM_EJBHOME", "ejb/QuestionResultBm");
    ANSWERBM_EJBHOME = readString(rs, "ANSWERBM_EJBHOME", "ejb/AnswerBm");
    TOOLBOXBM_EJBHOME = readString(rs, "TOOLBOXBM_EJBHOME", "ejb/ToolBoxBm");
    SURVEYBM_EJBHOME = readString(rs, "SURVEYBM_EJBHOME", "ejb/SurveyBm");
    PERSONALIZATIONBM_EJBHOME = readString(rs, "PERSONALIZATIONBM_EJBHOME", "ejb/PersonalizationBm");
    QUIZZBM_EJBHOME = readString(rs, "QUIZZBM_EJBHOME", "ejb/QuizzBm");
    SCOREBM_EJBHOME = readString(rs, "SCOREBM_EJBHOME", "ejb/ScoreBm");
    QUESTIONCONTAINERBM_EJBHOME = readString(rs, "QUESTIONCONTAINERBM_EJBHOME",
        "ejb/QuestionContainerBm");
    ATTACHMENT_EJBHOME = readString(rs, "ATTACHMENT_EJBHOME", "ejb/Attachment");
    VERSIONING_EJBHOME = readString(rs, "VERSIONING_EJBHOME", "ejb/Versioning");
    TASKMBM_EJBHOME = readString(rs, "TASKMBM_EJBHOME", "ejb/TaskmBm");
    COORDINATESBM_EJBHOME = readString(rs, "COORDINATESBM_EJBHOME", "ejb/CoordinatesBm");
    GALLERYBM_EJBHOME = readString(rs, "GALLERYBM_EJBHOME", "ejb/GalleryBm");
    CONNECTEURJDBC_EJBHOME = readString(rs, "CONNECTEURJDBC_EJBHOME", "ejb/ConnecteurJDBCBm");
    SILVERSTATISTICS_EJBHOME = readString(rs, "SILVERSTATISTICS_EJBHOME", "ejb/SilverStatistics");
    INTEREST_CENTER_EJBHOME = readString(rs, "INTEREST_CENTER_EJBHOME", "ejb/InterestCenter");
    PDC_SUBSCRIPTION_EJBHOME = readString(rs, "PDC_SUBSCRIPTION_EJBHOME", "ejb/pdcSubscription");
    FORUMSBM_EJBHOME = readString(rs, "FORUMSBM_EJBHOME", "ejb/ForumsBM");
    INDEXBM_EJBHOME = readString(rs, "INDEXBM_EJBHOME", "ejb/IndexEngineBm");
    SEARCHBM_EJBHOME = readString(rs, "SEARCHBM_EJBHOME", "ejb/SearchEngineBm");
    BOOKMARKBM_EJBHOME = readString(rs, "BOOKMARKBM_EJBHOME", "ejb/BookmarkBm");
    WEBSITESBM_EJBHOME = readString(rs, "WEBSITESBM_EJBHOME", "ejb/webSitesBm");
    FORMDESIGNERBM_EJBHOME = readString(rs, "FORMDESIGNERBM_EJBHOME", "ejb/FormDesignerBm");
    FORMMANAGERBM_EJBHOME = readString(rs, "FORMMANAGERBM_EJBHOME", "ejb/FormManagerBm");
    KMAXBM_EJBHOME = readString(rs, "KMAXBM_EJBHOME", "ejb/KmaxBm");
    ADBM_EJBHOME = readString(rs, "ADBM_EJBHOME", "ejb/AdBm");
    AD_EJBHOME = readString(rs, "AD_EJBHOME", "ejb/Ad");
    DOCUMENTBM_EJBHOME = readString(rs, "DOCUMENTBM_EJBHOME", "ejb/DocumentBm");
    BUSREMOTEACCESSBM_EJBHOME = readString(rs, "BUSREMOTEACCESSBM_EJBHOME", "ejb/BusRemoteAccessBm");
    ADMINBM_EJBHOME = readString(rs, "ADMINBM_EJBHOME", "ejb/AdminBm");
    PDCBM_EJBHOME = readString(rs, "PDCBM_EJBHOME", "ejb/PdcBm");
    PLANACTIONSBM_EJBHOME = readString(rs, "PLANACTIONSBM_EJBHOME", "ejb/PlanActionsBm");
    PROJECTMANAGERBM_EJBHOME = readString(rs, "PROJECTMANAGERBM_EJBHOME", "ejb/ProjectManagerBm");
    THESAURUSBM_EJBHOME = readString(rs, "THESAURUSBM_EJBHOME", "ejb/ThesaurusBm");
    AUTHENTICATIONBM_EJBHOME = readString(rs, "AUTHENTICATIONBM_EJBHOME", "ejb/AuthenticationBm");
    MYDBBM_EJBHOME = readString(rs, "MYDBBM_EJBHOME", "ejb/MyDBBm");
    FORMTEMPLATEBM_EJBHOME = readString(rs, "FORMTEMPLATEBM_EJBHOME", "ejb/FormTemplateBm");
    MYLINKSBM_EJBHOME = readString(rs, "MYLINKSBM_EJBHOME", "ejb/MyLinks");
    WEBPUBMANAGER_EJBHOME = readString(rs, "WEBPUBMANAGER_EJBHOME", "ejb/WebPublicationManagerBm");
    BLOGBM_EJBHOME = readString(rs, "BLOGBM_EJBHOME", "ejb/BlogBm");
    TAGCLOUDBM_EJBHOME = readString(rs, "TAGCLOUDBM_EJBHOME", "ejb/TagCloudBm");
    NOTATIONBM_EJBHOME = readString(rs, "NOTATIONBM_EJBHOME", "ejb/NotationBm");
    CLASSIFIEDSBM_EJBHOME = readString(rs, "CLASSIFIEDSBM_EJBHOME", "ejb/ClassifiedsBm");
    SILVERSTATISTICS_JMS_QUEUE = readString(rs, "SILVERSTATISTICS_JMS_QUEUE",
        "com.stratelia.silverpeas.silverstatistics.jms.SilverStatisticsJMSQUEUE");
    SILVERSTATISTICS_JMS_FACTORY = readString(rs, "SILVERSTATISTICS_JMS_FACTORY",
        "com.stratelia.silverpeas.silverstatistics.jms.QueueConnectionFactory");
    SILVERPEAS_DATASOURCE = readString(rs, "SILVERPEAS_DATASOURCE", "jdbc/Silverpeas");
    PUBLICATION_DATASOURCE = readString(rs, "PUBLICATION_DATASOURCE", "jdbc/Silverpeas");
    INTEREST_CENTER_DATASOURCE = readString(rs, "INTEREST_CENTER_DATASOURCE", "jdbc/Silverpeas");
    VERSIONING_DATASOURCE = readString(rs, "VERSIONING_DATASOURCE", "jdbc/Silverpeas");
    CONTACT_DATASOURCE = readString(rs, "CONTACT_DATASOURCE", "jdbc/Silverpeas");
    NODE_DATASOURCE = readString(rs, "NODE_DATASOURCE", "jdbc/Silverpeas");
    FAVORIT_DATASOURCE = readString(rs, "FAVORIT_DATASOURCE", "jdbc/Silverpeas");
    SUBSCRIBE_DATASOURCE = readString(rs, "SUBSCRIBE_DATASOURCE", "jdbc/Silverpeas");
    STATISTIC_DATASOURCE = readString(rs, "STATISTIC_DATASOURCE", "jdbc/Silverpeas");
    CALENDAR_DATASOURCE = readString(rs, "CALENDAR_DATASOURCE", "jdbc/Silverpeas");
    ALMANACH_DATASOURCE = readString(rs, "ALMANACH_DATASOURCE", "jdbc/Silverpeas");
    FORUMS_DATASOURCE = readString(rs, "FORUMS_DATASOURCE", "jdbc/Silverpeas");
    QUESTION_DATASOURCE = readString(rs, "QUESTION_DATASOURCE", "jdbc/Silverpeas");
    ANSWER_DATASOURCE = readString(rs, "ANSWER_DATASOURCE", "jdbc/Silverpeas");
    SURVEY_DATASOURCE = readString(rs, "SURVEY_DATASOURCE", "jdbc/Silverpeas");
    PERSONALIZATION_DATASOURCE = readString(rs, "PERSONALIZATION_DATASOURCE", "jdbc/Silverpeas");
    SCORE_DATASOURCE = readString(rs, "SCORE_DATASOURCE", "jdbc/Silverpeas");
    QUESTIONCONTAINER_DATASOURCE = readString(rs, "QUESTIONCONTAINER_DATASOURCE", "jdbc/Silverpeas");
    BOOKMARK_DATASOURCE = readString(rs, "BOOKMARK_DATASOURCE", "jdbc/Silverpeas");
    WEBSITES_DATASOURCE = readString(rs, "WEBSITES_DATASOURCE", "jdbc/Silverpeas");
    ATTACHMENT_DATASOURCE = readString(rs, "ATTACHMENT_DATASOURCE", "jdbc/Silverpeas");
    TASKM_DATASOURCE = readString(rs, "TASKM_DATASOURCE", "jdbc/Silverpeas");
    FORMDESIGNER_DATASOURCE = readString(rs, "FORMDESIGNER_DATASOURCE", "jdbc/Silverpeas");
    PDC_SUBSCRIPTION_DATASOURCE = readString(rs, "PDC_SUBSCRIPTION_DATASOURCE", "jdbc/Silverpeas");
    PUZZLE_DATASOURCE = readString(rs, "PUZZLE_DATASOURCE", "jdbc/Silverpeas");
    SILVERSTATISTICS_DATASOURCE = readString(rs, "SILVERSTATISTICS_DATASOURCE", "jdbc/Silverpeas");
    CRM_DATASOURCE = readString(rs, "CRM_DATASOURCE", "jdbc/Silverpeas");
    DATAWARNING_DATASOURCE = readString(rs, "DATAWARNING_DATASOURCE", "jdbc/Silverpeas");
    THESAURUS_DATASOURCE = readString(rs, "THESAURUS_DATASOURCE", "jdbc/Silverpeas");
    PDC_BUSIHM_DATASOURCE = readString(rs, "PDC_BUSIHM_DATASOURCE", "jdbc/Silverpeas");
    FILEBOXPLUS_DATASOURCE = readString(rs, "FILEBOXPLUS_DATASOURCE", "jdbc/Silverpeas");
    QUESTIONREPLY_DATASOURCE = readString(rs, "QUESTIONREPLY_DATASOURCE", "jdbc/Silverpeas");
    WHITEPAGES_DATASOURCE = readString(rs, "WHITEPAGES_DATASOURCE", "jdbc/Silverpeas");
    PERSISTENCE_EJB_DATASOURCE = readString(rs, "PERSISTENCE_EJB_DATASOURCE", "jdbc/Silverpeas");
    WEBPUBMANAGER_DATASOURCE = readString(rs, "WEBPUBMANAGER_DATASOURCE", "jdbc/Silverpeas");
    CONTAINERMANAGER_DATASOURCE = readString(rs, "CONTAINERMANAGER_DATASOURCE", "jdbc/Silverpeas");
    CONTENTMANAGER_DATASOURCE = readString(rs, "CONTENTMANAGER_DATASOURCE", "jdbc/Silverpeas");
    CLASSIFYENGINE_DATASOURCE = readString(rs, "CLASSIFYENGINE_DATASOURCE", "jdbc/Silverpeas");
    PDC_DATASOURCE = readString(rs, "PDC_DATASOURCE", "jdbc/Silverpeas");
    ADMIN_DATASOURCE = readString(rs, "ADMIN_DATASOURCE", "jdbc/Silverpeas");
    FORMTEMPLATE_DATASOURCE = readString(rs, "FORMTEMPLATE_DATASOURCE", "jdbc/Silverpeas");
    WORKFLOW_DATASOURCE = readString(rs, "WORKFLOW_DATASOURCE","jdbc/Silverpeas");
    CHAT_DATASOURCE = readString(rs, "CHAT_DATASOURCE", "jdbc/Silverpeas");
    INFOLETTER_DATASOURCE = readString(rs, "INFOLETTER_DATASOURCE", "jdbc/Silverpeas");
    OUTLOOK_DATASOURCE = readString(rs, "OUTLOOK_DATASOURCE", "jdbc/Silverpeas");
    GENERATOR_DATASOURCE = readString(rs, "GENERATOR_DATASOURCE", "jdbc/Silverpeas");
    FORMDESIGNER_DB_DATASOURCE = readString(rs, "FORMDESIGNER_DB_DATASOURCE", "jdbc/Silverpeas");
    DATABASE_DATASOURCE = readString(rs, "DATABASE_DATASOURCE", "jdbc/Silverpeas");
    PERSISTENCE_DB_DATASOURCE = readString(rs, "PERSISTENCE_DB_DATASOURCE", "jdbc/Silverpeas");
    WIKI_DATASOURCE = readString(rs, "WIKI_DATASOURCE", "jdbc/Silverpeas");
    THUMBNAIL_DATASOURCE = readString(rs, "THUMBNAIL_DATASOURCE", "jdbc/Silverpeas");
    DIRECT_DATASOURCE = readString(rs, "DIRECT_DATASOURCE", "serial://datasources/Silverpeas");
    MAIL_SESSION = readString(rs, "MAIL_SESSION", "java:comp/env/MailSession");
    USE_CATALOG_EJB = readString(rs, "USE_CATALOG_EJB", "java:comp/env/useCatalogEJB");
    SECURITY_ADAPTER_CLASSNAME = readString(rs, "SECURITY_ADAPTER_CLASSNAME",
        "java:comp/env/securityAdapterClassName");
    SEND_CONFIRMATION_MAIL = readString(rs, "SEND_CONFIRMATION_MAIL", "java:comp/env/sendConfirmationMail");
    JMS_FACTORY = readString(rs, "JMS_FACTORY",
        "com.stratelia.silverpeas.notificationserver.jms.QueueConnectionFactory");
    JMS_QUEUE = readString(rs, "JMS_QUEUE", "com.stratelia.silverpeas.notificationserver.jms.Queue");
    JMS_HEADER_CHANNEL = readString(rs, "JMS_HEADER_CHANNEL", "CHANNEL");
  }
}