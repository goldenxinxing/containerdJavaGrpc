# java tls
1、一步生成keystore：https://docs.spring.io/spring-cloud-skipper/docs/1.0.0.BUILD-SNAPSHOT/reference/html/configuration-security-enabling-https.html
keytool -genkey -alias skipper -keyalg RSA -keystore skipper.keystore \
-validity 3650 -storetype JKS \
-dname "CN=localhost, OU=Spring, O=Pivotal, L=Holualoa, ST=HI, C=US"  1
-keypass skipper -storepass skipper
keytool -genkey -alias starwhale -keyalg RSA -keystore gxx.p12 \
-validity 3650 -storetype PKCS12 \
-dname "CN=localhost, OU=Spring, O=Pivotal, L=Holualoa, ST=HI, C=US" \
-keypass starwhale -storepass starwhale
2、同时支持http和https：https://www.thomasvitale.com/https-spring-boot-ssl-certificate/