package com.dnastack.beacon.compliance.util

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.MessageLite
import com.google.protobuf.util.JsonFormat
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Type

/**
 * A simple converter Json <-> protobuf DTOs.
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
public class ProtoJsonConverter extends Converter.Factory {
    @Override
    Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<ResponseBody, Object>() {
            @Override
            Object convert(ResponseBody responseBody) throws IOException {
                if (!(type instanceof Class)) {
                    return null;
                }

                if (!(MessageLite.class.isAssignableFrom(type))) {
                    return null;
                }

                Class clazz = (Class) type;
                Method newBuilderMethod = clazz.getMethod("newBuilder");
                GeneratedMessage.Builder newBuilder = (GeneratedMessage.Builder) newBuilderMethod.invoke(null, null)

                def json = responseBody.string()
                JsonFormat.parser().merge(json, newBuilder)

                return newBuilder.build()
            }
        }
    }

    @Override
    Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new Converter<Object, RequestBody>() {
            @Override
            RequestBody convert(Object o) throws IOException {
                if (!(type instanceof Class)) {
                    return null;
                }

                if (!(MessageLite.class.isAssignableFrom(type))) {
                    return null;
                }

                MessageLite message = (MessageLite) o;
                String json = JsonFormat.printer().print(message)

                return RequestBody.create(MediaType.parse("application/json"), json);
            }
        }
    }

    @Override
    Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return super.stringConverter(type, annotations, retrofit)
    }

    public static ProtoJsonConverter create() {
        return new ProtoJsonConverter();
    }
}
