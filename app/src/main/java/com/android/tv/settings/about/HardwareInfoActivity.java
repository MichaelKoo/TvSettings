package com.android.tv.settings.about;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.android.tv.settings.R;

/**
 * Created by gavin on 2016/7/26.
 */
public class HardwareInfoActivity extends Activity{
    private static final String TAG="HardwareInfoActivity";
    private static final boolean DEBUG = true;

    private  final String STEING_DEFAULT = "unknow";


    private boolean isPause;
    private TextView ramTextView;
    private TextView cpuTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hardware_info);
        ramTextView = (TextView) findViewById(R.id.ram_used);
        cpuTextView = (TextView) findViewById(R.id.cpu_used);

        ramTextView.setText("RAM : " + getMemoryInfo());
//        cpuTextView.setText("CPU");

    }


    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
        getCpuUsedThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    private Handler aboutHarwareHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (!isPause) {
                        String cpuUsed = (String)msg.obj;
                        cpuTextView.setText("CPU : " +cpuUsed);
                    }
                    break;
                case 1:
                    if (!isPause) {
                        String ramUsed = (String)msg.obj;
                        ramTextView.setText("RAM : " +ramUsed);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public  String getCpuInfo(boolean isReadCpuUsed, String cpuUsed) {
        String cpuInfo = "";
        String strTmp = "";
        String[] oneCpuInfo = new String[20];
        int cpuNum = 0;

        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            cpuInfo += array[1];

            while ((strTmp = br.readLine()) != null && cpuNum < 20) {
                if(strTmp.indexOf("BogoMIPS") != -1) {
                    String[] arrayTmp = strTmp.split(":\\s+", 2);
                    oneCpuInfo[cpuNum] = "\nprocessor " + cpuNum + "       " + arrayTmp[1];
                    Log.e(TAG, oneCpuInfo[cpuNum]);
                    cpuNum ++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isReadCpuUsed) {
            cpuInfo += "        " + cpuUsed;//getCpuUsed();
        }

        for(int i = 0; i < cpuNum; i ++) {
            cpuInfo += oneCpuInfo[i];
        }
        return cpuInfo;
    }

    public  String getMemoryInfo() {
        String str1 = "/proc/meminfo";
        String strTmp = "";
        String memTotal="";
        String memFree="";
        String memInfo = "";
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);

            while ((strTmp = localBufferedReader.readLine()) != null) {
                if(strTmp.indexOf("MemTotal") != -1) {
                    memTotal = strTmp;
                    //Log.e(LOG_TAG, "---" + memTotal + getNumForString(memTotal));
                } else if (strTmp.indexOf("MemFree") != -1) {
                    memFree = strTmp;
                    //Log.e(LOG_TAG, "---" + memFree + getNumForString(memFree));
                }
            }
            memFree = getNumForString(memFree);
            memTotal = getNumForString(memTotal);
            int memFreeInt = Integer.parseInt(memFree);
            int memtotalInt = Integer.parseInt(memTotal);

            memInfo = String.valueOf(memFreeInt/1024) + "MB / " +
                    String.valueOf(memtotalInt/1024) + "MB";
            return memInfo;
        } catch (IOException e) {
        }
        return STEING_DEFAULT;
    }

    public static String getNumForString(String srcString) {
        String destString = "";
        for (int i = 0; i<srcString.length(); i++) {
            if (srcString.charAt(i) >= 48 && srcString.charAt(i)<=57) {
                destString += srcString.charAt(i);
            }
        }
        return destString;
    }

    public void getCpuUsedThread() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!isPause) {
                    try{
                        Thread.sleep(100);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message cpuMsg = new Message();
                    cpuMsg.what = 0;
                    cpuMsg.obj = getCpuUsed();
                    if (aboutHarwareHandler != null && !isPause) {
                        aboutHarwareHandler.sendMessage(cpuMsg);
                    }

                    if (isPause) {
                        break;
                    }

                    try{
                        Thread.sleep(2000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (isPause) {
                        break;
                    }

                    Message ramMsg = new Message();
                    ramMsg.what = 1;
                    ramMsg.obj = getMemoryInfo();
                    if (aboutHarwareHandler != null && !isPause) {
                        aboutHarwareHandler.sendMessage(ramMsg);
                    }

                    if (isPause) {
                        break;
                    }
                    try{
                        Thread.sleep(3000);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }




    private  String getCpuUsed() {
        int rate = 0;

        try {
            String result = "";
            Process p=Runtime.getRuntime().exec("top -n 1");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = br.readLine()) != null) {
                if (result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = result.split("%");
                    String CPUusage = getNumForString(CPUusr[0]);
                    String SYSusage = getNumForString(CPUusr[1]);
                    Log.e(TAG, CPUusage+ SYSusage);
                    rate = Integer.parseInt(CPUusage) + Integer.parseInt(SYSusage);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dstString = String.valueOf(rate) +"%";
        return dstString;
    }
}
