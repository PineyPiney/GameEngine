package com.pineypiney.game_engine.visual.util.shapes

import com.pineypiney.game_engine.util.sin60
import org.lwjgl.opengl.GL46C.*

class IndicesShape(vertices: FloatArray, numVertices: Int, val indices: IntArray) : Shape(vertices, numVertices) {

    private val EBO = glGenBuffers()

    init{

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        val bytes = 4 //Float.BYTES

        // How to read vertex array for indices
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * bytes, 0)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * bytes, (3L * bytes))
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * bytes, (6L * bytes))

    }

    companion object {

        val triangleVertices = floatArrayOf(
            0.0,	sin60,		0.0,	1.0,	0.0,	0.0,	0.5,	1.0,	// Top Corner
            -0.5,	0.0,	0.0,	1.0,	1.0,	0.0,	0.0,	0.5,	// Left Corner
            0.5,	-0.0,	0.0,	0.0,	1.0,	0.0,	1.0,	0.5,	// Right Corner
            0.0,	-sin60,		0.0,	0.0,	0.0,	1.0,	0.5,	0.0	    // Bottom Corner
        )
        val triangleIndices = intArrayOf(
            0, 1, 2,
            3, 2, 1
        )

        val triangleIndicesShape = IndicesShape(triangleVertices, 4, triangleIndices)
    }
}
