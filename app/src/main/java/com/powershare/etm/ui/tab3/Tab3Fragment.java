package com.powershare.etm.ui.tab3;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.powershare.etm.R;
import com.powershare.etm.bean.SearchAddress;
import com.powershare.etm.databinding.FragmentTab3Binding;
import com.powershare.etm.ui.base.BaseFragment;

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
        View.OnClickListener onClickListener = view -> {
            Intent intent = new Intent(activity, SearchLocActivity.class);
            intent.putExtra("type", view.getId() == R.id.recent_track_start_text ? 1 : 2);
            startActivityForResult(intent, 1);
        };
        binding.recentTrackStartText.setOnClickListener(onClickListener);
        binding.recentTrackEndText.setOnClickListener(onClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1 && data != null) {
            int type = data.getIntExtra("type", 1);
            SearchAddress item = (SearchAddress) data.getSerializableExtra("result");
            if (item != null && type == 1) {
                binding.recentTrackStartText.setText(item.getName());
            } else if (item != null && type == 2) {
                binding.recentTrackEndText.setText(item.getName());
            }
        }
    }
}
