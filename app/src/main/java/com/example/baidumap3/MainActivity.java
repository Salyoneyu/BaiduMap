package com.example.baidumap3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.CheckBox;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.model.LatLngBounds;
import com.google.android.material.navigation.NavigationView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;   //定义地图视图，只是负责显示用的
    private BaiduMap mBaiduMap = null; //定义百度地图对象，用来进行地图具体操作
//侧拉菜单
    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;
    private ImageView headView;

    // 我们通过继承抽象类BDAbstractListener并重写其onReceieveLocation方法来获取定位数据，并将其传给MapView。
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d("MapOut",location.toString());
            Log.d("MapOut","定位结果："+location.getLatitude()+",地址："+location.getLongitude());
            //mapView 销毁后不在处理新接收的位置，返回
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            Log.w("MapOut",locData.toString());
//实例化UiSettings类对象
            mBaiduMap.getUiSettings();
//通过设置enable为true或false 选择是否显示指南针
            mBaiduMap.setMyLocationData(locData);
            // 定位回调的时候打开指南针
            mBaiduMap.getUiSettings().setCompassEnabled(true);
        }
    }

    private LocationClient mLocationClient = null;
    private MyLocationListener mLocationListener = new MyLocationListener();

    /** Called when the user touches the button */
    public void sendMessage(View view) {
        Log.d("Button","click");
    }

    //定位权限
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 定位权限
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            // GPS权限
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            // 读写权限 好像没啥用
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //没有授权，编写申请权限代码
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    // 初始化定位
    private void initLocation() {

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        // 开启地图的定位图层
        mBaiduMap = mMapView.getMap();

        // 从文档里面复制过来的设置
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，设置定位模式，默认高精度
        option.setCoorType("bd09ll"); //坐标类型
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000); //每隔多少秒进行一次请求
        mLocationClient.setLocOption(option);

        // 自定义的内容，详见https://lbsyun.baidu.com/index.php?title=androidsdk/guide/create-map/location
        // 如果没有设置这里的MyLocationConfiguration.LocationMode.FOLLOWING的话，地图就会一直显示在北京，不会自动跳转
        MyLocationConfiguration mLocationConfiguration= new MyLocationConfiguration(
        MyLocationConfiguration.LocationMode.NORMAL, // 模式选择
        true, // 我也不知道这是什么参数
        BitmapDescriptorFactory.fromResource(R.drawable.checked),//选择图标
        0xAAFFFF88,// 精度圈填充颜色
          0xAA00FF00);//精度圈边框颜色
        // 使自定义内容生效
        mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);

        // 注册监听器，监听器可以回调定位的函数
        mLocationClient.registerLocationListener(mLocationListener);

        // 开启定位
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient.start();

        // 添加控件
        mMapView.setLogoPosition(LogoPosition.logoPostionleftBottom);
        // mBaiduMap.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        //实例化UiSettings类对象
        Log.d("settings","start");
        UiSettings mUiSettings = mBaiduMap.getUiSettings();
        System.out.println(mUiSettings.isOverlookingGesturesEnabled());
        //通过设置enable为true或false 选择是否显示指南针
        // 经过尝试，死活没法显示指南针
        // 参考https://www.jianshu.com/p/c7235e55e49b, 这里说在初始化的时候关掉指南针，在定位的时候打开，才能够显示指南针
        mUiSettings.setCompassEnabled(true);
        //通过设置enable为true或false 选择是否显示比例尺
        mMapView.showScaleControl(true);
        // 同时支持设置maxZoomLevel和minZoomLevel，方法为：
        mBaiduMap.setMaxAndMinZoomLevel(19, 3);
        //通过设置enable为true或false 选择是否显示缩放按钮
        mMapView.showZoomControls(true);
        //通过设置enable为true或false 选择是否启用地图平移
        mUiSettings.setScrollGesturesEnabled(true);
        //通过设置enable为true或false 选择是否启用地图缩放手势
        mUiSettings.setZoomGesturesEnabled(true);
        //通过设置enable为true或false 选择是否启用地图俯视功能
//        mUiSettings.setOverlookingGesturesEnabled(false);
        //通过设置enable为true或false 选择是否启用地图旋转功能
        mUiSettings.setRotateGesturesEnabled(true);

        System.out.println(mUiSettings.isOverlookingGesturesEnabled());
        System.out.println(mBaiduMap.getUiSettings().isOverlookingGesturesEnabled());


    }

    // 按钮点击的监听
    class MyButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            System.out.println("ButtonClick");
            System.out.println(v.getId());
            switch (v.getId()) {
                case R.id.btMapNormal:
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    Toast.makeText(MainActivity.this, "btMapNormal", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btStateLlite:
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    Toast.makeText(MainActivity.this, "btStateLlite", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btNone:
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                    Toast.makeText(MainActivity.this, "btNone", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }


    // 初始化所有按钮
    private void initButton(){
        Log.d("ButtonClick", "初始化按钮点击");

        Button btMapNormal  = (Button) findViewById(R.id.btMapNormal);
        Button btStateLlite = (Button) findViewById(R.id.btStateLlite);
        Button btNone = (Button) findViewById(R.id.btNone);
        MyButtonListener Listener = new MyButtonListener();
        btMapNormal.setOnClickListener(Listener);
        btStateLlite.setOnClickListener(Listener);
        btNone.setOnClickListener(Listener);

    }

    private void AddMarker(){
        //定义Maker坐标点
        LatLng point = new LatLng(30.963175, 114.400244);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.checked);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        requestPermission(); // 获取权限
        setContentView(R.layout.activity_main);

        initLocation();
        initButton();
        AddMarker();
        drawerLayout = findViewById(R.id.drawerLayout);
        navigation_view = findViewById(R.id.navigation_view);
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.all_item:
                        MapTools.drawHuLine(mBaiduMap);
//                        Toast.makeText(MainActivity.this, "点击了全部", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.a_item:
                        MapTools.drawPolygon(mBaiduMap);
//                        Toast.makeText(MainActivity.this, "点击了a", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.b_item:
                        MapTools.drawCircle(mBaiduMap);
//                        Toast.makeText(MainActivity.this, "点击了b", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.c_item:
                        //定义Maker坐标点,
                        LatLng point = new LatLng(30.527123, 114.405671);
                        //构建Marker图标
                        BitmapDescriptor bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.marker1);
                        //构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions option = new MarkerOptions()
                                .position(point)
                                .icon(bitmap);
                        //在地图上添加Marker，并显示
                        mBaiduMap.addOverlay(option);
                        break;
                    case R.id.d_item:
                        //        清除mBaidumap
                        mBaiduMap.clear();
                        //定义Maker坐标点
                        LatLng point1 = new LatLng(30.526149, 114.40724);
                        //构建Marker图标
                        BitmapDescriptor bitmap1 = BitmapDescriptorFactory
                                .fromResource(R.drawable.circle);
                        //构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions option1 = new MarkerOptions()
                                .position(point1) //必传参数
                                .icon(bitmap1) //必传参数
                                .draggable(true)
                                //设置平贴地图，在地图中双指下拉查看效果
                                .flat(true)
                                .alpha(0.5f);
                        //在地图上添加Marker，并显示
                        mBaiduMap.addOverlay(option1);
                        break;
                    case R.id.f_item:
                        mBaiduMap.clear();
                        //构建折线点坐标,
                        LatLng p1 = new LatLng(30.527123, 114.405671);
                        LatLng p2 = new LatLng(30.526779, 114.405241);
                        LatLng p3 = new LatLng(30.526149, 114.40724);
                        List<LatLng> points = new ArrayList<LatLng>();
                        points.add(p1);
                        points.add(p2);
                        points.add(p3);

                        //设置折线的属性
                        OverlayOptions mOverlayOptions = new PolylineOptions()
                                .width(10)
                                .color(0xAAFF0000)
                                .points(points);
                        //在地图上绘制折线
                        //mPloyline 折线对象
                        Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
                        break;
                    case R.id.g_item:
                        mBaiduMap.clear();
                        //构建折线点坐标,
                        LatLng p11 = new LatLng(30.527123, 114.405671);
                        LatLng p21 = new LatLng(30.526779, 114.405241);
                        LatLng p31 = new LatLng(30.526149, 114.40724);
                        List<LatLng> points1 = new ArrayList<LatLng>();
                        points1.add(p11);
                        points1.add(p21);
                        points1.add(p31);

                        //设置折线的属性
                        OverlayOptions mOverlayOptions1 = new PolylineOptions()
                                .width(10)
                                .color(0xAAFF0000)
                                .points(points1)
                                .dottedLine(true); //设置折线显示为虚线
                        //mPloyline 折线对象
                        Overlay mPolyline1 = mBaiduMap.addOverlay(mOverlayOptions1);
                        //设置折线显示为虚线
                        ((Polyline) mPolyline1).setDottedLine(true);
                        break;
                    case R.id.h_item:
                        mBaiduMap.clear();
                        //构建折线点坐标
                        List<LatLng> points2 = new ArrayList<LatLng>();
                        points2.add(new LatLng(30.527123, 114.405671));
                        points2.add(new LatLng(30.526779, 114.405241));
                        points2.add(new LatLng(30.526149, 114.40724));
                        points2.add(new LatLng(30.52632,114.407898));
                        points2.add(new LatLng(30.525762,114.408533));

                        List<Integer> colors = new ArrayList<>();
                        colors.add(Integer.valueOf(Color.BLUE));
                        colors.add(Integer.valueOf(Color.RED));
                        colors.add(Integer.valueOf(Color.YELLOW));
                        colors.add(Integer.valueOf(Color.GREEN));

//设置折线的属性
                        OverlayOptions mOverlayOptions2 = new PolylineOptions()
                                .width(10)
                                .color(0xAAFF0000)
                                .points(points2)
                                .colorsValues(colors);//设置每段折线的颜色

//在地图上绘制折线
//mPloyline 折线对象
                        Overlay mPolyline2 = mBaiduMap.addOverlay(mOverlayOptions2);
                        break;
                    case R.id.i_item:
                        BaiduMap.OnPolylineClickListener listener = new BaiduMap.OnPolylineClickListener() {
                            //处理Polyline点击逻辑
                            @Override
                            public boolean onPolylineClick(Polyline polyline) {
                                Toast.makeText(MainActivity.this, "Click on polyline", Toast.LENGTH_LONG).show();

                                return true;
                            }
                        };
                        //设置Polyline点击监听器
                        mBaiduMap.setOnPolylineClickListener(listener);
                        break;
                    case R.id.wenzi:
                        MapTools.addFont(mBaiduMap);
                        break;
                    case R.id.tupian:
                        MapTools.groundOvelay(mBaiduMap);
                        break;
                    case R.id.xixinkuang:
                        addInfoWindow(mBaiduMap);
                        break;
                    case R.id.ddh:
                        MapTools.ddh(mBaiduMap);
                        break;
                    case R.id.panAnimate:
                        MapTools.panAnimate(mBaiduMap);
                        break;
                    case R.id.panJump:
                        MapTools.panJump(mBaiduMap);
                        break;
                    case R.id.customHeatMap:
                        MapTools.customHeatMap(mBaiduMap);
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // 隐藏额头，本来想用这个方式看到指南针，但是也失败了
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

    }

    //添加信息框
    public void addInfoWindow(BaiduMap mBaiduMap) {
        mBaiduMap.clear();
        //文字覆盖物位置坐标
        LatLng point = new LatLng(30.526779, 114.405241);
        //用来构造InfoWindow的Button
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.drawable.popup);
        button.setText("中国地质大学（武汉）");
        //构造InfoWindow
        //point 描述的位置点
        //-100 InfoWindow相对于point在y轴的偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, point, -100);
        //使InfoWindow生效
        mBaiduMap.showInfoWindow(mInfoWindow);
    }
//    打开抽屉
    public void open(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
    }
    //  城市搜索
    public void citySearch(View view) {
        MapTools.citySearch(mBaiduMap);
    }
    //  周边搜索
    public void nearSearch(View view) {
        MapTools.nearSearch(mBaiduMap);
    }
    //  区域搜索
    public void squareSearch(View view) {
        MapTools.squareSearch(mBaiduMap);
    }
    //  地址转坐标
    public void locationToCoor(View view) {
        MapTools.locationToCoor(mBaiduMap, MainActivity.this);
    }
    //  坐标转地址
    public void coorToLocation(View view) {
        MapTools.coorToLocation(mBaiduMap, MainActivity.this);
    }
    //  步行路径规划
    public void walkRoutePlan(View view) {
        MapTools.walkRoutePlan(mBaiduMap, MainActivity.this);
    }
    //  driving路径规划
    public void carRoutePlan(View view) {
        MapTools.carRoutePlan(mBaiduMap, MainActivity.this);
    }
//批量添加overlay
    public void batchAdd(View view){
        mBaiduMap.clear();
        //创建OverlayOptions的集合
        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        //构造大量坐标数据
        LatLng point1 = new LatLng(30.527123, 114.405671);
        LatLng point2 = new LatLng(30.526779, 114.405241);
        LatLng point3 = new LatLng(30.526149, 114.40724);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //创建OverlayOptions属性
        OverlayOptions option1 =  new MarkerOptions()
                .position(point1)
                .icon(bitmap);
        OverlayOptions option2 =  new MarkerOptions()
                .position(point2)
                .icon(bitmap);
        OverlayOptions option3 =  new MarkerOptions()
                .position(point3)
                .icon(bitmap);
        //将OverlayOptions添加到list
        options.add(option1);
        options.add(option2);
        options.add(option3);
        //在地图上批量添加
        mBaiduMap.addOverlays(options);
    }
//    批量删除overlay
    public void batchDelete(View view){
        //清除地图上的所有覆盖物
        mBaiduMap.clear();
    }
    //图片覆盖物
    public void groundOvelay(View view) {
        mBaiduMap.clear();
        //定义Ground的显示地理范围
        LatLng southwest = new LatLng(30.527123, 114.405671);
        LatLng northeast = new LatLng(30.526779, 114.405241);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(northeast)
                .include(southwest)
                .build();
        //定义Ground显示的图片
        BitmapDescriptor bdGround = BitmapDescriptorFactory.fromResource(R.drawable.ground_overlay);
        //定义GroundOverlayOptions对象
        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds)
                .image(bdGround)
                .transparency(0.8f); //覆盖物透明度
        //在地图中添加Ground覆盖物
        mBaiduMap.addOverlay(ooGround);
    }
    public void setTraffic(View view) {
        mBaiduMap.setTrafficEnabled(((CheckBox) view).isChecked());
    }
    public void setBaiduHeatMap(View view) {
        mBaiduMap.setBaiduHeatMapEnabled(((CheckBox) view).isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}