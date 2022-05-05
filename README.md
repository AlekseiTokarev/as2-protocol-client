`curl -i -X POST -H "Content-Disposition: attachment; filename=\"smime.p7m\"" \
-H "Content-Type: application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"" \
-H "Content-Transfer-Encoding: base64" \
-H "AS2-TO: MyCompany_OID" \
-H "AS2-FROM: PartnerA_OID" \
-H "AS2-VERSION: 1.2" \
-H "MESSAGE-ID: <openssl%NOW%@LOCALHOST>" \
-H "Disposition-Notification-To: response@localhost" \
-H "DISPOSITION-NOTIFICATION-OPTIONS: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=optional, sha1" \
--data-binary @request_ENC.msg http://localhost:10080/as21`
