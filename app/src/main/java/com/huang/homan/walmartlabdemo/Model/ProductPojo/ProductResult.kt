package com.huang.homan.walmartlabdemo.Model.ProductPojo

data class ProductResult(
    val pageNumber: Int,
    val pageSize: Int,
    val products: List<Product>,
    val statusCode: Int,
    val totalProducts: Int
)