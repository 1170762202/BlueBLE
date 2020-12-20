package com.zlx.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zlx.myapplication.ACSUtility.blePort;
import com.zlx.myapplication.blue.BytesHexStrTranslate;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Zlx on 2018/12/12/012.
 * Email:1170762202@qq.com
 */
public class MainAc extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button mEnumPortsBtn, mOpenOrCloseBtn, mSendBtn, mClearBtn, mReceiveClearBtn, mSendClearBtn;
    private TextView mSendCountTV, mReceiveCountTV, mReceiveRateTV;
    private TextView mReceiveDataTV, mSendDataTV, tvData;
    private EditText mInternalTV;
    private TextView mSelectedPortTV;
    private Switch mSendFormat, mReceiveFormat, mReceiveClear;
    private ScrollView mScrollView;
    private CheckBox mPeriod;
    private ACSUtility util;
    private boolean isPortOpen = false, isReceiveHex = false, isSendHex = false, isClear = true;
    private boolean isPeriod = false;
    private blePort mCurrentPort, mSelectedPort;
    private String mReceivedData, mSendData;
    private int mTotalReceiveSize = 0, mLastSecondTotalReceiveSize = 0;
    private int mTotalSendSize = 0;
    private int mInternal = 0;
    private countRecevieRateThread mCountRecevieRateThread;
    private ProgressDialog mProgressDialog;
    public final static int REQEEST_ENUM_PORTS = 10;
    private RecyclerView recyclerView;

    private Handler mCountReceiveRateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //super.handleMessage(msg);

        }

    };
    private final static String TAG = "ACSMainActivity";
    private ACSUtility.IACSUtilityCallback userCallback = new ACSUtility.IACSUtilityCallback() {

        @Override
        public void didFoundPort(blePort newPort) {
            // TODO Auto-generated method stub

        }

        @Override
        public void didFinishedEnumPorts() {
            // TODO Auto-generated method stub

        }

        @Override
        public void didOpenPort(blePort port, Boolean bSuccess) {
            // TODO Auto-generated method stub
            Log.d(TAG, "The port is open ? " + bSuccess);
            if (bSuccess) {
                isPortOpen = true;
                mCurrentPort = port;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        updateUiObject();
                        showSuccessDialog();
                        getProgressDialog().cancel();
                    }
                });
            } else {
                getProgressDialog().cancel();
                showFailDialog();
            }

        }


        @Override
        public void didClosePort(blePort port) {
            // TODO Auto-generated method stub
            isPortOpen = false;
            mCurrentPort = null;
            if (getProgressDialog().isShowing()) {
                getProgressDialog().dismiss();
            }
            Toast.makeText(MainAc.this, "Disconnected from Peripheral", Toast.LENGTH_SHORT).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    updateUiObject();
                }
            });
        }

        @Override
        public void didPackageReceived(blePort port, byte[] packageToSend) {
            Log.e("TAG", "packageReceived=" + Arrays.toString(packageToSend));
            // TODO Auto-generated method stub
			/*try {
				mReceivedData = new String(packageToSend, "GBK");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateUiObject();
					}
				});
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
            StringBuilder sb = new StringBuilder();
            if (!isClear)
                sb.append(mReceivedData);
            if (isReceiveHex) {
                for (byte b : packageToSend) {
                    sb.append("0x");
                    if ((b & 0xff) <= 0x0f) {
                        sb.append("0");
                    }
                    sb.append(Integer.toHexString(b & 0xff) + " ");
                }
                Log.e("TAG", "接收到=" + sb.toString());

            } else {
                String Rx_str = "";
                for (int i = 0; i < packageToSend.length; i++) {
                    if (packageToSend[i] >= 0)
                        Rx_str = Rx_str + Integer.toHexString(packageToSend[i]) + " ";
                    else
                        Rx_str = Rx_str + Integer.toHexString(packageToSend[i] & 0x0ff) + " ";
                }
                Log.e("TAG", "接收到=" + Rx_str);
                try {
                    doReceiptMsg(Rx_str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mReceivedData = sb.toString();
            mTotalReceiveSize += packageToSend.length;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    updateUiObject();
                    //mLastSecondTotalReceiveSize = mTotalReceiveSize;
                }
            });
        }

        @Override
        public void heartbeatDebug() {
            // TODO Auto-generated method stub

        }

        @Override
        public void utilReadyForUse() {
            // TODO Auto-generated method stub

        }

        @Override
        public void didPackageSended(boolean succeed) {
            // TODO Auto-generated method stub
            if (succeed) {
                Toast.makeText(MainAc.this, "数据发送成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainAc.this, "数据发送失败", Toast.LENGTH_SHORT).show();
            }
        }

    };

    private List<String> list = new ArrayList<>();
    boolean flag = true;
    private int REQUEST_ENABLE_BT = 11;

    private void doReceiptMsg(String rxValue) throws Exception {
//        String Rx_str = new String(rxValue, "UTF-8");
//        listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] RX: " + Rx_str);
        String[] split1 = rxValue.split(" ");//每个字节的数组
//        for (int i = 0; i < split1.length; i++) {
//            if ((split1[i].equals("55")) && (split1[i + 1].equals("AA") || split1[i + 1].equals("aa"))) {
//                flag = true;
//                break;
//            }
//        }
        list.addAll(Arrays.asList(split1));

        if (list.size() < 458) {
            return;
        }

        Log.e("TAG", "sp=" + list.size());

        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s + " ");
        }
//        listAdapter.add("收到的数据：" + sb.toString());
        String[] split = builder.toString().split(" ");
//        boolean flag1 = (crc(rxValue)) >> 8 == rxValue[58] && (byte) crc(rxValue) == rxValue[59];
        set(split);
        list.clear();
    }

    private double calcTwoByte(String low, String high, double ex, int offset, int saveNo) {
        return savePost((Integer.parseInt(low, 16) + Integer.parseInt(high, 16) * 256) * ex + offset, saveNo);
    }

    private double calc3Byte(String low, String mid, String high, double ex, int offset, int saveNo) {
        return savePost((Integer.parseInt(low, 16) + Integer.parseInt(mid, 16) + Integer.parseInt(high, 16) * 256) * ex + offset, saveNo);
    }


    private double calcOneByte(String low, double ex, int offset, int saveNo) {
        return savePost(Integer.parseInt(low, 16) * ex + offset, saveNo);
    }

    private DecimalFormat df = new DecimalFormat("#.00");

    private double savePost(double f, int no) {
        BigDecimal bg = new BigDecimal(f);
        return bg.setScale(no, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private String getDCStatus(int value) {
        return value != 0 ? "开启均衡" : "未开启";
    }

    private String replacePointZero(String str) {
        return str.replace(".0", "");
    }

    private void set(String[] split) {
        StringBuilder sb = new StringBuilder();
        double 内总压 = calcTwoByte(split[7], split[8], 0.1, 0, 1);
        double 外总压 = calcTwoByte(split[9], split[10], 0.1, 0, 1);
        sb.append("内总压: " + 内总压 + "V");
        sb.append("\n");
        sb.append("外总压: " + 外总压 + "V");
        sb.append("\n");
        double 电流 = calcTwoByte(split[11], split[12], 0.1, 3000, 1);
        sb.append("电流: " + (电流 > 0 ? "放电 " : "充电 ") + 电流 + "A");
        sb.append("\n");

        double SOC = calcTwoByte(split[13], split[14], 0.1, 0, 1);
        sb.append("SOC: " + SOC + "%");
        sb.append("\n");

        double SOH = calcTwoByte(split[15], split[16], 0.1, 0, 1);
        sb.append("SOH: " + SOH + "%");
        sb.append("\n");

        double 最高单体电压 = calcTwoByte(split[17], split[18], 1, 0, 1);
        sb.append("最高单体电压: " + 最高单体电压 + "mV");
        sb.append("\n");

        double 最低单体电压 = calcTwoByte(split[19], split[20], 1, 0, 1);
        sb.append("最低单体电压: " + 最低单体电压 + "mV");
        sb.append("\n");

        double 最高单体电压位置 = calcOneByte(split[21], 1, 0, 1);
        sb.append("最高单体电压位置: " + 最高单体电压位置 + "");
        sb.append("\n");

        double 最低单体电压位置 = calcOneByte(split[22], 1, 0, 1);
        sb.append("最低单体电压位置: " + 最低单体电压位置 + "");
        sb.append("\n");

        double 最高单体温度 = calcOneByte(split[23], 1, -40, 1);
        sb.append("最高单体温度: " + 最高单体温度 + "℃");
        sb.append("\n");

        double 最低单体温度 = calcOneByte(split[24], 1, -40, 1);
        sb.append("最低单体温度: " + 最低单体温度 + "℃");
        sb.append("\n");

        double 最高单体温度位置 = calcOneByte(split[25], 1, -40, 1);
        sb.append("最高单体温度位置: " + 最高单体温度位置 + "℃");
        sb.append("\n");

        double 最低单体温度位置 = calcOneByte(split[26], 1, -40, 1);
        sb.append("最低单体温度位置: " + 最低单体温度位置 + "℃");
        sb.append("\n");

        //27 + 20*2 -1
        int index = 66;
        int pos = 0;
        for (int i = 27; i <= index; i += 2) {
            double cell = calcTwoByte(split[i], split[i + 1], 1, 0, 1);
            sb.append("单体电压_" + (pos++) + ": " + cell + "mV");
            sb.append("\n");
        }

        //67 + 10 * 1 - 1
        index = 76;
        pos = 0;
        for (int i = 67; i <= index; i++) {
            double cell = calcTwoByte(split[i], split[i + 1], 1, 0, 1);
            sb.append("电池温度_" + (pos++) + ": " + cell + "℃");
            sb.append("\n");
        }
        double 环境温度 = calcOneByte(split[77], 1, 0, 1);
        sb.append("环境温度1: " + 环境温度 + "℃");
        sb.append("\n");

        double 环境温度2 = calcOneByte(split[78], 1, 0, 1);
        sb.append("环境温度2: " + 环境温度2 + "℃");
        sb.append("\n");

        double MOS管温度 = calcOneByte(split[79], 1, 0, 1);
        sb.append("MOS管温度: " + MOS管温度 + "℃");
        sb.append("\n");

        double 均衡状态 = calc3Byte(split[80], split[81], split[82], 1, 0, 1);
        sb.append("均衡状态: " + (均衡状态 == 0 ? "断开" : "闭合"));
        sb.append("\n");

        double Satus1 = calcOneByte(split[83], 1, 0, 1);
        sb.append("Satus1: " + Satus1);
        sb.append("\n");

        double Satus2 = calcOneByte(split[84], 1, 0, 1);
        sb.append("Satus2: " + Satus2);
        sb.append("\n");

        index = 93;
        pos = 0;
        for (int i = 85; i <= index; i++) {
            byte[] bytes = BytesHexStrTranslate.toBytes(split[i]);
            pos++;
            sb.append("电池系统故障" + pos + ":" + "" + getDCStatus(bytes[0] >> pos & 0x01));
        }

        index = 101;
        pos = 0;
        for (int i = 94; i <= index; i++) {
            byte[] bytes = BytesHexStrTranslate.toBytes(split[i]);
            pos++;
            sb.append("BMS硬件故障" + pos + ":" + "" + getDCStatus(bytes[0] >> pos & 0x01));
        }

        double 电池系统故障1 = calcOneByte(split[85], 1, 0, 1);
        sb.append("Satus2: " + Satus2);
        sb.append("\n");


        double 电池状态 = calcOneByte(split[8], 1, 0, 1);
        String 电池状态_status = null;
        if (电池状态 == 0) {
            电池状态_status = "静止";
        } else if (电池状态 == 1) {
            电池状态_status = "充电";
        } else {
            电池状态_status = "放电";
        }
        sb.append("电池状态:" + 电池状态_status);
        byte[] bytes3 = BytesHexStrTranslate.toBytes(split[9]);
//        tv5.setVisibility(View.GONE);
//        tv5.setText("第1节:" + getDCStatus(split[0]) + "  第2节:" + getDCStatus(split[1]) + "  第3节:" +
//                getDCStatus(split[2]) + "  第4节:" + getDCStatus(split[3]) + "  第5节:" + getDCStatus(split[4]) + "  第6节:" + getDCStatus(split[5]));
//        double 均衡状态 = calcOneByte(s1, 1, 0, 1);
//        tv5.setText("均衡状态:" + (均衡状态 == 0 ? "未开启均衡" : "开启均衡"));
//        replacePointZero(tv5);

        double 年 = calcOneByte(split[10], 1, 0, 0);
        double 月 = calcOneByte(split[11], 1, 0, 0);
        double 日 = calcOneByte(split[12], 1, 0, 0);
        double 时 = calcOneByte(split[13], 1, 0, 0);
        double 分 = calcOneByte(split[14], 1, 0, 0);
        double 秒 = calcOneByte(split[15], 1, 0, 0);
        sb.append((split[10] + "-" + split[11] + "-" + split[12] + " " + split[13] + ":" + split[14] + ":" + split[15]));
        double 电压节数 = calcOneByte(split[16], 1, 0, 1);
        double 温度个数 = calcOneByte(split[17], 1, 0, 1);
        sb.append("电压节数:" + 电压节数);
        sb.append("温度个数:" + 温度个数);
//        replacePointZero(tv7);
//        replacePointZero(tv8);

        double 第1节电池电压 = calcTwoByte(split[18], split[19], 1, 0, 1);
        double 第2节电池电压 = calcTwoByte(split[20], split[21], 1, 0, 1);
        sb.append("第1节电池电压:" + 第1节电池电压 + "mV" + "\n第1节均衡状态:" + getDCStatus(bytes3[0] & 0x01));
        sb.append("第2节电池电压:" + 第2节电池电压 + "mV" + "\n第2节均衡状态:" + getDCStatus(bytes3[0] >> 1 & 0x01));
//        replacePointZero(tv9);
//        replacePointZero(tv10);

        double 第3节电池电压 = calcTwoByte(split[22], split[23], 1, 0, 1);
        double 第4节电池电压 = calcTwoByte(split[24], split[25], 1, 0, 1);
        sb.append("第3节电池电压:" + 第3节电池电压 + "mV" + "\n第3节均衡状态:" + getDCStatus(bytes3[0] >> 2 & 0x01));
//        sb.append("第4节电池电压:" + 第4节电池电压 + "mV\n第4节均衡状态:" + getDCStatus(bytes3[0] >> 3 & 0x01));
//        replacePointZero(tv11);
//        replacePointZero(tv12);

        double 第5节电池电压 = calcTwoByte(split[26], split[27], 1, 0, 1);
        double 第6节电池电压 = calcTwoByte(split[28], split[29], 1, 0, 1);
        sb.append("第5节电池电压:" + 第5节电池电压 + "mV\n第5节均衡状态:" + getDCStatus(bytes3[0] >> 4 & 0x01));
        sb.append("第6节电池电压:" + 第6节电池电压 + "mV\n第6节均衡状态:" + getDCStatus(bytes3[0] >> 5 & 0x01));
//        replacePointZero(tv13);
//        replacePointZero(tv14);

        double 电池温度 = calcOneByte(split[30], 1, -40, 1);
        double 单体电池压差 = calcTwoByte(split[31], split[32], 1, 0, 1);
        sb.append("电池温度:" + 电池温度 + "");
        sb.append("单体电池压差:" + 单体电池压差 + "mV");
//        replacePointZero(tv15);
//        replacePointZero(tv16);


//        replacePointZero(tv29);
//        replacePointZero(tv30);


        double 电池故障1 = calcOneByte(split[54], 1, 0, 1);
        double 电池故障2 = calcOneByte(split[55], 1, 0, 1);
        double 电池故障3 = calcOneByte(split[56], 1, 0, 1);
        byte[] bytes = BytesHexStrTranslate.toBytes(split[54]);
        sb.append("电池故障:");
        sb.append("\n");
        if ((bytes[0] & 0x03) != 0) {
            sb.append("总压过高" + (bytes[0] & 0x03) + "级\n");
        }
        if ((bytes[0] >> 2 & 0x03) != 0) {
            sb.append("总压过低" + (bytes[0] >> 2 & 0x03) + "级\n");
        }
        if ((bytes[0] >> 4 & 0x03) != 0) {
            sb.append("单体过高" + (bytes[0] >> 4 & 0x03) + "级\n");
        }
        if ((bytes[0] >> 6 & 0x03) != 0) {
            sb.append("单体过低" + (bytes[0] >> 6 & 0x03) + "级\n");
        }

//        tv31.append(" 电池故障2:");
        byte[] bytes1 = BytesHexStrTranslate.toBytes(split[55]);
        if ((bytes1[0] & 0x03) != 0) {
            sb.append("压差过大" + (bytes1[0] & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 2 & 0x03) != 0) {
            sb.append("温差过大" + (bytes1[0] >> 2 & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 4 & 0x03) != 0) {
            sb.append("温度过高" + (bytes1[0] >> 4 & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 6 & 0x03) != 0) {
            sb.append("温度过低" + (bytes1[0] >> 6 & 0x03) + "级\n");
        }
//        tv31.append(" 电池故障3:");
        byte[] bytes2 = BytesHexStrTranslate.toBytes(split[56]);
        if ((bytes2[0] & 0x03) != 0) {
            sb.append("充电过流" + (bytes2[0] & 0x03) + "级\n");
        }
        if ((bytes2[0] >> 2 & 0x03) != 0) {
            sb.append("放电过流" + (bytes2[0] >> 2 & 0x03) + "级\n");
        }
        if ((bytes2[0] >> 4 & 0x03) != 0) {
            sb.append("SOC过高" + (bytes2[0] >> 4 & 0x03) + "级\n");
        }
        if ((bytes2[0] >> 6 & 0x03) != 0) {
            sb.append("SOC过低" + (bytes2[0] >> 6 & 0x03) + "级\n");
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
        tvData.setText(sb.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQEEST_ENUM_PORTS
                && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();

            BluetoothDevice device = bundle.getParcelable(BluetoothDevice.EXTRA_DEVICE);
            mSelectedPort = util.new blePort(device);
            //util = new ACSUtility(this, userCallback);
            //util.setUserCallback(userCallback);
            updateUiObject();
        } else if (requestCode == REQUEST_ENABLE_BT) {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(MainAc.this, "Bluetooth Disable...Quit...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);

        util = new ACSUtility(this, userCallback);
        mCountRecevieRateThread = new countRecevieRateThread();

        mCountReceiveRateHandler.postDelayed(mCountRecevieRateThread, 1000);
        initViewObject();
    }

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //mSendDataTV.setFocusable(true);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        util.closeACSUtility();
    }

    private void initViewObject() {
        mEnumPortsBtn = (Button) findViewById(R.id.enumAllPortsBtn);
        mOpenOrCloseBtn = (Button) findViewById(R.id.openOrClosePortsBtn);
        mSendBtn = (Button) findViewById(R.id.sendBtn);
        mClearBtn = (Button) findViewById(R.id.clearBtn);
        mReceiveClearBtn = (Button) findViewById(R.id.receiveTextClearBtn);
        mSendClearBtn = (Button) findViewById(R.id.sendTextClearBtn);


        mSendCountTV = (TextView) findViewById(R.id.sendCount);
        mReceiveCountTV = (TextView) findViewById(R.id.receiveCount);
        mReceiveRateTV = (TextView) findViewById(R.id.receviceRate);

        mReceiveDataTV = (TextView) findViewById(R.id.receiveData);
        tvData = (TextView) findViewById(R.id.tvData);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSendDataTV = (TextView) findViewById(R.id.sendData);
        mInternalTV = (EditText) findViewById(R.id.internal);

        mSelectedPortTV = (TextView) findViewById(R.id.selectedPort);

        mSendFormat = (Switch) findViewById(R.id.sendFormat);
        mReceiveFormat = (Switch) findViewById(R.id.receiveFormat);
        mReceiveClear = (Switch) findViewById(R.id.receiveClear);

        mScrollView = (ScrollView) findViewById(R.id.scroll);

        mPeriod = (CheckBox) findViewById(R.id.period);

        mEnumPortsBtn.setOnClickListener(this);
        mOpenOrCloseBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        mReceiveClearBtn.setOnClickListener(this);
        mSendClearBtn.setOnClickListener(this);

        mSendFormat.setOnCheckedChangeListener(this);
        mReceiveFormat.setOnCheckedChangeListener(this);
        mReceiveClear.setOnCheckedChangeListener(this);

        //mSendDataTV.setFocusable(false);

        //mInternalTV.seton
        isPeriod = mPeriod.isChecked();
        mPeriod.setOnCheckedChangeListener(this);

        //mReceiveDataTV.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		/*mEnumPortsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MainAc.this, EnumPortActivity.class);
				MainAc.this.startActivityForResult(intent, REQEEST_ENUM_PORTS);
			}
			
		});
		mOpenOrCloseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isPortOpened) {
					util.closePort();
				}
				else if (mSelectedPort != null) {
						util.openPort(mSelectedPort);
				}
				else {
						Log.i(TAG, "User didn't select a port...So the port won't be opened...");
				}
			}
			
		});
		mSendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String data = (String) mSendDataTV.getText();
				util.writePort(data.getBytes());
			}
			
		});*/

        mOpenOrCloseBtn.setText("Open");
    }

    private void updateUiObject() {
        // TODO Auto-generated method stub
        if (isPortOpen) {
            mOpenOrCloseBtn.setText("Close");
        } else {
            mOpenOrCloseBtn.setText("Open");
        }
        mSendCountTV.setText(mTotalSendSize + "");
        mReceiveCountTV.setText(mTotalReceiveSize + "");
        //mReceiveRateTV.setText((mTotalReceiveSize - mLastSecondTotalReceiveSize) + " B/s");

        mReceiveDataTV.setText(mReceivedData);


        if (mSelectedPort != null) {
            mSelectedPortTV.setText(mSelectedPort._device.getName());
        }
        scrollToBottom();
		/*if (isPortOpen) {
			
		}*/
    }

    private void scrollToBottom() {
        int off = mReceiveDataTV.getHeight() - mScrollView.getHeight();
        if (off > 0) {
            mScrollView.scrollTo(0, off);
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(MainAc.this)
                .setTitle("Open Port")
                .setMessage("open port success")
                .setPositiveButton("comfirm", new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showFailDialog() {
        new AlertDialog.Builder(MainAc.this)
                .setTitle("Open Port")
                .setMessage("open port failed")
                .setPositiveButton("comfirm", new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                }).show();
    }

    private ProgressDialog getProgressDialog() {
        if (mProgressDialog != null) {
            return mProgressDialog;
        }
        mProgressDialog = new ProgressDialog(MainAc.this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Connecting...");
        //progressDialog.setTitle(title)
        return mProgressDialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    class countRecevieRateThread implements Runnable {

        public void run() {
            // TODO Auto-generated method stub
            //super.run();
            //mReceiveRate.setText("接收速率：" + (totalReceiveSize - lastSecondTotalReceiveSize) + " B/s");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //updateUiObject();
                    mReceiveRateTV.setText((mTotalReceiveSize - mLastSecondTotalReceiveSize) + " B/s");
                }
            });
            mLastSecondTotalReceiveSize = mTotalReceiveSize;
            mCountReceiveRateHandler.postDelayed(mCountRecevieRateThread, 1000);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.enumAllPortsBtn:
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(MainAc.this, EnumPortActivity.class);
                this.startActivityForResult(intent, REQEEST_ENUM_PORTS);
                break;
            case R.id.openOrClosePortsBtn:
                if (isPortOpen) {
                    util.closePort();
                } else if (mSelectedPort != null) {
                    // 等待窗口
                    getProgressDialog().show();
                    util.openPort(mSelectedPort);
                } else {
                    Log.i(TAG, "User didn't select a port...So the port won't be opened...");
                }

                break;
            case R.id.sendBtn:
                //StringBuilder sb = new StringBuilder();
                sendData();
			/*String data = mSendDataTV.getText().toString();
			ByteArrayBuffer bab = new ByteArrayBuffer(data.length() / 2);
			
			if (isSendHex) {
				for (int i = 0; i < data.length();) {
					if (data.charAt(i) != ' ') {
						String byteStr =  data.substring(i, i + 2);
						bab.append(hexStrToByteArray(byteStr));
						i += 2;
					} else {
						i++;
					}
				}
			} else {
				byte []dataBytes = data.getBytes(); 
				bab.append(dataBytes, 0, dataBytes.length);
			}
			mTotalSendSize += bab.toByteArray().length;
			util.writePort(bab.toByteArray());
			updateUiObject();*/
			
			/*String data = "";
			try {
				data = readFileSdcardFile("/sdcard/test.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mTotalSendSize += data.length();
			util.writePort(data.getBytes());
			updateUiObject();*/
                break;
            case R.id.clearBtn:
                mTotalSendSize = 0;
                mTotalReceiveSize = 0;
                updateUiObject();
                break;
            case R.id.receiveTextClearBtn:
                mReceivedData = "";
                updateUiObject();
                break;
            case R.id.sendTextClearBtn:
                mSendData = "";
                mSendDataTV.setText(mSendData);
                updateUiObject();
        }
    }

    private void sendData() {
        String data = mSendDataTV.getText().toString();
        ByteArrayBuffer bab = new ByteArrayBuffer(data.length() / 2);

        if (isSendHex) {
            for (int i = 0; i < data.length(); ) {
                if (data.charAt(i) != ' ') {
                    String byteStr = data.substring(i, i + 2);
                    bab.append(hexStrToByteArray(byteStr));
                    i += 2;
                } else {
                    i++;
                }
            }
        } else {
            byte[] dataBytes = data.getBytes();
            bab.append(dataBytes, 0, dataBytes.length);
        }
        mTotalSendSize += bab.toByteArray().length;
        util.writePort(bab.toByteArray());
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                updateUiObject();
            }

        });

    }

    private byte hexStrToByteArray(String hexString) {
        byte[] hexStrBytes = hexString.getBytes();
        byte bit0 = Byte.decode("0x" + new String(new byte[]{hexStrBytes[0]}));
        bit0 = (byte) (bit0 << 4);
        byte bit1 = Byte.decode("0x" + new String(new byte[]{hexStrBytes[1]}));
        return (byte) (bit0 | bit1);
    }

    private SendPeriodThread mSendThread;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        switch (buttonView.getId()) {
            case R.id.sendFormat:
                mSendData = "";
                mSendDataTV.setText(mSendData);
                isSendHex = isChecked;
                break;
            case R.id.receiveFormat:
                mReceivedData = "";
                mReceiveDataTV.setText(mReceivedData);
                isReceiveHex = isChecked;
                break;
            case R.id.receiveClear:
                mReceivedData = "";
                mReceiveDataTV.setText(mReceivedData);
                isClear = !isChecked;
                break;
            case R.id.period:
                isPeriod = isChecked;
                if (isPeriod) {
                    mInternal = Integer.parseInt(mInternalTV.getText().toString());
                    mSendThread = new SendPeriodThread(mInternal);
                    mSendThread.start();
                } else {
                    if (mSendThread != null) {
                        mSendThread.stopThread();
                    }
                }
                break;
        }
    }

    private String readFileSdcardFile(String fileName) throws IOException {
        String res = "";
        byte[] buffer = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);

            int length = fin.available();

            buffer = new byte[length];
            fin.read(buffer);

            res = EncodingUtils.getString(buffer, "UTF-8");

            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return buffer;
        return res;
    }

    public class SendPeriodThread extends Thread {
        int mInternal;
        //byte []mData;
        boolean start = true;

        public SendPeriodThread(int internal) {
            mInternal = internal;
            //mData = data;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            while (start) {
                sendData();
                try {
                    Thread.sleep(mInternal);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void stopThread() {
            start = false;
        }
    }
}
