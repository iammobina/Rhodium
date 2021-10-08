package com.fatemesaffari.map;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;

    LocationRequest mLocationRequest;
    long UPDATE_INTERVAL = 5 * 1000; // 5 secs
    long FASTEST_INTERVAL = 5 * 1000; // 5 sec
    
    Marker current;
    String info;
    Integer color = -1;

    /*
    4 => Excellent    dark green
    3 => Good         green
    2 => Fair         yellow
    1 => Poor         orange
    0 => Very Poor    red
   -1 => No Signal    black
    */

    // Mobile info
    String lte_rsrq, lte_rsrp, lte_cinr, lte_rrsi;
    String umts_rscp, umts_ec;
    String gsm_rxlev, gsm_dbm;
    String plmn, plmnname, imei;

    String signalStrength1, signalStrength2;
    String cellIDText;
    String cellMccText;
    String cellMncText;
    String cellPciText;
    String cellTacText;
    String cellLacText;
    String plmn_text;
    String imei_text;


    public static AppDatabase appDatabase;

    TelephonyManager telephonyManager;
    PhoneStateListener phoneStateListener;
    SignalListener signalListener;
    int cellSig, cellID, cellMcc, cellMnc, cellPci, cellTac, cellLac;
    int lte_int_rsrq, lte_int_rsrp, lte_int_cinr, lte_int_rssi;
    int umts_int_ec, umts_int_rscp ;
    int gsm_int_rxlev,gsm_int_dbm;

    List<CellInfo> cellInfoList;
    int cellsigdbm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setupRoomDatabase();
        setContentView(R.layout.activity_maps);

        startLocationUpdates();
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // Start
        getMobileInfo();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        fetchLocation();

        // Update
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        getMobileInfo();

        // New location
        String msg = "Updated Location: " + location.getLatitude() + "," + location.getLongitude() + "\n" + info;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // Remove previous Marker
        current.remove();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        fetchLocation();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);

                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        current = googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));

        // Set Color
        if (color == 4) // Excellent    dark green
            googleMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(Color.rgb(0,100,0)).fillColor(Color.rgb(0,100,0)));
        if (color == 3) // Good         green
            googleMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(Color.GREEN).fillColor(Color.GREEN));
        if (color == 2) // Fair         yellow
            googleMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(Color.YELLOW).fillColor(Color.YELLOW));
        if (color == 1) // Poor         orange
            googleMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(Color.rgb(255,165,0)).fillColor(Color.rgb(255,165,0)));
        if (color == 0) // Very Poor    red
            googleMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(Color.RED).fillColor(Color.RED));
        if (color == -1) // No Signal   black
            googleMap.addCircle(new CircleOptions().center(latLng).radius(10).strokeColor(Color.BLACK).fillColor(Color.BLACK));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            }
        }
    }

    // Mobile info
    private void getMobileInfo(){
        List<Parametrs> params=appDatabase.globalDao().getAll();

        phoneStateListener = new PhoneStateListener();
        signalListener = new SignalListener();

        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalListener, SignalListener.LISTEN_SIGNAL_STRENGTHS);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);


        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cellInfoList = telephonyManager.getAllCellInfo();
            String plmn = telephonyManager.getSimOperator();

        } catch (Exception e) {
            Log.d("SignalStrength", "+++++++++++++++++++++++++++++++++++++++++ null array spot 1: " + e);
        }

        try {
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoGsm)
                {

                    gsm_int_rxlev=((CellInfoGsm) cellInfo).getCellSignalStrength().getAsuLevel();
                    gsm_int_rxlev=getDbmLevel(gsm_int_dbm);
                    gsm_int_dbm=((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
                    gsm_int_rxlev=getDbmLevel(gsm_int_dbm);
                    color = gsm_int_rxlev;

                }

                if (cellInfo instanceof CellInfoLte) {

                    lte_int_rsrp = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrp();
                    lte_int_rsrp=getRsrpLevel(lte_int_rsrp);
                    lte_int_rsrq = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq();
                    lte_int_rsrq=getRsrqLevel(lte_int_rsrq);
                    lte_int_cinr = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr();
                    lte_int_cinr=getSnrLevel(lte_int_cinr);
//                    lte_int_rssi = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssi();
//                    lte_int_rssi=getAsuLevel(lte_int_rssi);
                    color = lte_int_rsrp;
                }
                if (cellInfo instanceof CellInfoWcdma)
                {
                    umts_int_ec=((CellInfoWcdma) cellInfo).getCellSignalStrength().getAsuLevel();
                    umts_int_ec=getAsuLevel(umts_int_ec);
                    umts_int_rscp=((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
                    umts_int_rscp=getDbmLevel(umts_int_rscp);
                    color = umts_int_rscp;

                }

            }

        } catch (Exception e) {
            Log.d("SignalStrength", "++++++++++++++++++++++ null array spot 2: " + e);
        }
    }
    private void setupRoomDatabase() {
        appDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "parametrs")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (signalListener != null) {
                telephonyManager.listen(signalListener, SignalListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();

        try {
            if (signalListener != null) {
                telephonyManager.listen(signalListener, SignalListener.LISTEN_NONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getDbmLevel(int dbm) {
        if (dbm < -100) return 0;     // to describe clearly 0 is very poor
        else if (dbm < -95) return 1; // poor
        else if (dbm < -85) return 2; // fair
        else if (dbm < -75) return 3; // good
        else if (dbm != 0) return 4;  // excellent
        else return -1;               // null
    }


    public static int getSnrLevel(int snr) {
        return snr / 2;
    }

    public static int getAsuLevel(int asu) {
        if (asu == 99) return -1;
        else return (2 * asu) - 113;
    }

    public static int getRsrpLevel(int rsrp) {
        if (rsrp > -84) return 4;
        else if (rsrp > -102) return 3;
        else if (rsrp > -111) return 2;
        else if (rsrp > -112) return 1;
        else return 0;
    }

    public static int getRsrqLevel(int rsrq) {
        if (rsrq > -5) return 4;
        else if (rsrq > -10) return 2;
        else return 1;
    }

    private class SignalListener extends PhoneStateListener {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @SuppressLint({"MissingPermission"})
        @Override

        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalListener, SignalListener.LISTEN_SIGNAL_STRENGTHS);
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            String ltestr = signalStrength.toString();
            String[] parts = ltestr.split(" ");
            String cellSig2 = parts[9];

            try {

                cellInfoList = telephonyManager.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        cellSig = ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
                        cellsigdbm = getRsrpLevel(cellSig);

                        cellID = ((CellInfoLte) cellInfo).getCellIdentity().getCi();

                        cellMcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();

                        cellMnc = ((CellInfoLte) cellInfo).getCellIdentity().getMnc();
                        cellPci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();

                        cellTac = ((CellInfoLte) cellInfo).getCellIdentity().getTac();

                    }
                    if (cellInfo instanceof CellInfoWcdma)
                    {
                        cellSig = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
                        cellsigdbm = getDbmLevel(cellSig);
                        cellID = ((CellInfoWcdma) cellInfo).getCellIdentity().getCid();
                        cellMcc = ((CellInfoWcdma) cellInfo).getCellIdentity().getMcc();
                        cellMnc = ((CellInfoWcdma) cellInfo).getCellIdentity().getMnc();
                        cellPci = ((CellInfoWcdma) cellInfo).getCellIdentity().getPsc();
                        cellTac = ((CellInfoWcdma) cellInfo).getCellIdentity().getLac();
                    }
                    if (cellInfo instanceof CellInfoGsm)
                    {
                        cellSig = ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
                        cellsigdbm = getDbmLevel(cellSig);
                        cellID = ((CellInfoGsm) cellInfo).getCellIdentity().getCid();
                        cellMcc = ((CellInfoGsm) cellInfo).getCellIdentity().getMcc();
                        cellMnc = ((CellInfoGsm) cellInfo).getCellIdentity().getMnc();
                        cellPci = ((CellInfoGsm) cellInfo).getCellIdentity().getBsic();
                        cellTac = ((CellInfoGsm) cellInfo).getCellIdentity().getArfcn();
                        cellLac = ((CellInfoGsm) cellInfo).getCellIdentity().getLac();

                    }

                }
            } catch (Exception e) {
                Log.d("SignalStrength", "+++++++++++++++++++++++++++++++ null array spot 3: " + e);
            }
            
//            imei = telephonyManager.getImei();
            plmn = telephonyManager.getSimOperator();
            plmnname = telephonyManager.getSimOperatorName();

            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            CellLocation location = telephonyManager.getCellLocation();
            GsmCellLocation gsmLocation = (GsmCellLocation) location;

            int cellId = gsmLocation.getCid();
            int lac = gsmLocation.getLac();

            if (cellID == 0) cellID = cellId;
            if (cellLac == 0) cellLac = lac;

            appDatabase.globalDao().insertAll(new Parametrs(plmnname,plmn,cellLac,cellSig,cellID,cellMcc,cellMnc,cellPci,cellTac,lte_int_rsrq,lte_int_rsrp,lte_int_cinr,lte_int_rssi,umts_int_ec, umts_int_rscp, gsm_int_rxlev, gsm_int_dbm));

            plmn_text = "PLMN = " + plmn;
//            imei_text = "IMEI = " + imei;

            signalStrength1 = "SignalStrength1 = " + String.valueOf(cellSig);
            signalStrength2 = "SignalStrength2 = " + cellSig2;

            cellIDText = "CellID = " + (cellID);
            cellMccText = "CellMcc = " + (cellMcc);
            cellMncText = "CellMnc = " + (cellMnc);
            cellPciText = "CellPci = " + (cellPci);
            cellTacText = "CellTac = " + (cellTac);
            cellLacText = "CellLac = " + (cellLac);

            lte_cinr = "Lte_cinr = " + (lte_int_cinr);
            lte_rsrp = "Lte_rsrp = " + (lte_int_rsrp);
            lte_rsrq = "Lte_rsrq = " + (lte_int_rsrq);
            lte_rrsi = "Lte_rrsi = " + (lte_int_rssi);

            umts_rscp = "Umts_rscp = " + umts_int_rscp;
            umts_ec = "Umts_ec = " + umts_int_ec;
            gsm_rxlev = "Gsm_rxlev = " + gsm_int_rxlev;
            gsm_dbm = "Gsm dbm = " + gsm_int_dbm;

            info =  plmn_text + ", " + plmnname +"\n" +
                    imei_text + "\n" +
                    signalStrength1 + "\n" +
                    signalStrength2 + "\n" +
                    cellIDText + "\n" +
                    cellMccText + "\n" +
                    cellMncText + ", " + cellPciText + "\n" +
                    cellTacText + ", " + cellLacText + "\n" +
                    lte_cinr + ", " + lte_rsrp + "\n" +
                    lte_rsrq + ", " + lte_rrsi + "\n" +
                    umts_rscp + ", " + umts_ec + ", " + gsm_rxlev + ", " + gsm_dbm;

            super.onSignalStrengthsChanged(signalStrength);
        }
    }
}