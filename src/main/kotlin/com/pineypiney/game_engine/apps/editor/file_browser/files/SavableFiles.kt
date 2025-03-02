package com.pineypiney.game_engine.apps.editor.file_browser.files

import com.pineypiney.game_engine.apps.editor.EditorScreen
import java.io.File

class SavableFiles(val name: String, val ext: String, val defaultContents: ByteArray, val save: (File, EditorScreen) -> Unit, val load: (File, EditorScreen) -> Unit) {

	companion object {
		val list = mutableListOf<SavableFiles>()

		fun add(name: String, ext: String, default: ByteArray, save: (File, EditorScreen) -> Unit, load: (File, EditorScreen) -> Unit){
			list.add(SavableFiles(name, ext, default, save, load))
		}
	}
}