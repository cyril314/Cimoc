package com.haleydu.cimoc.source;

import android.util.Pair;
import com.google.common.collect.Lists;
import com.haleydu.cimoc.App;
import com.haleydu.cimoc.core.Manga;
import com.haleydu.cimoc.model.Chapter;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.parser.JsonIterator;
import com.haleydu.cimoc.parser.MangaCategory;
import com.haleydu.cimoc.parser.MangaParser;
import com.haleydu.cimoc.parser.SearchIterator;
import com.haleydu.cimoc.soup.Node;
import com.haleydu.cimoc.utils.HttpUtils;
import com.haleydu.cimoc.utils.StringUtils;
import okhttp3.Headers;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Manhuatai extends MangaParser {

    public static final int TYPE = 49;
    public static final String DEFAULT_TITLE = "漫画台";
    public static final String HOST_STR = "m.manhuatai.com";
    public static final String URL_STR = HTTPS + HOST_STR;

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public Manhuatai(Source source) {
        init(source, new Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = StringUtils.format(URL_STR + "/api/getsortlist/?product_id=2&productname=mht&platformname=wap&orderby=click&search_key=%s&page=%d&size=48",
                URLEncoder.encode(keyword, "UTF-8"), page);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        JSONObject object = new JSONObject(html);

        return new JsonIterator(object.getJSONObject("data").getJSONArray("data")) {
            @Override
            protected Comic parse(JSONObject object) throws JSONException {
                String title = object.getString("comic_name");
                String comid_id = object.getString("comic_id");
                String cid = object.getString("comic_newid") + "-" + comid_id;
                String cover = "https://image.yqmh.com/mh/" + comid_id + ".jpg-300x400.webp";
                return new Comic(TYPE, cid, title, cover, "", "");
            }
        };
    }

    private Node getComicNode(String cid) throws Manga.NetworkErrorException {
        Request request = getInfoRequest(cid.split("-")[0]);
        String html = Manga.getResponseBody(App.getHttpClient(), request);
        return new Node(html);
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = URL_STR + "/".concat(cid.split("-")[0]) + "/";
        return HttpUtils.getSimpleMobileRequest(url);
    }

    //获取封面等信息（非搜索页）
    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#detail > div > div > h1:nth-child(1)");
        String cover = body.attr("#detail > img", "data-src");
        cover = "https:" + cover;
        String intro = body.text("#js_comciDesc");
        comic.setInfo(title, cover, "", intro, "", false);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i = 0;
        for (Node node : new Node(html).list("#js_chapter_list > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(1);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    private String _path = null;

    //获取漫画图片Request
    @Override
    public Request getImagesRequest(String cid, String path) {
        _path = path;
        String img_url = "/api/getchapterinfov2?product_id=2&productname=mht&platformname=wap&comic_id=%s&chapter_newid=1&isWebp=1&quality=low";
        String url = StringUtils.format(URL_STR + img_url, cid.split("-")[1]);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        try {
            JSONObject object = new JSONObject(html);
            if (object.getInt("status") != 0) {
                return list;
            }

            JSONArray chapters = object.getJSONObject("data").getJSONObject("current_chapter").getJSONArray("chapter_img_list");
            for (int i = 0; i < chapters.length(); i++) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                list.add(new ImageUrl(id, comicChapter, i, chapters.getString(i), false));
            }
        } catch (JSONException ex) {
            // ignore
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("span.update").substring(0, 10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("a.sdiv")) {
            String cid = node.hrefWithSplit(0);
            String title = node.attr("title");
            String cover = node.getChild("img").attr("data-url");
            Node node1 = null;
            try {
                node1 = getComicNode(cid);
            } catch (Manga.NetworkErrorException e) {
                e.printStackTrace();
            }
            if (StringUtils.isEmpty(cover) && node1 != null) {
                cover = node1.src("#offlinebtn-container > img");
            }
            String author = null;
            String update = null;
            if (node1 != null) {
                author = node1.text("div.jshtml > ul > li:nth-child(3)").substring(3);
                update = node1.text("div.jshtml > ul > li:nth-child(5)").substring(3);
            }
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return StringUtils.format(URL_STR + "/%s_p%%d.html", args[CATEGORY_SUBJECT]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部漫画", "all"));
            list.add(Pair.create("知音漫客", "zhiyinmanke"));
            list.add(Pair.create("神漫", "shenman"));
            list.add(Pair.create("风炫漫画", "fengxuanmanhua"));
            list.add(Pair.create("漫画周刊", "manhuazhoukan"));
            list.add(Pair.create("飒漫乐画", "samanlehua"));
            list.add(Pair.create("飒漫画", "samanhua"));
            list.add(Pair.create("漫画世界", "manhuashijie"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return false;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            return null;
        }

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", URL_STR);
    }
}
