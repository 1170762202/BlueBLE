package com.zlx.myapplication.blue;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zlx.myapplication.R;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();// 创建一个handler对象

    Thread autoSendThread = null;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private int mState = UART_PROFILE_DISCONNECTED;

    private ArrayAdapter<String> listAdapter;
    private ListView messageListView;
    private BluetoothDevice mDevice = null;
    // private BluetoothDevice mDevice = null;
    private UartService mService = null;
    private Button btnHome, btnScan, btnSend, btnReset, btnClear;
    private BluetoothAdapter mBtAdapter = null;
    private EditText editText_sendMessage;
    private TextView textview_iscConnected;
    private TextView sendValueLength;
    private long sendValueNum = 0;
    private long recValueNum = 0;
    private TextView sendTimes;
    private CheckBox checkBox_dataRec;
    private CheckBox checkBox_autoSend;
    private EditText editText_sendIntervalVal;
    private RadioButton radioSendASCII, radioSendHEX, radioReASCII, radioReHEX;
    private TextView textViewRecLength;
    // private ImageButton imagebuttonHome;
    private ImageButton imagebuttonScan;
    private Spinner spinnerInterval;
    private TextView textViewRecNumVal;

    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView tv5;
    private TextView tv6;
    private TextView tv7;
    private TextView tv8;
    private TextView tv9;
    private TextView tv10;
    private TextView tv11;
    private TextView tv12;
    private TextView tv13;
    private TextView tv14;
    private TextView tv15;
    private TextView tv16;
    private TextView tv17;
    private TextView tv18;
    private TextView tv19;
    private TextView tv20;
    private TextView tv21;
    private TextView tv22;
    private TextView tv23;
    private TextView tv24;
    private TextView tv25;
    private TextView tv26;
    private TextView tv27;
    private TextView tv28;
    private TextView tv29;
    private TextView tv30;
    private TextView tv31;
    private TextView tv32;
    private boolean flag;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.second_layout);

        // imagebuttonScan=(ImageButton) findViewById(R.id.imageButton_scan);
        btnScan = (Button) findViewById(R.id.button_scan);
        btnSend = (Button) findViewById(R.id.button_send);
        btnReset = (Button) findViewById(R.id.button_reset);
        btnClear = (Button) findViewById(R.id.button_clear);
        editText_sendMessage = (EditText) findViewById(R.id.edittext_sendText);
        textview_iscConnected = (TextView) findViewById(R.id.textView_isconnected_info);
        sendValueLength = (TextView) findViewById(R.id.textView_send_length_val);
        sendTimes = (TextView) findViewById(R.id.textView_send_val);
        checkBox_dataRec = (CheckBox) findViewById(R.id.checkBox_data_rec);
        checkBox_autoSend = (CheckBox) findViewById(R.id.checkBox_auto_send);
        editText_sendIntervalVal = (EditText) findViewById(R.id.edittext_send_interval_val);
        radioSendASCII = (RadioButton) findViewById(R.id.radio_send_ASCII);
        radioSendHEX = (RadioButton) findViewById(R.id.radio_send_HEX);
        radioReASCII = (RadioButton) findViewById(R.id.radio_receive_ASCII);
        radioReHEX = (RadioButton) findViewById(R.id.radio_receive_HEX);
        textViewRecLength = (TextView) findViewById(R.id.textView_rec_length_val);
        btnHome = (Button) findViewById(R.id.button_home);
        textViewRecNumVal = (TextView) findViewById(R.id.textView_Rec_Num_Val);
        // imagebuttonHome = (ImageButton) findViewById(R.id.imageButton_home);
        // 发送时间间隔配置
        // spinnerInterval = (Spinner) findViewById(R.id.spinner_interval);
        // final Integer arrInt[] = new Integer[] { 10, 20, 30, 40, 50, 60, 70
        // };
        // ArrayAdapter<Integer> arrayAdapterInt = new
        // ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item,
        // arrInt);
        // spinnerInterval.setAdapter(arrayAdapterInt);

        request();
        // 接收框配置
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        // 在连接成功之前保证其他发送接收的控件不可用
        editText_sendMessage.setEnabled(false);
        checkBox_autoSend.setEnabled(false);
        checkBox_autoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(MainActivity.this, "自动发送已" + (b ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();

                if (b) {
                    if (countDownTimer == null) {
                        countDownTimer = new CountDownTimer(24 * 60 * 60 * 1000, Integer.parseInt(editText_sendIntervalVal.getText().toString())) {
                            @Override
                            public void onTick(long l) {
                                try {
                                    String message = editText_sendMessage.getText().toString();
                                    final byte[] Tx_value = message.getBytes("UTF-8");
                                    mService.writeRXCharacteristic(Tx_value);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendValueLength.setText(Tx_value.length + "");
                                            sendTimes.setText((++sendValueNum) + "");
                                        }
                                    });
                                } catch (UnsupportedEncodingException e) {
                                    System.out.println(e.toString());
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFinish() {

                            }
                        };
                        countDownTimer.start();
                    }
                } else {

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                }
            }
        });
        editText_sendIntervalVal.setEnabled(false);
        btnSend.setEnabled(false);
        Init_service();// 初始化后台服务


        // "scan/stop"按钮对应的监听器
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个蓝牙适配器对象
                mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                // 如果未打开蓝牙就弹出提示对话框提示用户打开蓝牙
                if (!mBtAdapter.isEnabled()) {
                    toastMessage("对不起，蓝牙还没有打开");
                    System.out.println("蓝牙还没有打开");
                    // 弹出请求打开蓝牙对话框
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    // 如果已经打开蓝牙则与远程蓝牙设备进行连接
                    if (btnScan.getText().equals("搜索")) {
                        /**
                         * 当"scan"按钮点击后，进入DeviceListActivity.class类，弹出该类对应的窗口
                         * ，并自动在窗口内搜索周围的蓝牙设备
                         */
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        /**
                         * 当scan按钮点击之后，该按钮就会变成stop按钮， 如果此时点击了stop按钮，那么就会执行下面的内容
                         */
                        if (mDevice != null) {
                            // 断开连接
                            mService.disconnect();
                        }
                    }
                }
            }
        });
        // "Send"按钮对应的监听器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioSendASCII.isChecked()) {
                    try {
                        String message = editText_sendMessage.getText().toString();
                        byte[] Tx_value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(Tx_value);
                        sendValueLength.setText(Tx_value.length + "");
                        sendTimes.setText((++sendValueNum) + "");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (radioSendHEX.isChecked()) {
                    boolean hex_flag = true;
                    String s1 = editText_sendMessage.getText().toString();
                    for (int i = 0; i < s1.length(); i++) {
                        char charV = s1.charAt(i);
                        if ((charV >= '0' && charV <= '9') || (charV >= 'a' && charV <= 'f')
                                || (charV >= 'A' && charV <= 'F')) {
                        } else {
                            hex_flag = false;
                            break;
                        }
                    }
                    if (hex_flag) {
                        byte[] bytes;
                        if (0 == s1.length() % 2) {
                            bytes = Utils.hexStringToBytes(s1);
                            mService.writeRXCharacteristic(bytes);
                            sendValueLength.setText(s1.length() + "");
                            sendTimes.setText((++sendValueNum) + "");
                        } else {
                            String s2 = s1.substring(0, (s1.length() - 1));
                            bytes = Utils.hexStringToBytes(s2);
                            mService.writeRXCharacteristic(bytes);
                            sendValueLength.setText((s1.length() - 1) + "");
                            sendTimes.setText((++sendValueNum) + "");
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "【错误】: 输入的字符不是 16进制", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
            }
        });
        btnReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText_sendMessage.setText("");
                sendValueLength.setText("");
                sendTimes.setText("");
                sendValueNum = 0;
            }
        });
        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listAdapter.clear();
                textViewRecLength.setText("");
                textViewRecNumVal.setText("");
                recValueNum = 0;
            }
        });
        btnHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        init();

    }

    private void init() {
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);
        tv4 = findViewById(R.id.tv4);
        tv5 = findViewById(R.id.tv5);
        tv6 = findViewById(R.id.tv6);
        tv7 = findViewById(R.id.tv7);
        tv8 = findViewById(R.id.tv8);
        tv9 = findViewById(R.id.tv9);
        tv10 = findViewById(R.id.tv10);
        tv11 = findViewById(R.id.tv11);
        tv12 = findViewById(R.id.tv12);
        tv13 = findViewById(R.id.tv13);
        tv14 = findViewById(R.id.tv14);
        tv15 = findViewById(R.id.tv15);
        tv16 = findViewById(R.id.tv16);
        tv17 = findViewById(R.id.tv17);
        tv18 = findViewById(R.id.tv18);
        tv19 = findViewById(R.id.tv19);
        tv20 = findViewById(R.id.tv20);
        tv21 = findViewById(R.id.tv21);
        tv22 = findViewById(R.id.tv22);
        tv23 = findViewById(R.id.tv23);
        tv24 = findViewById(R.id.tv24);
        tv25 = findViewById(R.id.tv25);
        tv26 = findViewById(R.id.tv26);
        tv27 = findViewById(R.id.tv27);
        tv28 = findViewById(R.id.tv28);
        tv29 = findViewById(R.id.tv29);
        tv30 = findViewById(R.id.tv30);
        tv31 = findViewById(R.id.tv31);
        tv32 = findViewById(R.id.tv32);
    }

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private void request() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                // 如果选择搜索到的蓝牙设备页面操作成功（即选择远程设备成功，并返回所选择的远程设备地址信息）
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    System.out.println("远程蓝牙Address：" + mDevice);
                    System.out.println("mserviceValue:" + mService);
                    deviceName = mDevice.getName();
                    boolean isconnected = mService.connect(deviceAddress);
                    System.out.println("已连接吗？" + isconnected);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 如果请求打开蓝牙页面操作成功（蓝牙成功打开）
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙已经成功打开", Toast.LENGTH_SHORT).show();
                } else {
                    // 请求打开蓝牙页面操作不成功（蓝牙为打开或者打开错误）
                    // Log.d(TAG, "蓝牙未打开");
                    System.out.println("蓝牙未打开");
                    Toast.makeText(this, "打开蓝牙时发生错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                System.out.println("wrong request code");
                break;
        }
    }

    private void Init_service() {
        System.out.println("Init_service");
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver,
                makeGattUpdateIntentFilter());
    }

    // UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        // 与UART服务的连接建立
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            System.out.println("uart服务对象：" + mService);
            if (!mService.initialize()) {
                System.out.println("创建蓝牙适配器失败");
                // 因为创建蓝牙适配器失败，导致下面的工作无法进展，所以需要关闭当前uart服务
                finish();
            }
        }

        // 与UART服务的连接失去
        public void onServiceDisconnected(ComponentName classname) {
            // mService.disconnect(mDevice);
            mService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private String deviceName;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            // 建立连接
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                System.out.println("BroadcastReceiver:ACTION_GATT_CONNECTED");
                textview_iscConnected.setText(deviceName + ":已建立连接");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

                btnScan.setText("断开");
                editText_sendMessage.setEnabled(true);
                checkBox_autoSend.setEnabled(true);
                editText_sendIntervalVal.setEnabled(true);
                btnSend.setEnabled(true);
                listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                mState = UART_PROFILE_CONNECTED;
            }
            // 断开连接
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                System.out.println("BroadcastReceiver:ACTION_GATT_DISCONNECTED");
                textview_iscConnected.setText("已断开连接");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                btnScan.setText("搜索");
                editText_sendMessage.setEnabled(false);
                checkBox_autoSend.setEnabled(false);
                editText_sendIntervalVal.setEnabled(false);
                btnSend.setEnabled(false);
                listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                mState = UART_PROFILE_DISCONNECTED;
                mService.close();
            }
            // 有数据可以接收
            if ((action.equals(UartService.ACTION_DATA_AVAILABLE)) && (checkBox_dataRec.isChecked())) {
                byte[] rxValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                if (radioReASCII.isChecked()) {
                    try {
                        String Rx_str = new String(rxValue, "UTF-8");
//                        doReceiptMsg(Rx_str);
                        listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] RX: " + Rx_str);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                } else {
                    String Rx_str = "";
                    for (int i = 0; i < rxValue.length; i++) {
                        if (rxValue[i] >= 0)
                            Rx_str = Rx_str + Integer.toHexString(rxValue[i]) + " ";
                        else
                            Rx_str = Rx_str + Integer.toHexString(rxValue[i] & 0x0ff) + " ";
                    }
//                    listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] RX: " + Rx_str);
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    try {
                        doReceiptMsg(Rx_str);

                    } catch (Exception e) {
                        System.out.println(e.toString());
//                        toastMessage(e.toString());
                    }
                }
                textViewRecLength.setText(Integer.toString(rxValue.length));
                textViewRecNumVal.setText((++recValueNum) + "");
            }
            // 未知功能1
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            // 未知功能2
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                toastMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private List<String> list = new ArrayList<>();

    private boolean flag2 = false;

    private void doReceiptMessage(String value) throws Exception {
        String[] split = value.split(" ");//每个字节的数组
        for (String s : split) {
            list.add(s);
        }

        StringBuilder sb = new StringBuilder();

        for (String s : list) {
            sb.append(s);
        }

        byte[] data = BytesHexStrTranslate.toBytes(sb.toString());
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 85 && data[i + 1] == -86) {
                if (list.size() - i >= 59) {
                    int crc = crc(data, i, 57);
                    if (data[i + 57] == (byte) crc && (data[i + 58] == (byte) (crc >> 8))) {

                        StringBuilder sb1 = new StringBuilder();
                        for (String s : list) {
                            sb1.append(s + " ");
                        }
                        set(sb1.toString().split(" "));
                    } else {
                        list.clear();
                    }
                }
            }
        }

    }

    private void set(String[] split) {
        double volt = calcTwoByte(split[2], split[3], 0.1, 0, 1);
        double dianliu = calcTwoByte(split[4], split[5], 0.1, -3000, 1);
        tv1.setText("总电压: " + volt + "V");
        tv2.setText("总电流: " + (dianliu > 0 ? "放电 " : "充电 ") + dianliu + "A");
        double SOC = calcTwoByte(split[6], split[7], 0.1, 0, 1);
        double 电池状态 = calcOneByte(split[8], 1, 0, 1);
        String 电池状态_status = null;
        if (电池状态 == 0) {
            电池状态_status = "静止";
        } else if (电池状态 == 1) {
            电池状态_status = "充电";
        } else {
            电池状态_status = "放电";
        }
        tv3.setText("SOC: " + SOC + "%");
        tv4.setText("电池状态:" + 电池状态_status);
        byte[] bytes3 = BytesHexStrTranslate.toBytes(split[9]);
        tv5.setVisibility(View.GONE);
//        tv5.setText("第1节:" + getDCStatus(split[0]) + "  第2节:" + getDCStatus(split[1]) + "  第3节:" +
//                getDCStatus(split[2]) + "  第4节:" + getDCStatus(split[3]) + "  第5节:" + getDCStatus(split[4]) + "  第6节:" + getDCStatus(split[5]));
//        double 均衡状态 = calcOneByte(s1, 1, 0, 1);
//        tv5.setText("均衡状态:" + (均衡状态 == 0 ? "未开启均衡" : "开启均衡"));
        replacePointZero(tv5);

        double 年 = calcOneByte(split[10], 1, 0, 0);
        double 月 = calcOneByte(split[11], 1, 0, 0);
        double 日 = calcOneByte(split[12], 1, 0, 0);
        double 时 = calcOneByte(split[13], 1, 0, 0);
        double 分 = calcOneByte(split[14], 1, 0, 0);
        double 秒 = calcOneByte(split[15], 1, 0, 0);
        tv6.setText((split[10] + "-" + split[11] + "-" + split[12] + " " + split[13] + ":" + split[14] + ":" + split[15]));
        double 电压节数 = calcOneByte(split[16], 1, 0, 1);
        double 温度个数 = calcOneByte(split[17], 1, 0, 1);
        tv7.setText("电压节数:" + 电压节数);
        tv8.setText("温度个数:" + 温度个数);
        replacePointZero(tv7);
        replacePointZero(tv8);

        double 第1节电池电压 = calcTwoByte(split[18], split[19], 1, 0, 1);
        double 第2节电池电压 = calcTwoByte(split[20], split[21], 1, 0, 1);
        tv9.setText("第1节电池电压:" + 第1节电池电压 + "mV" + "\n第1节均衡状态:" + getDCStatus(bytes3[0] & 0x01));
        tv10.setText("第2节电池电压:" + 第2节电池电压 + "mV" + "\n第2节均衡状态:" + getDCStatus(bytes3[0] >> 1 & 0x01));
        replacePointZero(tv9);
        replacePointZero(tv10);

        double 第3节电池电压 = calcTwoByte(split[22], split[23], 1, 0, 1);
        double 第4节电池电压 = calcTwoByte(split[24], split[25], 1, 0, 1);
        tv11.setText("第3节电池电压:" + 第3节电池电压 + "mV" + "\n第3节均衡状态:" + getDCStatus(bytes3[0] >> 2 & 0x01));
        tv12.setText("第4节电池电压:" + 第4节电池电压 + "mV\n第4节均衡状态:" + getDCStatus(bytes3[0] >> 3 & 0x01));
        replacePointZero(tv11);
        replacePointZero(tv12);

        double 第5节电池电压 = calcTwoByte(split[26], split[27], 1, 0, 1);
        double 第6节电池电压 = calcTwoByte(split[28], split[29], 1, 0, 1);
        tv13.setText("第5节电池电压:" + 第5节电池电压 + "mV\n第5节均衡状态:" + getDCStatus(bytes3[0] >> 4 & 0x01));
        tv14.setText("第6节电池电压:" + 第6节电池电压 + "mV\n第6节均衡状态:" + getDCStatus(bytes3[0] >> 5 & 0x01));
        replacePointZero(tv13);
        replacePointZero(tv14);

        double 电池温度 = calcOneByte(split[30], 1, -40, 1);
        double 单体电池压差 = calcTwoByte(split[31], split[32], 1, 0, 1);
        tv15.setText("电池温度:" + 电池温度 + "");
        tv16.setText("单体电池压差:" + 单体电池压差 + "mV");
        replacePointZero(tv15);
        replacePointZero(tv16);

        double 电池温差 = calcOneByte(split[33], 1, 0, 1);
        double 最高单体电压 = calcTwoByte(split[34], split[35], 1, 0, 1);
        tv17.setText("电池温差:" + 电池温差 + "℃");
        tv18.setText("最高单体电压:" + 最高单体电压 + "mV");
        replacePointZero(tv17);
        replacePointZero(tv18);

        double 最低单体电压 = calcTwoByte(split[36], split[37], 1, 0, 1);
        double 最高单体电压位置 = calcOneByte(split[38], 1, 0, 1);
        tv19.setText("最低单体电压:" + 最低单体电压 + "mV");
        tv20.setText("最高单体电压位置:" + 最高单体电压位置);
        replacePointZero(tv19);
        replacePointZero(tv20);

        double 最低单体电压位置 = calcOneByte(split[39], 1, 0, 1);
        double 最高温度 = calcOneByte(split[40], 1, -40, 1);
        tv21.setText("最低单体电压位置:" + 最低单体电压位置);
        tv22.setText("最高温度:" + 最高温度 + "℃");
        replacePointZero(tv21);
        replacePointZero(tv22);

        double 最低温度 = calcOneByte(split[41], 1, -40, 1);
        double 最高温度位置 = calcOneByte(split[42], 1, 0, 1);
        tv23.setText("最低温度:" + 最低温度 + "℃");
        tv24.setText("最高温度位置:" + 最高温度位置);
        replacePointZero(tv23);
        replacePointZero(tv24);

        double 最低温度位置 = calcOneByte(split[43], 1, 0, 1);
        double 过充次数 = calcTwoByte(split[44], split[45], 1, 0, 1);
        tv25.setText("最低温度位置:" + 最低温度位置);
        replacePointZero(tv25);
        tv26.setText("过充次数:" + 过充次数);
        replacePointZero(tv26);

        double 过放次数 = calcTwoByte(split[46], split[47], 1, 0, 1);
        double 充电过流次数 = calcTwoByte(split[48], split[49], 1, 0, 1);
        tv27.setText("过放次数:" + 过放次数);
        tv28.setText("充电过流次数:" + 充电过流次数);
        replacePointZero(tv27);
        replacePointZero(tv28);

        double 过温次数 = calcTwoByte(split[50], split[51], 1, 0, 1);
        double 循环次数 = calcTwoByte(split[52], split[53], 1, 0, 1);
        tv29.setText("过温次数:" + 过温次数);
        tv30.setText("循环次数:" + 循环次数);
        replacePointZero(tv29);
        replacePointZero(tv30);


        double 电池故障1 = calcOneByte(split[54], 1, 0, 1);
        double 电池故障2 = calcOneByte(split[55], 1, 0, 1);
        double 电池故障3 = calcOneByte(split[56], 1, 0, 1);
        byte[] bytes = BytesHexStrTranslate.toBytes(split[54]);
        tv31.setText("电池故障:");
        tv31.append("\n");
        if ((bytes[0] & 0x03) != 0) {
            tv31.append("总压过高" + (bytes[0] & 0x03) + "级\n");
        }
        if ((bytes[0] >> 2 & 0x03) != 0) {
            tv31.append("总压过低" + (bytes[0] >> 2 & 0x03) + "级\n");
        }
        if ((bytes[0] >> 4 & 0x03) != 0) {
            tv31.append("单体过高" + (bytes[0] >> 4 & 0x03) + "级\n");
        }
        if ((bytes[0] >> 6 & 0x03) != 0) {
            tv31.append("单体过低" + (bytes[0] >> 6 & 0x03) + "级\n");
        }

//        tv31.append(" 电池故障2:");
        byte[] bytes1 = BytesHexStrTranslate.toBytes(split[55]);
        if ((bytes1[0] & 0x03) != 0) {
            tv31.append("压差过大" + (bytes1[0] & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 2 & 0x03) != 0) {
            tv31.append("温差过大" + (bytes1[0] >> 2 & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 4 & 0x03) != 0) {
            tv31.append("温度过高" + (bytes1[0] >> 4 & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 6 & 0x03) != 0) {
            tv31.append("温度过低" + (bytes1[0] >> 6 & 0x03) + "级\n");
        }
//        tv31.append(" 电池故障3:");
        byte[] bytes2 = BytesHexStrTranslate.toBytes(split[56]);
        if ((bytes2[0] & 0x03) != 0) {
            tv31.append("充电过流" + (bytes2[0] & 0x03) + "级\n");
        }
        if ((bytes2[0] >> 2 & 0x03) != 0) {
            tv31.append("放电过流" + (bytes2[0] >> 2 & 0x03) + "级\n");
        }
        if ((bytes2[0] >> 4 & 0x03) != 0) {
            tv31.append("SOC过高" + (bytes2[0] >> 4 & 0x03) + "级\n");
        }
        if ((bytes2[0] >> 6 & 0x03) != 0) {
            tv31.append("SOC过低" + (bytes2[0] >> 6 & 0x03) + "级\n");
        }


//        tv31.setText("电池故障1:" + 电池故障1 + getStatus(电池故障1) + "     电池故障2:" +
//                电池故障2 + getStatus(电池故障2) + "     电池故障3:" + 电池故障3 + getStatus(电池故障3));

//        double 检验码 = 0;
//        for (String s : split) {
//            检验码 += Integer.parseInt(s, 16);
//        }
//        检验码 = 检验码 / 100;
//        tv32.setText("检验码:" + 检验码);
//        tv32.setText("检验码:" + 检验码);
    }

    private void replacePointZero(TextView textView) {
        String s = textView.getText().toString();
        textView.setText(s.replace(".0", ""));
    }

    private String getDCStatus(int value) {
        return value != 0 ? "开启均衡" : "未开启";
    }

    private void doReceiptMsg(String rxValue) throws Exception {
//        String Rx_str = new String(rxValue, "UTF-8");
//        listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] RX: " + Rx_str);

        String[] split1 = rxValue.split(" ");//每个字节的数组

        for (int i = 0; i < split1.length; i++) {
            if ((split1[i].equals("55")) && (split1[i + 1].equals("AA") || split1[i + 1].equals("aa"))) {
                flag = true;
                break;
            }
        }
        if (flag) {
            for (String s : split1) {
                list.add(s);
            }
        }

        if (list.size() > 59) {
            flag = false;
            list.clear();
            return;
        }


        if (list.size() == 59) {
            flag = false;

            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
            }


            StringBuilder builder = new StringBuilder();
            for (String s : list) {
                builder.append(s + " ");
            }
//        listAdapter.add("收到的数据：" + sb.toString());
            String[] split = builder.toString().split(" ");
//        boolean flag1 = (crc(rxValue)) >> 8 == rxValue[58] && (byte) crc(rxValue) == rxValue[59];
            listAdapter.clear();
            set(split);

            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

        }
    }

    int crc(byte[] data, int startIndex, int len) {
        int result = 0;
        for (int i = 0; i < len; i++) {
            result += data[i + startIndex];
        }

        return result;

    }

    /**
     * @param low
     * @param high
     * @param ex
     * @param offset
     * @return
     */
    private double calcTwoByte(String low, String high, double ex, int offset, int saveNo) {
        return savePost((Integer.parseInt(low, 16) + Integer.parseInt(high, 16) * 256) * ex + offset, saveNo);
    }

    private double calcOneByte(String low, double ex, int offset, int saveNo) {
        return savePost(Integer.parseInt(low, 16) * ex + offset, saveNo);
    }

    private DecimalFormat df = new DecimalFormat("#.00");

    private double savePost(double f, int no) {
        BigDecimal bg = new BigDecimal(f);
        return bg.setScale(no, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
        try {
            // 解注册广播过滤器
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            System.out.println(ignore.toString());
        }
        // 解绑定服务
        unbindService(mServiceConnection);
        // 关闭服务对象
        mService.stopSelf();
        mService = null;
    }

    private void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("在MainActivity下按下了back键");
    }
}
