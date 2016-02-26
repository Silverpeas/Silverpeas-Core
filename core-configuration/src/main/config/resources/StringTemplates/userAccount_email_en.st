$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Hello  $userDetail.firstName$ $userDetail.lastName$,<br/>
Your account has been successfully $if(createdUser)$created$else$modified$endif$.<br/>
You will find below the details of your connection:
login : $userDetail.login$
password : $pwd$
Connection URL : <a href="$loginURL$">Follow this link...</a>