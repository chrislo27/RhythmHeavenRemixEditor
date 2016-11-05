package chrislo27.rhre.registry;

import chrislo27.rhre.Main;
import chrislo27.rhre.json.GameObject;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ionium.animation.Animation;
import ionium.registry.handler.IAssetLoader;
import ionium.util.AssetMap;
import ionium.util.BiObjectMap;

import java.util.*;
import java.util.stream.Collectors;

public class GameRegistry {

	private static GameRegistry instance;
	public final BiObjectMap<String, Game> games = new BiObjectMap<>();
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private GameRegistry() {
	}

	public static GameRegistry instance() {
		if (instance == null) {
			instance = new GameRegistry();
			instance.init();
		}

		return instance;
	}

	private void init() {
		final long startTime = System.nanoTime();

		FileHandle gamesList = Gdx.files.internal("data/games.json");
		List<String> games = Arrays.stream(gson.fromJson(gamesList.readString("UTF-8"), String[].class))
				.collect(Collectors.toList());

		for (String gameDef : games) {
			FileHandle gameFh = Gdx.files.internal("sounds/cues/" + gameDef + "/data.json");
			GameObject gameObj = gson.fromJson(gameFh.readString("UTF-8"), GameObject.class);

			Game game;
			List<Pattern> patterns = new ArrayList<>();
			List<SoundCue> soundCues = new ArrayList<>();

			for (GameObject.SoundObject so : gameObj.cues) {
				soundCues.add(new SoundCue(so.id, so.fileExtension, so.name, so.deprecatedIDs == null
						? new ArrayList<>()
						: Arrays.stream(so.deprecatedIDs).collect(Collectors.toList()), so.duration, so.canAlterPitch,
						so.canAlterDuration, so.introSound, so.baseBpm, so.loops));
			}

			for (GameObject.PatternObject po : gameObj.patterns) {
				Pattern p;
				List<Pattern.PatternCue> patternCues = new ArrayList<>();

				for (GameObject.PatternObject.CueObject pc : po.cues) {
					patternCues.add(new Pattern.PatternCue(pc.id, pc.beat, pc.duration, pc.semitone));
				}

				p = new Pattern(po.id, po.name, po.isStretchable, patternCues);
				patterns.add(p);
			}

			game = new Game(gameObj.gameID, gameObj.gameName, soundCues, patterns,
					gameObj.series == null ? Series.UNKNOWN : Series.valueOf(gameObj.series.toUpperCase(Locale.ROOT)));

			Main.logger.info("Loaded " + game.component1() + " with " + game.component3().size() + " cues and " +
					game.component4().size() + " patterns");

			this.games.put(game.component1(), game);
		}

		Main.logger.info("Loaded " + this.games.getAllValues().size + " games, took " +
				((System.nanoTime() - startTime) / 1_000_000.0) + " ms");
	}

	public CueAssetLoader getAssetLoader() {
		return new CueAssetLoader();
	}

	public static class CueAssetLoader implements IAssetLoader {

		private CueAssetLoader() {

		}

		@Override
		public void addManagedAssets(AssetManager manager) {
			GameRegistry.instance().games.getAllValues().forEach(g -> g.component3().forEach(sc -> manager.load
					(AssetMap
							.add("soundCue_" + sc.component1(), "sounds/cues/" + sc.component1() + "." + sc.component2
									()),
					Sound.class)));
		}

		@Override
		public void addUnmanagedTextures(HashMap<String, Texture> textures) {

		}

		@Override
		public void addUnmanagedAnimations(HashMap<String, Animation> animations) {

		}
	}

}
