package SeiTchizKeys;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public class KeyStoreAux {
	
	private char[] store_pass;
	private Certificate certificate;
	private KeyStore ks;
	public KeyStoreAux(String keyStorePath, String StorePassword) {
		
		store_pass = StorePassword.toCharArray();
		
		//obter certificado keystore
		try {
		     FileInputStream store = new FileInputStream(keyStorePath);
			//fornecido pelo JCE; armazena tambem chaves secretas
		     ks = KeyStore.getInstance("JKS");
		     ks.load(store,store_pass);
		     certificate = ks.getCertificate("keyStore");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Certificate getCertificate() {
		return certificate;
	}
	
	public PrivateKey getPrivateKey() {
		try {
			//get key com a pass
			return (PrivateKey) ks.getKey("keyStore",store_pass);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public PublicKey getPublicKey() {
		return certificate.getPublicKey();
	}
}


