package chrislo27.rhre.editor

import chrislo27.rhre.EditorScreen
import chrislo27.rhre.Main
import chrislo27.rhre.json.PaletteObject
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.palette.DarkPalette
import chrislo27.rhre.palette.LightPalette
import chrislo27.rhre.palette.PaletteUtils
import chrislo27.rhre.track.PlayingState
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.FileChooser
import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import ionium.registry.AssetRegistry
import ionium.registry.ScreenRegistry
import ionium.stage.Stage
import ionium.stage.ui.ImageButton
import ionium.stage.ui.LocalizationStrategy
import ionium.stage.ui.TextButton
import ionium.stage.ui.skin.Palettes
import ionium.util.MathHelper
import ionium.util.Utils
import ionium.util.i18n.Localization
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JFileChooser

class EditorStageSetup(private val screen: EditorScreen) {

	private val main: Main = screen.main
	var stage: Stage? = null
		private set

	init {
		create()
	}

	private fun create() {
		val palette = Palettes.getIoniumDefault(main.font, main.font)
		stage = Stage(main.camera.viewportWidth, main.camera.viewportHeight)

		run {
			val playRemix = object : ImageButton(stage, palette,
												 AssetRegistry.getAtlasRegion("ionium_ui-icons", "play")) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					screen.editor.remix.playingState = (PlayingState.PLAYING)
				}
			}
			stage!!.addActor<ImageButton>(playRemix)

			playRemix.align(Align.topLeft).setScreenOffset(0.5f, 0f)
					.setPixelOffset((-BUTTON_HEIGHT / 2).toFloat(), PADDING.toFloat(), BUTTON_HEIGHT.toFloat(),
									BUTTON_HEIGHT.toFloat())

			playRemix.color.set(0f, 0.5f, 0.055f, 1f)
		}
		run {
			val pauseRemix = object : ImageButton(stage, palette,
												  AssetRegistry.getAtlasRegion("ionium_ui-icons", "pause")) {

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					if (screen.editor.remix.playingState == PlayingState.PLAYING)
						screen.editor.remix.playingState = (PlayingState.PAUSED)
				}

			}

			pauseRemix.color.set(0.75f, 0.75f, 0.25f, 1f)
			stage!!.addActor<ImageButton>(pauseRemix).align(Align.topLeft).setScreenOffset(0.5f, 0f)
					.setPixelOffset((-BUTTON_HEIGHT / 2 - PADDING - BUTTON_HEIGHT).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(),
									BUTTON_HEIGHT.toFloat())
		}
		run {
			val stopRemix = object : ImageButton(stage, palette,
												 AssetRegistry.getAtlasRegion("ionium_ui-icons", "stop")) {

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					screen.editor.remix.playingState = (PlayingState.STOPPED)
				}

			}

			stopRemix.color.set(242 / 255f, 0.0525f, 0.0525f, 1f)
			stage!!.addActor<ImageButton>(stopRemix).align(Align.topLeft).setScreenOffset(0.5f, 0f)
					.setPixelOffset((BUTTON_HEIGHT / 2 + PADDING).toFloat(), PADDING.toFloat(), BUTTON_HEIGHT.toFloat(),
									BUTTON_HEIGHT.toFloat())
		}

		run {
			val exitGame = object : ImageButton(stage, palette,
												AssetRegistry.getAtlasRegion("ionium_ui-icons", "no")) {

				internal var exitRun = { Gdx.app.exit() }

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					if (screen.editor.remix.playingState !== PlayingState.STOPPED)
						return
				}

			}

			exitGame.color.set(0.85f, 0.25f, 0.25f, 1f)
			exitGame.align(Align.topRight).setPixelOffset(PADDING.toFloat(), PADDING.toFloat(), BUTTON_HEIGHT.toFloat(),
														  BUTTON_HEIGHT.toFloat())
			//			stage.addActor(exitGame);
		}

		run {
			val info = object : ImageButton(stage, palette,
											TextureRegion(AssetRegistry.getTexture("ui_infobutton"))) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.setScreen(ScreenRegistry.get("info"))
				}
			}

			stage!!.addActor<ImageButton>(info).align(Align.topRight).setPixelOffset(PADDING.toFloat(),
																					 PADDING.toFloat(),
																					 BUTTON_HEIGHT.toFloat(),
																					 BUTTON_HEIGHT.toFloat())
		}

		run {
			val lang = object : ImageButton(stage, palette,
											TextureRegion(AssetRegistry.getTexture("ui_language"))) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					Localization.instance().nextLanguage(1)
					Localization.instance().saveToSettings(main.preferences)
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					super.render(batch, alpha)

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.instance().currentBundle.locale.name + "\n" +
								Localization.get("editor.translationsmaynotbeaccurate")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x + this.width + PADDING.toFloat(),
													   this.y - (PADDING * 3).toFloat() - height,
													   -(width + PADDING * 2),
													   height + PADDING * 2)
						main.font.draw(batch, text, this.x + this.width, this.y - PADDING * 2, 0f,
									   Align.right, false)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)
					}
				}
			}

			stage!!.addActor<ImageButton>(lang).align(Align.topRight)
					.setPixelOffset((PADDING * 2 + BUTTON_HEIGHT).toFloat(), PADDING.toFloat(), BUTTON_HEIGHT.toFloat(),
									BUTTON_HEIGHT.toFloat())
		}

		run {
			val fs = object : ImageButton(stage, palette, TextureRegion(AssetRegistry.getTexture("ui_fullscreen"))) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					if (Gdx.graphics.isFullscreen) {
						Gdx.graphics.setWindowedMode(main.preferences.getInteger("width", 1280),
													 main.preferences.getInteger("height", 720))
					} else {
						Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
					}

					main.persistWindowSettings()
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.fullscreen")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x + this.width + PADDING.toFloat(),
													   this.y - (PADDING * 3).toFloat() - height,
													   -(width + PADDING * 2),
													   height + PADDING * 2)
						main.font.draw(batch, text, this.x + this.width, this.y - PADDING * 2, 0f,
									   Align.right, false)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)
					}
				}
			}

			stage!!.addActor(fs).align(Align.topRight)
					.setPixelOffset((PADDING * 3 + BUTTON_HEIGHT * 2).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val reset = object : ImageButton(stage, palette,
											 TextureRegion(AssetRegistry.getTexture("ui_resetwindow"))) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					Gdx.graphics.setWindowedMode(1280, 720)
					main.persistWindowSettings()
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.reset")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x + this.width + PADDING.toFloat(),
													   this.y - (PADDING * 3).toFloat() - height,
													   -(width + PADDING * 2),
													   height + PADDING * 2)
						main.font.draw(batch, text, this.x + this.width, this.y - PADDING * 2, 0f,
									   Align.right, false)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)
					}
				}
			}

			stage!!.addActor(reset).align(Align.topRight)
					.setPixelOffset((PADDING * 4 + BUTTON_HEIGHT * 3).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val interval = object : TextButton(stage, palette, "editor.button.snap") {

				private val intervals = intArrayOf(4, 6, 8, 12, 16, 24, 32)
				private var interval = 0

				init {
					i10NStrategy = object : LocalizationStrategy() {

						override fun get(key: String?, vararg objects: Any): String {
							if (key == null)
								return ""

							return Localization.get(key, "1/" + intervals[interval])
						}

					}
				}

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					interval++

					if (interval >= intervals.size) {
						interval = 0
					}

					screen.editor.snappingInterval = 1f / intervals[interval]
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.button.snapHint")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x - PADDING,
													   this.y - (PADDING * 3).toFloat() - height,
													   width + PADDING * 2, height + PADDING * 2)
						main.font.draw(batch, text, this.x, this.y - PADDING * 2)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)

						if (Utils.isButtonJustPressed(Input.Buttons.RIGHT)) {
							interval = 0
							screen.editor.snappingInterval = 1f / intervals[interval]
						}
					}
				}

			}

			stage!!.addActor<TextButton>(interval).align(Align.topRight)
					.setPixelOffset((PADDING * 5 + BUTTON_HEIGHT * 4).toFloat(), PADDING.toFloat(),
									(BUTTON_HEIGHT * 3).toFloat(), BUTTON_HEIGHT.toFloat())

		}

		run {
			val metronomeFrames: List<TextureRegion> by lazy {
				val tex = AssetRegistry.getTexture("ui_metronome")
				val size = 64
				return@lazy listOf(TextureRegion(tex, size * 2, 0, size, size),
								   TextureRegion(tex, size * 3, 0, size, size),
								   TextureRegion(tex, size * 4, 0, size, size),
								   TextureRegion(tex, size * 3, 0, size, size),
								   TextureRegion(tex, size * 2, 0, size, size),
								   TextureRegion(tex, size, 0, size, size),
								   TextureRegion(tex, 0, 0, size, size),
								   TextureRegion(tex, size, 0, size, size)
								  )
			}

			val metronome = object : ImageButton(stage, palette, metronomeFrames[0]) {

				private var start: Long = 0

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					screen.editor.remix.tickEachBeat = !screen.editor.remix.tickEachBeat
					start = System.currentTimeMillis()
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					if (screen.editor.remix.tickEachBeat) {
						val time = 1.25f
						this.textureRegion = metronomeFrames[(MathHelper.getSawtoothWave(
								System.currentTimeMillis() - start + (time / metronomeFrames.size * 1000).toInt(), time)
								* metronomeFrames.size)
								.toInt().coerceIn(0, metronomeFrames.size - 1)]
					} else {
						this.textureRegion = metronomeFrames[0]
					}

					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)
				}
			}

			stage!!.addActor<ImageButton>(metronome).align(Align.topRight)
					.setPixelOffset((PADDING * 6 + BUTTON_HEIGHT + BUTTON_HEIGHT * 6).toFloat(), PADDING.toFloat(),
									(BUTTON_HEIGHT).toFloat(),
									BUTTON_HEIGHT.toFloat())

		}

		run {
			val music = object : TextButton(stage, palette, "editor.button.music") {

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.setScreen(ScreenRegistry.get("music"))
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.button.muteMusicHint")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x - PADDING,
													   this.y - (PADDING * 3).toFloat() - height,
													   width + PADDING * 2, height + PADDING * 2)
						main.font.draw(batch, text, this.x, this.y - PADDING * 2)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)

						if (Utils.isButtonJustPressed(Input.Buttons.RIGHT)) {
							Remix.muteMusic = !Remix.muteMusic
							this.localizationKey = if (Remix.muteMusic)
								"editor.button.musicMuted"
							else
								"editor.button.music"
						}
					}
				}
			}

			stage!!.addActor<TextButton>(music).align(Align.topRight)
					.setPixelOffset((PADDING * 7 + BUTTON_HEIGHT + BUTTON_HEIGHT * 10).toFloat(), PADDING.toFloat(),
									(BUTTON_HEIGHT * 6 + PADDING * 3).toFloat(),
									BUTTON_HEIGHT.toFloat())

		}

		run {
			val newButton = object : ImageButton(stage, palette,
												 AssetRegistry.getAtlasRegion("ionium_ui-icons", "newFile")) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.setScreen(ScreenRegistry.get("new"))
				}
			}

			newButton.color.set(0.25f, 0.25f, 0.25f, 1f)
			stage!!.addActor<ImageButton>(newButton).align(Align.topLeft)
					.setPixelOffset(PADDING.toFloat(), PADDING.toFloat(), BUTTON_HEIGHT.toFloat(),
									BUTTON_HEIGHT.toFloat())
		}

		run {
			val openButton = object : ImageButton(stage, palette,
												  AssetRegistry.getAtlasRegion("ionium_ui-icons", "openFile")) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.setScreen(ScreenRegistry.get("load"))
				}
			}

			openButton.color.set(0.25f, 0.25f, 0.25f, 1f)
			stage!!.addActor<ImageButton>(openButton).align(Align.topLeft)
					.setPixelOffset((PADDING * 2 + BUTTON_HEIGHT).toFloat(), PADDING.toFloat(), BUTTON_HEIGHT.toFloat(),
									BUTTON_HEIGHT.toFloat())
		}

		run {
			val saveButton = object : ImageButton(stage, palette,
												  AssetRegistry.getAtlasRegion("ionium_ui-icons", "saveFile")) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.screen = ScreenRegistry.get("save")
				}
			}

			saveButton.color.set(0.25f, 0.25f, 0.25f, 1f)
			stage!!.addActor<ImageButton>(saveButton).align(Align.topLeft)
					.setPixelOffset((PADDING * 3 + BUTTON_HEIGHT * 2).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val isMac = System.getProperty("os.name").startsWith("Mac")
			var pathToApp = main.preferences.getString("audacityLocation",
													   if (System.getProperty("os.name").startsWith("Windows"))
														   "C:\\Program Files (x86)\\Audacity\\audacity.exe"
													   else
														   null)
			var file =
					if (pathToApp == null) null else File(pathToApp)

			val audacity = object : ImageButton(stage, palette,
												TextureRegion(AssetRegistry.getTexture("ui_audacity"))) {
				internal val isRunning = AtomicBoolean(false)
				internal val shouldCheckVisibility = AtomicBoolean(true)

				internal val chooser: FileChooser = object : FileChooser() {
					init {
						this.fileSelectionMode = JFileChooser.FILES_ONLY
						this.dialogTitle = "Select your Audacity application"
					}
				}

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					if (!isRunning.get() && file != null && file!!.exists() && file!!.isFile) {
						isRunning.set(true)

						val thread = Thread {
							var process: Process? = null
							try {
								process = Runtime.getRuntime().exec((if (isMac) "open " else "") + pathToApp)

								process!!.waitFor()
							} catch (e: Exception) {
								e.printStackTrace()
							} finally {
								if (process != null)
									process.destroy()
								isRunning.set(false)
							}
						}
						thread.isDaemon = true
						thread.start()
					}
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					super.render(batch, alpha)

					if (shouldCheckVisibility.get()) {
						shouldCheckVisibility.set(false)
						isEnabled = file != null && file!!.exists()
					}

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.button.audacity")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x - PADDING,
													   this.y - (PADDING * 3).toFloat() - height,
													   width + PADDING * 2, height + PADDING * 2)
						main.font.draw(batch, text, this.x, this.y - PADDING * 2)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)

						if (!isRunning.get() && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
							isRunning.set(true)

							val thread = Thread {
								try {
									chooser.currentDirectory = if (file == null || !file!!.exists())
										File(System.getProperty("user.home"), "Desktop")
									else
										file
									chooser.isVisible = true
									val result = chooser.showDialog(null, "Select")

									if (result == JFileChooser.APPROVE_OPTION) {
										file = chooser.selectedFile
										pathToApp = file?.absolutePath
									}
								} catch (e: Exception) {
									e.printStackTrace()
								} finally {
									main.preferences.putString("audacityLocation",
															   file?.absolutePath)
									main.preferences.flush()

									shouldCheckVisibility.set(true)
									isRunning.set(false)
								}
							}
							thread.isDaemon = true
							thread.start()
						}
					}
				}
			}

			stage!!.addActor<ImageButton>(audacity)

			audacity.align(Align.topLeft)
					.setPixelOffset((PADDING * 4 + BUTTON_HEIGHT * 3).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val scripting = object : ImageButton(stage, palette,
												 TextureRegion(AssetRegistry.getTexture("ui_script"))) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.setScreen(ScreenRegistry.get("script"))
				}
			}

			scripting.color.set(0.1f, 0.1f, 0.1f, 1f)
			stage!!.addActor<ImageButton>(scripting).align(Align.topLeft)
					.setPixelOffset((PADDING * 5 + BUTTON_HEIGHT * 4).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val undo = object : ImageButton(stage, palette,
											AssetRegistry.getAtlasRegion("ionium_ui-icons", "back")) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					if (screen.editor.remix.playingState === PlayingState.STOPPED)
						screen.editor.remix.undo()
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					super.render(batch, alpha)

					this.isEnabled = screen.editor.remix.canUndo()

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.undo")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x - PADDING,
													   this.y - (PADDING * 3).toFloat() - height,
													   width + PADDING * 2, height + PADDING * 2)
						main.font.draw(batch, text, this.x, this.y - PADDING * 2)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)
					}
				}
			}

			undo.color.set(0.1f, 0.1f, 0.1f, 1f)
			stage!!.addActor<ImageButton>(undo).align(Align.topLeft)
					.setPixelOffset((PADDING * 6 + BUTTON_HEIGHT * 5).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())

			val tex = TextureRegion(AssetRegistry.getAtlasRegion("ionium_ui-icons", "back"))
			tex.flip(true, false)
			val redo = object : ImageButton(stage, palette, tex) {
				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					if (screen.editor.remix.playingState === PlayingState.STOPPED)
						screen.editor.remix.redo()
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					super.render(batch, alpha)

					this.isEnabled = screen.editor.remix.canRedo()

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.redo")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x - PADDING,
													   this.y - (PADDING * 3).toFloat() - height,
													   width + PADDING * 2, height + PADDING * 2)
						main.font.draw(batch, text, this.x, this.y - PADDING * 2)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)
					}
				}
			}

			redo.color.set(0.1f, 0.1f, 0.1f, 1f)
			stage!!.addActor<ImageButton>(redo).align(Align.topLeft)
					.setPixelOffset((PADDING * 7 + BUTTON_HEIGHT * 6).toFloat(), PADDING.toFloat(),
									BUTTON_HEIGHT.toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val paletteSwap = object : ImageButton(stage, palette,
												   TextureRegion(AssetRegistry.getTexture("ui_palette"))) {
				private val palettes = mutableListOf<AbstractPalette>()
				private val folder: FileHandle by lazy { Gdx.files.local("palettes/") }
				private var num = 0

				private fun buildExamplePalette() {
					val example = folder.child("example.json")

					val po = object : PaletteObject() {
						init {
							val lp = LightPalette()

							editorBg = PaletteUtils.toHex(lp.editorBg)
							staffLine = PaletteUtils.toHex(lp.staffLine)

							soundCue = PaletteUtils.toHex(lp.soundCue.bg)
							stretchableSoundCue = PaletteUtils.toHex(lp.stretchableSoundCue.bg)
							patternCue = PaletteUtils.toHex(lp.pattern.bg)
							stretchablePatternCue = PaletteUtils.toHex(lp.stretchablePattern.bg)

							selectionCueTint = PaletteUtils.toHex(lp.selectionTint)

							selectionBg = PaletteUtils.toHex(lp.selectionFill)
							selectionBorder = PaletteUtils.toHex(lp.selectionBorder)

							beatTracker = PaletteUtils.toHex(lp.beatTracker)
							bpmTracker = PaletteUtils.toHex(lp.bpmTracker)
							bpmTrackerSelected = PaletteUtils.toHex(lp.bpmTrackerSelected)
							musicStartTracker = PaletteUtils.toHex(lp.musicStartTracker)

							cueText = PaletteUtils.toHex(lp.cueText)
						}
					}

					example.writeString(JsonHandler.toJson(po, PaletteObject::class.java), false, "UTF-8")
				}

				init {
					initialize()
				}

				private fun initialize() {
					palettes.clear()
					palettes.add(LightPalette())
					palettes.add(DarkPalette())
					palettes.add(PaletteUtils.getRHRE0Palette())

					if (!folder.exists()) {
						folder.mkdirs()
					}

					buildExamplePalette()

					if (folder.exists() && folder.isDirectory) {
						val list = folder.list { dir, name ->
							name != "example.json" && name.toLowerCase(Locale.ROOT).endsWith(".json")
						}
						ionium.templates.Main.logger.info("Found " + list.size + " palette files")

						Arrays.stream(list).forEach { fh ->
							ionium.templates.Main.logger.info("Loading palette " + fh.name())

							palettes.add(PaletteUtils.getPaletteFromObject(
									JsonHandler.fromJson(fh.readString("UTF-8"), PaletteObject::class.java)))

							ionium.templates.Main.logger.info("Loaded palette " + fh.name())
						}
					}

					num = main.preferences.getInteger("palette", 0)
					if (num >= palettes.size)
						num = 0

					cycle()
				}

				private fun cycle(speed: Float = 0.35f) {
					main.switchPalette(palettes[num], speed)

					main.preferences.putInteger("palette", num)
					main.preferences.flush()

					num++
					if (num >= palettes.size)
						num = 0
				}

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					cycle()
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)

					if (this.stage.isMouseOver(this)) {
						main.font.data.setScale(0.5f)

						val text = Localization.get("editor.reloadPalettes")
						val width = Utils.getWidth(main.font, text)
						val height = Utils.getHeight(main.font, text)

						batch.setColor(0f, 0f, 0f, 0.75f)
						ionium.templates.Main.fillRect(batch, this.x - PADDING,
													   this.y - (PADDING * 3).toFloat() - height,
													   width + PADDING * 2, height + PADDING * 2)
						main.font.draw(batch, text, this.x, this.y - PADDING * 2)
						main.font.data.setScale(1f)
						batch.setColor(1f, 1f, 1f, 1f)

						if (Utils.isButtonJustPressed(Input.Buttons.RIGHT)) {
							initialize()
							cycle(0.075f)
						}
					}
				}
			}

			stage!!.addActor<ImageButton>(paletteSwap).align(Align.topLeft)
					.setPixelOffset((PADDING * 8 + BUTTON_HEIGHT * 7).toFloat(), PADDING.toFloat(),
									(BUTTON_HEIGHT).toFloat(), BUTTON_HEIGHT.toFloat())
		}

		run {
			val inspections = object : ImageButton(stage, palette, TextureRegion(AssetRegistry.getTexture("ui_inspections"))) {
				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)
				}

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.screen = ScreenRegistry.get("inspections")
				}
			}

			stage!!.addActor(inspections).align(Align.topLeft)
					.setPixelOffset((PADDING * 9 + BUTTON_HEIGHT * 8).toFloat(), PADDING.toFloat(),
									(BUTTON_HEIGHT).toFloat(), BUTTON_HEIGHT.toFloat())

		}

		run {
			val tapalong = object : TextButton(stage, palette, "editor.button.tapalong") {

				override fun onClickAction(x: Float, y: Float) {
					super.onClickAction(x, y)

					main.screen = ScreenRegistry.get("tapalong")
				}

				override fun render(batch: SpriteBatch, alpha: Float) {
					getPalette().labelFont.data.setScale(0.5f)
					super.render(batch, alpha)
					getPalette().labelFont.data.setScale(1f)
				}
			}

			stage!!.addActor<TextButton>(tapalong).align(Align.topLeft)
					.setPixelOffset((PADDING * 9 + BUTTON_HEIGHT * 12).toFloat(), PADDING.toFloat(),
									(BUTTON_HEIGHT * 5 + PADDING).toFloat(), BUTTON_HEIGHT.toFloat())
		}
	}

	companion object {

		@JvmField
		val BUTTON_HEIGHT = 32
		@JvmField
		val PADDING = 4
		@JvmField
		val BAR_HEIGHT = BUTTON_HEIGHT + PADDING * 2

	}

}
