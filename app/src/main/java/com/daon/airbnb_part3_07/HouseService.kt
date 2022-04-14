package com.daon.airbnb_part3_07

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/b96b31e4-0342-46a5-aa40-38792d5f3e24")
    fun getHouseList(): Call<HouseDto>
}