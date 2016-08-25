package com.heroku.nakau;

import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapingNakau {

    public static void main(String[] args) throws IOException {
        List<Map> result = new ArrayList<>();
        for (int i = 1; i < 200; i++) {
            String absUrl = "/jp/menu/detail/in/" + i;
            String url = "http://www.nakau.co.jp" + absUrl;
            Document document = Jsoup.connect(url).followRedirects(false).timeout(Integer.MAX_VALUE).get();
            if (document.select("div.columnRight figure img").isEmpty()) {
                System.out.println(url + " does not contain image. skipped.");
            } else {
                System.out.println(url + " connected.");
                Map map = new LinkedHashMap<>();
                String title = document.select("div.columnLeft h1.title").text();
                map.put("url", absUrl);
                map.put("title", title);
                if (i > 100) {
                    map.put("menu", "takeout");
                    map.put("takeout", true);
                } else {
                    map.put("menu", getCategory(title));
                    map.put("takeout", false);
                }
                result.add(map);
                List<Map> val = new ArrayList<>();
                Iterator<Element> priceIterator = document.select("table.column3 tr.price td").iterator();
                Iterator<Element> quickNoIterator = document.select("table.column3 tr.quickNo td").iterator();
                Iterator<Element> calIterator = document.select("table.column3 tr.kcal td").iterator();
                while (priceIterator.hasNext()) {
                    Map col = new LinkedHashMap<>();
                    Element typeAndPrice = priceIterator.next();
                    Elements span = typeAndPrice.select("span");
                    String type;
                    if (span.isEmpty()) {
                        type = "";
                    } else {
                        type = span.first().text();
                    }
                    String price = typeAndPrice.text().replaceFirst("^" + type, "").replace(",", "").replace("円", "");
                    col.put("type", type);
                    col.put("price", Integer.parseInt(price));
                    col.put("quickNo", quickNoIterator.next().text());
                    col.put("kcal", Integer.parseInt(calIterator.next().text()));
                    val.add(col);
                }
                map.put("val", val);
                map.put("img", document.select("div.columnRight figure img").first().attr("src"));
            }
        }
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(result);
        System.out.println(jsonString);
    }

    public static String getCategory(String title) {
        if (title.contains("朝")) {
            return "morning";
        } else if (title.contains("お子様")) {
            return "child";
        } else if (title.contains("定食")) {
            return "teishoku";
        } else {
            switch (title) {
                case "唐あげ":
                case "サラダ":
                case "ライス":
                case "こだわり卵のぷりん":
                case "みそ汁":
                case "とん汁":
                case "つけもの":
                case "こだわり卵":
                case "牛皿":
                case "缶ビール":
                case "納豆（単品）":
                case "鮭（単品）":
                case "かきあげ（単品）":
                    return "side";
                case "つけものセット":
                case "サラダセット":
                case "たまごセット":
                case "唐あげセット":
                    return "set";
            }
            return "main";
        }
    }
}
