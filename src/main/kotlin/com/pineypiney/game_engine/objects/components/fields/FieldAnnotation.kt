package com.pineypiney.game_engine.objects.components.fields

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class EditorIgnore()

@Target(AnnotationTarget.PROPERTY)
annotation class IntFieldRange(val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE){

}

@Target(AnnotationTarget.PROPERTY)
annotation class FloatFieldRange(val min: Float, val max: Float){

}


