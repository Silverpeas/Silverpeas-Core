UPDATE st_domain
SET className = replace(replace(replace(className,
                                        'com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver',
                                        'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver'
                                ),
                                'com.stratelia.silverpeas.domains.ldapdriver.LDAPDriver',
                                'org.silverpeas.core.admin.domain.driver.ldapdriver.LDAPDriver'
                        ),
                        'com.stratelia.silverpeas.domains.sqldriver.SQLDriver',
                        'org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver'
);