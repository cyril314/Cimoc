package com.haleydu.cimoc.helper;

import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.ComicDao;
import com.haleydu.cimoc.model.DaoSession;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.source.*;

import java.util.ArrayList;
import java.util.List;

public class UpdateHelper {

    private static final int VERSION = 999;

    public static void update(PreferenceManager manager, final DaoSession session) {
        int version = manager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            initSource(session);
            manager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    private static void deleteDownloadFromLocal(final DaoSession session) {
        session.runInTx(new Runnable() {
            @Override
            public void run() {
                ComicDao dao = session.getComicDao();
                List<Comic> list = dao.queryBuilder().where(ComicDao.Properties.Local.eq(true)).list();
                if (!list.isEmpty()) {
                    for (Comic comic : list) {
                        comic.setDownload(null);
                    }
                    dao.updateInTx(list);
                }
            }
        });
    }

    /**
     * 初始化图源
     */
    private static void initSource(DaoSession session) {
        List<Source> list = new ArrayList<>();
        list.add(Animx2.getDefaultSource());
        list.add(BaiNian.getDefaultSource());
        list.add(Cartoonmad.getDefaultSource());
        list.add(ChuiXue.getDefaultSource());
        list.add(CopyMH.getDefaultSource());
        list.add(DM5.getDefaultSource());
        list.add(DmzjFix.getDefaultSource());
        list.add(Dmzjv2.getDefaultSource());
        list.add(EHentai.getDefaultSource());
        list.add(GuFeng.getDefaultSource());
        list.add(HHAAZZ.getDefaultSource());
        list.add(Hhxxee.getDefaultSource());
        list.add(HotManga.getDefaultSource());
        list.add(IKanman.getDefaultSource());
        list.add(JMTT.getDefaultSource());
        list.add(MangaBZ.getDefaultSource());
        list.add(Mangakakalot.getDefaultSource());
        list.add(MangaNel.getDefaultSource());
        list.add(ManHuaDB.getDefaultSource());
        list.add(Manhuatai.getDefaultSource());
        list.add(MH50.getDefaultSource());
        list.add(MH57.getDefaultSource());
        list.add(MHLove.getDefaultSource());
        list.add(MiGu.getDefaultSource());
        list.add(Ohmanhua.getDefaultSource());
        list.add(PuFei.getDefaultSource());
        list.add(QiManWu.getDefaultSource());
        list.add(QiMiaoMH.getDefaultSource());
        list.add(SixMH.getDefaultSource());
        list.add(Tencent.getDefaultSource());
        list.add(TuHao.getDefaultSource());
        list.add(U17.getDefaultSource());
        list.add(Webtoon.getDefaultSource());
        list.add(WebtoonDongManManHua.getDefaultSource());
        list.add(YKMH.getDefaultSource());
        list.add(YYLS.getDefaultSource());
        session.getSourceDao().insertOrReplaceInTx(list);
    }
}
