package com.soufianekre.floatingdraw.models

import android.app.Activity
import android.graphics.Path

import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.models.actions.*
import com.soufianekre.floatingdraw.ui.base.BaseActivity
import java.io.ObjectInputStream
import java.io.Serializable
import java.security.InvalidParameterException
import java.util.*

class CustomPath : Path(),Serializable {
    val actions = LinkedList<DrawAction>()

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val copiedActions = actions.map { it }
        copiedActions.forEach {
            it.perform(this)
        }
    }

    fun readObject(pathData: String, activity: BaseActivity) {
        val tokens = pathData.split("\\s+".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        var i = 0
        try {
            while (i < tokens.size) {
                when (tokens[i][0]) {
                    'M' -> addAction(DrawMove(tokens[i]))
                    'L' -> addAction(DrawLine(tokens[i]))
                    'Q' -> {
                        // Quad actions are of the following form:
                        // "Qx1,y1 x2,y2"
                        // Since we split the tokens by whitespace, we need to join them again
                        if (i + 1 >= tokens.size)
                            throw InvalidParameterException("Error parsing the data for a Quad.")

                        addAction(DrawQuad(tokens[i] + " " + tokens[i + 1]))
                        ++i
                    }
                }
                ++i
            }
        } catch (e: Exception) {
            activity.showError(activity.getString(R.string.unknown_error_occurred))
        }
    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    private fun addAction(action: DrawAction) {
        when (action) {
            is DrawMove -> moveTo(action.x, action.y)
            is DrawLine -> lineTo(action.x, action.y)
            is DrawQuad -> quadTo(action.x1, action.y1, action.x2, action.y2)
        }
    }

    override fun moveTo(x: Float, y: Float) {
        actions.add(DrawMove(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        actions.add(DrawLine(x, y))
        super.lineTo(x, y)
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(DrawQuad(x1, y1, x2, y2))
        super.quadTo(x1, y1, x2, y2)
    }
}
