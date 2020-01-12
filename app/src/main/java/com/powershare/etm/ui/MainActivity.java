package com.powershare.etm.ui;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.constant.PermissionConstants;
import com.powershare.etm.R;
import com.powershare.etm.adapter.MyPagerAdapter;
import com.powershare.etm.component.MyDialog;
import com.powershare.etm.databinding.ActivityMainBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.tabmap.TabMapFragment;
import com.powershare.etm.vm.LoginViewModel;
import com.powershare.etm.ui.setting.SettingActivity;
import com.powershare.etm.ui.tab1.Tab1Fragment;
import com.powershare.etm.ui.tab2.Tab2Fragment;
import com.powershare.etm.ui.tab3.Tab3Fragment;
import com.powershare.etm.ui.tab4.Tab4Fragment;
import com.powershare.etm.util.LocationUtils;
import com.powershare.etm.util.PermissionHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ViewPager mViewPager;
    private QMUITabSegment mTabSegment;
    private QMUITopBarLayout mTopBar;
    private LoginViewModel loginViewModel;

    @Override
    protected View initContentView() {
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        mTopBar = binding.topBar;
        mViewPager = binding.pager;
        mTabSegment = binding.tabs;
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
    }

    @Override
    protected void onMounted() {
        initTopBar();
        initPermissions();

        int currentTab = 0;
        if (getIntent() != null) {
            currentTab = getIntent().getIntExtra("tabIndex", 0);
        }
        initPagers(currentTab);
        initTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginViewModel.checkLogin();
    }

    private void initTopBar() {
        mTopBar.setTitle(R.string.app_name);
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addRightImageButton(R.mipmap.setting, View.generateViewId()).setOnClickListener(view -> go(SettingActivity.class));
    }

    private void initPermissions() {
        PermissionHelper.getPermission(() -> {
            if (!LocationUtils.isLocationEnabled() || !LocationUtils.isGpsEnabled()) {
                new MyDialog.Builder(this)
                        .setContent("需要开启定位服务并且使用高精度(GPS)定位模式，APP才能正常使用")
                        .setSureText("去设置")
                        .setSureListener(sureBtn -> LocationUtils.openGpsSettings()).create().show();
            }
        }, PermissionConstants.STORAGE, PermissionConstants.LOCATION);
    }

    private void initPagers(int currentTab) {
        ArrayList<Fragment> viewList = new ArrayList<>();
        viewList.add(Tab1Fragment.newInstance());
        viewList.add(Tab2Fragment.newInstance());
        viewList.add(Tab3Fragment.newInstance());
        viewList.add(TabMapFragment.newInstance());
        viewList.add(Tab4Fragment.newInstance());
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), viewList));
        mViewPager.setCurrentItem(currentTab);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTabs() {
        mTabSegment.setDefaultNormalColor(ContextCompat.getColor(this, R.color.grayA6));
        mTabSegment.setDefaultSelectedColor(ContextCompat.getColor(this, R.color.colorAccent));
        QMUITabSegment.Tab tab1 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this, R.mipmap.tab1),
                ContextCompat.getDrawable(this, R.mipmap.tab1_active),
                getText(R.string.title_tab1), false
        );
        QMUITabSegment.Tab tab2 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this, R.mipmap.tab2),
                ContextCompat.getDrawable(this, R.mipmap.tab2_active),
                getText(R.string.title_tab2), false
        );
        QMUITabSegment.Tab tab3 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this, R.mipmap.tab3),
                ContextCompat.getDrawable(this, R.mipmap.tab3_active),
                getText(R.string.title_tab3), false
        );
        QMUITabSegment.Tab tabMap = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this, R.mipmap.tab_map),
                ContextCompat.getDrawable(this, R.mipmap.tab_map_active),
                getText(R.string.title_tab_map), false
        );
        QMUITabSegment.Tab tab4 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this, R.mipmap.tab4),
                ContextCompat.getDrawable(this, R.mipmap.tab4_active),
                getText(R.string.title_tab4), false
        );
        mTabSegment.addTab(tab1)
                .addTab(tab2)
                .addTab(tab3)
                .addTab(tabMap)
                .addTab(tab4);
        mTabSegment.notifyDataChanged();
        mTabSegment.setupWithViewPager(mViewPager, false);
        //
        ViewGroup viewGroup = (ViewGroup) mTabSegment.getChildAt(0);
        View.OnTouchListener onTouchListener = (view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && !LoginViewModel.isLogin()) {
                go(LoginActivity.class);
                return true;
            }
            return false;
        };
        viewGroup.getChildAt(0).setOnTouchListener(onTouchListener);
        viewGroup.getChildAt(1).setOnTouchListener(onTouchListener);
    }

    public void selectTab(int tabIndex) {
        mTabSegment.selectTab(tabIndex);
    }
}
