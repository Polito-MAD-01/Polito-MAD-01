package com.example.polito_mad_01.model

data class User(
    var name: String,
    var surname: String,
    var nickname: String,
    var birthdate: String,
    val gender: String,
    var location: String,
    var achievements: List<String>,
    var skills: MutableMap<String, String>,
    var image_uri : String?,
    var email : String,
    var phoneNumber : String,
    val availability : MutableMap<String,Boolean>
) {

    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        listOf(),
        mutableMapOf(),
        null,
        "",
        "",
        mutableMapOf()
    )
    override fun toString(): String =
        "$name $surname $nickname $birthdate $gender $location $achievements $skills"
}


