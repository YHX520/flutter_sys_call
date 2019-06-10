package syscall.zixuan.com.flutter_sys_call.video.takevideo.camera

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable

import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import syscall.zixuan.com.flutter_sys_call.R


class CameraProgressBar : View {
    /**
     * 默认缩小值
     */
    private var scale = DEF_SCALE

    /**
     * 内圆颜色
     */
    private var innerColor = Color.BLACK
    /**
     * 背景颜色
     */
    private val backgroundColor = Color.WHITE
    /**
     * 外圆颜色
     */
    private var outerColor = Color.parseColor("#e8e8e8")
    /**
     * 进度颜色
     */
    private var progressColor = Color.parseColor("#2DD0CF")
    /**
     * 进度宽
     */
    private var progressWidth = 10
    /**
     * 内圆宽度
     */
    private var innerRadio = 10
    /**
     * 进度
     */
    private var progress: Int = 0
    /**
     * 最大进度
     */
    private var maxProgress = 100
    /**
     * paint
     */
    private var backgroundPaint: Paint? = null
    private var progressPaint: Paint? = null
    private var innerPaint: Paint? = null
    /**
     * 圆的中心坐标点, 进度百分比
     */
    private var sweepAngle: Float = 0.toFloat()
    /**
     * 手识识别
     */
    private var mDetector: GestureDetectorCompat? = null
    /**
     * 是否为长按录制
     */
    private var isLongClick: Boolean = false
    /**
     * 是否产生滑动
     */
    private var isBeingDrag: Boolean = false
    /**
     * 滑动单位
     */
    private var mTouchSlop: Int = 0
    /**
     * 记录上一次Y轴坐标点
     */
    private var mLastY: Float = 0.toFloat()
    /**
     * 是否长按放大
     */
    private var isLongScale: Boolean = false

    private var listener: OnProgressTouchListener? = null


    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CameraProgressBar)
            innerColor = a.getColor(R.styleable.CameraProgressBar_innerColor, innerColor)
            outerColor = a.getColor(R.styleable.CameraProgressBar_outerColor, outerColor)
            progressColor = a.getColor(R.styleable.CameraProgressBar_progressColor, progressColor)
            innerRadio = a.getDimensionPixelOffset(R.styleable.CameraProgressBar_innerRadio, innerRadio)
            progressWidth = a.getDimensionPixelOffset(R.styleable.CameraProgressBar_progressWidth, progressWidth)
            progress = a.getInt(R.styleable.CameraProgressBar_progres, progress)
            scale = a.getFloat(R.styleable.CameraProgressBar_scale, scale)
            isLongScale = a.getBoolean(R.styleable.CameraProgressBar_isLongScale, isLongScale)
            maxProgress = a.getInt(R.styleable.CameraProgressBar_maxProgress, maxProgress)
            a.recycle()
        }
        backgroundPaint = Paint()
        backgroundPaint!!.isAntiAlias = true
        backgroundPaint!!.color = backgroundColor

        progressPaint = Paint()
        progressPaint!!.isAntiAlias = true
        progressPaint!!.strokeWidth = progressWidth.toFloat()
        progressPaint!!.style = Paint.Style.STROKE

        innerPaint = Paint()
        innerPaint!!.isAntiAlias = true
        innerPaint!!.strokeWidth = innerRadio.toFloat()
        innerPaint!!.style = Paint.Style.STROKE

        sweepAngle = progress.toFloat() / maxProgress * 360

        mDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                isLongClick = false
                if (this@CameraProgressBar.listener != null) {
                    this@CameraProgressBar.listener!!.onClick(this@CameraProgressBar)
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent) {
                isLongClick = true
                postInvalidate()
                mLastY = e.y
                if (this@CameraProgressBar.listener != null) {
                    this@CameraProgressBar.listener!!.onLongClick(this@CameraProgressBar)
                }
            }
        })
        mDetector!!.setIsLongpressEnabled(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (width > height) {
            setMeasuredDimension(height, height)
        } else {
            setMeasuredDimension(width, width)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val circle = width / 2.0f

        if (/*isLongScale && */!isLongClick) {
            canvas.scale(scale, scale, circle, circle)
        }


        //画内圆
        val backgroundRadio = circle - progressWidth.toFloat() - innerRadio.toFloat()
        canvas.drawCircle(circle, circle, backgroundRadio, backgroundPaint!!)

        //画内外环
        val halfInnerWidth = innerRadio / 2.0f + progressWidth
        val innerRectF = RectF(halfInnerWidth, halfInnerWidth, width - halfInnerWidth, width - halfInnerWidth)
        canvas.drawArc(innerRectF, -90f, 360f, true, innerPaint!!)

        progressPaint!!.color = outerColor
        val halfOuterWidth = progressWidth / 2.0f
        val outerRectF = RectF(halfOuterWidth, halfOuterWidth, getWidth() - halfOuterWidth, getWidth() - halfOuterWidth)
        canvas.drawArc(outerRectF, -90f, 360f, true, progressPaint!!)

        progressPaint!!.color = progressColor
        canvas.drawArc(outerRectF, -90f, sweepAngle, false, progressPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isLongScale) {
            return super.onTouchEvent(event)
        }
        this.mDetector!!.onTouchEvent(event)
        when (MotionEventCompat.getActionMasked(event)) {
            MotionEvent.ACTION_DOWN -> {
                isLongClick = false
                isBeingDrag = false
            }
            MotionEvent.ACTION_MOVE -> if (isLongClick) {
                val y = event.y
                if (isBeingDrag) {
                    val isUpScroll = y < mLastY
                    mLastY = y
                    if (this.listener != null) {
                        this.listener!!.onZoom(isUpScroll)
                    }
                } else {
                    isBeingDrag = Math.abs(y - mLastY) > mTouchSlop
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isBeingDrag = false
                if (isLongClick) {
                    isLongClick = false
                    postInvalidate()
                    if (this.listener != null) {
                        this.listener!!.onLongClickUp(this)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> if (isLongClick) {
                if (this.listener != null) {
                    this.listener!!.onPointerDown(event.rawX, event.rawY)
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        val superData = super.onSaveInstanceState()
        bundle.putParcelable("superData", superData)
        bundle.putInt("progress", progress)
        bundle.putInt("maxProgress", maxProgress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        val superData = bundle.getParcelable<Parcelable>("superData")
        progress = bundle.getInt("progress")
        maxProgress = bundle.getInt("maxProgress")
        super.onRestoreInstanceState(superData)
    }

    /**
     * 设置进度
     * @param progress
     */
    fun setProgress(progress: Int) {
        var progress = progress
        if (progress <= 0) progress = 0
        if (progress >= maxProgress) progress = maxProgress
        if (progress == this.progress) return
        this.progress = progress
        this.sweepAngle = progress.toFloat() / maxProgress * 360
        postInvalidate()
    }

    /**
     * 还原到初始状态
     */
    fun reset() {
        isLongClick = false
        this.progress = 0
        this.sweepAngle = 0f
        postInvalidate()
    }

    fun getProgress(): Int {
        return progress
    }

    fun setLongScale(longScale: Boolean) {
        isLongScale = longScale
    }

    fun setMaxProgress(maxProgress: Int) {
        this.maxProgress = maxProgress
    }

    fun setOnProgressTouchListener(listener: OnProgressTouchListener) {
        this.listener = listener
    }

    /**
     * 进度触摸监听
     */
    interface OnProgressTouchListener {
        /**
         * 单击
         * @param progressBar
         */
        fun onClick(progressBar: CameraProgressBar)

        /**
         * 长按
         * @param progressBar
         */
        fun onLongClick(progressBar: CameraProgressBar)

        /**
         * 移动
         * @param zoom true放大
         */
        fun onZoom(zoom: Boolean)

        /**
         * 长按抬起
         * @param progressBar
         */
        fun onLongClickUp(progressBar: CameraProgressBar)

        /**
         * 触摸对焦
         * @param rawX
         * @param rawY
         */

        fun onPointerDown(rawX: Float, rawY: Float)
    }

    companion object {
        /**
         * 默认缩小值
         */
        val DEF_SCALE = 0.75f
    }

}
