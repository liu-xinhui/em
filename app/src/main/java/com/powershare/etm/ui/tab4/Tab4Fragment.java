package com.powershare.etm.ui.tab4;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.databinding.FragmentTab4Binding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.CarViewModel;

import java.util.List;

import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class Tab4Fragment extends BaseFragment {

    private FragmentTab4Binding binding;
    private CarViewModel carViewModel;
    private BaseRecyclerAdapter<CarModel> adapter;

    public static Tab4Fragment newInstance() {
        return new Tab4Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab4Binding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        carViewModel = ViewModelProviders.of(activity).get(CarViewModel.class);
    }

    @Override
    protected void onMounted() {
        //车辆列表recyclerView
        adapter = new BaseRecyclerAdapter<CarModel>(R.layout.item_car) {
            @Override
            protected void bindView(BaseByViewHolder holder, CarModel carModel, int position) {
                holder.setText(R.id.title, carModel.getName());
                String[] photoIds = carModel.getPhotoIds();
                if (photoIds != null && photoIds.length > 0) {
                    ImageView imageView = (ImageView) holder.getView(R.id.image);
                    Glide.with(Tab4Fragment.this)
                            .load(CommonUtil.getImageUrl(carModel.getCarModelCode(), photoIds[0]))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView);
                }
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnRefreshListener(() -> getCarList(true));
        binding.recyclerView.setRefreshing(true);
        binding.recyclerView.setOnItemClickListener((v, position) -> {
            Intent intent = new Intent(activity, CarDetailActivity.class);
            intent.putExtra("item", adapter.getData().get(position));
            go(intent);
        });
        this.getCarList(false);
    }

    private void getCarList(boolean network) {
        carViewModel.carList(network).observe(this, new MyObserver<List<CarModel>>() {

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
