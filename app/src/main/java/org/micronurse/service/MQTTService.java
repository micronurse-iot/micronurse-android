package org.micronurse.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
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
    private MQTTReceiver receiver;

    public MQTTService() throws MqttException {
        String clientId = CLIENT_ID_PREFIX + GlobalInfo.user.getPhoneNumber();
        mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
        mqttClient.setCallback(this);
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        connOpts.setKeepAliveInterval(30);
        connOpts.setUserName(USERNAME_PREFIX + GlobalInfo.user.getPhoneNumber());
        connOpts.setPassword(GlobalInfo.token.toCharArray());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new MQTTReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_MQTT_ACTION);
        filter.addCategory(getPackageName());
        registerReceiver(receiver, filter);
        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!exitFlag){
                    try {
                        mqttClient.connect(connOpts);
                        Intent i = new Intent(Application.ACTION_MQTT_BROKER_CONNECTED);
                        i.addCategory(getPackageName());
                        sendBroadcast(i);
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
                            String topic = ((MQTTSubscriptionAction) mqttAction).topic;
                            if(((MQTTSubscriptionAction) mqttAction).topicUserId != null && !((MQTTSubscriptionAction) mqttAction).topicUserId.isEmpty())
                                topic += '/' + ((MQTTSubscriptionAction) mqttAction).topicUserId;
                            mqttClient.subscribe(topic, ((MQTTSubscriptionAction) mqttAction).qos, new IMqttMessageListener() {
                                @Override
                                public void messageArrived(String topic, MqttMessage message) throws Exception {
                                    Intent intent = new Intent(((MQTTSubscriptionAction) mqttAction).action);
                                    intent.addCategory(getPackageName());
                                    String topicUserId = parseTopicUser(topic);
                                    if(topicUserId != null && !topicUserId.isEmpty())
                                        intent.putExtra(Application.BUNDLE_KEY_USER_ID, topicUserId);
                                    intent.putExtra(Application.BUNDLE_KEY_MESSAGE, new String(message.getPayload()));
                                    sendBroadcast(intent);
                                }
                            });
                            Log.i(GlobalInfo.LOG_TAG, "Subscribe on topic <" + topic + "> successfully.");
                        }
                        actionQueue.poll();
                    } catch (InterruptedException | MqttException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {}
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
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
    public void messageArrived(String topic, MqttMessage message) throws Exception {}

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    private String parseTopicUser(String topic){
        if(topic == null || topic.isEmpty())
            return null;
        int i;
        for(i = topic.length() - 1; i >= 0; i--){
            if(topic.charAt(i) == '/')
                break;
        }
        if(i < 0 || i == topic.length() - 1)
            return null;
        return topic.substring(i + 1);
    }

    public static class MQTTSubscriptionAction implements Serializable{
        private String topic;
        private String topicUserId;
        private int qos;
        private String action;

        public MQTTSubscriptionAction(String topic, String topicUserId, int qos, String broadcastAction) {
            this.topic = topic;
            this.topicUserId = topicUserId;
            this.qos = qos;
            this.action = broadcastAction;
        }
    }

    private class MQTTReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Serializable o = intent.getSerializableExtra(Application.BUNDLE_KEY_MQTT_ACTION);
            if(o == null)
                return;
            actionQueue.add(o);
        }
    }
}
