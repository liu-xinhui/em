package com.powershare.etm.ui.tab2;

import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.blankj.utilcode.util.FragmentUtils;
import com.powershare.etm.R;
import com.powershare.etm.databinding.FragmentTab2Binding;
import com.powershare.etm.ui.base.BaseFragment;

public class Tab2Fragment extends BaseFragment {

    private StartTrackFragment startTrackFragment;

    public static Tab2Fragment newInstance() {
        return new Tab2Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        FragmentTab2Binding binding = FragmentTab2Binding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentUtils.add(fragmentManager, startTrackFragment = StartTrackFragment.newInstance(), R.id.fragment_container);
        }
    }

    @Override
    protected void loadData() {
        if (startTrackFragment != null) {
            startTrackFragment.initData();
        }
    }
}
