
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {
    private static final String FROM_EMAIL = "recepty@cambalik.eu";
    private static final String USERNAME = "recepty@cambalik.eu";
    private static final String PASSWORD = "LyzMcP3ijDQcpWr";
    private static final String HOST;
    private static final int PORT;
    private static final boolean USE_TLS;
    private static final boolean USE_SSL;

    public static void sendEmail(String toEmail, String subject, String htmlBody) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        if (USE_TLS) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        if (USE_SSL) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.localhost", "receptar-app");
        props.put("mail.smtp.ehlo", "true");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("recepty@cambalik.eu", "LyzMcP3ijDQcpWr");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("recepty@cambalik.eu"));
            message.setRecipients(RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("Email úspešne odoslaný na: " + toEmail + " (z recepty@cambalik.eu)");
        } catch (Exception var6) {
            System.err.println("Nepodarilo sa odoslať email na: " + toEmail);
            var6.printStackTrace();
        }

    }

    static {
        switch ("recepty@cambalik.eu".substring("recepty@cambalik.eu".indexOf("@") + 1).toLowerCase()) {
            case "gmail.com":
                HOST = "smtp.gmail.com";
                PORT = 587;
                USE_TLS = true;
                USE_SSL = false;
                break;
            case "outlook.com":
            case "hotmail.com":
                HOST = "smtp-mail.outlook.com";
                PORT = 587;
                USE_TLS = true;
                USE_SSL = false;
                break;
            case "seznam.cz":
                HOST = "smtp.seznam.cz";
                PORT = 465;
                USE_TLS = false;
                USE_SSL = true;
                break;
            case "zoho.com":
            case "zoho.eu":
                HOST = "smtp.zoho.eu";
                PORT = 587;
                USE_TLS = true;
                USE_SSL = false;
                break;
            case "centrum.sk":
                HOST = "smtp.centrum.sk";
                PORT = 465;
                USE_TLS = false;
                USE_SSL = true;
                break;
            default:
                HOST = "mailin.endora.cz";
                PORT = 587;
                USE_TLS = true;
                USE_SSL = false;
        }

    }
}
