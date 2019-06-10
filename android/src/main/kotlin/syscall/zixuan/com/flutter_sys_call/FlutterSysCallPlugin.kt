package syscall.zixuan.com.flutter_sys_call

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Vibrator
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import syscall.zixuan.com.flutter_sys_call.video.takevideo.CameraActivity
import syscall.zixuan.com.flutter_sys_call.video.takevideo.utils.RequestCode
import androidx.core.app.ActivityCompat.startActivityForResult
import com.yzq.zxinglibrary.android.CaptureActivity
import com.yzq.zxinglibrary.common.Constant


class FlutterSysCallPlugin(newActivity: Activity) : MethodCallHandler, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

    var SCAN_CODE=300;


    var activity: Activity? = null;

    private var result: Result? = null;

    init {
        activity = newActivity;
    }

    var permissionList = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {

            var flutterSysCallPlugin = FlutterSysCallPlugin(registrar.activity())
            //方法调用监听
            val methodChannel = MethodChannel(registrar.messenger(), "flutter_sys_call")
            methodChannel.setMethodCallHandler(flutterSysCallPlugin)
            ///监听页面调用的回调
            // 注册ActivityResult回调
            registrar.addActivityResultListener(flutterSysCallPlugin);
            //权限回调监听
            registrar.addRequestPermissionsResultListener(flutterSysCallPlugin);

            ///事件上报监听
            val eventChannel = EventChannel(registrar.messenger(), "flutter_sys_call.even")

            eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(objectc: Any?, evenSink: EventChannel.EventSink?) {

                }

                override fun onCancel(p0: Any?) {

                }

            })

        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {

        this.result = result;

        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "doVibrator" -> {
                doVibrator()
                result.success(true);
            }
            "recordVideo" -> {
                if (checkReadPermission(permissionList, 300)) {
                    activity!!.startActivityForResult(Intent(activity, CameraActivity::class.java), 200)
                }
            }
            "QRScan" -> {
                val intent = Intent(activity!!, CaptureActivity::class.java)
                activity!!.startActivityForResult(intent, SCAN_CODE)
            }
            else -> {
                result.notImplemented()
            }
        }
        if (call.method == "getPlatformVersion") {

        } else {

        }


    }

    /**
     * 震动
     */
    private fun doVibrator() {
        val vibrator = activity!!.getSystemService(Activity.VIBRATOR_SERVICE) as Vibrator;
        vibrator.vibrate(longArrayOf(100, 1000, 500, 1000), -1);
    }

    private fun checkReadPermission(permissions: Array<String>, requestCode: Int): Boolean {

        var flag = true;
        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(activity!!.baseContext, permission) != PackageManager.PERMISSION_GRANTED) {
                flag = false;
                ActivityCompat.requestPermissions(activity!!, permissions, requestCode);
            }

        }


        return flag;
    }

    ///Activity回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {

        System.out.println("返回码" + resultCode + "请求码"+requestCode)


            if (data != null) {
                 print(requestCode);
                if (resultCode == RequestCode.TAKE_PHOTO) {
                    ///拍图片
                    ///返回图片路径
                    result!!.success(data.getStringExtra("photo"))

                } else if (resultCode == RequestCode.TAKE_VIDEO) {
                    ///拍视频
                    ///返回视屏路径
                    result!!.success(data.getStringExtra("video"))
                }else if(requestCode==SCAN_CODE){
                    result!!.success(data.getStringExtra(Constant.CODED_CONTENT))
                }
            } else {
                result!!.success("");
            }

        return false;
    }

    ///请求权限回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grants: IntArray?): Boolean {

        print("请求" + grants!!.toList())

        var flag = true;
        for (grand in grants!!) {
            if (grand != PackageManager.PERMISSION_GRANTED) {
                flag = false;
            }
        }
        return false;

    }


//    fun createChargingStateChangeReceiver(events: EventChannel.EventSink): BroadcastReceiver {
//
//
//
//
//    }

}
