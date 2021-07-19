package com.example.wawgflcomposerealm

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wawgflcomposerealm.data.ChoicesDao
import com.example.wawgflcomposerealm.model.Choices
import com.example.wawgflcomposerealm.model.History
import com.example.wawgflcomposerealm.ui.theme.WAWGFLComposeRealmTheme
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import java.text.SimpleDateFormat

@ExperimentalUnitApi
@Composable
fun HistoryList(navController: NavController, choiceId: String? = null) {
    val state = rememberLazyListState()
    val historyItems = mutableListOf<History>()
    var historyTitle = "All Visits"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mma")
    val choiceData = ChoicesDao()
    val context = LocalContext.current as MainActivity
    if(choiceId == null)
    {
        historyItems.addAll(choiceData.realm.where<History>().sort("visitDate", Sort.DESCENDING).findAll())
    }
    else
    {
        historyItems.addAll(choiceData.getHistory(choiceId))
        historyTitle = String.format("%s History",
            context.placeNames[choiceId] ?: "Unknown")
    }
    BackHandler(onBack = {
        navController.popBackStack()
    })

    Column(modifier = Modifier
        .fillMaxSize()){
        Text(historyTitle,
            fontSize = 24.0.sp, // TextUnit(24.0F, TextUnitType.Sp)
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .weight(300.0F)
                .padding(10.dp)
                .fillMaxHeight(0.1F)
        )


            LazyColumn(
                state = state,

                modifier = Modifier
                    .fillMaxHeight(0.9F)
                    .fillMaxWidth()
            ) {
                items(historyItems)
                { item ->
                    var choiceName = ""
                    if (choiceId == null) {
                        val choiceData = ChoicesDao()
                        choiceName = String.format(
                            " - %s",
                            context.placeNames[item.placeId] ?: "Unknown"
                        )
                    }
                    Text(
                        dateFormat.format(item.visitDate) + choiceName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

    }
}

@ExperimentalUnitApi
@Preview
@Composable
fun MyPreview2()
{
    Realm.init(LocalContext.current)
    val nav = rememberNavController()
    WAWGFLComposeRealmTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            HistoryList(nav)
        }
    }
}