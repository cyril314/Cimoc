package com.haleydu.cimoc.soup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @AUTO 解析页面
 * @Author Cyril
 * @DATE 2022/11/23
 */
public class MDocument {

    private Document document;

    public MDocument(String html) {
        this.document = Jsoup.parse(html);
    }

    public String text(String cssQuery) {
        try {
            Elements elements = document.select(cssQuery);
            return String.valueOf(elements.first());
        } catch (Exception e) {
            return null;
        }
    }
}
