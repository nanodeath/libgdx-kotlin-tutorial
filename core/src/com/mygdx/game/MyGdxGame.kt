package com.mygdx.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.google.inject.*

class MyGdxGame : ApplicationAdapter() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var img: Texture
    internal val engine = Engine()
    private lateinit var injector: Injector

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        injector = Guice.createInjector(GameModule(this))
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach { system ->
            engine.addSystem(system)
        }

        createEntities()
    }

    private fun createEntities() {
        engine.addEntity(Entity().apply {
            add(TextureComponent(img))
            add(TransformComponent(Vector2(1F, 1F)))
        })
    }

    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}

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
        batch.draw(img, position.x, position.y, img.width.pixelsToMeters, img.height.pixelsToMeters)
    }
}

val Int.pixelsToMeters: Float
    get() = this / 32F

class GameModule(private val myGdxGame: MyGdxGame) : Module {
    override fun configure(binder: Binder) {
        binder.bind(SpriteBatch::class.java).toInstance(myGdxGame.batch)
    }

    @Provides @Singleton
    fun systems() : Systems {
        return Systems(listOf(
                RenderingSystem::class.java
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
}

data class Systems(val list: List<Class<out EntitySystem>>)
