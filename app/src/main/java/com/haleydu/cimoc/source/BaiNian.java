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
import com.haleydu.cimoc.utils.HttpUtils;
import com.haleydu.cimoc.utils.StringUtils;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

public class BaiNian extends MangaParser {

    public static final int TYPE = 13;
    public static final String DEFAULT_TITLE = "百年漫画";
    public static final String HOST_STR = "m.bnmanhua.cc";
    public static final String URL_STR = "https:" + HOST_STR;

    public BaiNian(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    // 这里一直无法弄好
    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = URL_STR + "/index.php/search.html";
        }

        RequestBody requestBodyPost = new FormBody.Builder().add("keyword", keyword).build();
        return HttpUtils.getMobileRequest(url).post(requestBodyPost)
                .addHeader("Referer", URL_STR).addHeader("Host", HOST_STR).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul.tbox_m > li.vbox")) {
            @Override
            protected Comic parse(Node node) {
                String title = node.attr("a.vbox_t", "title");
                String cid = node.attr("a.vbox_t", "href");
                String cover = node.attr("a.vbox_t > mip-img", "src");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return URL_STR.concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(HOST_STR));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = URL_STR.concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.attr("div.dbox > div.img > mip-img", "src");
        String title = body.text("div.dbox > div.data > h4");
        String intro = body.text("div.tbox_js");
        String author = body.text("div.dbox > div.data > p.dir").substring(3).trim();
        String update = body.text("div.dbox > div.data > p.act").substring(3, 13).trim();
        boolean status = isFinish(body.text("span.list_item"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i = 0;
        for (Node node : new Node(html).list("div.tabs_block > ul > li > a")) {
            String title = node.text();
            String path = node.href();
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = URL_STR.concat(path);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String host = StringUtils.match("src=\"(.*?)\\/upload", html, 1);
        String path_str = StringUtils.match("z_img=\'\\[(.*?)\\]\'", html, 1);
        if (path_str != null && !path_str.equals("")) {
            try {
                String[] array = path_str.split(",");
                for (int i = 0; i != array.length; ++i) {
                    String path = array[i].replace("\"", "").replace("\\", "");
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id, comicChapter, i + 1, host + "/" + path, false));
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
        // 这里表示的是 parseInfo 的更新时间
        return new Node(html).text("div.dbox > div.data > p.act").substring(3, 13).trim();
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", URL_STR);
    }
}
