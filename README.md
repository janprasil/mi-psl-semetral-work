Generate self-signed trusted certificate and import it into JKS
====

keytool -genkeypair -v
  -alias localhost
  -dname "CN=localhost"
  -keystore play-webapp.jks
  -keypass changeme
  -storepass changeme 
  -keyalg RSA 
  -keysize 4096 
  -ext KeyUsage:critical="keyCertSign" 
  -ext BasicConstraints:critical="ca:true" 
  -validity 365
  
Export the public certificate into certificate.crt file 
====

keytool -export -v 
  -alias localhost
  -file certificate.crt 
  -keypass changeme
  -storepass changeme
  -keystore play-webapp.jks 
  -rfc

Run Play application
====
  
sbt run
  
Run Play application with HTTPS support
====
  
sbt run -Dhttps.port=9443 -Dplay.server.https.keyStore.path=conf/play-webapp.jks -Dplay.server.https.keyStore.password=changeme


