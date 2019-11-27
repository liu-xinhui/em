package com.hold.electrify.ui.tab4;

import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hold.electrify.R;
import com.hold.electrify.bean.CarModel;
import com.hold.electrify.databinding.FragmentTab4Binding;
import com.hold.electrify.ui.base.BaseFragment;
import com.hold.electrify.util.MyObserver;

import java.util.List;

import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class Tab4Fragment extends BaseFragment {

    private FragmentTab4Binding binding;
    private Tab4ViewModel tab4ViewModel;
    private BaseRecyclerAdapter<CarModel> adapter;

    public static Tab4Fragment newInstance() {
        return new Tab4Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab4Binding.inflate(inflater);
        tab4ViewModel = ViewModelProviders.of(this).get(Tab4ViewModel.class);
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        //车辆列表recyclerView
        adapter = new BaseRecyclerAdapter<CarModel>(R.layout.item_car) {
            @Override
            protected void bindView(BaseByViewHolder holder, CarModel carModel, int position) {
                holder.setText(R.id.title, carModel.getName());
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnRefreshListener(this::getCarListData);
        binding.recyclerView.setRefreshing(true);
        this.getCarListData();
    }

    private void getCarListData() {
        //车辆列表数据
        tab4ViewModel.carList().observe(this, new MyObserver<List<CarModel>>() {

            @Override
            public void onSuccess(List<CarModel> carModels) {
                adapter.setNewData(carModels);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                binding.recyclerView.setRefreshing(false);
            }
        });
    }

}
