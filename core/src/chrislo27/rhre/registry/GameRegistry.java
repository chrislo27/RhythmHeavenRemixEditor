package chrislo27.rhre.registry;

import chrislo27.rhre.Main;
import chrislo27.rhre.editor.Editor;
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

import java.util.*;
import java.util.stream.Collectors;

public class GameRegistry {

	private static GameRegistry instance;
	public final Map<String, Game> games = new LinkedHashMap<>();
	public final List<Game> gameList = new ArrayList<>();
	public final Map<Series, List<Game>> gamesBySeries = new LinkedHashMap<>();
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

	public Game get(String id) {
		return games.get(id);
	}

	public SoundCue getCue(String id) {
		return games.values().stream().map(g -> g.getCue(id)).filter(Objects::nonNull).findFirst().orElse(null);
	}

	public SoundCue getCueRaw(String id) {
		return games.values().stream()
				.map(g -> g.getSoundCues().stream().filter(it -> it.getId().equals(id)).findFirst().orElse(null))
				.filter(Objects::nonNull).findFirst().orElse(null);
	}

	private void init() {
		final long startTime = System.nanoTime();

		FileHandle gamesList = Gdx.files.internal("data/games.json");
		List<String> games = Arrays.stream(gson.fromJson(gamesList.readString("UTF-8"), String[].class))
				.collect(Collectors.toList());
		Map<Series, Integer> numberPerSeries = new HashMap<>();

		for (String gameDef : games) {
			Main.logger.info("Loading " + gameDef);
			FileHandle gameFh = Gdx.files.internal("sounds/cues/" + gameDef + "/data.json");
			GameObject gameObj = gson.fromJson(gameFh.readString("UTF-8"), GameObject.class);

			Game game;
			List<Pattern> patterns = new ArrayList<>();
			List<SoundCue> soundCues = new ArrayList<>();

			for (GameObject.SoundObject so : gameObj.cues) {
				soundCues.add(new SoundCue(so.id, so.fileExtension, so.name == null ? so.id : so.name,
						so.deprecatedIDs == null
								? new ArrayList<>()
								: Arrays.stream(so.deprecatedIDs).collect(Collectors.toList()), so.duration,
						so.canAlterPitch, so.canAlterDuration, so.introSound, so.baseBpm, so.loops));
			}

			for (GameObject.PatternObject po : gameObj.patterns) {
				Pattern p;
				List<Pattern.PatternCue> patternCues = new ArrayList<>();

				for (GameObject.PatternObject.CueObject pc : po.cues) {
					patternCues.add(new Pattern.PatternCue(pc.id, pc.beat, pc.track, pc.duration, pc.semitone));
				}

				p = new Pattern(po.id, po.name, po.isStretchable, patternCues, false);
				patterns.add(p);
			}

			soundCues.stream().filter(sc -> soundCues.stream().noneMatch(
					sc2 -> sc == sc2 && sc2.getIntroSound() != null && sc2.getIntroSound().equals(sc.getId())))
					.forEach(sc -> {
						List<Pattern.PatternCue> l = new ArrayList<>();

						l.add(new Pattern.PatternCue(sc.getId(), 0, 0, sc.getDuration(), 0));

						patterns.add(new Pattern(sc.getId() + "_AUTO-GENERATED", "cue: " + sc.getName(),
								sc.getCanAlterDuration(), l, true));
					});

			FileHandle iconFh = Gdx.files.internal("sounds/cues/" + gameDef + "/icon.png");

			game = new Game(gameObj.gameID, gameObj.gameName, soundCues, patterns,
					gameObj.series == null ? Series.UNKNOWN : Series.valueOf(gameObj.series.toUpperCase(Locale.ROOT)),
					iconFh.exists() ? ("sounds/cues/" + gameDef + "/icon.png") : null);

			if (!iconFh.exists())
				Main.logger.warn(game.getId() + " is missing icon.png");

			Main.logger.info("Loaded " + game.getId() + " with " + game.getSoundCues().size() + " cues and " +
					game.getPatterns().size() + " patterns");

			this.games.put(game.getId(), game);
			{
				List<Game> seriesList = this.gamesBySeries.getOrDefault(game.getSeries(), new ArrayList<>());
				seriesList.add(game);
				this.gamesBySeries.put(game.getSeries(), seriesList);
			}
			this.gameList.add(game);
			numberPerSeries.put(game.getSeries(), numberPerSeries.getOrDefault(game.getSeries(), 0) + 1);
		}

		numberPerSeries.entrySet().stream().filter(e -> e.getValue() > Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y)
				.forEach(e -> {
					Main.logger.warn("");
					Main.logger
							.warn("Series " + e.getKey().toString() + " has " + e.getValue() + " games, maximum is " +
									(Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y));
					Main.logger.warn("");
				});

		Main.logger.info("Loaded " + this.games.size() + " games, took " +
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
			GameRegistry.instance().games.values().forEach(g -> g.getSoundCues().forEach(sc -> manager.load(AssetMap
							.add("soundCue_" + sc.getId(), "sounds/cues/" + sc.getId() + "." + sc.getFileExtension()),
					Sound.class)));
			GameRegistry.instance().games.values().forEach(g -> manager.load(AssetMap.add("gameIcon_" + g.getId(),
					g.getIcon() == null
							? ("images/missing_game_icon.png")
							: ("sounds/cues/" + g.getId() + "/icon.png")), Texture.class));
		}

		@Override
		public void addUnmanagedTextures(HashMap<String, Texture> textures) {

		}

		@Override
		public void addUnmanagedAnimations(HashMap<String, Animation> animations) {

		}
	}

}
