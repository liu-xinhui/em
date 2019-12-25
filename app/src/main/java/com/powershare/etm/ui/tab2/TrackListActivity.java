package com.powershare.etm.ui.tab2;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.databinding.ActivityTrackListBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.TrackViewModel;

import java.util.List;

import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class TrackListActivity extends BaseActivity {
    private ActivityTrackListBinding binding;
    private TrackViewModel trackViewModel;
    private BaseRecyclerAdapter<Trip> adapter;
    private int currentPage = 1;

    @Override
    protected View initContentView() {
        binding = ActivityTrackListBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
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
        adapter = new BaseRecyclerAdapter<Trip>(R.layout.item_track_history) {
            @Override
            protected void bindView(BaseByViewHolder holder, Trip trip, int position) {
                holder.setText(R.id.start_name, trip.getStartAddress());
                holder.setText(R.id.end_name, trip.getDestAddress());
                holder.setText(R.id.date, TimeUtils.millis2String(trip.getStartTimestamp(), "yyyy.MM.dd   HH:mm:ss"));
                holder.setText(R.id.start_soc, trip.getStartSoc() + "%");
                holder.setText(R.id.end_soc, trip.getDestSoc() + "%");

                ImageView endImg = (ImageView) holder.getView(R.id.end_img);
                if (trip.getDestSoc() >= 30) {
                    endImg.setImageResource(R.mipmap.history_end_white);
                } else {
                    endImg.setImageResource(R.mipmap.history_end);
                }

                holder.setText(R.id.mileage, AMapUtil.formatDouble(trip.getMileage()) + "KM");
                holder.setText(R.id.power, AMapUtil.formatDouble(trip.getStartSoc() - trip.getDestSoc()) + "%");
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnRefreshListener(() -> getListData(true));
        binding.recyclerView.setOnLoadMoreListener(() -> getListData(false));
        binding.recyclerView.setRefreshing(true);

        binding.recyclerView.setOnItemClickListener((v, position) -> {
            Trip trip = adapter.getData().get(position);
            Intent intent = new Intent(this, TrackDetailActivity.class);
            intent.putExtra("trickId", trip.getId());
            startActivity(intent);
        });
    }

    private void getListData(boolean isRefresh) {
        int page = isRefresh ? 1 : (currentPage + 1);
        trackViewModel.traceQuery(page).observe(this, new MyObserver<List<Trip>>() {
            @Override
            public void onSuccess(List<Trip> trips) {
                if (page == 1) {
                    currentPage = 1;
                    if (CollectionUtils.isEmpty(trips)) {
                        CommonUtil.showToast("无行程记录");
                    } else {
                        adapter.setNewData(trips);
                    }
                } else {
                    if (CollectionUtils.isEmpty(trips)) {
                        binding.recyclerView.loadMoreEnd();
                    } else {
                        currentPage = currentPage + 1;
                        adapter.addData(trips);
                        binding.recyclerView.loadMoreComplete();
                    }
                }
            }

            @Override
            public void onFinish() {
                if (isRefresh) {
                    binding.recyclerView.setRefreshing(false);
                }
            }
        });
    }
}
