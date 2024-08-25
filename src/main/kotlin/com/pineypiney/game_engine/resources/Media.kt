package com.pineypiney.game_engine.resources

abstract class Media : Resource() {

	open var status: MediaStatus = MediaStatus.STOPPED

	abstract fun play(volume: Float = 1f)
	abstract fun pause()
	abstract fun resume()
	abstract fun stop()


	enum class MediaStatus {
		PLAYING,
		PAUSED,
		STOPPED,
	}
}