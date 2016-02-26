<div class="profilPhoto">
	<a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
	$if(user.connected)$
			$if(notMyself)$
				<a href="#" title="Sprechen Sie mit $user.firstName$" onclick="javascript:window.open('$context$/RcommunicationUser/jsp/OpenDiscussion?userId=$user.id$','popupDiscussion$user.id$','menubar=no, status=no, scrollbars=no, menubar=no, width=600, height=450')">
					<img src="$context$/util/icons/online.gif" alt="Sprechen Sie mit $user.firstName$" title="Sprechen Sie mit $user.firstName$"/>
				</a>
			$else$
				<img src="$context$/util/icons/online.gif" alt="connected"/>
			$endif$
			Verbunden seit $user.durationOfCurrentSession$
		$else$
			<img src="$context$/util/icons/offline.gif" alt="deconnected"/> Offline
        $endif$
     </li>
     $if(status)$
	<li class="status">
	  "$status$"
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
	  Telefon : $phone$
	$endif$
     </li>
   </ul>
 </div>
<div class="action">
  $if(notMyself)$
	$if(notAContact)$
	  <a href="#" rel="$user.id$,$user.lastName$ $user.firstName$" class="link invitation">Schicken Sie eine Einladung</a>
	$endif$
	<a href="#" rel="$user.id$,$user.lastName$ $user.firstName$" class="link notification">Senden Sie eine Nachricht</a>
  $endif$
</div>