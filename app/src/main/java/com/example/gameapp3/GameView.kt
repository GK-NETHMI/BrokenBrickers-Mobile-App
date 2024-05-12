package com.example.gameapp3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import java.util.Random


class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var ballX: Float = 0f
    private var ballY: Float = 0f
    private var velocity: Velocity = Velocity(25, 32)
    private val handler = Handler()
    private val UPDATE_MILLIS: Long = 30
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            invalidate()
            handler.postDelayed(this, UPDATE_MILLIS)
        }
    }
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val brickPaint = Paint()
    private val TEXT_SIZE = 120f
    private var paddleX: Float = 0f
    private var paddleY: Float = 0f
    private var oldX: Float = 0f
    private var oldPaddleX: Float = 0f
    private var points: Int = 0
    private var life: Int = 2
    private lateinit var ball: Bitmap
    private lateinit var paddle: Bitmap
    private var dWidth: Int = 0
    private var dHeight: Int = 0
    private var ballWidth: Int = 0
    private var ballHeight: Int = 0
    private lateinit var mpHit: MediaPlayer
    private lateinit var mpMiss: MediaPlayer
    private lateinit var mpBreak: MediaPlayer
    private var random = java.util.Random()
    private var bricks = arrayOfNulls<Brick?>(150)
    private var numBricks = 0
    private var brokenBricks = 0
    private var gameOver = false

    init {
        ball = BitmapFactory.decodeResource(resources, R.drawable.ball)
        paddle = BitmapFactory.decodeResource(resources, R.drawable.paddle)
        mpHit = MediaPlayer.create(context, R.raw.hit)
        mpMiss = MediaPlayer.create(context, R.raw.miss)
        mpBreak = MediaPlayer.create(context, R.raw.breaking)

        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        random = Random()

        ballX = random.nextInt(dWidth - 50).toFloat()
        ballY = (dHeight * 2 / 3).toFloat() // Adjusted starting position for the ball
        paddleY = (dHeight * 5 / 6).toFloat() // Adjusted starting position for the paddle
        paddleX = (dWidth / 6 - paddle.width / 6).toFloat()
        ballWidth = ball.width
        ballHeight = ball.height

        createBricks()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun createBricks() {
        val brickWidth = dWidth / 8
        val brickHeight = dHeight / 16
        val numRows = 8 // Adjust the number of rows as per your requirement
        val numColumns = 10 // Adjust the number of columns as per your requirement
        for (row in 0 until numRows) {
            for (column in 0 until numColumns) {
                bricks[numBricks] = Brick(isVisible, row, column, brickWidth, brickHeight)
                numBricks++
            }
        }
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        ballX += velocity.getX()
        ballY += velocity.getY()

        if (ballX >= dWidth - ball.width || ballX <= 0) {
            velocity.setX(velocity.getX() * -1)
        }

        if (ballY <= 0) {
            velocity.setY(velocity.getY() * -1)
        }

        if (ballY > paddleY + paddle.height) {
            ballX = (1 + random.nextInt(dWidth - ball.width - 1)).toFloat()
            ballY = (dHeight / 3).toFloat()
            mpMiss.start()
            velocity.setX(xVelocity())
            velocity.setY(32)
            life--
            if (life == 0) {
                gameOver = true
                launchGameOver()
            }
        }

        if ((ballY + ball.height >= paddleX)
            && (ballX <= paddleX + paddle.width)
            && (ballY + ball.height >= paddleY)
            && (ballY + ball.height <= paddleY + paddle.height)
        ) {
            mpHit.start()
            velocity.setX(velocity.getX() + 1)
            velocity.setY((velocity.getY() + 1) * -1)
        }

        canvas.drawBitmap(ball, ballX, ballY, null)
        canvas.drawBitmap(paddle, paddleX, paddleY, null)

        for (i in 0 until numBricks) {
            if (bricks[i]?.getVisibility() == true) {
                canvas.drawRect(
                    bricks[i]!!.column * bricks[i]!!.width + 1f,
                    bricks[i]!!.row * bricks[i]!!.height + 1f,
                    bricks[i]!!.column * bricks[i]!!.width + bricks[i]!!.width - 1f,
                    bricks[i]!!.row * bricks[i]!!.height + bricks[i]!!.height - 1f,
                    brickPaint
                )
            }
        }

    // Draw points
    textPaint.textSize = TEXT_SIZE /2
        textPaint.color = Color.RED
    canvas.drawText("Point: $points", 10f, TEXT_SIZE/2, textPaint)


     if (life == 1) {
        healthPaint.color = Color.RED
    }

    canvas.drawRect(dWidth - 200f, 30f, dWidth - 200f + 60f * life, 80f, healthPaint)

    for (i in 0 until numBricks) {
        if (bricks[i]?.getVisibility() == true) {
            if (ballX + ballWidth >= bricks[i]!!.column * bricks[i]!!.width
                && ballX <= bricks[i]!!.column * bricks[i]!!.width + bricks[i]!!.width
                && ballY <= bricks[i]!!.row * bricks[i]!!.height + bricks[i]!!.height
                && ballY >= bricks[i]!!.row * bricks[i]!!.height
            ) {
                mpBreak.start()
                velocity.setY((velocity.getY() + 1) * -1)
                bricks[i]!!.setInvisible()
                points += 10
                brokenBricks++
                if (brokenBricks == numBricks) {
                    launchGameOver()
                }
            }
        }
    }

    if (brokenBricks == numBricks) {
        gameOver = true
    }

    if (!gameOver) {
        handler.postDelayed(runnable, UPDATE_MILLIS)
    }
}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        if (touchY >= paddleY) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldPaddleX = paddleX
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift = oldX - touchX
                    val newPaddleX = oldPaddleX - shift
                    paddleX = when {
                        newPaddleX <= 0 -> 0F
                        newPaddleX >= dWidth - paddle.width -> (dWidth - paddle.width).toFloat()
                        else -> newPaddleX
                    }
                }
            }
        }
        return true
    }

    private fun launchGameOver() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    private fun xVelocity(): Int {
        val values = intArrayOf(-15, -10, -5, 5, 10, 15)
        val index = random.nextInt(6)
        return values[index]
    }
}
