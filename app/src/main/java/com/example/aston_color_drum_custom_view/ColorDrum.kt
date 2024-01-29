package com.example.aston_color_drum_custom_view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class ColorDrum(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    //////////////////////////////////////////////////////////////////////////////////////////
    // common
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    private val values by lazy { colors.values.toList() }
    private val boundsRect = Rect()
    private var minParentSize = 0

    private val inputTextDefValue = "SomeText"
    private val inoutImageDefValue =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_background, null)?.toBitmap()

    private val colors = mapOf(
        R.color.red to "red",
        R.color.orange to "https://placekitten.com/640/360",
        R.color.yellow to "yellow",
        R.color.green to "https://placebeard.it/640x360",
        R.color.ocean to "ocean",
        R.color.blue to "https://placebear.com/640/360",
        R.color.violet to "violet",
    )


    // for btn rotates
    private val btnRotatesY by lazy { offSetTopDrum + btnRotatesX }
    private val radiusBtnRotates by lazy { minParentSize / 10f }
    private val btnRotatesX by lazy { minParentSize / 2f }
    private var bntRotatesIsClicked = false
    private val painterBtnRotates = Paint()

    // for drum
    private val offSetTopDrum by lazy { offSetTopText * 3 }
    private val sweepAngle = 360f / colors.size
    private var drumSizePercent = 100f
    private var rotationPath = 0f
    private var isRotates = false

    private val painterDrum = Paint().apply {
        style = Paint.Style.FILL
    }

    // for inputText
    private val offSetTopText = 100f
    private var inputText = "Some Text"
    private val painterText = Paint().apply {
        style = Paint.Style.FILL
        textSize = 50f
    }

    // for inputImage
    private var inputImage: Bitmap? = null
    private val painterImage = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        minParentSize = width.coerceAtMost(height)
    }

    override fun onDraw(canvas: Canvas) {
        paintInputImage(canvas)
        paintInputText(canvas)
        paintDrum(canvas)
        paintBtnRotates(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val clickedX = event.x
        val clickedY = event.y

        return when {
            containsClickBtnRotates(event.action, clickedX, clickedY) -> true
            else -> false
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    fun updateDrumSize(newDrumSizePercent: Float) {
        drumSizePercent = newDrumSizePercent
        invalidate()
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    private fun paintBtnRotates(canvas: Canvas) {
        painterBtnRotates.apply {
            color = if (bntRotatesIsClicked) Color.DKGRAY else Color.GRAY
            style = Paint.Style.FILL
        }
        canvas.drawCircle(btnRotatesX, btnRotatesY, radiusBtnRotates, painterBtnRotates)

        painterBtnRotates.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = minParentSize / 100f
        }
        canvas.drawCircle(btnRotatesX, btnRotatesY, radiusBtnRotates, painterBtnRotates)

        val btnTitle = "GO"
        painterText.apply {
            getTextBounds(btnTitle, 0, btnTitle.length, boundsRect)
            color = Color.WHITE
        }

        canvas.drawText(
            btnTitle,
            btnRotatesX - boundsRect.exactCenterX(),
            btnRotatesY - boundsRect.exactCenterY(),
            painterText
        )

        canvas.drawLine(
            btnRotatesX + radiusBtnRotates,
            btnRotatesY,
            btnRotatesX + (radiusBtnRotates * 2),
            btnRotatesY,
            painterBtnRotates
        )
    }

    private fun paintInputImage(canvas: Canvas) {
        val checkedBitmap = inputImage ?: ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_launcher_background,
            null
        )?.toBitmap()
        ?: return

        val rect = Rect(width / 2, 0, width, (offSetTopText * 2).toInt())

        val reductionFactor = min(
            rect.width() / checkedBitmap.width.toFloat(),
            rect.height() / checkedBitmap.height.toFloat()
        )

        val newImageWidth = checkedBitmap.width * reductionFactor
        val newImageHeight = checkedBitmap.height * reductionFactor

        val startX = (width / 2) + (rect.width() / 2) - (newImageWidth / 2)
        val startY = (rect.height() / 2) - (newImageHeight / 2)

        canvas.drawBitmap(
            checkedBitmap,
            null,
            Rect(
                startX.toInt(),
                startY.toInt(),
                (startX + newImageWidth).toInt(),
                (startY + newImageHeight).toInt()
            ),
            painterImage
        )
    }

    private fun paintInputText(canvas: Canvas) {
        painterText.apply {
            getTextBounds(inputText, 0, inputText.length, boundsRect)
            color = Color.BLUE
        }

        canvas.drawText(
            inputText,
            (minParentSize / 4f) - boundsRect.exactCenterX(),
            offSetTopText,
            painterText
        )
    }

    private fun paintDrum(canvas: Canvas) {
        val offSetOnChangeSize = ((minParentSize - (minParentSize / 100 * drumSizePercent)) / 2)
        val left = 0f + offSetOnChangeSize
        val top = offSetTopDrum + offSetOnChangeSize
        val right = minParentSize - offSetOnChangeSize
        val bottom = minParentSize - offSetOnChangeSize + offSetTopDrum

        colors.keys.forEachIndexed { index: Int, color: Int ->
            painterDrum.color = ResourcesCompat.getColor(resources, color, null)

            canvas.drawArc(
                left, top, right, bottom,
                sweepAngle * index + rotationPath,
                sweepAngle,
                true,
                painterDrum
            )
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    private fun containsClickBtnRotates(action: Int, clickedX: Float, clickedY: Float): Boolean {
        if (sqrt((clickedX - btnRotatesX).pow(2) + (clickedY - btnRotatesY).pow(2)) > radiusBtnRotates) return false
        bntRotatesIsClicked = action == MotionEvent.ACTION_DOWN
        invalidate()
        if (!isRotates) rotateDrum()
        return true
    }

    private fun rotateDrum() {
        coroutineScope.launch {
            isRotates = true
            for (step in Random.nextInt(30, 50) downTo 0) {
                val startTimeRotate = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTimeRotate < 100) {

                    delay(16)
                    invalidate()
                    rotationPath += step
                }
            }
            isRotates = false
            rotationPath %= 360
            val indexWins = ((360 - rotationPath) / sweepAngle).toInt()

            defineActions(indexWins)
        }
    }

    private fun defineActions(indexWins: Int) {
        if (indexWins % 2 == 0) inputText = values[indexWins]
        else loadNewImageBitmap(values[indexWins])
        invalidate()
    }

    fun reset() {
        rotationPath = 0f
        inputText = inputTextDefValue
        inputImage = inoutImageDefValue
        invalidate()
    }

    private fun loadNewImageBitmap(uri: String) {
        coroutineScope.launch {
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .addListener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        inputImage = resource
                        invalidate()
                        return true
                    }
                })
                .error(R.drawable.ic_launcher_background)
                .preload()
        }
    }

}