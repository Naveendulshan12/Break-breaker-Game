package com.example.brickbreakerkotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import java.util.Random

class GameView(context: Context) : View(context) {

    private var ballX: Float = 0.toFloat()
    private var ballY: Float = 0.toFloat()
    private var velocity = Velocity(25, 32)
    private var handler: Handler
    private val UPDATE_MILLIS: Long = 30
    private val runnable: Runnable
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val brickPaint = Paint()
    private val TEXT_SIZE = 120f
    private var paddleX: Float = 0.toFloat()
    private var paddleY: Float = 0.toFloat()
    private var oldX: Float = 0.toFloat()
    private var oldPaddleX: Float = 0.toFloat()
    private var points = 0
    private var life = 3
    private var ball: Bitmap
    private var paddle: Bitmap
    private lateinit var restart: Bitmap
    private lateinit var exit: Bitmap
    private lateinit var newHighest: Bitmap
    private lateinit var play: Bitmap
    private var dWidth: Int = 0
    private var dHeight: Int = 0
    private var ballWidth: Int = 0
    private var ballHeight: Int = 0
    private var mpHit: MediaPlayer
    private var mpMiss: MediaPlayer
    private var mpBreak: MediaPlayer
    private val random = Random()
    private val bricks = arrayOfNulls<Brick>(30)
    private var numBricks = 0
    private var brokenBricks = 0
    private var gameOver = false

    init {
        ball = BitmapFactory.decodeResource(resources, R.drawable.ball)
        val increasedBallWidth = ball.width / 10
        val increasedBallHeight = ball.height / 10
        ball = Bitmap.createScaledBitmap(ball, increasedBallWidth, increasedBallHeight, false)

        paddle = BitmapFactory.decodeResource(resources, R.drawable.paddle)
        val reducedPaddleWidth = paddle.width / 10
        val reducedPaddleHeight = paddle.height / 10
        paddle = Bitmap.createScaledBitmap(paddle, reducedPaddleWidth, reducedPaddleHeight, false)

        handler = Handler()
        runnable = Runnable { invalidate() }
        mpHit = MediaPlayer.create(context, R.raw.hit)
        mpMiss = MediaPlayer.create(context, R.raw.miss)
        mpBreak = MediaPlayer.create(context, R.raw.breaking)
        textPaint.color = Color.WHITE
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        healthPaint.color = Color.GREEN
        brickPaint.color = Color.argb(255, 249, 129, 0)
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        ballX = random.nextInt(dWidth - 50).toFloat()
        ballY = (dHeight / 3).toFloat()
        paddleY = (dHeight * 4) / 5.toFloat()
        paddleX = (dWidth / 2 - paddle.width / 2).toFloat()
        ballWidth = ball.width
        ballHeight = ball.height
        createBricks()
    }

    private fun createBricks() {
        val brickWidth = dWidth / 8
        val brickHeight = dHeight / 16
        for (column in 0 until 8) {
            for (row in 0 until 3) {
                bricks[numBricks] = Brick(row, column, brickWidth, brickHeight)
                numBricks++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        ballX += velocity.x
        ballY += velocity.y
        if (ballX >= dWidth - ball.width || ballX <= 0) {
            velocity.x *= -1
        }
        if (ballY <= 0) {
            velocity.y *= -1
        }
        if (ballY > paddleY + paddle.height) {
            ballX = (1 + random.nextInt(dWidth - ball.width - 1)).toFloat()
            ballY = (dHeight / 3).toFloat()
            if (mpMiss != null) {
                mpMiss.start()
            }
            velocity.x = xVelocity()
            velocity.y = 32
            life--
            if (life == 0) {
                gameOver = true
                launchGameOver()
            }
        }
        if (ballX + ball.width >= paddleX && ballX <= paddleX + paddle.width &&
            ballY + ball.height >= paddleY && ballY + ball.height <= paddleY + paddle.height
        ) {
            if (mpHit != null) {
                mpHit.start()
            }
            velocity.x = velocity.x + 1
            velocity.y = (velocity.y + 1) * -1
        }
        canvas.drawBitmap(ball, ballX, ballY, null)
        canvas.drawBitmap(paddle, paddleX, paddleY, null)
        for (i in 0 until numBricks) {
            if (bricks[i]?.getVisibility() == true) {
                canvas.drawRect(
                    (bricks[i]?.column ?: 0) * (bricks[i]?.width ?: 0).toFloat() + 1,
                    (bricks[i]?.row ?: 0) * (bricks[i]?.height ?: 0).toFloat() + 1,
                    (bricks[i]?.column ?: 0) * (bricks[i]?.width ?: 0).toFloat() + (bricks[i]?.width
                        ?: 0) - 1,
                    (bricks[i]?.row ?: 0) * (bricks[i]?.height ?: 0).toFloat() + (bricks[i]?.height
                        ?: 0) - 1,
                    brickPaint
                )
            }
        }
        canvas.drawText("$points", 20f, TEXT_SIZE, textPaint)
        if (life == 2) {
            healthPaint.color = Color.YELLOW
        } else if (life == 1) {
            healthPaint.color = Color.RED
        }
        canvas.drawRect(
            (dWidth - 200).toFloat(), 30f, (dWidth - 200 + 60 * life).toFloat(), 80f,
            healthPaint
        )
        for (i in 0 until numBricks) {
            if (bricks[i]?.getVisibility() == true) {
                if (ballX + ballWidth >= (bricks[i]?.column ?: 0) * (bricks[i]?.width ?: 0) &&
                    ballX <= (bricks[i]?.column ?: 0) * (bricks[i]?.width ?: 0) + (bricks[i]?.width
                        ?: 0) &&
                    ballY <= (bricks[i]?.row ?: 0) * (bricks[i]?.height ?: 0) + (bricks[i]?.height
                        ?: 0) &&
                    ballY >= (bricks[i]?.row ?: 0) * (bricks[i]?.height ?: 0)
                ) {
                    if (mpBreak != null) {
                        mpBreak.start()
                    }
                    velocity.y = ((velocity.y + 1) * -1).toFloat().toInt()
                    bricks[i]?.setInvisible()
                    points += 10
                    brokenBricks++
                    if (brokenBricks == 24) {
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= paddleY) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.x
                oldPaddleX = paddleX
            }
            if (action == MotionEvent.ACTION_MOVE) {
                val shift = oldX - touchX
                val newPaddleX = oldPaddleX - shift
                paddleX = when {
                    newPaddleX <= 0 -> 0f
                    newPaddleX >= dWidth - paddle.width -> (dWidth - paddle.width).toFloat()
                    else -> newPaddleX
                }
            }
        }
        return true
    }

    private fun launchGameOver() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        startActivity(context, intent, null)
        (context as Activity).finish()
    }

    private fun xVelocity(): Int {
        val values = intArrayOf(-35, -30, -25, 25, 30, 35)
        val index = random.nextInt(6)
        return values[index]
    }
}
