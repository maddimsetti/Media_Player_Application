package com.example.mediaplayerapp.mediaplayer

import android.net.Uri

class MediaPlayer {
    var url: String? = null
    var title: String? = null
    var content: String? = null
    var dateTime: String? = null

    constructor() {}
    constructor(url: String?, title: String?, content: String?, dateTime: String) {
        this.url = url
        this.title = title
        this.content = content
        this.dateTime = dateTime
    }
}