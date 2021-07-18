package com.example.wawgflcomposerealm.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.text.style.CharacterStyle
import android.util.Base64
import android.util.Log
import com.example.wawgflcomposerealm.model.LocalChoice
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.sync.Semaphore


class PlacesAPI {


    companion object {
        fun getPartial(): String {
            return "QUl6YVN5QkNsLWtPLU5RNm80Zy1MbHhyY1c5WkFMUHFlSVowRlZr"
        }

        fun getValue(): ByteArray {
            return Base64.decode(getPartial(), 0)
        }

        @SuppressLint("MissingPermission")
        fun getPlaces(
            context: Context,
            maxNumber: Int = 50,
            keyword: String = ""
        ): List<LocalChoice> {
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.TYPES,
                Place.Field.PHOTO_METADATAS
            )
            val resultList = mutableListOf<LocalChoice>()
            Places.initialize(context, String(getValue()))
            val placesAPI = Places.createClient(context)

            val lm = context.getSystemService(Context.LOCATION_SERVICE)
                    as LocationManager
            if (lm != null) {

                var location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location == null) {
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location == null) {
                        location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    }
                }
                if (location != null) {
                    val currentLng = location!!.longitude
                    val currentLat = location!!.latitude
                    val boundary = RectangularBounds.newInstance(
                        LatLng(currentLat - 0.05, currentLng + 0.05),
                        LatLng(currentLat + 0.05, currentLng - 0.05)
                    )
                    for (thisQuery in "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray())
                    {
                    val findRequest2 = FindAutocompletePredictionsRequest.builder()
                        .setOrigin(LatLng(currentLat, currentLng))
                        //.setLocationBias(boundary)
                        .setCountries("US")
                        .setLocationRestriction(boundary)
                        .setQuery(thisQuery.toString())
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .build()

                    val maxAllowed = 60 - ChoicesDao().getAll().size

                    val semaphore = Semaphore(1)
                    val placeSemaphore = Semaphore(1)

                    // Google won't return more than 60 for free
                    runBlocking {
                        placesAPI.findAutocompletePredictions(findRequest2).addOnCompleteListener {

                                task ->
                            if (task.isSuccessful) {
                                val placesReturned = mutableListOf<Place>()
                                for (place in task.result.autocompletePredictions) {
                                    for (i in 0..(place.placeTypes.size - 1)) {
                                        if (place.placeTypes[i] == Place.Type.RESTAURANT) {
                                            val newLocalChoice = LocalChoice.convertResult(
                                                context = context,
                                                placeId = place.placeId,
                                                placeName = place.getPrimaryText(null).toString(),
                                                placeAddress = place.getSecondaryText(null)
                                                    .toString(),
                                                placeDistance = (place.distanceMeters ?: 0).toDouble()
                                            )
                                            if (newLocalChoice != null) {
                                                resultList.add(newLocalChoice)
                                            }
                                            break
                                        }
                                    }

                                }
                            }


                            semaphore.release()
                            Log.i("place", "Got 'em")
                        }

                        semaphore.acquire() // grab the first one
                        Log.i("place", "Get 'em")
                        semaphore.acquire()
                        Log.i("place", "Really Got 'em")
                    }
                    }
                }
            }

            return resultList
        }
    }
}
/*
suspend private fun fetchPlaces(context: Context,
                    service: PlacesEndpoints,
                    resultList : MutableList<LocalChoice>,
                    currentLat: Double,
                    currentLng: Double,
                    maxNumber: Int,
                    nextPage: String = "") {

 var apiCall = service.getPlace(
    key = String(getValue()),
    location = String.format(
        "%f,%f",
        currentLat, currentLng
    )
)
if (nextPage != "")
{
    apiCall = service.getNextPlace(
        key = String(getValue()),
        nextPage = nextPage
    )
}

withContext(Dispatchers.IO)
{
    // let's wait a half second to see if that helps with the null next pages
    Thread.sleep(1500)
    var newNextPage = nextPage
    val response = apiCall.execute()
    if (response.isSuccessful) {
        val thisResponse = response.body()

        if(thisResponse!!.next_page_token != null)
        {
            newNextPage = thisResponse!!.next_page_token
            Log.i("nextpage", newNextPage ?: "NULL")
            resultList.addAll(
                LocalChoice.convertResults(
                    context,
                    thisResponse!!.results,
                    currentLng,
                    currentLat
                )
            )
        }
        if(resultList.size < maxNumber && thisResponse!!.next_page_token != null)
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
 */