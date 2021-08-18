package com.example.mediaplayerapp.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log

class LoginLoader {

    fun loginUserWithEmailIdPassword(request: MediaPlayerLoginRequest, loginListener: LoginListener) {

        MediaPlayerClient.instance?.MediaPlayerRestAdapter("https://identitytoolkit.googleapis.com")
            ?.loginUser(request)?.enqueue(object: Callback<MediaPlayerLoginResponse> {
                override fun onResponse(
                    call: Call<MediaPlayerLoginResponse>,
                    response: Response<MediaPlayerLoginResponse>
                ) {
                        if(response.isSuccessful) {
                            response.body()?.let { loginListener.onLogin(it) }
                        } else {
                            loginListener.onFailure("Login UnSuccessful")
                        }
                }

                override fun onFailure(call: Call<MediaPlayerLoginResponse>, t: Throwable) {
                    t.message?.let { loginListener.onFailure(it) }
                }

            })
    }


}