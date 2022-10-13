import java.lang.Thread;
import java.io. * ;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security. * ;
import javax.net.ssl.SSLContext;
import java.io.FileWriter;
import java.io.BufferedWriter;
import com.cyberark.conjur.api.Conjur;  //Java dili için Conjur tarafından sunulan library import
import com.cyberark.conjur.api.Token;   //Java dili için Conjur tarafından sunulan library import

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class testapp {
    public static void main(String[]args) {
        while (true) {
            try {
                Thread.sleep(5000);

                //##############Conjur sertifikasının trust edilmesi ve bağlantı sırasında kullanılması
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
                //##############Conjur sertifikasının trust edilmesi ve bağlantı sırasında kullanılması

                //CHECK EXAMPLE SECRET
                Token token = Token.fromFile(Paths.get("/var/run/conjur/access-token"));
                Conjur conjur = new Conjur(token, conjurSSLContext); //Daha önceki bölümde yaratılan SSL context bir conjur connection açılması
                String secret = conjur.variables().retrieveSecret(System.getenv("CONJUR_SECRET_DB_PASSWORD")); //Örnek bir secret retrive edilmesi
                System.out.println("SECRET RESULT: " + secret);
                //CHECK EXAMPLE SECRET

                //POSTGRES CONNECTION
                final String url = "jdbc:postgresql://" + System.getenv("CONJUR_SECRET_DB_HOSTNAME") + "/" + System.getenv("CONJUR_SECRET_DB_NAME");
                final String user = System.getenv("CONJUR_SECRET_DB_USERNAME");
                final String password = secret;

                try {
                    Connection conn = null;
                    Class.forName("org.postgresql.Driver");
                    conn = DriverManager.getConnection(url, user, password);
                    System.out.println("Connected to the PostgreSQL server successfully.");
                } catch (SQLException e) {
                    System.out.println("FAILED: Postgres Connection Failed:");
                    System.out.println(e.getMessage());
                }
				//psql -d sampledb
				//CREATE ROLE myuser LOGIN PASSWORD 'mypass';
                //POSTGRES CONNECTION

            } catch (Exception expn) {
                // catching the exception
                System.out.println(expn);
            }
        }
    }
}
