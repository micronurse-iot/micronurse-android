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

/**
 *
 * @author buptsse-zero <GGGZ-1101-28@Live.cn>
 */
public class GsonUtil {
    private static GsonBuilder getDefaultGsonBuilder(){
        return new GsonBuilder()
                   .setDateFormat("yyyy-MM-dd HH:mm:ss")
                   .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                   .registerTypeAdapter(Bitmap.class, new BitmapAdapter());
    }
    
    public static Gson getGson(){
        return getDefaultGsonBuilder().create();
    }
}

class BitmapAdapter implements JsonSerializer<Bitmap>, JsonDeserializer<Bitmap>{
    @Override
    public JsonElement serialize(Bitmap src, Type typeOfSrc, JsonSerializationContext context) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new JsonPrimitive(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
    }

    @Override
    public Bitmap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        byte[] bitmapByteArray;
        Log.i("Bitmap Adapter", "deserialize: " + json.getAsString());
        bitmapByteArray = Base64.decode(json.getAsString(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
    }
}
