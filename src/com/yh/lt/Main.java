package com.yh.lt;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

public class Main {
    private final static String URL_TEMPLATE="https://lardi-trans.com/gruz/?countryfrom={0}&countryto={1}&mass2={2}&startSearch=%D0%A1%D0%B4%D0%B5%D0%BB%D0%B0%D1%82%D1%8C+%D0%B2%D1%8B%D0%B1%D0%BE%D1%80%D0%BA%D1%83";
    private final static long SLEEP = 10 * 60 * 1000 / 1000;

    final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[]) throws IOException {
        scheduleParser();


    }


    private static void scheduleParser(){
        new Thread(() -> {
            while(true){
            try {
                Thread.sleep(SLEEP);
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

    private static void contactSupport(String s) {
        // todo
    }

    public static void parse() throws Exception {

            parse(MessageFormat.format(URL_TEMPLATE, "BY", "LT", 2));
            parse(MessageFormat.format(URL_TEMPLATE, "LT", "BY", 2));
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
}
