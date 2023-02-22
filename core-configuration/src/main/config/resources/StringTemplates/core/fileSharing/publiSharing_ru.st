<b>$senderUser.firstName$ $senderUser.lastName$</b> поделился публикацией <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Внимание, эта публикация доступна до $ticket.endDate; format="dd/MM/yyyy"$.
$endif$


$if(additionalMessage)$
Сообщение :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
