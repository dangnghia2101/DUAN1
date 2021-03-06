package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.TaiKhoan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DangKyActivity extends AppCompatActivity {

    private EditText edHoTen, edSoDT, edDiaChi, edMaOTP;

    private ImageButton imBtn_ThemHinhDK;
    private ImageView imgTrove, imgHinhDK;

    private Button btnDangKy, btnXacThucOTP;

    // variable for FirebaseAuth class x??c th???c OTP
    private FirebaseAuth mAuth;
    // string for storing our verification ID OTP
    private String verificationId;

    //CSDL
    FirebaseFirestore db;
    //Image firebase
    StorageReference storageReference;

    private Dialog dialogOTP, diaLogThongBaoThongTinTK;

    //Load image
    int GALEERY_REQUEST_CODE = 105;
    Uri contenUri;
    String imageFileName = "";
    private String imageHinhLuu = "";

    private List<String> listSoDTKhachHang;
    private List<TaiKhoan> listTaiKhoan;

    private TaiKhoan taiKhoan;
    private int Quyen = 2;
    private String soDTTT;
    private String diaChi, hoTen, soDT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        edHoTen = findViewById(R.id.ed_HoTen);
        edSoDT = findViewById(R.id.ed_SDT);
        edDiaChi = findViewById(R.id.ed_DiaChi);
        imBtn_ThemHinhDK = findViewById(R.id.imBtn_ThemHinhDK);
        imgTrove = findViewById(R.id.imgTrove);
        imgHinhDK = findViewById(R.id.imgHinhDK);
        btnDangKy = findViewById(R.id.btnDangKy);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        getSoDT();
        getAllTaiKhoan(getApplication());

        imgTrove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
               startActivity(intent);
            }
        });

        // Th??m ???nh ?????i di???n
        imBtn_ThemHinhDK.setOnClickListener(new View.OnClickListener() {

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

        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                     hoTen = edHoTen.getText().toString();
                     soDT = edSoDT.getText().toString().trim();
                    //trim() x??a k?? t??? tr???ng ??? ?????u v?? cu???i c???a s??? ??i???n tho???i
                    diaChi = edDiaChi.getText().toString();

                    if(!kiemLoiNhap(hoTen, soDT, diaChi).isEmpty()) {
                        Toast.makeText(getApplication(), kiemLoiNhap(hoTen, soDT, diaChi), Toast.LENGTH_SHORT).show();
                    }else{
                        //Load h??nh ???nh l??n firebase
                        if(imageFileName.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Ch??a ???????c th??m h??nh ???nh", Toast.LENGTH_SHORT).show();
                        }else {
                            //Th??m t??i kho???n v??o database
//                            Random random = new Random();
//                            int x = random.nextInt((1000-1+1)+1);
//                            String maTK = "TK" + x;
                            UUID uuid = UUID.randomUUID();
                            String maTK = String.valueOf(uuid);

                            taiKhoan = new TaiKhoan(maTK, hoTen, soDT, soDT, diaChi, 2, imageFileName,1000000);

                            db = FirebaseFirestore.getInstance();

//                           Ki???m tra m?? t??i kho???n ???? t???n t???i ch??a
                            final CollectionReference reference = db.collection("TAIKHOAN");
                            reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    int check = 0;
                                    if(task.isSuccessful()){
                                        QuerySnapshot snapshot = task.getResult();
                                        for(QueryDocumentSnapshot doc: snapshot){
                                            //Log.d("=======> ", doc.get("MaTK").toString());
                                            if(String.valueOf(taiKhoan.getMaTK()).equals(doc.get("MaTK").toString()) || taiKhoan.getSDT().equals(doc.get("SDT").toString())){
                                                check = 1;
                                                break;
                                            }
                                        }
                                        if(check == 0){
                                            //M??? dialog x??c th???c OTP
                                            diaLogOpenOTP();
                                        }else
                                            Toast.makeText(getApplication(), "M?? t??i kho???n ???? t???n t???i", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplication(), "L???i " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //
    public void getAllTaiKhoan(Context context){
        listTaiKhoan = new ArrayList<>();

        final CollectionReference reference = db.collection("TAIKHOAN");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            String maTK = doc.get("MaTK").toString();
                            String hoTen = doc.get("HoTen").toString();
                            String matKhau = doc.get("MatKhau").toString();
                            String soDT = doc.get("SDT").toString();
                            String diaChi = doc.get("DiaChi").toString();
                            int quyen = Integer.parseInt(doc.get("Quyen").toString());
                            String hinhAnh = doc.get("HinhAnh").toString();
                            int soDu = Integer.parseInt(doc.get("SoDu").toString());

                            taiKhoan = new TaiKhoan(maTK, hoTen, matKhau, soDT, diaChi, quyen, hinhAnh, soDu);
                            listTaiKhoan.add(taiKhoan);
                        }
                    }else{
                        Toast.makeText(DangKyActivity.this, "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d("=====> ", "L???i get t??i kho???n: " + e.getMessage());
                }
            }
        });
    }

    //l???y s??? ??i???n tho???i trong danh s??ch t??i kho???n c???a firestore xu???ng, l??u v??o listSoDTKhachHang
    public void getSoDT(){
        listSoDTKhachHang = new ArrayList<>();

        final CollectionReference reference = db.collection("TAIKHOAN");

        reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if(task.isSuccessful()){
                        QuerySnapshot snapshot = task.getResult();
                        for(QueryDocumentSnapshot doc: snapshot){
                            soDTTT = doc.get("SDT").toString();

                            listSoDTKhachHang.add(soDTTT);
                        }
                    }else{
                        Toast.makeText(DangKyActivity.this, "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Log.d("=====> ", "L???i: " + e.getMessage());
                }
            }
        });
    }

    //h??m ki???m l???i nh???p
    private String kiemLoiNhap(String _hoTen, String _SDT, String _diaChi) {
        String loi = "";
        //pattern ?????nh d???ng s??? ??i???n tho???i ch??? ???????c s??? 03, 08, 09
        String pattern = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
        if (_hoTen.isEmpty())
            loi += "B???n ch??a nh???p h??? t??n";
        else if (!kiemKhoangTrang(_hoTen))
            loi += "Kh??ng ???????c nh???p kho???ng tr???ng";
        else if (_hoTen.length()<5 || _hoTen.length()>30)
            loi += "H??? t??n bao g???m 5 ?????n 30 k?? t???";


        if(_SDT.isEmpty()) {
            loi += "\nB???n ch??a nh???p s??? ??i???n tho???i";
        }else if(!_SDT.matches(pattern)){
            loi += "\nS??? ??i???n tho???i kh??ng ????ng ?????nh d???ng";
        }else if(kiemTraSoDTTonTai(_SDT)){
            loi += "\nS??? ??i???n tho???i ???? t???n t???i";
        }

        if (_diaChi.isEmpty())
            loi += "\nB???n ch??a nh???p ?????a ch???";
        else if (!kiemKhoangTrang(_diaChi))
            loi += "Kh??ng ???????c nh???p kho???ng tr???ng";
        else if(_diaChi.length()<5 || _diaChi.length()>30)
            loi += "\n?????a ch??? bao g???m 5 ?????n 30 k?? t???";

        return loi;
    }

    //ki???m tra s??? ??i???n tho???i ???? ???????c s??? d???ng th?? kh??ng ???????c ????ng k?? n???a
    private boolean kiemTraSoDTTonTai(String _duLieu){
        try {
            for(String _soDTTT: listSoDTKhachHang){
                if(_soDTTT.equals(_duLieu)){
                    return true;
                }
            }
        }catch (Exception e){
            Log.d("======> ", "L???i " + e.getMessage());
            return false;
        }

        return false;
    }

    //h??m ki???m tra l???i nh???p kho???ng tr???ng to??n b??? Edittext
    private Boolean kiemKhoangTrang(String _duLieu){
        for (int i = 0; i < _duLieu.length(); i++) {
            if(!Character.isWhitespace(_duLieu.charAt(i))){
                return true;
            }
        }
        return false;
    }

    //h??m ki???m tra l???i nh???p kho???ng c??ch tr???ng
    private Boolean kiemKhoangTrangSDT(String _duLieu){
        for (int i = 0; i < _duLieu.length(); i++) {
            if(Character.isWhitespace(_duLieu.charAt(i))){
                return true;
            }
        }
        return false;
    }

    //Th??m t??i kho???n v??o firestore b???ng m?? t??i kho???n
    private void themTaiKhoanToFireStore(TaiKhoan taiKhoan){
        final CollectionReference collectionReference = db.collection("TAIKHOAN");

        Map<String, Object> data = new HashMap<>();
        data.put("MaTK", taiKhoan.getMaTK());
        data.put("HoTen", taiKhoan.getHoTen());
        data.put("DiaChi", taiKhoan.getDiaChi());
        data.put("MatKhau", taiKhoan.getMatKhau());
        data.put("SoDu", taiKhoan.getSoDu());
        data.put("SDT", taiKhoan.getSDT());
        data.put("Quyen", Quyen);
        data.put("HinhAnh", taiKhoan.getHinhAnh());

        try {
            collectionReference.document(taiKhoan.getMaTK() + "").set(data);
        }catch (Exception e){
            Log.d("Error_addTKFirebase", e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK){
            contenUri = data.getData();
            String timSamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timSamp + "." + getFileExt(contenUri);
            if (data.getExtras() != null){
                Bundle caigio = data.getExtras();
                Bitmap bitmap = (Bitmap) caigio.get("data");
                imgHinhDK.setImageBitmap(bitmap);
            }else{
                imgHinhDK.setImageURI(contenUri);
            }
        }
    }

    private  String getFileExt(Uri uri){
        ContentResolver c = getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(uri));
    }

    private void uploadImageToFirebase(String name, Uri contentUri){
        StorageReference image = storageReference.child("IM_TAIKHOAN/"+name);
        try {
            image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
//                            Log.d("==> Done", " Load h??nh ???nh l??n Firebase th??nh c??ng "+ uri.toString());
                            // Th??m t??i kho???n l??n firebase
                            taiKhoan.setHinhAnh(uri.toString());
                            imageHinhLuu = uri.toString();
                            Log.d("=====>", "L???i uri: " + uri.toString());
                            themTaiKhoanToFireStore(taiKhoan);

                            //hi???n th??? dialog th??ng tin t??i kho???n
                            diaLogThongBaoThongTinTK();
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
            taiKhoan.setHinhAnh("");
            themTaiKhoanToFireStore(taiKhoan);
        }
    }

    //dialog x??c th???c m?? otp
    private void diaLogOpenOTP(){
        // G???i m?? OTP ?????n ??i???n tho???i
        sendVerificationCode(edSoDT.getText().toString());

        dialogOTP = new Dialog(DangKyActivity.this);
        dialogOTP.setContentView(R.layout.dialog_otp_firebase);

        dialogOTP.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.6);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.2);
        dialogOTP.getWindow().setLayout(width, height);

        edMaOTP = dialogOTP.findViewById(R.id.edMaOTP);
        btnXacThucOTP = dialogOTP.findViewById(R.id.btnXacThucOTP);

        btnXacThucOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode(edMaOTP.getText().toString());
            }
        });

        dialogOTP.show();
    }


    private void diaLogThongBaoThongTinTK(){
        diaLogThongBaoThongTinTK = new Dialog(DangKyActivity.this);
        diaLogThongBaoThongTinTK.setContentView(R.layout.dialog_thongtin_taikhoan);

        diaLogThongBaoThongTinTK.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.8);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.7);
        diaLogThongBaoThongTinTK.getWindow().setLayout(width, height);

        TextView tv_HoTenTTDK = diaLogThongBaoThongTinTK.findViewById(R.id.tv_dialogHoTenTTDK);
        TextView tv_SoDTTTDK = diaLogThongBaoThongTinTK.findViewById(R.id.tv_dialogSoDTTTDK);
        TextView tv_MatKhauTTDK = diaLogThongBaoThongTinTK.findViewById(R.id.tv_dialogMatKhauTTDK);
        TextView tv_DiaChiTTDK = diaLogThongBaoThongTinTK.findViewById(R.id.tv_dialogDiaChiTTDK);
        ImageView imv_HinhTTDK = diaLogThongBaoThongTinTK.findViewById(R.id.imv_dialogHinhTTDK);
        TextView tv_XacNhanTTDK = diaLogThongBaoThongTinTK.findViewById(R.id.tv_dialogXacNhanTTDK);

        tv_HoTenTTDK.setText(hoTen);
        tv_SoDTTTDK.setText(soDT);
        tv_MatKhauTTDK.setText(soDT);
        tv_DiaChiTTDK.setText(diaChi);

        Log.d("======>", "Hinh ???nh"+ imageHinhLuu);
        if(imageHinhLuu.isEmpty()){

            imv_HinhTTDK.setImageResource(R.drawable.im_food);
        }else{
            Picasso.with(getApplication()).load(imageHinhLuu).resize(2048, 1600).centerCrop().onlyScaleDown().into(imv_HinhTTDK);
        }

        tv_XacNhanTTDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               diaLogThongBaoThongTinTK.dismiss();

                Intent intent = new Intent(DangKyActivity.this, DangNhapActivity.class);
                startActivity(intent);
            }
        });

        diaLogThongBaoThongTinTK.show();
    }

    // G???i m?? x??c th???c OTP
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                edMaOTP.setText(code);
                //verifying the code
                verifyCode(code);
            }
        }
        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(DangKyActivity.this, "PhoneAuthProvider "+e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            //ResendToken = forceResendingToken;
        }
    };

    //G???i m?? x??c th???c
    private void sendVerificationCode(String mobile) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84"+mobile)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(DangKyActivity.this)
                        .setCallbacks(mCallback)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Ti???n h??nh x??c th???c v?? ????ng nh???p v??o ???ng d???ng
    private void verifyCode(String otp) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        //signing the user
        signInWithCredential(credential);
    }

    //
    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(DangKyActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(DangKyActivity.this, "X??c th???c th??nh c??ng", Toast.LENGTH_SHORT).show();
                            // Load avatar l??n firebase
                            uploadImageToFirebase(imageFileName, contenUri);
                            dialogOTP.dismiss();


                        } else {
                            //verification unsuccessful.. display an error message
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                String message = "Nh???p m?? x??c th???c kh??ng h???p l???";
                                Toast.makeText(DangKyActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
//                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
//                            snackbar.setAction("Dismiss", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                }
//                            });
//                            snackbar.show();
                        }
                    }
                });
    }
}