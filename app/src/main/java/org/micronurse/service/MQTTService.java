package org.micronurse.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.micronurse.Application;
import org.micronurse.util.GlobalInfo;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class MQTTService extends Service implements MqttCallback {
    public static final String BROKER_URL = "tcp://micronurse-mqttbroker:13883";
    public static final String CLIENT_ID_PREFIX = "micronurse_mobile_user:";
    public static final String USERNAME_PREFIX = "micronurse_mobile_user:";

    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;
    private boolean exitFlag = false;
    private Thread networkThread;
    private Object mqttAction;
    private LinkedBlockingDeque<Object> actionQueue = new LinkedBlockingDeque<>();

    public MQTTService() throws MqttException {
        String clientId = CLIENT_ID_PREFIX + GlobalInfo.user.getPhoneNumber();
        mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
        mqttClient.setCallback(this);
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        connOpts.setKeepAliveInterval(30);
        connOpts.setAutomaticReconnect(false);
        connOpts.setUserName(USERNAME_PREFIX + GlobalInfo.user.getPhoneNumber());
        connOpts.setPassword(GlobalInfo.token.toCharArray());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!exitFlag){
                    try {
                        mqttClient.connect(connOpts);
                        Log.i(GlobalInfo.LOG_TAG, "Connected to MQTT broker.");
                        break;
                    } catch (MqttException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                while(!exitFlag){
                    try{
                        mqttAction = actionQueue.pollFirst(2, TimeUnit.SECONDS);
                        if(mqttAction == null)
                            continue;
                        final Object mqttActionClone = mqttAction;
                        if(mqttActionClone instanceof MQTTSubscriptionAction){
                            String topic = getFullTopic(((MQTTSubscriptionAction) mqttActionClone).topic,
                                    ((MQTTSubscriptionAction) mqttActionClone).topicUserId, ((MQTTSubscriptionAction) mqttActionClone).receiverId);
                            mqttClient.subscribe(topic, ((MQTTSubscriptionAction) mqttActionClone).qos, new IMqttMessageListener() {
                                @Override
                                public void messageArrived(String topic, MqttMessage message) throws Exception {
                                    Intent intent = new Intent(((MQTTSubscriptionAction) mqttActionClone).action);
                                    intent.addCategory(getPackageName());
                                    parseTopic(topic, intent);
                                    intent.putExtra(Application.BUNDLE_KEY_MESSAGE, new String(message.getPayload()));
                                    sendBroadcast(intent);
                                }
                            });
                            Log.i(GlobalInfo.LOG_TAG, "Subscribe on topic <" + topic + "> successfully.");
                        }else if(mqttActionClone instanceof MQTTPublishAction){
                            String topic = getFullTopic(((MQTTPublishAction) mqttActionClone).topic, ((MQTTPublishAction) mqttActionClone).topicUserId,
                                    ((MQTTPublishAction) mqttActionClone).receiverId);
                            mqttClient.publish(topic, ((MQTTPublishAction) mqttActionClone).message.getBytes(), ((MQTTPublishAction) mqttActionClone).qos,
                                    ((MQTTPublishAction) mqttActionClone).retain);
                            Log.i(GlobalInfo.LOG_TAG, "Publish on topic <" + topic + "> successfully.");
                            if(((MQTTPublishAction) mqttActionClone).action != null) {
                                Intent intent = new Intent(((MQTTPublishAction) mqttActionClone).action);
                                intent.addCategory(getPackageName());
                                parseTopic(topic, intent);
                                if (((MQTTPublishAction) mqttActionClone).messageId != null)
                                    intent.putExtra(Application.BUNDLE_KEY_MESSAGE_ID, ((MQTTPublishAction) mqttActionClone).messageId);
                                sendBroadcast(intent);
                            }
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                        try {
                            actionQueue.putFirst(mqttAction);
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {}
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                networkThread = null;
            }
        });
        networkThread.start();
    }

    @Override
    public void onDestroy() {
        try {
            exitFlag = true;
            if(networkThread != null){
                networkThread.join(2000);
            }
            mqttClient.disconnect(1500);
            Log.i(GlobalInfo.LOG_TAG, "Disconnected from MQTT server.");
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MQTTServiceBinder();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(GlobalInfo.LOG_TAG, "Lost connection from MQTT broker.");
        cause.printStackTrace();
        while (!exitFlag) {
            try {
                Log.i(GlobalInfo.LOG_TAG, "Trying to reconnect to MQTT broker...");
                mqttClient.connect(connOpts);
                Log.i(GlobalInfo.LOG_TAG, "Reconnected to MQTT broker.");
                break;
            }catch (MqttException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());
        Log.i(GlobalInfo.LOG_TAG, "Offline Message Arrived: " + msg);
        Intent intent = new Intent();
        parseTopic(topic, intent);
        intent.putExtra(Application.BUNDLE_KEY_MESSAGE, msg);
        intent.addCategory(getPackageName());
        if(intent.getStringExtra(Application.BUNDLE_KEY_TOPIC).equals(GlobalInfo.TOPIC_SENSOR_DATA_REPORT)){
            intent.setAction(Application.ACTION_SENSOR_DATA_REPORT);
        }else if(intent.getStringExtra(Application.BUNDLE_KEY_TOPIC).equals(GlobalInfo.TOPIC_SENSOR_WARNING)){
            intent.setAction(Application.ACTION_SENSOR_WARNING);
        }else if(intent.getStringExtra(Application.BUNDLE_KEY_TOPIC).equals(GlobalInfo.TOPIC_CHATTING)){
            intent.setAction(Application.ACTION_CHAT_MESSAGE_RECEIVED);
        }
        sendBroadcast(intent);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    private String getFullTopic(String topic, String topicUserId, String receiverId){
        String result = topic;
        if(receiverId != null && !receiverId.isEmpty())
            result += '/' + receiverId;
        if(topicUserId != null && !topicUserId.isEmpty())
            result += '/' + topicUserId;
        return result;
    }

    private void parseTopic(String topicWithUser, Intent intent){
        if(topicWithUser == null || topicWithUser.isEmpty())
            return;
        String topic = topicWithUser;
        String[] topicSplit = topicWithUser.split("/");
        if(topicSplit.length > 1){
            int end = topicSplit.length - 1;
            String topicUser = topicSplit[topicSplit.length - 1];
            intent.putExtra(Application.BUNDLE_KEY_USER_ID, topicUser);
            if(topicSplit.length > 2){
                String receiver = topicSplit[topicSplit.length - 2];
                intent.putExtra(Application.BUNDLE_KEY_RECEIVER_ID, receiver);
                end = topicSplit.length - 2;
            }
            topic = "";
            for(int i = 0; i < end; i++){
                topic += topicSplit[i];
                if(i < end - 1)
                    topic += '/';
            }
        }
        intent.putExtra(Application.BUNDLE_KEY_TOPIC, topic);
    }

    public void addMQTTAction(Object action){
        try {
            actionQueue.putLast(action);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class MQTTServiceBinder extends Binder{
        public MQTTService getService(){
            return MQTTService.this;
        }
    }

    public static class MQTTSubscriptionAction implements Serializable{
        private String topic;
        private String topicUserId;
        private String receiverId;
        private int qos;
        private String action;

        public MQTTSubscriptionAction(String topic, String topicUserId, int qos, String broadcastAction) {
            this.topic = topic;
            this.topicUserId = topicUserId;
            this.qos = qos;
            this.action = broadcastAction;
        }

        public MQTTSubscriptionAction(String topic, String topicUserId, String receiverId, int qos, String action) {
            this(topic, topicUserId, qos, action);
            this.receiverId = receiverId;
        }
    }

    public static class MQTTPublishAction implements Serializable{
        private String topic;
        private String topicUserId;
        private String receiverId;
        private int qos;
        private boolean retain = false;
        private String message;
        private String messageId;
        private String action;

        public MQTTPublishAction(String topic, String topicUserId, String receiverId, int qos, String message, @Nullable String messageId, @Nullable String action) {
            this(topic, topicUserId, qos, message, messageId, action);
            this.receiverId = receiverId;
        }

        public MQTTPublishAction(String topic, String topicUserId, int qos, String message, @Nullable String messageId, @Nullable String action) {
            this.topic = topic;
            this.topicUserId = topicUserId;
            this.qos = qos;
            this.message = message;
            this.messageId = messageId;
            this.action = action;
        }
    }

}
