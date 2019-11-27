package com.hold.electrify.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchHistoryHelper {
    private static final String key = "report_search_history";
    private static final SPUtils SP_UTILS = SPUtils.getInstance();
    private List<String> list;

    public SearchHistoryHelper() {
        list = getHistories();
    }

    public void addOneHistory(String item) {
        if (!TextUtils.isEmpty(item)) {
            list.remove(item);
            if (list.size() >= 5) {
                list.remove(list.size() - 1);
            }
            list.add(0, item);
            SP_UTILS.put(key, TextUtils.join(",", list));
        }
    }

    public List<String> getList() {
        return list == null ? new ArrayList<>() : list;
    }

    private List<String> getHistories() {
        List<String> data = new ArrayList<>();
        String str = SP_UTILS.getString(key);
        if (TextUtils.isEmpty(str)) {
            return data;
        }
        String[] split = TextUtils.split(str, ",");
        Collections.addAll(data, split);
        return data;
    }

    public void clear() {
        SP_UTILS.remove(key);
    }
}
