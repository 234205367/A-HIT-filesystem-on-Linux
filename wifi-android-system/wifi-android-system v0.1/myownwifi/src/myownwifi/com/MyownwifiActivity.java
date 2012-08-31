//wifi-android-system v0.1

// "THE BEER-WARE LICENSE" (Revision 43):
// Steve Guo wrote this file. As long as you retain this notice you can do whatever you want with this stuff. 
// If we meet some day, and you think this stuff is worth it, you can buy me a beer in return.


package myownwifi.com;

import myownwifi.com.R;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import myownwifi.com.*;
import android.R.string;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiInfo;     
import android.net.wifi.WifiManager.WifiLock; 


public class MyownwifiActivity extends Activity implements SensorEventListener{
	
	SensorManager mSensorManager; //������
	
    private final String TAG = "WifiSoftAP";
    public static final String WIFI_AP_STATE_CHANGED_ACTION =
        "android.net.wifi.WIFI_AP_STATE_CHANGED";

    public static final int WIFI_AP_STATE_DISABLING = 0;
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLING = 2;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    public static final int WIFI_AP_STATE_FAILED = 4;  
    //TextView result;
    public static WifiManager wifiManager;
    public static WifiReceiver receiverWifi;
    public static List<ScanResult> wifiList;
	//���������б�  
    public static List<WifiConfiguration> wifiConfiguration;
	StringBuilder resultList = new StringBuilder();
	
    public float degree;//��ǰ���򴫸����ĽǶ�ֵ��0-360����ˢ���ٶȷǳ���
    
    public float passoutdegree = -1;//���ÿ����һ�Σ���degree��ֵ����passoutdegree
    								//�����жϵ�ǰ�ֻ�����0-180��180-360�����Ƿ����ı�
    public int index = 1;//����wifireceiverһֱ���յ�wifi�Ĺ㲥������һֱ���ظ�������ͬwifi�ȵ�Ķ���
    					 //index��������wifireceiverʹ��ֻ�ڵ�һ���յ��㲥ʱ�����ض�wifi�ȵ�
    public int diyici = 1;//ͬindex������ͬ
    public int timecounter1 = 1;//���ڶ�android��̲�����Ϥ��android�Ķ�ʱ�Լ���ʱ����û������
    public int timecounter2 = 0;//������timecounter1��timecounter2�����涨ʱ���ܣ�
    							//ÿ��ѭ����һ�����Ｘ��ʱ������ز�������timecounter���㣬���100��ѭ���ܹ���ʱ2������
    public int APorconnect = 0;//��¼��ǰ�õ��ĽǶȶ�Ӧ���ֻ�Ӧ�е�״̬��180-360������wifi�ȵ㣻0-180������wifi�ȵ㣩
    public int formerAPorconnect = 0;//��¼��һ�Σ����6��ǰ���ֻ�Ӧ�е�״̬
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //��ȡ�������
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        formerAPorconnect = APorconnect;//����ǰ״̬��ֵ����һ��״̬
 
        //������һ��ʼ���Ƚ���һ�Σ��жϵ�ǰ�Ƕȣ���������wifi�ȵ��������wifi
        if(passoutdegree >= 180){
    		APorconnect = 1;
    	}
    	else{
       		APorconnect = 0;
    	}
		if(APorconnect == 1){
			setWifiApEnabled(true);
		}
		else{			
			connectAP();
		}
    }
    @Override 
    protected void onResume(){
    	super.onResume();
    	//ע�������
    	if (receiverWifi != null)
			registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }  
    //ȡ��ע��
    @Override
    protected void onPause(){
    	mSensorManager.unregisterListener(this);
    	if (receiverWifi != null)
			unregisterReceiver(receiverWifi);
    	super.onPause();
    	
    }
    //ע������
    @Override
    protected void onStop(){
    	mSensorManager.unregisterListener(this);
    	super.onStop();
    	
    }
    //���ȸı�
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		//��ȡ����event�Ĵ���������		
		int sensorType = event.sensor.getType();
		//��ⷽ�򴫸������õ�����Ƕȣ�0-360��0��90��180��360�ֱ��Ӧ�ڶ���������
		//��������ֻ���ԣ���Ȼһ���ڣ�0��180����Χ����һ���ڣ�180,360����Χ
		//��ôֻҪ�180,360������wifi�ȵ㣬��һ����0,180�����������ض�wifi�ȵ㼴��ʵ��ͨѶ
		switch(sensorType){
			case Sensor.TYPE_ORIENTATION:
				degree = event.values[0]; //��ȡzת���ĽǶ�
				TextView ddegree = (TextView) findViewById(R.id.Direction);
				ddegree.setText("Direction:" + String.valueOf(degree));//���ֵ�᲻ͣ�ı仯��ֻҪ�Ƕȷ�����΢�ı䣬��������ͻᱻ����
				//��һ�εõ�����Ƕȵ�ʱ��
				if(diyici == 1){
					passoutdegree = degree;
					TextView passdegree = (TextView) findViewById(R.id.wifistate);
					passdegree.setText("Passoutdegree:" + String.valueOf(passoutdegree));
			        if(passoutdegree >= 180){
			        	setWifiApEnabled(true);
			    	}
			    	else{
			    		connectAP();
			    	}
			        diyici ++;
				}
				//֮��õ�����Ƕȵ�ʱ��
				else{
					//һЩ�Ѳ���
					if(diyici % 2 == 0)
						diyici ++;
					else
						diyici --;
					//�������timecounter2�ӵ���300�������2*3 = 6�룩
					if(timecounter2 % 300 == 0)
					{
						timecounter2 = 1;//timecounter���¿�ʼ����
						passoutdegree = degree;//�õ���ǰ�ķ��򴫸����Ƕ�ֵ
						TextView passdegree = (TextView) findViewById(R.id.wifistate);
						passdegree.setText("Passoutdegree:" + String.valueOf(passoutdegree));
						//���ݽǶ�ֵ�ж��ֻ�Ӧ�ý���wifi�ȵ㻹������wifi�ȵ�
				        if(passoutdegree >= 180){
				    		APorconnect = 1;
				    	}
				    	else{
				       		APorconnect = 0;
				    	}
				        //�ж������ֻ�Ӧ�ô��ڵ�״̬�Ƿ��ǰһ���ж���ͬ������ͬ��������Ӧ�ı䣬��ͬ��û�ж���
				        if(formerAPorconnect != APorconnect){
				        	if(APorconnect == 1){
								setWifiApEnabled(true);
							}
							else{								
								connectAP();
							}
				        	formerAPorconnect = APorconnect;//����ǰ״̬��ֵ����һ��״̬
				        }				
					}
					
					timecounter2 ++;					
				}				
		}		
	}
    //������ֵ�ı�
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
		
	}
	//��ʵû���õ�startscan
	public void StartScan() {
		//��wifi
		
		wifiManager.setWifiEnabled(true);

		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		//result.setText("\nScaning...\n");
		TextView scannet = (TextView) findViewById(R.id.State);
		scannet.setText("State: Scaning...");
	}	
	//����wifi�ȵ�ĺ�������Ϊ����������ͱ���Э���wifi�ȵ�Ĵ�������д���ã�����ѡ����û������Ĺ���wifi�ȵ�
	public boolean setWifiApEnabled(boolean enabled) {
		if (enabled) { // disable WiFi in any case
			wifiManager.setWifiEnabled(false);
		}
    	
		try {
			WifiConfiguration apConfig = new WifiConfiguration();
			apConfig.SSID = "GossipDog";
			apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			
			Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			Log.e(TAG, "Cannot set WiFi AP state", e);
			return false;
		}
	}	
	//�����ʵҲû���õ�
	public int getWifiApState() {
		try {
			Method method = wifiManager.getClass().getMethod("getWifiApState");
			return (Integer) method.invoke(wifiManager);
		} catch (Exception e) {
			Log.e(TAG, "Cannot get WiFi AP state", e);
			return WIFI_AP_STATE_FAILED;
		}
	}
	//ͬ��
	public boolean isApEnabled() {
        int state = getWifiApState();
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
	}
    //����GossipDog�ȵ�  
    public void connectAP() {
    	int wifistate = getWifiApState();
    	if(wifistate != 1){
        	setWifiApEnabled(false);
    	}  	
    	if(wifiManager.getWifiState() == 1)
    		wifiManager.setWifiEnabled(true);
    	receiverWifi = new WifiReceiver();//��Ҫ�����������½����receiverwifi��Ķ���ͼ���receiverwifiʱ���е�   	
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		return ;
    	
    }   
    //û���õ�
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
        	BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return connectedIP;
    }
	
    //wifireceiver�ඨ��
	class WifiReceiver extends BroadcastReceiver {

		public void onReceive(Context c, Intent intent) {
			//ͬ���洫������ֵ���ǲ�ͣ�ı䵼�������onchang�������ǲ�ͣ����һ��������ֻ�����һ�������ȵ�Ķ���������ɹ��Ļ���
			if(index == 1){
				//����ط���wifilist��ʵ����Ϊ�˴�ӡ���õ�������wifi�ȵ�SSID����Ϣ��������
				resultList = new StringBuilder();
				wifiList = wifiManager.getScanResults();
				for (int i = 0; i < wifiList.size(); i++) {
					resultList.append(new Integer(i + 1).toString() + ".");
					resultList.append((wifiList.get(i)).toString());
					resultList.append("\n\n");
				}
				//��������wifi�ȵ�Ĳ���
			    WifiConfiguration wcg = new WifiConfiguration();
			    wcg.SSID = "\"GossipDog\"";//����ط�����д����gossipdog����˫�����쳣��Ҫ�������˺þ�
			    wcg.wepKeys[0] = ""; 
	            wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
	            wcg.wepTxKeyIndex = 0;
	            wcg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);   
	        	wcg.networkId = wifiManager.addNetwork(wcg);        	
	        	boolean ifconnected = wifiManager.enableNetwork(wcg.networkId, true);
	        	//���wifi���ӳɹ���index��Ϊ2�����ٽ������Ӳ���
	        	if(ifconnected == true){
	        		Toast.makeText(MyownwifiActivity.this, "Connected to wifiAP!", 1).show();
	        		index ++;
	        	}
	        	else
	        		Toast.makeText(MyownwifiActivity.this, "Searching...", 1).show();
			}
			//�Ѳ���
			else{
				if(index % 2 == 0){
					index ++;
				}
				else
					index --;
			}
		}
	}

}
	
	 
	
