<b>$senderUser.firstName$ $senderUser.lastName$</b> общая папка <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Внимание, содержимое файла доступно до $ticket.endDate; format="dd/MM/yyyy"$.
$endif$


$if(additionalMessage)$
Сообщение :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
