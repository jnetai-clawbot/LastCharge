package com.jnetai.lastcharge

import android.graphics.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Bundle
import android.widget.ImageView
import android.widget.ScrollView
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*
import java.util.*
import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LastCharge"
        const val CURRENT_VERSION = "1.0.0"
        const val GITHUB_REPO = "jnetai-clawbot/LastCharge"
    }

    private lateinit var gameView: GameView
    private lateinit var aboutButton: Button
    private lateinit var scoreText: TextView
    private lateinit var batteryText: TextView
    private lateinit var timerText: TextView
    private lateinit var enemyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF0A0A1A.toInt()
        window.navigationBarColor = 0xFF0A0A1A.toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0A0A1A.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val hudBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 8)
        }

        batteryText = TextView(this).apply {
            text = "Battery: 100%"
            setTextColor(0xFFCCCCCC.toInt())
            textSize = 16f
            typeface = Typeface.MONOSPACE
            setPadding(0, 0, 0, 4)
        }

        timerText = TextView(this).apply {
            text = "Dawn in: 120s"
            setTextColor(0xFFFF6644.toInt())
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setPadding(0, 0, 0, 4)
        }

        scoreText = TextView(this).apply {
            text = "Score: 0"
            setTextColor(0xFFFF2222.toInt())
            textSize = 18f
            typeface = Typeface.MONOSPACE
        }

        enemyText = TextView(this).apply {
            text = "Defeated: 0"
            setTextColor(0xFFFF4444.toInt())
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setPadding(0, 0, 0, 0)
        }

        hudBar.addView(batteryText)
        hudBar.addView(timerText)
        hudBar.addView(scoreText)
        hudBar.addView(enemyText)

        gameView = GameView(this, ::updateHud)

        val buttonBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(16, 8, 16, 48)
        }

        val restartBtn = Button(this).apply {
            text = "Restart"
            setBackgroundColor(0xFF1A2A3A.toInt())
            setTextColor(0xFFCCCCCC.toInt())
            textSize = 14f
            minHeight = 0
            minimumHeight = 80
            setPadding(24, 12, 24, 12)
            setOnClickListener { gameView.restart() }
        }

        aboutButton = Button(this).apply {
            text = "About"
            setBackgroundColor(0xFF1A2A3A.toInt())
            setTextColor(0xFFFF2222.toInt())
            textSize = 14f
            minHeight = 0
            minimumHeight = 80
            setPadding(24, 12, 24, 12)
            setOnClickListener { showAbout() }
        }

        buttonBar.addView(restartBtn)
        val spacer = View(this).apply { layoutParams = LinearLayout.LayoutParams(32, 0) }
        buttonBar.addView(spacer)
        buttonBar.addView(aboutButton)

        root.addView(hudBar)
        root.addView(gameView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(buttonBar)
        setContentView(root)
    }

    private fun updateHud(battery: Int, timeLeft: Int, score: Int, defeated: Int) {
        runOnUiThread {
            batteryText.text = "Battery: $battery%"
            timerText.text = "Dawn in: ${timeLeft}s"
            scoreText.text = "Score: $score"
            enemyText.text = "Defeated: $defeated"
            if (battery <= 20) batteryText.setTextColor(0xFFFF2222.toInt())
            else batteryText.setTextColor(0xFFCCCCCC.toInt())
        }
    }

    private fun showAbout() {
        val builder = AlertDialog.Builder(this, R.style.AboutDialogTheme)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 32)
            setBackgroundColor(0xFF151528.toInt())
        }

        layout.addView(TextView(this).apply {
            text = "Last Charge"
            setTextColor(0xFFFF2222.toInt())
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 8)
        })

        layout.addView(TextView(this).apply {
            text = "Made by jnetai.com"
            setTextColor(0xFF888899.toInt())
            textSize = 14f
            setPadding(0, 0, 0, 16)
        })

        layout.addView(TextView(this).apply {
            text = "Version $CURRENT_VERSION"
            setTextColor(0xFFCCCCCC.toInt())
            textSize = 16f
            setPadding(0, 0, 0, 24)
        })

        val checkBtn = Button(this).apply {
            text = "Check for Update"
            setBackgroundColor(0xFF440000.toInt())
            setTextColor(0xFFFF2222.toInt())
            textSize = 15f
            minimumHeight = 96
            setPadding(32, 16, 32, 16)
            val btn = this
            setOnClickListener {
                btn.isEnabled = false
                btn.text = "Checking..."
                checkForUpdate { result ->
                    runOnUiThread {
                        btn.text = result
                        btn.isEnabled = true
                    }
                }
            }
        }
        layout.addView(checkBtn)

        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 24)
        })

        val shareBtn = Button(this).apply {
            text = "Share App"
            setBackgroundColor(0xFF234A6A.toInt())
            setTextColor(0xFF00CCFF.toInt())
            textSize = 15f
            minimumHeight = 96
            setPadding(32, 16, 32, 16)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Last Charge")
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                }
                startActivity(Intent.createChooser(intent, "Share via"))
            }
        }
        layout.addView(shareBtn)

        val scrollView = ScrollView(this).apply {
            addView(layout)
        }

        builder.setView(scrollView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun checkForUpdate(callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.github.com/repos/$GITHUB_REPO/releases/latest")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val latestTag = json.getString("tag_name").removePrefix("v")

                if (latestTag != CURRENT_VERSION) {
                    callback("New version $latestTag available!")
                } else {
                    callback("You're up to date!")
                }
            } catch (e: Exception) {
                callback("Could not check updates: ${e.message}")
            }
        }
    }
}

class GameView(context: Context, private val hudCallback: (Int, Int, Int, Int) -> Unit) : View(context) {
    companion object {
        const val DAWN_DURATION = 120f
        const val MOVE_DRAIN = 2
        const val COMBAT_DRAIN = 5
        const val FLASHLIGHT_DRAIN_PER_SEC = 1f
        const val TAG = "GameView"
    }

    private val random = Random()
    private var playerX = 0f
    private var playerY = 0f
    private var battery = 100
    private var timeElapsed = 0f
    private var flashlightOn = false
    private var enemiesDefeated = 0
    private var score = 0
    private var gameOver = false
    private var playerMoved = false

    private val collectibles = mutableListOf<Collectible>()
    private val enemies = mutableListOf<LastChargeEnemy>()
    private var lastMoveDrainTime = 0L
    private var lastFlashlightDrainTime = 0L
    private var lastChargeAttractTime = 0L

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchActive = false
    private var flashTime = 0f
    private var shakeAmount = 0f
    private var gameOverTime = 0f

    private val darkPaint = Paint().apply { color = 0xFF0A0A1A.toInt(); style = Paint.Style.FILL }
    private val floorPaint = Paint().apply { color = 0xFF111122.toInt(); style = Paint.Style.FILL }
    private val gridPaint = Paint().apply { color = 0xFF15152A.toInt(); style = Paint.Style.STROKE; strokeWidth = 1f }
    private val playerPaint = Paint().apply { color = 0xFFFF2222.toInt(); style = Paint.Style.FILL }
    private val playerGlowPaint = Paint().apply { color = 0x44FF2222.toInt(); style = Paint.Style.FILL }
    private val playerOffPaint = Paint().apply { color = 0xFF332222.toInt(); style = Paint.Style.FILL }
    private val outletPaint = Paint().apply { color = 0xFF44FF44.toInt(); style = Paint.Style.FILL }
    private val outletGlowPaint = Paint().apply { color = 0x3344FF44.toInt(); style = Paint.Style.FILL }
    private val solarPaint = Paint().apply { color = 0xFFFFAA00.toInt(); style = Paint.Style.FILL }
    private val solarGlowPaint = Paint().apply { color = 0x33FFAA00.toInt(); style = Paint.Style.FILL }
    private val crankPaint = Paint().apply { color = 0xFF4488FF.toInt(); style = Paint.Style.FILL }
    private val crankGlowPaint = Paint().apply { color = 0x334488FF.toInt(); style = Paint.Style.FILL }
    private val enemyPaint = Paint().apply { color = 0xFFFF3344.toInt(); style = Paint.Style.FILL }
    private val enemyGlowPaint = Paint().apply { color = 0x44FF3344.toInt(); style = Paint.Style.FILL }
    private val flashlightPaint = Paint().apply { color = 0x33FF2222.toInt(); style = Paint.Style.FILL }
    private val batteryBarBgPaint = Paint().apply { color = 0xFF1A1A2A.toInt(); style = Paint.Style.FILL }
    private val batteryBarPaint = Paint().apply { color = 0xFFFF2222.toInt(); style = Paint.Style.FILL }
    private val batteryLowPaint = Paint().apply { color = 0xFFFF6622.toInt(); style = Paint.Style.FILL }
    private val gameOverOverlay = Paint().apply { color = 0xDD000000.toInt(); style = Paint.Style.FILL }
    private val textPaint = Paint().apply {
        color = 0xFFFF2222.toInt()
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val smallTextPaint = Paint().apply {
        color = 0xFFCCCCCC.toInt()
        textSize = 24f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val collectTextPaint = Paint().apply {
        color = 0xFF888899.toInt()
        textSize = 18f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }
    private val pathPaint = Paint().apply {
        color = 0x4400CCFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
    }

    init {
        spawnCollectibles()
        spawnEnemies(4)
        playerX = width.toFloat() / 2f
        playerY = height.toFloat() / 2f
    }

    private fun spawnCollectibles() {
        collectibles.clear()
        for (i in 0 until 4) {
            collectibles.add(Collectible(OutletType.POWER_OUTLET,
                random.nextFloat() * 0.8f + 0.1f, random.nextFloat() * 0.7f + 0.05f))
        }
        for (i in 0 until 3) {
            collectibles.add(Collectible(OutletType.SOLAR_PANEL,
                random.nextFloat() * 0.8f + 0.1f, random.nextFloat() * 0.7f + 0.05f))
        }
        for (i in 0 until 3) {
            collectibles.add(Collectible(OutletType.CRANK_GENERATOR,
                random.nextFloat() * 0.8f + 0.1f, random.nextFloat() * 0.7f + 0.05f))
        }
    }

    private fun spawnEnemies(count: Int) {
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val dist = 0.3f + random.nextFloat() * 0.3f
            enemyLoop@ for (attempt in 0..20) {
                val ex = 0.5f + cos(angle) * dist * (0.5f + attempt * 0.1f)
                val ey = 0.5f + sin(angle) * dist * (0.5f + attempt * 0.1f)
                if (ex in 0.05f..0.95f && ey in 0.05f..0.95f) {
                    val pdx = ex - playerX / maxOf(1f, width.toFloat())
                    val pdy = ey - playerY / maxOf(1f, height.toFloat())
                    if (sqrt(pdx * pdx + pdy * pdy) > 0.15f) {
                        enemies.add(LastChargeEnemy(ex, ey))
                        break@enemyLoop
                    }
                }
            }
        }
    }

    private fun spawnEnemyNearCharger() {
        val chargers = collectibles.filter { it.active }
        if (chargers.isEmpty()) return
        val charger = chargers[random.nextInt(chargers.size)]
        val angle = random.nextFloat() * 2f * PI.toFloat()
        val dist = 0.06f + random.nextFloat() * 0.04f
        val ex = (charger.normX + cos(angle) * dist).coerceIn(0.03f, 0.97f)
        val ey = (charger.normY + sin(angle) * dist).coerceIn(0.03f, 0.97f)
        enemies.add(LastChargeEnemy(ex, ey))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (oldw == 0 && oldh == 0) {
            playerX = w / 2f
            playerY = h / 2f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) {
            if (event.action == MotionEvent.ACTION_UP && gameOverTime > 0.5f) {
                restart()
            }
            return true
        }

        val nx = event.x / width.toFloat()
        val ny = event.y / height.toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                touchActive = true

                for (e in enemies) {
                    val dx = (nx - e.normX) * width
                    val dy = (ny - e.normY) * height
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < 60f && battery >= COMBAT_DRAIN) {
                        battery -= COMBAT_DRAIN
                        enemiesDefeated++
                        shakeAmount = 8f
                        enemies.remove(e)
                        flashTime = 0.15f

                        if (battery <= 0) {
                            battery = 0
                            gameOver = true
                            gameOverTime = 0f
                        }
                        updateScore()
                        touchActive = false
                        break
                    }
                }

                for (c in collectibles) {
                    val dx = (nx - c.normX) * width
                    val dy = (ny - c.normY) * height
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < 65f && !c.active) {
                        c.active = true
                        when (c.type) {
                            OutletType.POWER_OUTLET -> {
                                battery = minOf(100, battery + 30)
                                collectibles.remove(c)
                                collectibles.add(Collectible(OutletType.POWER_OUTLET,
                                    random.nextFloat() * 0.8f + 0.1f, random.nextFloat() * 0.7f + 0.05f))
                                spawnEnemyNearCharger()
                                spawnEnemyNearCharger()
                            }
                            OutletType.SOLAR_PANEL -> {
                                battery = minOf(100, battery + 5)
                                collectibles.remove(c)
                                collectibles.add(Collectible(OutletType.SOLAR_PANEL,
                                    random.nextFloat() * 0.8f + 0.1f, random.nextFloat() * 0.7f + 0.05f))
                                spawnEnemyNearCharger()
                            }
                            OutletType.CRANK_GENERATOR -> {
                                battery = minOf(100, battery + 15)
                                collectibles.remove(c)
                                collectibles.add(Collectible(OutletType.CRANK_GENERATOR,
                                    random.nextFloat() * 0.8f + 0.1f, random.nextFloat() * 0.7f + 0.05f))
                                spawnEnemyNearCharger()
                            }
                        }
                        flashTime = 0.2f
                        updateScore()
                        touchActive = false
                        break
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!touchActive) return true
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY
                val dist = sqrt(dx * dx + dy * dy)
                if (dist > 20f) {
                    val angle = atan2(dy.toDouble(), dx.toDouble())
                    val moveDist = minOf(dist, 40f)
                    val mx = (cos(angle) * moveDist).toFloat()
                    val my = (sin(angle) * moveDist).toFloat()
                    playerX = (playerX + mx).coerceIn(20f, width.toFloat() - 20f)
                    playerY = (playerY + my).coerceIn(20f, height.toFloat() - 20f)
                    touchStartX = event.x
                    touchStartY = event.y

                    val now = System.currentTimeMillis()
                    if (now - lastMoveDrainTime > 300 && battery > 0) {
                        battery = maxOf(0, battery - MOVE_DRAIN)
                        lastMoveDrainTime = now
                        if (battery <= 0) {
                            gameOver = true
                            gameOverTime = 0f
                        }
                        updateScore()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchActive = false
            }
        }
        return true
    }

    private fun updateScore() {
        val timeBonus = (timeElapsed * 10).toInt()
        score = timeBonus + enemiesDefeated * 50
    }

    val gameLoop = object : Runnable {
        override fun run() {
            if (gameOver) {
                gameOverTime += 0.033f
                invalidate()
                postDelayed(this, 33)
                return
            }
            update()
            invalidate()
            postDelayed(this, 33)
        }
    }

    init {
        post(gameLoop)
    }

    private fun update() {
        timeElapsed += 0.033f

        if (timeElapsed >= DAWN_DURATION) {
            gameOver = true
            gameOverTime = 0f
            updateScore()
            return
        }

        if (flashTime > 0f) flashTime -= 0.033f
        if (shakeAmount > 0f) shakeAmount = maxOf(0f, shakeAmount - 0.5f)

        if (flashlightOn && battery > 0) {
            val now = System.currentTimeMillis()
            val elapsed = (now - lastFlashlightDrainTime) / 1000f
            if (elapsed >= 1f && lastFlashlightDrainTime > 0) {
                battery = maxOf(0, battery - FLASHLIGHT_DRAIN_PER_SEC.toInt())
                lastFlashlightDrainTime = now
                if (battery <= 0) {
                    gameOver = true
                    gameOverTime = 0f
                    updateScore()
                    return
                }
            } else if (lastFlashlightDrainTime == 0L) {
                lastFlashlightDrainTime = now
            }
            updateScore()
        }

        val now = System.currentTimeMillis()
        if (now - lastChargeAttractTime > 5000) {
            lastChargeAttractTime = now
            val chargingCollectible = collectibles.firstOrNull { it.active }
            if (chargingCollectible != null) {
                if (random.nextFloat() < 0.4f) {
                    spawnEnemyNearCharger()
                }
            }
        }

        val nx = playerX / maxOf(1f, width.toFloat())
        val ny = playerY / maxOf(1f, height.toFloat())

        for (e in enemies) {
            val dx = nx - e.normX
            val dy = ny - e.normY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > 0.01f) {
                val speed = if (flashlightOn && dist < 0.3f) e.baseSpeed * 1.5f else e.baseSpeed
                e.normX += (dx / dist * speed).coerceIn(-0.015f, 0.015f)
                e.normY += (dy / dist * speed).coerceIn(-0.015f, 0.015f)
            }
            e.normX = e.normX.coerceIn(0.02f, 0.98f)
            e.normY = e.normY.coerceIn(0.02f, 0.98f)

            val ex = e.normX * width
            val ey = e.normY * height
            val pdx = playerX - ex
            val pdy = playerY - ey
            val pdist = sqrt(pdx * pdx + pdy * pdy)
            if (pdist < 35f) {
                gameOver = true
                gameOverTime = 0f
                updateScore()
                return
            }
        }

        if (enemies.size < 4 && random.nextFloat() < 0.02f) {
            spawnEnemies(1)
        }

        for (c in collectibles) {
            val pnx = playerX / maxOf(1f, width.toFloat())
            val pny = playerY / maxOf(1f, height.toFloat())
            val dx = pnx - c.normX
            val dy = pny - c.normY
            val dist = sqrt(dx * dx + dy * dy)

            if (c.active) {
                when (c.type) {
                    OutletType.POWER_OUTLET -> {
                        if (dist < 0.08f && battery < 100) {
                            battery = minOf(100, battery + 8)
                            updateScore()
                        } else if (dist > 0.1f) {
                            c.active = false
                        }
                    }
                    OutletType.SOLAR_PANEL -> {
                        if (dist < 0.1f && battery < 100) {
                            battery = minOf(100, battery + 2)
                            updateScore()
                        } else if (dist > 0.12f) {
                            c.active = false
                        }
                    }
                    OutletType.CRANK_GENERATOR -> {
                        c.active = false
                    }
                }
            }
        }

        updateScore()
        val timeLeft = maxOf(0, (DAWN_DURATION - timeElapsed).toInt())
        hudCallback(battery, timeLeft, score, enemiesDefeated)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        if (battery <= 0 && gameOver) {
            canvas.drawRect(0f, 0f, w, h, darkPaint)
        } else {
            canvas.drawRect(0f, 0f, w, h, darkPaint)
            val gridSize = 50f
            val gridAlpha = if (battery > 30) 40 else maxOf(5, battery * 1.3f).toInt()
            gridPaint.alpha = gridAlpha
            var gx = 0f
            while (gx < w) {
                canvas.drawLine(gx, 0f, gx, h, gridPaint)
                gx += gridSize
            }
            var gy = 0f
            while (gy < h) {
                canvas.drawLine(0f, gy, w, gy, gridPaint)
                gy += gridSize
            }
        }

        val shakeX = if (shakeAmount > 0f) (random.nextFloat() - 0.5f) * shakeAmount * 2f else 0f
        val shakeY = if (shakeAmount > 0f) (random.nextFloat() - 0.5f) * shakeAmount * 2f else 0f

        canvas.save()
        canvas.translate(shakeX, shakeY)

        for (c in collectibles) {
            val cx = c.normX * w
            val cy = c.normY * h
            val glowPaint = when (c.type) {
                OutletType.POWER_OUTLET -> outletGlowPaint
                OutletType.SOLAR_PANEL -> solarGlowPaint
                OutletType.CRANK_GENERATOR -> crankGlowPaint
            }
            val fillPaint = when (c.type) {
                OutletType.POWER_OUTLET -> outletPaint
                OutletType.SOLAR_PANEL -> solarPaint
                OutletType.CRANK_GENERATOR -> crankPaint
            }

            val pulseScale = if (c.active) 1.0f + (sin(timeElapsed * 4f).toFloat() * 0.15f) else 1.0f
            val glowRadius = 35f * pulseScale
            glowPaint.alpha = if (c.active) 120 else 40
            canvas.drawCircle(cx, cy, glowRadius, glowPaint)

            val shapeRadius = 22f
            val drawPaint = fillPaint
            drawPaint.alpha = if (battery <= 0 && gameOver) 30 else 200
            canvas.drawCircle(cx, cy, shapeRadius, drawPaint)

            when (c.type) {
                OutletType.POWER_OUTLET -> {
                    val iconPaint = Paint().apply {
                        color = 0xFF000000.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                        alpha = 100
                    }
                    canvas.drawLine(cx - 8, cy - 6, cx - 8, cy + 6, iconPaint)
                    canvas.drawLine(cx - 8, cy - 6, cx - 2, cy - 6, iconPaint)
                    canvas.drawLine(cx - 8, cy + 6, cx - 2, cy + 6, iconPaint)
                    canvas.drawLine(cx + 2, cy - 2, cx + 8, cy + 2, iconPaint)
                    canvas.drawLine(cx + 2, cy + 2, cx + 8, cy - 2, iconPaint)
                }
                OutletType.SOLAR_PANEL -> {
                    val iconPaint = Paint().apply {
                        color = 0xFF663300.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                        alpha = 120
                    }
                    canvas.drawLine(cx, cy - 12, cx, cy + 12, iconPaint)
                    canvas.drawLine(cx - 12, cy, cx + 12, cy, iconPaint)
                    canvas.drawLine(cx - 8, cy - 8, cx + 8, cy + 8, iconPaint)
                    canvas.drawLine(cx + 8, cy - 8, cx - 8, cy + 8, iconPaint)
                }
                OutletType.CRANK_GENERATOR -> {
                    val iconPaint = Paint().apply {
                        color = 0xFF000022.toInt()
                        style = Paint.Style.STROKE
                        strokeWidth = 2f
                        alpha = 120
                    }
                    canvas.drawCircle(cx, cy, 8f, iconPaint)
                    canvas.drawCircle(cx, cy, 4f, iconPaint)
                }
            }

            val label = when (c.type) {
                OutletType.POWER_OUTLET -> "⚡POWER"
                OutletType.SOLAR_PANEL -> "☀SOLAR"
                OutletType.CRANK_GENERATOR -> "⚙CRANK"
            }
            collectTextPaint.alpha = if (battery <= 0 && gameOver) 30 else 180
            canvas.drawText(label, cx, cy - 34, collectTextPaint)

            if (c.active) {
                val statusPaint = Paint().apply {
                    color = 0xFF44FF44.toInt()
                    textSize = 14f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    alpha = 200
                }
                canvas.drawText("CHARGING", cx, cy + 42, statusPaint)
            }
        }

        val flashAlpha = if (flashTime > 0f) (flashTime / 0.2f * 80).toInt() else 0
        if (flashAlpha > 0) {
            val flashPaint = Paint().apply {
                color = 0xFFFF2222.toInt()
                alpha = flashAlpha
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, w, h, flashPaint)
        }

        for (e in enemies) {
            val ex = e.normX * w
            val ey = e.normY * h

            enemyGlowPaint.alpha = if (battery <= 0 && gameOver) 8 else 60
            canvas.drawCircle(ex, ey, 28f, enemyGlowPaint)

            val alpha = if (battery <= 0 && gameOver) 30 else 220
            enemyPaint.alpha = alpha
            canvas.drawCircle(ex, ey, 16f, enemyPaint)

            val eyePaint = Paint().apply {
                color = 0xFFFF0000.toInt()
                style = Paint.Style.FILL
                alpha = alpha
            }
            canvas.drawCircle(ex - 5, ey - 4, 4f, eyePaint)
            canvas.drawCircle(ex + 5, ey - 4, 4f, eyePaint)

            val pupilPaint = Paint().apply {
                color = 0xFFFFFF00.toInt()
                style = Paint.Style.FILL
                alpha = alpha
            }
            val lookX = playerX - ex
            val lookY = playerY - ey
            val lookDist = maxOf(1f, sqrt(lookX * lookX + lookY * lookY))
            canvas.drawCircle(ex - 5 + (lookX / lookDist) * 1.5f, ey - 4 + (lookY / lookDist) * 1.5f, 1.5f, pupilPaint)
            canvas.drawCircle(ex + 5 + (lookX / lookDist) * 1.5f, ey - 4 + (lookY / lookDist) * 1.5f, 1.5f, pupilPaint)
        }

        if (battery <= 0 && gameOver) {
            playerOffPaint.alpha = 40
            canvas.drawCircle(playerX, playerY, 18f, playerOffPaint)
        } else {
            val glowRadius = if (flashlightOn) 60f else 35f
            playerGlowPaint.alpha = if (battery <= 20) 60 else 100
            canvas.drawCircle(playerX, playerY, glowRadius * (1f + sin(timeElapsed * 3f).toFloat() * 0.1f), playerGlowPaint)

            playerPaint.alpha = 220
            canvas.drawCircle(playerX, playerY, 18f, playerPaint)

            val innerPaint = Paint().apply {
                color = 0xFFFF6666.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawCircle(playerX, playerY, 8f, innerPaint)
        }

        if (flashlightOn && battery > 0 && !gameOver) {
            flashlightPaint.alpha = 60
            canvas.drawCircle(playerX, playerY, 200f, flashlightPaint)
            flashlightPaint.alpha = 30
            canvas.drawCircle(playerX, playerY, 350f, flashlightPaint)
        }

        canvas.restore()

        val barX = 16f
        val barY = 8f
        val barW = w - 32f
        val barH = 20f
        canvas.drawRoundRect(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, 6f, 6f, batteryBarBgPaint)
        val fillW = barW * (battery / 100f)
        val barColor = when {
            battery > 50 -> batteryBarPaint
            battery > 20 -> batteryLowPaint
            else -> Paint().apply { color = 0xFFFF0000.toInt(); style = Paint.Style.FILL }
        }
        canvas.drawRoundRect(barX, barY, barX + fillW, barY + barH, 4f, 4f, barColor)

        val barTextPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 14f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.MONOSPACE
            alpha = 180
        }
        canvas.drawText("$battery%", w / 2f, barY + 16f, barTextPaint)

        if (gameOver) {
            gameOverOverlay.alpha = minOf(180, (gameOverTime * 255f).toInt())
            canvas.drawRect(0f, 0f, w, h, gameOverOverlay)

            val reasonText = if (battery <= 0) "Battery Depleted" else "Dawn Survival Failed"
            textPaint.alpha = minOf(255, (gameOverTime * 300f).toInt())
            smallTextPaint.alpha = minOf(255, (gameOverTime * 300f).toInt())

            canvas.drawText("GAME OVER", w / 2f, h / 2f - 40, textPaint)
            canvas.drawText(reasonText, w / 2f, h / 2f + 10, smallTextPaint)

            if (battery <= 0) {
                val darkText = Paint().apply {
                    color = 0xFF444444.toInt()
                    textSize = 18f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.MONOSPACE
                    alpha = minOf(200, (gameOverTime * 200f).toInt())
                }
                canvas.drawText("Lights go out... Enemies overrun you", w / 2f, h / 2f + 45, darkText)
            }

            val scorePaint = Paint().apply {
                color = 0xFFFF2222.toInt()
                textSize = 22f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
                alpha = minOf(255, (gameOverTime * 300f).toInt())
            }
            canvas.drawText("Score: $score  |  Defeated: $enemiesDefeated", w / 2f, h / 2f + 80, scorePaint)

            val restartPaint = Paint().apply {
                color = 0xFFCCCCCC.toInt()
                textSize = 20f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
                alpha = minOf(180, (gameOverTime * 200f).toInt())
            }
            canvas.drawText("Tap to Restart", w / 2f, h / 2f + 130, restartPaint)
        }
    }

    fun restart() {
        playerX = width / 2f
        playerY = height / 2f
        battery = 100
        timeElapsed = 0f
        flashlightOn = false
        enemiesDefeated = 0
        score = 0
        gameOver = false
        playerMoved = false
        lastMoveDrainTime = 0L
        lastFlashlightDrainTime = 0L
        lastChargeAttractTime = 0L
        flashTime = 0f
        shakeAmount = 0f
        gameOverTime = 0f
        collectibles.clear()
        enemies.clear()
        spawnCollectibles()
        spawnEnemies(4)
        updateScore()
        hudCallback(battery, DAWN_DURATION.toInt(), score, enemiesDefeated)
        invalidate()
    }
}

enum class OutletType { POWER_OUTLET, SOLAR_PANEL, CRANK_GENERATOR }

data class Collectible(
    val type: OutletType,
    val normX: Float,
    val normY: Float,
    var active: Boolean = false
)

data class LastChargeEnemy(
    var normX: Float,
    var normY: Float,
    val baseSpeed: Float = 0.004f + Math.random().toFloat() * 0.003f
)
