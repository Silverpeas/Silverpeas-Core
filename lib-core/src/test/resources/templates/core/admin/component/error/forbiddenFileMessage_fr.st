Le fichier <i><b>$forbiddenFileName$</b></i> est interdit.
<br/>Seuls les fichiers <i><b>$fileFilters$</b></i> sont $if(isAuthorized)$autorisés$else$interdits$endif$.
<br/>Ce filtre de fichiers est défini $if(isGloballySet)$globalement au niveau du serveur$else$au niveau des paramètres de l'application$endif$.
$if(fromComponentUrl)$<br/>Revenir à la page d'accueil de l'application <a href="$fromComponentUrl$">$fromComponentName$</a>.$endif$