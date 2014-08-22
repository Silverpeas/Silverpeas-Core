function bytesToSize(bytes, precision) {
  var sizes = [ jQuery.i18n.prop("file.size.bytes"),
      jQuery.i18n.prop("file.size.Kb"), jQuery.i18n.prop("file.size.Mb"),
      jQuery.i18n.prop("file.size.Gb"), jQuery.i18n.prop("file.size.Tb") ];
  var posttxt = 0;
  if (bytes == 0)
    return 'n/a';
  while (bytes >= 1024) {
    posttxt++;
    bytes = bytes / 1024;
  }
  return bytes.toFixed(precision) + " " + sizes[posttxt];
}