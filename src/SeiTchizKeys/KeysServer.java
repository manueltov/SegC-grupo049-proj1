package SeiTchizKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class KeysServer extends Keys {
	//caminho
	
	private static final String CERTIFICATE ="src"+File.separator+"Server"+File.separator+"certServer.cer";
	private static final String USER_CERTIFICATE = "src"+File.separator+"Server"+File.separator+"UserKeys"+File.separator;
	
	public KeysServer(String keyStore, String keyStorePass) {
		//vai ter o certificado
		super(keyStore,keyStorePass, CERTIFICATE);
		
	}
	public static String getCertPath() {
		return CERTIFICATE;
	}
	public static String getUserCertificatePath(String userID) {
		
		return USER_CERTIFICATE+userID+"Public.cer";
		
	}
	
	public static void saveUserCertificate(String user, Certificate cert) {
		
		File cFile = new File( user + "Public.cer");
		try (FileOutputStream fileOut = new FileOutputStream(cFile)) {
			cFile.createNewFile();
			fileOut.write(cert.getEncoded());
		} catch (CertificateException|IOException e) {
			e.printStackTrace();
		}
	}
	public static Certificate getCertificate(String filePath) throws CertificateException {
		String filePath1=getUserCertificatePath(filePath);
		File certificado = new File(filePath1);
		if (!certificado.isFile()) {
			return null;
		}
		try (FileInputStream in = new FileInputStream(certificado)) {	
			CertificateFactory cFile = CertificateFactory.getInstance("X509");
			return cFile.generateCertificate(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Certificate getUserCertificate(String user) {
		
		try (FileInputStream in = new FileInputStream(getUserCertificatePath(user))) {
			CertificateFactory cFile = CertificateFactory.getInstance("X509");
			return cFile.generateCertificate(in);
		} catch (CertificateException|IOException e) {
			return null;
		}
	}
	
	
}
