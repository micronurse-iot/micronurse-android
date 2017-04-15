package org.micronurse.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Date;

/**
 *
 * @author buptsse-zero <GGGZ-1101-28@Live.cn>
 */
public class GsonUtil {
    private static final GsonBuilder defaultBuilder = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Bitmap.class, new BitmapAdapter());
    private static final Gson defaultGson = defaultBuilder.create();

    public static GsonBuilder getDefaultGsonBuilder(){
        return defaultBuilder;
    }

    public static Gson getGson(){
        return defaultGson;
    }

    private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime() / 1000);
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsLong() * 1000);
        }
    }

    private static class BitmapAdapter implements JsonSerializer<Bitmap>, JsonDeserializer<Bitmap>{
        @Override
        public JsonElement serialize(Bitmap src, Type typeOfSrc, JsonSerializationContext context) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return new JsonPrimitive(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
        }

        @Override
        public Bitmap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            byte[] bitmapByteArray;
            bitmapByteArray = Base64.decode(json.getAsString(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
        }
    }
}
