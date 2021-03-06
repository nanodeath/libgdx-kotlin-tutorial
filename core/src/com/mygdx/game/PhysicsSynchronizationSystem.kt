package com.mygdx.game

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.google.inject.Inject

class PhysicsSynchronizationSystem @Inject constructor() : IteratingSystem(Family.all(TransformComponent::class.java, PhysicsComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.physics.body
        entity.transform.position.set(body.position)
        entity.transform.angleRadian = body.angle
    }
}