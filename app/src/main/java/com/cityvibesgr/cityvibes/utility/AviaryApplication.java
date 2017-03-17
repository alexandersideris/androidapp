package com.cityvibesgr.cityvibes.utility;

import android.app.Application;

import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;

/**
 * Created by alexsideris on 8/7/16.
 */

public class AviaryApplication extends Application implements IAdobeAuthClientCredentials {

    /* Be sure to fill in the two strings below. */
    private static final String CREATIVE_SDK_CLIENT_ID = Keys.CSDK_CLIENT_ID;
    private static final String CREATIVE_SDK_CLIENT_SECRET = Keys.CSDK_CLIENT_SECRET;

    @Override
    public void onCreate() {
        super.onCreate();
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());
    }

    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }

    @Override
    public String[] getAdditionalScopesList() {
        return new String[0];
    }

    @Override
    public String getRedirectURI() {
        return null;
    }

}
