package com.das.etsywebscraper

import com.das.etsywebscraper.data.PageOfProducts
import com.das.etsywebscraper.data.Product
import com.das.webcrawlertools.Log
import com.das.webcrawlertools.WebCrawlerTools
import com.das.webcrawlertools.w
import org.jsoup.nodes.Element
import java.lang.Exception


@Suppress("unused")
class EtsyWebScraper{

    private val locTag = EtsyWebScraper::class.java.simpleName.toString()

    private val baseCategoryEndpoint = "https://www.etsy.com/c/"

    private val pageQuery = "?ref=pagination&page="

    private fun scrapePage(url: String): PageOfProducts?{
        try{

            val doc = WebCrawlerTools.parseUrl(url) ?: return null

            val pageNumber = url.substringAfter("page=").toIntOrNull() ?: 1

            val targetTags = mutableListOf<Element>()

            val liTags = doc.select("li").forEach {
                val classType = it.attr("class")
                if (classType.contains("wt-show-xs wt-show-md wt-show-lg")){
                    targetTags.add(it)
                }
            }

            // sort through targets + Build data
            val products = mutableListOf<Product>()
            for(t in targetTags){

                // Extract link to the product
                val linkToProduct = t.select("a").attr("href").toString()
                val title = linkToProduct.substringBefore("?").substringAfterLast("/")
                var rating: String? = ""
                var nOfRatings: String? = ""
                var price: String? = ""
                var isFreeShipping = false

                // Extract the com.das.etsywebcrawler.data.Product Info Card
                val divTags = t.select("div").forEach { d ->
                    if(d.attr("class").contains("v2-listing-card__info")){


                        val allTextNodes = d.select("div.wt-align-items-center")
                            .select("span").textNodes()


                        allTextNodes.removeAll {
                            it.isBlank || it.text() == "$"
                        }

                        allTextNodes.forEachIndexed { index, textNode ->

                            when (index) {

                                // Extract rating
                                0 -> {
                                    rating = textNode.text().substringBefore(" out of")
                                }

                                // Extract Number of Ratings
                                1 -> {
                                    nOfRatings = textNode.text().substringAfter("(")
                                        .substringBefore(")")
                                }

                                // Extract Price
                                2 -> {
                                    price = if (textNode.text().contains("Sale Price")){
                                        textNode.text().substringAfter("Sale Price $")
                                    }else{
                                        textNode.text()
                                    }
                                }
                                allTextNodes.lastIndex -> {
                                    if (textNode.text().contains("FREE shipping")){
                                        isFreeShipping = true
                                    }
                                }
                            }
                        }

                        if (!rating.isNullOrBlank()
                            && !nOfRatings.isNullOrBlank()
                            && !price.isNullOrBlank()){
                            products.add(
                                Product(
                                    title = title,
                                    link = linkToProduct,
                                    merchantRating = rating!!.toDouble() / 5.0,
                                    numberOfRatings = nOfRatings!!.replace(",", "").toInt(),
                                    salePrice = price!!.replace(",", "").toDouble(),
                                    isFreeShipping = isFreeShipping
                                )
                            )
                        }
                    }
                }
            }

            return PageOfProducts(pageNumber, products)
        }catch (e: Exception){
            Log.w(locTag, "Exception Triggered Scraping Etsy Category Page. URL: $url, Exception: $e, " +
                    "Stacktrace: ${e.stackTraceToString()}")
            return null
        }
    }

    fun getProductsByCategory(cat: ProductCategory, page: Int? = null): PageOfProducts?{
        return scrapePage(
            baseCategoryEndpoint + cat.category + pageQuery + "${page ?: 1}"
        )
    }

    fun getProductsByUrl(url: String): PageOfProducts?{
        return scrapePage(url)
    }
}


fun main() {
    println(EtsyWebScraper().getProductsByCategory(ProductCategory.JewelryAndAccessories))
}