package SeiTchizKeys;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public  class Keys {

	private KeyStoreAux keys=null;
	
	public Keys(String keyStore, String keyStorePass, String cert) {
		this.keys = new KeyStoreAux(keyStore,keyStorePass);
	}
	
	public Certificate getCertificate() {
		return keys.getCertificate();
	}
	
	//D to decipher ; c to cipher
	public static  byte[] decipher(byte[] bytesTo, Key publicKey) {
		return convertPublic(bytesTo,publicKey,Cipher.DECRYPT_MODE);
	}
	public byte[] decripherPublic(byte[] bytesTo) {
		Key publicKey = keys.getPublicKey();
		return  convertPublic(bytesTo,publicKey,Cipher.DECRYPT_MODE);
	}
	
	public byte[] decipherPrivate(byte[] bytesTo) {
		Key privKey = keys.getPrivateKey();
		return convertPrivate(bytesTo,privKey,Cipher.DECRYPT_MODE);
	}
	public static byte[] cipher(byte[] bytesTo, Key key) {
		return  convertPublic(bytesTo,key,Cipher.ENCRYPT_MODE);
	}
	
	public byte[] cripherPublic(byte[] bytesTo) {
		Key pubKey = keys.getPublicKey();
		return  convertPublic(bytesTo,pubKey,Cipher.ENCRYPT_MODE);
	}
	
	public byte[] cripherPrivate(byte[] bytesTo) {
		Key privKey = keys.getPrivateKey();
		return convertPrivate(bytesTo,privKey,Cipher.ENCRYPT_MODE);
	}
	
	//privates
	private static byte[] convertPrivate(byte[] bytesTo, Key privKey, int decrypt) {
		
		try {
			Cipher c = Cipher.getInstance(privKey.getAlgorithm());
			c.init(decrypt,privKey);
			return c.doFinal(bytesTo);
		} catch (NoSuchAlgorithmException|NoSuchPaddingException|InvalidKeyException|IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	//publics
	private static byte[] convertPublic(byte[] bytesTo, Key key, int decryptMode) {
		try {
			Cipher c = Cipher.getInstance(key.getAlgorithm());
			c.init(decryptMode,key);
			return c.doFinal(bytesTo);
		} catch (NoSuchAlgorithmException|NoSuchPaddingException|InvalidKeyException|IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

}
