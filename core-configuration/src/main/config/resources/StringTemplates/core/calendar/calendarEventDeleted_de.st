$if(several)$
$senderName$ hat allen Veranstaltungen <b>$contributionName$</b>$if(contributionStartDate)$ ab dem <i>$contributionDate$</i> gelöscht$else$ gelöscht$endif$
$else$
$senderName$ hat die Veranstaltung <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ vom <i>$contributionStartDate$</i> bis zum <i>$contributionEndDate$</i> gelöscht$else$ für den <i>$contributionStartDate$</i> gelöscht$endif$$else$ gelöscht$endif$
$endif$