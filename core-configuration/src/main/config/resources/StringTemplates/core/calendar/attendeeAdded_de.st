$if(several)$
$senderName$ bittet um Ihre Teilnahme an allen Veranstaltungen <b>$contributionName$</b>$if(contributionStartDate)$ ab dem <i>$contributionDate$</i>$endif$
$else$
$senderName$ bittet um Ihre Teilnahme an die Veranstaltung <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ vom <i>$contributionStartDate$</i> bis zum <i>$contributionEndDate$</i>$else$ für den <i>$contributionStartDate$</i>$endif$$endif$
$endif$