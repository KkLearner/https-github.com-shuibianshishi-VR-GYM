package com.asha.md360player4android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.plugins.IMDHotspot;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.texture.MD360BitmapTexture;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * using MD360Renderer
 *
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MD360PlayerActivity extends Activity {

    private static final String TAG = "MD360PlayerActivity";

    private static final SparseArray<String> sDisplayMode = new SparseArray<>();
    private static final SparseArray<String> sInteractiveMode = new SparseArray<>();
    private static final SparseArray<String> sProjectionMode = new SparseArray<>();
    private static final SparseArray<String> sAntiDistortion = new SparseArray<>();


    ImageView fullVedio;
    RelativeLayout video_tool_rlPlayProg;//进度控制
    private SeekBar processSeekBar;                    // 播放进度条
    private TextView currTimeText;                  // 当前播放时间
    private TextView totalTimeText;             // 时间总长度
    private ToggleButton playBtn;        // 启动、暂停按钮

    ToggleButton video_tool_tbtnGyro,video_tool_tbtnVR;//遥感控制、分屏控制
    Boolean IsVertical=true;//竖屏
    Boolean IstbtnGyro=true;//遥感，false 为触屏
    Boolean IstbtnVR=true;//true 分屏, false 为全屏

    LinearLayout vedio_view;

    int vedio_view_height=400;

    static {
        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_NORMAL,"NORMAL");
        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_GLASS,"GLASS");

        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION,"MOTION");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_TOUCH,"TOUCH");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH,"M & T");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION,"CARDBOARD M");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH,"CARDBOARD M&T");

        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_SPHERE,"SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180,"DOME 180");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230,"DOME 230");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180_UPPER,"DOME 180 UPPER");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230_UPPER,"DOME 230 UPPER");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL,"STEREO H SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL,"STEREO V SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FIT,"PLANE FIT");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_CROP,"PLANE CROP");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FULL,"PLANE FULL");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL,"MULTI FISH EYE HORIZONTAL");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL,"MULTI FISH EYE VERTICAL");
        sProjectionMode.put(CustomProjectionFactory.CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL,"CUSTOM MULTI FISH EYE");

        sAntiDistortion.put(1,"ANTI-ENABLE");
        sAntiDistortion.put(0,"ANTI-DISABLE");
    }


    public static void startVideo(Context context, Uri uri){
        start(context, uri, VideoPlayerActivity.class);
    }


    private static void start(Context context, Uri uri, Class<? extends Activity> clz){
        Intent i = new Intent(context,clz);
        i.setData(uri);
        context.startActivity(i);
    }

    private MDVRLibrary mVRLibrary;

    private List<MDAbsPlugin> plugins = new LinkedList<>();

    private MDPosition logoPosition = MDPosition.newInstance().setY(-8.0f).setYaw(-90.0f);

    private MDPosition[] positions = new MDPosition[]{
            MDPosition.newInstance().setZ(-8.0f).setYaw(-45.0f),
            MDPosition.newInstance().setZ(-18.0f).setYaw(15.0f).setAngleX(15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-10.0f).setAngleX(-15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(30.0f).setAngleX(30),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-30.0f).setAngleX(-30),
            MDPosition.newInstance().setZ(-5.0f).setYaw(30.0f).setAngleX(60),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45).setAngleY(45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(0.0f).setAngleX(90),
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set content view
        setContentView(R.layout.vedio);

        video_tool_rlPlayProg=(RelativeLayout)findViewById(R.id.video_tool_rlPlayProg);
        vedio_view = (LinearLayout)findViewById(R.id.activity_md_using_surface_view);
        video_tool_tbtnGyro=(ToggleButton)findViewById(R.id.video_tool_tbtnGyro);
        video_tool_tbtnVR=(ToggleButton)findViewById(R.id.video_tool_tbtnVR);
        video_tool_tbtnVR.setVisibility(View.GONE);
        video_tool_tbtnGyro.setVisibility(View.GONE);
        fullVedio = (ImageView)findViewById(R.id.video_tool_imgFullscreen);
        fullVedio.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams playParams = (RelativeLayout.LayoutParams) video_tool_rlPlayProg.getLayoutParams();
        playParams.addRule(RelativeLayout.LEFT_OF, R.id.video_tool_imgFullscreen);
        video_tool_rlPlayProg.setLayoutParams(playParams);

        fullVedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsVertical) {
                    LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) vedio_view.getLayoutParams();
                    linearParams.height = MATCH_PARENT;
                    vedio_view_height = vedio_view.getHeight();
                    vedio_view.setLayoutParams(linearParams);
                    IsVertical = false;
                    fullVedio.setVisibility(View.GONE);
                    video_tool_tbtnVR.setVisibility(View.VISIBLE);
                    video_tool_tbtnGyro.setVisibility(View.VISIBLE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
                    RelativeLayout.LayoutParams playParams = (RelativeLayout.LayoutParams) video_tool_rlPlayProg.getLayoutParams();
                    playParams.addRule(RelativeLayout.LEFT_OF, R.id.video_tool_tbtnGyro);
                    video_tool_rlPlayProg.setLayoutParams(playParams);
                }
            }
        });

        // init VR Library
        mVRLibrary = createVRLibrary();

        final List<View> hotspotPoints = new LinkedList<>();
        hotspotPoints.add(findViewById(R.id.hotspot_point1));
        hotspotPoints.add(findViewById(R.id.hotspot_point2));
        mVRLibrary.switchDisplayMode(MD360PlayerActivity.this, 102);
        video_tool_tbtnVR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int key=1;
                if(IstbtnVR){
                    IstbtnVR=false;//变为全屏
                    key=0;
                }else {
                    IstbtnVR=true;
                    key=1;//变为分屏
                }
                int i = 0;
                int size = key+1;
                for (View point : hotspotPoints){
                    point.setVisibility(i < size ? View.VISIBLE : View.GONE);
                    i++;
                }
                if(key==1)
                mVRLibrary.switchDisplayMode(MD360PlayerActivity.this, 102);
                else mVRLibrary.switchDisplayMode(MD360PlayerActivity.this, 101);
            }
        });

        video_tool_tbtnGyro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int key=0;
                if(IstbtnGyro){
                    IstbtnGyro=false;//变为触屏
                    key=1;
                }
                else {
                    IstbtnGyro=true;//变为遥感
                    key=0;
                }
                mVRLibrary.switchInteractiveMode(MD360PlayerActivity.this, key);
            }
        });



        getVRLibrary().setEyePickChangedListener(new MDVRLibrary.IEyePickListener() {
            @Override
            public void onHotspotHit(IMDHotspot hotspot, long hitTimestamp) {
                String text = hotspot == null ? "nop" : String.format(Locale.CHINESE, "%s  %fs", hotspot.getTitle(), (System.currentTimeMillis() - hitTimestamp) / 1000.0f );

                if (System.currentTimeMillis() - hitTimestamp > 5000){
                    getVRLibrary().resetEyePick();
                }
            }
        });
    }

    abstract protected MDVRLibrary createVRLibrary();

    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }

    protected Uri getUri() {
        Intent i = getIntent();
        if (i == null || i.getData() == null){
            return null;
        }
        return i.getData();
    }

    public void cancelBusy(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public void busy(){
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    private class AndroidDrawableProvider implements MDVRLibrary.IBitmapProvider{

        private int res;

        public AndroidDrawableProvider(int res) {
            this.res = res;
        }

        @Override
        public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), this.res);
            callback.texture(bitmap);
        }
    }

    public static void startVideo(Context context, RealWorld realworld){
        Intent intent = new Intent(context,VideoPlayerActivity.class);
        intent.putExtra("imageId",realworld.getImageId());
        intent.putExtra("name",realworld.getName());
        intent.putExtra("detail",realworld.getDetail());
        intent.putExtra("url",realworld.getUrl());
        context.startActivity(intent);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) { // 监听HOME和返回键
        // TODO Auto-generated method stub
        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                LinearLayout.LayoutParams linearParams =  (LinearLayout.LayoutParams)vedio_view.getLayoutParams();
                linearParams.height = vedio_view_height;
                vedio_view.setLayoutParams(linearParams);
                IsVertical=true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
                video_tool_tbtnVR.setVisibility(View.GONE);
                video_tool_tbtnGyro.setVisibility(View.GONE);
                fullVedio = (ImageView)findViewById(R.id.video_tool_imgFullscreen);
                fullVedio.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams playParams = (RelativeLayout.LayoutParams) video_tool_rlPlayProg.getLayoutParams();
                playParams.addRule(RelativeLayout.LEFT_OF, R.id.video_tool_imgFullscreen);
                video_tool_rlPlayProg.setLayoutParams(playParams);
                return true;
            case KeyEvent.KEYCODE_HOME:
                break;
            default:
                break;
        }
        return false;
    }
}