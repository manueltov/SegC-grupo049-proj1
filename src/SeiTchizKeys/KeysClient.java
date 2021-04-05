package SeiTchizKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.crypto.spec.SecretKeySpec;

public class KeysClient extends Keys{
	private static String seperator = File.separator;
	
	public KeysClient(String username, String keyStorePath, String keyStorePass) {
		super(keyStorePath,keyStorePass,getCertificatePath(username));
		
	}
	
	private static String getCertificatePath(String username) {
	
		return "src"+File.separator+"Client"+File.separator+"PubKeys"+seperator+username+seperator+username+"Public.cer";
	}
	
	public String cipherKey(byte[] toCipher, String key) {
		return new String(cipher(toCipher,new SecretKeySpec(key.getBytes(),"AES")));
	}
	public String decipherKey(byte[] toDeCipher, String key) {
		return new String(decipher(toDeCipher,new SecretKeySpec(key.getBytes(),"AES")));
	}
	
	public static Key getPublicKey(String userID) throws java.security.cert.CertificateException, FileNotFoundException {
		
		FileInputStream fi = new FileInputStream(getCertificatePath(userID));
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		return cf.generateCertificate(fi).getPublicKey();
		
	}
	
	public static byte[] getSimetricKey() {
		SecureRandom key = new SecureRandom();
		byte[] keyB = new byte[16];
		key.nextBytes(keyB);

		return keyB;
	}
}