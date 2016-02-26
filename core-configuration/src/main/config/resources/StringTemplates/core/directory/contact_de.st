<div class="profilPhoto">
	<a href="javascript:SP_openWindow('$context$$url$')">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="javascript:SP_openWindow('$context$$url$')">$contact.lastName$ $contact.firstName$</a></li>
     <li class="userMail">
	 <a href="mailto:$mail$">$mail$</a>
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
$if(mail)$<div class="action">
  <a href="mailto:$mail$" class="link notification">Send an email</a>
</div>$endif$