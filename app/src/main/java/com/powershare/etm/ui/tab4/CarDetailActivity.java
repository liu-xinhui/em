package com.powershare.etm.ui.tab4;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.databinding.ActivityCarDetailBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.ArrayList;
import java.util.List;

public class CarDetailActivity extends BaseActivity {
    private ActivityCarDetailBinding binding;
    private QMUITopBarLayout mTopBar;

    @Override
    protected View initContentView() {
        binding = ActivityCarDetailBinding.inflate(getLayoutInflater());
        mTopBar = binding.topBar;
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        initTopBar();
        //取值
        Intent intent = getIntent();
        CarModel currentCar = (CarModel) intent.getSerializableExtra("item");
        if (currentCar == null) {
            return;
        }

        binding.carTitle.setText(currentCar.getName());
        String[] photoIds = currentCar.getPhotoIds();
        if (photoIds != null && photoIds.length > 0) {
            String[] photoUrls = new String[photoIds.length];
            for (int i = 0; i < photoIds.length; i++) {
                photoUrls[i] = CommonUtil.getImageUrl(currentCar.getCarModelCode(), photoIds[i]);
            }
            binding.banner.setBitmapUrls(photoUrls);
        } else {
            binding.banner.setBitmapUrls(null);
        }

        List<String> items = new ArrayList<>();
        items.add(AMapUtil.formatDouble(currentCar.getMaxSoeKm()) + ",km,续航里程");
        items.add(AMapUtil.formatDouble(currentCar.getCafc()) + ",kwh/100km,能量消耗");
        items.add(AMapUtil.formatDouble(currentCar.getMaxSpeed()) + ",km/h,最高时速");
        items.add(Double.valueOf(currentCar.getAccTime()).intValue() + ",min,快充时间");
        items.add(AMapUtil.formatDouble(currentCar.getChargeTimeKm100()) + ",h,慢充时间");
        items.add(AMapUtil.formatDouble(currentCar.getMaxSoeKwh()) + ",kWh,电池容量");

        for (String item : items) {
            String[] itemArr = item.split(",");
            View view = LayoutInflater.from(this).inflate(R.layout.item_title_value, null);
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
    }

    private void initTopBar() {
        mTopBar.setTitle(R.string.app_name);
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

}
