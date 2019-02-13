/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.network.networkBuilder

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ServiceBuilder {
    companion object {
        private var instance: CoinService? = null
        private const val baseUrl = "https://api.datamuse.com/"
        private val serviceMap: HashMap<String, CoinService> = HashMap()
        fun getService(): CoinService {
            if (serviceMap[baseUrl] == null) {
                instance = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addConverterFactory(ToStringConverterFactory())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build().create(CoinService::class.java)
                serviceMap[baseUrl] = instance!!
            }
            return serviceMap[baseUrl]!!
        }

        fun getServiceNewBase(newBaseUrl: String): CoinService {
            if (serviceMap[newBaseUrl] == null) {
                instance = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addConverterFactory(ToStringConverterFactory())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build().create(CoinService::class.java)
                serviceMap[newBaseUrl] = instance!!
            }
            return serviceMap[newBaseUrl]!!
        }
    }
}