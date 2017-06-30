$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Hello  $userDetail.firstName$ $userDetail.lastName$,<br/>
Your account has been successfully $if(createdUser)$created$else$modified$endif$.<br/>
You will find below details of your connection:
<ul>
<li>Login : $userDetail.login$</li>
<li>Password : $pwd$</li>
<li>Connection : <a href="$loginURL$">Follow this link...</a></li>
</ul>
$if(extraMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$extraMessage$</div>
$endif$