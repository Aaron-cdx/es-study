package com.duanxi.jd.utils;

import com.duanxi.jd.entity.Good;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caoduanxi
 * @Date 2021/1/9 13:52
 * @Motto Keep thinking, keep coding!
 */
public class JsoupUtils {
    public static void main(String[] args) throws IOException {
        for (Good java : getJDGoods("java")) {
            System.out.println(java);
        }
    }

    public static List<Good> getJDGoods(String kewWord) throws IOException {
        List<Good> list = new ArrayList<>();
        Document document = Jsoup.parse(new URL("http://search.jd.com/Search?keyword=" + kewWord), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements lis = element.getElementsByTag("li");
        for (Element el : lis) {
            String image = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String title = el.getElementsByClass("p-name").eq(0).text();
            String price = el.getElementsByClass("p-price").eq(0).text();
            list.add(new Good(image, title, price));
        }
        return list;
    }
}
