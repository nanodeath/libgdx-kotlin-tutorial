package com.mygdx.game

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.google.inject.Inject

class RenderingSystem @Inject constructor(private val batch: SpriteBatch,
                                          private val camera: OrthographicCamera) : IteratingSystem(Family.all(TransformComponent::class.java, TextureComponent::class.java).get()) {
    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined
        batch.begin()
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val img = entity.texture.texture
        val position = entity.transform.position
        batch.draw(img,
                position.x - img.width.pixelsToMeters / 2F, position.y - img.height.pixelsToMeters / 2F,
                img.width.pixelsToMeters, img.height.pixelsToMeters)
    }
}