package com.powershare.etm.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public abstract class BaseFragment extends Fragment {

    protected BaseActivity activity;

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

    //此处创建viewModel
    protected void createViewModel() {
    }

    //此处加载数据
    protected void onMounted() {
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