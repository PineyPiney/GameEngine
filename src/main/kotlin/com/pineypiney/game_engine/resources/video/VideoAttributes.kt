package com.pineypiney.game_engine.resources.video

import org.bytedeco.javacv.FFmpegFrameGrabber

data class VideoAttributes(
	val length: Double,
	val frameRate: Double,
	val frameNumber: Int,
	val width: Int,
	val height: Int,
	val pixelFormat: Int,
	val sampleRate: Int
) {

	constructor(grabber: FFmpegFrameGrabber) : this(
		grabber.lengthInTime * 1e-6,
		grabber.frameRate,
		grabber.frameNumber,
		grabber.imageWidth,
		grabber.imageHeight,
		grabber.pixelFormat,
		grabber.sampleRate
	)
}