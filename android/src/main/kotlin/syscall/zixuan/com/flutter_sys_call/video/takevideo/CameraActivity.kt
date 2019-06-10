package syscall.zixuan.com.flutter_sys_call.video.takevideo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import syscall.zixuan.com.flutter_sys_call.R
import syscall.zixuan.com.flutter_sys_call.video.takevideo.camera.CameraManager
import syscall.zixuan.com.flutter_sys_call.video.takevideo.camera.CameraProgressBar
import syscall.zixuan.com.flutter_sys_call.video.takevideo.camera.CameraView
import syscall.zixuan.com.flutter_sys_call.video.takevideo.camera.MediaPlayerManager
import syscall.zixuan.com.flutter_sys_call.video.takevideo.utils.FileUtils
import syscall.zixuan.com.flutter_sys_call.video.takevideo.utils.RequestCode

import java.io.File
import java.util.concurrent.TimeUnit


class CameraActivity : AppCompatActivity(), View.OnClickListener {

    private var mContext: Context? = null
    /**
     * TextureView
     */
    private var mTextureView: TextureView? = null
    /**
     * 带手势识别
     */
    private var mCameraView: CameraView? = null
    /**
     * 录制按钮
     */
    private var mProgressbar: CameraProgressBar? = null
    /**
     * 顶部像机设置
     */
    private var rl_camera: RelativeLayout? = null
    /**
     * 关闭,选择,前后置
     */
    private var iv_close: ImageView? = null
    private var iv_choice: ImageView? = null
    private var iv_facing: ImageView? = null
    /**
     * 闪光
     */
    private var tv_flash: TextView? = null
    /**
     * camera manager
     */
    private var cameraManager: CameraManager? = null
    /**
     * player manager
     */
    private var playerManager: MediaPlayerManager? = null
    /**
     * true代表视频录制,否则拍照
     */
    private var isSupportRecord: Boolean = false
    /**
     * 视频录制地址
     */
    private var recorderPath: String? = null
    /**
     * 录制视频的时间,毫秒
     */
    private var recordSecond: Int = 0
    /**
     * 获取照片订阅, 进度订阅
     */
    private var takePhotoSubscription: Subscription? = null
    private var progressSubscription: Subscription? = null
    /**
     * 是否正在录制
     */
    private var isRecording: Boolean = false

    /**
     * 是否为点了拍摄状态(没有拍照预览的状态)
     */
    private var isPhotoTakingState: Boolean = false
    private var mTv_tack: TextView? = null

    /**
     * camera回调监听
     */
    private val listener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            if (recorderPath != null) {
                iv_choice!!.visibility = View.VISIBLE
                setTakeButtonShow(false)
                playerManager!!.playMedia(Surface(texture), recorderPath!!)
            } else {
                setTakeButtonShow(true)
                iv_choice!!.visibility = View.GONE
                cameraManager!!.openCamera(this@CameraActivity, texture, width, height)
            }
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }
    private var photo: String? = null

    private val callback = Camera.PictureCallback { data, camera ->
        setTakeButtonShow(false)
        takePhotoSubscription = Observable.create(object : Observable.OnSubscribe<Boolean> {
            override fun call(subscriber: Subscriber<in Boolean>) {
                if (!subscriber.isUnsubscribed()) {
                    val photoPath = FileUtils.getUploadPhotoFile(this@CameraActivity)
                    //保存拍摄的图片
                    isPhotoTakingState = FileUtils.savePhoto(photoPath, data, cameraManager!!.isCameraFrontFacing)
                    if (isPhotoTakingState) {
                        photo = photoPath
                    }
                    subscriber.onNext(isPhotoTakingState)
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Boolean>() {
                override fun onCompleted() {

                }

                override fun onError(e: Throwable) {

                }

                override fun onNext(aBoolean: Boolean?) {
                    if (aBoolean != null && aBoolean) {
                        iv_choice!!.visibility = View.VISIBLE
                    } else {
                        setTakeButtonShow(true)
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_camera)
        initView()
        initDatas()
    }


    private fun initView() {
        mTextureView = findViewById<View>(R.id.mTextureView) as TextureView
        mCameraView = findViewById<View>(R.id.mCameraView) as CameraView
        mProgressbar = findViewById<View>(R.id.mProgressbar) as CameraProgressBar
        rl_camera = findViewById<View>(R.id.rl_camera) as RelativeLayout
        iv_close = findViewById<View>(R.id.iv_close) as ImageView
        iv_choice = findViewById<View>(R.id.iv_choice) as ImageView
        iv_facing = findViewById<View>(R.id.iv_facing) as ImageView
        tv_flash = findViewById<View>(R.id.tv_flash) as TextView
        mTv_tack = findViewById<View>(R.id.tv_tack) as TextView
        iv_close!!.setOnClickListener(this)
        iv_choice!!.setOnClickListener(this)
        iv_facing!!.setOnClickListener(this)
        tv_flash!!.setOnClickListener(this)
    }

    protected fun initDatas() {
        cameraManager = CameraManager.getInstance(application)
        playerManager = MediaPlayerManager.getInstance(application)
        cameraManager!!.setCameraType(if (isSupportRecord) 1 else 0)

       // tv_flash!!.visibility = if (cameraManager!!.isSupportFlashCamera) View.VISIBLE else View.GONE
       // setCameraFlashState()
       // iv_facing!!.visibility = if (cameraManager!!.isSupportFrontCamera) View.VISIBLE else View.GONE
        //rl_camera!!.visibility = if (cameraManager!!.isSupportFlashCamera || cameraManager!!.isSupportFrontCamera)
       //     View.VISIBLE
       // else
       //     View.GONE

        val max = MAX_RECORD_TIME / PLUSH_PROGRESS
        mProgressbar!!.setMaxProgress(max)

        mProgressbar!!.setOnProgressTouchListener(object : CameraProgressBar.OnProgressTouchListener {
            override fun onClick(progressBar: CameraProgressBar) {
                mTv_tack!!.visibility = View.GONE
                cameraManager!!.takePhoto(callback)
            }

            override fun onLongClick(progressBar: CameraProgressBar) {
                mTv_tack!!.visibility = View.GONE
                isSupportRecord = true
                cameraManager!!.setCameraType(1)
                rl_camera!!.visibility = View.GONE
                recorderPath = FileUtils.getUploadVideoFile(this@CameraActivity)
                cameraManager!!.startMediaRecord(recorderPath!!)
                isRecording = true
                progressSubscription =
                        Observable.interval(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).take(max)
                            .subscribe(object : Subscriber<Long>() {
                                override fun onCompleted() {
                                    stopRecorder(true)
                                }

                                override fun onError(e: Throwable) {

                                }

                                override fun onNext(aLong: Long?) {
                                    mProgressbar!!.setProgress(mProgressbar!!.getProgress() + 1)
                                }
                            })
            }

            override fun onZoom(zoom: Boolean) {
                cameraManager!!.handleZoom(zoom)
            }

            override fun onLongClickUp(progressBar: CameraProgressBar) {
                isSupportRecord = false
                cameraManager!!.setCameraType(0)
                stopRecorder(true)
                if (progressSubscription != null) {
                    progressSubscription!!.unsubscribe()
                }
            }

            override fun onPointerDown(rawX: Float, rawY: Float) {
                if (mTextureView != null) {
                    mCameraView!!.setFoucsPoint(PointF(rawX, rawY))
                }
            }
        })

        mCameraView!!.setOnViewTouchListener(object : CameraView.OnViewTouchListener {
            override fun handleFocus(x: Float, y: Float) {
                cameraManager!!.handleFocusMetering(x, y)
            }

            override fun handleZoom(zoom: Boolean) {
                cameraManager!!.handleZoom(zoom)
            }
        })
    }

    /**
     * 设置闪光状态
     */
    private fun setCameraFlashState() {
        val flashState = cameraManager!!.cameraFlash
        when (flashState) {
            0 //自动
            -> {
                tv_flash!!.isSelected = true
                tv_flash!!.text = "自动"
            }
            1//open
            -> {
                tv_flash!!.isSelected = true
                tv_flash!!.text = "开启"
            }
            2 //close
            -> {
                tv_flash!!.isSelected = false
                tv_flash!!.text = "关闭"
            }
        }
    }

    /**
     * 是否显示录制按钮
     *
     * @param isShow
     */
    private fun setTakeButtonShow(isShow: Boolean) {
        if (isShow) {
            mProgressbar!!.setVisibility(View.VISIBLE)
            rl_camera!!.visibility =
                    if (cameraManager!!.isSupportFlashCamera or cameraManager!!.isSupportFrontCamera)
                        View.VISIBLE
                    else
                        View.GONE
        } else {
            mProgressbar!!.setVisibility(View.GONE)
            rl_camera!!.visibility = View.GONE
        }
    }

    /**
     * 停止拍摄
     */
    private fun stopRecorder(play: Boolean) {
        isRecording = false
        cameraManager!!.stopMediaRecord()
        recordSecond = mProgressbar!!.getProgress() * PLUSH_PROGRESS//录制多少毫秒
        mProgressbar!!.reset()
        if (recordSecond < MIN_RECORD_TIME) {//小于最小录制时间作废
            if (recorderPath != null) {
                FileUtils.delteFiles(File(recorderPath))
                recorderPath = null
                recordSecond = 0
            }
            setTakeButtonShow(true)
        } else if (play && mTextureView != null && mTextureView!!.isAvailable) {
            setTakeButtonShow(false)
            mProgressbar!!.setVisibility(View.GONE)
            iv_choice!!.visibility = View.VISIBLE
            cameraManager!!.closeCamera()
            playerManager!!.playMedia(Surface(mTextureView!!.surfaceTexture), recorderPath!!)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mTextureView!!.isAvailable) {
            if (recorderPath != null) {//优先播放视频
                iv_choice!!.visibility = View.VISIBLE
                setTakeButtonShow(false)
                playerManager!!.playMedia(Surface(mTextureView!!.surfaceTexture), recorderPath!!)
            } else {
                iv_choice!!.visibility = View.GONE
                setTakeButtonShow(true)
                cameraManager!!.openCamera(
                    this, mTextureView!!.surfaceTexture,
                    mTextureView!!.width, mTextureView!!.height
                )
            }
        } else {
            mTextureView!!.surfaceTextureListener = listener
        }
    }

    override fun onPause() {
        if (progressSubscription != null) {
            progressSubscription!!.unsubscribe()
        }
        if (takePhotoSubscription != null) {
            takePhotoSubscription!!.unsubscribe()
        }
        if (isRecording) {
            stopRecorder(false)
        }
        cameraManager!!.closeCamera()
        playerManager!!.stopMedia()
        super.onPause()
    }

    override fun onDestroy() {
        mCameraView!!.removeOnZoomListener()
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_close -> {
                mTv_tack!!.visibility = View.VISIBLE
                if (recorderPath != null) {//有拍摄好的正在播放,重新拍摄
                    FileUtils.delteFiles(File(recorderPath))
                    recorderPath = null
                    recordSecond = 0
                    playerManager!!.stopMedia()
                    setTakeButtonShow(true)
                    iv_choice!!.visibility = View.GONE
                    cameraManager!!.openCamera(
                        this,
                        mTextureView!!.surfaceTexture,
                        mTextureView!!.width,
                        mTextureView!!.height
                    )
                } else if (isPhotoTakingState) {
                    isPhotoTakingState = false
                    iv_choice!!.visibility = View.GONE
                    setTakeButtonShow(true)
                    cameraManager!!.restartPreview()
                } else {
                    finish()
                }
            }
            R.id.iv_choice//选择图片或视频
            -> {
                //将拍摄的视频路径回传
                if (recorderPath != null) {
                    val intent = Intent()
                    intent.putExtra("video", recorderPath)
                    setResult(RequestCode.TAKE_VIDEO, intent)
                }
                if (photo != null) {
                    backPicture()
                }
                finish()
            }
            R.id.tv_flash -> {
                cameraManager!!.changeCameraFlash(
                    mTextureView!!.surfaceTexture,
                    mTextureView!!.width, mTextureView!!.height
                )
                setCameraFlashState()
            }
            R.id.iv_facing -> cameraManager!!.changeCameraFacing(
                this, mTextureView!!.surfaceTexture,
                mTextureView!!.width, mTextureView!!.height
            )
        }
    }

    private fun backPicture() {
        //将图片路径intent回传
        val intent = Intent()
        intent.putExtra("photo", photo)
        setResult(RequestCode.TAKE_PHOTO, intent)
    }

    companion object {
        /**
         * 获取相册
         */
        val REQUEST_PHOTO = 1
        /**
         * 获取视频
         */
        val REQUEST_VIDEO = 2
        /**
         * 最小录制时间
         */
        private val MIN_RECORD_TIME = 1 * 1000
        /**
         * 最长录制时间
         */
        private val MAX_RECORD_TIME = 10 * 1000
        /**
         * 刷新进度的间隔时间
         */
        private val PLUSH_PROGRESS = 100

        fun lanuchForPhoto(context: Activity) {
            val intent = Intent(context, CameraActivity::class.java)
            context.startActivityForResult(intent, REQUEST_PHOTO)
        }
    }

}
