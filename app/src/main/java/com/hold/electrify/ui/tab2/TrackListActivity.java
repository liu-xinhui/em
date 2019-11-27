package com.hold.electrify.ui.tab2;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hold.electrify.R;
import com.hold.electrify.bean.Trip;
import com.hold.electrify.databinding.ActivityTrackListBinding;
import com.hold.electrify.ui.base.BaseActivity;

import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class TrackListActivity extends BaseActivity {
    private ActivityTrackListBinding binding;
    private BaseRecyclerAdapter<Trip> adapter;

    @Override
    protected View initContentView() {
        binding = ActivityTrackListBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        initTopBar();
        initListView();
    }

    private void initTopBar() {
        binding.topBar.setTitle("历史行程记录");
        binding.topBar.setBackgroundAlpha(1);
        binding.topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    private void initListView() {
        //车辆列表recyclerView
        adapter = new BaseRecyclerAdapter<Trip>(R.layout.item_car) {
            @Override
            protected void bindView(BaseByViewHolder holder, Trip trip, int position) {
                holder.setText(R.id.title, trip.getUserId());
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnRefreshListener(this::getCarListData);
        binding.recyclerView.setRefreshing(true);
    }

    private void getCarListData() {

    }
}
