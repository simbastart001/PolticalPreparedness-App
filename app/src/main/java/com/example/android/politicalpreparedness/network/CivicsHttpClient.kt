package com.example.android.politicalpreparedness.network

import com.example.android.politicalpreparedness.BuildConfig
import okhttp3.OkHttpClient

/**
 * @DrStart:    This class is used to add the API_KEY to the request. It is used in the following
 *             classes: [CivicsHttpClient], [ElectionApiService], [RepresentativeApiService] and
 *             [VoterInfoApiService]. It is used in the following methods:
 * */
class CivicsHttpClient : OkHttpClient() {

    companion object {

        const val API_KEY = BuildConfig.API_KEY

        fun getClient(): OkHttpClient {
            return Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original
                        .url
                        .newBuilder()
                        .addQueryParameter("key", API_KEY)
                        .build()
                    val request = original
                        .newBuilder()
                        .url(url)
                        .build()
                    chain.proceed(request)
                }
                .build()
        }

    }

}