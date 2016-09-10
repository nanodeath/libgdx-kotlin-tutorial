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
import com.badlogic.gdx.physics.box2d.*
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
        val world = injector.getInstance(World::class.java)
        engine.addEntity(Entity().apply {
            add(TextureComponent(img))
            add(TransformComponent(Vector2(5F, 5F)))

            val body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            body.createFixture(PolygonShape().apply {
                setAsBox(img.width.pixelsToMeters / 2F, img.height.pixelsToMeters / 2F)
            }, 1.0F)
            body.setTransform(transform.position, 0F)
            add(PhysicsComponent(body))
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

class PhysicsSynchronizationSystem : IteratingSystem(Family.all(TransformComponent::class.java, PhysicsComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.transform.position.set(entity.physics.body.position)
    }
}

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

class PhysicsDebugSystem @Inject constructor(private val world: World,
                                             private val camera: OrthographicCamera) : EntitySystem() {
    private val renderer = Box2DDebugRenderer()

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
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
        batch.draw(img,
                position.x - img.width.pixelsToMeters / 2F, position.y - img.height.pixelsToMeters / 2F,
                img.width.pixelsToMeters, img.height.pixelsToMeters)
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
