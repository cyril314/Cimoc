package com.haleydu.cimoc.manager;

import android.util.SparseArray;
import com.haleydu.cimoc.component.AppGetter;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.model.SourceDao;
import com.haleydu.cimoc.model.SourceDao.Properties;
import com.haleydu.cimoc.parser.Parser;
import com.haleydu.cimoc.source.*;
import okhttp3.Headers;
import rx.Observable;

import java.util.List;

public class SourceManager {

    private static SourceManager mInstance;

    private SourceDao mSourceDao;
    private SparseArray<Parser> mParserArray = new SparseArray<>();

    private SourceManager(AppGetter getter) {
        mSourceDao = getter.getAppInstance().getDaoSession().getSourceDao();
    }

    public static SourceManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (SourceManager.class) {
                if (mInstance == null) {
                    mInstance = new SourceManager(getter);
                }
            }
        }
        return mInstance;
    }

    public Observable<List<Source>> list() {
        return mSourceDao.queryBuilder().orderAsc(Properties.Type).rx().list();
    }

    public Observable<List<Source>> listEnableInRx() {
        return mSourceDao.queryBuilder().where(Properties.Enable.eq(true)).orderAsc(Properties.Type).rx().list();
    }

    public List<Source> listEnable() {
        return mSourceDao.queryBuilder().where(Properties.Enable.eq(true)).orderAsc(Properties.Type).list();
    }

    public Source load(int type) {
        return mSourceDao.queryBuilder().where(Properties.Type.eq(type)).unique();
    }

    public long insert(Source source) {
        return mSourceDao.insert(source);
    }

    public void update(Source source) {
        mSourceDao.update(source);
    }

    public Parser getParser(int type) {
        Parser parser = mParserArray.get(type);
        if (parser == null) {
            Source source = load(type);
            switch (type) {
                case Animx2.TYPE:
                    parser = new Animx2(source);
                    break;
                case BaiNian.TYPE:
                    parser = new BaiNian(source);
                    break;
                case Cartoonmad.TYPE:
                    parser = new Cartoonmad(source);
                    break;
                case ChuiXue.TYPE:
                    parser = new ChuiXue(source);
                    break;
                case CopyMH.TYPE:
                    parser = new CopyMH(source);
                    break;
                case DM5.TYPE:
                    parser = new DM5(source);
                    break;
                case DmzjFix.TYPE:
                    parser = new DmzjFix(source);
                    break;
                case Dmzjv2.TYPE:
                    parser = new Dmzjv2(source);
                    break;
                case EHentai.TYPE:
                    parser = new EHentai(source);
                    break;
                case GuFeng.TYPE:
                    parser = new GuFeng(source);
                    break;
                case HHAAZZ.TYPE:
                    parser = new HHAAZZ(source);
                    break;
                case Hhxxee.TYPE:
                    parser = new Hhxxee(source);
                    break;
                case HotManga.TYPE:
                    parser = new HotManga(source);
                    break;
                case IKanman.TYPE:
                    parser = new IKanman(source);
                    break;
                case JMTT.TYPE:
                    parser = new JMTT(source);
                    break;
                case Locality.TYPE:
                    parser = new Locality();
                    break;
                case MangaBZ.TYPE:
                    parser = new MangaBZ(source);
                    break;
                case Mangakakalot.TYPE:
                    parser = new Mangakakalot(source);
                    break;
                case MangaNel.TYPE:
                    parser = new MangaNel(source);
                    break;
                case ManHuaDB.TYPE:
                    parser = new ManHuaDB(source);
                    break;
                case Manhuatai.TYPE:
                    parser = new Manhuatai(source);
                    break;
                case MH50.TYPE:
                    parser = new MH50(source);
                    break;
                case MH57.TYPE:
                    parser = new MH57(source);
                    break;
                case MHLove.TYPE:
                    parser = new MHLove(source);
                    break;
                case MiGu.TYPE:
                    parser = new MiGu(source);
                    break;
                case Ohmanhua.TYPE:
                    parser = new Ohmanhua(source);
                    break;
                case PuFei.TYPE:
                    parser = new PuFei(source);
                    break;
                case QiManWu.TYPE:
                    parser = new QiManWu(source);
                    break;
                case QiMiaoMH.TYPE:
                    parser = new QiMiaoMH(source);
                    break;
                case SixMH.TYPE:
                    parser = new SixMH(source);
                    break;
                case Tencent.TYPE:
                    parser = new Tencent(source);
                    break;
                case TuHao.TYPE:
                    parser = new TuHao(source);
                    break;
                case U17.TYPE:
                    parser = new U17(source);
                    break;
                case Webtoon.TYPE:
                    parser = new Webtoon(source);
                    break;
                case WebtoonDongManManHua.TYPE:
                    parser = new WebtoonDongManManHua(source);
                    break;
                case YKMH.TYPE:
                    parser = new YKMH(source);
                    break;
                case YYLS.TYPE:
                    parser = new YYLS(source);
                    break;
                default:
                    parser = new Null();
                    break;
            }
            mParserArray.put(type, parser);
        }
        return parser;
    }

    public class TitleGetter {

        public String getTitle(int type) {
            return getParser(type).getTitle();
        }
    }

    public class HeaderGetter {

        public Headers getHeader(int type) {
            return getParser(type).getHeader();
        }
    }
}
