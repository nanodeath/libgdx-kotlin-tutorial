package com.mygdx.game

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton

class GameModule(private val myGdxGame: MyGdxGame) : Module {
    override fun configure(binder: Binder) {
        binder.bind(SpriteBatch::class.java).toInstance(myGdxGame.batch)
    }

    @Provides @Singleton
    fun systems() : Systems {
        return Systems(listOf(
                PhysicsSystem::class.java,
                PhysicsSynchronizationSystem::class.java,
                RenderingSystem::class.java,
                PhysicsDebugSystem::class.java
        ))
    }

    @Provides @Singleton
    fun camera() : OrthographicCamera {
        val viewportWidth = Gdx.graphics.width.pixelsToMeters
        val viewportHeight = Gdx.graphics.height.pixelsToMeters
        return OrthographicCamera(viewportWidth, viewportHeight).apply {
            position.set(viewportWidth / 2F, viewportHeight / 2F, 0F)
            update()
        }
    }

    @Provides @Singleton
    fun world() : World {
        Box2D.init()
        return World(Vector2(0F, -9.81F), true)
    }
}

data class Systems(val list: List<Class<out EntitySystem>>)