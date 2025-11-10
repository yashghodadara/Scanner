package com.example.scanner.language

data class Language(
    val code: String,
    val name: String,
    val languageName: String,
    val flagIcon: Int,
    var isSelected: Boolean = false

)