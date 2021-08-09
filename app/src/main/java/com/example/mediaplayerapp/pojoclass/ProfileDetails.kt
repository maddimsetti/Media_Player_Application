package com.example.mediaplayerapp.pojoclass

class ProfileDetails {
    var fullName: String? = null
    var email: String? = null
    var phoneNumber: String? = null

    constructor() {}
    constructor(fullName: String?, email: String?, phoneNumber: String) {
        this.fullName = fullName
        this.email = email
        this.phoneNumber = phoneNumber
    }
}