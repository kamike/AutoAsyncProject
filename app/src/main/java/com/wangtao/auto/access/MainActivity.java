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
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private MediaProjectionManager projectionManager;
    private int SCREEN_SHOT = 1;
    private MediaProjection mediaProject;
    private ImageReader imageReader;
    private int imgWidth = 1080;
    private int imgHeight = 1920;
    private int mDensity = 2;
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        if (PermissionUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onclickStart(View view) {
        countIndex = 0;
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_SHOT);
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
                    try {
                        saveBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    image.close();

                }
            }, mHandler);
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
        Socket socket;
        try {// 创建一个Socket对象，并指定服务端的IP及端口号
            socket = new Socket("192.168.1.127", 1989);
            // 创建一个InputStream用户读取要发送的文件。
            InputStream inputStream = new FileInputStream("e://a.txt");
            // 获取Socket的OutputStream对象用于发送数据。
            OutputStream outputStream = socket.getOutputStream();
            // 创建一个byte类型的buffer字节数组，用于存放读取的本地文件
            byte buffer[] = new byte[4 * 1024];
            int temp = 0;
            // 循环读取文件
            while ((temp = inputStream.read(buffer)) != -1) {
                // 把数据写入到OuputStream对象中
                outputStream.write(buffer, 0, temp);
            }
            // 发送读取的数据到服务端
            outputStream.flush();

            /** 或创建一个报文，使用BufferedWriter写入,看你的需求 **/
//          String socketData = "[2143213;21343fjks;213]";
//          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
//                  socket.getOutputStream()));
//          writer.write(socketData.replace("\n", " ") + "\n");
//          writer.flush();
            /************************************************/
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
