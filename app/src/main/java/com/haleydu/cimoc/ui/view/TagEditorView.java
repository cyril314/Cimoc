package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.misc.Switcher;
import com.haleydu.cimoc.model.Tag;

import java.util.List;

public interface TagEditorView extends BaseView {

    void onTagLoadSuccess(List<Switcher<Tag>> list);

    void onTagLoadFail();

    void onTagUpdateSuccess();

    void onTagUpdateFail();

}
