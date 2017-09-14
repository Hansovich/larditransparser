package com.yh.lt;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

public class Main {
    private final static String URL_TEMPLATE="https://lardi-trans.com/gruz/?countryfrom={0}&countryto={1}&mass2={2}&startSearch=%D0%A1%D0%B4%D0%B5%D0%BB%D0%B0%D1%82%D1%8C+%D0%B2%D1%8B%D0%B1%D0%BE%D1%80%D0%BA%D1%83";
    private final static long SLEEP_PARSER = 10 * 60 * 1000;
    private final static long SLEEP_SENDER = 5 * 60 * 1000;
    private final static long SUPPORT_DELAY = 30 * 60 * 1000;
    private final static String TO_EMAIL = "yhankovich@gmail.com";
    private final static String SUPPORT_EMAIL = "yhankovich@gmail.com";
    private final static String GMAIL_ACCOUNT = "larditransparser";
    private final static String GMAIL_PASSWORD = "babagala";
    private static long LAST_TIME_SUPPORT_CONTACTED = 0;


    final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[]) throws IOException {
        scheduleParser();
        scheduleSender();
    }


    private static void scheduleParser(){
        new Thread(() -> {
            while(true){
            try {
                Thread.sleep(SLEEP_PARSER);
                logger.info("in scheduleParser()");
                    parse(MessageFormat.format(URL_TEMPLATE, "BY", "LT", 2));
                    parse(MessageFormat.format(URL_TEMPLATE, "LT", "BY", 2));
            } catch (Throwable e){
                logger.error("Critical error", e);
                contactSupport(e.toString());
            }
            }
        }).start();
    }

    private static void scheduleSender(){
        new Thread(() -> {
            while(true){
            try {
                Thread.sleep(SLEEP_SENDER);
                logger.info("in scheduleSender()");

                MySQLAccess mysql = new MySQLAccess();

                List<Offer> newOffers = mysql.readDataBase();

                if (newOffers.isEmpty()){
                    continue;
                }

                String message = "";
                for (Offer o : newOffers){
                    message += "<a href=\"https://lardi-trans.com/gruz/view/"+o.getId()+"\">" + o.getRowHtml() + "</a><br/><br/>";
                }

                sendEmail(TO_EMAIL, message, "Новые предложения lardi-trans");

                mysql.markOffersSent(newOffers);

            } catch (Throwable e){
                logger.error("Critical error", e);
                contactSupport(e.toString());
            }
            }
        }).start();
    }

    private static void contactSupport(String s) {
        try {

            if (System.currentTimeMillis() - SUPPORT_DELAY > LAST_TIME_SUPPORT_CONTACTED){
                sendEmail(SUPPORT_EMAIL, s, "lorditrans message");
                LAST_TIME_SUPPORT_CONTACTED = System.currentTimeMillis();
            }
        } catch (MessagingException e) {
            logger.error(e);
        }
    }


    private static void parse(String urlstr) throws IOException, SQLException {

        URL url = new URL(urlstr);
        HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
        httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");

        Document document = Jsoup.parse(httpcon.getInputStream(), "UTF-8", urlstr);

        Elements table = document.getElementsByClass("mygtTable");
        if (table.size() == 0 ){
            return;
        }

        for( Element offerEl : table.get(0).getElementsByClass("predlInfoRow")){

            String idAttr = offerEl.id();
            if (idAttr == null || !idAttr.contains("predlRowGruz")){
                String msg = "Cant Parse Offer Table: " + table;
                logger.info(msg);
                contactSupport(msg);
                continue;
            }

            String id = idAttr.replace("predlRowGruz", "");

            Offer offer = new Offer(offerEl.text(), id, null);

            persist(offer);
        }
    }

    private static void persist(Offer offer) throws SQLException {
        new MySQLAccess().writeDataBase(offer);
    }



    public static void sendEmail(String to, String html, String subj) throws MessagingException {

        if (html == null || html.trim().isEmpty()){
            return;
        }

        // Sender's email ID needs to be mentioned
        String from = "larditransparser@gmail.com";

        // Assuming you are sending email from localhost

        // Get system properties
        Properties properties = System.getProperties();


        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.host", "smtp.gmail.com");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        //properties.put("mail.debug", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        // Get the default Session object.
        Session session = Session.getInstance(properties, new GMailAuthenticator(GMAIL_ACCOUNT, GMAIL_PASSWORD));

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subj);
            message.setContent(html, "text/html; charset=utf-8");

            // Send message
            Transport.send(message);
            logger.debug("Sent message successfully: " + html);
    }

    static class GMailAuthenticator extends Authenticator {
        String user;
        String pw;
        public GMailAuthenticator (String username, String password)
        {
            super();
            this.user = username;
            this.pw = password;
        }
        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(user, pw);
        }
    }
}
