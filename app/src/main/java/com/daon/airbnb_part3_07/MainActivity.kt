package com.daon.airbnb_part3_07

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private val mapView : MapView by lazy {
        findViewById(R.id.mapView)
    }
    private val viewPager : ViewPager2 by lazy {
        findViewById(R.id.houseViewPager)
    }
    private val recyclerView : RecyclerView by lazy {
        findViewById(R.id.recyclerView)
    }
    private val currentLocationButton : LocationButtonView by lazy {
        findViewById(R.id.currentLocationButton)
    }
    private val bottomSheetTitleTextView : TextView by lazy {
        findViewById(R.id.bottomSheetTitleTextView)
    }

    private val recyclerViewAdapter = HouseListAdapter()
    private val viewPagerAdapter = HouseViewPagerAdapter(itemClickListener = {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "[지금 이 가격에 예약하세요!!] ${it.title} ${it.price} 사진보기 : ${it.imgUrl}")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, null))
    })
    private lateinit var naverMap : NaverMap
    private lateinit var locationSource : FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // activity 의 생명주기 연결
        mapView.onCreate(savedInstanceState)
        // map 의 정보를 관리하는 객체 가져오기
        mapView.getMapAsync(this)

        viewPager.adapter = viewPagerAdapter
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                    .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    override fun onMapReady(map: NaverMap) {
        // mapView 는 view 관련된 기능만 제공
        naverMap = map

        // 1. zoom level 설정
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        // 2. 초기 위치 설정
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.49804648065241, 127.02769178932371))
        naverMap.moveCamera(cameraUpdate)

        // 3. 현 위치 버튼 생성
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false
        currentLocationButton.map = naverMap

        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        // 4. 마커 생성
//        val marker = Marker()
//        marker.position = LatLng(37.502760657638845, 127.02965931387183)
//        marker.map = naverMap
////        marker.icon = MarkerIcons.BLACK
//        marker.icon = OverlayImage.fromResource(R.drawable.ic_launcher_foreground)

        getHouseListFromAPI()
    }

    private fun getHouseListFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java)
            .also {
                it.getHouseList()
                    .enqueue(object : Callback<HouseDto> {
                        override fun onResponse(
                            call: Call<HouseDto>,
                            response: Response<HouseDto>
                        ) {
                            if (response.isSuccessful.not()) {
                                // TODO 실패 처리에 대한 구현
                                return
                            }

                            response.body()?.let { dto ->
                                updateMarker(dto.items)
                                viewPagerAdapter.submitList(dto.items)
                                recyclerViewAdapter.submitList(dto.items)
                                bottomSheetTitleTextView.text = "${dto.items.size}개의 숙소"
                            }
                        }

                        override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                            // TODO 실패 처리에 대한 구현
                        }
                    })
            }
    }

    private fun updateMarker (houses : List<HouseModel>) {
        houses.forEach { house ->
            val marker = Marker()
            marker.position = LatLng(house.lat, house.lng)
            marker.map = naverMap
            marker.tag = house.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.GREEN

            // TODO marker click listener
            marker.onClickListener = this
        }
    }

    override fun onClick(overlay: Overlay): Boolean {
        viewPagerAdapter.currentList.firstOrNull { it.id == overlay.tag }
            ?.let {
                val position = viewPagerAdapter.currentList.indexOf(it)
                viewPager.currentItem = position
            }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (locationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)) {
            if (!locationSource.isActivated) {
                // 권한이 거부 되었음을 알려준다.
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    override fun onStart() {
        super.onStart()
        // activity 의 생명주기 연결
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        // activity 의 생명주기 연결
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // activity 의 생명주기 연결
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // activity 의 생명주기 연결
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        // activity 의 생명주기 연결
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // activity 의 생명주기 연결
        mapView.onDestroy()
    }

    // 메모리가 부족할 때 호출되는 메서드
    override fun onLowMemory() {
        super.onLowMemory()
        // activity 의 생명주기 연결
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}