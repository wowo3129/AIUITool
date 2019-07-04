package com.iflytek.aiui.demo.chat.ui.detail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iflytek.aiui.demo.chat.R;
import com.pddstudio.highlightjs.HighlightJsView;
import com.pddstudio.highlightjs.models.Language;
import com.pddstudio.highlightjs.models.Theme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 语义详情Fragment
 */
public class DetailFragment extends Fragment {
    private static final String DETAIL_KEY = "detail";

    public static DetailFragment createDetailFragment(String content) {
        DetailFragment fragment = new DetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DETAIL_KEY, content);
        fragment.setArguments(arguments);

        return fragment;
    }

    protected HighlightJsView mDetailView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mMainView = inflater.inflate(R.layout.detail_fragment, container, false);
        String content = getArguments().getString(DETAIL_KEY);
        mDetailView = mMainView.findViewById(R.id.detail_js_view);
        //设置高亮语言和样式
        mDetailView.setHighlightLanguage(Language.JSON);
        mDetailView.setTheme(Theme.ARDUINO_LIGHT);
        try {
            mDetailView.setSource(new JSONObject(content).toString(2));
        } catch (JSONException e) {
            try {
                mDetailView.setSource(new JSONArray(content).toString(2));
            }catch (JSONException j) {
                mDetailView.setSource(content);
            }
        }
        return mMainView;
    }
}
