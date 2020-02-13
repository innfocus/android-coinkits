package tech.act.coinkits.cardano.networking

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UserAgentInterceptor : Interceptor {

    companion object {
        private val userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36 Edg/80.0.361.48"
    }
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        val originalRequest: Request = chain.request()
        val requestWithUserAgent: Request = originalRequest.newBuilder()
                .header("User-Agent", userAgent)
                .build()
        return chain.proceed(requestWithUserAgent)
    }
}