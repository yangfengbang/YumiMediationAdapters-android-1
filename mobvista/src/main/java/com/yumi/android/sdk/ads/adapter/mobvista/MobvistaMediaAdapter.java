package com.yumi.android.sdk.ads.adapter.mobvista;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.MVRewardVideoHandler;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.RewardVideoListener;
import com.yumi.android.sdk.ads.beans.YumiProviderBean;
import com.yumi.android.sdk.ads.publish.adapter.YumiCustomerMediaAdapter;
import com.yumi.android.sdk.ads.publish.enumbean.LayerErrorCode;
import com.yumi.android.sdk.ads.utils.ZplayDebug;

import java.util.Map;

/**
 * Created by hjl on 2017/11/28.
 */
public class MobvistaMediaAdapter extends YumiCustomerMediaAdapter {

    private static final String TAG = "MobvistaMediaAdapter";
    private static final int REQUEST_NEXT_MEDIA = 0x001;
    private MVRewardVideoHandler mMvRewardVideoHandler;

    protected MobvistaMediaAdapter(Activity activity, YumiProviderBean yumiProviderBean) {
        super(activity, yumiProviderBean);
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_NEXT_MEDIA:
                    ZplayDebug.d(TAG, "Mobvista media Video REQUEST_NEXT_MEDIA ", onoff);
                    onPrepareMedia();
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onPrepareMedia() {
        try {
            ZplayDebug.d(TAG, "Mobvista request new media", onoff);
            if (mMvRewardVideoHandler != null) {
                ZplayDebug.d(TAG, "Mobvista media prapared", onoff);
                mMvRewardVideoHandler.load();
            } else {
                initHandler();
                mMvRewardVideoHandler.load();
            }
        }catch (Exception e)
        {
            ZplayDebug.e(TAG, "Mobvista media onPrepareMedia error:", e, onoff);
        }
    }

    @Override
    protected void onShowMedia() {
        try {
            if (mMvRewardVideoHandler != null) {
                if (mMvRewardVideoHandler.isReady()) {
                    mMvRewardVideoHandler.show(getProvider().getKey4());//"rewardid"
                    ZplayDebug.d(TAG, "Mobvista media onShowMedia true", onoff);
                } else {
                    mMvRewardVideoHandler.load();
                    ZplayDebug.d(TAG, "Mobvista media onShowMedia false", onoff);
                }
            }
        }catch (Exception e)
        {
            ZplayDebug.e(TAG, "Mobvista media onShowMedia error:", e, onoff);
        }
    }

    @Override
    protected boolean isMediaReady() {
        if(mMvRewardVideoHandler!=null) {
            ZplayDebug.d(TAG, "Mobvista media isMediaReady true", onoff);
            return mMvRewardVideoHandler.isReady();
        }
        ZplayDebug.d(TAG, "Mobvista media isMediaReady false", onoff);
        return false;
    }

    @Override
    protected void init() {
        try {
            MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
            Map<String, String> map = sdk.getMVConfigurationMap(getProvider().getKey1(), getProvider().getKey2()); //appId, appKey
            sdk.init(map, getContext());
            initHandler();
        }catch (Exception e)
        {
            ZplayDebug.e(TAG, "Mobvista media init error:", e, onoff);
        }
    }

    @Override
    protected void callOnActivityDestroy() {
        mMvRewardVideoHandler = null;
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onActivityResume() {

    }

    private void initHandler() {
        try {
            mMvRewardVideoHandler = new MVRewardVideoHandler(getActivity(), getProvider().getKey3()); //UnitId
            mMvRewardVideoHandler.setRewardVideoListener(new RewardVideoListener() {

                @Override
                public void onVideoLoadSuccess(String unitId) {
                    ZplayDebug.d(TAG, "Mobvista media onVideoLoadSuccess unitId:"+unitId, onoff);
                    layerPrepared();
                }

                @Override
                public void onVideoLoadFail(String errorMsg) {
                    ZplayDebug.d(TAG, "Mobvista media onVideoLoadFail errorMsg:"+errorMsg, onoff);
                    afreshRequestAD(30);
                }

                @Override
                public void onShowFail(String errorMsg) {
                    ZplayDebug.d(TAG, "Mobvista media onShowFail errorMsg:"+errorMsg, onoff);
                    layerPreparedFailed(LayerErrorCode.ERROR_NO_FILL);
                    afreshRequestAD(15);
                }

                @Override
                public void onAdShow() {
                    ZplayDebug.d(TAG, "Mobvista media onAdShow", onoff);
                    layerExposure();
                    layerMediaStart();
                }

                @Override
                public void onAdClose(boolean isCompleteView, String RewardName, float RewardAmout) {
                    //三个参数为：是否播放完，奖励名，奖励积分
                    ZplayDebug.d(TAG, "Mobvista media onAdClose isCompleteView :" +isCompleteView+ "   RewardName:" + RewardName + "   RewardAmout:" + RewardAmout, onoff);
                    if(isCompleteView){
                        layerIncentived();
                        layerMediaEnd();
                    }
                    layerClosed();
                }

                @Override
                public void onVideoAdClicked(String unitId) {
                    ZplayDebug.d(TAG, "Mobvista media onVideoAdClicked unitId:"+unitId, onoff);
                    layerClicked();
                }

            });
        } catch (Exception e) {
            ZplayDebug.e(TAG, "Mobvista media initHandler error:", e, onoff);
        }
    }

    /***
     * Mobvista SDK 延时从新请求广告，（请求失败和展示失败时需要手动重新请求）
     * @param delaySecond  延迟时间，单位秒
     */
    private void afreshRequestAD(int delaySecond)
    {
        try {
            ZplayDebug.d(TAG, "Mobvista media Video requestAD delaySecond"+delaySecond, onoff);
            mHandler.sendEmptyMessageDelayed(REQUEST_NEXT_MEDIA, delaySecond * 1000);
        }catch (Exception e)
        {
            ZplayDebug.e(TAG, "Mobvista media requestAD error ",e, onoff);
        }
    }
}