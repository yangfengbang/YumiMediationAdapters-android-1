package com.yumi.android.sdk.ads.adapter.oneway;

import android.app.Activity;

import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.YumiDebug;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerMediaAdapter;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

import mobi.oneway.sdk.OnewaySdk;
import mobi.oneway.sdk.OnewaySdkError;
import mobi.oneway.sdk.OnewaySdkListener;
import mobi.oneway.sdk.OnewayVideoFinishType;

/**
 * Created by Administrator on 2017/10/27.
 */

public class OnewayMediaAdapter extends YumiCustomerMediaAdapter {
    private String TAG = "OnewayMediaAdapter";
    private Activity activity;
    private OnewaySdkListener listener;

    protected OnewayMediaAdapter(Activity activity, YumiProviderBean yumiProviderBean) {
        super(activity, yumiProviderBean);
        this.activity = activity;
    }

    @Override
    protected void onPrepareMedia() {

    }

    @Override
    protected void onShowMedia() {
        OnewaySdk.showAdVideo(activity);
    }

    @Override
    protected boolean isMediaReady() {
        return OnewaySdk.isPlacementAdPlayable();
    }

    @Override
    protected void init() {
        creatListener();
        OnewaySdk.init(activity, getProvider().getKey1(), listener, YumiDebug.isDebugMode());
    }

    private void creatListener() {
        listener = new OnewaySdkListener() {
            @Override
            public void onAdReady(String placementID) {
                ZplayDebug.d(TAG, "Oneway media prepared", onoff);
                layerPrepared();
            }

            @Override
            public void onAdStart(String placementID) {
                ZplayDebug.d(TAG, "Oneway media shown", onoff);
                layerExposure();
                layerMediaStart();
            }

            @Override
            public void onAdFinish(String placementID, OnewayVideoFinishType onewayVideoFinishType) {
                ZplayDebug.d(TAG, "Oneway media closed", onoff);
                layerMediaEnd();
                if (onewayVideoFinishType == OnewayVideoFinishType.COMPLETED) {
                    layerIncentived();
                }
                layerClosed();
            }

            @Override
            public void onSdkError(OnewaySdkError onewaySdkError, String s) {
                ZplayDebug.d(TAG, "Oneway media onSdkError onewaySdkError : " + onewaySdkError + "    s : " + s, onoff);
                layerPreparedFailed(decodeError(onewaySdkError, s));
            }
        };
    }

    private LayerErrorCode decodeError(OnewaySdkError onewaySdkError, String msg) {
        if (onewaySdkError == null) {
            return LayerErrorCode.ERROR_INTERNAL;
        }

        LayerErrorCode result;
        if (onewaySdkError.equals(OnewaySdkError.CAMPAIGN_NO_FILL)) {
            result = LayerErrorCode.ERROR_NO_FILL;
        } else if (onewaySdkError.equals(OnewaySdkError.INITIALIZE_FAILED)) {
            result = LayerErrorCode.ERROR_NETWORK_ERROR;
        } else {
            result = LayerErrorCode.ERROR_INTERNAL;
        }
        result.setExtraMsg("Oneway errorName: " + onewaySdkError + " errorMsg: " + msg);
        return result;
    }

    @Override
    protected void callOnActivityDestroy() {

    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {

    }
}
