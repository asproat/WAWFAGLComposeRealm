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
                           results: List<Place>,
                           lng: Double, lat: Double) : List<LocalChoice>
        {
            val resultList = mutableListOf<LocalChoice>()
            val choiceData = ChoicesDao()

            val currentLocation = Location("current")
            currentLocation.latitude = lat
            currentLocation.longitude = lng

            for (result in results) {

                // if it's not already in the list
                if(choiceData.getById(result.id ?: "") == null) {
                    val choiceLocation = Location("choice")
                    choiceLocation.latitude = result.latLng?.latitude ?: 0.0
                    choiceLocation.longitude = result.latLng?.longitude ?: 0.0

                    val distance = currentLocation.distanceTo(choiceLocation)
                    if (distance <=
                        context
                            .getSharedPreferences("appprefs", Context.MODE_PRIVATE)
                            .getFloat("MaxDistance", 3000.0F)
                    ) {
                        var localChoice = LocalChoice()
                        localChoice.choiceName = result.name ?: ""
                        localChoice.choiceAddress = result.address ?: ""
                        localChoice.choiceDistance = distance
                        localChoice.placeId = result.id!!
                        if (result.photoMetadatas != null && result.photoMetadatas!!.size > 0) {
                            localChoice.photoMetadata = result.photoMetadatas!![0].zza()
                        }

                        resultList.add(localChoice)
                    }
                }
            }

            return resultList
        }
    }
}