package com.example.wawgflcomposerealm.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.example.wawgflcomposerealm.model.LocalChoice
import com.example.wawgflcomposerealm.model.PlacesResponse
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class PlacesAPI {

    companion object {
        @SuppressLint("MissingPermission")
        fun getPlaces(
            context: Context,
            maxNumber: Int = 100,
            keyword: String = ""
        ): List<LocalChoice> {
            val resultList = mutableListOf<LocalChoice>()
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            val service: PlacesEndpoints = retrofit.create(PlacesEndpoints::class.java)

            val lm = context.getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager
            if (lm != null) {
                val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    val currentLng = location!!.longitude
                    val currentLat = location!!.latitude

                    runBlocking {
                        val job: Job = launch {
                            fetchPlaces(
                                context,
                                service,
                                resultList,
                                currentLat,
                                currentLng,
                                maxNumber,
                                ""
                            )
                        }
                        job.join()
                        Log.i("place", "Got 'em")
                    }
                }
            }

        return resultList
    }

    suspend private fun fetchPlaces(context: Context,
                            service: PlacesEndpoints,
                            resultList : MutableList<LocalChoice>,
                            currentLat: Double,
                            currentLng: Double,
                            maxNumber: Int,
                            nextPage: String = "") {

         var apiCall = service.getPlace(
            key = "AIzaSyDO2xTjMtvtXniJK1Idby9uKU8-oQo8g9s",
            location = String.format(
                "%f,%f",
                currentLat, currentLng
            )
        )
        if (nextPage != "")
        {
            apiCall = service.getNextPlace(
                key = "AIzaSyDO2xTjMtvtXniJK1Idby9uKU8-oQo8g9s",
                nextPage = nextPage
            )
        }

        withContext(Dispatchers.IO)
        {
            var newNextPage = nextPage
            val response = apiCall.execute()
            if (response.isSuccessful) {
                val thisResponse = response.body()
                if(thisResponse!!.next_page_token != null)
                {
                    newNextPage = thisResponse!!.next_page_token
                }
                Log.i("nextpage", newNextPage ?: "NULL")
                    resultList.addAll(
                        LocalChoice.convertResults(
                            context,
                            thisResponse!!.results,
                            currentLng,
                            currentLat
                        )
                    )
                if(resultList.size < maxNumber && newNextPage != null)
                {
                    fetchPlaces(context,
                        service,
                        resultList,
                        currentLat,
                        currentLng,
                        maxNumber,
                        newNextPage ?: "")
                }
            }
        }
    }
}
    private interface PlacesEndpoints
    {
        @GET("/maps/api/place/nearbysearch/json")
        fun getPlace(
            @Query("key") key: String,
            @Query("location") location: String,
            @Query("type") type : String = "restaurant",
            @Query("rankby") rankBy: String = "distance",
            @Query("keyword") keyword: String = "",
            @Query("pagetoken") nextPage: String = ""
        ) : Call<PlacesResponse>

        @GET("/maps/api/place/nearbysearch/json")
        fun getNextPlace(
            @Query("key") key: String,
            @Query("pagetoken") nextPage: String = ""
        ) : Call<PlacesResponse>

    }
}