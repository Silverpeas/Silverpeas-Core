$if(several)$
$senderName$ has deleted all the events <b>$contributionName$</b>$if(contributionStartDate)$ from <i>$contributionStartDate$</i>$endif$
$else$
$senderName$ has deleted the event <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ from <i>$contributionStartDate$</i> to <i>$contributionEndDate$</i>$else$ for <i>$contributionStartDate$</i>$endif$$endif$
$endif$