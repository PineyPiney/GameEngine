package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.util.exceptions.ArrayTooSmallException
import glm_.vec3.Vec3

data class Face @Throws(ArrayTooSmallException::class) constructor(val vertices: Array<Mesh.MeshVertex>, val smoothShading: Boolean = false){

    init {
        if(vertices.size < 3){
            throw ArrayTooSmallException("3 vertices are need to make a face")
        }
    }

    // The * is called the spread operator, and converts arrays to varargs
    val normal: Vec3 = average(*(vertices.map { it.normal }.toTypedArray()))

    fun average(vararg vecs: Vec3): Vec3{
        var v = Vec3()
        vecs.forEach {
            v = v + it
        }
        return v.normalizeAssign()
    }

    override fun toString(): String {
        return "${vertices[0]}\n${vertices[1]}\n${vertices[2]}\n"
    }
}