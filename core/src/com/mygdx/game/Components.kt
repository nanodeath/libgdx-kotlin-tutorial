package com.mygdx.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2


class TransformComponent(val position: Vector2) : Component {
    companion object : ComponentResolver<TransformComponent>(TransformComponent::class.java)
}

val Entity.transform: TransformComponent
    get() = TransformComponent[this]

class TextureComponent(val texture: Texture) : Component {
    companion object : ComponentResolver<TextureComponent>(TextureComponent::class.java)
}

val Entity.texture: TextureComponent
    get() = TextureComponent[this]

open class ComponentResolver<T : Component>(componentClass: Class<T>) {
    val MAPPER = ComponentMapper.getFor(componentClass)
    operator fun get(entity: Entity) = MAPPER.get(entity)
}