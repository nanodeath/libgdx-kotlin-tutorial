package com.mygdx.game

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.google.inject.Inject

class RenderingSystem @Inject constructor(private val batch: SpriteBatch,
                                          private val camera: OrthographicCamera) :
        IteratingSystem(Family.all(TransformComponent::class.java).one(TextureComponent::class.java, TextureRegionComponent::class.java).get()) {
    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined
        batch.begin()
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = entity.transform.position
        entity.tryGet(TextureComponent)?.let { textureComponent ->
            val img = textureComponent.texture
            batch.draw(img,
                    position.x - img.width.pixelsToMeters / 2F, position.y - img.height.pixelsToMeters / 2F,
                    img.width.pixelsToMeters, img.height.pixelsToMeters)
        }

        entity.tryGet(TextureRegionComponent)?.let { textureRegionComponent ->
            val img = textureRegionComponent.textureRegion
            val width = img.regionWidth.pixelsToMeters
            val height = img.regionHeight.pixelsToMeters
            val scale = entity.transform.scale

            batch.draw(img,
                    position.x - width / 2, position.y - height / 2,
                    width / 2F, height / 2F,
                    width, height,
                    scale, scale,
                    entity.transform.angleRadian.toDegrees
            )
        }
    }
}

val Float.toDegrees : Float
    get() = MathUtils.radiansToDegrees * this