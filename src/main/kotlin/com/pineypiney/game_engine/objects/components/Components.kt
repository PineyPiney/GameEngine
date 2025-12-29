package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.*
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionPolygonRenderer
import com.pineypiney.game_engine.objects.components.widgets.ActionTextFieldComponent
import com.pineypiney.game_engine.objects.components.widgets.ButtonComponent
import com.pineypiney.game_engine.objects.components.widgets.CheckBoxComponent
import com.pineypiney.game_engine.objects.components.widgets.TextFieldComponent
import com.pineypiney.game_engine.objects.components.widgets.scrollList.ScrollBarComponent
import com.pineypiney.game_engine.objects.components.widgets.slider.*
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.firstNotNullOfOrNull
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
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
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.javaType

class Components {

	companion object {
		private val components: MutableSet<KClass<out ComponentI>> = mutableSetOf()
		private val fieldTypes: MutableSet<FieldType<*>> = mutableSetOf()

		fun addComponent(component: KClass<out ComponentI>) {
			if (!component.isAbstract) components.add(component)
		}

		fun addFieldType(fieldType: FieldType<*>){
			fieldTypes.add(fieldType)
		}

		fun <T> addFieldType(fieldCreator: (id: String, getter: () -> T, setter: (T) -> Unit) -> ComponentField<T>, default: () -> T, klass: KClass<*> = default()!!::class){
			fieldTypes.add(FieldType(default, klass, fieldCreator))
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
					// Components all take a parent GameObject argument
					if (param.name == "parent" && param.type.javaType == GameObject::class.java) params[param] = parent
					// If the constructor has a default option then use that one
					else if (param.isOptional) continue
					else {
						// Find the default value for this class
						val value = fieldTypes.firstNotNullOfOrNull({it.default()}) { d -> d::class == param.type.classifier }
						if(value != null) params[param] = value
						// Last resort, if the argument is nullable then set the argument to null
						else if(param.type.isMarkedNullable) params[param] = null
						// Otherwise try a new constructor
						else  continue@const
					}
				}
				return constructor.callBy(params)
			}
			return null
		}

		init {
			addComponent(Collider2DComponent::class)
			addComponent(Collider3DComponent::class)

			addComponent(AtlasAnimatedSprite::class)
			addComponent(CollisionPolygonRenderer::class)
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

			addFieldType(::BoolField, { true })
			addFieldType(::IntField, {1})
			addFieldType(FieldType({1}, setOf(IntFieldRange::class)) { i, g, s, a ->
				val rangeA = a.firstOrNull { it is IntFieldRange } as? IntFieldRange
				val range = rangeA?.let { IntRange(it.min, it.max) } ?: 0..1
				IntRangeField(i, range, g, s)
			})
			addFieldType(::UIntField, {1u})
			addFieldType(::FloatField, {1f})
			addFieldType(::DoubleField, {1.0})
			addFieldType(::Vec2iField, {Vec2i()})
			addFieldType(::Vec3iField, {Vec3i()})
			addFieldType(::Vec4iField, {Vec4i()})
			addFieldType(::Vec2Field, {Vec2()})
			addFieldType(::Vec3Field, {Vec3()})
			addFieldType(::Vec4Field, {Vec4()})
			addFieldType(::QuatField, {Quat()})
			addFieldType(::ShaderField, {Shader.brokeShader})
			addFieldType(::TextureField, {Texture.broke})
			addFieldType(::ModelField, {Model.brokeModel})
			addFieldType(::Shape2DField, {Rect2D(Vec2(0f), 1f, 1f)}, Shape2D::class)
			addFieldType(::GameObjectField, { null }, GameObject::class)
		}

		@Suppress("FilterIsInstanceResultIsAlwaysEmpty")
		fun <C, T> getDefaultField(property: KMutableProperty1<C, T>, component: C, parent: String = ""): ComponentField<*>?{
			val fieldsOfType = fieldTypes.filter { fieldType -> property.returnType.withNullability(false) == fieldType.klass.starProjectedType }.filterIsInstance<FieldType<T>>()
			for(i in fieldsOfType){
				if(i.annotations.isNotEmpty() && i.annotations.all { property.findAnnotations(it).isNotEmpty() }){
					return i.fieldCreator(parent + property.name, { property.get(component) }, { property.set(component, it)}, property.annotations.toSet())
				}
			}

			val defaultField = fieldsOfType.firstOrNull { it.annotations.isEmpty() }
			return if(defaultField == null) null
			else defaultField.fieldCreator(parent + property.name, {property.get(component)}, { property.set(component, it)}, emptySet())
		}

		inline fun <reified T, C: Any> get(property: KMutableProperty1<C, Any>, component: C): T{
			try {
				return property.get(component) as T
			}
			catch (e: NullPointerException){
				throw e
			}
		}
	}
}