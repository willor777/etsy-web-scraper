package com.das.etsywebscraper.data

data class PageOfProducts(
    val pageNumber: Int,
    val products: List<Product>
){
    fun toStringPretty(): String{
        var base = "Page Of Products Number: $pageNumber\n" +
                "Title | Sale Price | Free Shipping | Number Of Ratings | Merchant Rating | Link\n"

        for (p in products){
            base += "${p.title} | ${p.salePrice} | ${p.isFreeShipping} | ${p.numberOfRatings} " +
                    "| ${p.merchantRating} | ${p.link}\n"
        }
        return base
    }
}
