package com.example.myapplication.Fagment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.LoaiNhaHangAdapter;
import com.example.myapplication.Adapter.MonAnAdapter;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonAnFragment extends Fragment {
    private GridView gv_MonAn;
    private FloatingActionButton flBtnThemMA;
    private SearchView svMonAn;

    private TextView tv_TenNhaHangMA, tv_PhiVanChuyenMA, tv_ThoiGianMA, tv_DanhGiaMA;
    private ImageView imv_HinhNenMA, imv_TroVe, imv_toGioHang;

    private List<MonAnNH> listMonAn;

    private MonAnNH monAnNH;

    //Firestore
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        View view = inflater.inflate(R.layout.fragment_mon_an, container, false);

        anhXa(view);

        getAllMonAn(getContext()); // Lấy tất cả món ăn từ Firestore xuống

        return view;
    }

    private void anhXa(View view){
        flBtnThemMA = view.findViewById(R.id.flBtnThemMA);
        gv_MonAn = view.findViewById(R.id.gv_MonAn);
        tv_TenNhaHangMA = view.findViewById(R.id.tv_TenNhaHangMA);
        tv_PhiVanChuyenMA = view.findViewById(R.id.tv_PhiVanChuyenMA);
        tv_ThoiGianMA = view.findViewById(R.id.tv_ThoiGianMA);
        tv_DanhGiaMA  = view.findViewById(R.id.tv_DanhGiaMA);
        imv_HinhNenMA  = view.findViewById(R.id.imv_HinhNenMA);
        imv_TroVe  = view.findViewById(R.id.imv_TroVe);
        imv_toGioHang = view.findViewById(R.id.imv_toGioHang);

        //lấy dữ liệu từ fragment nhà hàng
        Bundle bundle = this.getArguments();
        String tenNhaHang = bundle.getString("TenNH");
        String hinhAnh = bundle.getString("HinhAnh");
        String thoiGian = bundle.getString("ThoiGian");
        Double danhGia = bundle.getDouble("DanhGia");
        int phiVanChuyen = bundle.getInt("PhiVanChuyen");

        tv_TenNhaHangMA.setText(tenNhaHang);
        tv_PhiVanChuyenMA.setText(formatNumber(phiVanChuyen) + " VND");
        tv_ThoiGianMA.setText(thoiGian + "m");
        tv_DanhGiaMA.setText(danhGia + "");
        if(hinhAnh.isEmpty()){
            imv_HinhNenMA.setImageResource(R.drawable.im_food);
        }else{
            Picasso.with(getContext()).load(hinhAnh).into(imv_HinhNenMA);
        }
        tv_ThoiGianMA.setText(thoiGian);

        imv_TroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        imv_toGioHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void getAllMonAn(Context context){
        listMonAn = new ArrayList<>();

        Bundle bundle = this.getArguments();
        String _maNH = bundle.getString("MaNH");

        final CollectionReference reference = db.collection("MONANNH");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot) {
                            String maMA = doc.get("MaMA").toString();
                            String maNH = doc.get("MaNH").toString();
                            String tenMon = doc.get("TenMon").toString();
                            String maMenuNH = doc.get("MaMenuNH").toString();
                            String chiTiet = doc.get("ChiTiet").toString();
                            int gia = Integer.parseInt(doc.get("Gia").toString());
                            String hinhAnh = doc.get("HinhAnh").toString();

                            if (maNH.equals(_maNH)) {
                                monAnNH = new MonAnNH(maMA, maNH, maMenuNH, tenMon, chiTiet, gia, hinhAnh);
                                listMonAn.add(monAnNH);
                            }
                        }

                        MonAnAdapter adapter = new MonAnAdapter(listMonAn, getContext());
                        gv_MonAn.setNumColumns(2);
                        gv_MonAn.setAdapter(adapter);

                    }else{
                        Toast.makeText(getContext(), "Kiểm tra kết nối mạng của bạn. Lỗi "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Định dạng sang số tiền
    private String formatNumber(int number){
        // tạo 1 NumberFormat để định dạng số theo tiêu chuẩn của nước Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }
}