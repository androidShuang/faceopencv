package kong.qingwei.opencv320;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kongqw.ObjectDetectingView;
import com.kongqw.ObjectDetector;
import com.kongqw.listener.OnFaceDetectorListener;
import com.kongqw.listener.OnOpenCVLoadListener;
import com.kongqw.utils.FaceUtil;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ObjectDetectingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener,OnFaceDetectorListener{

    private ObjectDetectingView objectDetectingView;
    private ObjectDetector mFaceDetector;
    private ObjectDetector mEyeDetector;
    private ObjectDetector mUpperBodyDetector;
    private ObjectDetector mLowerBodyDetector;
    private ObjectDetector mFullBodyDetector;
    private ObjectDetector mSmileDetector;
    private boolean faceIden = true;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_object_detecting);
        iv = (ImageView) findViewById(R.id.iv);

        ((RadioButton) findViewById(R.id.rb_face)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.rb_eye)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.rb_upper_body)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.rb_lower_body)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.rb_full_body)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.rb_smile)).setOnCheckedChangeListener(this);

        objectDetectingView = (ObjectDetectingView) findViewById(R.id.photograph_view);
        objectDetectingView.setOnOpenCVLoadListener(new OnOpenCVLoadListener() {
            @Override
            public void onOpenCVLoadSuccess() {
                Toast.makeText(getApplicationContext(), "OpenCV 加载成功", Toast.LENGTH_SHORT).show();
                mFaceDetector = new ObjectDetector(getApplicationContext(), R.raw.lbpcascade_frontalface, 6, 0.2F, 0.2F, new Scalar(255, 0, 0, 255));
                mEyeDetector = new ObjectDetector(getApplicationContext(), R.raw.haarcascade_eye, 6, 0.1F, 0.1F, new Scalar(0, 255, 0, 255));
                mUpperBodyDetector = new ObjectDetector(getApplicationContext(), R.raw.haarcascade_upperbody, 3, 0.3F, 0.4F, new Scalar(0, 0, 255, 255));
                mLowerBodyDetector = new ObjectDetector(getApplicationContext(), R.raw.haarcascade_lowerbody, 3, 0.3F, 0.4F, new Scalar(255, 255, 0, 255));
                mFullBodyDetector = new ObjectDetector(getApplicationContext(), R.raw.haarcascade_fullbody, 3, 0.3F, 0.5F, new Scalar(255, 0, 255, 255));
                mSmileDetector = new ObjectDetector(getApplicationContext(), R.raw.haarcascade_smile, 10, 0.2F, 0.2F, new Scalar(0, 255, 255, 255));
                findViewById(R.id.radio_group).setVisibility(View.VISIBLE);
            }

            @Override
            public void onOpenCVLoadFail() {
                Toast.makeText(getApplicationContext(), "OpenCV 加载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotInstallOpenCVManager() {
                showInstallDialog();
            }
        });

        objectDetectingView.loadOpenCV(getApplicationContext());
    }

    /**
     * 切换摄像头
     *
     * @param view view
     */
    public void swapCamera(View view) {
        objectDetectingView.swapCamera();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rb_face:
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "人脸检测", Toast.LENGTH_SHORT).show();
                    mFaceDetector.setOnFaceDetectorListener(this);
                    objectDetectingView.addDetector(mFaceDetector);
                } else {
                    objectDetectingView.removeDetector(mFaceDetector);
                }
                break;
            case R.id.rb_eye:
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "眼睛检测", Toast.LENGTH_SHORT).show();
                    objectDetectingView.addDetector(mEyeDetector);
                } else {
                    objectDetectingView.removeDetector(mEyeDetector);
                }
                break;
            case R.id.rb_upper_body:
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "上半身检测", Toast.LENGTH_SHORT).show();
                    objectDetectingView.addDetector(mUpperBodyDetector);
                } else {
                    objectDetectingView.removeDetector(mUpperBodyDetector);
                }
                break;
            case R.id.rb_lower_body:
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "下半身检测", Toast.LENGTH_SHORT).show();
                    objectDetectingView.addDetector(mLowerBodyDetector);
                } else {
                    objectDetectingView.removeDetector(mLowerBodyDetector);
                }
                break;
            case R.id.rb_full_body:
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "全身检测", Toast.LENGTH_SHORT).show();
                    objectDetectingView.addDetector(mFullBodyDetector);
                } else {
                    objectDetectingView.removeDetector(mFullBodyDetector);
                }
                break;
            case R.id.rb_smile:
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "微笑检测", Toast.LENGTH_SHORT).show();
                    objectDetectingView.addDetector(mSmileDetector);
                } else {
                    objectDetectingView.removeDetector(mSmileDetector);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFace(Mat mat, MatOfRect rect) {
        Log.e("faceIden",faceIden+"");
        if(faceIden) {
            List<Rect> rectList = rect.toList();
            if(rectList!=null&&rectList.size()>0) {
                FaceUtil.saveImage(this, mat, rectList.get(0), "123");
                final Bitmap bitmap = FaceUtil.getImage(this,"123");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       if(bitmap!=null){
                           iv.setImageBitmap(bitmap);
                           try {
                               String pic = PictureUtils.bitmapToString(bitmap);
                               uploadPic(pic,"http://www.awesomekick.com:9900/user");
                               faceIden = false;
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                    }
                });
                Log.e("Face", "回调成功了" + rectList.size());
            }
        }
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public void uploadPic(String image, String url) throws IOException {



        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).build();

        UserDao user = new UserDao();
        user.setName("zhangsan");
        user.setAge("12");
        user.setImage(image);

        Gson gson = new Gson();

        String json = gson.toJson(user);

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                faceIden = true;
                Log.e("请求报错",e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("判断","0".equals(response.body().toString())+"");
                if("0".equals(response.body().toString())){
                    faceIden = true;
                }
                faceIden = true;
                Log.e("node返回的数据",response.body().string());
            }
        });
//        return response.body().string();
    }

}
