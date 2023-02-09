El archivo <b>$forbiddenFileName$</b> está prohibido.
<br/>Solo archivos <b>$fileFilters$</b> están $if(isAuthorized)$autorizados$else$prohibidos$endif$.
<br/>Este filtro de archivos se establece $if(isGloballySet)$globalmente en el nivel del servidor$else$en el nivel de configuración de la aplicación$endif$.
$if(fromComponentUrl)$<br/> Volver a la página de inicio de la aplicación <a href="$fromComponentUrl$">$fromComponentName$</a> .$endif$
