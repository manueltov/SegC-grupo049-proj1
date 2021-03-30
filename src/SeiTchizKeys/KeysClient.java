package SeiTchizKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Key;
import java.security.cert.CertificateFactory;

import javax.crypto.spec.SecretKeySpec;

public class KeysClient extends Keys{
	private static String seperator = File.separator;
	
	public KeysClient(String username, String keyStorePath, String keyStorePass) {
		super(keyStorePath,keyStorePass,getCertificatePath(username));
		
	}
	
	private static String getCertificatePath(String username) {
	
		return "src"+File.separator+"Client"+"PubKeys"+seperator+username+seperator+username+"Public.cer";
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
}