package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.resources.video.Video
import glm_.vec2.Vec2

class VideoPlayer(override val video: Video, override var origin: Vec2 = Vec2(-1, -1), override val size: Vec2 = Vec2(2, 2)): AbstractVideoPlayer() {

}