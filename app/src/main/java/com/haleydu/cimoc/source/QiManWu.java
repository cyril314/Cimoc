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
import com.haleydu.cimoc.utils.StringUtils;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class QiManWu extends MangaParser {

    public static final int TYPE = 53;
    public static final String DEFAULT_TITLE = "奇漫屋";
    public static final String HOST_STR = "m.qiman58.com";
    public static final String URL_STR = HTTP + HOST_STR;

    public QiManWu(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page != 1) return null;
        String url = URL_STR + "/spotlight/?keyword=" + keyword;
        RequestBody body = new FormBody.Builder().add("keyword", keyword).build();
        return HttpUtils.getMobileRequest(url, body);
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".search-result > .comic-list-item")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.href("a");
                String title = node.text("p.comic-name");
                String cover = node.attr("img", "src");
                String author = node.text("p.comic-author");
                return new Comic(TYPE, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return URL_STR.concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(URL_STR));
    }

    @Override
    public Request getInfoRequest(String cid) {
        return HttpUtils.getSimpleMobileRequest(getUrl(cid));
    }

    private static String ChapterHtml;

    @Override
    public Comic parseInfo(String html, Comic comic) {
        ChapterHtml = html;
        Node body = new Node(html);
        String update = body.text(".box-back2 > :eq(4)");
        if (!update.contains("更新时间：")) update = body.text(".box-back2 > :eq(3)");
        update = update.replace("更新时间：", "");
        String title = body.text(".box-back2 > h1");
        String intro = body.text("span.comic-intro");
        String author = body.text(".box-back2 > :eq(2)");
        String cover = body.src(".box-back1 > img");
        boolean status = isFinish(body.text(".box-back2 > p.txtItme.c1"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = URL_STR + "/bookchapter/";
        String id = Objects.requireNonNull(StringUtils.match(" data: \\{ \"id\":(.*?),", html, 1)).trim();
        String id2 = Objects.requireNonNull(StringUtils.match(", \"id2\":(.*?)\\},", html, 1)).trim();
        RequestBody body = new FormBody.Builder().add("id", id).add("id2", id2).build();
        return HttpUtils.getMobileRequest(url).post(body).addHeader("Referer", URL_STR)
                .addHeader("Host", HOST_STR).build();
    }

    /**
     * 解析章节
     *
     * @param html        页面源代码
     * @param comic       漫画名
     * @param sourceComic
     */
    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        try {
            int k = 0;
            for (Node node : new Node(ChapterHtml).list("div.list-wrap > div > ul > li")) {
                String title = node.text("a");
                String path = node.attr("li", "data-id");
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + k++), sourceComic, title, path));
            }
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                JSONObject chapter = array.getJSONObject(i);
                String title = chapter.getString("name");
                String path = chapter.getString("id");
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + k++), sourceComic, title, path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format(URL_STR + "/%s/%s.html", cid, path);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("eval\\((.*?\\}\\))\\)", html, 0);
        try {
            String str1 = DecryptionUtils.evalDecrypt(str, "newImgs");
            if (str1.isEmpty()) {
                str1 = DecryptionUtils.evalDecrypt(str);
            }
            String[] array = str1.split(",");
            for (int i = 0; i != array.length; ++i) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                list.add(new ImageUrl(id, comicChapter, i + 1, array[i], false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        Node body = new Node(html);
        String update = body.text(".box-back2 > :eq(4)");
        if (!update.contains("更新时间：")) update = body.text(".box-back2 > :eq(3)");
        update = update.replace("更新时间：", "");
        return update;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("User-Agent", HttpUtils.getHeadStr());
    }
}
