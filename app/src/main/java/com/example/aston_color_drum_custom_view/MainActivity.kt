package com.example.aston_color_drum_custom_view

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val colorDrum = findViewById<ColorDrum>(R.id.colorDrum)
        val seekBar = findViewById<SeekBar>(R.id.seekBarColorDrum)
        val reset = findViewById<FloatingActionButton>(R.id.btnRestart)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                colorDrum.updateDrumSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        seekBar.progress = 50

        reset.setOnClickListener { colorDrum.reset() }
    }
}