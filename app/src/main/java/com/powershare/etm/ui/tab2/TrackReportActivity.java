package com.powershare.etm.ui.tab2;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.TripReport;
import com.powershare.etm.databinding.ActivityTrackReportBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.CarViewModel;
import com.powershare.etm.vm.TrackViewModel;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.ArrayList;
import java.util.List;

public class TrackReportActivity extends BaseActivity {
    private ActivityTrackReportBinding binding;
    private TrackViewModel trackViewModel;
    private CarViewModel carViewModel;
    private QMUITopBarLayout mTopBar;

    @Override
    protected View initContentView() {
        binding = ActivityTrackReportBinding.inflate(getLayoutInflater());
        mTopBar = binding.topBar;
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
        getData();
    }

    private void initTopBar() {
        mTopBar.setTitle("行程报告详情");
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }


    private void getData() {
        trackViewModel.getTripReport().observe(this, new MyObserver<TripReport>() {
            @Override
            public void onSuccess(TripReport report) {
                String tip = "可贡献碳减排" + AMapUtil.formatDouble(report.getSaveCarbon()) + "kg,可节省燃油费用" + AMapUtil.formatDouble(report.getSaveFuelAmount()) + "元";
                binding.notice.setText(tip);

                List<String> items = new ArrayList<>();
                items.add(AMapUtil.formatDouble(report.getTotalMileage()) + ",km,总里程");
                items.add(report.getTotalChargeTimes() + ",次,总充电次数");
                for (String item : items) {
                    String[] itemArr = item.split(",");
                    View view = LayoutInflater.from(TrackReportActivity.this).inflate(R.layout.item_title_value, null);
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
                    binding.infoContainer.addView(view, param);
                }

                List<String> items2 = new ArrayList<>();
                items2.add(AMapUtil.formatDouble(report.getMaxMileage()) + ",km,单次最高里程");
                items2.add(AMapUtil.formatDouble(report.getMaxDuration()) + ",h,单次最高时长");
                for (String item : items2) {
                    String[] itemArr = item.split(",");
                    View view = LayoutInflater.from(TrackReportActivity.this).inflate(R.layout.item_title_value, null);
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
                    binding.infoContainer2.addView(view, param);
                }
                binding.start.setText(report.getMostFrequentStart());
                binding.end.setText(report.getMostFrequentDest());
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
        binding.carTitle.setText(result.getName());
        String[] photoIds = result.getPhotoIds();
        if (photoIds != null && photoIds.length > 0) {
            String[] photoUrls = new String[photoIds.length];
            for (int i = 0; i < photoIds.length; i++) {
                photoUrls[i] = CommonUtil.getImageUrl(result.getCarModelCode(), photoIds[i]);
            }
            binding.banner.setBitmapUrls(photoUrls);
        } else {
            binding.banner.setBitmapUrls(null);
        }
    }
}
