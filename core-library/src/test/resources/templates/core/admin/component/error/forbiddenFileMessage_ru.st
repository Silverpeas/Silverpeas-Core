Файл <b>$forbiddenFileName$</b> запрещенный.
<br/>Только файлы <b>$fileFilters$</b> $if(isAuthorized)$разрешены$else$запрещены$endif$.
<br/>Этот файловый фильтр устанавливается $if(isGloballySet)$глобально на уровне сервера$else$на уровне настроек приложения$endif$.
$if(fromComponentUrl)$<br/> Вернуться на главную страницу приложения <a href="$fromComponentUrl$">$fromComponentName$</a> .$endif$
