package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.fields.*
import com.pineypiney.game_engine.objects.components.rendering.*
import com.pineypiney.game_engine.objects.components.scrollList.ScrollBarComponent
import com.pineypiney.game_engine.objects.components.slider.*
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.firstNotNullOfOrNull
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
import kotlin.reflect.full.findAnnotations
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

		fun <T> addFieldType(default: () -> T, fieldCreator: (id: String, getter: () -> T, setter: (T) -> Unit) -> ComponentField<T>){
			fieldTypes.add(FieldType(default, fieldCreator))
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
					else {
						val value = fieldTypes.firstNotNullOfOrNull({it.default()}) { d -> d::class == param.type.classifier } ?: continue@const
						params[param] = value
					} // defaultParameters[param.type.javaClass.simpleName]?.invoke() ?: continue@const
				}
				return constructor.callBy(params)
			}
			return null
		}

		init {
			addComponent(Collider2DComponent::class)
			addComponent(Collider3DComponent::class)

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

			addFieldType({ true }, ::BoolField)
			addFieldType({1}, ::IntField)
			addFieldType(FieldType({1}, setOf(IntFieldRange::class)) { i, g, s, a ->
				val rangeA = a.firstOrNull { it is IntFieldRange } as? IntFieldRange
				val range = rangeA?.let { IntRange(it.min, it.max) } ?: 0..1
				IntRangeField(i, range, g, s)
			})
			addFieldType({1u}, ::UIntField)
			addFieldType({1f}, ::FloatField)
			addFieldType({1.0}, ::DoubleField)
			addFieldType({Vec2i()}, ::Vec2iField)
			addFieldType({Vec3i()}, ::Vec3iField)
			addFieldType({Vec4i()}, ::Vec4iField)
			addFieldType({Vec2()}, ::Vec2Field)
			addFieldType({Vec3()}, ::Vec3Field)
			addFieldType({Vec4()}, ::Vec4Field)
			addFieldType({Quat()}, ::QuatField)
			addFieldType({Shader.brokeShader}, ::ShaderField)
			addFieldType({Texture.broke}, ::TextureField)
			addFieldType({Model.brokeModel}, ::ModelField)
		}

		@Suppress("UNCHECKED_CAST")
		fun<C: Any> getDefaultField(property: KMutableProperty1<C, Any>, component: C, parent: String = ""): ComponentField<*>?{
			return when(property.returnType.javaType.typeName){
				"boolean" -> BoolField(parent + property.name, { property.get(component) as Boolean }){ property.set(component, it)}
				"int" -> {
					val range = property.findAnnotation<IntFieldRange>()
					if(range == null) IntField(parent + property.name, { property.get(component) as Int }){ property.set(component, it)}
					else IntRangeField(parent + property.name, IntRange(range.min, range.max), { property.get(component) as Int }
					){ property.set(component, it)}
				}
				"kotlin.UInt" -> UIntField(parent + property.name, { get<UInt, C>(property, component) }){ property.set(component, it)}
				"float" -> FloatField(parent + property.name, { get<Float, C>(property, component) }){ property.set(component, it)}
				"double" -> DoubleField(parent + property.name, { get<Double, C>(property, component) }){ property.set(component, it)}
				"glm_.vec2.Vec2" -> Vec2Field(parent + property.name, { get<Vec2, C>(property, component) }){ property.set(component, it)}
				"glm_.vec3.Vec3" -> Vec3Field(parent + property.name, { get<Vec3, C>(property, component) }){ property.set(component, it)}
				"glm_.vec4.Vec4" -> Vec4Field(parent + property.name, { get<Vec4, C>(property, component) }){ property.set(component, it)}
				"glm_.quat.Quat" -> QuatField(parent + property.name, { get<Quat, C>(property, component) }){ property.set(component, it)}

				"com.pineypiney.game_engine.resources.shaders.Shader" -> ShaderField(parent + property.name, { property.get(component) as Shader }){ property.set(component, it)}
				"com.pineypiney.game_engine.resources.textures.Texture" -> TextureField(parent + property.name, {property.get(component) as Texture}){ property.set(component, it)}
				"com.pineypiney.game_engine.resources.models.Model" -> ModelField(parent + property.name, {property.get(component) as Model}){ property.set(component, it)}
				else -> null
			}
		}

		@Suppress("FilterIsInstanceResultIsAlwaysEmpty")
		fun <C, T> getNewDefaultField(property: KMutableProperty1<C, T>, component: C, parent: String = ""): ComponentField<*>?{
			val fieldsOfType = fieldTypes.filter { fieldType -> fieldType.default()?.let{ it::class == property.returnType.classifier } == true }.filterIsInstance<FieldType<T>>()
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