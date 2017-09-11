package com.yh.lt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

public class Main {
    private final static String URL_TEMPLATE="https://lardi-trans.com/gruz/?countryfrom={0}&countryto={1}&mass2={2}&startSearch=%D0%A1%D0%B4%D0%B5%D0%BB%D0%B0%D1%82%D1%8C+%D0%B2%D1%8B%D0%B1%D0%BE%D1%80%D0%BA%D1%83";



    public static void main(String args[]) throws IOException {
        parse();
    }


    public static void parse() throws IOException {

        try {
            parse(MessageFormat.format(URL_TEMPLATE, "BY", "LT", 2));
            parse(MessageFormat.format(URL_TEMPLATE, "LT", "BY", 2));
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public static void parse(String urlstr) throws IOException {

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
                //todo notify yhankovich@gmail.com
                continue;
            }

            String id = idAttr.replace("predlRowGruz", "");

            Offer offer = new Offer(offerEl.text(), id);

            System.out.println(offerEl.text());
        }
    }


    private static final class Offer{
        private final String rowHtml;
        private final String id;

        public Offer(String rowHtml, String id) {
            this.rowHtml = rowHtml;
            this.id = id;
        }

        public String getRowHtml() {
            return rowHtml;
        }

        public String getId() {
            return id;
        }
    }
}
