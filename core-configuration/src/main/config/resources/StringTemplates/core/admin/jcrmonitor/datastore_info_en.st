<p>
Silverpeas uses a JCR <strong>DataStore</strong> to manage the files added into the platform. <br/>
By combining the use of a database and a disk space, it is in charge of increasing the performance of large data processing.<br/>
One of the peculiarities in its management is to never delete data, even when the latter are no longer useful.<br/>
As a result, the disk space occupied by the DataStore increases continuously as the JCR is used, especially with
$if(wbeEnabled)$
the inline editing and in-browser editing features.
$else$
the inline editing feature.
$endif$
<br/>To overcome this, a purge processing of data that has become useless has been implemented.
It launches automatically when the platform is no longer requested.<br/>
But in some contexts where the platform is used continuously, the latter never launches.<br/>
It is then possible to manually launch a purge from the menu of this screen.
</p>
<p>
The <strong>DataStore</strong> path on disk is:
<span class="path">$datastore_path$</span>.<br/>
</p>