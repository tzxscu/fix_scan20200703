package com.ttsssdz.fix_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private ListView infolist;
    private EditText editText;
    private Button button;
    private HashMap<String,Object> ass_info = null;
    private List<HashMap<String,Object>> assests_info = null;
    private  String net_result = null;
    private static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    private ZLoadingDialog dialog;


    private static String url="http://10.107.76.219/asset_web/Service1.asmx/getasset_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH) {
                   // System.out.println("这里是监听扫码枪的回车事件");
                    Toast.makeText(MainActivity.this,"这里是监听扫码枪的回车事件",Toast.LENGTH_SHORT).show();

                    return true;
                }
                if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER&&v.getText()!=null&& event.getAction() == KeyEvent.ACTION_DOWN){
                  //  System.out.println("这里是监听手机的回车事件");
                 //   getinfo(v.getText().toString());
                    dialog = new ZLoadingDialog(MainActivity.this);
                    dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)
                            .setLoadingColor(Color.parseColor("#00000000"))
                            .setHintText("正在加载中...")
//                                .setHintTextSize(16) // 设置字体大小
                            .setHintTextColor(Color.GRAY)  // 设置字体颜色
//                                .setDurationTime(0.5) // 设置动画时间百分比
                            .setDialogBackgroundColor(Color.parseColor("#cc111111")) // 设置背景色
                            .show();

                    infolist.setAdapter(null);
                    String code = editText.getText().toString();
                    getinfo(code);
                }
                return true;
            }
        });

      /*  button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new ZLoadingDialog(MainActivity.this);
                dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)
                        .setLoadingColor(Color.parseColor("#00000000"))
                        .setHintText("正在加载中...")
//                                .setHintTextSize(16) // 设置字体大小
                        .setHintTextColor(Color.GRAY)  // 设置字体颜色
//                                .setDurationTime(0.5) // 设置动画时间百分比
                        .setDialogBackgroundColor(Color.parseColor("#cc111111")) // 设置背景色
                        .show();

                String code = editText.getText().toString();
                getinfo(code);
            }
        });*/
    }

    private void initview(){
        editText = (EditText)findViewById(R.id.code_text);
        infolist = (ListView)findViewById(R.id.listinfo);
     //   button = (Button)findViewById(R.id.btn);
    }

    private void getinfo(final String assetcode){
      //  Toast.makeText(MainActivity.this,assetcode,Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //异步调用web请求——————————————————————————开始
                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.newBuilder()
                        .connectTimeout(15,TimeUnit.SECONDS)
                        .readTimeout(15,TimeUnit.SECONDS).build();
                RequestBody requestBody = new FormBody.Builder()
                        .add("asset_code",assetcode.toUpperCase())
                        .build();
                Request request = new  Request.Builder()
                        //.addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                        .post(requestBody)
                        .url(url).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()){
                        net_result = response.body().string();
                    }
                    else {
                        String json_message = "请求失败："+response.message() +response.code();
                        net_result ="{\"Ret\":false,\"Retmsg\":\""+json_message+"\"}";
                    }
                    response.body().close();

                }catch (Exception e) {
                    net_result ="{\"Ret\":false,\"Retmsg\":\""+ e.getMessage()+"\"}";

                }
                //——————————————————————————————————结束
                Message message = handler.obtainMessage();
                if(net_result == null){
                   String json_message2 = "返回为空";
                   net_result ="{\"Ret\":false,\"Retmsg\":\""+json_message2+"\"}";
                }
                message.obj = net_result;
                handler.sendMessage(message);
            }
        }).start();
    }

    private Handler handler = new Handler(){
      public void handleMessage(Message msg){
         // Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
          List<HashMap<String,Object>> listdata = getlistdata(msg.obj.toString());
          if ((Boolean) listdata.get(0).get("Ret")){
              SimpleAdapter listAdapter = new SimpleAdapter(MainActivity.this,listdata
                      ,R.layout.list_v_view_tiems
                      , new String[]{"typeid","kph","Stramassetcode","glbm","zrbm","yz","kssyrq","gzmc","fybm"},
                      new int[]{R.id.typeid,R.id.kph,R.id.Stramassetcode,R.id.glbm,R.id.zrbm,R.id.yz,R.id.kssyrq,R.id.gzmc,R.id.fybm});
              infolist.setAdapter(listAdapter);
             // Toast.makeText(MainActivity.this,listdata.get(0).get("Retmsg")+"1".toString(),Toast.LENGTH_LONG).show();
          }else {
             // Log.i("TAG, ",listdata.get(0).get("Retmsg").toString());
              Toast.makeText(MainActivity.this,listdata.get(0).get("Retmsg").toString(),Toast.LENGTH_SHORT).show();
          }

          editText.setText("");
          dialog.dismiss();
      }
    };


    private List<HashMap<String,Object>> getlistdata(String jsonstr){
        List<HashMap<String,Object>> data = new ArrayList<HashMap<String, Object>>();
        HashMap<String,Object> hashMap = new HashMap<String, Object>();
        try {
          //  jsonstr = "{\"Ret\":true,\"Stramassetcode\":\"YBDQ000597\",\"Stramassetcardcode\":\"2330\",\"Typeid\":\"0/29107\",\"Retmsg\":\"成功\",\"Glbm\":\"设备科办公室\",\"Zrbmmc\":\"铁二院分院住院\"}";
            JSONObject jsonObject = new JSONObject(jsonstr);
            Boolean retflag =jsonObject.getBoolean("Ret");
            if(!retflag){
                String retmsg = jsonObject.getString("Retmsg");
                hashMap.put("Ret",retflag);
                hashMap.put("Retmsg",retmsg);
                data.add(hashMap);
            }else {
                hashMap.put("Ret",retflag);
                hashMap.put("Retmsg",jsonObject.getString("Retmsg"));
                hashMap.put("typeid",jsonObject.getString("Typeid"));
                hashMap.put("kph",jsonObject.getString("Stramassetcardcode"));
                hashMap.put("Stramassetcode",jsonObject.getString("Stramassetcode"));
                hashMap.put("glbm",jsonObject.getString("Glbm"));
                hashMap.put("zrbm",jsonObject.getString("Zrbmmc"));
                hashMap.put("yz",jsonObject.getString("Gzyz"));
                hashMap.put("kssyrq",jsonObject.getString("Rzrq"));
                hashMap.put("gzmc",jsonObject.getString("Stramassetname"));
                hashMap.put("fybm",jsonObject.getString("Fybm"));
                data.add(hashMap);
            }
        }
        catch (JSONException e)
        {
            hashMap.put("Ret",false);
            hashMap.put("Retmsg",e.getMessage().toString());
            data.add(hashMap);
        }
        return data;
    }

}