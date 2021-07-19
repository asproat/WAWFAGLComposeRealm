package com.example.wawgflcomposerealm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.wawgflcomposerealm.ui.theme.WAWGFLComposeRealmTheme
import androidx.compose.ui.unit.*
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.wawgflcomposerealm.data.ChoicesDao
import com.example.wawgflcomposerealm.model.Choices
import io.realm.Realm
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    var currentChoiceId = ""
    var firstTime = true
    var rand = Random(Date().time)

    @ExperimentalUnitApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        // set up Realm
        Realm.init(applicationContext)
        setContent {
            val nav = rememberNavController()

            NavHost(navController = nav, startDestination = "home")
            {
                composable("home")
                {
                    TextAndButton(nav)
                }
                composable(
                    "choiceHistoryList/{placeId}",
                    arguments = listOf(navArgument("placeId") { type = NavType.StringType })
                )
                {
                    HistoryList(nav,it.arguments?.getString("placeId") )
                }
                composable("allHistoryList")
                {
                    HistoryList(nav, null)
                }
                composable("addChoices")
                {
                    AddChoices(nav)
                }
            }
        }
    }


}

@ExperimentalUnitApi
@Composable
fun TextAndButton(nav: NavController) {
    val choiceData = ChoicesDao()
    val context = (LocalContext.current as MainActivity)
    var firstTime = context.firstTime
    var showWait: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    context.firstTime = false
    var rand = (LocalContext.current as MainActivity).rand
    val state = rememberLazyListState()
    val list : SnapshotStateList<Choices> = remember{mutableStateListOf<Choices>()}
    val name by remember {
        derivedStateOf {
            try {
                list[state.firstVisibleItemIndex + 2].choiceAddress
            }
            catch(e: Exception)
            {
                ""
            }
        }
    }

    fun refreshList() {
        list.clear()

        list.add(Choices()) // blank for top line
        list.add(Choices()) // blank for top line
        list.addAll(choiceData.getAll())
        val listCount = list.size

        for (i in 1..(50 / listCount)) {
            for (j in 2.until(2 + listCount - 2)) {
                val choice = list[j]
                list.add(
                    Choices(
                        choice.placeId,
                        choice.choiceName,
                        choice.choiceAddress
                    )
                )
            }
        }
        list.add(Choices()) // blank for bottom line
        list.add(Choices()) // blank for bottom line
    }

    val coroutineScope = rememberCoroutineScope()

    suspend fun pickAPlace(list: MutableList<Choices>, state: LazyListState, rand: Random) {
        if(list.size > 4) {
            showWait.value = true
            for (i in 1..4) {
                val selectedIndex = rand.nextInt(list.size - 4) + 4
                state.animateScrollToItem(selectedIndex, 2)
            }
            showWait.value = false
        }
    }

    LaunchedEffect(key1 = "one", block = {
        refreshList()
        if (choiceData.getAll().size < 5) {
            nav.navigate("addChoices")
        }
        pickAPlace(list,state, rand)
    })

    Column {
        LazyColumn(state = state, modifier = Modifier.height(166.dp))
        {
            itemsIndexed(list)
            { index, item ->
                Text(
                    item.choiceName,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = TextUnit(24.0F, TextUnitType.Sp),
                    color = if (index == state.firstVisibleItemIndex + 2)
                        Color.Black
                    else
                        Color.LightGray
                )
            }
        }
        Greeting(name)
        Button(
            onClick = {
                coroutineScope.launch {
                    pickAPlace(list, state, rand)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        )
        {
            Text(
                "Pick Again",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
        Button(
            onClick = {
                coroutineScope.launch {
                    val selectedChoice = list[state.firstVisibleItemIndex + 2]
                    context.currentChoiceId = selectedChoice.placeId
                    val updateChoice = choiceData.getById(selectedChoice.placeId)
                    if (updateChoice != null) {
                        choiceData.addVisit(updateChoice)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        )
        {
            Text(
                "We're Going Here",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
        Button(
            onClick = {
                coroutineScope.launch {
                    val selectedChoice = list[state.firstVisibleItemIndex + 2]
                    Log.i("delete","before delete")
                    choiceData.deleteById(selectedChoice.placeId)
                    Log.i("delete","before refresh")
                    refreshList()
                    Log.i("delete","before pick")
                    pickAPlace(list, state, rand)
                    Log.i("delete","after pick")
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        )
        {
            Text(
                "Delete Choice",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
        Button(
            onClick = {
                val item = list[state.firstVisibleItemIndex + 2]
                nav.navigate("choiceHistoryList/${item.placeId}")
                {}
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        )
        {
            Text(
                "Show Selected Place History",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
        Button(
            onClick = {
                nav.navigate("allHistoryList")
                {}
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        )
        {
            Text(
                "Show All History",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
        Button(
            onClick = {
                showWait.value = true
                nav.navigate("addChoices")
                {}
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .padding(10.dp)
        )
        {
            Text(
                "Add Choices",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize())
    {
        if (showWait.value) {
            CircularProgressIndicator(color = Color.LightGray)
        }
    }
}



@ExperimentalUnitApi
@Composable
fun Greeting(name: String) {
    Text(
        text = "Address:\n$name",
        fontSize = TextUnit(12.0F, TextUnitType.Sp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    )
}


@ExperimentalUnitApi
@Preview
@Composable
fun MyPreview()
{

    val list: MutableList<Choices> = mutableListOf(
        Choices("123", "One", "A"),
        Choices("231", "Two", "B"),
        Choices("312", "Three", "C"),
        Choices("213", "Four", "D"),
        Choices("132", "Five", "E"),
        Choices("321", "Six", "F"),
    )

    val nav = rememberNavController()
    WAWGFLComposeRealmTheme {
        TextAndButton(nav)
    }
}