package com.example.wawgflcomposerealm.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.example.wawgflcomposerealm.data.ChoicesDao

public class LocalChoice {
    lateinit var choiceName: String
    lateinit var choiceAddress: String
    lateinit var placeId: String
             var choiceDistance: Float = 0.0F
    lateinit var photoReference: String

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
                if(choiceData.getById(result.place_id) == null) {
                    val choiceLocation = Location("choice")
                    choiceLocation.latitude = result.geometry.location.lat
                    choiceLocation.longitude = result.geometry.location.lng

                    val distance = currentLocation.distanceTo(choiceLocation)
                    if (distance <=
                        context
                            .getSharedPreferences("appprefs", Context.MODE_PRIVATE)
                            .getFloat("MaxDistance", 3000.0F)
                    ) {
                        var localChoice = LocalChoice()
                        localChoice.choiceName = result.name
                        localChoice.choiceAddress = result.vicinity
                        localChoice.choiceDistance = distance
                        localChoice.placeId = result.place_id
                        if (result.photos != null && result.photos.size > 0) {
                            localChoice.photoReference = result.photos[0].photo_reference
                        } else {
                            localChoice.photoReference = ""
                        }

                        resultList.add(localChoice)
                    }
                }
            }

            return resultList
        }
    }
}