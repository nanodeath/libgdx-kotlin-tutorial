package com.mygdx.game

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World
import com.google.inject.Inject

class PhysicsSystem @Inject constructor(private val world: World) : EntitySystem() {
    private var accumulator = 0f
    override fun update(deltaTime: Float) {
        // https://github.com/libgdx/libgdx/wiki/Box2d#stepping-the-simulation
        // http://gafferongames.com/game-physics/fix-your-timestep/
        val frameTime = Math.min(deltaTime, 0.25F)
        accumulator += frameTime
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            accumulator -= TIME_STEP
        }
        // Fixed time step
        //world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
    }

    companion object {
        private val TIME_STEP = 1.0f / 300f
        private val VELOCITY_ITERATIONS = 6
        private val POSITION_ITERATIONS = 2
    }
}