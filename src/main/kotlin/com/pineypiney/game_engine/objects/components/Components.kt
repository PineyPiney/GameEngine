package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.BoxCollider3DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.colliders.CompoundCollider3DComponent
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.*
import com.pineypiney.game_engine.objects.components.scrollList.ScrollBarComponent
import com.pineypiney.game_engine.objects.components.slider.*
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType

class Components {

	companion object {
		private val components: MutableSet<KClass<out ComponentI>> = mutableSetOf()

		fun addComponent(component: KClass<out ComponentI>) {
			if (!component.isAbstract) components.add(component)
		}

		fun getAllComponentNames() = components.mapNotNull { it.simpleName }

		fun getAllComponents(): Map<String, KClass<out ComponentI>> =
			components.associateBy { it.simpleName ?: "Anonymous" }

		fun getComponent(name: String): KClass<out ComponentI>? {
			return components.firstOrNull { it.simpleName == name }
		}

		fun createComponent(name: String, parent: GameObject): ComponentI? {
			val component = getComponent(name) ?: return null
			const@ for (constructor in component.constructors) {
				val params = mutableMapOf<KParameter, Any?>()
				for (param in constructor.parameters) {
					if (param.name == "parent" && param.type.javaType == GameObject::class.java) params[param] = parent
					else if (param.isOptional) continue
					else params[param] = defaultParameters[param.type.javaClass.simpleName]?.invoke() ?: continue@const
				}
				return constructor.callBy(params)
			}
			return null
		}

		init {
			addComponent(BoxCollider3DComponent::class)
			addComponent(Collider2DComponent::class)
			addComponent(Collider3DComponent::class)
			addComponent(CompoundCollider3DComponent::class)

			addComponent(CaretRendererComponent::class)
			addComponent(ColouredSpriteComponent::class)
			addComponent(ColourRendererComponent::class)
			addComponent(MeshedTextureComponent::class)
			addComponent(ModelRendererComponent::class)
			addComponent(SpriteComponent::class)
			addComponent(TextRendererComponent::class)

			addComponent(ScrollBarComponent::class)

			addComponent(ActionFloatSliderComponent::class)
			addComponent(ActionIntSliderComponent::class)
			addComponent(ColourSliderRendererComponent::class)
			addComponent(OutlinedSliderRendererComponent::class)
			addComponent(FloatSliderComponent::class)
			addComponent(SliderPointerComponent::class)

			addComponent(ActionTextFieldComponent::class)
			addComponent(AnimatedComponent::class)
			addComponent(ButtonComponent::class)
			addComponent(CheckBoxComponent::class)
			addComponent(ClickerComponent::class)
			addComponent(GameClickerComponent::class)
			addComponent(HoverComponent::class)
			addComponent(LightComponent::class)
			addComponent(RelativeTransformComponent::class)
			addComponent(Rigidbody2DComponent::class)
			addComponent(Rigidbody3DComponent::class)
			addComponent(TextFieldComponent::class)
			addComponent(TextureMapsComponent::class)
			addComponent(TransformComponent::class)
		}

		val defaultParameters: Map<String, () -> Any> = mapOf(
			"Int" to { 1 },
			"UInt" to { 1u },
			"Float" to { 1f },
			"Double" to { 1.0 },
			"Vec2" to { Vec2() },
			"Vec3" to { Vec3() },
			"Vec4" to { Vec4() },
			"Vec2i" to { Vec2i() },
			"Vec3i" to { Vec3i() },
			"Vec4i" to { Vec4i() },
			"Shader" to { Shader.brokeShader },
			"Texture" to { Texture.broke },
		)

		@Suppress("UNCHECKED_CAST")
		fun<C: Any> getNewDefaultField(property: KMutableProperty1<C, Any>, component: C, parent: String = ""): ComponentField<*>?{
			return when(property.returnType.javaType.typeName){
				"boolean" -> BoolField(parent + property.name, { property.get(component) as Boolean }){ property.set(component, it)}
				"int" -> {
					val range = property.findAnnotation<IntFieldRange>()
					if(range == null) IntField(parent + property.name, { property.get(component) as Int }){ property.set(component, it)}
					else IntRangeField(parent + property.name, IntRange(range.min, range.max), { property.get(component) as Int }
					){ property.set(component, it)}
				}
				"kotlin.UInt" -> UIntField(parent + property.name, { property.get(component) as UInt }){ property.set(component, it)}
				"float" -> FloatField(parent + property.name, { property.get(component) as Float }){ property.set(component, it)}
				"double" -> DoubleField(parent + property.name, { property.get(component) as Double }){ property.set(component, it)}
				"glm_.vec2.Vec2" -> Vec2Field(parent + property.name, { property.get(component) as Vec2 }){ property.set(component, it)}
				"glm_.vec3.Vec3" -> Vec3Field(parent + property.name, { property.get(component) as Vec3 }){ property.set(component, it)}
				"glm_.vec4.Vec4" -> Vec4Field(parent + property.name, { property.get(component) as Vec4 }){ property.set(component, it)}
				"glm_.quat.Quat" -> QuatField(parent + property.name, { property.get(component) as Quat }){ property.set(component, it)}

				"com.pineypiney.game_engine.resources.shaders.Shader" -> ShaderField(parent + property.name, { property.get(component) as Shader }){ property.set(component, it)}
				"com.pineypiney.game_engine.resources.textures.Texture" -> TextureField(parent + property.name, {property.get(component) as Texture}){ property.set(component, it)}
				"com.pineypiney.game_engine.resources.models.Model" -> ModelField(parent + property.name, {property.get(component) as Model}){ property.set(component, it)}
				else -> null
			}
		}
	}
}