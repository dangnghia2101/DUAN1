package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.DoanhThuMA;
import com.example.myapplication.Model.DoanhThuNH;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DoanhThuMAAdapter extends RecyclerView.Adapter<DoanhThuMAAdapter.MyViewHolder> {
    private List<DoanhThuMA> list;
    private Context context;

    public DoanhThuMAAdapter(List<DoanhThuMA> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doanhthuma, parent, false);

        return new DoanhThuMAAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DoanhThuMA dt = list.get(position);

        holder.tvTenMA.setText(dt.getTenMA());
        holder.tvLuotMua.setText(dt.getLuotMua()+"");
        holder.tvTongDT.setText(formatNumber(dt.getTongDT()));

        if(dt.getHinhAnh().isEmpty()){
            holder.imvHinh.setImageResource(R.drawable.ic_addimage);
        }else Picasso.with(context).load(dt.getHinhAnh()).resize(2048, 1600).centerCrop().onlyScaleDown().into(holder.imvHinh);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenMA, tvLuotMua, tvTongDT;
        ImageView imvHinh;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTenMA = itemView.findViewById(R.id.tv_itemTenMADTMA);
            tvLuotMua = itemView.findViewById(R.id.tv_itemTongLMDTMA);
            tvTongDT = itemView.findViewById(R.id.tv_itemTongDTMA);
            imvHinh = itemView.findViewById(R.id.imv_itemHinhDTMA);
        }
    }

    // ?????nh d???ng sang s??? ti???n
    private String formatNumber(int number){
        // t???o 1 NumberFormat ????? ?????nh d???ng s??? theo ti??u chu???n c???a n?????c Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }

}
