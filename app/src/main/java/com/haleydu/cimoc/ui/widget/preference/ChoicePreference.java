package com.haleydu.cimoc.ui.widget.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.R;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.ui.fragment.BaseFragment;
import com.haleydu.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.haleydu.cimoc.ui.widget.Option;

public class ChoicePreference extends Option implements View.OnClickListener {

    private PreferenceManager mPreferenceManager;
    private FragmentManager mFragmentManager;
    private Fragment mTargetFragment;
    private String mPreferenceKey;
    private String[] mItems;
    private int mChoice;
    private int mRequestCode;

    public ChoicePreference(Context context) {
        this(context, null);
    }

    public ChoicePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChoicePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.custom_option, this);

        mPreferenceManager = ((App) context.getApplicationContext()).getPreferenceManager();

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mFragmentManager != null) {
            ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.dialog_choice, mItems, mChoice, mRequestCode);
            if (mTargetFragment != null) {
                fragment.setTargetFragment(mTargetFragment, 0);
            }
            fragment.show(mFragmentManager, null);
        }
    }

    public void bindPreference(FragmentManager manager, String key, int def, int item, int request) {
        bindPreference(manager, null, key, def, item, request);
    }

    /**
     * 设置偏好
     *
     * @param manager  模块管理
     * @param fragment 设置模块
     * @param key      默认
     * @param def      默认值
     * @param item     参数集合
     * @param request
     */
    public void bindPreference(FragmentManager manager, BaseFragment fragment, String key, int def, int item, int request) {
        mFragmentManager = manager;
        mTargetFragment = fragment;
        mPreferenceKey = key;
        mChoice = mPreferenceManager.getInt(key, def);
        mItems = getResources().getStringArray(item);
        mSummaryView.setText(mItems[mChoice]);
        mRequestCode = request;
    }

    public int getValue() {
        return mChoice;
    }

    public void setValue(int choice) {
        mPreferenceManager.putInt(mPreferenceKey, choice);
        mChoice = choice;
        mSummaryView.setText(mItems[mChoice]);
    }
}
