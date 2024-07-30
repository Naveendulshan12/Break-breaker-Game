package com.example.brickbreakerkotlin

class Brick(private var isVisible: Boolean, var row: Int, var column: Int, var width: Int, var height: Int) {
    constructor(row: Int, column: Int, width: Int, height: Int) : this(true, row, column, width, height)

    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean {
        return isVisible
    }
}
