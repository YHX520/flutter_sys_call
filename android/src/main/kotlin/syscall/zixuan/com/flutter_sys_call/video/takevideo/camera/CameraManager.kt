package syscall.zixuan.com.flutter_sys_call.video.takevideo.camera

import android.app.Application
import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.widget.Toast
import syscall.zixuan.com.flutter_sys_call.video.takevideo.utils.LogUtils


import java.util.ArrayList


/**
 * 相机管理类
 */

class CameraManager private constructor(private val context: Application) {
    /**
     * camera
     */
    private var mCamera: Camera? = null
    /**
     * 视频录制
     */
    private var mMediaRecorder: MediaRecorder? = null
    /**
     * 相机闪光状态
     */
    var cameraFlash: Int = 0

    /**
     * 前后置状态
     */
    private var cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
    /**
     * 是否支持前置摄像,是否支持闪光
     */
    val isSupportFrontCamera: Boolean

    val isSupportFlashCamera: Boolean

    ///是否已经拍摄到相应的照片或视频
    var isTakePhotoOrVideo = false;
    /**
     * 录制视频的相关参数
     */
    private var mProfile: CamcorderProfile? = null
    /**
     * 0为拍照, 1为录像
     */
    private var cameraType: Int = 0

    val isCameraFrontFacing: Boolean
        get() = cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT

    init {
        isSupportFrontCamera = CameraUtils.isSupportFrontCamera
        isSupportFlashCamera = CameraUtils.isSupportFlashCamera(context)
        if (isSupportFrontCamera) {

            cameraFacing = CameraUtils.getCameraFacing(context, Camera.CameraInfo.CAMERA_FACING_BACK)
        }
        if (isSupportFlashCamera) {
            cameraFlash = CameraUtils.getCameraFlash(context)
        }
    }

    /**
     * 打开camera
     */
    fun openCamera(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraFacing)//打开当前选中的摄像头
                mProfile = CamcorderProfile.get(cameraFacing, CamcorderProfile.QUALITY_HIGH)
                mCamera!!.setDisplayOrientation(90)//默认竖直拍照
                mCamera!!.setPreviewTexture(surfaceTexture)
                initCameraParameters(cameraFacing, width, height)
                mCamera!!.startPreview()
            } catch (e: Exception) {
                if (mCamera != null) {
                    mCamera!!.release()
                    mCamera = null
                }
            }

        }
    }


    /**
     * 开启预览,前提是camera初始化了
     */
    fun restartPreview() {
        if (mCamera == null) return
        try {
            val parameters = mCamera!!.parameters
            val zoom = parameters.zoom
            if (zoom > 0) {
                parameters.zoom = 0
                mCamera!!.parameters = parameters
            }
            mCamera!!.startPreview()
        } catch (e: Exception) {
            LogUtils.i(e)
            if (mCamera != null) {
                mCamera!!.release()
                mCamera = null
            }
        }

    }

    private fun initCameraParameters(cameraId: Int, width: Int, height: Int) {
        val parameters = mCamera!!.parameters
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            val focusModes = parameters.supportedFocusModes
            if (focusModes != null) {
                if (cameraType == 0) {
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    }
                } else {
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                    }
                }
            }
        }
        parameters.setRotation(90)//设置旋转代码,
        when (cameraFlash) {
            0 -> parameters.flashMode = Camera.Parameters.FLASH_MODE_AUTO
            1 -> parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            2 -> parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
        }
        val pictureSizes = parameters.supportedPictureSizes
        val previewSizes = parameters.supportedPreviewSizes
        if (!isEmpty<Camera.Size>(pictureSizes) && !isEmpty<Camera.Size>(previewSizes)) {
            /*for (Camera.Size size : pictureSizes) {
                LogUtils.i("pictureSize " + size.width + "  " + size.height);
            }
            for (Camera.Size size : pictureSizes) {
                LogUtils.i("previewSize " + size.width + "  " + size.height);
            }*/
            val optimalPicSize = getOptimalSize(pictureSizes, width, height)
            val optimalPreSize = getOptimalSize(previewSizes, width, height)
            LogUtils.i("TextureSize " + width + " " + height + " optimalSize pic " + optimalPicSize!!.width + " " + optimalPicSize.height + " pre " + optimalPreSize!!.width + " " + optimalPreSize.height)
            parameters.setPictureSize(optimalPicSize.width, optimalPicSize.height)
            parameters.setPreviewSize(optimalPreSize.width, optimalPreSize.height)
            mProfile!!.videoFrameWidth = optimalPreSize.width
            mProfile!!.videoFrameHeight = optimalPreSize.height
            mProfile!!.videoBitRate = 5000000//此参数主要决定视频拍出大小
        }
        mCamera!!.parameters = parameters
    }

    /**
     * 释放摄像头
     */
    fun closeCamera() {
        this.cameraType = 0
        if (mCamera != null) {
            try {
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            } catch (e: Exception) {
                LogUtils.i(e)
                if (mCamera != null) {
                    mCamera!!.release()
                    mCamera = null
                }
            }

        }
    }

    /**
     * 集合不为空
     *
     * @param list
     * @param <E>
     * @return
    </E> */
    private fun <E> isEmpty(list: List<E>?): Boolean {
        return list == null || list.isEmpty()
    }

    /**
     * 获取最佳预览相机Size参数
     *
     * @return
     */
    private fun getOptimalSize(sizes: List<Camera.Size>, w: Int, h: Int): Camera.Size? {
        var optimalSize: Camera.Size? = null
        val targetRadio = h / w.toFloat()
        var optimalDif = java.lang.Float.MAX_VALUE //最匹配的比例
        var optimalMaxDif = Integer.MAX_VALUE//最优的最大值差距
        for (size in sizes) {
            val newOptimal = size.width / size.height.toFloat()
            val newDiff = Math.abs(newOptimal - targetRadio)
            if (newDiff < optimalDif) { //更好的尺寸
                optimalDif = newDiff
                optimalSize = size
                optimalMaxDif = Math.abs(h - size.width)
            } else if (newDiff == optimalDif) {//更好的尺寸
                val newOptimalMaxDif = Math.abs(h - size.width)
                if (newOptimalMaxDif < optimalMaxDif) {
                    optimalDif = newDiff
                    optimalSize = size
                    optimalMaxDif = newOptimalMaxDif
                }
            }
        }
        return optimalSize
    }

    /**
     * 缩放
     *
     * @param isZoomIn
     */
    fun handleZoom(isZoomIn: Boolean) {
        if (mCamera == null) return
        val params = mCamera!!.parameters ?: return
        if (params.isZoomSupported) {
            val maxZoom = params.maxZoom
            var zoom = params.zoom
            if (isZoomIn && zoom < maxZoom) {
                zoom++
            } else if (zoom > 0) {
                zoom--
            }
            params.zoom = zoom
            mCamera!!.parameters = params
        } else {
            LogUtils.i("zoom not supported")
        }
    }

    /**
     * 更换前后置摄像
     */
    fun changeCameraFacing(context: Context, surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        if (isSupportFrontCamera) {
            val cameraInfo = Camera.CameraInfo()
            val cameraCount = Camera.getNumberOfCameras()//得到摄像头的个数
            for (i in 0 until cameraCount) {
                Camera.getCameraInfo(i, cameraInfo)//得到每一个摄像头的信息
                if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) { //现在是后置，变更为前置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位为前置
                        closeCamera()
                        cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
                        CameraUtils.setCameraFacing(context, cameraFacing)
                        openCamera(context, surfaceTexture, width, height)
                        break
                    }
                } else {//现在是前置， 变更为后置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位
                        closeCamera()
                        cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT
                        CameraUtils.setCameraFacing(context, cameraFacing)
                        openCamera(context, surfaceTexture, width, height)
                        break
                    }
                }
            }
        } else { //不支持摄像机
            Toast.makeText(context, "您的手机不支持前置摄像", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 改变闪光状态
     */
    fun changeCameraFlash(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        if (!isSupportFlashCamera) {
            Toast.makeText(context, "您的手机不支闪光", Toast.LENGTH_SHORT).show()
            return
        }
        if (mCamera != null) {
            val parameters = mCamera!!.parameters
            if (parameters != null) {
                var newState = cameraFlash
                when (cameraFlash) {
                    0 //自动
                    -> {
                        parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                        newState = 1
                    }
                    1//open
                    -> {
                        parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                        newState = 2
                    }
                    2 //close
                    -> {
                        parameters.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                        newState = 0
                    }
                }
                cameraFlash = newState
                CameraUtils.setCameraFlash(context, newState)
                mCamera!!.parameters = parameters
            }
        }
    }

    /**
     * 拍照
     */
    fun takePhoto(callback: Camera.PictureCallback) {
        if (mCamera != null) {
            try {
                isTakePhotoOrVideo = true;
                mCamera!!.takePicture(null, null, callback)

            } catch (e: Exception) {
                Toast.makeText(context, "拍摄失败", Toast.LENGTH_SHORT).show()
            }

        }
    }


    /**
     * 开始录制视频
     */
    fun startMediaRecord(savePath: String) {

        if (mCamera == null || mProfile == null) return
        mCamera!!.unlock()
        if (mMediaRecorder == null) {
            mMediaRecorder = MediaRecorder()
            mMediaRecorder!!.setOrientationHint(90)
        }
        if (isCameraFrontFacing) {
            mMediaRecorder!!.setOrientationHint(270)
        }
        mMediaRecorder!!.reset()
        mMediaRecorder!!.setCamera(mCamera)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mMediaRecorder!!.setProfile(mProfile)
        mMediaRecorder!!.setOutputFile(savePath)
        try {
            mMediaRecorder!!.prepare()
            mMediaRecorder!!.start()
        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    /**
     * 停止录制
     */
    fun stopMediaRecord() {
        this.cameraType = 0
        stopRecorder()
        releaseMediaRecorder()
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder!!.reset()
                mMediaRecorder!!.release()
                mMediaRecorder = null
                mCamera!!.lock()
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.i(e)
            }

        }
    }

    private fun stopRecorder() {
        isTakePhotoOrVideo=true;
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder!!.stop()
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.i(e)
            }

        }
    }

    /**
     * 设置对焦类型
     *
     * @param cameraType
     */
    fun setCameraType(cameraType: Int) {
        this.cameraType = cameraType
        if (mCamera != null) {//拍摄视频时
            if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                val parameters = mCamera!!.parameters
                val focusModes = parameters.supportedFocusModes
                if (focusModes != null) {
                    if (cameraType == 0) {
                        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                        }
                    } else {
                        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                        }
                    }
                }
            }
        }
    }

    /**
     * 对焦
     *
     * @param x
     * @param y
     */
    fun handleFocusMetering(x: Float, y: Float) {


        val params = mCamera!!.parameters
        val previewSize = params.previewSize
        val focusRect = calculateTapArea(x, y, 1f, previewSize)
        val meteringRect = calculateTapArea(x, y, 1.5f, previewSize)
        mCamera!!.cancelAutoFocus()

        if (params.maxNumFocusAreas > 0) {
            val focusAreas = ArrayList<Camera.Area>()
            focusAreas.add(Camera.Area(focusRect, 1000))
            params.focusAreas = focusAreas
        } else {
            LogUtils.i("focus areas not supported")
        }
        if (params.maxNumMeteringAreas > 0) {
            val meteringAreas = ArrayList<Camera.Area>()
            meteringAreas.add(Camera.Area(meteringRect, 1000))
            params.meteringAreas = meteringAreas
        } else {
            LogUtils.i("metering areas not supported")
        }
        val currentFocusMode = params.focusMode
        params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        mCamera!!.parameters = params

        mCamera!!.autoFocus { success, camera ->
            val params = camera.parameters
            params.focusMode = currentFocusMode
            camera.parameters = params
        }
    }

    private fun calculateTapArea(x: Float, y: Float, coefficient: Float, previewSize: Camera.Size): Rect {
        val focusAreaSize = 300f
        val areaSize = java.lang.Float.valueOf(focusAreaSize * coefficient).toInt()
        val centerX = (x / previewSize.width - 1000).toInt()
        val centerY = (y / previewSize.height - 1000).toInt()
        val left = clamp(centerX - areaSize / 2, -1000, 1000)
        val top = clamp(centerY - areaSize / 2, -1000, 1000)
        val rectF = RectF(left.toFloat(), top.toFloat(), (left + areaSize).toFloat(), (top + areaSize).toFloat())
        return Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom))
    }

    private fun clamp(x: Int, min: Int, max: Int): Int {
        if (x > max) {
            return max
        }
        return if (x < min) {
            min
        } else x
    }

    companion object {

        private var INSTANCE: CameraManager? = null

        fun getInstance(context: Application): CameraManager {
            if (INSTANCE == null) {
                synchronized(CameraManager::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = CameraManager(context)
                    }
                }
            }
            return INSTANCE!!
        }
    }

}
