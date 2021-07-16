package com.example.wawgflcomposerealm.model
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class History(): RealmObject() {
    @PrimaryKey
    lateinit var historyId: UUID
    @Required
    lateinit var placeId: String
    lateinit var visitDate: Date
}