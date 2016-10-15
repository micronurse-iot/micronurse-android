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
import org.micronurse.util.GsonUtil;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MQTTService extends Service implements MqttCallback {
    public static final String BROKER_URL = "tcp://101.200.144.204:13883";
    public static final String CLIENT_ID_PREFIX = "micronurse_mobile_user:";
    public static final String USERNAME_PREFIX = "micronurse_mobile_user:";

    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;
    private boolean exitFlag = false;
    private Thread networkThread;
    private ConcurrentLinkedQueue<Object> actionQueue = new ConcurrentLinkedQueue<>();

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
                        if(actionQueue.isEmpty() || !mqttClient.isConnected()){
                            Thread.sleep(2000);
                            continue;
                        }
                        final Object mqttAction = actionQueue.peek();
                        if(mqttAction instanceof MQTTSubscriptionAction){
                            String topic = getFullTopic(((MQTTSubscriptionAction) mqttAction).topic,
                                    ((MQTTSubscriptionAction) mqttAction).topicUserId, ((MQTTSubscriptionAction) mqttAction).receiverId);
                            mqttClient.subscribe(topic, ((MQTTSubscriptionAction) mqttAction).qos, new IMqttMessageListener() {
                                @Override
                                public void messageArrived(String topic, MqttMessage message) throws Exception {
                                    Intent intent = new Intent(((MQTTSubscriptionAction) mqttAction).action);
                                    intent.addCategory(getPackageName());
                                    parseTopic(topic, intent);
                                    intent.putExtra(Application.BUNDLE_KEY_MESSAGE, new String(message.getPayload()));
                                    sendBroadcast(intent);
                                }
                            });
                            Log.i(GlobalInfo.LOG_TAG, "Subscribe on topic <" + topic + "> successfully.");
                        }else if(mqttAction instanceof MQTTPublishAction){
                            String topic = getFullTopic(((MQTTPublishAction) mqttAction).topic, ((MQTTPublishAction) mqttAction).topicUserId,
                                    ((MQTTPublishAction) mqttAction).receiverId);
                            mqttClient.publish(topic, ((MQTTPublishAction) mqttAction).message.getBytes(), ((MQTTPublishAction) mqttAction).qos,
                                    ((MQTTPublishAction) mqttAction).retain);
                            Log.i(GlobalInfo.LOG_TAG, "Publish on topic <" + topic + "> successfully.");
                            Intent intent = new Intent(((MQTTPublishAction) mqttAction).action);
                            intent.addCategory(getPackageName());
                            parseTopic(topic, intent);
                            if(((MQTTPublishAction) mqttAction).messageId != null)
                                intent.putExtra(Application.BUNDLE_KEY_MESSAGE_ID, ((MQTTPublishAction) mqttAction).messageId);
                            sendBroadcast(intent);
                        }
                        actionQueue.poll();
                    } catch (MqttException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {}
                    } catch (InterruptedException e) {}
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
        actionQueue.add(action);
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
            this.action = action;
        }
    }

    public static class MQTTPublishAction implements Serializable{
        private String topic;
        private String topicUserId;
        private String receiverId;
        private int qos;
        private boolean retain = false;
        private String message;
        private Serializable messageId;
        private String action;

        public MQTTPublishAction(String topic, String topicUserId, String receiverId, int qos, String message, @Nullable Serializable messageId, String action) {
            this(topic, topicUserId, qos, message, messageId, action);
            this.receiverId = receiverId;
        }

        public MQTTPublishAction(String topic, String topicUserId, int qos, String message, @Nullable Serializable messageId, String action) {
            this.topic = topic;
            this.topicUserId = topicUserId;
            this.qos = qos;
            this.message = message;
            this.messageId = messageId;
            this.action = action;
        }
    }

}
