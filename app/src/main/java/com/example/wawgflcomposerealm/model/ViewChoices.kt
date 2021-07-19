package com.example.wawgflcomposerealm.model

import java.util.*

data class ViewChoices (
        var placeId : String = "",
        var choiceName : String = "",
        var choiceAddress : String = "",
        var lastSelected: Date? = null
)
