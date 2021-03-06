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

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.myapplication.Adapter.MonAnAdapter;
import com.example.myapplication.Model.GioHangCT;
import com.example.myapplication.Model.MenuNH;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class MonAnFragment extends Fragment {
    private GridView gv_MonAn;
    private FloatingActionButton flBtnThemMA;
    private SearchView svMonAn;

    private TextView tv_TenNhaHangMA, tv_PhiVanChuyenMA, tv_ThoiGianMA, tv_DanhGiaMA;
    private ImageView imv_HinhNenMA, imv_TroVe, imv_danhGiaNH;

    private ImageView imv_ThemHinhMA, imv_SuaHinhMA;
    private TextView tv_dialogSuaMaNH, tv_dialogThemMaNH;
    private Spinner sp_ThemMaMenuNH, sp_dialogSuaMaMenuNH;
    private Dialog dialogThemMonAn, dialogSuaMonAn;

    private List<MonAnNH> listMonAn;
    private List<MonAnNH> listMonAnTimKiem;
    private List<String> listMaNH, listMaMenuNH;
    private List<GioHangCT> listGioHangCT;

    private MonAnNH monAnNH;

    private String thoiGian;
    private Double danhGia;
    private String hinhAnhNH;
    private int phiVanChuyen;
    private String _hinhAnh;
//    private String maDanhGia;

    private Dialog dialogDanhGiaNH;
    private String MaDanhGia = "";

    private String _maNH, _tenNH;
    public String maTKDangNhap, maTKChuNH;
    public int quyenTKDN;

    //Firestore
    private FirebaseFirestore db;
    //Image firebase
    private StorageReference storageReference;


    //Load image
    Uri contenUri;
    String imageFileName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_mon_an, container, false);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        anhXa(view);
        timKiemMA();

        getAllMonAn(getContext()); // L???y t???t c??? m??n ??n t??? Firestore xu???ng
        getAllMaNH(getContext());
        getAllMaMenuNH(getContext());
        getAllGioHangCT();

        //Nh???n n??t th??m m??n ??n
        flBtnThemMA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogThemMonAn(0);
            }
        });
        imv_TroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_out, R.anim.fade_out, R.anim.fade_in, R.anim.slide_in)
                        .replace(R.id.nav_FrameFragment, new NhaHangFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

//        gv_MonAn.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                //  (position);
//                Toast.makeText(getContext(), "Hihi", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });


        return view;
    }

    private void anhXa(View view){
        flBtnThemMA = view.findViewById(R.id.flBtnThemMA);
        gv_MonAn = view.findViewById(R.id.gv_MonAn);
        svMonAn = view.findViewById(R.id.sv_monAn);
        tv_TenNhaHangMA = view.findViewById(R.id.tv_TenNhaHangMA);
        tv_PhiVanChuyenMA = view.findViewById(R.id.tv_PhiVanChuyenMA);
        tv_ThoiGianMA = view.findViewById(R.id.tv_ThoiGianMA);
        tv_DanhGiaMA  = view.findViewById(R.id.tv_DanhGiaMA);
        imv_HinhNenMA  = view.findViewById(R.id.imv_HinhNenMA);
        imv_TroVe  = view.findViewById(R.id.imv_TroVe);
        imv_danhGiaNH = view.findViewById(R.id.imv_danhGiaNHMACT);

        //l???y d??? li???u t??? fragment nh?? h??ng
        Bundle bundle = this.getArguments();
        String tenNhaHang = bundle.getString("TenNH");
        hinhAnhNH = bundle.getString("HinhAnh");
        thoiGian = bundle.getString("ThoiGian");
        danhGia = bundle.getDouble("DanhGia");
        phiVanChuyen = bundle.getInt("PhiVanChuyen");
        MaDanhGia = bundle.getString("MaDanhGia");
        maTKChuNH = bundle.getString("MaTK");

        Intent intent = getActivity().getIntent();
        maTKDangNhap = intent.getStringExtra("MaTK");
        quyenTKDN = intent.getIntExtra("Quyen", 2);

        if(quyenTKDN == 2){
            flBtnThemMA.setVisibility(View.INVISIBLE);
        }else if(!maTKDangNhap.equals(maTKChuNH) && quyenTKDN != 0){
            flBtnThemMA.setVisibility(View.INVISIBLE);
        }

        tv_TenNhaHangMA.setText(tenNhaHang);
        tv_PhiVanChuyenMA.setText(formatNumber(phiVanChuyen) + " VND");
        tv_ThoiGianMA.setText(thoiGian + " m");
        tv_DanhGiaMA.setText(danhGia + "");
        if(hinhAnhNH.isEmpty()){
            imv_HinhNenMA.setImageResource(R.drawable.im_food);
        }else{
            Picasso.with(getContext()).load(hinhAnhNH).into(imv_HinhNenMA);
        }

        imv_danhGiaNH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_danhGiaNH();
            }
        });
    }



    //Dialog g???i ????nh gi??
    private void dialog_danhGiaNH(){
        dialogDanhGiaNH =  new Dialog(getContext());
        dialogDanhGiaNH.setContentView(R.layout.dialog_danhgianh);

        dialogDanhGiaNH.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.6);
        dialogDanhGiaNH.getWindow().setLayout(width,height);

        ToggleButton tg1 = dialogDanhGiaNH.findViewById(R.id.tg_danhGia1);
        ToggleButton tg2 = dialogDanhGiaNH.findViewById(R.id.tg_danhGia2);
        ToggleButton tg3 = dialogDanhGiaNH.findViewById(R.id.tg_danhGia3);
        ToggleButton tg4 = dialogDanhGiaNH.findViewById(R.id.tg_danhGia4);
        ToggleButton tg5 = dialogDanhGiaNH.findViewById(R.id.tg_danhGia5);
        Button btnGuiDanhGia = dialogDanhGiaNH.findViewById(R.id.btn_dialogGuiDanhGiaNH);

        btnGuiDanhGia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int danhGia = 0;
//                if(tg1.isChecked()) danhGia+=1;
//                if(tg2.isChecked()) danhGia+=1;
//                if(tg3.isChecked()) danhGia+=1;
//                if(tg4.isChecked()) danhGia+=1;
//                if(tg5.isChecked()) danhGia+=1;

                if(danhGia != 0) {
                    db.collection("DANHGIANH").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot snapshot = task.getResult();
                                for (QueryDocumentSnapshot doc : snapshot) {
                                    String maDG = doc.get("MaDanhGia").toString();
                                    int tongDG = Integer.parseInt(doc.get("TongDG").toString());
                                    int LuotDG = Integer.parseInt(doc.get("LuotDG").toString());

//                                Log.d("==> ", MaDanhGia);
                                    if (maDG.equals(MaDanhGia)) {
                                        int danhGia = 0;
                                        if(tg1.isChecked()) danhGia+=1;
                                        if(tg2.isChecked()) danhGia+=1;
                                        if(tg3.isChecked()) danhGia+=1;
                                        if(tg4.isChecked()) danhGia+=1;
                                        if(tg5.isChecked()) danhGia+=1;

                                        updateDanhGiaGH(tongDG, LuotDG, danhGia);
                                        break;
                                    }

                                }
                            }
                        }
                    });
                }else Toast.makeText(getContext(), "B???n ch??a ch???n ????nh gi??", Toast.LENGTH_SHORT).show();
            }
        });

        dialogDanhGiaNH.show();
    }

    //C???p nh???t l???i ????nh gi?? gi??? h??ng
    public void updateDanhGiaGH(int _tongDG, int _luotDG, int _danhGia){
        db.collection("DANHGIANH").document(MaDanhGia)
                .update(
                        "TongDG" , _tongDG + _danhGia,
                        "LuotDG" , _luotDG + 1
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "C???m ??n b???n ???? ????nh gi??", Toast.LENGTH_SHORT).show();
                dialogDanhGiaNH.dismiss();
            }
        });

    }

    //
    public void getAllMonAn(Context context){
        listMonAn = new ArrayList<>();

        Bundle bundle = this.getArguments();
        _maNH = bundle.getString("MaNH");
        _tenNH = bundle.getString("TenNH");

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
                        goiAdapter();
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d("===> ", "getAllMonAN " + e.getMessage());
                }
            }
        });
    }


        //add d??? li???u m?? nh?? h??ng v??o listMaNH
    public void getAllMaNH(Context context){
        listMaNH = new ArrayList<>();

        final CollectionReference reference = db.collection("NHAHANG");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String maNH = doc.get("MaNH").toString();

                            listMaNH.add(maNH);
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d("=====> ", "getAllMaNH: " + e.getMessage());
                }
            }
        });
    }

    //add d??? li???u m?? menu nh?? h??ng v??o listMaMenuNH
    public void getAllMaMenuNH(Context context){
        listMaMenuNH = new ArrayList<>();

        final CollectionReference reference = db.collection("MENUNH");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String maMenuNH = doc.get("MaMenuNH").toString();

                            listMaMenuNH.add(maMenuNH);
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d("=====> ", "getAllMenuNH: " + e.getMessage());
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
                    Log.d("===> ", "getAllGioHangCT " + e.getMessage());
                }
            }
        });
    }

    //truy???n d??? li???u c???a fragment m??n ??n v??o fragment m??n ??n chi ti???t
    public void BundleFragmentMonAnCT(String _maMA, String _tenMon, int _gia, String _chiTiet, String _hinhMA){
        Bundle bundle = new Bundle();
        bundle.putString("MaMA", _maMA);
        bundle.putString("TenMon", _tenMon);
        bundle.putString("HinhAnh", _hinhMA);
        bundle.putInt("Gia", _gia);
        bundle.putString("ChiTiet", _chiTiet);
        bundle.putInt("PhiVanChuyen", phiVanChuyen);
        bundle.putString("ThoiGian", thoiGian);
        bundle.putDouble("DanhGia", danhGia);
        bundle.putString("MaNH", _maNH);
        bundle.putString("TenNH", _tenNH);
        bundle.putString("HinhAnhNH", hinhAnhNH);
//        bundle.putString("MaDanhGia", maDanhGia);
        MonAnCTFragment monAnCTFragment = new MonAnCTFragment();
        monAnCTFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.nav_FrameFragment, monAnCTFragment)
                .addToBackStack(null)
                .commit();
    }

    //g???i adapter m??n ??n
    private void goiAdapter(){
        MonAnAdapter adapter = new MonAnAdapter(listMonAn, getContext(), this);
        gv_MonAn.setNumColumns(2);
        gv_MonAn.setAdapter(adapter);
    }

    // ?????nh d???ng sang s??? ti???n
    private String formatNumber(int number){
        // t???o 1 NumberFormat ????? ?????nh d???ng s??? theo ti??u chu???n c???a n?????c Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }

   // t??m ki???m m??n ??n
    private void timKiemMA(){
        try {
            svMonAn.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    try {
                        listMonAnTimKiem = new ArrayList<>();

                        for (MonAnNH monAnNH : listMonAn) {
                            String tenMonAn = monAnNH.getTenMon().toLowerCase();

                            if (tenMonAn.contains(query.toLowerCase())) {
                                listMonAnTimKiem.add(monAnNH);
                            }
                        }

                        getMonAnTimKiem(listMonAnTimKiem);
                    }catch (Exception e){ Toast.makeText(getContext(), "L???i: ch??a c?? d??? li???u", Toast.LENGTH_SHORT).show();}
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    try {
                        listMonAnTimKiem = new ArrayList<>();

                        for (MonAnNH monAnNH : listMonAn) {
                            String tenMonAn = monAnNH.getTenMon().toLowerCase();

                            if (tenMonAn.contains(newText.toLowerCase())) {
                                listMonAnTimKiem.add(monAnNH);
                            }
                        }

                        getMonAnTimKiem(listMonAnTimKiem);
                    }catch (Exception e){ Toast.makeText(getContext(), "L???i: ch??a c?? d??? li???u", Toast.LENGTH_SHORT).show();}
                    return false;
                }
            });
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    //xu???t m??n ??n t??m ki???m ra danh s??ch
    private void getMonAnTimKiem(List<MonAnNH> list){
        try {
            MonAnAdapter adapter  = new MonAnAdapter(list, getContext(), this);
            gv_MonAn.setNumColumns(2);
            gv_MonAn.setAdapter(adapter);
        }catch (Exception e){
            Toast.makeText(getContext(), "Kh??ng t??m th???y m??n ??n", Toast.LENGTH_SHORT).show();
        }
    }

    //dialog th??m m??n ??n
    private void dialogThemMonAn(int positon){
        dialogThemMonAn =  new Dialog(getContext());
        dialogThemMonAn.setContentView(R.layout.dialog_them_monan);

        dialogThemMonAn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        dialogThemMonAn.getWindow().setLayout(width, height);

        EditText ed_Ten = dialogThemMonAn.findViewById(R.id.ed_dialogThemTenMA);
        EditText ed_ChiTiet = dialogThemMonAn.findViewById(R.id.ed_dialogThemChiTietMA);
        EditText ed_Gia = dialogThemMonAn.findViewById(R.id.ed_dialogThemGiaMA);
        imv_ThemHinhMA = dialogThemMonAn.findViewById(R.id.imv_dialogThemHinhMA);
        tv_dialogThemMaNH = dialogThemMonAn.findViewById(R.id.tv_dialogThemMaNH);
        sp_ThemMaMenuNH = dialogThemMonAn.findViewById(R.id.sp_dialogThemMaMenuNH);
        TextView tv_HuyThem = dialogThemMonAn.findViewById(R.id.tv_dialogHuyThemMA);
        TextView tv_XacNhanThem = dialogThemMonAn.findViewById(R.id.tv_dialogXacNhanThemMA);

        //g???i danh s??ch m?? nh?? h??ng, m?? menu nh?? h??ng l??n spinner
        getMaNHtoSpinner(0);
        getMaMenuNHtoSpinner(0);

        imv_ThemHinhMA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent lib = new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("image/*");

                Intent chua = Intent.createChooser(cam, "Ch???n");
                chua.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{lib});

                startActivityForResult(chua, 999);
            }
        });

        tv_XacNhanThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenMon = ed_Ten.getText().toString();
                String chiTiet = ed_ChiTiet.getText().toString();
                String gia = ed_Gia.getText().toString();

                if(!kiemLoiONhap(tenMon, chiTiet, gia).isEmpty()){
                    Toast.makeText(getContext(), kiemLoiONhap(tenMon, chiTiet, gia), Toast.LENGTH_SHORT).show();
                }else{
                    Random random =  new Random();
                    int x = random.nextInt((10000-1+1)+1);
                    String maMA = "MA" + x;
                    String maMenuNH = sp_ThemMaMenuNH.getSelectedItem().toString();

                    monAnNH = new MonAnNH(maMA, _maNH, maMenuNH, tenMon, chiTiet, Integer.parseInt(gia), "");

                    uploadImageMonAnToFirebase(imageFileName, contenUri, 0);
                }
            }
        });

        tv_HuyThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogThemMonAn.dismiss();
            }
        });

        dialogThemMonAn.show();

    }


    //dialog s???a m??n ??n
    public void dialogSuaMonAn(int positon){
        dialogSuaMonAn =  new Dialog(getContext());
        dialogSuaMonAn.setContentView(R.layout.dialog_sua_monan);

        dialogSuaMonAn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getContext().getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getContext().getResources().getDisplayMetrics().heightPixels*0.8);
        dialogSuaMonAn.getWindow().setLayout(width, height);

        EditText edSuaTenMA = dialogSuaMonAn.findViewById(R.id.ed_dialogSuaTenMA);
        EditText edSuaChiTietMA = dialogSuaMonAn.findViewById(R.id.ed_dialogSuaChiTietMA);
        EditText edSuaGiaMA = dialogSuaMonAn.findViewById(R.id.ed_dialogSuaGiaMA);
        tv_dialogSuaMaNH = dialogSuaMonAn.findViewById(R.id.tv_dialogSuaMaNH);
        sp_dialogSuaMaMenuNH = dialogSuaMonAn.findViewById(R.id.sp_dialogSuaMaMenuNH);
        imv_SuaHinhMA = dialogSuaMonAn.findViewById(R.id.imv_dialogSuaHinhMA);
        TextView tvHuySua = dialogSuaMonAn.findViewById(R.id.tv_dialogHuySuaMA);
        TextView tvXacNhanSua = dialogSuaMonAn.findViewById(R.id.tv_dialogXacNhanSuaMA);

        //g???i danh s??ch m?? nh?? h??ng , m?? menu nh?? h??ng l??n spinner
        getMaNHtoSpinner(1);
        getMaMenuNHtoSpinner(1);

        //?????y th??ng tin l??n dialog s???a m??n ??n
        edSuaTenMA.setText(listMonAn.get(positon).getTenMon());
        edSuaChiTietMA.setText(listMonAn.get(positon).getChiTiet());
        edSuaGiaMA.setText(listMonAn.get(positon).getGia() + "");
        _hinhAnh = listMonAn.get(positon).getHinhAnh();

        if(_hinhAnh.isEmpty()){
            imv_SuaHinhMA.setImageResource(R.drawable.im_food);
        }else{
            Picasso.with(getContext()).load(listMonAn.get(positon).getHinhAnh()).into(imv_SuaHinhMA);
        }

        tvHuySua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSuaMonAn.dismiss();
            }
        });

        imv_SuaHinhMA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent lib = new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("image/*");

                Intent chua = Intent.createChooser(cam, "Ch???n");
                chua.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{lib});

                startActivityForResult(chua, 888);
            }
        });

        tvXacNhanSua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenMon = edSuaTenMA.getText().toString();
                String chiTiet = edSuaChiTietMA.getText().toString();
                String gia = edSuaGiaMA.getText().toString();

                if(!kiemLoiONhap(tenMon, chiTiet, gia).isEmpty()){
                    Toast.makeText(getContext(), kiemLoiONhap(tenMon, chiTiet, gia), Toast.LENGTH_SHORT).show();
                }else{
                    //Th??m l??n Firebase
                    String maMA = listMonAn.get(positon).getMaMA();
                    String maMenuNH = sp_dialogSuaMaMenuNH.getSelectedItem().toString();

                    int _gia = Integer.parseInt(gia);
                    monAnNH = new MonAnNH(maMA, _maNH, maMenuNH, tenMon, chiTiet, _gia, "");
                    //?????y h??nh ???nh l??n firebase
                    uploadImageMonAnToFirebase(imageFileName, contenUri, 1); // S??? 0 l?? th??m m??n ??n, s??? 1 l?? s???a m??n ??n
                }
            }
        });

        dialogSuaMonAn.show();
    }


    //dialog x??a m??n ??n
    public void dialogXoaMonAn(int positon){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Th??ng b??o")
                .setMessage("B???n ch???n ch???n mu???n x??a m??n ??n n??y kh??ng?")
                .setPositiveButton("C??", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {

                            if(!KiemTraMonAnTrongGioHang(listMonAn.get(positon).getMaMA())) {

                                // Delete b???ng m??n ??n
                                db.collection("MONANNH").document(listMonAn.get(positon).getMaMA())
                                        .delete();

                                deleteGioHangFireBase(listMonAn.get(positon).getMaMA());

                                getAllMonAn(getContext()); // L???y t???t c??? m??n ??n t??? Firestore xu???ng
                                getAllMaNH(getContext());
                                getAllMaMenuNH(getContext());
                                getAllGioHangCT();
                                Toast.makeText(getContext(), "X??a m??n ??n th??nh c??ng", Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(getContext(), "M??n ??n ???? ???????c mua, kh??ng th??? x??a", Toast.LENGTH_SHORT).show();

                        }catch (Exception e){
                            Log.d("===> ", "dialogXoaMonAN " + e.getMessage());
                        }

                    }
                }).setNegativeButton("Kh??ng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }


    private String kiemLoiONhap(String tenMa, String ctMa, String giaMa){
        String loi = "";
        try {
            if (tenMa.isEmpty()) loi += "\nB???n ch??a nh???p t??n m??n ??n";
            else if (!kiemKhoangTrang(tenMa))
                loi += "\nT??n m??n ??n kh??ng ???????c nh???p kho???ng tr???ng";

            if (ctMa.isEmpty()) loi += "\nB???n ch??a nh???p chi ti???t m??n ??n";
            else if (!kiemKhoangTrang(ctMa))
                loi += "\nM??n ??n chi ti???t kh??ng ???????c nh???p kho???ng tr???ng";

            int _giaMa = Integer.parseInt(giaMa);
            if (_giaMa <= 1000) loi += "\nGi?? m??n ??n kh??ng ???????c nh??? h??n 1000 VND";


            if (giaMa.isEmpty()) loi += "\nB???n ch??a nh???p gi?? m??n ??n";
        }catch (Exception e){
            loi += "\n" + e.getMessage();
        }
        return loi;

    }

    private Boolean kiemKhoangTrang(String _duLieu){
        for (int i = 0; i < _duLieu.length(); i++) {
            if(!Character.isWhitespace(_duLieu.charAt(i))){
                return true;
            }
        }
        return false;
    }


    //l???y m?? nh?? h??ng l??n spinner
    private void getMaNHtoSpinner(int chucNang){
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, listMaNH);

            // Layout for All ROWs of Spinner.  (Optional for ArrayAdapter).
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if(chucNang == 0){
                tv_dialogThemMaNH.setText(_maNH);
            }else{
                tv_dialogSuaMaNH.setText(_maNH);
            }
    }

    //l???y m?? menu nh?? h??ng l??n spinner
    private void getMaMenuNHtoSpinner(int chucNang){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, listMaMenuNH);

        // Layout for All ROWs of Spinner.  (Optional for ArrayAdapter).
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(chucNang == 0){
            sp_ThemMaMenuNH.setAdapter(adapter);
        }else{
            sp_dialogSuaMaMenuNH.setAdapter(adapter);
        }
    }



    // X??? l?? s??? ki???n load h??nh l??n ImaveView
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        //X??? l?? th??m ???nh l??n imageview ???nh m??n ??n
        if (requestCode == 999 && resultCode == RESULT_OK){
            contenUri = data.getData();
            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
            if (data.getExtras() != null){
                Bundle caigio = data.getExtras();
                Bitmap bitmap = (Bitmap) caigio.get("data");
                imv_ThemHinhMA.setImageBitmap(bitmap);
            }else{
                imv_ThemHinhMA.setImageURI(contenUri);
            }
        }

        //X??? l?? th??m ???nh l??n imageview ???nh dialog s???a th??ng tin nh?? h??ng
        if (requestCode == 888 && resultCode == RESULT_OK){
            contenUri = data.getData();
            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
            if (data.getExtras() != null){
                Bundle caigio = data.getExtras();
                Bitmap bitmap = (Bitmap) caigio.get("data");
                imv_SuaHinhMA.setImageBitmap(bitmap);
            }else{
                imv_SuaHinhMA.setImageURI(contenUri);
            }
        }
    }

    private  String getFileExt(Uri uri){
        ContentResolver c = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(uri));
    }


    //Load h??nh l??n folder h??nh ???nh c???a m??n ??n
    private void uploadImageMonAnToFirebase(String name, Uri contentUri, int congViec){
        StorageReference image = storageReference.child("IM_MONAN/" + name);
        try {
            image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            // Th??m ???nh m??n ??n l??n firebase
                            monAnNH.setHinhAnh(uri.toString());
                            if(congViec == 0) {
                                themMonAnToFireStore(monAnNH);
                            }else{
                                capnhatMonAnToFireStore(monAnNH);
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
//                themMonAnToFireStore(monAnNH);
                Toast.makeText(getContext(), "B???n ch??a ch???n h??nh", Toast.LENGTH_SHORT).show();
            }else{
                monAnNH.setHinhAnh(_hinhAnh);
                capnhatMonAnToFireStore(monAnNH);
            }
        }
    }


  //   C???p nh???p th??ng tin b???ng m??n ??n l??n Firebase
    private void capnhatMonAnToFireStore(MonAnNH monAnNH){
        final CollectionReference reference = db.collection("MONANNH");

            Map map = new HashMap<String, Object>();
            map.put("MaMA", monAnNH.getMaMA());
            map.put("MaNH", monAnNH.getMaNH());
            map.put("MaMenuNH", monAnNH.getMaMenuNH());
            map.put("TenMon", monAnNH.getTenMon());
            map.put("Gia", monAnNH.getGia());
            map.put("ChiTiet", monAnNH.getChiTiet());
            map.put("HinhAnh", monAnNH.getHinhAnh());

        try {
            //c???p nh???t d??? li???u firebase
            reference.document(monAnNH.getMaMA() + "").set(map, SetOptions.merge());

            dialogSuaMonAn.dismiss();
            Toast.makeText(getContext(), "Ch???nh s???a m??n ??n th??nh c??ng", Toast.LENGTH_SHORT).show();
            //C???p nh???t l???i girdview
            getAllMonAn(getContext());
        }catch (Exception e){
            Log.d("===> ", "CapNhatMonAnToFireStore " + e.getMessage());
        }
    }


    // Th??m th??ng tin m??n ??n m???i l??n Firebase
    private void themMonAnToFireStore(MonAnNH monAnNH){
        UUID uuid = UUID.randomUUID();

        final CollectionReference collectionReference = db.collection("MONANNH");

        Map<String, Object> data = new HashMap<>();
        data.put("MaMA", String.valueOf(uuid));
        data.put("MaNH", monAnNH.getMaNH());
        data.put("Gia", monAnNH.getGia());
        data.put("MaMenuNH", monAnNH.getMaMenuNH());
        data.put("HinhAnh", monAnNH.getHinhAnh());
        data.put("ChiTiet", monAnNH.getChiTiet());
        data.put("TenMon", monAnNH.getTenMon());

        try {
            collectionReference.document(String.valueOf(uuid)).set(data);

            dialogThemMonAn.dismiss();

            Toast.makeText(getContext(), "Th??m m??n ??n th??nh c??ng", Toast.LENGTH_SHORT).show();
            getAllMonAn(getContext()); // L???y t???t c??? m??n ??n t??? Firestore xu???ng
            getAllMaNH(getContext());
            getAllMaMenuNH(getContext());
        }catch (Exception e){
            Log.d("Error add Firebase:", e.getMessage());
        }
    }

    //Ki???m tra m??n ??n ???? ???????c mua ch??a, n???u chua th??
    private Boolean KiemTraMonAnTrongGioHang(String _maMA){
        for(GioHangCT gh: listGioHangCT){
            if(_maMA.equals(gh.getMaMA())){
                return true; // c?? m??n ??n trong gi??? h??ng, kh??ng ??c x??a
            }
        }

        return false;// ???????c x??a
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
                        Log.d("===> ", "deleteGioHangToFriebaase " + e.getMessage());
                    }
                }
            });

        }catch (Exception e){
            Log.d("===> ", "deleteGioHangToFriebaase " + e.getMessage());

        }

    }

}