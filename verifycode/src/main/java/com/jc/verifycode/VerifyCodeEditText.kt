package com.jc.verifycode

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import java.util.*


/**
 * 自定义验证码输入框
 */
class VerifyCodeEditText @JvmOverloads constructor(
    context: Context,
    var attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {


    var mFigures = 0// 验证码个数
    var mCodeMargin = 0// 验证码之间的间距
    var mSelectColor = 0// 选中框的颜色
    var mNormalColor = 0// 普通框的颜色
    var mBorderRadius = 0f// 边框直角的曲度
    var mBorderWidth = 0f// 边框的厚度
    var mCursorWidth = 0f// 光标宽度
    var mCursorColor = 0// 光标的颜色
    var mCursorDuration = 0L// 光标闪烁的时间


    var onVerifyCodeChangedListener: OnVerifyCodeChangedListener? = null
    var mCurrentPosition = 0// 当前验证码的位置
    var mEachRectLength = 0// 矩形边长
    val mNormalPaint = Paint()
    val mSelectPaint = Paint()
    val mCursorPaint = Paint()

    // 控制光标闪烁
    var isCursorShowing = false
    var mCursorTimerTask: TimerTask? = null
    var mCursorTimer: Timer? = null

    init {
        initAttr()
        initPaint()
        initCursorTimer()
        isFocusableInTouchMode = true
        initTextChangedListener()
    }

    private fun initAttr() {
        val ta = context.obtainStyledAttributes(attrs!!, R.styleable.VerifyCodeEditText)
        mFigures = ta.getInteger(R.styleable.VerifyCodeEditText_figures, 6)
        mCodeMargin = ta.getDimension(R.styleable.VerifyCodeEditText_codeMargin, 0f).toInt()
        mSelectColor = ta.getColor(R.styleable.VerifyCodeEditText_selectBorderColor, currentTextColor)
        mNormalColor =
            ta.getColor(
                R.styleable.VerifyCodeEditText_normalBorderColor,
                resources.getColor(android.R.color.darker_gray)
            )
        mBorderRadius = ta.getDimension(R.styleable.VerifyCodeEditText_borderRadius, 6f)
        mBorderWidth = ta.getDimension(R.styleable.VerifyCodeEditText_borderWidth, 1f)
        mCursorWidth = ta.getDimension(R.styleable.VerifyCodeEditText_cursorWidth, 1f)
        mCursorColor =
            ta.getColor(
                R.styleable.VerifyCodeEditText_cursorColor,
                resources.getColor(android.R.color.darker_gray)
            )
        mCursorDuration =
            ta.getInteger(R.styleable.VerifyCodeEditText_cursorDuration, DEFAULT_CURSOR_DURATION)
                .toLong()

        ta.recycle()

        // force LTR because of bug: https://github.com/JustKiddingBaby/VercodeEditText/issues/4
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutDirection = LAYOUT_DIRECTION_LTR
        }
    }

    private fun initPaint() {
        mNormalPaint.isAntiAlias = true
        mNormalPaint.color = mNormalColor
        mNormalPaint.style = Paint.Style.STROKE// 空心
        mNormalPaint.strokeWidth = mBorderWidth

        mSelectPaint.isAntiAlias = true
        mSelectPaint.color = mSelectColor
        mSelectPaint.style = Paint.Style.STROKE// 空心
        mSelectPaint.strokeWidth = mBorderWidth

        mCursorPaint.isAntiAlias = true
        mCursorPaint.color = mCursorColor
        mCursorPaint.style = Paint.Style.FILL_AND_STROKE
        mCursorPaint.strokeWidth = mCursorWidth
    }

    private fun initCursorTimer() {
        mCursorTimerTask = object : TimerTask() {
            override fun run() {
                // 通过光标间歇性显示实现闪烁效果
                isCursorShowing = !isCursorShowing
                postInvalidate()
            }
        }
        mCursorTimer = Timer()
    }

    private fun initTextChangedListener() {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mCurrentPosition = text!!.length
                postInvalidate()
                if (text!!.length == mFigures) {
                    onVerifyCodeChangedListener?.onInputCompleted(text!!)
                } else if (text!!.length > mFigures) {
                    text!!.delete(mFigures, text!!.length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                mCurrentPosition = text!!.length
                postInvalidate()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mCurrentPosition = text!!.length
                postInvalidate()
                onVerifyCodeChangedListener?.onVerCodeChanged(text!!, start, before, count)
            }

        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthResult = 0
        var heightResult = 0

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            widthResult = widthSize
        } else {
            widthResult = getScreenWidth(context)
        }
        // 每个矩形的宽度
        mEachRectLength = (widthResult - (mFigures - 1) * mCodeMargin) / mFigures

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode == MeasureSpec.EXACTLY) {
            heightResult = heightSize
        } else {
            heightResult = mEachRectLength
        }

        setMeasuredDimension(widthResult, heightResult)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            if (action == MotionEvent.ACTION_DOWN) {
                requestFocus()
                setSelection(text!!.length)
                showKeyBoard(context)
                return false
            }
        }

        return super.onTouchEvent(event)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        mCurrentPosition = text!!.length
        val width = mEachRectLength - paddingLeft - paddingRight
        val height = measuredHeight - paddingTop - paddingBottom
        for (i in 0 until mFigures) {
            canvas.save()
            val start = width * i + mCodeMargin * i + mBorderWidth
            var end = start + width - mBorderWidth
            if (i == mFigures - 1) {
                end -= mBorderWidth
            }
            // 画矩形选框
            val rect = RectF(start, mBorderWidth, end, height.toFloat() - mBorderWidth)
            if (i == mCurrentPosition) {//选中的下一个状态
                canvas.drawRoundRect(rect, mBorderRadius, mBorderRadius, mSelectPaint)
            } else {
                canvas.drawRoundRect(rect, mBorderRadius, mBorderRadius, mNormalPaint)
            }
            canvas.restore()
        }

        // 绘制文字
        val value = text.toString()
        for (i in 0 until value.length) {
            canvas.save()
            val start = width * i + mCodeMargin * i
            val x = start + width / 2f// x
            paint.textAlign = Paint.Align.CENTER
            paint.color = currentTextColor
            val fontMetrics = paint.fontMetrics
            val baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top
            canvas.drawText(value[i].toString(), x, baseline, paint)
            canvas.restore()
        }

        // 绘制光标
        if (!isCursorShowing && isCursorVisible && mCurrentPosition < mFigures && hasFocus()) {
            canvas.save()
            val startX = (width + mCodeMargin) * mCurrentPosition + width / 2f
            val startY = height / 4f
            val endX = startX
            val endY = height - height / 4f
            canvas.drawLine(startX, startY, endX, endY, mCursorPaint)
            canvas.restore()
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 启动定时任务，定时刷新实现光标闪烁
        mCursorTimer?.scheduleAtFixedRate(mCursorTimerTask, 0, mCursorDuration)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mCursorTimer?.cancel()
        mCursorTimer = null
    }

    private fun getScreenWidth(context: Context?): Int {
        val metrics = DisplayMetrics()
        val wm = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    fun showKeyBoard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }

    companion object {
        val DEFAULT_CURSOR_DURATION = 400//光标闪烁的默认间隔时间
    }

    /**
     * 验证码变化时候的监听事件
     */
    interface OnVerifyCodeChangedListener {
        /**
         * 当验证码变化的时候
         */
        fun onVerCodeChanged(s: CharSequence, start: Int, before: Int, count: Int)

        /**
         * 输入完毕后的回调
         */
        fun onInputCompleted(s: CharSequence)
    }

}
