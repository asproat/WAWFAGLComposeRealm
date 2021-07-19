package com.example.wawgflcomposerealm.model

import android.content.Context
import android.location.Location
import com.example.wawgflcomposerealm.data.ChoicesDao
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place

public class LocalChoice {
    lateinit var choiceName: String
    lateinit var choiceAddress: String
    lateinit var placeId: String
             var choiceDistance: Float = 0.0F
             var photoMetadata: String? = null

    companion object
    {
        fun convertResults(context: Context,
                           results: List<Results>,
                           lng: Double, lat: Double) : List<LocalChoice>
        {
            val resultList = mutableListOf<LocalChoice>()
            val choiceData = ChoicesDao()

            val currentLocation = Location("current")
            currentLocation.latitude = lat
            currentLocation.longitude = lng

            for (result in results) {

                // if it's not already in the list
                if(choiceData.getById(result.place_id ?: "") == null) {
                    val choiceLocation = Location("choice")
                    choiceLocation.latitude = result.geometry.location.lat ?: 0.0
                    choiceLocation.longitude = result.geometry.location.lng ?: 0.0

                    val distance = currentLocation.distanceTo(choiceLocation)
                    if (distance <=
                        context
                            .getSharedPreferences("appprefs", Context.MODE_PRIVATE)
                            .getFloat("MaxDistance", 3000.0F)
                    ) {
                        var localChoice = LocalChoice()
                        localChoice.choiceName = result.name ?: ""
                        localChoice.choiceAddress = result.vicinity ?: ""
                        localChoice.choiceDistance = distance
                        localChoice.placeId = result.place_id!!
                        if (result.photos != null && result.photos!!.size > 0) {
                            localChoice.photoMetadata = result.photos!![0].photo_reference
                        }

                        resultList.add(localChoice)
                    }
                }
            }

            return resultList
        }

        fun convertResult(context: Context,
                           placeId: String,
                            placeName: String,
                            placeAddress: String,
                            placeDistance: Double,) : LocalChoice?
        {
            val choiceData = ChoicesDao()
            var localChoice : LocalChoice? = null
                if(choiceData.getById(placeId ?: "") == null) {
4
                        localChoice = LocalChoice()
                        localChoice.choiceName = placeName ?: ""
                        localChoice.choiceAddress = placeAddress ?: ""
                        localChoice.choiceDistance = placeDistance.toFloat()
                        localChoice.placeId = placeId

                    }
            return localChoice
        }
    }
}