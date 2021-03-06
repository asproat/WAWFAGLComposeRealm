package com.example.wawgflcomposerealm.data
import android.content.Context
import com.example.wawgflcomposerealm.model.Choices
import com.example.wawgflcomposerealm.model.History
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class ChoicesDao {
    var realm: Realm

    init
    {
        val config = RealmConfiguration.Builder().name("Choices")
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .build()
        realm = Realm.getInstance(config)
    }

    fun create(id: String, name: String, address: String, transaction: Boolean = false)
    {
        val choice = Choices(id, name, address)
        if(transaction)
        {
            realm.executeTransaction {
                realm.insert(choice)
            }
        }
        else {
            realm.insert(choice)
        }
    }

    fun getChoiceCount() : Int
    {
        realm.refresh()
        return realm.where<Choices>().findAll().count()
    }

    fun getAll(context: Context) : List<Choices>
    {
        realm.refresh() // make sure deleted items are gone
        return realm.where<Choices>().findAll().toList()
    }

    fun getById(id: String) : Choices?
    {
        return realm.where<Choices>().equalTo("placeId", id).findFirst()
    }

    fun deleteById(id: String)
    {
        var choiceToDelete = realm.where<Choices>().equalTo("placeId", id).findFirst()
        var historyToDelete = realm.where<History>().equalTo("placeId", id).sort("visitDate", Sort.DESCENDING).findAll()
        realm.executeTransaction {
            for (history in historyToDelete) {
                history.deleteFromRealm()
            }
            choiceToDelete?.deleteFromRealm()
        }
    }

    fun addVisit(choiceId: String, date: Date = Date())
    {
        realm.executeTransaction {
            val choice = realm.where<Choices>().equalTo("placeId", choiceId).findFirst()
            if(choice != null) {
                choice.lastSelected = date
                val history = History()
                history.historyId = UUID.randomUUID()
                history.placeId = choice.placeId
                history.visitDate = date
                realm.insert(history)
            }
        }
    }

    fun getHistory(choiceId: String) : List<History>
    {
        return realm.where<History>().equalTo("placeId", choiceId).sort("visitDate", Sort.DESCENDING).findAll()
    }
}