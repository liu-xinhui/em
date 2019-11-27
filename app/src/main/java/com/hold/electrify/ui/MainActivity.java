package com.hold.electrify.ui;

import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.constant.PermissionConstants;
import com.hold.electrify.R;
import com.hold.electrify.adapter.MyPagerAdapter;
import com.hold.electrify.databinding.ActivityMainBinding;
import com.hold.electrify.ui.base.BaseActivity;
import com.hold.electrify.ui.setting.SettingActivity;
import com.hold.electrify.ui.tab1.Tab1Fragment;
import com.hold.electrify.ui.tab2.Tab2Fragment;
import com.hold.electrify.ui.tab3.Tab3Fragment;
import com.hold.electrify.ui.tab4.Tab4Fragment;
import com.hold.electrify.util.PermissionHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ViewPager mViewPager;
    private QMUITabSegment mTabSegment;
    private QMUITopBarLayout mTopBar;

    @Override
    protected View initContentView() {
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        mTopBar = binding.topBar;
        mViewPager = binding.pager;
        mTabSegment = binding.tabs;
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        initTopBar();
        initPermissions();
        initPagers();
        initTabs();
    }

    private void initTopBar() {
        mTopBar.setTitle(R.string.app_name);
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addRightImageButton(R.mipmap.setting, View.generateViewId()).setOnClickListener(view -> go(SettingActivity.class));
    }

    private void initPermissions() {
        PermissionHelper.getPermission(() -> {
        }, PermissionConstants.STORAGE, PermissionConstants.LOCATION);
    }

    private void initPagers() {
        ArrayList<Fragment> viewList = new ArrayList<>();
        viewList.add(Tab1Fragment.newInstance());
        viewList.add(Tab2Fragment.newInstance());
        viewList.add(Tab3Fragment.newInstance());
        viewList.add(Tab4Fragment.newInstance());
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), viewList));
    }

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
        QMUITabSegment.Tab tab4 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(this, R.mipmap.tab4),
                ContextCompat.getDrawable(this, R.mipmap.tab4_active),
                getText(R.string.title_tab4), false
        );
        mTabSegment.addTab(tab1)
                .addTab(tab2)
                .addTab(tab3)
                .addTab(tab4);
        mTabSegment.notifyDataChanged();
        mTabSegment.setupWithViewPager(mViewPager, false);
        mTabSegment.selectTab(0);
    }

}
