package com.example.mediaplayerapp.mediaplayer

class MediaPlayer {

    constructor() {}
    var url: String? = null
    var title: String? = null
    var content: String? = null
    var dateTime: String? = null


    constructor(url: String?, title: String?, content: String?, dateTime: String) {
        this.url = url
        this.title = title
        this.content = content
        this.dateTime = dateTime
    }
}