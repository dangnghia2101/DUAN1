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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.LoaiNhaHangAdapter;
import com.example.myapplication.Adapter.MonAnAdapter;
import com.example.myapplication.Adapter.NhaHangAdapter;
import com.example.myapplication.Model.LoaiNhaHang;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.Model.NhaHang;
import com.example.myapplication.Model.YeuThich;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import static android.app.Activity.RESULT_OK;

public class MonAnFragment extends Fragment {
    private GridView gv_MonAn;
    private FloatingActionButton flBtnThemMA;
    private SearchView svMonAn;

    private TextView tv_TenNhaHangMA, tv_PhiVanChuyenMA, tv_ThoiGianMA, tv_DanhGiaMA;
    private ImageView imv_HinhNenMA, imv_TroVe, imv_toGioHang;

    private ImageView imv_ThemHinhMA;
    private Spinner sp_ThemMaNH;
    private Spinner sp_ThemMaMenuNH;
    private Dialog dialogThemMonAn;

    private List<MonAnNH> listMonAn;

    private MonAnNH monAnNH;

    private String thoiGian;
    private Double danhGia;
    private String hinhAnhNH;
    private int phiVanChuyen;

    private String _maNH, _tenNH;
    public int viTriMonAn = 0 ;

    //Firestore
    private FirebaseFirestore db;
    // variable for FirebaseAuth class xác thực OTP
    private FirebaseAuth mAuth;
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

        View view = inflater.inflate(R.layout.fragment_mon_an, container, false);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        anhXa(view);

        getAllMonAn(getContext()); // Lấy tất cả món ăn từ Firestore xuống

        //Nhấn nút thêm món ăn
        flBtnThemMA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
        imv_toGioHang = view.findViewById(R.id.imv_toGioHang);

        //lấy dữ liệu từ fragment nhà hàng
        Bundle bundle = this.getArguments();
        String tenNhaHang = bundle.getString("TenNH");
        hinhAnhNH = bundle.getString("HinhAnh");
        thoiGian = bundle.getString("ThoiGian");
        danhGia = bundle.getDouble("DanhGia");
        phiVanChuyen = bundle.getInt("PhiVanChuyen");

        tv_TenNhaHangMA.setText(tenNhaHang);
        tv_PhiVanChuyenMA.setText(formatNumber(phiVanChuyen) + " VND");
        tv_ThoiGianMA.setText(thoiGian + " m");
        tv_DanhGiaMA.setText(danhGia + "");
        if(hinhAnhNH.isEmpty()){
            imv_HinhNenMA.setImageResource(R.drawable.im_food);
        }else{
            Picasso.with(getContext()).load(hinhAnhNH).into(imv_HinhNenMA);
        }

        imv_TroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                        .replace(R.id.nav_FrameFragment, new NhaHangFragment())
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
        gv_MonAn.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
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
                        Toast.makeText(getContext(), "Kiểm tra kết nối mạng của bạn. Lỗi "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //truyền dữ liệu của fragment món ăn vào fragment món ăn chi tiết
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
        MonAnCTFragment monAnCTFragment = new MonAnCTFragment();
        monAnCTFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.nav_FrameFragment, monAnCTFragment)
                .addToBackStack(null)
                .commit();
    }

    //
    private void goiAdapter(){
        MonAnAdapter adapter = new MonAnAdapter(listMonAn, getContext(), this);
        gv_MonAn.setNumColumns(2);
        gv_MonAn.setAdapter(adapter);
    }

    // Định dạng sang số tiền
    private String formatNumber(int number){
        // tạo 1 NumberFormat để định dạng số theo tiêu chuẩn của nước Anh
        Locale localeEN = new Locale("en", "EN");
        NumberFormat en = NumberFormat.getInstance(localeEN);

        return en.format(number);
    }

    //tìm kiếm món ăn
    private void timKiemMA(){
        svMonAn.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String tk_monan = svMonAn.getQuery() + "";
                listNHSearch = new ArrayList<>();

                for(MonAnNH monAnNH: listNhaHangTheoLoai){
                    String tenMonAn = String.valueOf(monAnNH.getTenMon());

                    if(tk_monan.contains(tenMonAn)){
                        listNHSearch.add(monAnNH);
                    }
                }

                getMonAnTimKiem(listNHSearch);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getMonAnTimKiem(listNhaHangTheoLoai);
                return false;
            }
        });
    }

    //xuất món ăn tìm kiếm ra danh sách
    private void getMonAnTimKiem(List<MonAnNH> list){
        MonAnAdapter adapter  = new MonAnAdapter(list, getContext(), this);
        gv_MonAn.setNumColumns(2);
        gv_MonAn.setAdapter(adapter);
    }

    private void dialogThemMonAn(int positon){
        dialogThemMonAn =  new Dialog(getContext());
        dialogThemMonAn.setContentView(R.layout.dialog_them_monan);

        dialogThemMonAn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.6);
        dialogThemMonAn.getWindow().setLayout(width,height);

        EditText ed_Ten = dialogThemMonAn.findViewById(R.id.ed_dialogThemTenMA);
        EditText ed_ChiTiet = dialogThemMonAn.findViewById(R.id.ed_dialogThemChiTietMA);
        EditText ed_Gia = dialogThemMonAn.findViewById(R.id.ed_dialogThemGiaMA);
        ImageView imv_ThemHinhMA = dialogThemMonAn.findViewById(R.id.imv_dialogThemHinhMA);
        Spinner sp_ThemMaNH = dialogThemMonAn.findViewById(R.id.sp_dialogThemMaNH);
        Spinner sp_ThemMaMenuNH = dialogThemMonAn.findViewById(R.id.sp_dialogThemMaMenuNH);
        TextView tv_HuyThem = dialogThemMonAn.findViewById(R.id.tv_dialogHuyThemMA);
        TextView tv_XacNhanThem = dialogThemMonAn.findViewById(R.id.tv_dialogXacNhanThemMA);

//        //Lấy danh sách mã loại nhà hàng lên spinner
//        getMaLoaiLoaiNHToSpiner(0);

        imv_ThemHinhMA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent lib = new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("image/*");

                Intent chua = Intent.createChooser(cam, "Chọn");
                chua.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{lib});

                startActivityForResult(chua, 999);
//               Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//               startActivityForResult(gallery, GALEERY_REQUEST_CODE);
            }
        });

        tv_XacNhanThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenMon = ed_Ten.getText().toString();
                String chiTiet = ed_ChiTiet.getText().toString();
                String gia = ed_Gia.getText().toString();

                if(tenMon.isEmpty() || chiTiet.isEmpty() || gia.isEmpty()){
                    Toast.makeText(getContext(), "Không được để trống thông tin", Toast.LENGTH_SHORT).show();
                }else{
                    Random random =  new Random();
                    int x = random.nextInt((10000-1+1)+1);
                    String maMA = "MA" + x;

//                    MonAnNH monAnNH = new MonAnNH(maMA, tenMon, chiTiet, gia, "");
//
//                    uploadImageToFirebase(imageFileName, contenUri);
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

    //xóa món ăn
    private void dialog_xoaMonAn(int positon){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Thông báo")
                .setMessage("Bạn chắn chắn muốn xóa món ăn này không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            // Delete bảng nhà hàng
                            db.collection("MONANNH").document(listMonAn.get(positon).getMaMA())
                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                                    getAllMonAn(viTriLoaiNH);
                                    Toast.makeText(getContext(), "Xóa món ăn thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }catch (Exception e){
                            Toast.makeText(getContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }



    // Xử lí sự kiện load hình lên ImaveView
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


        //Xử lí thêm ảnh lên imageview ảnh món ăn
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


//        //Xử lí thêm ảnh lên imageview ảnh  dialog sửa thông tin nhà hàng
//        if (requestCode == 777 && resultCode == RESULT_OK){
//            contenUri = data.getData();
//            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
//            if (data.getExtras() != null){
//                Bundle caigio = data.getExtras();
//                Bitmap bitmap = (Bitmap) caigio.get("data");
//                imvHinhSuaNH.setImageBitmap(bitmap);
//            }else{
//                imvHinhSuaNH.setImageURI(contenUri);
//            }
//        }


    }

    private  String getFileExt(Uri uri){
        ContentResolver c = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(uri));
    }


    //Load hình lên folder hình ảnh của món ăn
//    private void uploadImageNHToFirebase(String name, Uri contentUri, int congViec){
//        StorageReference image = storageReference.child("IM_MONAN/"+name);
//        try {
//            image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            //Log.d("==> Done", " Load hình ảnh lên Firebase thành công "+ uri.toString());
//                            // Thêm nhà hàng lên firebase
//                            nhaHang.setHinhAnh(uri.toString());
//                            if(congViec == 0) {
//                                themNHToFireStore(loaiNhaHang);
//                            }else{
//                                updateFirebase(nhaHang);
//                            }
//                        }
//                    });
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.d("==> Exception", e.getMessage());
//                }
//            });
//        }catch (Exception e){
//            nhaHang.setHinhAnh("");
//            themNHToFireStore(loaiNhaHang);
//        }
//    }


    // Cập nhập thông tin bảng món ăn lên Firebase
//    private void updateFirebase(MonAnNH monAnNH){
//        final CollectionReference reference = db.collection("MONANNH");
//        try {
//            Map map = new HashMap<String, Object>();
//            map.put("MaMA", monAnNH.getMaMA());
//            map.put("MaNH", monAnNH.getMaNH());
//            map.put("MaMenuNH", monAnNH.getMaMenuNH());
//            map.put("TenMon", monAnNH.getTenMon());
//            map.put("Gia", monAnNH.getGia());
//            map.put("ChiTiet", monAnNH.getChiTiet());
//            map.put("HinhAnh", monAnNH.getHinhAnh());
//
//            reference.document(monAnNH.getMaMA() + "").set(map, SetOptions.merge());
//
//            dialogSuaNH.dismiss();
//            //Cập nhật lại girdview
//            getAllMonAn(getContext());
//        }catch (Exception e){
//            Toast.makeText(getContext(), "Error update Firebase: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


    // Thêm thông tin món ăn mới lên Firebase
    private void themMonAnToFireStore(MonAnNH monAnNH){
        final CollectionReference collectionReference = db.collection("MONANNH");

        Map<String, Object> data = new HashMap<>();
        data.put("MaMA", monAnNH.getMaMA());
        data.put("MaNH", monAnNH.getMaNH());
        data.put("Gia", monAnNH.getGia());
        data.put("MaMenuNH", monAnNH.getGia());
        data.put("HinhAnh", monAnNH.getGia());
        data.put("ChiTiet", monAnNH.getGia());
        data.put("TenMon", monAnNH.getGia());

        try {
            collectionReference.document(monAnNH.getMaMA() + "").set(data);
            dialogThemMonAn.dismiss();
            Toast.makeText(getContext(), "Thêm mã món ăn thành công", Toast.LENGTH_SHORT).show();
            getAllMonAn(getContext());
        }catch (Exception e){
            Log.d("Error_addTKFirebase", e.getMessage());
        }
    }
}