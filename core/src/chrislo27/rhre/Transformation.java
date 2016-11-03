package chrislo27.rhre;

import chrislo27.rhre.json.GameObject;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import ionium.registry.AssetRegistry;
import ionium.util.BiObjectMap;

public class Transformation {

	public BiObjectMap<String, Cue> cues = new BiObjectMap<>();
	private Gson gson = new Gson();

	public Transformation() {
		doThing();
	}

	private void print(Game game) {
		System.out.println(game.name);

		GameObject obj = new GameObject();
		obj.gameID = game.name;
		obj.gameName = game.name;
		obj.patterns = new GameObject.PatternObject[0];

		Array<GameObject.SoundObject> soundObjects = new Array<>();

		for (Cue cue : cues.getAllValues()) {
			GameObject.SoundObject so = new GameObject.SoundObject();

			so.id = game.name + "/" + cue.file;
			so.duration = cue.duration;
			so.canAlterDuration = cue.canAlterDuration;
			so.canAlterPitch = cue.canAlterPitch;
			so.loops = cue.soundLoops;
			so.name = "";
			so.introSound = cue.oneTimeSound;
			if (cue.pitchWithBpm > 0) {
				so.baseBpm = cue.pitchWithBpm;
			}

			soundObjects.add(so);
		}

		obj.cues = soundObjects.toArray(GameObject.SoundObject.class);

		System.out.println(gson.toJson(obj) + "\n\n");

		cues = new BiObjectMap<>();
	}

	public void doThing() {
		{
			Game ci = GameList.getGame("countIn");

			put(new Cue(ci, ci.name, "silence", 1f).setCanAlterDuration(true));
			put(new Cue(ci, ci.name, "and", 0.5f));
			put(new Cue(ci, ci.name, "cowbell", 0.5f));
			put(new Cue(ci, ci.name, "one", 0.5f));
			put(new Cue(ci, ci.name, "two", 0.5f));
			put(new Cue(ci, ci.name, "three", 0.5f));
			put(new Cue(ci, ci.name, "four", 0.5f));

			print(ci);
		}

		{
			Game ls = GameList.getGame("lockstep");

			put(new Cue(ls, ls.name, "march1", 0.5f));
			put(new Cue(ls, ls.name, "march2", 0.5f));
			put(new Cue(ls, ls.name, "march1_bkbt", 1 / 3f));
			put(new Cue(ls, ls.name, "march2_bkbt", 1 / 3f));
			put(new Cue(ls, ls.name, "hai", 0.5f));
			put(new Cue(ls, ls.name, "bkbt_ha", 0.5f));
			put(new Cue(ls, ls.name, "bkbt_hoi", 1 / 3f));
			put(new Cue(ls, ls.name, "bkbt_boh", 1 / 3f));
			put(new Cue(ls, ls.name, "return_hee", 0.5f));
			put(new Cue(ls, ls.name, "return_ha", 1 / 3f));
			put(new Cue(ls, ls.name, "corruptHai", 0.5f));
			print(ls);
		}

		{
			Game mm = GameList.getGame("munchyMonk");

			put(new Cue(mm, mm.name, "gulp", 3 / 8f));
			put(new Cue(mm, mm.name, "gulp3-1", 0.5f));
			put(new Cue(mm, mm.name, "gulp3-2", 0.5f));
			put(new Cue(mm, mm.name, "gulp3-3", 0.5f));
			put(new Cue(mm, mm.name, "one", 0.5f));
			put(new Cue(mm, mm.name, "three", 0.5f));
			put(new Cue(mm, mm.name, "try", 3 / 8f));
			put(new Cue(mm, mm.name, "two", 0.5f));
			print(mm);
		}

		{
			Game dd = GameList.getGame("donkDonk");
			float third = 1f / 3f;

			put(new Cue(dd, dd.name, "blastoff", 2f));
			put(new Cue(dd, dd.name, "deetdeetdoot1", third * 2));
			put(new Cue(dd, dd.name, "deetdeetdoot2", third * 2));
			put(new Cue(dd, dd.name, "deetdeetdoot3", third * 2));
			put(new Cue(dd, dd.name, "deetdeetduh1", third * 2));
			put(new Cue(dd, dd.name, "deetdeetduh2", third * 2));
			put(new Cue(dd, dd.name, "deetdeetduh3", third * 2));
			put(new Cue(dd, dd.name, "donk1", third * 2));
			put(new Cue(dd, dd.name, "donk2", third * 2));
			put(new Cue(dd, dd.name, "dwonk1", third * 2));
			put(new Cue(dd, dd.name, "dwonk2", third * 2));
			print(dd);
		}

		{
			Game tt = GameList.getGame("tapTroupe");
			float dottedEighth = 0.75f;

			put(new Cue(tt, tt.name, "and", 0.5f));
			put(new Cue(tt, tt.name, "bombom1", 0.75f));
			put(new Cue(tt, tt.name, "bombom2", 0.75f));
			put(new Cue(tt, tt.name, "ready1", 0.5f));
			put(new Cue(tt, tt.name, "ready2", 0.5f));
			put(new Cue(tt, tt.name, "step1", 0.5f));
			put(new Cue(tt, tt.name, "step2", 0.5f));
			put(new Cue(tt, tt.name, "tapNook", 0.75f), "taptaptap3");
			put(new Cue(tt, tt.name, "taptaptap1", 0.75f));
			put(new Cue(tt, tt.name, "taptaptap2", 0.75f));
			print(tt);
		}

		{
			Game tt = GameList.getGame("tapTrial");

			put(new Cue(tt, tt.name, "jumptap1_tap", 0.5f));
			put(new Cue(tt, tt.name, "jumptap1", 0.5f));
			put(new Cue(tt, tt.name, "jumptap2_tap", 0.5f));
			put(new Cue(tt, tt.name, "jumptap2", 0.5f));
			put(new Cue(tt, tt.name, "ook", 0.5f));
			put(new Cue(tt, tt.name, "ooki1", 0.5f));
			put(new Cue(tt, tt.name, "ooki2", 0.5f));
			put(new Cue(tt, tt.name, "ookook1", 0.5f));
			put(new Cue(tt, tt.name, "ookook2", 0.5f));
			put(new Cue(tt, tt.name, "tap", 0.5f));
			print(tt);
		}

		{
			Game sd = GameList.getGame("spaceDance");

			put(new Cue(sd, sd.name, "turn1", 0.5f));
			put(new Cue(sd, sd.name, "right1", 0.5f));
			put(new Cue(sd, sd.name, "lets1", 0.5f));
			put(new Cue(sd, sd.name, "sit1", 1 / 3f));
			put(new Cue(sd, sd.name, "down1", 0.5f));
			put(new Cue(sd, sd.name, "pa1", 1 / 3f));
			put(new Cue(sd, sd.name, "punch1", 1.5f));
			put(new Cue(sd, sd.name, "turn2", 0.5f));
			put(new Cue(sd, sd.name, "right2", 0.5f));
			put(new Cue(sd, sd.name, "lets2", 0.5f));
			put(new Cue(sd, sd.name, "sit2", 1 / 3f));
			put(new Cue(sd, sd.name, "down2", 0.5f));
			put(new Cue(sd, sd.name, "pa2", 1 / 3f));
			put(new Cue(sd, sd.name, "punch2", 1.5f));
			print(sd);
		}

		{
			Game bb = GameList.getGame("blueBirds");

			put(new Cue(bb, bb.name, "peck", 0.5f));
			put(new Cue(bb, bb.name, "peckyourbeak1", 0.5f));
			put(new Cue(bb, bb.name, "peckyourbeak2", 0.5f));
			put(new Cue(bb, bb.name, "peckyourbeak3", 0.5f));
			put(new Cue(bb, bb.name, "stretch1", 0.5f));
			put(new Cue(bb, bb.name, "stretch2", 0.5f));
			put(new Cue(bb, bb.name, "stretchoutyourneck1", 4 / 6f));
			put(new Cue(bb, bb.name, "stretchoutyourneck2", 0.5f));
			put(new Cue(bb, bb.name, "stretchoutyourneck3", 0.5f));
			put(new Cue(bb, bb.name, "stretchoutyourneck4", 0.4f));
			print(bb);
		}

		{
			Game cs = GameList.getGame("cropStomp");

			put(new Cue(cs, cs.name, "molefling", 0.5f));
			put(new Cue(cs, cs.name, "pick1", 0.5f));
			put(new Cue(cs, cs.name, "pick2", 0.5f));
			put(new Cue(cs, cs.name, "stomp", 0.5f));
			put(new Cue(cs, cs.name, "walk", 0.5f));
			print(cs);
		}

		{
			Game rs = GameList.getGame("ringside");
			put(new Cue(rs, rs.name, "wubba1-1", 0.25f));
			put(new Cue(rs, rs.name, "wubba1-2", 0.25f));
			put(new Cue(rs, rs.name, "dubba1-1", 0.25f));
			put(new Cue(rs, rs.name, "dubba1-2", 0.25f));
			put(new Cue(rs, rs.name, "dubba1-3", 0.25f));
			put(new Cue(rs, rs.name, "dubba1-4", 0.25f));
			put(new Cue(rs, rs.name, "that1", 0.5f));
			put(new Cue(rs, rs.name, "true1", 0.5f));
			put(new Cue(rs, rs.name, "woah1", 0.5f));
			put(new Cue(rs, rs.name, "you1", 0.25f));
			put(new Cue(rs, rs.name, "go1", 0.5f));
			put(new Cue(rs, rs.name, "big1", 0.5f));
			put(new Cue(rs, rs.name, "guy1", 0.5f));
			put(new Cue(rs, rs.name, "pose1", 0.5f));
			put(new Cue(rs, rs.name, "for1", 1 / 6f));
			put(new Cue(rs, rs.name, "the1", 0.25f));
			put(new Cue(rs, rs.name, "fans1", 0.5f));
			put(new Cue(rs, rs.name, "wubba2-1", 0.25f));
			put(new Cue(rs, rs.name, "wubba2-2", 0.25f));
			put(new Cue(rs, rs.name, "dubba2-1", 0.25f));
			put(new Cue(rs, rs.name, "dubba2-2", 0.25f));
			put(new Cue(rs, rs.name, "dubba2-3", 0.25f));
			put(new Cue(rs, rs.name, "dubba2-4", 0.25f));
			put(new Cue(rs, rs.name, "that2", 0.5f));
			put(new Cue(rs, rs.name, "true2", 0.5f));
			put(new Cue(rs, rs.name, "woah2", 0.5f));
			put(new Cue(rs, rs.name, "you2", 0.25f));
			put(new Cue(rs, rs.name, "go2", 0.5f));
			put(new Cue(rs, rs.name, "big2", 0.5f));
			put(new Cue(rs, rs.name, "guy2", 0.5f));
			put(new Cue(rs, rs.name, "pose2", 0.5f));
			put(new Cue(rs, rs.name, "for2", 1 / 6f));
			put(new Cue(rs, rs.name, "the2", 0.25f));
			put(new Cue(rs, rs.name, "fans2", 0.5f));
			put(new Cue(rs, rs.name, "wubba3-1", 0.25f));
			put(new Cue(rs, rs.name, "wubba3-2", 0.25f));
			put(new Cue(rs, rs.name, "dubba3-1", 0.25f));
			put(new Cue(rs, rs.name, "dubba3-2", 0.25f));
			put(new Cue(rs, rs.name, "dubba3-3", 0.25f));
			put(new Cue(rs, rs.name, "dubba3-4", 0.25f));
			put(new Cue(rs, rs.name, "that3", 0.5f));
			put(new Cue(rs, rs.name, "true3", 0.5f));
			put(new Cue(rs, rs.name, "woah3", 0.5f));
			put(new Cue(rs, rs.name, "you3", 0.25f));
			put(new Cue(rs, rs.name, "go3", 0.5f));
			put(new Cue(rs, rs.name, "big3", 0.5f));
			put(new Cue(rs, rs.name, "guy3", 0.5f));
			put(new Cue(rs, rs.name, "pose3", 0.5f));
			put(new Cue(rs, rs.name, "for3", 1 / 6f));
			put(new Cue(rs, rs.name, "the3", 0.25f));
			put(new Cue(rs, rs.name, "fans3", 0.5f));
			put(new Cue(rs, rs.name, "poseAnd", 0.5f));
			put(new Cue(rs, rs.name, "ye", 1));
			put(new Cue(rs, rs.name, "yell1", 0.5f));
			put(new Cue(rs, rs.name, "yell2", 0.5f));
			put(new Cue(rs, rs.name, "yell3", 0.5f));
			put(new Cue(rs, rs.name, "camera1", 1));
			put(new Cue(rs, rs.name, "camera2", 1));
			put(new Cue(rs, rs.name, "camera3", 1));
			put(new Cue(rs, rs.name, "muscles1", 0.5f));
			put(new Cue(rs, rs.name, "muscles2", 0.5f));
			print(rs);
		}

		{
			Game shoot = GameList.getGame("shootEmUp");

			put(new Cue(shoot, shoot.name, "spawn", 0.25f));
			put(new Cue(shoot, shoot.name, "shoot", 0.25f));
			put(new Cue(shoot, shoot.name, "commStart", 1));
			put(new Cue(shoot, shoot.name, "commEnd", 1));
			print(shoot);
		}

		{
			Game wd = GameList.getGame("workingDough");

			put(new Cue(wd, wd.name, "leftSmall", 0.25f));
			put(new Cue(wd, wd.name, "leftBig", 0.25f));
			put(new Cue(wd, wd.name, "rightSmall", 0.25f));
			put(new Cue(wd, wd.name, "rightBig", 0.25f));
			print(wd);
		}

		{
			Game mdw = GameList.getGame("moaiDooWop");

			Cue d1 = new Cue(mdw, mdw.name, "d1", 0.125f).hideFromList();
			put(new Cue(mdw, mdw.name, "ooo1", 1).setCanAlterDuration(true).setOneTimeSound(d1.soundId));
			put(new Cue(mdw, mdw.name, "wop1", 0.5f));
			put(new Cue(mdw, mdw.name, "pah1", 0.5f));

			Cue d2 = new Cue(mdw, mdw.name, "d2", 0.125f).hideFromList();
			put(new Cue(mdw, mdw.name, "ooo2", 1).setCanAlterDuration(true).setOneTimeSound(d2.soundId));
			put(new Cue(mdw, mdw.name, "wop2", 0.5f));
			put(new Cue(mdw, mdw.name, "pah2", 0.5f));

			put(new Cue(mdw, mdw.name, "stoneGrind", 0.5f));

			put(d1);
			put(d2);
			print(mdw);
		}

		{
			Game dn = GameList.getGame("dogNinja");

			put(new Cue(dn, dn.name, "bone1", 0.5f));
			put(new Cue(dn, dn.name, "bone2", 0.5f));
			put(new Cue(dn, dn.name, "fruit1", 0.5f));
			put(new Cue(dn, dn.name, "fruit2", 0.5f));
			put(new Cue(dn, dn.name, "pan1", 0.5f));
			put(new Cue(dn, dn.name, "pan2", 0.5f));
			put(new Cue(dn, dn.name, "tire1", 0.5f));
			put(new Cue(dn, dn.name, "tire2", 0.5f));
			print(dn);
		}

		{
			Game ss = GameList.getGame("shrimpShuffle");

			put(new Cue(ss, ss.name, "to", 1 / 3f));
			put(new Cue(ss, ss.name, "ge", 2 / 3f));
			put(new Cue(ss, ss.name, "ther", 0.5f));

			put(new Cue(ss, ss.name, "one1", 0.5f));
			put(new Cue(ss, ss.name, "two1", 0.5f));
			put(new Cue(ss, ss.name, "three1", 0.5f));

			put(new Cue(ss, ss.name, "three2", 0.5f));
			put(new Cue(ss, ss.name, "two2", 0.5f));
			put(new Cue(ss, ss.name, "one2", 0.5f));

			put(new Cue(ss, ss.name, "a", 0.5f));
			put(new Cue(ss, ss.name, "b", 0.5f));
			put(new Cue(ss, ss.name, "c", 0.5f));

			put(new Cue(ss, ss.name, "ah", 0.25f));
			put(new Cue(ss, ss.name, "wa", 0.5f));
			put(new Cue(ss, ss.name, "ha", 0.5f));
			put(new Cue(ss, ss.name, "uhn", 0.5f));
			print(ss);
		}

		{
			Game km = GameList.getGame("karateMan");

			put(new Cue(km, km.name, "barrelout", 0.5f));
			put(new Cue(km, km.name, "bulbhit", 0.5f));
			put(new Cue(km, km.name, "bulbout", 0.5f));
			put(new Cue(km, km.name, "hit3cue1", 0.5f));
			put(new Cue(km, km.name, "hit3cue2", 0.5f));
			put(new Cue(km, km.name, "offbeatpotout", 0.5f));
			put(new Cue(km, km.name, "pothit", 0.5f));
			put(new Cue(km, km.name, "potout", 0.5f));
			put(new Cue(km, km.name, "punchkick1", 0.5f));
			put(new Cue(km, km.name, "punchkick2", 0.5f));
			put(new Cue(km, km.name, "hit4", 0.5f));
			put(new Cue(km, km.name, "rock", 0.5f));
			put(new Cue(km, km.name, "punchy1", 0.25f));
			put(new Cue(km, km.name, "punchy2", 0.25f));
			put(new Cue(km, km.name, "punchy3", 0.25f));
			put(new Cue(km, km.name, "punchy4", 0.25f));
			put(new Cue(km, km.name, "ko", 0.5f));
			put(new Cue(km, km.name, "pow", 0.5f));
			print(km);
		}

		{
			Game rr = GameList.getGame("rhythmRally");

			put(new Cue(rr, rr.name, "hit1", 0.5f));
			put(new Cue(rr, rr.name, "hit2", 0.5f));
			put(new Cue(rr, rr.name, "hit3", 0.5f));
			put(new Cue(rr, rr.name, "tonk", 0.5f));
			put(new Cue(rr, rr.name, "tink", 0.5f));
			put(new Cue(rr, rr.name, "whistle", 0.5f));
			print(rr);
		}

		{
			Game ss = GameList.getGame("spaceSoccer");

			put(new Cue(ss, ss.name, "kick", 0.5f));
			put(new Cue(ss, ss.name, "highkicklow1", 0.5f));
			put(new Cue(ss, ss.name, "highkicklow2", 0.5f));
			put(new Cue(ss, ss.name, "highkicklow3", 0.5f));
			put(new Cue(ss, ss.name, "dispense1", 0.75f));
			put(new Cue(ss, ss.name, "dispense2", 0.25f));
			put(new Cue(ss, ss.name, "dispense3", 0.25f));
			put(new Cue(ss, ss.name, "dispense4", 0.25f));
			put(new Cue(ss, ss.name, "dispense5", 0.5f));
			print(ss);
		}

		{
			Game mr = GameList.getGame("microRow");

			put(new Cue(mr, mr.name, "go", 3 / 8f));
			put(new Cue(mr, mr.name, "ding", 0.5f));
			put(new Cue(mr, mr.name, "dash1", 3 / 8f));
			put(new Cue(mr, mr.name, "dash2", 5 / 8f));
			put(new Cue(mr, mr.name, "triple1", 0.5f));
			put(new Cue(mr, mr.name, "triple2", 0.5f));
			put(new Cue(mr, mr.name, "triple3", 0.5f));
			print(mr);
		}

		{
			Game ct = GameList.getGame("clappyTrio");

			put(new Cue(ct, ct.name, "clap", 0.25f));
			put(new Cue(ct, ct.name, "ready", 0.25f));
			print(ct);
		}

		{
			Game ww = GameList.getGame("wizardWaltz");

			put(new Cue(ww, ww.name, "grow", 0.5f));
			put(new Cue(ww, ww.name, "plant", 0.5f));
			print(ww);
		}

		{
			Game vp = GameList.getGame("rhythmTweezers");

			put(new Cue(vp, vp.name, "appear", 0.25f));
			put(new Cue(vp, vp.name, "longAppear", 0.25f));
			put(new Cue(vp, vp.name, "pluck", 0.25f));
			Cue gr = new Cue(vp, vp.name, "gr", 0.125f).hideFromList();
			put(new Cue(vp, vp.name, "aaa", 0.5f).setSoundLoops(true).setOneTimeSound(gr.soundId));
			put(new Cue(vp, vp.name, "b", 0.5f));
			put(new Cue(vp, vp.name, "cashier", 1));
			put(gr);
			print(vp);
		}

		{
			Game gc = GameList.getGame("gleeClub");

			Cue start = new Cue(gc, gc.name, "singBegin", 0.125f).hideFromList();

			put(new Cue(gc, gc.name, "singLoop", 1).setCanAlterDuration(true).setOneTimeSound(start.soundId)
					.setCanAlterPitch(true));
			put(new Cue(gc, gc.name, "singEnd", 0.5f));
			put(start);
			print(gc);
		}

		{
			Game fb = GameList.getGame("fillbots");

			put(new Cue(fb, fb.name, "mediumFall", 0.5f));
			put(new Cue(fb, fb.name, "bigFall", 0.5f));
			put(new Cue(fb, fb.name, "smallFall", 0.5f));
			put(new Cue(fb, fb.name, "mediumBot", 6).setUsesPitch(105));
			put(new Cue(fb, fb.name, "bigBot", 9).setUsesPitch(105));
			put(new Cue(fb, fb.name, "smallBot", 3).setUsesPitch(105));
			print(fb);
		}

		{
			Game bts = GameList.getGame("builtToScaleDS");

			put(new Cue(bts, bts.name, "c", 0.25f).setCanAlterPitch(true));
			put(new Cue(bts, bts.name, "pew", 0.5f));
			print(bts);
		}

		{
			Game hr = GameList.getGame("bouncyRoad");

			put(new Cue(hr, hr.name, "tink", 0.25f).setCanAlterPitch(true));
			put(new Cue(hr, hr.name, "cymbal", 0.5f).setCanAlterPitch(true));
			put(new Cue(hr, hr.name, "spySnort", 0.25f).setCanAlterPitch(true));
			print(hr);
		}

		{
			Game ll = GameList.getGame("loveLizards");

			put(new Cue(ll, ll.name, "bzzt1", 0.5f));
			put(new Cue(ll, ll.name, "bzzt2", 0.5f));
			put(new Cue(ll, ll.name, "bzzt3", 0.5f));
			put(new Cue(ll, ll.name, "bzzt4", 0.5f));

			put(new Cue(ll, ll.name, "happy", 0.5f));
			print(ll);
		}

		{
			Game em = GameList.getGame("exhibitionMatch");

			put(new Cue(em, em.name, "stance", 0.5f));
			put(new Cue(em, em.name, "throw", 0.5f));
			put(new Cue(em, em.name, "hit", 1));
			put(new Cue(em, em.name, "hitHomerun", 2));
			print(em);
		}

		{
			Game lp = GameList.getGame("launchParty");

			put(new Cue(lp, lp.name, "one", 0.5f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "one-go", 1).setCanAlterPitch(true));

			put(new Cue(lp, lp.name, "three-3", 0.5f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "three-2", 0.5f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "three-1", 0.5f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "three-go", 1).setCanAlterPitch(true));

			put(new Cue(lp, lp.name, "five-5", 0.5f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "five-4", 1 / 3f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "five-3", 1 / 3f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "five-2", 1 / 3f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "five-1", 1 / 3f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "five-go", 1).setCanAlterPitch(true));

			put(new Cue(lp, lp.name, "seven-7", 0.5f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-6", 1 / 8f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-5", 1 / 8f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-4", 1 / 8f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-3", 1 / 8f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-2", 1 / 8f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-1", 2 / 8f).setCanAlterPitch(true));
			put(new Cue(lp, lp.name, "seven-go", 1).setCanAlterPitch(true));
			print(lp);
		}

		{
			Game n = GameList.getGame("ninja");

			put(new Cue(n, n.name, "shoot", 0.25f));
			put(new Cue(n, n.name, "cut", 0.25f));
			print(n);
		}

		{
			Game ab = GameList.getGame("spaceball");

			put(new Cue(ab, ab.name, "shoot", 0.5f));
			put(new Cue(ab, ab.name, "hit", 0.5f));
			put(new Cue(ab, ab.name, "longShoot", 0.5f));
			print(ab);
		}

		{
			Game db = GameList.getGame("sickBeats");

			put(new Cue(db, db.name, "appear", 0.5f));
			put(new Cue(db, db.name, "stab", 0.5f));
			print(db);
		}

		{
			Game fh = GameList.getGame("frogHop");

			put(new Cue(fh, fh.name, "shake", 0.25f));
			put(new Cue(fh, fh.name, "one", 0.5f));
			put(new Cue(fh, fh.name, "two", 0.5f));
			put(new Cue(fh, fh.name, "three", 0.5f));
			put(new Cue(fh, fh.name, "four", 0.5f));
			put(new Cue(fh, fh.name, "yahoo1", 0.5f));
			put(new Cue(fh, fh.name, "yahoo2", 0.5f));
			put(new Cue(fh, fh.name, "yahoo3", 0.5f));
			put(new Cue(fh, fh.name, "yahoo4", 0.5f));
			put(new Cue(fh, fh.name, "yeah1", 0.5f));
			put(new Cue(fh, fh.name, "yeah2", 0.5f));
			put(new Cue(fh, fh.name, "spinitboys1", 0.5f));
			put(new Cue(fh, fh.name, "spinitboys2", 0.5f));
			put(new Cue(fh, fh.name, "spinitboys3", 0.5f));
			put(new Cue(fh, fh.name, "spinitboys4", 0.5f));
			put(new Cue(fh, fh.name, "spinitboys5", 0.5f));
			put(new Cue(fh, fh.name, "spinitboys6", 0.5f));
			print(fh);
		}

		{
			Game sd = GameList.getGame("splashdown");

			put(new Cue(sd, sd.name, "alleyoop", 1));
			put(new Cue(sd, sd.name, "ascend", 0.25f));
			put(new Cue(sd, sd.name, "descend", 0.25f));
			put(new Cue(sd, sd.name, "oooh", 1).setCanAlterDuration(true).setSoundLoops(false));
			put(new Cue(sd, sd.name, "spin1", 0.5f));
			put(new Cue(sd, sd.name, "spin2", 0.5f));
			put(new Cue(sd, sd.name, "start", 0.5f));
			put(new Cue(sd, sd.name, "yeah1", 0.5f));
			put(new Cue(sd, sd.name, "yeah2", 0.5f));
			print(sd);
		}

		{
			Game sc = GameList.getGame("catchyTune");

			put(new Cue(sc, sc.name, "clap", 0.5f));
			put(new Cue(sc, sc.name, "orange", 0.5f));
			put(new Cue(sc, sc.name, "melon", 0.5f));
			print(sc);
		}

		{
			Game bon = GameList.getGame("bonOdori");

			put(new Cue(bon, bon.name, "dondo1", 0.5f));
			put(new Cue(bon, bon.name, "dondo2", 0.5f));
			put(new Cue(bon, bon.name, "dondon1", 0.5f));
			put(new Cue(bon, bon.name, "dondon2", 0.5f));
			put(new Cue(bon, bon.name, "pan", 0.5f));
			put(new Cue(bon, bon.name, "panpan1", 0.5f));
			put(new Cue(bon, bon.name, "panpan2", 0.5f));
			print(bon);
		}

		{
			Game dd = GameList.getGame("doubleDate");

			put(new Cue(dd, dd.name, "basketballBounce", 0.25f));
			put(new Cue(dd, dd.name, "footballBounce", 0.5f));
			put(new Cue(dd, dd.name, "footballKick", 0.5f));
			put(new Cue(dd, dd.name, "kick", 0.5f));
			put(new Cue(dd, dd.name, "soccerBounce", 0.5f));
			print(dd);
		}

		{
			Game ll = GameList.getGame("loveLab");

			put(new Cue(ll, ll.name, "clear", 0.5f));
			put(new Cue(ll, ll.name, "leftCatch", 0.5f));
			put(new Cue(ll, ll.name, "rightCatch", 0.5f));
			put(new Cue(ll, ll.name, "leftThrow", 0.5f));
			put(new Cue(ll, ll.name, "rightThrow", 0.5f));
			put(new Cue(ll, ll.name, "shake1", 0.25f));
			put(new Cue(ll, ll.name, "shake2", 0.25f));
			print(ll);
		}

		{
			Game sf = GameList.getGame("screwbotFactory");

			Cue letsgoStart = new Cue(sf, sf.name, "letsgoStart", 0.5f).hideFromList();
			Cue ohyeahStart = new Cue(sf, sf.name, "ohyeahStart", 0.5f).hideFromList();

			put(new Cue(sf, sf.name, "screwingOhYeah", 1).setCanAlterDuration(true)
					.setOneTimeSound(ohyeahStart.soundId));
			put(new Cue(sf, sf.name, "screwingLetsGo", 1).setCanAlterDuration(true)
					.setOneTimeSound(letsgoStart.soundId));
			put(new Cue(sf, sf.name, "clankclank1", 0.5f));
			put(new Cue(sf, sf.name, "clankclank2", 0.5f));
			put(new Cue(sf, sf.name, "oh", 0.5f));
			put(new Cue(sf, sf.name, "yeah", 0.5f));
			put(new Cue(sf, sf.name, "lets", 0.5f));
			put(new Cue(sf, sf.name, "go", 0.5f));
			put(new Cue(sf, sf.name, "letsgoPlace", 0.5f));
			put(new Cue(sf, sf.name, "letsgoStart", 0.5f));
			put(new Cue(sf, sf.name, "ohyeahPlace", 0.5f));
			put(letsgoStart);
			put(ohyeahStart);
			put(new Cue(sf, sf.name, "complete", 0.5f));
			print(sf);
		}

		{
			Game c = GameList.getGame("cannery");

			put(new Cue(c, c.name, "ding", 0.5f));
			put(new Cue(c, c.name, "steam", 0.5f));
			print(c);
		}

		{
			Game bts = GameList.getGame("builtToScale");

			put(new Cue(bts, bts.name, "1", 0.5f));
			put(new Cue(bts, bts.name, "2", 0.5f));
			put(new Cue(bts, bts.name, "3", 0.5f));
			put(new Cue(bts, bts.name, "4", 0.5f));
			put(new Cue(bts, bts.name, "prepare", 0.5f));
			put(new Cue(bts, bts.name, "shoot", 0.5f));
			print(bts);
		}

	}

	public void put(Cue cue, String... deprecatedNames) {
		cues.put(cue.folder + "_" + cue.file, cue);

		if (deprecatedNames.length > 0) {
			for (String s : deprecatedNames) {
				cues.put(cue.folder + "_" + s, cue);
			}
		}
	}

	public static class Cue {

		public final Game game;
		public final String folder;
		public final String file;
		public final String soundId;
		public final float duration;
		public final String folderParent;
		public float pitchWithBpm = -1;
		public boolean canAlterDuration = false;
		public boolean canAlterPitch = false;
		public boolean soundLoops = false;
		public String oneTimeSound = null;
		public boolean shouldAppearInList = true;

		public Cue(Game game, String folderParent, String folder, String file, float duration) {
			this.game = game;
			this.folder = folder;
			this.file = file;
			this.duration = duration;
			this.folderParent = folderParent;

			soundId = "cue_" + folder + "_" + file;
		}

		public Cue(Game game, String folder, String file, float duration) {
			this(game, "sounds/cues/", folder, file, duration);
		}

		public Cue setUsesPitch(float baseBpm) {
			pitchWithBpm = baseBpm;

			return this;
		}

		public Cue setCanAlterPitch(boolean b) {
			canAlterPitch = b;

			return this;
		}

		/**
		 * A sound that can be played at the start of the beat
		 *
		 * @param s
		 * @return
		 */
		public Cue setOneTimeSound(String s) {
			oneTimeSound = s;

			return this;
		}

		/**
		 * Also sets sound looping to true.
		 *
		 * @param b
		 * @return
		 */
		public Cue setCanAlterDuration(boolean b) {
			canAlterDuration = b;
			setSoundLoops(true);

			return this;
		}

		public Cue setSoundLoops(boolean b) {
			soundLoops = b;

			return this;
		}

		public Cue hideFromList() {
			shouldAppearInList = false;

			return this;
		}

		public Sound getSFX() {
			return AssetRegistry.getSound(soundId);
		}

		public Sound getOneTimeSFX() {
			if (oneTimeSound == null)
				return null;

			return AssetRegistry.getSound(oneTimeSound);
		}

	}

	public static class Game {

		public String name;

		public Game(String s) {
			name = s;
		}

	}

	public static class GameList {
		public static Game getGame(String s) {
			return new Game(s);
		}
	}

}
