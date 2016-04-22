
sql.eachRow('select id, propFilename from st_domain') { row ->
  String fixedPropFilename = row.propFilename.replaceAll(/^.*\.domains/, 'org.silverpeas.domains')
  sql.executeUpdate("update st_domain set propFilename=${fixedPropFilename} where id=${row.id}")
}
