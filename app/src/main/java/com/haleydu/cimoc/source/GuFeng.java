package com.haleydu.cimoc.source;

import com.google.common.collect.Lists;
import com.haleydu.cimoc.model.Chapter;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.parser.MangaParser;
import com.haleydu.cimoc.parser.NodeIterator;
import com.haleydu.cimoc.parser.SearchIterator;
import com.haleydu.cimoc.parser.UrlFilter;
import com.haleydu.cimoc.soup.Node;
import com.haleydu.cimoc.utils.HttpUtils;
import com.haleydu.cimoc.utils.StringUtils;
import okhttp3.Headers;
import okhttp3.Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

public class GuFeng extends MangaParser {

    public static final int TYPE = 25;
    public static final String DEFAULT_TITLE = "古风漫画";
    public static final String HOST_STR = "www.gfmh88.com";
    public static final String URL_STR = HTTP + HOST_STR;

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public GuFeng(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format(URL_STR + "/search/?keywords=%s", URLEncoder.encode(keyword, "UTF-8"));
        }
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public String getUrl(String cid) {
        return URL_STR.concat("/manhua/").concat(cid).concat("/");
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(HOST_STR));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#w0 > ul > li")) {
            @Override
            protected Comic parse(Node node) {
                String cover = node.attr("a > img", "src");
                String title = node.text("p > a");
                String cid = node.attr("p > a", "href").replace(URL_STR + "/manhua/", "");
                cid = cid.substring(0, cid.length() - 1);

                String update = node.attr("data-key");
                String author = node.text("p.auth");

                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        return HttpUtils.getSimpleMobileRequest(getUrl(cid));
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.comic_deCon > h1");
        String cover = body.src("div.comic_i_img > img");
        String intro = body.text("div.comic_deCon > p");
        String author = body.text("div.comic_deCon > ul.comic_deCon_liO > li:nth-child(1)");

        // 连载状态
        boolean status = isFinish("连载");
        comic.setInfo(title, cover, null, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<Chapter>();
        int i = 0;
        for (Node node : new Node(html).list("ul[id=chapter-list-1] > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format(URL_STR + "/manhua/%s/%s.html", cid, path);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<ImageUrl>();
        String str = StringUtils.match("chapterImages = \\[(.*?)\\]", html, 1);
        if (str != null) {
            try {
                String[] array = str.split(",");
                String urlPrev = StringUtils.match("chapterPath = \"(.*?)\"", html, 1);
                for (int i = 0; i != array.length; ++i) {
                    // 去掉首末两端的双引号
                    String s = array[i].substring(1, array[i].length() - 1);
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id, comicChapter, i + 1, "http://res1.kingwar.cn/" + urlPrev + s, false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        // 这里表示的是更新时间
        return new Node(html).text("div.pic > dl:eq(4) > dd");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
    }
}
