package com.example.myapplication.Fagment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.LoaiNhaHangAdapter;
import com.example.myapplication.Adapter.NhaHangAdapter;
import com.example.myapplication.Model.DanhGiaNH;
import com.example.myapplication.Model.GioHangCT;
import com.example.myapplication.Model.LoaiNhaHang;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.Model.NhaHang;
import com.example.myapplication.Model.TaiKhoan;
import com.example.myapplication.Model.YeuThich;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class NhaHangFragment extends Fragment {
    private List<NhaHang> listNhaHang;
    private List<NhaHang> listNhaHangTheoLoai;
    private List<DanhGiaNH> listDanhGia;
    private List<LoaiNhaHang> listLoaiNhaHang;
    private List<NhaHang> listNHSearch;
    private List<YeuThich> listYeuThich;
    private List<MonAnNH> listMonAn;
    private List<GioHangCT> listGioHangCT;
    private List<TaiKhoan> listTaiKhoan;

    //List cho spinner
    private List<String> listMaLoaiNH;
    private List<String> listMaTK;


    private NhaHang nhaHang;
    private LoaiNhaHang loaiNhaHang;

    private RecyclerView rcv_nhahang;
    private RecyclerView rcv_loainhahang;

    private TextView tvTenTK;
    private TextInputLayout tipSoDuTK;
    private ImageView imvThemLoaiNH, imvAvatar;
    private FloatingActionButton flBtnThemNH;
    private SearchView svNhaHang;

    //Dialog spinner th??m nh?? h??ng
//    private Spinner spMaTK;
    private Spinner spMaLoaiNH;

    //Dialog spinner s???a nh?? h??ng
//    private Spinner spMaTKSuaNH;
    private Spinner spMaLoaiNHSuaNH;
    private ImageView imvHinhSuaNH;

    private Dialog dialogThemLoaiNH;
    private Dialog dialogThemNH;
    private Dialog dialogSuaNH;

    //V??? tr?? hi???n t???i ??ang ch???n lo???i nh?? h??ng
    public int viTriLoaiNH = 0 ;

    public int QuyenDN = 2;

    //Dialog h??nh th??m lo???i nh?? h??ng
    private ImageView imvHinhLoai;

    //Dialog H??nh th??m nh?? h??ng
    private ImageView imvHinh;

    public String _maTK;


    // variable for FirebaseAuth class x??c th???c OTP
    private FirebaseAuth mAuth;
    //Firestore
    private FirebaseFirestore db;
    //Image firebase
    private StorageReference storageReference;


    //Load image
    int GALEERY_REQUEST_CODE = 105;
    Uri contenUri;
    String imageFileName ="";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_nha_hang, container, false);
        //Th??m th??ng tin ph???n t??i kho???n m??n h??nh ch??nh
        anhxa(v);

        kiemTraQuyenDangNhap();

        rcv_nhahang =v.findViewById(R.id.rcv_restaurant);
        rcv_loainhahang =v.findViewById(R.id.rcv_categoryRes);

        //G???i Firebase xu???ng
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        getMaLoaiTKToSpiner(0);
        // L???y danh y??u th??ch
        getAllYeuThich(getContext());

        //L???y danh s??ch m??n ??n
        getAllMonAn();

        //L???y danh s??ch gi??? h??ng t??? firebase
        getAllGioHangCT();

        //L???y danh s??ch ????nh gi?? xu???ng
        getAllDanhGia(getContext());


        //Lo???i nh?? h??ng
        rcv_loainhahang.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rcv_loainhahang, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // G??n bi???n cho gi?? tr??? v??? tr?? lo???i nh?? h??ng
                viTriLoaiNH = position;
                getAllNhaHangTheoLoai(position);
            }

            @Override
            public void onLongClick(View view, int position) {
//                if(QuyenDN == 0) {
//                    xoaLoaiNhaHang(listLoaiNhaHang.get(position).getMaLoaiNH(), listLoaiNhaHang.get(position).getTenLoaiNH());
//                }

                if(QuyenDN == 0) {
                    final CharSequence[] items = {"X??a", "Ch???nh s???a"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle(listLoaiNhaHang.get(position).getTenLoaiNH());
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if(item == 0){
                                alertDialogXoaNH(listLoaiNhaHang.get(position).getMaLoaiNH());
                                dialog.dismiss();
                            }else{
                                suaLoaiNH(listLoaiNhaHang.get(position).getMaLoaiNH(), listLoaiNhaHang.get(position).getTenLoaiNH());
                                dialog.dismiss();
                            }
                        }
                    });
                    builder.show();
                    Log.d("===> ", "M?? lo???i nh?? h??ng ?????u v??o: " + listLoaiNhaHang.get(position).getMaLoaiNH());
                }
            }
        }));


        // RecycleView nh?? h??ng
        rcv_nhahang.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rcv_nhahang, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                if(kiemTraQuyenDangNhapLongClick(listNhaHangTheoLoai.get(position))){
                    deleteNhaHangFireBase(position);
                };

            }
        }));

        //Nh???n n??t th??m lo???i nh?? h??ng
        imvThemLoaiNH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_themLoaiNH(0);
            }
        });

        //Nh???n n??t ????? th??m nh?? h??ng
        flBtnThemNH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_themNH(0);

            }
        });


        //Nh???n t??m ki???m n??t search
        search();

        return v;
    }


    // Ki???m tra t??i kho???n ???????c nh???n longClick ????? x??a c??c t??c v??? kh??ng
    private Boolean kiemTraQuyenDangNhapLongClick(NhaHang nh){
        if(QuyenDN == 0){
            return true; // T??i kho???n c?? quy???n nh???n x??a
        }else if(_maTK.equals(nh.getMaTK()) && QuyenDN == 1){
            return true;
        }
        return false;
    }

    public String getMaTK(){
        return _maTK;
    }

    public void chuyenDenFragmentMonAN(String _maNH, String _tenNH, String _hinhNH, int _phiVanChuyen, String _thoiGian, Double _danhGia, String _maDG, String _maTK){
        Bundle bundle = new Bundle();
        bundle.putString("MaNH", _maNH);
        bundle.putString("TenNH", _tenNH);
        bundle.putString("HinhAnh", _hinhNH);
        bundle.putInt("PhiVanChuyen", _phiVanChuyen);
        bundle.putString("ThoiGian", _thoiGian);
        bundle.putDouble("DanhGia", _danhGia);
        bundle.putString("MaDanhGia", _maDG);
        bundle.putString("MaTK", _maTK);
        MonAnFragment monAnFragment = new MonAnFragment();
        monAnFragment.setArguments(bundle);

        //getFragmentManager().beginTransaction().replace(R.id.nav_FrameFragment, monAnFragment).commit();

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.nav_FrameFragment, monAnFragment)
                .addToBackStack(null)
                .commit();
    }


    //Ki???m tra nh???ng nh?? h??ng m?? t??i kho???n y??u th??ch
    public Boolean check_favorite(String _maNH, int position){
        try {
            for(YeuThich yt: listYeuThich){
                if(yt.getMaNH().equals(_maNH)){
                    listNhaHang.get(position).setMaYT(yt.getMaYT());
                    return true;
                }
            }
        }catch (Exception e){
            return false;
        }

        return false;
    }


    private void anhxa(View v){
        tvTenTK = v.findViewById(R.id.tv_tenTaiKhoan_NhaHang);
        tipSoDuTK = v.findViewById(R.id.tip_soDuTK);
        imvThemLoaiNH = v.findViewById(R.id.imv_addLoaiNhFragNH);
        flBtnThemNH = v.findViewById(R.id.fbtn_themNhaHang);
        svNhaHang  = v.findViewById(R.id.sv_nhaHang);
        imvAvatar = v.findViewById(R.id.imv_avatarTaiKhoan_NhaHang);

        Intent intent = getActivity().getIntent();
        String tentk = intent.getStringExtra("HoTen");
        _maTK = intent.getStringExtra("MaTK");
        QuyenDN = intent.getIntExtra("Quyen", 2);
        String hinhAnh = intent.getStringExtra("HinhAnh");
        int soDu = Integer.parseInt(intent.getStringExtra("SoDu"));

        tvTenTK.setText(tentk);
        tipSoDuTK.getEditText().setText("S??? d??    "+formatNumber(soDu)+" VND");

        if(hinhAnh.isEmpty()){
            imvAvatar.setImageResource(R.drawable.avatar);
        }else Picasso.with(getContext()).load(hinhAnh).into(imvAvatar);
    }

    //Delete x??a lo???i nh?? h??ng
    private void xoaLoaiNhaHang(String _maLNH, String _tenLNH){
        Dialog dialog =  new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_chonsuaxoalnh);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.2);
        dialog.getWindow().setLayout(width,height);

        Button btnXoa = dialog.findViewById(R.id.btn_dialogXoaLNH);
        Button btnSua = dialog.findViewById(R.id.btn_dialogSuaLNH);

        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogXoaNH(_maLNH);
                dialog.dismiss();
            }
        });

        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suaLoaiNH(_maLNH, _tenLNH);
                dialog.dismiss();
            }
        });

        dialog.show();



    }


    //Delete s???a lo???i nh?? h??ng
    private void suaLoaiNH(String _maLNH, String _tenLNH){
        Dialog dialog =  new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_suatenlnh);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.3);
        dialog.getWindow().setLayout(width,height);

        Button btnSua = dialog.findViewById(R.id.btn_dialogXacNhanSuaLNH);
        EditText edtTenLNH = dialog.findViewById(R.id.edt_dialogSuaTenLNH);

        edtTenLNH.setText(_tenLNH);

        btnSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tenLNH = edtTenLNH.getText().toString().trim();
                if(!kiemKhoangTrang(tenLNH)){
                    Toast.makeText(getContext(), "Kh??ng ???????c nh???p kho???ng tr???ng", Toast.LENGTH_SHORT).show();
                }else if(tenLNH.length() < 4 || tenLNH.length() > 15){
                    Toast.makeText(getContext(), "T??n lo???i nh?? h??ng qu?? d??i ho???c qu?? ng???n", Toast.LENGTH_SHORT).show();
                }else{
                    //Cho t??i kho???n th??nh quy???n ch??? nh?? h??ng
                    db.collection("LOAINHAHANG").document(_maLNH)
                            .update(
                                    "TenLoaiNH", tenLNH
                            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            getAllLoaiNhaHang(getContext());
                        }
                    });

                }


            }
        });

        dialog.show();



    }

    //H???i ng?????i d??ng c?? ch???c mu???n x??a lo???i nh?? h??ng kh??ng
    private void alertDialogXoaNH(String _maLNH){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Th??ng b??o")
                .setMessage("B???n ch???n ch???n mu???n x??a lo???i nh?? h??ng kh??ng?")
                .setPositiveButton("C??", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {

                            int co = 1;
                            for(NhaHang nh: listNhaHang){
                                if(nh.getMaLoaiNH().equals(_maLNH)){
                                    co = 0;
                                    break;
                                }
                            }

                            if(co == 1) {
                                db.collection("LOAINHAHANG").document(_maLNH)
                                        .delete();

                                getAllLoaiNhaHang(getContext());
                            }else{
                                Toast.makeText(getContext(), "Kh??ng ???????c lo???i nh?? h??ng, h??y x??a t???t c??? nh?? h??ng thu???c lo???i nh?? h??ng n??y ????? x??a", Toast.LENGTH_SHORT).show();
                            }

                        }catch (Exception e){
                            Toast.makeText(getContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("===> ", "alertDialogXoaNH " + e.getMessage());
                        }
                    }
                }).setNegativeButton("Kh??ng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    //Ki???m tra quy???n ????ng nh???p ph?? h???p v???i ng?????i d??ng
    public void kiemTraQuyenDangNhap(){
        if(QuyenDN >= 1){
            flBtnThemNH.setVisibility(View.INVISIBLE);
            imvThemLoaiNH.setVisibility(View.INVISIBLE);
        }
    }

    // ?????nh d???ng sang s??? ti???n
    private String formatNumber(int number){
        // t???o 1 NumberFormat ????? ?????nh d???ng s??? theo ti??u chu???n c???a n?????c Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }

    //Xu???t t???t c??? nh?? nh?? h??ng l??n list
    public void getAllNhaHang(Context context){
        listNhaHang = new ArrayList<>();

        final CollectionReference reference = db.collection("NHAHANG");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String MaNH = doc.get("MaNH").toString().trim();
                            String MaLoaiNH = doc.get("MaLoaiNH").toString().trim();
                            String MaTK = doc.get("MaTK").toString().trim();
                            String TenNH = doc.get("TenNH").toString().trim();
                            String ThoiGian = doc.get("ThoiGian").toString().trim();
                            int PhiVanChuyen = 0;
                            try {
                                PhiVanChuyen = Integer.parseInt(doc.get("PhiVanChuyen").toString());
                            }catch (Exception e){};
                            String HinhAnh = doc.get("HinhAnh").toString();
                            String MaDG = doc.get("MaDG").toString().trim();

                            Double danhGia = tinhDanhGiaTB(MaNH);
                            nhaHang = new NhaHang(MaNH, MaLoaiNH, MaTK, TenNH, ThoiGian, PhiVanChuyen, HinhAnh, danhGia, MaDG, "");
                            listNhaHang.add(nhaHang);

                        }
//                        NhaHangAdapter adapter  = new NhaHangAdapter(listNhaHang, getContext());
//                        rcv_nhahang.setLayoutManager(new LinearLayoutManager(getContext()));
//                        rcv_nhahang.setAdapter(adapter);

                          //Xu???t danh s??ch nh?? h??ng ???????c ch???n theo lo???i

                          getAllNhaHangTheoLoai(0);
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), "L???i get nh?? h??ng: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("=====> ", "L???i get nh?? h??ng: " + e.getMessage());
                }
            }
        });
    }


    //T??nh l?????t sao c???a nh?? h??ng
    private Double tinhDanhGiaTB(String maNH){
        for (DanhGiaNH dg: listDanhGia){
            if(maNH.equals(dg.getMaNH())) {
                Double kq = (Double.valueOf(dg.getTongDG()) / Double.valueOf(dg.getLuotDG()));

                return Math.round(kq*100)/100.00;
            }
        }
        return 0.0;
    }

    //L???y t??n lo???i c???a nh?? h??ng
    public String getTenLoaiNhaHang(String maLoaiNH){
        for (LoaiNhaHang loai: listLoaiNhaHang){
            if(maLoaiNH.equals(loai.getMaLoaiNH()))
                return loai.getTenLoaiNH();
        }
        return "";
    }



    //Xu???t t???t c??? ????nh gi??
    public void getAllDanhGia(Context context){
        listDanhGia = new ArrayList<>();

        final CollectionReference reference = db.collection("DANHGIANH");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String MaDanhGia = doc.get("MaDanhGia").toString();
                            String MaNH = doc.get("MaNH").toString();
                            int LuotDG = Integer.parseInt(doc.get("LuotDG").toString());
                            int TongDG = Integer.parseInt(doc.get("TongDG").toString());

                            DanhGiaNH danhGiaNH = new DanhGiaNH(MaDanhGia, MaNH, LuotDG, TongDG);
                            listDanhGia.add(danhGiaNH);
                        }

                        //L???y danh s??ch lo???i nh?? h??ng xu???ng
                        getAllLoaiNhaHang(getContext());

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(),"Error getAllDanhGia: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("===> ", "getAllDanhGia " + e.getMessage());
                }
            }
        });
    }

    //L???y danh s??ch nh?? h??ng
    public void getAllLoaiNhaHang(Context context){
        listLoaiNhaHang = new ArrayList<>();

        final CollectionReference reference = db.collection("LOAINHAHANG");
        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String MaLoaiNH = doc.get("MaLoaiNH").toString();
                            String TenLoaiNH = doc.get("TenLoaiNH").toString();
                            String HinhAnh = doc.get("HinhAnh").toString();

                            LoaiNhaHang loaiNhaHang =  new LoaiNhaHang(MaLoaiNH, TenLoaiNH, HinhAnh);
                            listLoaiNhaHang.add(loaiNhaHang);
                        }

                        LinearLayoutManager layoutManager
                                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                        LoaiNhaHangAdapter adapter  = new LoaiNhaHangAdapter(listLoaiNhaHang, getContext());
                        rcv_loainhahang.setLayoutManager(layoutManager);
                        rcv_loainhahang.setAdapter(adapter);

                        //Xuat danh sach nha hang len recycleview
                        getAllNhaHang(getContext());

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), "Error getLoaiNhaHang "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("===> ", "getLoaiNhaHang " + e.getMessage());
                }
            }
        });
    }

    // L???y danh s??ch yeu th??ch
    public void getAllYeuThich(Context context){
        listYeuThich = new ArrayList<>();

        final CollectionReference reference = db.collection("YEUTHICH");
        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String maNH = doc.get("MaNH").toString();
                            String maTK = doc.get("MaTK").toString();
                            String maYT = doc.get("MaYT").toString();

                            if (maTK.equals(_maTK)) {
                                YeuThich yt = new YeuThich(maNH, maTK, maYT);
                                listYeuThich.add(yt);
                            }
                        }

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), "Error getAllYeuThich"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("====> ", "getAllYeuThich " + e.getMessage());
                }
            }
        });
    }

    //Xu???t List nh?? h??ng theo lo???i nh?? h??ng ch???n
    private void getAllNhaHangTheoLoai(int position){
        try {
            //Xu???t list l??n recycleView nh?? h??ng
            listNhaHangTheoLoai = new ArrayList<>();

            String maLoai = listLoaiNhaHang.get(position).getMaLoaiNH();
            for (int i=0; i<listNhaHang.size(); i++) {
                if (maLoai.equals("LNH01") && check_favorite(listNhaHang.get(i).getMaNH(), i)) {
                    //L???y y??u th??ch c???a t??i kho???n
                    listNhaHangTheoLoai.add(listNhaHang.get(i));
                } else if (listNhaHang.get(i).getMaLoaiNH().equals(maLoai) && !maLoai.equals("LNH01")) {
                    listNhaHangTheoLoai.add(listNhaHang.get(i));
                }
            }

            // L???y danh s??ch m?? lo???i nh?? h??ng ????? ?????y l??n spinner
            listMaLoaiNH = new ArrayList<>();
            for (LoaiNhaHang lnh : listLoaiNhaHang) {
                if(!lnh.getMaLoaiNH().equals("LNH01")) listMaLoaiNH.add(lnh.getTenLoaiNH()  );
            }

            NhaHangAdapter adapter = new NhaHangAdapter(listNhaHangTheoLoai, getContext(), this);
            rcv_nhahang.setLayoutManager(new LinearLayoutManager(getContext()));
            rcv_nhahang.setAdapter(adapter);
        }catch (Exception e){
            //Toast.makeText(getContext(), "Error adapter nha hang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("===> ", "Error adapter nha hang: " + e.getMessage());
        }

    }

    //T??m m?? t??? t??n lo???i nh?? h??ng ???? ch???n
    private String timMaTuTen(String _tenLoaiNH){
        for(LoaiNhaHang lnh: listLoaiNhaHang){
            if(lnh.getTenLoaiNH().equalsIgnoreCase(_tenLoaiNH)) return lnh.getMaLoaiNH();
        }
        return "";
    }

    //T??m m?? t??? t??n lo???i nh?? h??ng ???? ch???n
    private String timMaTKTuSDT(String _sdt){
        for(TaiKhoan tk: listTaiKhoan){
            if(tk.getSDT().equalsIgnoreCase(_sdt)) return tk.getMaTK();
        }
        return "";
    }

    //
    public void getAllMonAn(){
        listMonAn = new ArrayList<>();

        final CollectionReference reference = db.collection("MONANNH");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        MonAnNH monAnNH;

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
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("===> ", "getAllDanhGia " + e.getMessage());
                }
            }
        });
    }


    // L???y danh s??ch gi??? h??ng chi ti???t t??? Firebase xu???ng
    public void getAllGioHangCT(){
        listGioHangCT = new ArrayList<>();

        final CollectionReference reference = db.collection("GIOHANGCT");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        GioHangCT gioHangCT;
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot) {
                            String maMA = doc.get("MaMA").toString();
                            String maGHCT = doc.get("MaGHCT").toString();
                            String maGH = doc.get("MaGH").toString();
                            int soLuong = Integer.parseInt(doc.get("SoLuong").toString());
                            String tenMonThem = doc.get("TenMonThem").toString();
                            String thoiGian = doc.get("ThoiGian").toString();
                            int trangThai = Integer.parseInt(doc.get("TrangThai").toString());

                            if(trangThai == 1) {
                                gioHangCT = new GioHangCT(maGH, maGHCT, maMA, "", soLuong, 0, "", tenMonThem, thoiGian, trangThai, "", false, 0);
                                listGioHangCT.add(gioHangCT);
                            }
                        }

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), "Error getAllGioHangCT"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("=====>", "getAllGioHangChiTiet" + e.getMessage());
                }
            }
        });
    }

    //Dialog th??m lo???i nh?? h??ng
    private void dialog_themLoaiNH(int positon){
        dialogThemLoaiNH =  new Dialog(getContext());
        dialogThemLoaiNH.setContentView(R.layout.dialog_themloainh);

        dialogThemLoaiNH.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.6);
        dialogThemLoaiNH.getWindow().setLayout(width,height);

        EditText edtTenLoai = dialogThemLoaiNH.findViewById(R.id.edt_dialogThemTenLoaiNH);
        imvHinhLoai = dialogThemLoaiNH.findViewById(R.id.imv_dialogThemHinhLoaiNH);
        TextView tvHuyThem = dialogThemLoaiNH.findViewById(R.id.tv_dialogHuyThemLoaiNH);
        TextView tvXacNhan = dialogThemLoaiNH.findViewById(R.id.tv_dialogXacNhanThemLoaiNH);

        imvHinhLoai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent lib = new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("image/*");

                Intent chua = Intent.createChooser(cam, "Ch???n");
                chua.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{lib});

                startActivityForResult(chua, 999);
//                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(gallery, GALEERY_REQUEST_CODE);
            }
        });


        tvXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenLoai = edtTenLoai.getText().toString().trim();

                if(tenLoai.isEmpty()){
                    Toast.makeText(getContext(), "Vui l??ng nh???p t??n lo???i nh?? h??ng", Toast.LENGTH_SHORT).show();
                }else if(!kiemKhoangTrang(tenLoai)){
                    Toast.makeText(getContext(), "Kh??ng ???????c nh???p kho???ng tr???ng", Toast.LENGTH_SHORT).show();
                }else if(kiemTraTrungTenLoaiNH(tenLoai)){
                    Toast.makeText(getContext(), "???? t???n t???i t??n lo???i nh?? h??ng, vui l??ng nh???p t??n kh??c", Toast.LENGTH_SHORT).show();
                }else if(tenLoai.length() < 4 || tenLoai.length() >15){
                    Toast.makeText(getContext(), "T??n lo???i qu?? d??i ho???c qu?? ng???n", Toast.LENGTH_SHORT).show();
                }else{

                    UUID uuid = UUID.randomUUID();
                    String maLoai = String.valueOf(uuid);

                    loaiNhaHang = new LoaiNhaHang(maLoai, tenLoai, "");

                    uploadImageToFirebase(imageFileName, contenUri);
                }
            }
        });

        tvHuyThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogThemLoaiNH.dismiss();
            }
        });

        dialogThemLoaiNH.show();
    }

    //Ki???m tra t??n lo???i nh?? h??ng ???? th??m ch??a, n???u ???? c?? tr??? v??? true
    private Boolean kiemTraTrungTenLoaiNH(String _tenLoaiNH){
        for(LoaiNhaHang lnh: listLoaiNhaHang){
            if(lnh.getTenLoaiNH().equalsIgnoreCase(_tenLoaiNH)) return true;
        }
        return false;
    }


    //Dialog th??m nh?? h??ng
    private void dialog_themNH(int positon){
        dialogThemNH =  new Dialog(getContext());
        dialogThemNH.setContentView(R.layout.dialog_themnhahang);

        dialogThemNH.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        dialogThemNH.getWindow().setLayout(width,height);

        EditText edtPhiChuyenNh = dialogThemNH.findViewById(R.id.ed_dialogPhiVCThemNH);
        EditText edtTenNH = dialogThemNH.findViewById(R.id.ed_dialogTenNHThemNH);
        EditText edtThoiGian = dialogThemNH.findViewById(R.id.ed_dialogThoiGianGiaoThemNH);
        EditText edtSDT = dialogThemNH.findViewById(R.id.ed_dialogSDTThemNH);
        spMaLoaiNH = dialogThemNH.findViewById(R.id.sp_dialogMaLoaiNHThemNH);
//        spMaTK = dialogThemNH.findViewById(R.id.sp_dialogMaTKThemNH);
        imvHinh = dialogThemNH.findViewById(R.id.imv_dialogHinhThemNH);
        TextView tvHuyThem = dialogThemNH.findViewById(R.id.tv_dialogHuyThemNH);
        TextView tvXacNhan = dialogThemNH.findViewById(R.id.tv_dialogXacNhanThemNH);

        //L???y danh s??ch m?? lo???i nh?? h??ng l??n spinner
        getMaLoaiLoaiNHToSpiner(0);
        //L???y danh s??ch m?? t??i kho???n l?? spinner
//        getMaLoaiTKToSpiner(0);

        imvHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent lib = new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("image/*");

                Intent chua = Intent.createChooser(cam, "Ch???n");
                chua.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{lib});

                startActivityForResult(chua, 888);
                //Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(gallery, GALEERY_REQUEST_CODE);
            }
        });

        tvXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenNH = edtTenNH.getText().toString().trim();
                String thoiGian = edtThoiGian.getText().toString().trim();
                String phiVanChuyen = edtPhiChuyenNh.getText().toString().trim();
                String sdt = edtSDT.getText().toString().trim();

                if(!kiemLoiONhap(tenNH, thoiGian, phiVanChuyen, sdt).isEmpty()){
                    Toast.makeText(getContext(), kiemLoiONhap(tenNH, thoiGian, phiVanChuyen, sdt), Toast.LENGTH_SHORT).show();
                }else if(!kiemTraTonTaiTK(sdt)){
                    Toast.makeText(getContext(), "S??? ??i???n tho???i n??y ch??a ???????c ????ng k?? t??i kho???n", Toast.LENGTH_SHORT).show();
                }else{
                    UUID uuid = UUID.randomUUID();
                    String maNH = String.valueOf(uuid);

                    int ship = Integer.parseInt(phiVanChuyen);
                    //Th??m ????nh gi?? v?? th??m nh?? h??ng l??n Firebase
                    themDanhGiaNHToFireStore(maNH, tenNH, thoiGian, ship, sdt);

                }
            }
        });

        tvHuyThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogThemNH.dismiss();
            }
        });

        dialogThemNH.show();
    }

    //Ki???m tra _sdt ???? c?? trong DB ch??a
    private Boolean kiemTraTonTaiTK(String _sdt){
        for(TaiKhoan tk: listTaiKhoan){
            if(tk.getSDT().equals(_sdt)) return true;
        }
        return false;
    }

    //L???y sdt t??? m?? t??i kho???n
    private String getSDTTuMaTK(String _maTK){
        Toast.makeText(getContext(), listTaiKhoan.size()+"", Toast.LENGTH_SHORT).show();
        for(TaiKhoan tk: listTaiKhoan){
            if(tk.getMaTK().equals(_maTK)) return tk.getSDT();
        }
        return "";
    }

    private String kiemLoiONhap(String tenNh, String thoiGian, String phiVC, String _sdt){
        String loi = "";
        try {
            String pattern = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
            if(_sdt.isEmpty()) {
                loi += "\nB???n ch??a nh???p s??? ??i???n tho???i";
            }else if(!_sdt.matches(pattern)){
                loi += "\nS??? ??i???n tho???i kh??ng ????ng ?????nh d???ng";
            }

            if (tenNh.isEmpty()) loi += "\nB???n ch??a nh???p t??n nh?? h??ng";
            else if (!kiemKhoangTrang(tenNh))
                loi += "\nT??n nh?? h??ng kh??ng ???????c nh???p kho???ng tr???ng";

            if(tenNh.length() < 5) loi += "\nT??n nh?? h??ng qu?? ng???n";

            if (thoiGian.isEmpty()) loi += "\nB???n ch??a nh???p th???i gian giao h??ng";
            else if (!kiemKhoangTrang(thoiGian))
                loi += "\nTh???i gian kh??ng ???????c nh???p kho???ng tr???ng";

            int _thoiGian = Integer.parseInt(thoiGian);
            if (_thoiGian <= 1) loi += "\nTh???i gian giao h??ng ph???i l???n h??n m???t";
            else if(_thoiGian > 240) loi += "\n Th???i gian giao h??ng kh??ng ???????c qu?? 240 ph??t" ;

            int _phiVC = Integer.parseInt(phiVC);
            if (phiVC.isEmpty()) loi += "\nB???n ch??a nh???p ph?? v???n chuy???n";
            else if(_phiVC > 2000000) loi += "\n Ph?? v???n chuy???n kh??ng ???????c v?????t qu?? 2,000,000 VND";

        }catch (Exception e){
            loi += "\n" + e.getMessage();
        }
        return loi;

    }

    //Ki???m tra ?? nh???p c?? nh???p to??n kho???ng tr???ng ko
    private Boolean kiemKhoangTrang(String _duLieu){
        for (int i = 0; i < _duLieu.length(); i++) {
            if(!Character.isWhitespace(_duLieu.charAt(i))){
                return true;
            }
        }
        return false;
    }

    //////////////// Spinner m?? lo???i nh?? h??ng

    private void getMaLoaiLoaiNHToSpiner(int chucNang){

//        listMaLoaiNH = new ArrayList<>();
//
//        for(LoaiNhaHang lnh: listLoaiNhaHang){
//            listMaLoaiNH.add(lnh.getMaLoaiNH());
//        }

        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, listMaLoaiNH);

            // Layout for All ROWs of Spinner.  (Optional for ArrayAdapter).
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (chucNang == 0) {
                spMaLoaiNH.setAdapter(adapter);
            } else {
                spMaLoaiNHSuaNH.setAdapter(adapter);
            }
        }catch (Exception e){

        }
    }



    //////////////// Spinner m?? t??i kho???n

    private void getMaLoaiTKToSpiner(int chucNang){

        listMaTK = new ArrayList<>();
        listTaiKhoan = new ArrayList<>();

        final CollectionReference reference = db.collection("TAIKHOAN");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        TaiKhoan taiKhoan;
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String MaTK = doc.get("MaTK").toString();
                            String _quyen = doc.get("Quyen").toString();
                            String diaChi = doc.get("DiaChi").toString();
                            String hinhAnh = doc.get("HinhAnh").toString();
                            String soDT = doc.get("SDT").toString();
                            int soDu = Integer.parseInt(doc.get("SoDu").toString());
                            String matKhau = doc.get("MatKhau").toString();
                            int quyen = Integer.parseInt(doc.get("Quyen").toString());

                            taiKhoan = new TaiKhoan(MaTK, "", matKhau, soDT, diaChi, quyen, hinhAnh, soDu);
                            listTaiKhoan.add(taiKhoan);

                            //Kh??ng cho admin l??m ch??? h??ng h??ng
                            if(!_quyen.equals("0")) listMaTK.add(soDT);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                                android.R.layout.simple_list_item_1, listMaTK);

                        // Layout for All ROWs of Spinner.  (Optional for ArrayAdapter).
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//                        if(chucNang == 0) {
//                            spMaTK.setAdapter(adapter);
//                        }else{
//                            spMaTKSuaNH.setAdapter(adapter);
//                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), "Error getMaLoaiToSpinner"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    //Dialog s???a nh?? h??ng
    public void dialog_suaNH(int positon){
        dialogSuaNH =  new Dialog(getContext());
        dialogSuaNH.setContentView(R.layout.dialog_suanhahang);

        dialogSuaNH.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getContext().getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getContext().getResources().getDisplayMetrics().heightPixels*0.8);
        dialogSuaNH.getWindow().setLayout(width,height);

        EditText edtPhiChuyenNh = dialogSuaNH.findViewById(R.id.ed_dialogPhiVCSuaNH);
        EditText edtTenNH = dialogSuaNH.findViewById(R.id.ed_dialogTenNHSuaNH);
        EditText edtThoiGian = dialogSuaNH.findViewById(R.id.ed_dialogThoiGianGiaoSuaNH);
        spMaLoaiNHSuaNH = dialogSuaNH.findViewById(R.id.sp_dialogMaLoaiNHSuaNH);
//        spMaTKSuaNH = dialogSuaNH.findViewById(R.id.sp_dialogMaTKSuaNH);
        EditText edtSdt = dialogSuaNH.findViewById(R.id.ed_dialogSDTSuaNH);
        imvHinhSuaNH = dialogSuaNH.findViewById(R.id.imv_dialogHinhSuaNH);
        TextView tvHuyThem = dialogSuaNH.findViewById(R.id.tv_dialogHuySuaNH);
        TextView tvXacNhan = dialogSuaNH.findViewById(R.id.tv_dialogXacNhanSuaNH);
        imvHinhSuaNH = dialogSuaNH.findViewById(R.id.imv_dialogHinhSuaNH);

        //L???y danh s??ch m?? t??i kho???n l?? spinner
//        getMaLoaiTKToSpiner(1);

        // ?????y th??ng tin l??n dialog s???a nh?? h??ng
        edtPhiChuyenNh.setText(listNhaHangTheoLoai.get(positon).getPhiVanChuyen()+"");
        edtTenNH.setText(listNhaHangTheoLoai.get(positon).getTenNH());
        edtThoiGian.setText(listNhaHangTheoLoai.get(positon).getThoiGian());
        String sdt = getSDTTuMaTK(listNhaHangTheoLoai.get(positon).getMaTK());
        edtSdt.setText(sdt);


        //L???y danh s??ch m?? lo???i nh?? h??ng l??n spinner
        getMaLoaiLoaiNHToSpiner(1);

        if(listNhaHangTheoLoai.get(positon).getHinhAnh().isEmpty()){
            imvHinhSuaNH.setImageResource(R.drawable.im_food);
        }else{
            Picasso.with(getContext()).load(listNhaHangTheoLoai.get(positon).getHinhAnh()).resize(2048, 1600).centerCrop().onlyScaleDown().into(imvHinhSuaNH);
        }
        //L??u ???????ng d???n h??nh ???nh
        imageFileName = listNhaHangTheoLoai.get(positon).getHinhAnh();

        tvHuyThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSuaNH.dismiss();
            }
        });

        imvHinhSuaNH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent lib = new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("image/*");

                Intent chua = Intent.createChooser(cam, "Ch???n");
                chua.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{lib});

                startActivityForResult(chua, 777);
                //Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(gallery, GALEERY_REQUEST_CODE);
            }
        });

        tvXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenNH = edtTenNH.getText().toString().trim();
                String thoiGian = edtThoiGian.getText().toString().trim();
                String phiVanChuyen = edtPhiChuyenNh.getText().toString().trim();
                String sdt = edtSdt.getText().toString().trim();

                if(!kiemLoiONhap(tenNH, thoiGian, phiVanChuyen, sdt).isEmpty()){
                    Toast.makeText(getContext(), kiemLoiONhap(tenNH, thoiGian, phiVanChuyen, sdt), Toast.LENGTH_SHORT).show();
                }else if(!kiemTraTonTaiTK(sdt)){
                    Toast.makeText(getContext(), "S??? ??i???n tho???i n??y ch??a ???????c ????ng k?? t??i kho???n", Toast.LENGTH_SHORT).show();
                }else{
                    //Th??m ????nh gi?? v?? th??m nh?? h??ng l??n Firebase
                    String maNH = listNhaHangTheoLoai.get(positon).getMaNH();
                    String maDG = listNhaHangTheoLoai.get(positon).getMaDG();
                    String maYT = listNhaHangTheoLoai.get(positon).getMaYT();
                    Double danhGia = listNhaHangTheoLoai.get(positon).getDanhGia();

                    String maLNH = timMaTuTen(spMaLoaiNHSuaNH.getSelectedItem().toString());
                    String maTK = timMaTKTuSDT(sdt);

                    nhaHang  = new NhaHang(maNH, maLNH, maTK, tenNH, thoiGian, Integer.parseInt(phiVanChuyen), "", danhGia, maDG, maYT);
                    //?????y h??nh ???nh l??n firebase sau ???? c???p nh???t t???t c??? d?? li???u l??n Firebase
                    uploadImageNHToFirebase(imageFileName, contenUri, 1); // S??? 0 l?? th??m nh?? h??ng, s??? 1 l?? s???a nh?? h??ng
                    dialogSuaNH.dismiss();
                    Toast.makeText(getContext(), "S???a nh?? h??ng th??nh c??ng", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogSuaNH.show();
    }

    // Nh???n y??u th??ch nh?? h??ng
    public void press_favorite(int position){
        Random random =  new Random();
        int x = random.nextInt((10000-1+1)+1);
        String maYT = "YT" + x;

        Intent intent = getActivity().getIntent();
        String maTK = intent.getStringExtra("MaTK");

        String maNH = listNhaHangTheoLoai.get(position).getMaNH();

        themYeuThichNHToFireStore(new YeuThich(maNH, maTK, maYT));
    }

    // b??? ch???n y??u th??ch nh?? h??ng
    public void unpress_favorite(int position){
        db.collection("YEUTHICH").document(listNhaHangTheoLoai.get(position).getMaYT() + "")
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // L???y danh y??u th??ch
                getAllYeuThich(getContext());

                //L???y danh s??ch ????nh gi?? xu???ng
                getAllDanhGia(getContext());
            }
        });
    }

    //T??m ki???m nh?? h??ng

    private void search(){
        try {
            svNhaHang.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    try {
                        listNHSearch = new ArrayList<>();

                        for (NhaHang nh : listNhaHangTheoLoai) {
                            String tenNh = nh.getTenNH();

                            if (tenNh.contains(query)) {
                                listNHSearch.add(nh);
                            }
                        }

                        getNhaHangSearch(listNHSearch);
                    }catch (Exception e){ Toast.makeText(getContext(), "L???i: ch??a c?? d??? li???u", Toast.LENGTH_SHORT).show();}
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    try {
                        listNHSearch = new ArrayList<>();

                        for (NhaHang nh : listNhaHangTheoLoai) {
                            String tenNh = nh.getTenNH();

                            if (tenNh.contains(newText)) {
                                listNHSearch.add(nh);
                            }
                        }

                        getNhaHangSearch(listNHSearch);
                    }catch (Exception e){ Toast.makeText(getContext(), "L???i: ch??a c?? d??? li???u", Toast.LENGTH_SHORT).show();}
                    return false;
                }
            });
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getNhaHangSearch(List<NhaHang> list){
        NhaHangAdapter adapter  = new NhaHangAdapter(list, getContext(), this);
        rcv_nhahang.setLayoutManager(new LinearLayoutManager(getContext()));
        rcv_nhahang.setAdapter(adapter);
    }



    // X??? l?? s??? ki???n load h??nh l??n ImaveView
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == GALEERY_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                contenUri = data.getData();
//                String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
//                imvHinhLoai.setImageURI(contenUri);
//            }
//        }


        //X??? l?? th??m ???nh l??n imageview ???nh lo???i nh?? h??ng
        if (requestCode == 999 && resultCode == RESULT_OK){
            contenUri = data.getData();
            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
            if (data.getExtras() != null){
                Bundle caigio = data.getExtras();
                Bitmap bitmap = (Bitmap) caigio.get("data");
                imvHinhLoai.setImageBitmap(bitmap);
            }else{
                imvHinhLoai.setImageURI(contenUri);
            }
        }

        //X??? l?? th??m ???nh l??n imageview ???nh dialog th??m  nh?? h??ng
        if (requestCode == 888 && resultCode == RESULT_OK){
            contenUri = data.getData();
            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
            if (data.getExtras() != null){
                Bundle caigio = data.getExtras();
                Bitmap bitmap = (Bitmap) caigio.get("data");
                imvHinh.setImageBitmap(bitmap);
            }else{
                imvHinh.setImageURI(contenUri);
            }
        }

        //X??? l?? th??m ???nh l??n imageview ???nh  dialog s???a th??ng tin nh?? h??ng
        if (requestCode == 777 && resultCode == RESULT_OK){
            contenUri = data.getData();
            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
            if (data.getExtras() != null){
                Bundle caigio = data.getExtras();
                Bitmap bitmap = (Bitmap) caigio.get("data");
                imvHinhSuaNH.setImageBitmap(bitmap);
            }else{
                imvHinhSuaNH.setImageURI(contenUri);
            }
        }


    }

    private  String getFileExt(Uri uri){
        ContentResolver c = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(uri));
    }


    //Load h??nh l??n folder h??nh ???nh c???a  nh?? h??ng
    private void uploadImageNHToFirebase(String name, Uri contentUri, int congViec){
        StorageReference image = storageReference.child("IM_NHAHANG/"+name);
        try {
            image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Log.d("==> Done", " Load h??nh ???nh l??n Firebase th??nh c??ng "+ uri.toString());
                            // Th??m nh?? h??ng l??n firebase
                            nhaHang.setHinhAnh(uri.toString());
                            if(congViec == 0) {
                                themNHToFireStore();
                            }else{
                                updateFirebase(nhaHang);
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("==> Exception", e.getMessage());
                }
            });
        }catch (Exception e){
            if(congViec == 0) {
//                themNHToFireStore();
                Toast.makeText(getContext(), "B???n ch??a ch???n h??nh", Toast.LENGTH_SHORT).show();
            }else{
                nhaHang.setHinhAnh(imageFileName);
                updateFirebase(nhaHang);
            }
        }
    }

    //Load h??nh l??n folder h??nh ???nh c???a lo???i nh?? h??ng
    private void uploadImageToFirebase(String name, Uri contentUri){
        StorageReference image = storageReference.child("IM_LOAINHAHANG/"+name);
        try {
            image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Log.d("==> Done", " Load h??nh ???nh l??n Firebase th??nh c??ng "+ uri.toString());
                            // Th??m nh?? h??ng l??n firebase
                            loaiNhaHang.setHinhAnh(uri.toString());
                            themLoaiNHToFireStore(loaiNhaHang);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("==> Exception", e.getMessage());
                }
            });
        }catch (Exception e){
//            loaiNhaHang.setHinhAnh("");
//            themLoaiNHToFireStore(loaiNhaHang);
            Toast.makeText(getContext(), "Ch??a t???i ???nh l??n", Toast.LENGTH_SHORT).show();
        }
    }


    // C???p nh???p th??ng tin b???ng nh?? h??ng l??n Firebase
    private void updateFirebase(NhaHang nhaHang){
        final CollectionReference reference = db.collection("NHAHANG");
        try {
            Map map = new HashMap<String, Object>();
            map.put("MaNH", nhaHang.getMaNH());
            map.put("MaLoaiNH", nhaHang.getMaLoaiNH());
            map.put("MaTK", nhaHang.getMaTK());
            map.put("TenNH", nhaHang.getTenNH());
            map.put("ThoiGian", nhaHang.getThoiGian());
            map.put("PhiVanChuyen", nhaHang.getPhiVanChuyen());
            map.put("HinhAnh", nhaHang.getHinhAnh());
            map.put("MaDG", nhaHang.getMaDG());
            map.put("MaYT", nhaHang.getMaYT());
            reference.document(nhaHang.getMaNH() + "").set(map, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //C???p nh???t l???i listView
                    getAllNhaHang(getContext());
//                    getAllDanhGia(getContext());
                }
            });

        }catch (Exception e){
            Toast.makeText(getContext(), "Error update Firebase: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    //?????y nh?? h??ng l??n Firestore
    private void themLoaiNHToFireStore(LoaiNhaHang loaiNhaHang){
        final CollectionReference collectionReference = db.collection("LOAINHAHANG");

        Map<String, Object> data = new HashMap<>();
        data.put("MaLoaiNH", loaiNhaHang.getMaLoaiNH());
        data.put("TenLoaiNH", loaiNhaHang.getTenLoaiNH());
        data.put("HinhAnh", loaiNhaHang.getHinhAnh());

        try {
            collectionReference.document(loaiNhaHang.getMaLoaiNH() + "").set(data);
            dialogThemLoaiNH.dismiss();
            Toast.makeText(getContext(), "Th??m lo???i nh?? h??ng th??nh c??ng", Toast.LENGTH_SHORT).show();

            // L???y danh y??u th??ch
            getAllYeuThich(getContext());

            //L???y danh s??ch m??n ??n
            getAllMonAn();

            //L???y danh s??ch gi??? h??ng t??? firebase
            getAllGioHangCT();

            //L???y danh s??ch ????nh gi?? xu???ng
            getAllDanhGia(getContext());
        }catch (Exception e){
            Log.d("Error_addTKFirebase", e.getMessage());
        }
    }

    // Th??m b???ng ????nh gi?? khi th??m nh?? h??ng m???i
    private void themDanhGiaNHToFireStore(String maNH, String tenNH, String thoiGian, int phiVanChuyen, String _sdt){
        final CollectionReference collectionReference = db.collection("DANHGIANH");

        UUID uuid = UUID.randomUUID();
        String MaDG = String.valueOf(uuid);

        Map<String, Object> data = new HashMap<>();
        data.put("MaDanhGia", MaDG);
        data.put("MaNH", maNH);
        data.put("TongDG", 0);
        data.put("LuotDG", 0);

        try {
            collectionReference.document(MaDG).set(data);

            String maLNH = timMaTuTen(spMaLoaiNH.getSelectedItem().toString());
            String maTK = timMaTKTuSDT(_sdt);

            nhaHang = new NhaHang(maNH, maLNH, maTK, tenNH, thoiGian, phiVanChuyen, imageFileName, 0.0, MaDG, "");
            uploadImageNHToFirebase(imageFileName, contenUri, 0); // S??? 0 l?? th??m nh?? h??ng, s??? 1 l?? s???a nh?? h??ng
        }catch (Exception e){
            Log.d("Error_addTKFirebase", e.getMessage());
        }
    }

    // Th??m object nh?? h??ng l??n Firebase
    private void themNHToFireStore(){

        final CollectionReference collectionReference = db.collection("NHAHANG");

        Map<String, Object> data = new HashMap<>();
        data.put("MaLoaiNH", nhaHang.getMaLoaiNH());
        data.put("MaTK", nhaHang.getMaTK());
        data.put("HinhAnh", nhaHang.getHinhAnh());
        data.put("MaNH", nhaHang.getMaNH());
        data.put("PhiVanChuyen", nhaHang.getPhiVanChuyen());
        data.put("ThoiGian", nhaHang.getThoiGian());
        data.put("TenNH", nhaHang.getTenNH());
        data.put("MaDG", nhaHang.getMaDG());


        try {
            //Cho t??i kho???n th??nh quy???n ch??? nh?? h??ng
            db.collection("TAIKHOAN").document(nhaHang.getMaTK())
                    .update(
                            "Quyen", 1
                    );

            collectionReference.document(nhaHang.getMaNH()).set(data);
            dialogThemNH.dismiss();
            Toast.makeText(getContext(), "Th??m nh?? h??ng th??nh c??ng", Toast.LENGTH_SHORT).show();
            getAllNhaHang(getContext());
        }catch (Exception e){
            Log.d("Error_addTKFirebase", e.getMessage());
        }
    }


    // Th??m object nh?? h??ng l??n Firebase
    private void themYeuThichNHToFireStore(YeuThich yt){
        final CollectionReference collectionReference = db.collection("YEUTHICH");

        Map<String, Object> data = new HashMap<>();
        data.put("MaNH", yt.getMaNH());
        data.put("MaTK", yt.getMaTK());
        data.put("MaYT", yt.getMaYT());

        try {
            collectionReference.document(yt.getMaYT() + "").set(data);

            // L???y danh y??u th??ch
            getAllYeuThich(getContext());

            //L???y danh s??ch ????nh gi?? xu???ng
            getAllDanhGia(getContext());
        }catch (Exception e){
            Log.d("Error_addTKFirebase", e.getMessage());
        }
    }

    //Ki???m tra m??n ??n ???? ???????c mua ch??a, n???u chua th??
    private Boolean KiemTraMonAnTrongGioHang(String _maNH){
        for(MonAnNH ma: listMonAn){
            if(ma.getMaNH().equals(_maNH)){
                for(GioHangCT gh: listGioHangCT){
                    if(ma.getMaMA().equals(gh.getMaMA())){
                        return true; // c?? m??n ??n trong gi??? h??ng, kh??ng ??c x??a
                    }
                }
            }
        }
        return false;// ???????c x??a
    }

    // Delete nh?? h??ng
    private void deleteNhaHangFireBase(int positon){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Th??ng b??o")
                .setMessage("B???n ch???n ch???n mu???n x??a nh?? h??ng kh??ng?")
                .setPositiveButton("C??", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            if(!KiemTraMonAnTrongGioHang(listNhaHangTheoLoai.get(positon).getMaNH())) {
                                try {
                                    // Delete b???ng nh?? h??ng
                                    db.collection("NHAHANG").document(listNhaHangTheoLoai.get(positon).getMaNH() + "")
                                            .delete();

                                    //Delete b???ng d??nh gi?? nh?? h??ng c?? m?? nh?? h??ng v???a x??a
                                    db.collection("DANHGIANH").document(listNhaHangTheoLoai.get(positon).getMaDG() + "")
                                            .delete();

                                    //Delete b???ng YEUTHICH c?? c??ng m?? nh?? h??ng v???a x??a
                                    db.collection("YEUTHICH").document(listNhaHangTheoLoai.get(positon).getMaYT() + "")
                                            .delete();
                                }catch (Exception e){
                                    Log.d("===> ", e.getMessage());
                                }

                                //X??a m??n ??n c???a nh?? h??ng
                                deleteMonAnNHFireBase(listNhaHangTheoLoai.get(positon).getMaNH());

                                // L???y danh y??u th??ch
                                getAllYeuThich(getContext());

                                //L???y danh s??ch m??n ??n
                                getAllMonAn();

                                //L???y danh s??ch gi??? h??ng t??? firebase
                                getAllGioHangCT();

                                //L???y danh s??ch ????nh gi?? xu???ng
                                getAllDanhGia(getContext());
                                Toast.makeText(getContext(), "X??a nh?? h??ng th??nh c??ng", Toast.LENGTH_SHORT).show();

                            }else Toast.makeText(getContext(), "M??n ??n ??ang ???????c mua, kh??ng ???????c x??a nh?? h??ng", Toast.LENGTH_SHORT).show();
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


    // Delete gi??? h??ng t??? m?? m??n ??n
    private void deleteGioHangFireBase(String _maMA){

        try {
            final CollectionReference reference = db.collection("GIOHANGCT");
            reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    try {
                        if(task.isSuccessful()){
                            QuerySnapshot snapshot = task.getResult();
                            for(QueryDocumentSnapshot doc: snapshot){
                                String maMA = doc.get("MaMA").toString();
                                String maGHCT = doc.get("MaGHCT").toString();
                                int trangThai = Integer.parseInt(doc.get("TrangThai").toString());

                                if (maMA.equals(_maMA) && trangThai == 0) {
                                    // Delete b???ng nh?? h??ng
                                    db.collection("GIOHANGCT").document(maGHCT)
                                            .delete();
                                }
                            }
                        }else{
                            Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(), "Error getAllYeuThich"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }catch (Exception e){
            Toast.makeText(getContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    // Delete m??n ??n t??? m?? nh?? h??ng
    private void deleteMonAnNHFireBase(String _maNH){
        try {
             final CollectionReference reference = db.collection("MONANNH");
                reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = task.getResult();
                                for (QueryDocumentSnapshot doc : snapshot) {
                                    String maMA = doc.get("MaMA").toString();
                                    String maNH = doc.get("MaNH").toString();

                                    if (maNH.equals(_maNH)) {
                                        // Delete b???ng nh?? h??ng
                                        db.collection("MONANNH").document(maMA)
                                                .delete();


                                        deleteGioHangFireBase(maMA);
                                    }
                                }

                            } else {
                                Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error getAllYeuThich" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }catch (Exception e){
            Toast.makeText(getContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}