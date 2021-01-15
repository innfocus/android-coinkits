package tech.act.coinkits.cardano.networking

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UserAgentInterceptor : Interceptor {

    companion object {
        private const val userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36 Edg/83.0.478.37"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestWithUserAgent: Request = originalRequest.newBuilder()
//                .header("User-Agent", userAgent)
                .header("yoroi-version", "android / 2.2.0")
                .header("tangata-manu", "yoroi")
                .build()
        return chain.proceed(requestWithUserAgent)
    }
}