package com.cityvibesgr.cityvibes.utility;

import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by alexsideris on 02/04/2017.
 */

public class MyFirebaseInstanceIDService  extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Toast.makeText(this, "New Token: " + refreshedToken, Toast.LENGTH_SHORT).show();
    }
}
