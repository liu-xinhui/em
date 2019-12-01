package com.powershare.etm.util;

import android.text.TextUtils;

import com.amap.api.services.help.Tip;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchLocHistoryHelper {
    private static final String key = "search_loc_history";
    private static final SPUtils SP_UTILS = SPUtils.getInstance();
    private List<Tip> list;
    private Gson gson = new Gson();

    public SearchLocHistoryHelper() {
        list = getHistories();
    }

    @SuppressWarnings("all")
    public void addOneHistory(Tip address) {
        if (address != null && address.getName() != null) {
            Iterator<Tip> iterator = list.iterator();
            while (iterator.hasNext()) {
                Tip item = iterator.next();
                if (address.getName().equals(item.getName())) {
                    iterator.remove();
                }
            }
            if (list.size() >= 20) {
                list.remove(list.size() - 1);
            }
            list.add(0, address);
            SP_UTILS.put(key, gson.toJson(list));
        }
    }

    public List<Tip> getList() {
        return list == null ? new ArrayList<>() : list;
    }

    private List<Tip> getHistories() {
        List<Tip> data = new ArrayList<>();
        String str = SP_UTILS.getString(key);
        if (TextUtils.isEmpty(str)) {
            return data;
        }
        List<Tip> list = gson.fromJson(str, new TypeToken<List<Tip>>() {
        }.getType());
        data.addAll(list);
        return data;
    }

    public void clear() {
        SP_UTILS.remove(key);
    }
}
