/*
 * Copyright (c) 2019.
 */

package tech.act.coinkits.network.networkBuilder;



import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ToStringConverterFactory extends Converter.Factory {

    @Nullable
    @Override
    public Converter<?, okhttp3.RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    @Nullable
    @Override
    public Converter<okhttp3.ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (String.class.equals(type)) {
            return new Converter<okhttp3.ResponseBody, Object>() {
                @NonNull
                @Override
                public Object convert(@NonNull okhttp3.ResponseBody value) throws IOException {
                    return value.string();
                }
            };
        }
        return super.responseBodyConverter(type, annotations, retrofit);
    }
}