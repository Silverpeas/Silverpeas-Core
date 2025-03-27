<div class="profilPhoto">
	<a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
	$if(user.connected)$
			$if(notMyself)$
				$if(chatEnabled)$
				  <a href="#" title="Chat with $user.firstName$" onclick="javascript:SilverChat.gui.openChatWindow('$user.chatId$', '$user.displayedName$')">
				  <img src="$context$/util/icons/online.gif" alt="Chat with $user.firstName$" title="Chat with $user.firstName$"/>
				  </a>
				$else$
				  <img src="$context$/util/icons/online.gif" alt="connected"/>
				$endif$
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
     $if(mail)$
     <li class="userMail">
        <a href="mailto:$mail$">$mail$</a>
     </li>
     $endif$
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
      <a href="#" rel="$invitationReceived$" class="link view-invitation">View invitation</a>
    $elseif(aContact)$
      <a href="#" rel="$user.id$,$user.firstName$ $user.lastName$,deleteRelationCallback" class="link delete-relation">Remove from my contacts</a>
    $else$
      <a href="#" rel="$user.id$,$user.firstName$ $user.lastName$" class="link invitation">Send an invitation</a>
    $endif$

    $if(chatEnabled)$
      <a href="#" class="link notification" title="Chat with $user.firstName$" onclick="javascript:SilverChat.gui.openChatWindow('$user.chatId$', '$user.displayedName$')">
        Chat with $user.firstName$
      </a>
    $else$
    	<a href="#" onclick="sp.messager.open(null, {recipientUsers: $user.id$, recipientEdition: false});" class="link notification">Send a message</a>
    $endif$

  $endif$
</div>