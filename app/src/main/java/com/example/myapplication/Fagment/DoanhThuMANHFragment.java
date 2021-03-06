package com.example.myapplication.Fagment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Adapter.DoanhThuMAAdapter;
import com.example.myapplication.Adapter.DoanhThuNHAdapter;
import com.example.myapplication.Model.DanhGiaNH;
import com.example.myapplication.Model.DoanhThuMA;
import com.example.myapplication.Model.DoanhThuNH;
import com.example.myapplication.Model.GioHangCT;
import com.example.myapplication.Model.MonAnNH;
import com.example.myapplication.Model.NhaHang;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DoanhThuMANHFragment extends Fragment {
    private List<MonAnNH> listMonAn;
    private List<GioHangCT> listGioHangCT;
    private List<NhaHang> listNhaHang;
    private List<DanhGiaNH> listDanhGia;
    private List<DoanhThuMA> listDoanhThu;
    private List<DoanhThuMA> listDoanhThuTimKiem;

    //Model
    private MonAnNH monAnNH;
    private GioHangCT gioHangCT;
    private NhaHang nhaHang;

    private RecyclerView rcv_doanhThuMA; // hi???n th??n tin c??c m??n ??n trong gi??? h??ng
    private TextView tvChonNgay1, tvChonNgay2;
    private ImageView imvTroVe;
    private SearchView svTimKiemMA;

    private int lastSelectedYear; // L??u n??m ????? hi???n l??n ng??y ch???n
    private int lastSelectedMonth; // L??u th??ng ????? hi???n l??n ng??y ch???n
    private int lastSelectedDayOfMonth; // L??u ng??y ????? hi???n l??n ng??y ch???n

    private String ngayDau, ngayCuoi, maNHBund; // Bi???n to??n c???c l??u bi???n ng??y ?????u ti??n v?? ng??y cu???i

    //Firestore
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_doanh_thu_m_a_n_h, container, false);
        anhxa(v);

        db = FirebaseFirestore.getInstance();

        getAllMonAn();

        //Tr??? l???i m??n h??nh doanh thu nh?? h??ng
        imvTroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_out, R.anim.fade_out, R.anim.fade_in, R.anim.slide_in)
                        .replace(R.id.nav_FrameFragment, new DoanhThuNHFragment())
                        .addToBackStack(null)
                        .commit();            }
        });

        tvChonNgay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chonNgayDatePicker(0);
            }
        });

        tvChonNgay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chonNgayDatePicker(1);
            }
        });

        timKiemMA();

        return v;
    }

    private void anhxa(View v){
        rcv_doanhThuMA = v.findViewById(R.id.rcv_doanhThuMA);
        tvChonNgay1 = v.findViewById(R.id.tv_chonNgay1DTMA);
        tvChonNgay2 = v.findViewById(R.id.tv_chonNgay2DTMA);
        imvTroVe  = v.findViewById(R.id.imv_TroveTrongDTMA);
        svTimKiemMA  = v.findViewById(R.id.sv_searchDTMA);

        //l???y d??? li???u t??? fragment nh?? h??ng
        Bundle bundle = this.getArguments();
        maNHBund = bundle.getString("MaNH");
    }


    // t??m ki???m m??n ??n
    private void timKiemMA(){
        try {
            svTimKiemMA.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    try {
                        listDoanhThuTimKiem = new ArrayList<>();
                        for (DoanhThuMA dt : listDoanhThu) {
                            String tenNH = dt.getTenMA().toLowerCase();

                            if (tenNH.contains(query.toLowerCase())) {
                                listDoanhThuTimKiem.add(dt);
                            }
                        }

                        adapter_gioHang(listDoanhThuTimKiem);
                    }catch (Exception e){ Toast.makeText(getContext(), "L???i: ch??a c?? d??? li???u", Toast.LENGTH_SHORT).show();}
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    try {
                        listDoanhThuTimKiem = new ArrayList<>();
                        for (DoanhThuMA dt : listDoanhThu) {
                            String tenNH = dt.getTenMA().toLowerCase();

                            if (tenNH.contains(newText.toLowerCase())) {
                                listDoanhThuTimKiem.add(dt);
                            }
                        }

                        adapter_gioHang(listDoanhThuTimKiem);
                    }catch (Exception e){ Toast.makeText(getContext(), "L???i: ch??a c?? d??? li???u", Toast.LENGTH_SHORT).show();}
                    return false;
                }
            });
        }catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Hi???n l??n DatePickerDialog ????? ch???n ng??y
    private void chonNgayDatePicker(int vitri){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        this.lastSelectedYear = c.get(Calendar.YEAR);
        this.lastSelectedMonth = c.get(Calendar.MONTH);
        this.lastSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        // Date Select Listener.
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String day = String.valueOf(dayOfMonth);
                String mon = String.valueOf(month+1);

                if(dayOfMonth<10) day = "0"+day;
                if(month<9) mon = "0"+mon;

                String kq = day + "-" + mon + "-" + year;

                if(vitri == 0){
                    tvChonNgay1.setText(kq);
                    ngayDau = kq;
                }else {
                    tvChonNgay2.setText(kq);
                    ngayCuoi = kq;

                    getAllGioHangCT(getContext()); // L???y danh s??ch gi??? h??ng t??? Firestore xu???ng
                };
            }
        };

        DatePickerDialog datePickerDialog = null;

        // Create DatePickerDialog:
        datePickerDialog = new DatePickerDialog(getContext(),
                dateSetListener, lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth);


        // Show
        datePickerDialog.show();
    }



    //T??nh t???ng doanh thu t???ng nh?? h??ng
    private void tinhDoanhThuMANH(){
        listDoanhThu = new ArrayList<>();
        for(MonAnNH ma: listMonAn){
            int tongDT = 0;
            int demDH = 0;

            //T??nh doanh thu nh?? h??ng theo m?? nh?? h??ng
            for(GioHangCT gh: listGioHangCT){
                if(gh.getMaMA().equals(ma.getMaMA())){
//                    tongDT += (gh.getGiaMA() * gh.getSoLuong());
                    tongDT +=gh.getTongGiaDH();
                    demDH += gh.getSoLuong(); //?????m t???ng ????n h??ng
                }
            }
            DoanhThuMA doanhThuMA = new DoanhThuMA(ma.getMaMA(), ma.getTenMon(), ma.getHinhAnh(), demDH, tongDT);
            listDoanhThu.add(doanhThuMA);
        }

        //S???p x???p danh s??ch doanh thu gi???m d???n
        Collections.sort(listDoanhThu, new Comparator<DoanhThuMA>() {
            @Override
            public int compare(DoanhThuMA o1, DoanhThuMA o2) {
                return String.valueOf(o2.getTongDT()).compareTo(String.valueOf(o1.getTongDT()));

            }
        });



        //?????y list l??n adapter gi??? h??ng
        adapter_gioHang(listDoanhThu);
    }

    // L???y danh s??ch m??n ??n t??? Firebase xu???ng
    public void getAllMonAn(){
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

                            if(maNH.equals(maNHBund)) {
                                monAnNH = new MonAnNH(maMA, maNH, maMenuNH, tenMon, chiTiet, gia, hinhAnh);
                                listMonAn.add(monAnNH);
                            }
                        }
                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // L???y danh s??ch gi??? h??ng chi ti???t t??? Firebase xu???ng
    public void getAllGioHangCT(Context context){
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
                            Timestamp thoiGian = (Timestamp) doc.get("ThoiGian");
                            int trangThai = Integer.parseInt(doc.get("TrangThai").toString());
                            long tongGiaDH = Long.parseLong(doc.get("TongTien").toString());

                            DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                            Date dateNow = format.parse(format.format(thoiGian.toDate()));
                            Date date1 = format.parse(ngayDau);
                            Date date2 = format.parse(ngayCuoi);

                            String ngayMua = format.format(thoiGian.toDate());

                            if(trangThai==1 && dateNow.getTime() >= date1.getTime() &&  dateNow.getTime() <= date2.getTime()) {
                                gioHangCT = new GioHangCT(maGH, maGHCT, maMA, "", soLuong, 0, "", tenMonThem, ngayMua, trangThai, "", false, tongGiaDH);
                                listGioHangCT.add(gioHangCT);
                            }
                        }

                        // Th??m ?????y ????? th??ng tin v??o gi??? h??ng chi ti??t
                        getAllDetail_gioHang();

                    }else{
                        Toast.makeText(getContext(), "Ki???m tra k???t n???i m???ng c???a b???n. L???i "+ task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
//                    Toast.makeText(getContext(), "Error getAllGioHangCT"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("=====>", e.getMessage());
                }
            }
        });
    }


    //Set Adapter gi??? h??ng
    private void adapter_gioHang(List<DoanhThuMA> list){
        DoanhThuMAAdapter adapter  = new DoanhThuMAAdapter(list, getContext());
        rcv_doanhThuMA.setLayoutManager(new LinearLayoutManager(getContext()));
        rcv_doanhThuMA.setAdapter(adapter);
    }

    //C???p nh???t ?????y ????? th??ng tin gi??? h??ng l??n listGioHangCT;
    private void getAllDetail_gioHang(){
        for(int i=0; i<listGioHangCT.size(); i++){
            addDetail_gioHang(listGioHangCT.get(i).getMaMA(), i);
        }


        //T??nh doanh thu c???a t???ng nh?? h??ng
        tinhDoanhThuMANH();
    }

    //T??m ki???m m??n ??n b???ng m?? m??n ??n, n???u c?? th??m m??n ??n v??o list gi??? h??ng
    private void addDetail_gioHang(String maMA, int positon){
        for(MonAnNH ma: listMonAn){
            if(maMA.equals(ma.getMaMA())){
                listGioHangCT.get(positon).setTenMA(ma.getTenMon());
                listGioHangCT.get(positon).setGiaMA(ma.getGia());
                listGioHangCT.get(positon).setHinhAnh(ma.getHinhAnh());
                listGioHangCT.get(positon).setMaTK(ma.getMaNH()); // ==> Coi ch???ng, ??ag ????? t???m m?? nh?? h??ng v?? tr?????ng m?? t??i kho???n

            }
        }
    }
}