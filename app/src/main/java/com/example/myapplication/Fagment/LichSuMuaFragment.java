package com.example.myapplication.Fagment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.GioHangAdapter;
import com.example.myapplication.Adapter.LichSuMHAdapter;
import com.example.myapplication.Model.GioHang;
import com.example.myapplication.Model.GioHangCT;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LichSuMuaFragment extends Fragment {

    private RecyclerView rcv_lsMua;
    private TextView tvXoa;
    private ImageView imvTroVe;

    private List<MonAnNH> listMonAn;
    private List<GioHang> listGioHang;
    private List<GioHangCT> listGioHangCT;
    private List<String> listTenNH;

    private GioHang gioHang;
    private MonAnNH monAnNH;
    private GioHangCT gioHangCT;

    //Firestore
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lich_su_mua, container, false);

        anhxa(v);

        //G???i Firebase xu???ng
        db = FirebaseFirestore.getInstance();

        listTenNH = new ArrayList<>();

        getAllMonAn(getContext()); // L???y t???t c??? m??n ??n t??? Firebase xu???ng

        tvXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickXoa();
            }
        });

        imvTroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_out, R.anim.fade_out, R.anim.fade_in, R.anim.slide_in)
                        .replace(R.id.nav_FrameFragment, new CaiDatFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    private void anhxa(View v){
        rcv_lsMua = v.findViewById(R.id.rcv_lichSuMH);
        tvXoa = v.findViewById(R.id.tv_xoaLichSu);
        tvXoa.setVisibility(View.INVISIBLE);

        imvTroVe = v.findViewById(R.id.imv_TroveTrongLSMH);
    }


    // ?????nh d???ng sang s??? ti???n
    private String formatNumber(int number){
        // t???o 1 NumberFormat ????? ?????nh d???ng s??? theo ti??u chu???n c???a n?????c Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }

    //L??u l???i c??c checkbox ???? ch???n
    public void checkedGioHang(int positon){
        listGioHangCT.get(positon).setTrangThaiCheckbox(true); // l??u l???i c??c m??n ??n ???? nh???n ch???n
    }

    //B??? l??u c??c checkbox
    public void uncheckedGioHang(int positon){
        listGioHangCT.get(positon).setTrangThaiCheckbox(false); // l??u l???i c??c m??n ??n ???? nh???n ch???n
    }


    //L???y danh s??ch gi??? h??ng t??? Firebase xu???ng
    public void getAllGioHang(Context context){
        listGioHang = new ArrayList<>();

        Intent intent = getActivity().getIntent();
        String _maTK = intent.getStringExtra("MaTK");

        final CollectionReference reference = db.collection("GIOHANG");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot) {
                            String maGH = doc.get("MaGH").toString();
                            String maTK = doc.get("MaTK").toString();

                            if(maTK.equals(_maTK)) {
                                gioHang = new GioHang(maGH, maTK);
                                listGioHang.add(gioHang);

                                // L???y list gi??? h??ng chi ti???t
                                getAllGioHangCT(getContext(), maGH);
                                break;
                            }
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("===> ", "getAllGioHang " + e.getMessage());
                }
            }
        });
    }

    // L???y danh s??ch m??n ??n t??? Firebase xu???ng
    public void getAllMonAn(Context context){
        listMonAn = new ArrayList<>();

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

                            monAnNH = new MonAnNH(maMA, maNH, maMenuNH, tenMon, chiTiet, gia, hinhAnh);
                            listMonAn.add(monAnNH);
                        }

                        getAllGioHang(getContext()); //L???y danh t???t c??? danh s??ch gi??? h??ng t??? Firebase xu???ng

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("===> ", "getAllMonAN" + e.getMessage());
                }
            }
        });
    }

    // L???y danh s??ch gi??? h??ng chi ti???t t??? Firebase xu???ng
    public void getAllGioHangCT(Context context, String _maGH){
        listGioHangCT = new ArrayList<>();

        final CollectionReference reference = db.collection("GIOHANGCT");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot) {
                            String maMA = doc.get("MaMA").toString();
                            String maGHCT = doc.get("MaGHCT").toString();
                            String maGH = doc.get("MaGH").toString();
                            int soLuong = Integer.parseInt(doc.get("SoLuong").toString());
                            String tenMonThem = doc.get("TenMonThem").toString();
                            String thoiGian = doc.get("ThoiGian").toString();
                            int trangThai = Integer.parseInt(doc.get("TrangThai").toString());
                            long tongGiaDH = Long.parseLong(doc.get("TongTien").toString());

                            if(_maGH.equals(maGH) && trangThai==1) {
                                gioHangCT = new GioHangCT(maGH, maGHCT, maMA, "", soLuong, 0, "", tenMonThem, thoiGian, trangThai, "", false, tongGiaDH);
                                listGioHangCT.add(gioHangCT);

                            }
                        }
                        // Th??m ?????y ????? th??ng tin v??o gi??? h??ng chi ti??t
                        getAllDetail_gioHang();

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("=====>", "getAllGioHangCT " + e.getMessage());
                }
            }
        });
    }


    //Set Adapter gi??? h??ng
    private void adapter_gioHang(){
        LichSuMHAdapter adapter  = new LichSuMHAdapter(listGioHangCT, getContext(), this);
        rcv_lsMua.setLayoutManager(new LinearLayoutManager(getContext()));
        rcv_lsMua.setAdapter(adapter);
    }

    //C???p nh???t ?????y ????? th??ng tin gi??? h??ng l??n listGioHangCT;
    private void getAllDetail_gioHang(){
        for(int i=0; i<listGioHangCT.size(); i++){
            addDetail_gioHang(listGioHangCT.get(i).getMaMA(), i);
        }

        //?????y list l??n adapter gi??? h??ng
        adapter_gioHang();
    }

    //T??m ki???m m??n ??n b???ng m?? m??n ??n, n???u c?? th??m m??n ??n v??o list gi??? h??ng
    private void addDetail_gioHang(String maMA, int positon){
        for(MonAnNH ma: listMonAn){
            if(maMA.equals(ma.getMaMA())){
                listGioHangCT.get(positon).setTenMA(ma.getTenMon());
                listGioHangCT.get(positon).setGiaMA(ma.getGia());
                listGioHangCT.get(positon).setHinhAnh(ma.getHinhAnh());

                //C???ng d???n v??o t???ng ti???n gi??? h??ng

                Intent intent = getActivity().getIntent();
                //Th??m m?? t??i kho???n v??o list gi??? h??ng
                listGioHangCT.get(positon).setMaTK(intent.getStringExtra("MaTK"));
            }
        }
    }

    // Duy???t danh s??ch ki???m tra xem item n??o c?? ch???n Checkbox th?? x??a n??
    public void clickXoa(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Th??ng b??o")
                .setMessage("B???n ch???n ch???n mu???n x??a nh?? h??ng kh??ng?")
                .setPositiveButton("C??", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            int duyet = 0;
                            for(GioHangCT gh: listGioHangCT){
                                if(gh.getTrangThaiCheckbox()) {
                                    deleteGioHangCTFirestore(gh.getMaGHCT());
                                    duyet = 1;
                                }
                            }

                            if(duyet == 0) Toast.makeText(getContext(), "B???n ch??a ch???n checkbox n??o!!", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(getContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }).setNegativeButton("Kh??ng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    // X??a m??n ??n trong gi??? h??ng
    private void deleteGioHangCTFirestore(String _maGHCT){

        db.collection("GIOHANGCT").document(_maGHCT)
                .delete();

        getAllGioHang(getContext()); //L???y danh t???t c??? danh s??ch gi??? h??ng t??? Firebase xu???ng, sau ???? ????a danh s??ch gi??? h??ng l??n rcv
    }




}