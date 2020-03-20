package com.powershare.etm.ui.tab2;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripReport;
import com.powershare.etm.component.ImageView360;
import com.powershare.etm.databinding.ActivityTrackListBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.CarViewModel;
import com.powershare.etm.vm.TrackViewModel;

import java.util.ArrayList;
import java.util.List;

import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class TrackListActivity extends BaseActivity {
    private ActivityTrackListBinding binding;
    private TrackViewModel trackViewModel;
    private CarViewModel carViewModel;
    private BaseRecyclerAdapter<Trip> adapter;
    private int currentPage = 1;
    private List<CarModel> mCarModels;
    private View headView;

    @Override
    protected View initContentView() {
        binding = ActivityTrackListBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
        carViewModel = ViewModelProviders.of(this).get(CarViewModel.class);
    }

    @Override
    protected void onMounted() {
        initTopBar();
        initListView();
        getCarListData();
    }

    private void initTopBar() {
        binding.topBar.setTitle("历史行程记录");
        binding.topBar.setBackgroundAlpha(1);
        binding.topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    private void getCarListData() {
        //车辆列表数据
        carViewModel.carList(false).observe(this, new MyObserver<List<CarModel>>() {

            @Override
            public void onSuccess(List<CarModel> carModels) {
                mCarModels = carModels;
            }
        });
    }

    private String getCarModelName(String carModelId) {
        if (mCarModels != null) {
            for (CarModel mCarModel : mCarModels) {
                if (mCarModel.getId().equals(carModelId)) {
                    return mCarModel.getName();
                }
            }
        }
        return "";
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
                holder.setText(R.id.mileage, AMapUtil.formatDouble(trip.getMileage()) + "km");
                holder.setText(R.id.power, AMapUtil.formatDouble(trip.getEnergy()) + "kWh");
                holder.setText(R.id.temp, trip.getTemperature() + "℃");
                holder.setText(R.id.carModel, getCarModelName(trip.getCarModelId()));
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnRefreshListener(() -> getListData(true));
        binding.recyclerView.setOnLoadMoreListener(() -> getListData(false));
        headView = LayoutInflater.from(this).inflate(R.layout.view_track_detail_head, binding.recyclerView, false);
        binding.recyclerView.addHeaderView(headView);
        binding.recyclerView.setRefreshing(true);

        View more = headView.findViewById(R.id.more);
        more.setOnClickListener(view -> go(TrackReportActivity.class));

        binding.recyclerView.setOnItemClickListener((v, position) -> {
            Trip trip = adapter.getData().get(position);
            Intent intent = new Intent(this, TrackDetailActivity.class);
            intent.putExtra("trickId", trip.getId());
            startActivity(intent);
        });
        getHeadData();
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

    private void getHeadData() {
        trackViewModel.getTripReport().observe(this, new MyObserver<TripReport>() {
            @Override
            public void onSuccess(TripReport report) {
                List<String> items = new ArrayList<>();
                items.add(AMapUtil.formatDouble(report.getTotalMileage()) + ",km,总里程");
                items.add(report.getTotalChargeTimes() + ",次,总充电次数");
                GridLayout infoContainer = headView.findViewById(R.id.info_container);
                for (String item : items) {
                    String[] itemArr = item.split(",");
                    View view = LayoutInflater.from(TrackListActivity.this).inflate(R.layout.item_title_value, null);
                    TextView value = view.findViewById(R.id.item_title_value);
                    TextView unit = view.findViewById(R.id.item_title_value_unit);
                    TextView title = view.findViewById(R.id.item_title);
                    value.setText(itemArr[0]);
                    unit.setText(itemArr[1]);
                    title.setText(itemArr[2]);
                    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                    param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1);
                    param.width = 0;
                    param.height = SizeUtils.dp2px(66);
                    int margin = SizeUtils.dp2px(8);
                    param.setMargins(margin, margin, margin, margin);
                    infoContainer.addView(view, param);
                }
                getCar(report.getCarModelId());
            }
        });
    }

    private void getCar(String carId) {
        carViewModel.getCar(carId).observe(this, new MyObserver<CarModel>() {
            @Override
            public void onSuccess(CarModel result) {
                LogUtils.json(result);
                initHeadUi(result);
            }
        });
    }

    private void initHeadUi(CarModel result) {
        TextView titleTv = headView.findViewById(R.id.car_title);
        ImageView360 banner = headView.findViewById(R.id.banner);
        titleTv.setText(result.getName());
        String[] photoIds = result.getPhotoIds();
        if (photoIds != null && photoIds.length > 0) {
            String[] photoUrls = new String[photoIds.length];
            for (int i = 0; i < photoIds.length; i++) {
                photoUrls[i] = CommonUtil.getImageUrl(result.getCarModelCode(), photoIds[i]);
            }
            banner.setBitmapUrls(photoUrls);
        } else {
            banner.setBitmapUrls(null);
        }
    }
}
