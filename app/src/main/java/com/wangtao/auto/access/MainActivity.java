package com.wangtao.auto.access;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.wangtao.auto.access.utils.LogUtils;
import com.wangtao.auto.access.utils.PermissionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    public static final int SERVER_PORT = 8887;
    public static String SERVER_ADDRESS = "192.168.1.94";
    public static int SCREEN_Height;
    public static int SCREEN_Width;

    private MediaProjectionManager projectionManager;
    private int SCREEN_SHOT = 1;
    private MediaProjection mediaProject;
    private ImageReader imageReader;
    private int imgWidth = 1080;
    private int imgHeight = 1920;
    private int mDensity = 2;
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;


    private EditText etIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initScreenSize();
        etIpAddress = (EditText) findViewById(R.id.main_ip_et);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        imgWidth = ScreenUtils.getScreenWidth() / 1;
        imgHeight = ScreenUtils.getScreenHeight() / 1;

        if (PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        }
        EventBus.getDefault().register(this);
    }

    private void initScreenSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SCREEN_Height = metrics.heightPixels;
        SCREEN_Width = metrics.widthPixels;
    }

    public void onclickStart(View view) {
        SERVER_ADDRESS = etIpAddress.getText().toString();
        if (TextUtils.isEmpty(SERVER_ADDRESS)) {
            Toast.makeText(this, "服务器ip错误!", Toast.LENGTH_SHORT).show();
            return;
        }

        countIndex = 0;
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_SHOT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCREEN_SHOT) {
            mediaProject = projectionManager.getMediaProjection(resultCode, data);
            imageReader = ImageReader.newInstance(imgWidth, imgHeight, PixelFormat.RGBA_8888, 2);
            if (imageReader != null) {
                LogUtils.i("imageReader===success");
            }


            mediaProject.createVirtualDisplay("screencap", imgWidth, imgHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, imageReader.getSurface(), null, mHandler);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = imageReader.acquireLatestImage();
                    if (image == null) {
                        return;
                    }
                    int width = image.getWidth();

                    int height = image.getHeight();

                    final Image.Plane[] planes = image.getPlanes();

                    final ByteBuffer buffer = planes[0].getBuffer();

                    int pixelStride = planes[0].getPixelStride();

                    int rowStride = planes[0].getRowStride();

                    int rowPadding = rowStride - pixelStride * width;

                    Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);

                    bitmap.copyPixelsFromBuffer(buffer);

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

//                        saveBitmap(bitmap);
                    sendBitmaSocket(bitmap);


                    image.close();

                }
            }, mHandler);
        }
    }

    private void sendBitmaSocket(final Bitmap bitmap) {
        new Thread() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                    dos.writeInt(baos.size());
                    dos.write(baos.toByteArray());
                    dos.flush();
                    baos.close();
                    //x,y
                    DataInputStream out = new DataInputStream(socket.getInputStream());
                    String str = out.readUTF();
                    LogUtils.i("从服务器读取到：" + str);
                    EventBus.getDefault().post(str);
                    out.close();
                    dos.close();

                    socket.close();
                } catch (IOException e) {
                    if (!(e instanceof EOFException)) {
                        e.printStackTrace();
                    }

                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setSocketImage(Image image) {

        Image.Plane[] planesArray = image.getPlanes();

        Socket socket;
        try {// 创建一个Socket对象，并指定服务端的IP及端口号
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeInt(0);
            dos.write(0);

            socket.close();
            LogUtils.i("上传文件完成了===");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int countIndex = 0;

    private void saveBitmap(Bitmap bitmap) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + "/android_test/screen");
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file.getAbsolutePath() + "/screen_" + System.currentTimeMillis() + ".jpg");
        if (f.exists()) {
            f.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(f);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        out.flush();
        out.close();
        countIndex++;
        LogUtils.i("截屏了多少张图片了：" + countIndex);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onclickStop(View view) {
        if (mediaProject == null) {
            return;
        }
        mediaProject.stop();
        Toast.makeText(this, "停止截屏", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length < 1) {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionSuccess(requestCode);
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();

        }

    }

    private void permissionSuccess(int requestCode) {
        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
    }


    public void onclickSocketTest(View view) {
        SERVER_ADDRESS = etIpAddress.getText().toString();
        new Thread() {
            @Override
            public void run() {
                LogUtils.i("init=======");
                final File file = new File(Environment.getExternalStorageDirectory() + "/android_test/screen/test1.jpg");
                if (!file.exists()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "文件不存在:" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                Socket socket;
                try {// 创建一个Socket对象，并指定服务端的IP及端口号
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    int size = (int) file.length();
                    System.out.println("size = " + size / 1024.0f);
                    byte[] data = new byte[size];
                    // 创建一个InputStream用户读取要发送的文件。
                    FileInputStream inputStream = new FileInputStream(file);
                    inputStream.read(data);

                    dos.writeInt(size);
                    dos.write(data);
                    inputStream.close();
                    socket.close();
                    LogUtils.i("上传文件完成了===");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getServerOnclickXY(int positionXY) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
