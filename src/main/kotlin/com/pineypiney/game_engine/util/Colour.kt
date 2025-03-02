package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.extension_functions.wrap
import glm_.mat3x3.Mat3
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.*

class Colour {

	constructor(r: Float, g: Float, b: Float, a: Float = 1f, model: ColourModel = ColourModel.RGB){
		val v: Vec3 = when(model){
			ColourModel.RGB -> Vec3(r, g, b)
			ColourModel.HSV -> hsv2rgb(r, g, b)
			ColourModel.HSL -> hsl2rgb(r, g, b)
			ColourModel.CIEXYZ -> xyz2rgb(r, g, b)
			ColourModel.OKLAB -> oklab2rgb(r, g, b)
			ColourModel.OKLCH -> oklab2rgb(oklch2oklab(r, g, b))
		}

		this.r = (v.x * 255f).toInt().toByte()
		this.g = (v.y * 255f).toInt().toByte()
		this.b = (v.z * 255f).toInt().toByte()
		this.a = (a * 255f).toInt().toByte()
	}

	constructor(r: Byte, g: Byte, b: Byte, a: Byte = -1){
		this.r = r
		this.g = g
		this.b = b
		this.a = a
	}

	constructor(r: Int, g: Int, b: Int, a: Int = 255){
		this.r = r.toByte()
		this.g = g.toByte()
		this.b = b.toByte()
		this.a = a.toByte()
	}

	constructor(hex: Number){
		val int = hex.toInt()
		a = ((int shr 24) and 255).toByte()
		r = ((int shr 16) and 255).toByte()
		g = ((int shr 8) and 255).toByte()
		b = (int and 255).toByte()
	}

	constructor(hex: UInt){
		a = ((hex shr 24) and 255u).toByte()
		r = ((hex shr 16) and 255u).toByte()
		g = ((hex shr 8) and 255u).toByte()
		b = (hex and 255u).toByte()
	}

	constructor(hex: String): this(Integer.parseInt(hex))

	val r: Byte
	val g: Byte
	val b: Byte
	val a: Byte

	val rf get() = r.toUByte().toFloat() * b2f
	val gf get() = g.toUByte().toFloat() * b2f
	val bf get() = b.toUByte().toFloat() * b2f
	val af get() = a.toUByte().toFloat() * b2f

	val rgbValue: Vec3 get() = Vec3(rf, gf, bf)
	val rgbaValue: Vec4 get() = Vec4(rf, gf, bf, af)
	val linearRgbValue: Vec3 get() = Vec3(sRGB2Linear(rf), sRGB2Linear(gf), sRGB2Linear(bf))
	val hsvValue: Vec3 get() = rgb2hsv(rf, gf, bf)
	val hslValue: Vec3 get() = rgb2hsl(rf, gf, bf)
	val cieValue: Vec3 get() = rgb2xyz(rf, gf, bf)
	val oklabValue: Vec3 get() = rgb2oklab(rf, gf, bf)
	val oklchValue: Vec3 get() = oklabValue.run { oklab2oklch(x, y, z) }

	override fun toString(): String {
		return "Colour(${r.toUByte()}, ${g.toUByte()}, ${b.toUByte()})"
	}

	companion object {

		const val b2f = 0.00392156862f

		val rgb2lmsMatrix = Mat3(
			0.4122214708f, 0.2119034982f, 0.0883024619f,
			0.5363325363f, 0.6806995451f, 0.2817188376f,
			0.0514459929f, 0.1073969566f, 0.6299787005f
		)

		val rgbLinear2xyzMatrix = Mat3(
			0.4124f, 0.2126f, 0.0193f,
			0.3576f, 0.7152f, 0.1192f,
			0.1805f, 0.0722f, 0.9505f
		)

		// https://en.wikipedia.org/wiki/Oklab_color_space#Conversion_from_CIE_XYZ

		val cie2oklabMatrix1 = Mat3(
			0.8189330101f, 0.3618667424f, -0.1288597137f,
			0.0329845436f, 0.9293118715f, 0.0361456387f,
			0.0482003018f, 0.2643662691f, 0.6338517070f
		)

		val cie2oklabMatrix2 = Mat3(
			0.2104542553f, 1.9779984951f, 0.0259040371f,
			0.7936177850f, -2.4285922050f, 0.7827717662f,
			-0.0040720468f, 0.4505937099f, -0.8086757660f
		)

		// https://en.wikipedia.org/wiki/SRGB#Transfer_function_(%22gamma%22)
		fun sRGB2Linear(rgb: Float): Float{
			return if(rgb < .04045f) rgb / 12.92f
			else ((rgb + .055f) / 1.055f).pow(2.4f)
		}

		fun linear2sRGB(rgb: Float): Float{
			return if(rgb < .00031308f) rgb * 12.92f
			else (rgb.pow(1f/2.4f) * 1.055f) - .055f
		}

		// https://math.stackexchange.com/questions/556341/rgb-to-hsv-color-conversion-algorithm
		fun rgb2hsv(r: Float, g: Float, b: Float): Vec3 {
			val max = maxOf(r, g, b)
			val min = minOf(r, g, b)
			val d = max - min

			val h = when(max){
				r -> 60 * ((g - b) / d).mod(6f)
				g -> 60 * (((b - r) / d) + 2)
				else -> 60 * (((r - g) / d) + 4)
			}
			val s = if(max == 0f) 0f else d / max

			return Vec3(h, s, max)
		}

		// https://stackoverflow.com/questions/51203917/math-behind-hsv-to-rgb-conversion-of-colors
		fun hsv2rgb(h: Float, s: Float, v: Float): Vec3{
			val i = (h * .01666667f).toInt()
			val f = h * 6 - i
			val p = v * (1 - s)
			val q = v * (1 - f * s)
			val t = v * (1 - (1 - f) * s)

			val (r, g, b) = when(i % 6){
				0 -> Vec3(v, t, p)
				1 -> Vec3(q, v, p)
				2 -> Vec3(p, v, t)
				3 -> Vec3(p, q, v)
				4 -> Vec3(t, p, v)
				else -> Vec3(v, p, q)
			}

			return Vec3(r, g, b)

		}

		fun rgb2hsl(r: Float, g: Float, b: Float): Vec3 {
			val max = maxOf(r, g, b)
			val min = minOf(r, g, b)
			val d = max - min

			val l = (min + max) * .5f
			val s = if(l <= .5f) d / (max + min)
			else d / (2f - d)

			val h = when(max){
				r -> 60 * ((g - b) / d).mod(6f)
				g -> 60 * (((b - r) / d) + 2)
				else -> 60 * (((r - g) / d) + 4)
			}

			return Vec3(h, s, l)
		}
		// https://stackoverflow.com/a/9493060
		fun hsl2rgb(h: Float, s: Float, l: Float): Vec3{
			fun hueToRgb(p: Float, q: Float, ti: Float): Float {
				var t = ti.wrap(0f, 1f)
				if (t < 1f/6) return p + (q - p) * 6f * t
				if (t < 1f/2) return q
				if (t < 2f/3) return p + (q - p) * (2f/3 - t) * 6f
				return p
			}

			return if (s == 0f) {
				Vec3(l)
			} else {
				val q = if(l < 0.5f) l * (1f + s) else (l + s) - l * s
				val p = 2f * l - q
				Vec3(hueToRgb(p, q, h + 1f/3), hueToRgb(p, q, h), hueToRgb(p, q, h - 1f/3))
			}
		}

		// https://en.wikipedia.org/wiki/Oklab_color_space#Conversion_from_sRGB
		fun rgb2xyz(r: Float, g: Float, b: Float): Vec3{
			return rgbLinear2xyzMatrix * Vec3(sRGB2Linear(r), sRGB2Linear(g), sRGB2Linear(b)) * 100f
			//return cie2oklabMatrix1.inverse() * lms
		}

		fun xyz2rgb(xyz: Vec3): Vec3{
			val linear = rgbLinear2xyzMatrix.inverse() * xyz * .01f
			return Vec3(linear2sRGB(linear.x), linear2sRGB(linear.y), linear2sRGB(linear.z))
		}

		fun xyz2rgb(x: Float, y: Float, z: Float): Vec3{
			return xyz2rgb(Vec3(x, y, z))
		}


		// https://gist.github.com/earthbound19/e7fe15fdf8ca3ef814750a61bc75b5ce
		fun rgb2oklab(r: Float, g: Float, b: Float): Vec3{
			val linear = Vec3(sRGB2Linear(r), sRGB2Linear(g), sRGB2Linear(b))
			val lms = rgb2lmsMatrix * linear
			val dlms = Vec3(cbrt(lms.x), cbrt(lms.y), cbrt(lms.z))
			return cie2oklabMatrix2 * dlms
		}

		// https://en.wikipedia.org/wiki/Oklab_color_space#Conversion_to_CIE_XYZ_and_sRGB
		fun oklab2rgb(Lab: Vec3): Vec3{
			val dlms = cie2oklabMatrix2.inverse() * Lab
			val lms = Vec3(dlms.x.pow(3f), dlms.y.pow(3f), dlms.z.pow(3f))
			val linear = rgb2lmsMatrix.inverse() * lms
			return Vec3(linear2sRGB(linear.x), linear2sRGB(linear.y), linear2sRGB(linear.z))
		}

		fun oklab2rgb(L: Float, a: Float, b: Float): Vec3{
			return oklab2rgb(Vec3(L, a, b))
		}

		fun oklab2oklch(L: Float, a: Float, b: Float): Vec3{
			return Vec3(L, sqrt(a*a + b*b), atan2(b, a))
		}

		fun oklch2oklab(L: Float, C: Float, h: Float): Vec3{
			return Vec3(L, C * cos(h), C * sin(h))
		}
	}

	enum class ColourModel {
		RGB,
		HSV,
		HSL,
		CIEXYZ,
		OKLAB,
		OKLCH,
	}
}