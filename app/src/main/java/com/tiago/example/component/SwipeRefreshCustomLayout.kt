package com.tiago.example.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import androidx.core.content.ContextCompat
import androidx.customview.widget.ViewDragHelper.INVALID_POINTER
import com.tiago.example.R
import com.tiago.example.util.Slingshot
import com.tiago.example.util.dpToPx
import com.tiago.example.util.onAnimationEnd

class SwipeRefreshCustomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyleAttr: Int = 0
) : ViewGroup(context, attrs, defaultStyleAttr) {

    companion object {
        private const val DRAG_RATE = 0.5f
        private const val DRAG_MAX_DISTANCE = 75
        private const val ANIMATION_DURATION = 700
        private const val LOADING_HEIGHT = 100
        private const val LOADING_WIDTH = 100
    }

    private var refreshListener: ISwipeRefreshCustomListener? = null
    private var targetView: View? = null
    private var loadingView: View? = null

    private var currentTopOffset: Int = 0

    private var totalDragDistance: Int = 0

    private var activePointerId: Int = 0

    private var initialMotionY: Int = 0

    private var touchSlop: Int = 0

    private var isRefreshing: Boolean = false
    private var isBeingDragged: Boolean = false
    private var isToNotify: Boolean = false

    private var animationFrom: Int = 0

    private val animationDefault = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val targetTop = animationFrom - (animationFrom * interpolatedTime).toInt()
            val offset = targetTop - targetView?.top!!

            setTargetOffsetTop(offset)
        }
    }

    private val animationExpanded = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            val targetTop = animationFrom + ((totalDragDistance - animationFrom) * interpolatedTime).toInt()
            val offset = targetTop - targetView?.top!!

            setTargetOffsetTop(offset)
        }
    }

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        totalDragDistance = dpToPx(DRAG_MAX_DISTANCE)

        addLoadingView()

        setWillNotDraw(false)
        setRefreshing(false)

        isChildrenDrawingOrderEnabled = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        ensureTarget()

        val centerWidth = measuredWidth / 2
        val centerHeight = totalDragDistance / 2

        val halfLoadingWidth = LOADING_WIDTH / 2
        val halfLoadingHeight = LOADING_HEIGHT / 2

        targetView?.layout(left, top, measuredWidth, measuredHeight)
        loadingView?.layout(
            centerWidth - halfLoadingWidth,
            centerHeight - halfLoadingHeight,
            centerWidth + halfLoadingWidth,
            centerHeight + halfLoadingHeight
        )
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        Log.d("SwipeTest", "onInterceptTouchEvent $event")

        val can = canChildScrollUp()
        Log.d("SwipeTest", "canChildScrollUp $can")
        if (!isEnabled || canChildScrollUp() || isRefreshing) {
            return false
        }

        when(event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                setTargetOffsetTop(0)
                activePointerId = event.getPointerId(0)
                isBeingDragged = false
                val motionY = getMotionEventY(event, activePointerId)
                if (motionY == -1) {
                    return false
                }
                initialMotionY = motionY
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }

                val motionY = getMotionEventY(event, activePointerId)
                if (motionY == -1) {
                    return false
                }

                val yDiff = motionY - initialMotionY
                if (yDiff > touchSlop && !isBeingDragged) {
                    isBeingDragged = true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isBeingDragged = false
                activePointerId = INVALID_POINTER
            }
        }

        return isBeingDragged
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("SwipeTest", "onTouchEvent $event")

        if (!isBeingDragged) {
            return super.onTouchEvent(event)
        }

        when(event?.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    return false
                }

                val pointerY = event.getY(pointerIndex)
                val targetY = Slingshot.calculateTargetY(
                    pointerY,
                    initialMotionY,
                    totalDragDistance,
                    DRAG_RATE
                )

                setTargetOffsetTop(targetY - currentTopOffset)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                activePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_UP -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }

                val pointerIndex = event.findPointerIndex(activePointerId)
                val overScrollTop = (event.getY(pointerIndex) - initialMotionY) * DRAG_RATE

                isBeingDragged = false

                if (overScrollTop > totalDragDistance) {
                    setRefreshing(refreshing = true, notify = true)
                } else {
                    isRefreshing = false
                    animateToDefaultPosition()
                }

                activePointerId = INVALID_POINTER
                return false
            }
        }

        return true
    }

    private fun addLoadingView() {
        loadingView = LayoutInflater
            .from(context)
            .inflate(R.layout.loading_item, this, false)

        addView(loadingView, 0)
    }

    private fun ensureTarget() {
        if (targetView == null && childCount > 0) {
            for (position in 0 until childCount) {
                val child = getChildAt(position)

                if (child != loadingView) {
                    targetView = child

                    if (targetView?.background == null) {
                        targetView?.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                }
            }
        }
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (isRefreshing != refreshing) {
            isRefreshing = refreshing
            isToNotify = notify

            ensureTarget()

            if (isRefreshing) {
                animateToExpandedPosition()
            } else {
                animateToDefaultPosition()
            }
        }
    }

    private fun canChildScrollUp(): Boolean {
        return targetView?.canScrollVertically(-1) ?: false
    }

    private fun setTargetOffsetTop(offset: Int) {
        targetView?.offsetTopAndBottom(offset)
        updateCurrentTopOffset()
    }

    private fun getMotionEventY(event: MotionEvent, activePointerId: Int): Int {
        val index = event.findPointerIndex(activePointerId)
        if (index < 0) {
            return -1
        }
        return event.getY(index).toInt()
    }

    private fun updateCurrentTopOffset() {
        currentTopOffset = targetView?.top!!
    }

    fun setListener(listener: ISwipeRefreshCustomListener) {
        refreshListener = listener
    }

    fun setRefreshing(refreshing: Boolean) {
        if (isRefreshing != refreshing) {
            setRefreshing(refreshing, false)
        }
    }

    private fun animateToDefaultPosition() {
        animationFrom = currentTopOffset

        animationDefault.reset()
        animationDefault.duration = ANIMATION_DURATION.toLong()
        animationDefault.interpolator = DecelerateInterpolator(2f)
        animationDefault.onAnimationEnd { updateCurrentTopOffset() }

        playAnimation(animationDefault)
    }

    private fun animateToExpandedPosition() {
        animationFrom = currentTopOffset

        animationExpanded.reset()
        animationExpanded.duration = ANIMATION_DURATION.toLong()
        animationExpanded.interpolator = DecelerateInterpolator(2f)

        playAnimation(animationExpanded)

        if (isRefreshing) {
            if (isToNotify) {
                refreshListener?.onRefresh()
            }
        } else {
            animateToDefaultPosition()
        }

        updateCurrentTopOffset()
    }

    private fun playAnimation(animation: Animation) {
        loadingView?.clearAnimation()
        loadingView?.startAnimation(animation)
    }

}
