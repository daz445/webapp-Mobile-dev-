package ru.dmitryzyrynov.cw

data class Product(
    val id: Int,
    var isFinish: Int,
    val name: String,
    val change: String,
    val imageResId: Int)

