<p>
Silverpeas utiliza un <strong>DataStore</strong> de JCR para administrar los archivos agregados en la plataforma.<br/>
Al combinar el uso de una base de datos y espacio en disco, se encarga de aumentar
el rendimiento del procesamiento de big data.<br/>
Una de las peculiaridades en su gestión es la de no borrar nunca los datos, incluso cuando estos últimos ya no sean útiles.<br/>
Como resultado, el espacio en disco ocupado por el DataStore aumenta constantemente a medida que se utiliza el JCR, en particular con
$if(wbeEnabled)$
Funciones de edición en línea y edición en el navegador.
$else$
funcionalidad de edición en línea.
$endif$
<br/>Para superar esto, se ha implementado un procesamiento de depuración de datos que se han vuelto inútiles.
Se inicia automáticamente cuando ya no se solicita la plataforma.<br/>
Pero en algunos contextos donde la plataforma se usa sin una interrupción significativa, esta última nunca se inicia.<br/>
Entonces es posible iniciar manualmente una purga desde el menú que ofrece esta pantalla.
</p>
<p>
La ruta del <strong>almacén</strong> de datos en el disco es:
<span class="path">$datastore_path$</span> .<br/>
</p>
