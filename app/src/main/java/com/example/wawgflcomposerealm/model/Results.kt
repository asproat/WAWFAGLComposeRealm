package com.example.wawgflcomposerealm.model
import com.google.gson.annotations.SerializedName
/*
Copyright (c) 2021 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */


data class Results (

	@SerializedName("business_status") val business_status : String,
	@SerializedName("geometry") val geometry : Geometry,
	@SerializedName("icon") val icon : String,
	@SerializedName("name") val name : String,
	@SerializedName("opening_hours") val opening_hours : Opening_hours,
	@SerializedName("photos") val photos : List<Photos>,
	@SerializedName("place_id") val place_id : String,
	@SerializedName("plus_code") val plus_code : Plus_code,
	@SerializedName("price_level") val price_level : Int,
	@SerializedName("rating") val rating : Double,
	@SerializedName("reference") val reference : String,
	@SerializedName("scope") val scope : String,
	@SerializedName("types") val types : List<String>,
	@SerializedName("user_ratings_total") val user_ratings_total : Int,
	@SerializedName("vicinity") val vicinity : String
)