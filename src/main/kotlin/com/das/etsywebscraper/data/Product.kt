package com.das.etsywebscraper.data

data class Product(
    val title: String,
    val link: String,
    val merchantRating: Double,
    val numberOfRatings: Int,
    val salePrice: Double,
    val isFreeShipping: Boolean,

)
