package com.example.socketserviceappliacation03195020;


import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class MyAPP extends android.app.Application {

    //socket通信
    private Socket socket = null;
    private ServerSocket serverSocket = null;

    public static OutputStream outputStream;
    private static ConnectListener mListener;
    final LinkedList<Socket> list = new LinkedList<Socket>();

    private HandlerThread mHandlerThread;
    //子线程中的Handle实例
    private Handler mSubThreadHandler;



    @Override
    public void onCreate() {
        super.onCreate();
        //启动服务器
        ServerListeners listeners = new ServerListeners();
        listeners.start();
        initHandlerThread();
    }

    public class ServerListeners extends Thread {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(7777);
                while (true) {
                    System.out.println("等待客户端请求...");
                    socket = serverSocket.accept();
                    System.out.println("收到请求。服务器建立连接...");
                    System.out.println("客户端" + socket.getInetAddress().getHostAddress() + "连接成功");
                    System.out.println("客户端" + socket.getRemoteSocketAddress() + "连接成功");
                    list.add(socket);
                    //每次都启动一个新的线程
                    new Thread(new Task(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initHandlerThread() {
//创建HandlerThread实例
        mHandlerThread = new HandlerThread("handler_thread");
//开始运行线程
        mHandlerThread.start();
//获取HandlerThread线程中的Iooper实例
        Looper loop = mHandlerThread.getLooper();
//创建Handler与该线程绑定。
        mSubThreadHandler = new Handler(loop) {
            public void handleMessage(Message msg) {
                writeMsg(msg.getData().getString("datal"));
            }
        };
    }
    /**
     * 处理Socket请求的线程类
     */
    class Task implements Runnable {
        private Socket socket;

        /**
         * 构造函数
         *
         * @param socket
         */
        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                int size;
                try {
                    InputStream inputStream = null;//输入流
                    inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        if (buffer[0] != (byte) 0xEE) {
                            //将读取的1024个字节构造成一个String类型的变量
                            String data = new String(buffer, 0, size, "gbk");
                            Message message = new Message();
                            message.what = 100;
                            Bundle bundle = new Bundle();
                            bundle.putString("data", data);

                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //接口回调
    public interface ConnectListener {
        void onReceiveData(String data);
    }

    public void setOnConnectListener(ConnectListener listener) {
        this.mListener = listener;
    }


    Handler mHandler  = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 100:
                    if(mListener!=null){
                        mListener.onReceiveData(msg.getData().getString("data"));
                    }
                    break;
            }
        }
    };





    /**
     *发送数据
     *
     *@param
     */
    public void send(String bytes){
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("datal",bytes);
        msg.setData(bundle);
        mSubThreadHandler.sendMessage(msg);
    }
    private void writeMsg(String msg){
        for (Socket s :list){
            System.out.println("客户端"+s.getInetAddress().getHostAddress());
            try{
                outputStream = s.getOutputStream();
                if (outputStream != null){
                    outputStream.write(msg.getBytes("gbk"));
                    outputStream.flush();
                }
            }catch (IOException e){
                //TODO Auto-generated catch block
                e.printStackTrace();
            }catch (Exception e){
                System.out.println("客户端socket不存在。");
            }
        }
    }
    /**
     * 断开连接
     */
    public void disconnect()throws IOException{
        System.out.println("客户端是否关闭1");
        if (list.size() != 0){
            for (Socket s: list){
                s.close();
                System.out.println("客户端是否关闭2");
            }
        }
        if (outputStream != null)
            outputStream.close();
        list.clear();
    }
}
