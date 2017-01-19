function bytesToSize(bytes, precision) {
  var sizes = [ window.i18n.prop("file.size.bytes"),
      window.i18n.prop("file.size.Kb"), window.i18n.prop("file.size.Mb"),
      window.i18n.prop("file.size.Gb"), window.i18n.prop("file.size.Tb") ];
  var posttxt = 0;
  if (bytes == 0)
    return 'n/a';
  while (bytes >= 1024) {
    posttxt++;
    bytes = bytes / 1024;
  }
  return bytes.toFixed(precision) + " " + sizes[posttxt];
}