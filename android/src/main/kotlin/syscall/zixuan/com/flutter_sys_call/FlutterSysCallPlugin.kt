package syscall.zixuan.com.flutter_sys_call

import android.app.Activity
import android.os.Vibrator
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterSysCallPlugin(newActivity:Activity): MethodCallHandler {
  var activity:Activity?=null;
  init {
    activity=newActivity;
  }

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_sys_call")
      channel.setMethodCallHandler(FlutterSysCallPlugin(registrar.activity()))
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
     when(call.method){
       "getPlatformVersion"->{
         result.success("Android ${android.os.Build.VERSION.RELEASE}")
       }
       "doVibrator"->{
         doVibrator()
         result.success(true);
       }
       else-> {
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
    var vibrator=activity!!.getSystemService(Activity.VIBRATOR_SERVICE) as Vibrator;
    vibrator.vibrate(3000);
  }
}
