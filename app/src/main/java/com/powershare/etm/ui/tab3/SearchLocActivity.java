package com.powershare.etm.ui.tab3;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.powershare.etm.R;
import com.powershare.etm.databinding.ActivitySearchLocBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.SearchLocHistoryHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.ArrayList;
import java.util.List;

import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class SearchLocActivity extends BaseActivity {
    private ActivitySearchLocBinding binding;
    private QMUITopBarLayout mTopBar;
    private BaseRecyclerAdapter<Tip> adapter;
    private boolean searchTextEmpty = true;
    private int type;

    @Override
    protected View initContentView() {
        binding = ActivitySearchLocBinding.inflate(getLayoutInflater());
        mTopBar = binding.topBar;
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        initTopBar();
        Intent intent = getIntent();
        //type=1起点，type=2终点
        type = intent.getIntExtra("type", 1);
        binding.cancel.setOnClickListener(view -> finish());
        initList();
        initMap();
    }

    private void initTopBar() {
        mTopBar.setTitle(R.string.app_name);
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    private void initList() {
        adapter = new BaseRecyclerAdapter<Tip>(R.layout.item_search_loc) {
            @Override
            protected void bindView(BaseByViewHolder holder, Tip item, int position) {
                holder.setText(R.id.name, item.getName());
                holder.setText(R.id.address, item.getAddress());
            }
        };
        adapter.setHasStableIds(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnItemClickListener((v, position) -> {
            Tip item = adapter.getData().get(position);
            Intent i = new Intent();
            i.putExtra("type", type);
            i.putExtra("result", item);
            setResult(1, i);
            finish();
        });
        setHistory();
    }

    private void setHistory() {
        String count = SearchLocHistoryHelper.getInstance().getList().size() + "条";
        binding.historyCount.setText(count);
        adapter.setNewData(SearchLocHistoryHelper.getInstance().getList());
    }

    private void initMap() {

        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    searchTextEmpty = true;
                    binding.historyTitle.setVisibility(View.VISIBLE);
                    setHistory();
                } else {
                    searchTextEmpty = false;
                    binding.historyTitle.setVisibility(View.GONE);
                    InputtipsQuery inputQuery = new InputtipsQuery(charSequence.toString(), "上海");
                    inputQuery.setCityLimit(false);//限制在当前城市
                    Inputtips inputTips = new Inputtips(SearchLocActivity.this, inputQuery);
                    inputTips.setInputtipsListener((list, code) -> {
                        if (code == 1000 && !searchTextEmpty) {
                            List<Tip> newList = new ArrayList<>();
                            for (Tip tip : list) {
                                if (tip.getPoiID() != null && tip.getPoint() != null) {
                                    newList.add(tip);
                                }
                            }
                            adapter.setNewData(newList);
                        }
                    });
                    inputTips.requestInputtipsAsyn();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
