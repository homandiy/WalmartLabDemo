package com.huang.homan.walmartlabdemo.Model.ProductPojo

data class Product(
    val inStock: Boolean,
    val longDescription: String,
    val price: String,
    val productId: String,
    val productImage: String,
    val productName: String,
    val reviewCount: Int,
    val reviewRating: Double,
    val shortDescription: String
)