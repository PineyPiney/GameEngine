package com.pineypiney.game_engine

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

fun main(){
    val copy = Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor)
    println("Copy: $copy")
}