package syscall.zixuan.com.flutter_sys_call.video.takevideo.camera
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import androidx.core.view.MotionEventCompat
import rx.Observable
import rx.Subscriber
import rx.Subscription


import java.util.concurrent.TimeUnit


class CameraView : View {
    /**
     * focus paint
     */
    private var paint: Paint? = null
    private var clearPaint: Paint? = null

    private val paintColor = Color.GREEN
    /**
     * 进度订阅
     */
    private var subscription: Subscription? = null
    /**
     * focus rectf
     */
    private val rectF = RectF()
    /**
     * focus size
     */
    private val focusSize = 120

    private val lineSize = focusSize / 4
    /**
     * 上一次两指距离
     */
    private var oldDist = 1f
    /**
     * 画笔宽
     */
    private val paintWidth = 6.0f
    /**
     * s
     */
    private var scale: Float = 0.toFloat()

    private var listener: OnViewTouchListener? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint = Paint()
        paint!!.color = paintColor
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = paintWidth

        clearPaint = Paint()
        clearPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        if (event.pointerCount == 1 && action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            setFoucsPoint(x, y)
            if (listener != null) {
                listener!!.handleFocus(x, y)
            }
        } else if (event.pointerCount >= 2) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> oldDist = getFingerSpacing(event)
                MotionEvent.ACTION_MOVE -> {
                    val newDist = getFingerSpacing(event)
                    if (newDist > oldDist) {
                        if (this.listener != null) {
                            this.listener!!.handleZoom(true)
                        }
                    } else if (newDist < oldDist) {
                        if (this.listener != null) {
                            this.listener!!.handleZoom(false)
                        }
                    }
                    oldDist = newDist
                }
            }
        }
        return true
    }

    /**
     * 计算两点触控距离
     * @param event
     * @return
     */
    private fun getFingerSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * 设置坐标点(坐标为rawX, rawY)
     */
    fun setFoucsPoint(pointF: PointF) {
        val transPointF = transPointF(pointF, this)
        setFoucsPoint(transPointF.x, transPointF.y)
    }

    /**
     * 设置当前触摸点
     * @param x
     * @param y
     */
    private fun setFoucsPoint(x: Float, y: Float) {
        if (subscription != null) {
            subscription!!.unsubscribe()
        }
        rectF.set(x - focusSize, y - focusSize, x + focusSize, y + focusSize)
        val count = ANIM_MILS / ANIM_UPDATE
        subscription = Observable.interval(ANIM_UPDATE.toLong(), TimeUnit.MILLISECONDS).take(count)
            .subscribe(object : Subscriber<Long>() {
                override fun onCompleted() {
                    scale = 0f
                    postInvalidate()
                }

                override fun onError(e: Throwable) {
                    scale = 0f
                    postInvalidate()
                }

                override fun onNext(aLong: Long?) {
                    val current = (aLong ?: 0).toFloat()
                    scale = 1 - current / count
                    if (scale <= 0.5f) {
                        scale = 0.5f
                    }
                    postInvalidate()
                }
            })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (scale != 0f) {
            val centerX = rectF.centerX()
            val centerY = rectF.centerY()
            canvas.scale(scale, scale, centerX, centerY)
            canvas.drawRect(rectF, paint!!)
            canvas.drawLine(rectF.left, centerY, rectF.left + lineSize, centerY, paint!!)
            canvas.drawLine(rectF.right, centerY, rectF.right - lineSize, centerY, paint!!)
            canvas.drawLine(centerX, rectF.top, centerX, rectF.top + lineSize, paint!!)
            canvas.drawLine(centerX, rectF.bottom, centerX, rectF.bottom - lineSize, paint!!)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (subscription != null) {
            subscription!!.unsubscribe()
        }
    }

    /**
     * 根据raw坐标转换成屏幕中所在的坐标
     * @param pointF
     * @return
     */
    private fun transPointF(pointF: PointF, view: View): PointF {
        pointF.x -= view.x
        pointF.y -= view.y
        val parent = view.parent
        return if (parent is View) {
            transPointF(pointF, parent as View)
        } else {
            pointF
        }
    }

    fun setOnViewTouchListener(listener: OnViewTouchListener) {
        this.listener = listener
    }

    fun removeOnZoomListener() {
        this.listener = null
    }

    interface OnViewTouchListener {
        /**
         * 对焦
         * @param x
         * @param y
         */
        fun handleFocus(x: Float, y: Float)

        /**
         * 缩放
         * @param zoom true放大反之
         */
        fun handleZoom(zoom: Boolean)

    }

    companion object {
        /**
         * 动画时长
         */
        private val ANIM_MILS = 600
        /**
         * 动画每多久刷新一次
         */
        private val ANIM_UPDATE = 30
    }

}
