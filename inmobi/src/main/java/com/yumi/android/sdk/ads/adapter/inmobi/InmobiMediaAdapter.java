package com.yumi.android.sdk.ads.adapter.inmobi;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;
import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerMediaAdapter;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

import java.util.Map;

public class InmobiMediaAdapter extends YumiCustomerMediaAdapter {

    private static final String TAG = "InmobiMediaAdapter";
    private InMobiInterstitial media;
    private InterstitialAdEventListener mediaListener;
    private boolean isCallbackInExposure = false;

    private static final int REQUEST_NEXT_MEDIA = 0x001;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_NEXT_MEDIA:
                    if (media != null && mediaListener != null) {
                        ZplayDebug.d(TAG, "inmobi media Video REQUEST_NEXT_MEDIA ", onoff);
                        layerNWRequestReport();
                        isCallbackInExposure = false;
                        media.load();
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };

    protected InmobiMediaAdapter(Activity activity, YumiProviderBean provider) {
        super(activity, provider);
    }

    @Override
    public void onActivityPause() {
    }

    @Override
    public void onActivityResume() {
    }

    @Override
    protected final void callOnActivityDestroy() {
        try {
            InmobiExtraHolder.onDestroy();
            if (mHandler != null && mHandler.hasMessages(REQUEST_NEXT_MEDIA)) {
                mHandler.removeMessages(REQUEST_NEXT_MEDIA);
            }
        } catch (Exception e) {
            ZplayDebug.e(TAG, "inmobi media callOnActivityDestroy error ", e, onoff);
        }
    }

    @Override
    protected void onPrepareMedia() {
        ZplayDebug.d(TAG, "inmobi request new media", onoff);
        isCallbackInExposure = false;
        if (media == null) {
            String key2 = getProvider().getKey2();
            long placementID = 0L;
            if (key2 != null && key2.length() > 0) {
                try {
                    placementID = Long.valueOf(key2);
                } catch (NumberFormatException e) {
                    ZplayDebug.e(TAG, "", e, onoff);
                    layerPreparedFailed(LayerErrorCode.ERROR_OVER_RETRY_LIMIT);
                    return;
                }
            } else {
                layerPreparedFailed(LayerErrorCode.ERROR_OVER_RETRY_LIMIT);
                return;
            }
            media = new InMobiInterstitial(getActivity(), placementID,
                    mediaListener);
        }
        media.load();
    }

    @Override
    protected void onShowMedia() {
        isCallbackInExposure = true;
        media.show();
    }

    @Override
    protected boolean isMediaReady() {
        if (media != null && media.isReady()) {
            return true;
        }
        return false;
    }

    @Override
    protected void init() {
        ZplayDebug.i(TAG, "accounID : " + getProvider().getKey1(), onoff);
        ZplayDebug.i(TAG, "placementID : " + getProvider().getKey2(), onoff);
        InmobiExtraHolder.initInmobiSDK(getActivity(), getProvider().getKey1());
        mediaListener = new InterstitialAdEventListener() {

            @Override
            public void onUserLeftApplication(InMobiInterstitial arg0) {
                ZplayDebug.d(TAG, "inmobi media left application", onoff);
                layerClicked();
            }

            @Override
            public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                super.onRewardsUnlocked(inMobiInterstitial, map);
                ZplayDebug.d(TAG, "onRewardsUnlocked ", onoff);
                layerIncentived();
                layerMediaEnd();
            }

            @Override
            public void onAdLoadSucceeded(InMobiInterstitial arg0) {
                if (!isCallbackInExposure) {
                    ZplayDebug.d(TAG, "inmobi media load successed", onoff);
                    layerPrepared();
                }
            }

            @Override
            public void onAdLoadFailed(InMobiInterstitial arg0,
                                       InMobiAdRequestStatus arg1) {
                if (!isCallbackInExposure) {
                    ZplayDebug.d(TAG, "inmobi media load failed " + arg1.getStatusCode(), onoff);
                    layerPreparedFailed(InmobiExtraHolder.decodeError(arg1.getStatusCode()));
                }
                requestAD(getProvider().getNextRequestInterval());
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial arg0) {
                ZplayDebug.d(TAG, "inmobi media exposure", onoff);
                layerExposure();
                layerMediaStart();
            }

            @Override
            public void onAdDismissed(InMobiInterstitial arg0) {
                ZplayDebug.d(TAG, "inmobi media closed", onoff);
                layerClosed();
                requestAD(2);
            }
        };
    }

    private void requestAD(int delaySecond) {
        try {
            if (!mHandler.hasMessages(REQUEST_NEXT_MEDIA)) {
                ZplayDebug.d(TAG, "inmobi media Video requestAD delaySecond" + delaySecond, onoff);
                mHandler.sendEmptyMessageDelayed(REQUEST_NEXT_MEDIA, delaySecond * 1000);
            }
        } catch (Exception e) {
            ZplayDebug.e(TAG, "inmobi media requestAD error ", e, onoff);
        }
    }
}
