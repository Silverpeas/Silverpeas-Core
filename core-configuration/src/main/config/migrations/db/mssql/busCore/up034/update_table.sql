UPDATE st_domain
SET className = replace(className,
    'com.silverpeas.domains.silverpeasdriver.SilverpeasDomainDriver',
    'org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver');