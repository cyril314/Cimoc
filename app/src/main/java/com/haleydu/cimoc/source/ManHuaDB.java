package com.haleydu.cimoc.source;

import com.haleydu.cimoc.model.Chapter;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.parser.MangaParser;
import com.haleydu.cimoc.parser.NodeIterator;
import com.haleydu.cimoc.parser.SearchIterator;
import com.haleydu.cimoc.parser.UrlFilter;
import com.haleydu.cimoc.soup.Node;
import com.haleydu.cimoc.utils.DecryptionUtils;
import com.haleydu.cimoc.utils.HttpUtils;
import com.haleydu.cimoc.utils.JsonUtils;
import com.haleydu.cimoc.utils.StringUtils;
import okhttp3.Headers;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ManHuaDB extends MangaParser {

    public static final int TYPE = 46;
    public static final String DEFAULT_TITLE = "漫画DB";
    public static final String HOST_STR = "www.manhuadb.com";
    public static final String URL_STR = HTTPS + HOST_STR;

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public ManHuaDB(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format(URL_STR + "/search?q=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return URL_STR.concat("/manhua/").concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.manhuadb.com"));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("a.d-block")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(1);
                String title = node.attr("title");
                String cover = node.attr("img", "data-original");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = URL_STR + "/manhua/".concat(cid);
        return HttpUtils.getSimpleMobileRequest(getUrl(cid));
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("h1.comic-title");
//        String cover = body.src("div.cover > img"); // 这一个封面可能没有
        String cover = body.src("td.comic-cover > img");
        String author = body.text("a.comic-creator");
        String intro = body.text("p.comic_story");
        boolean status = isFinish(body.text("a.comic-pub-state"));

        String update = body.text("a.comic-pub-end-date");
        if (update == null || update.equals("")) {
            update = body.text("a.comic-pub-date");
        }
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i = 0;
        for (Node node : new Node(html).list("#comic-book-list > div > ol > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format(URL_STR + "/manhua/%s/%s.html", cid, path);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();

        try {
            final String imageHost = StringUtils.match("data-host=\"(.*?)\"", html, 1);
            final String imagePre = StringUtils.match("data-img_pre=\"(.*?)\"", html, 1);
            final String base64Data = StringUtils.match("var img_data = '(.*?)';", html, 1);
            final String jsonStr = DecryptionUtils.base64Decrypt(base64Data);
            final JSONArray imageList = JsonUtils.jsonStr2JsonArray(jsonStr);
            for (int i = 0; i < imageList.length(); i++) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);

                final JSONObject image = imageList.getJSONObject(i);
                final String imageUrl = imageHost + imagePre + image.getString("img");

                list.add(new ImageUrl(id, comicChapter, image.getInt("p"), imageUrl, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return null;
    }

    @Override
    public String parseLazy(String html, String url) {
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        // 这里表示的是更新时间
        Node body = new Node(html);
        String update = body.text("a.comic-pub-end-date");
        if (update == null || update.equals("")) {
            update = body.text("a.comic-pub-date");
        }
        return update;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", URL_STR);
    }
}
