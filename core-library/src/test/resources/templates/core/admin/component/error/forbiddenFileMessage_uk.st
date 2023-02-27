Файл <b>$forbiddenFileName$</b> заборонено.
<br/>Тільки файли <b>$fileFilters$</b> $if(isAuthorized)$дозволено$else$заборонено$endif$.
<br/>Цей файловий фільтр встановлено $if(isGloballySet)$глобально на рівні сервера$else$на рівні налаштувань програми$endif$.
$if(fromComponentUrl)$<br/> Повернутися на головну сторінку програми <a href="$fromComponentUrl$">$fromComponentName$</a> .$endif$
