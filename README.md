# micronurse-android
The Android client of Micro Nurse IoT application.

## Build Properties

Before building, you should set the following properties in `gradle.properties`:

+ `MN_HTTP_API_BASE_URL_V1`: The base URL of HTTP API(v1 version) that client will use to interact with web server. The web server that this URL points to should running project [micronurse-webserver-django](https://github.com/micronurse-iot/micronurse-webserver-django).
+ `MN_MQTT_BROKER_URL`: The URL of MQTT broker that client will interact with. The MQTT broker should running project [micronurse-mqtt-broker-mosca](https://github.com/micronurse-iot/micronurse-mqtt-broker-mosca).
+ `MN_KEYSTORE_FILE`: The keystore file used to generate signed APK.
+ `MN_KEYSTORE_PASSWORD`: Keystore password of `MN_KEYSTORE_FILE`.
+ `MN_KEYSTORE_ALIAS_NAME`: An alias that exist in `MN_KEYSTORE_FILE`.
+ `MN_KEYSTORE_ALIAS_PASSWORD`: Corresponding password of `MN_KEYSTORE_ALIAS_NAME`.
+ `BAIDU_LBS_API_KEY`: The API key of Baidu Map SDK. For more details about Baidu Map SDK, visit [http://lbsyun.baidu.com](http://lbsyun.baidu.com)