import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Trieda EmailSender slúži na odosielanie e-mailových správ zo systému.
 * Obsahuje automatickú konfiguráciu SMTP servera na základe domény odosielateľa
 * a podporuje odosielanie obsahu vo formáte HTML.
 */
public class EmailSender {

    /** Predvolená e-mailová adresa odosielateľa */
    private static final String FROM_EMAIL = "recepty@cambalik.eu";

    /** Prihlasovacie meno pre SMTP autentifikáciu */
    private static final String USERNAME = "recepty@cambalik.eu";

    /** Heslo pre SMTP autentifikáciu */
    private static final String PASSWORD = "";

    /** Adresa SMTP hostiteľa (určená automaticky) */
    private static final String HOST;

    /** Port SMTP servera (určený automaticky) */
    private static final int PORT;

    /** Príznak, či sa má použiť TLS šifrovanie */
    private static final boolean USE_TLS;

    /** Príznak, či sa má použiť SSL šifrovanie */
    private static final boolean USE_SSL;

    /**
     * Odošle e-mailovú správu s HTML obsahom.
     * * @param toEmail   E-mailová adresa príjemcu
     * @param subject   Predmet správy
     * @param htmlBody  Samotný obsah e-mailu v HTML formáte
     */
    public static void sendEmail(String toEmail, String subject, String htmlBody) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        // Konfigurácia zabezpečenia podľa zistených nastavení
        if (USE_TLS) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        if (USE_SSL) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        // Dodatočné nastavenia pre stabilitu a kompatibilitu
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.localhost", "receptar-app");
        props.put("mail.smtp.ehlo", "true");

        // Vytvorenie relácie s autentifikátorom
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Nastavenie obsahu ako HTML s kódovaním UTF-8
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email úspešne odoslaný na: " + toEmail + " (z " + FROM_EMAIL + ")");

        } catch (Exception e) {
            System.err.println("Nepodarilo sa odoslať email na: " + toEmail);
            e.printStackTrace();
        }
    }

    /**
     * Statický inicializačný blok, ktorý nastavuje parametre SMTP servera
     * na základe domény použitej v adrese odosielateľa.
     */
    static {
        String domain = FROM_EMAIL.substring(FROM_EMAIL.indexOf("@") + 1).toLowerCase();

        switch (domain) {
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
                // Predvolené nastavenia pre hosting Endora alebo iné vlastné domény
                HOST = "mailin.endora.cz";
                PORT = 587;
                USE_TLS = true;
                USE_SSL = false;
        }
    }
}
