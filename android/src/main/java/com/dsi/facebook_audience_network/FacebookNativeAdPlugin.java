package com.dsi.facebook_audience_network;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;
import com.facebook.ads.NativeAdViewAttributes;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.Log;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

class FacebookNativeAdPlugin extends PlatformViewFactory {

    private final BinaryMessenger messenger;

    FacebookNativeAdPlugin(BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
    }


    @Override
    public PlatformView create(Context context, int id, Object args) {
        return new FacebookNativeAdView(context, id, (HashMap) args, this.messenger);
    }
}

class FacebookNativeAdView implements PlatformView, NativeAdListener {

    static List<NativeBannerAd> listBanner = new ArrayList();
    static List<NativeAd> listNative = new ArrayList();
    private LinearLayout adView;

    private final MethodChannel channel;
    private final HashMap args;
    private final Context context;

    private NativeAd nativeAd;
    private NativeBannerAd bannerAd;

    FacebookNativeAdView(Context context, int id, HashMap args, BinaryMessenger messenger) {


        adView = new LinearLayout(context);

        this.channel = new MethodChannel(messenger,
                FacebookConstants.NATIVE_AD_CHANNEL + "_" + id);

        this.args = args;
        this.context = context;
        if ((boolean) args.get("banner_ad")) {
            if(listBanner.size() == 0){
                bannerAd = new NativeBannerAd(context, (String) this.args.get("id"));
                NativeAdBase.NativeLoadAdConfig loadAdConfig = bannerAd.buildLoadAdConfig().withAdListener(this).withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).build();
                bannerAd.loadAd(loadAdConfig);
                loadNativeBannerAds(channel);
            }else{
                bannerAd = listBanner.remove(0);
                adView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showNativeAd();
                    }
                }, 200);
            }
            loadNativeBannerAds(channel);
        } else {
            if(listNative.size() == 0){
                nativeAd = new NativeAd(context, (String) this.args.get("id"));
                NativeAdBase.NativeLoadAdConfig loadAdConfig = nativeAd.buildLoadAdConfig().withAdListener(this).withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).build();
                nativeAd.loadAd(loadAdConfig);
                loadNativeAds(channel);
            }else {
                nativeAd = listNative.remove(0);
                adView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showNativeAd();
                    }
                }, 200);
            }
            loadNativeAds(channel);

        }
    }

    private void loadNativeBannerAds(final MethodChannel channel1){
        NativeBannerAd _bannerAd = new NativeBannerAd(context, (String) this.args.get("id"));
        NativeAdBase.NativeLoadAdConfig loadAdConfig = _bannerAd.buildLoadAdConfig().withAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e("FanBanner", adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.e("FanBanner", "Loaded");
                listBanner.add((NativeBannerAd)ad);
                HashMap<String, Object> args = new HashMap<>();
                args.put("nativeBannerCached", listBanner.size());
                args.put("nativeCached", listNative.size());
                channel1.invokeMethod(FacebookConstants.LOAD_SUCCESS_METHOD, args);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        }).withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).build();

        _bannerAd.loadAd(loadAdConfig);
    }

    private void loadNativeAds(final MethodChannel channel1){
        NativeAd _nativeAd = new NativeAd(context, (String) this.args.get("id"));
        NativeAdBase.NativeLoadAdConfig loadAdConfig = _nativeAd.buildLoadAdConfig().withAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {
                listNative.add((NativeAd)ad);
                HashMap<String, Object> args = new HashMap<>();
                args.put("nativeBannerCached", listBanner.size());
                args.put("nativeCached", listNative.size());
                channel1.invokeMethod(FacebookConstants.LOAD_SUCCESS_METHOD, args);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        }).withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).build();

        _nativeAd.loadAd(loadAdConfig);
    }

    private NativeAdViewAttributes getViewAttributes(Context context, HashMap args) {
        NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes(context);

        if (args.get("bg_color") != null)
            viewAttributes.
                    setBackgroundColor(Color.parseColor((String) args.get("bg_color")));
        if (args.get("title_color") != null)
            viewAttributes.
                    setTitleTextColor(Color.parseColor((String) args.get("title_color")));
        if (args.get("desc_color") != null)
            viewAttributes.
                    setDescriptionTextColor(Color.parseColor((String) args.get("desc_color")));
        if (args.get("button_color") != null)
            viewAttributes.
                    setButtonColor(Color.parseColor((String) args.get("button_color")));
        if (args.get("button_title_color") != null)
            viewAttributes.
                    setButtonTextColor(Color.parseColor((String) args.get("button_title_color")));
        if (args.get("button_border_color") != null)
            viewAttributes.
                    setButtonBorderColor(Color.parseColor((String) args.get("button_border_color")));

        return viewAttributes;
    }

    private NativeBannerAdView.Type getBannerSize(HashMap args) {
        final int height = (int) args.get("height");

        switch (height) {
            case 50:
                return NativeBannerAdView.Type.HEIGHT_50;
            case 100:
                return NativeBannerAdView.Type.HEIGHT_100;
            case 120:
                return NativeBannerAdView.Type.HEIGHT_120;
            default:
                return NativeBannerAdView.Type.HEIGHT_120;
        }
    }

    @Override
    public View getView() {
        return adView;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void onError(Ad ad, AdError adError) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("placement_id", ad.getPlacementId());
        args.put("invalidated", ad.isAdInvalidated());
        args.put("error_code", adError.getErrorCode());
        args.put("error_message", adError.getErrorMessage());

        channel.invokeMethod(FacebookConstants.ERROR_METHOD, args);
    }

    @Override
    public void onAdLoaded(Ad ad) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("nativeBannerCached", listBanner.size());
        args.put("nativeCached", listNative.size());
        channel.invokeMethod(FacebookConstants.LOAD_SUCCESS_METHOD, args);
        adView.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNativeAd();
            }
        }, 200);
    }

    private void showNativeAd() {
        if (adView.getChildCount() > 0)
            adView.removeAllViews();

        if ((boolean) this.args.get("banner_ad")) {
            adView.addView(NativeBannerAdView.render(this.context,
                    this.bannerAd,
                    getBannerSize(this.args),
                    getViewAttributes(this.context, this.args)));
        } else {
            //View view = inflateView();
            // adView.addView(view);
            adView.addView(NativeAdView.render(this.context,
                    this.nativeAd,
                    getViewAttributes(this.context, this.args)));
        }

        channel.invokeMethod(FacebookConstants.LOADED_METHOD, args);
    }

    @Override
    public void onAdClicked(Ad ad) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("placement_id", ad.getPlacementId());
        args.put("invalidated", ad.isAdInvalidated());

        channel.invokeMethod(FacebookConstants.CLICKED_METHOD, args);
    }

    @Override
    public void onLoggingImpression(Ad ad) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("placement_id", ad.getPlacementId());
        args.put("invalidated", ad.isAdInvalidated());

        channel.invokeMethod(FacebookConstants.LOGGING_IMPRESSION_METHOD, args);
    }

    @Override
    public void onMediaDownloaded(Ad ad) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("placement_id", ad.getPlacementId());
        args.put("invalidated", ad.isAdInvalidated());

        channel.invokeMethod(FacebookConstants.MEDIA_DOWNLOADED_METHOD, args);
    }
}
