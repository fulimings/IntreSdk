package com.intre.mylibrary.serialport;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YQ04150 on 2018/7/11 0011.
 */

public class SerialPort {
    private static volatile SerialPort mSerialPort;
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
    private FileInputStream in;
    private FileOutputStream out;
    private boolean alwaysReceive = true;
    private byte[] buffer = new byte[1 * 1024];
    private List<OnDataChangeListener> list = new ArrayList<>();
    private boolean initSuccess = false;

    static {
        System.loadLibrary("native-lib");
    }

    public static SerialPort getInstance() {
        if (mSerialPort == null) {

            synchronized (SerialPort.class) {
                if (mSerialPort == null) {
                    mSerialPort = new SerialPort();
                }
            }

        }

        return mSerialPort;
    }


    public boolean initSerialPort(String devicePath, int baudrate) {
        File device = new File(devicePath);
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    return false;
                }
            } catch (IOException ioException) {
                return false;

            } catch (InterruptedException interruptedException) {
                return false;
            }
        }
        FileDescriptor fileDescriptor = open(device.getAbsolutePath(), baudrate, 0);
        if (fileDescriptor != null) {
            out = new FileOutputStream(fileDescriptor);
            in = new FileInputStream(fileDescriptor);
            if (out != null && in != null) {
                fixedThreadPool.execute(receiveRunnable);
                return true;
            }

        }
        return false;
    }


    private final Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            while (alwaysReceive) {
                try {
                    SystemClock.sleep(1);
                    for (OnDataChangeListener listener : list) {
                        listener.onDataChanged(in.read(buffer), buffer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }


            }


        }
    };

    public void writeData(byte[] dataArray) throws IOException {
        if (initSuccess) {
            out.write(dataArray);
        }

    }


    /**
     * 注册数据接收接口,项目中一个地方注册就可以
     *
     * @param onDataChangeListener
     */
    public void registerListener(OnDataChangeListener onDataChangeListener) {

        list.add(onDataChangeListener);

    }

    /**
     * 反注册 数据接收接口
     *
     * @param onDataChangeListener
     */
    private void unRegisterListener(OnDataChangeListener onDataChangeListener) {
        if (list.contains(onDataChangeListener)) {
            list.remove(onDataChangeListener);
        }
    }

    public interface OnDataChangeListener {
        void onDataChanged(int size, byte[] buffer);
    }


    private native static FileDescriptor open(String path, int baudrate,
                                              int flags);

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        alwaysReceive = false;
        close();
    }

    private native void close();
}
