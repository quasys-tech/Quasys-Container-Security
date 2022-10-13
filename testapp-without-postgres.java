import java.lang.Thread;
import java.io. * ;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security. * ;
import javax.net.ssl.SSLContext;
import java.io.FileWriter;
import java.io.BufferedWriter;
import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Token;


public class testapp {
    public static void main(String[]args) {
        while (true) {
            try {
                Thread.sleep(5000);

                //TRUST CERT
                final String conjurTlsCaPath = "/var/www/java/conjur.pem";
                BufferedWriter writer = new BufferedWriter(new FileWriter(conjurTlsCaPath));
                writer.write(System.getenv("CONJUR_SSL_CERTIFICATE"));
                writer.close();

                final java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                final FileInputStream certIs = new FileInputStream(conjurTlsCaPath);
                final java.security.cert.Certificate cert = cf.generateCertificate(certIs);

                final KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(null);
                ks.setCertificateEntry("conjurTlsCaPath", cert);

                final javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                SSLContext conjurSSLContext = SSLContext.getInstance("TLS");
                conjurSSLContext.init(null, tmf.getTrustManagers(), null);
                //TRUST CERT

                //CHECK EXAMPLE SECRET
                Token token = Token.fromFile(Paths.get("/var/run/conjur/access-token"));
                Conjur conjur = new Conjur(token, conjurSSLContext);
                String secret = conjur.variables().retrieveSecret(System.getenv("CONJUR_SECRET_DB_PASSWORD"));
                System.out.println("SECRET RESULT: " + secret);
                //CHECK EXAMPLE SECRET

            } catch (Exception expn) {
                // catching the exception
                System.out.println(expn);
            }
        }
    }
}
