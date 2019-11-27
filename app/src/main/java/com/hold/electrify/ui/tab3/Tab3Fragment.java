package com.hold.electrify.ui.tab3;

import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.hold.electrify.databinding.FragmentTab3Binding;
import com.hold.electrify.ui.base.BaseFragment;

public class Tab3Fragment extends BaseFragment {

    private FragmentTab3Binding binding;
    private Tab3ViewModel tab3ViewModel;

    public static Tab3Fragment newInstance() {
        return new Tab3Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab3Binding.inflate(inflater);
        tab3ViewModel = ViewModelProviders.of(this).get(Tab3ViewModel.class);
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        System.out.println("------------");
    }
}
