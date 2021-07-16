package com.example.wawgflcomposerealm.model
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Choices(id: String = "", name: String = "", address: String = "") : RealmObject() {
    @PrimaryKey
    var placeId = id
    @Required
    var choiceName = name
    var choiceAddress = address
    var lastSelected: Date? = null
}