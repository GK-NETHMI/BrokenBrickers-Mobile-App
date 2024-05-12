package com.example.gameapp3

class Brick(private var isVisible: Boolean, val row: Int, val column: Int, val width: Int, val height: Int) {

    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean {
        return isVisible
    }
}
