<div class="profilPhoto">
	<a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
	$if(user.connected)$
			$if(notMyself)$
				<a href="#" title="Talk with $user.firstName$" onclick="javascript:window.open('$context$/RcommunicationUser/jsp/OpenDiscussion?userId=$user.id$','popupDiscussion$user.id$','menubar=no, status=no, scrollbars=no, menubar=no, width=600, height=450')">
				<img src="$context$/util/icons/online.gif" alt="Talk with $user.firstName$" title="Talk with $user.firstName$"/>
				</a>
			$else$
				<img src="$context$/util/icons/online.gif" alt="connected"/>
			$endif$
			Online for $user.durationOfCurrentSession$
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
	  Phone : $phone$
	$endif$
     </li>
   </ul>
 </div>
<div class="action actionShownOnMouseOver">
  $if(notMyself)$
    $if(invitationSent)$
      <a href="#" rel="$invitationSent$" class="link cancel-invitation">Cancel invitation</a>
    $elseif(invitationReceived)$
      <a href="$context$/RMyProfil/jsp/MyInvitations" class="link view-invitation">View invitation</a>
    $elseif(aContact)$
      <a href="#" rel="$user.id$,$user.firstName$ $user.lastName$" class="link delete-relation">Remove from my contacts</a>
    $else$
      <a href="#" rel="$user.id$,$user.firstName$ $user.lastName$" class="link invitation">Send an invitation</a>
    $endif$
  	<a href="#" rel="$user.id$,$user.lastName$ $user.firstName$" class="link notification">Send a message</a>
  $endif$
</div>