package com.powershare.etm.ui.tab4;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.databinding.FragmentTab4Binding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;

import java.util.List;

import me.jingbin.library.ByRecyclerView;
import me.jingbin.library.adapter.BaseByViewHolder;
import me.jingbin.library.adapter.BaseRecyclerAdapter;

public class Tab4Fragment extends BaseFragment {

    private int imageCorner = SizeUtils.dp2px(6);
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
                String[] photoIds = carModel.getPhotoIds();
                if (photoIds != null && photoIds.length > 0) {
                    ImageView imageView = (ImageView) holder.getView(R.id.image);
                    Glide.with(Tab4Fragment.this)
                            .load(CommonUtil.getImageUrl(carModel.getCarModelCode(), photoIds[0]))
                            //.centerInside()
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                            //.transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView);
                }
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnRefreshListener(this::getCarListData);
        binding.recyclerView.setRefreshing(true);
        binding.recyclerView.setOnItemClickListener(new ByRecyclerView.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(activity, CarDetailActivity.class);
                intent.putExtra("item", adapter.getData().get(position));
                go(intent);
            }
        });
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
