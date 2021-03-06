package com.example.wawgflcomposerealm

import android.util.Base64
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.wawgflcomposerealm.data.ChoicesDao
import com.example.wawgflcomposerealm.data.PlacesAPI
import com.example.wawgflcomposerealm.model.Choices
import com.example.wawgflcomposerealm.model.LocalChoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@Composable
fun AddChoices(nav: NavController)
{
    val context = LocalContext.current
    val state = rememberLazyListState()
    val list : SnapshotStateList<LocalChoice> = remember{mutableStateListOf<LocalChoice>()}
    val myActivity = LocalContext.current as MainActivity
    var showWait: MutableState<Boolean> = remember { mutableStateOf(true) }

    val partial = "QUl6YVN5QUx5allFUjhtOTNIMVBmTjFPTmdlQ1JINU9tTzkzbkxR"

    val value = Base64.decode(partial, 0)

    suspend fun loadChoices()
    {
        return withContext(Dispatchers.IO)
        {
            list.addAll(PlacesAPI.getPlaces(context))
            showWait.value = false
        }
    }

    LaunchedEffect(key1 = "one", block = {
        loadChoices()
    })

    BackHandler(onBack = {
        nav.popBackStack()
    })

    Column()
    {
        LazyColumn(state = state,
            modifier = Modifier.fillMaxSize()) {
            itemsIndexed(list)
            {
                index, item ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showWait.value = true
                        val choiceData = ChoicesDao()
                        choiceData.create(
                            item.placeId,
                            name = "",
                            address = "",
                            transaction = true
                        )
                        list.remove(item)
                        showWait.value = false
                        myActivity.firstTime = true

                    }
                   )
                {
                    Image(painter = rememberImagePainter(
                        String.format(
                            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=50&key=%s&photoreference=%s",
                            String(value),
                            item.photoMetadata)),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp))
                    Column() {
                        Text(
                            item.choiceName,
                            fontSize = 18.0.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            String.format("%.1f miles", item.choiceDistance * 0.000621371F),
                            fontSize = 12.0.sp
                        )
                    }
                }
            }
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    )
    {
        if (showWait.value) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(0.25F)
            )
        }
    }

}

