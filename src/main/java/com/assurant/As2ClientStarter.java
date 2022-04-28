package com.assurant;

import com.helger.as2lib.client.AS2Client;
import com.helger.as2lib.client.AS2ClientRequest;
import com.helger.as2lib.client.AS2ClientResponse;
import com.helger.as2lib.client.AS2ClientSettings;
import com.helger.as2lib.crypto.ECompressionType;
import com.helger.as2lib.crypto.ECryptoAlgorithmCrypt;
import com.helger.as2lib.crypto.ECryptoAlgorithmSign;
import com.helger.as2lib.disposition.DispositionOptions;
import com.helger.commons.mime.CMimeType;
import com.helger.mail.datasource.ByteArrayDataSource;
import com.helger.security.keystore.EKeyStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class As2ClientStarter {
    private final static Logger LOGGER = LoggerFactory.getLogger(As2ClientStarter.class);

    public static void main(String[] args) {
        As2ClientStarter starter = new As2ClientStarter();
        starter.sendAs2Request();
    }

    private void sendAs2Request() {
        AS2ClientSettings settings = getAs2ClientSettings();

        // Build client request
        final AS2ClientRequest aRequest = new AS2ClientRequest("AS2 test message from as2-lib");
        aRequest.setData(new DataHandler(new ByteArrayDataSource(getBytes("request.xml"), null, null)));
        aRequest.setContentType(CMimeType.APPLICATION_XML.getAsString());

        // Send message
        final AS2ClientResponse response = new AS2Client().sendSynchronous(settings, aRequest);
        if (response.hasException()) {
            LOGGER.error(response.getAsString());
        }
        LOGGER.info("Done");
    }

    private AS2ClientSettings getAs2ClientSettings() {
        AS2ClientSettings settings = new AS2ClientSettings();
        settings.setKeyStore(EKeyStoreType.PKCS12, getBytes("openAs2_certs.p12"), "testas2");

        settings.setSenderData("PartnerA_OID", "as2msgs@partnera.com", "partnera");
        settings.setReceiverData("MyCompany_OID", "mycompany", "http://localhost:10080/HttpReceiverr");

        settings.setPartnershipName(settings.getSenderAS2ID() + "_" + settings.getReceiverAS2ID());

        final ECryptoAlgorithmSign eSignAlgo = ECryptoAlgorithmSign.DIGEST_SHA_256;

        final ECryptoAlgorithmCrypt eCryptAlgo = ECryptoAlgorithmCrypt.CRYPT_3DES;

        final ECompressionType eCompress = ECompressionType.ZLIB;
        final boolean bCompressBeforeSigning = true;

        DispositionOptions mdnOptions = new DispositionOptions()
                .setMICAlg(eSignAlgo)
                .setMICAlgImportance(DispositionOptions.IMPORTANCE_REQUIRED)
                .setProtocol(DispositionOptions.PROTOCOL_PKCS7_SIGNATURE)
                .setProtocolImportance(DispositionOptions.IMPORTANCE_REQUIRED);
        settings.setMDNOptions(mdnOptions);

        settings.setEncryptAndSign(eCryptAlgo, eSignAlgo);
        settings.setCompress(eCompress, bCompressBeforeSigning);

        settings.setEncryptAndSign(eCryptAlgo, eSignAlgo);
        settings.setCompress(eCompress, bCompressBeforeSigning);
        settings.setMessageIDFormat("github-phax-as2-lib-$date.uuuuMMdd-HHmmssZ$-$rand.1234$@$msg.sender.as2_id$_$msg.receiver.as2_id$");
        settings.setRetryCount(1);
        settings.setConnectTimeoutMS(10_000);
        settings.setReadTimeoutMS(10_000);
        return settings;
    }

    private byte[] getBytes(String path) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return getBytesFromInputStream(classLoader.getResourceAsStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
}