package com.powershare.etm.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.LogUtils;

import lombok.Setter;


public abstract class BaseFragment extends Fragment {

    protected BaseActivity activity;
    @Setter
    private boolean dataLoaded = false;

    protected abstract View initContentView(LayoutInflater inflater);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initContentView(inflater);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (BaseActivity) getActivity();
        createViewModel();
        onMounted();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d("onResume-" + this.getClass().getName());
        if (!dataLoaded) {
            dataLoaded = true;
            LogUtils.d("loadData-" + this.getClass().getName());
            loadData();
        }
    }

    //此处创建viewModel
    protected void createViewModel() {
    }

    //此处加载ui
    protected void onMounted() {
    }

    //此处懒加载数据
    protected void loadData() {
    }

    public void go(Class activity) {
        Intent intent = new Intent(this.activity, activity);
        startActivity(intent);
    }

    public void go(Intent intent) {
        startActivity(intent);
    }

    public void showLoading() {
        activity.showLoading();
    }

    public void hideLoading() {
        activity.hideLoading();
    }


}