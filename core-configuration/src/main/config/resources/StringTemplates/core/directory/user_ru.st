<div class="profilPhoto">
<a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$avatar$</a>
</div>
<div class="info">
<ul>
<li class="userName"><a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
<li class="infoConnection">
$if(user.connected)$
$if(notMyself)$
$if(chatEnabled)$
<a href="#" title="Discuter avec $user.firstName$" onclick="javascript:SilverChat.gui.openChatWindow('$user.chatId$', '$user.displayedName$')">
<img src="$context$/util/icons/online.gif" alt="Discuter avec $user.firstName$" title="Общайтесь с $user.firstName$"/>
</a>
$else$
<img src="$context$/util/icons/online.gif" alt="связанный"/>
$endif$
$else$
<img src="$context$/util/icons/online.gif" alt="связанный"/>
$endif$
Подключено с $user.durationOfCurrentSession$
$else$
<img src="$context$/util/icons/offline.gif" alt="отключен"/>Не в сети
$endif$
</li>
$if(status)$
<li class="status">
&quot;$status$&quot;
</li>
$endif$
<li class="userType">
$type$
</li>
<li class="userMail">
<a href="#" onclick="OpenPopup($user.id$,'$user.lastName$ $user.firstName$')">$mail$</a>
</li>
<li class="extra">
$if(extra.title)$
$extra.title$<br/>
$endif$
$if(phone)$
Телефон: $phone$
$endif$
</li>
</ul>
</div>
<div class="action actionShownOnMouseOver">
$if(notMyself)$
$if(invitationSent)$
<a href="#" rel="$invitationSent$" class="link cancel-invitation">Отменить приглашение</a>
$elseif(invitationReceived)$
<a href="#" rel="$invitationReceived$" class="link view-invitation">Посмотреть приглашение</a>
$elseif(aContact)$
<a href="#" rel="$user.id$,$user.firstName$ $user.lastName$,deleteRelationCallback" class="link delete-relation">Удалить из моих контактов</a>
$else$
<a href="#" rel="$user.id$,$user.firstName$ $user.lastName$" class="link invitation">Отправить приглашение</a>
$endif$
<a href="#" onclick="sp.messager.open(null, {recipientUsers: $user.id$, recipientEdition: false});" class="link notification">Отправить сообщение</a>
$endif$
</div>
