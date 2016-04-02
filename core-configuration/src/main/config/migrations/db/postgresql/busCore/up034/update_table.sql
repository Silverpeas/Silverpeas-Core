UPDATE st_domain
SET propfilename = replace(replace(replace(propfilename,
                                           'com.stratelia.webactiv',
                                           'org.silverpeas'
                                   ),
                                   'com.stratelia.silverpeas',
                                   'org.silverpeas'
                           ),
                           'com.silverpeas',
                           'org.silverpeas'
);

DROP TABLE favorit;