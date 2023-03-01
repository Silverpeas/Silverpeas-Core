$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Здравствуйте $userDetail.firstName$ $userDetail.lastName$,<br/>
Ваша учетная запись была $if(createdUser)$создана$else$изменена$endif$ на платформе Silverpeas.<br/>
Ваши настройки подключения следующие:
<ul>
<li>Идентификатор: $userDetail.login$</li>
<li>Пароль: $pwd$</li>
<li>Соединение: <a href="$loginURL$">ссылка доступа</a></li>
</ul>
$if(extraMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$extraMessage$</div>
$endif$
