package com.example.myapplication.Fagment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.Adapter.LoaiNhaHangAdapter;
import com.example.myapplication.Adapter.MonAnThemAdapter;
import com.example.myapplication.Model.GioHang;
import com.example.myapplication.Model.GioHangCT;
import com.example.myapplication.Model.MonAnThem;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MonAnCTFragment extends Fragment {
    private RecyclerView rcv_monAnThem;

    private TextView tv_tenMonCT, tv_giaMonCT, tv_chiTietMACT, tv_PhiVanChuyenMACT, tv_ThoiGianMACT, tv_DanhGiaMACT, tv_soLuongMonAnCT;
    private ImageView imv_hinhMonAnCT, imv_TroVe, imvCong, imvTru, imv_toGioHang;
    private Button btnThemVaoGioHang;


    private List<MonAnNH> listMonAn;
    private List<MonAnThem> listMonThem;

    private List<GioHang> listGioHang;
    private List<GioHangCT> listGioHangCT;

    private GioHangCT gioHangCT;

    private MonAnNH monAnNH;

    private String TenMonThem;
    private String MaGioHang = "";
    private int SoLuongMonAnGH = 0; // S??? l?????ng m??n ??n t??? gi??? h??ng c?? m?? m??n ??n ???? t??m
//    private String MaDanhGia = "";

//    private Dialog dialogDanhGiaNH;

    private int SoLuongMA = 1;
    //Firestore
    private FirebaseFirestore db;

    public MonAnCTFragment() {
        // Required empty public constructor
    }

    public static MonAnCTFragment newInstance() {
        MonAnCTFragment fragment = new MonAnCTFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mon_an_c_t, container, false);

        //G???i Firebase xu???ng
        db = FirebaseFirestore.getInstance();

        anhXa(view);

        getMaGH();

        getAllGioHang();

        thayDoiSoLuongMonAn();

        addListMonThem();

        clickThemGioHang();

        getAllMonAnChiTiet(getContext());

        imv_toGioHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                        .replace(R.id.nav_FrameFragment, new GioHangFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void addListMonThem(){
        listMonThem = new ArrayList<>();
        listMonThem.add(new MonAnThem("????i g??", R.drawable.ic_chickendleg, 0));
        listMonThem.add(new MonAnThem("C??m", R.drawable.ic_ricebow, 0));
        listMonThem.add(new MonAnThem("???t", R.drawable.ic_pepper, 0));
        listMonThem.add(new MonAnThem("C?? chua", R.drawable.ic_tomato, 0));
        listMonThem.add(new MonAnThem("Canh", R.drawable.ic_soup, 0));
        listMonThem.add(new MonAnThem("Pepsi", R.drawable.ic_pepsi, 0));
        listMonThem.add(new MonAnThem("Tr?? s???a", R.drawable.ic_bubbletea, 0));

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        MonAnThemAdapter monAnThemAdapter = new MonAnThemAdapter(getContext(), listMonThem, this);
        rcv_monAnThem.setLayoutManager(layoutManager);
        rcv_monAnThem.setAdapter(monAnThemAdapter);
    }

    //Nh???n button x??c nh???n th??m v??o gi??? h??ng
    private void clickThemGioHang(){
        btnThemVaoGioHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TenMonThem = "";
                //L???y c??c m??n ??n th??m m?? kh??ch h??ng ch???n
                for(MonAnThem ma: listMonThem){
                    if(ma.getCheckBox() == 1) TenMonThem += " " + ma.getTenMonThem();
                }

                //N???u t??i kho???n ch??a c?? m?? gi??? h??ng th?? t???o m?? gi??? h??ng cho no
                if(MaGioHang.isEmpty()) {
                    themGioHangToFireStore();
                }else{
                    if(kiemTraMonAnTrongGH(gioHangCT.getMaMA()).isEmpty()){
                        themGioHangCTToFireStore(MaGioHang);
                    }else{
                        updateMonAnTrongGioHang(kiemTraMonAnTrongGH(gioHangCT.getMaMA()), SoLuongMA);
                    }

                }
            }
        });
    }

    // Kh??ch h??ng nh???n t??ng gi???m s??? l?????ng m??n ??n
    private void thayDoiSoLuongMonAn(){
        imvCong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoLuongMA +=1;
                tv_soLuongMonAnCT.setText(SoLuongMA +"");
            }
        });

        imvTru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SoLuongMA > 1) {
                    SoLuongMA -= 1;
                    tv_soLuongMonAnCT.setText(SoLuongMA + "");
                }
            }
        });
    }

    private void anhXa(View view) {
        rcv_monAnThem = view.findViewById(R.id.rcv_monAnThemCT);
        tv_PhiVanChuyenMACT = view.findViewById(R.id.tv_PhiVanChuyenMACT);
        tv_ThoiGianMACT = view.findViewById(R.id.tv_ThoiGianMACT);
        tv_DanhGiaMACT = view.findViewById(R.id.tv_DanhGiaMACT);
        imv_hinhMonAnCT = view.findViewById(R.id.imv_hinhMonAnCT);
        imv_TroVe = view.findViewById(R.id.imv_TroVe);
        imv_toGioHang = view.findViewById(R.id.imv_toGioHang);
        tv_tenMonCT = view.findViewById(R.id.tv_tenMonAnCT);
        tv_giaMonCT = view.findViewById(R.id.tv_giaMonAnCT);
        tv_chiTietMACT = view.findViewById(R.id.tv_chiTietMonAnCT);
        imvCong = view.findViewById(R.id.imv_congSLMonAnCT);
        imvTru = view.findViewById(R.id.imv_truSLMonAnCT);
        tv_soLuongMonAnCT = view.findViewById(R.id.tv_soLuongMACT);
        btnThemVaoGioHang = view.findViewById(R.id.btn_themGHMonAnCT);

        //l???y d??? li???u t??? fragment m??n ??n
        Bundle bundle = this.getArguments();
        String maMA = bundle.getString("MaMA");
        String maNH = bundle.getString("MaNH");
        String tenNH = bundle.getString("TenNH");
        String hinhAnhNH = bundle.getString("HinhAnhNH");
        int gia = bundle.getInt("Gia");
        String tenMon = bundle.getString("TenMon");
        String chiTiet = bundle.getString("ChiTiet");
        String hinhAnh = bundle.getString("HinhAnh");
        String thoiGian = bundle.getString("ThoiGian");
//        MaDanhGia = bundle.getString("MaDanhGia");
        Double danhGia = bundle.getDouble("DanhGia");
        int phiVanChuyen = bundle.getInt("PhiVanChuyen");


        Intent intent = getActivity().getIntent();
        String maTK = intent.getStringExtra("MaTK");
        //L???y model ????? th??m gi??? h??ng m???i
        gioHangCT = new GioHangCT("", "", maMA, maTK, SoLuongMA, gia, tenMon, "", "", 0, hinhAnh, false, 0);

        tv_tenMonCT.setText(tenMon);
        tv_giaMonCT.setText(formatNumber(gia) + " VND");
        tv_chiTietMACT.setText(chiTiet);
        tv_PhiVanChuyenMACT.setText(formatNumber(phiVanChuyen) + " VND");
        tv_ThoiGianMACT.setText(thoiGian + " m");
        tv_DanhGiaMACT.setText(danhGia + "");
        if (hinhAnh.isEmpty()) {
            imv_hinhMonAnCT.setImageResource(R.drawable.im_food);
        } else {
            Picasso.with(getContext()).load(hinhAnh).into(imv_hinhMonAnCT);
        }

        imv_TroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("MaMA", maMA);
                bundle.putString("Gia", gia + "");
                bundle.putString("TenMon", tenMon);
                bundle.putString("ChiTiet", chiTiet);
                bundle.putString("HinhAnh", hinhAnhNH);
                bundle.putInt("PhiVanChuyen", phiVanChuyen);
                bundle.putString("ThoiGian", thoiGian);
                bundle.putDouble("DanhGia", danhGia);
                bundle.putString("MaNH", maNH);
                bundle.putString("TenNH", tenNH);
                MonAnFragment monAnFragment = new MonAnFragment();
                monAnFragment.setArguments(bundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                        .replace(R.id.nav_FrameFragment, monAnFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        imv_toGioHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                        .replace(R.id.nav_FrameFragment, new GioHangFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    public void getAllMonAnChiTiet(Context context){
        listMonAn = new ArrayList<>();

        Bundle bundle = this.getArguments();
        String _maMA = bundle.getString("MaMA");

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

                            if (maMA.equals(_maMA)) {
                                monAnNH = new MonAnNH(maMA, maNH, maMenuNH, tenMon, chiTiet, gia, hinhAnh);
                                listMonAn.add(monAnNH);
                            }
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Ki???m tra t??i kho???n ???? c?? m?? gi??? h??ng ch??a
    public void getMaGH(){

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

                            if(gioHangCT.getMaTK().equals(maTK)){
                                MaGioHang = maGH;
                                return;
                            }
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Th??m gi??? h??ng chi ti???t m???i l??n Firebase
    private void themGioHangCTToFireStore(String _maGH){
        final CollectionReference collectionReference = db.collection("GIOHANGCT");

        UUID uuid = UUID.randomUUID();
        String maGHCT = String.valueOf(uuid);

        Map<String, Object> data = new HashMap<>();
        data.put("MaGH", _maGH);
        data.put("MaGHCT", maGHCT);
        data.put("MaMA", gioHangCT.getMaMA());
        data.put("SoLuong", SoLuongMA);
        data.put("TenMonThem", TenMonThem);
        data.put("ThoiGian", FieldValue.serverTimestamp());
        data.put("TrangThai", gioHangCT.getTrangThai());
        data.put("TongTien", 0);

        try {
            collectionReference.document(maGHCT).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                            .replace(R.id.nav_FrameFragment, new NhaHangFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });

        }catch (Exception e){
            Log.d("Error add Firebase:", e.getMessage());
        }
    }


    // Th??m gi??? h??ng m???i l??n Firebase
    private void themGioHangToFireStore(){
        final CollectionReference collectionReference = db.collection("GIOHANG");

        Random random =  new Random();
        int x = random.nextInt((50000-1+1)+1);
        String maGH = "GH" + x;

        Map<String, Object> data = new HashMap<>();
        data.put("MaGH", maGH);
        data.put("MaTK", gioHangCT.getMaTK());

        try {
            collectionReference.document(maGH).set(data);
            themGioHangCTToFireStore(maGH);
        }catch (Exception e){
            Log.d("Error add Firebase:", e.getMessage());
        }
    }


    //Khi th??m  m??n ??n ???? c?? trong gi??? h??ng th?? m??n ??n ???? s??? t??ng s??? l?????ng l??n
    private void updateMonAnTrongGioHang(String _maGHCT, int _soLuong){
        //Cho t??i kho???n th??nh quy???n ch??? nh?? h??ng
        db.collection("GIOHANGCT").document(_maGHCT)
                .update(
                        "SoLuong", _soLuong + SoLuongMonAnGH,
                        "ThoiGian", FieldValue.serverTimestamp(),
                        "TenMonThem", TenMonThem
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SoLuongMonAnGH += _soLuong;

                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                        .replace(R.id.nav_FrameFragment, new NhaHangFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });


    }


    // ?????nh d???ng sang s??? ti???n
    private String formatNumber(int number){
        // t???o 1 NumberFormat ????? ?????nh d???ng s??? theo ti??u chu???n c???a n?????c Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }


    //Ki???m tra ???? c?? m??n ??n t???n t???i trong gi??? h??ng hay ch??a, n???u c?? th?? xu???t r?? m?? gi??? h??ng chi ti???t c???a m??n ??n, c??n kh??ng xu???t r???ng
    private String kiemTraMonAnTrongGH(String _maMA){
        for(GioHangCT ghct: listGioHangCT){
            if(ghct.getMaMA().equals(_maMA)){
                SoLuongMonAnGH = ghct.getSoLuong();
                return ghct.getMaGHCT();
            }
        }
        return "";
    }

    //L???y danh s??ch gi??? h??ng t??? Firebase xu???ng
    public void getAllGioHang(){
        listGioHang = new ArrayList<>();

        Intent intent = getActivity().getIntent();
        String _maTK = intent.getStringExtra("MaTK");

        final CollectionReference reference = db.collection("GIOHANG");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    GioHang gioHang;
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot) {
                            String maGH = doc.get("MaGH").toString();
                            String maTK = doc.get("MaTK").toString();

                            if(maTK.equals(_maTK)) {
                                gioHang = new GioHang(maGH, maTK);
                                listGioHang.add(gioHang);

                                // L???y list gi??? h??ng chi ti???t
                                getAllGioHangCT(maGH);
                                break;
                            }
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), "Error getAllGioHang: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // L???y danh s??ch gi??? h??ng chi ti???t t??? Firebase xu???ng
    public void getAllGioHangCT(String _maGH){
        listGioHangCT = new ArrayList<>();

        final CollectionReference reference = db.collection("GIOHANGCT");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    GioHangCT gioHangCT;
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

                            if(_maGH.equals(maGH) && trangThai==0) {
                                gioHangCT = new GioHangCT(maGH, maGHCT, maMA, "", soLuong, 0, "", tenMonThem, thoiGian, trangThai, "", false, 0);
                                listGioHangCT.add(gioHangCT);
                            }
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), "Error getAllGioHangCT"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("=====>", e.getMessage());
                }
            }
        });
    }

}