package org.michaz.cellid;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class LocationUpdateIntentService extends IntentService {


    private TelephonyManager tm;
    private GoogleApiClient mGoogleApiClient;

    public LocationUpdateIntentService() {
        super("LocationUpdateIntentService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        tm = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        if (tm == null) {
            Log.e("LocationUpdate", "onStart: tm == null");
        } else {
            Log.i("LocationUpdate", "onStart: tm initialized");
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("locationUpdate", "Location update.");
        LocationResult locationResult = LocationResult.extractResult(intent);
        if (locationResult != null) {
            List<CellInfo> allCellInfo = tm.getAllCellInfo();
            if (allCellInfo != null) { // happens!
                for (CellInfo cellInfo : allCellInfo) {
                    Log.i("allCellInfo", cellInfo.toString());
                    if (cellInfo.isRegistered()) {
                        if (cellInfo instanceof CellInfoLte) {
                            DatabaseReference newCellUpdate = FirebaseDatabase.getInstance().getReference().child("cellInfoLte").push();
                            HashMap<String, Object> values = new HashMap<>();
                            values.put("timestamp", Calendar.getInstance().getTimeInMillis());
                            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                            values.put("deviceId", tm.getDeviceId());
                            values.put("ci", cellInfoLte.getCellIdentity().getCi());
                            values.put("mcc", cellInfoLte.getCellIdentity().getMcc());
                            values.put("mnc", cellInfoLte.getCellIdentity().getMnc());
                            values.put("pci", cellInfoLte.getCellIdentity().getPci());
                            values.put("tac", cellInfoLte.getCellIdentity().getTac());
                            Location lastLocation = locationResult.getLastLocation();
                            values.put("lat", lastLocation.getLatitude());
                            values.put("lng", lastLocation.getLongitude());
                            newCellUpdate.setValue(values);
                            Log.i("locationUpdate", "Logged Lte.");
                        } else if (cellInfo instanceof CellInfoGsm) {
                            DatabaseReference newCellUpdate = FirebaseDatabase.getInstance().getReference().child("cellInfoGsm").push();
                            newCellUpdate.child("timestamp").setValue(Calendar.getInstance().getTimeInMillis());
                            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                            newCellUpdate.child("deviceId").setValue((tm.getDeviceId()));
                            newCellUpdate.child("mcc").setValue((cellInfoGsm.getCellIdentity().getMcc()));
                            newCellUpdate.child("mnc").setValue((cellInfoGsm.getCellIdentity().getMnc()));
                            newCellUpdate.child("lac").setValue((cellInfoGsm.getCellIdentity().getLac()));
                            Location lastLocation = locationResult.getLastLocation();
                            newCellUpdate.child("lat").setValue(lastLocation.getLatitude());
                            newCellUpdate.child("lng").setValue(lastLocation.getLongitude());
                            Log.i("locationUpdate", "Logged Gsm.");
                        }
                    }
                }
            }
        }
        List<NeighboringCellInfo> neighboringCellInfo = tm.getNeighboringCellInfo();
        if (neighboringCellInfo != null) {
            for (NeighboringCellInfo cellInfo : neighboringCellInfo) {
                Log.i("neighboringCellInfo", cellInfo.toString());
            }
        }
    }

}
