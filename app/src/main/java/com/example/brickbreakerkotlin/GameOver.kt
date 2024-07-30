package com.example.brickbreakerkotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOver : AppCompatActivity() {
    private lateinit var tvPoints: TextView
    private lateinit var ivNewHighest: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)
        ivNewHighest = findViewById(R.id.ivNewHeighest)
        tvPoints = findViewById(R.id.tvPoints)
        val points = intent.getIntExtra("points", 0)
        if (points == 240) {
            ivNewHighest.visibility = View.VISIBLE
        }
        tvPoints.text = points.toString()
    }

    fun restart(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun exit(view: View) {
        finish()
    }
}
