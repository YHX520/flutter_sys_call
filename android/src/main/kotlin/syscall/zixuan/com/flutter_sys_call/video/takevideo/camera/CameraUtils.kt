package syscall.zixuan.com.flutter_sys_call.video.takevideo.camera

import android.content.Context
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import syscall.zixuan.com.flutter_sys_call.video.takevideo.utils.SPUtils


internal object CameraUtils {
    /**
     * 摄像机闪光灯状态
     */
    val CAMERA_FLASH = "camera_flash"
    /**
     * 摄像机前后置状态
     */
    val CAMERA_AROUND = "camera_around"

    /**
     * 摄像机是否支持前置拍照
     * @return
     */
    val isSupportFrontCamera: Boolean
        get() {
            val cameraCount = Camera.getNumberOfCameras()
            val info = Camera.CameraInfo()
            for (i in 0 until cameraCount) {
                Camera.getCameraInfo(i, info)
                if (info.facing == 1) {
                    return true
                }
            }
            return false
        }

    /**
     * 获取相机闪光灯状态
     * @return 0为自动,1为打开,其他为关闭
     */
    fun getCameraFlash(context: Context): Int {
        return SPUtils.get(context, CAMERA_FLASH, 0) as Int
    }

    /**
     * 设置相机闪光状态
     * @param flash
     */
    fun setCameraFlash(context: Context, flash: Int) {
        SPUtils.put(context, CAMERA_FLASH, flash)
    }

    /**
     * 获取摄像头是否为前置或后
     *
     * @param context
     * @return 0为后置,1为前置
     */
    fun getCameraFacing(context: Context, defaultId: Int): Int {
        return SPUtils.get(context, CAMERA_AROUND, defaultId) as Int
    }

    /**
     * 设置摄像头前置或后
     *
     * @param context
     * @param around
     */
    fun setCameraFacing(context: Context, around: Int) {
        SPUtils.put(context, CAMERA_AROUND, around)
    }

    /**
     * 是否支持闪光
     * @param context
     * @return
     */
    fun isSupportFlashCamera(context: Context): Boolean {
        val features = context.packageManager.systemAvailableFeatures
        for (info in features) {
            if (PackageManager.FEATURE_CAMERA_FLASH == info.name)
                return true
        }
        return false
    }

}
