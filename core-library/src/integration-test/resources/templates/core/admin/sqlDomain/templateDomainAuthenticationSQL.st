# Silverpeas default driver authentication
# ----------------------------------------

# Fallback type : could be one of the following values : none, ifNotRejected, always
fallbackType         = none
allowPasswordChange  = $allowPasswordChange$

# Authentication servers
# Available types are :
#       org.silverpeas.core.security.authentication.AuthenticationCAS
#       org.silverpeas.core.security.authentication.AuthenticationSQL
#       org.silverpeas.core.security.authentication.AuthenticationLDAP
autServersCount = 1
autServer0.type    								= org.silverpeas.core.security.authentication.AuthenticationSQL
autServer0.enabled 								= true
autServer0.SQLDataSourceJNDIName			= $SQLDataSourceJNDIName$
autServer0.SQLUserTableName 					= $SQLUserTableName$
autServer0.SQLUserLoginColumnName 				= login
autServer0.SQLUserPasswordColumnName 			= password
autServer0.SQLUserPasswordAvailableColumnName 	= passwordValid
