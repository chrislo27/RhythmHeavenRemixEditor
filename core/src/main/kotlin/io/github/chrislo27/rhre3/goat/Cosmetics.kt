package io.github.chrislo27.rhre3.goat


enum class GoatBackgrounds(val file: String, val nam: String) {

    DEFAULT("images/goat/bg/rhythm.png", "Default"),
    AURORA("images/goat/bg/aurora.png", "Aurora"),
    SUNSET("images/goat/bg/sunset.png", "Sunset"),
    COSMIC_DANCE("images/goat/bg/cosmic dance.png", "Cosmic Dance"),
    WINTER("images/goat/bg/winter.png", "Winter"),
    FINAL_DESTINATION("images/goat/bg/final destination.png", "Final Destination"),
    BUNNY_HOP("images/goat/bg/bunny hop.png", "Bunny Hop"),
    HEY_BABY("images/goat/bg/hey baby.png", "Hey Baby"),
    SPACE_DANCE("images/goat/bg/space dance.png", "Space Dance"),
    SPACE_DANCE_R3("images/goat/bg/space dance r3.png", "Space Dance (Remix 3)"),
    TRAM_AND_PAULINE_R3("images/goat/bg/tram and pauline r3.png", "Tram and Pauline (Remix 3)"),
    WIZARD_WALTZ("images/goat/bg/wizard's waltz.png", "Wizard's Waltz"),
    MARCHING_ORDERS("images/goat/bg/marching orders.png", "Marching Orders"),
    CROP_STOMP("images/goat/bg/crop stomp.png", "Crop Stomp"),
    SPACEBALL("images/goat/bg/spaceball.png", "Spaceball"),
    BON_ODORI("images/goat/bg/bon-odori.png", "Bon-Odori"),
    BUILT_TO_SCALE_FEVER("images/goat/bg/built to scale fever.png", "Built to Scale Fever"),
    CAFE("images/goat/bg/cafe.png", "Cafe"),
    CHEER_READERS("images/goat/bg/cheer readers.png", "Cheer Readers"),
    CLAPPY_TRIO("images/goat/bg/clappy trio.png", "Clappy Trio"),
    DJ_SCHOOL("images/goat/bg/dj school.png", "DJ School"),
    LIVING_ROOM("images/goat/bg/living room.png", "Living Room"),
    NIGHT_WALK_FEVER("images/goat/bg/night walk fever.png", "Night Walk Fever"),
    NINJA_BODYGUARD("images/goat/bg/ninja bodyguard.png", "Ninja Bodyguard"),
    QUIZ_AUDIENCE("images/goat/bg/quiz audience.png", "Quiz Audience"),
    RAP_MEN("images/goat/bg/Rap Men.png", "Rap Men"),
    RINGSIDE_BG("images/goat/bg/ringside.png", "Ringside"),
    SAMURAI_SLICE_FOGGY("images/goat/bg/samurai slice foggy.png", "Samurai Slice Foggy"),
    SAMURAI_SLICE("images/goat/bg/samurai slice.png", "Samurai Slice"),
    SNEAKY_SPIRITS("images/goat/bg/sneaky spirits.png", "Sneaky Spirits"),
    TAP_TRIAL("images/goat/bg/tap trial.png", "Tap Trial"),
    TENGAMI("images/goat/bg/tengami.png", "Tengami"),
    TOSS_BOYS("images/goat/bg/toss boys.png", "Toss Boys"),
    TRAM_AND_PAULINE("images/goat/bg/tram and pauline.png", "Tram and Pauline"),
    BLUE("images/goat/bg/blue.png", "Blue"),
    BAR("images/goat/bg/bar.png", "Bar"),
    HEY_BABY_2("images/goat/bg/hey baby 2.png", "Hey Baby 2"),
    NINJA_REINCARNATE("images/goat/bg/ninja reincarnate.png", "Ninja Reincarnate"),
    SNAPPY_TRIO("images/goat/bg/snappy trio.png", "Snappy Trio"),
    SNEAKY_SPIRITS_2("images/goat/bg/sneaky spirits 2.png", "Sneaky Spirits 2"),
    RAP_WOMEN("images/goat/bg/rap women.png", "Rap Women"),
    FLIPPER_FLOP("images/goat/bg/flipper-flop.png", "Flipper-Flop"),
    DOG_NINJA("images/goat/bg/dog ninja.png", "Dog Ninja"),
    DRUMMER_DUEL("images/goat/bg/drummer duel.png", "Drummer Duel"),
    FAN_CLUB_2("images/goat/bg/fan club 2.png", "Fan Club 2"),
    FRUIT_BASKET_2("images/goat/bg/fruit basket 2.png", "Fruit Basket 2"),
    GLEE_CLUB_2("images/goat/bg/glee club 2.png", "Glee Club 2"),
    SNEAKY_SPIRITS_STORY("images/goat/bg/sneaky spirits story.png", "Sneaky Spirits (Story)"),
    SPACE_SOCCER("images/goat/bg/space soccer.png", "Space Soccer"),
    FILLBOTS_BEE_REMIX("images/goat/bg/fillbots bee.png", "Fillbots (Bee Remix)"),
    GLEE_CLUB_STORY("images/goat/bg/glee club 1.png", "Glee Club 1"),
    SHOWTIME("images/goat/bg/showtime.png", "Showtime"),
    FIREWORKS("images/goat/bg/fireworks.png", "Fireworks"),
    FIREWORKS_SMILE("images/goat/bg/fireworks-smile.png", "Fireworks Smile");

    companion object {
        val VALUES = values().toList()
    }

}

enum class GoatFaces(val file: String, val nam: String, val isMask: Boolean = false) {

    DEFAULT("images/goat/face/smile.png", "Default"),
    DISAPPOINTED("images/goat/face/disappointed.png", "Disappointed"),
    EYES_OPEN_SMILE("images/goat/face/eyes_open_smile.png", "Smile"),
    FROWN("images/goat/face/frown.png", "Frown"),
    GRIMACE("images/goat/face/grimace.png", "Grimace"),
    SMIRK("images/goat/face/smirk.png", "Smirk"),
    STARE("images/goat/face/stare.png", "Stare"),
    YAWN("images/goat/face/yawn.png", "Yawn"),
    PYROVISION("images/goat/face/pyrovision.png", "Pyrovision Goggles"),
    MASK_WITH_YELLOW_HORNS("images/goat/face/mask_with_yellow_horns.png", "Dededemask", isMask = true),
    LENNY("images/goat/face/lenny.png", "Lenny"),
    ANNOYED("images/goat/face/annoyed.png", "Annoyed"),
    SHOCK("images/goat/face/shock.png", "Shock"),
    BEARD_BURGER("images/goat/face/beard burger.png", "Beard Burger"),
    STOMP_FARMER("images/goat/face/stomp farmer.png", "Stomp Farmer"),
    CHORUS_KID("images/goat/face/chorus kid.png", "Chorus Kid"),
    GNOME("images/goat/face/gnome.png", "Gnome"),
    GRAND("images/goat/face/grand.png", "Grand"),
    LANKY_VILLAIN("images/goat/face/lanky villain.png", "Lanky Villain"),
    SPACE_KICKER("images/goat/face/space kicker.png", "Space Kicker"),
    COSMIC_DANCER("images/goat/face/cosmic dancer.png", "Cosmic Dancer"),
    MULTIPLE_GRIN("images/goat/face/multiple grin.png", "Multiple Grin"),
    SUNGLASSES_1("images/goat/face/sunglasses 1.png", "Sunglasses 1"),
    SUNGLASSES_2("images/goat/face/sunglasses 2.png", "Sunglasses 2"),
    SQUIRREL("images/goat/face/squirrel.png", "Squirrel"),
    KARATE_JOE_SMIRK("images/goat/face/karate joe smirk.png", "Karate Joe Smirk"),
    MANNEQUIN("images/goat/face/mannequin.png", "Mannequin"),
    MEOW_MIXER("images/goat/face/meow mixer.png", "Meow Mixer"),
    MR_UPBEAT("images/goat/face/mr upbeat.png", "Mr. Upbeat"),
    O_O("images/goat/face/o_O.png", "o_O"),
    RED_RAPPER("images/goat/face/red rapper.png", "Red Rapper"),
    GOAT_EXE("images/goat/face/goatexe.png", "Goat.exe"),
    DISGUST("images/goat/face/disgust.png", "Disgust"),
    PAPRIKA("images/goat/face/paprika.png", "Paprika", isMask = true),
    SAFFRON("images/goat/face/saffron.png", "Saffron", isMask = true),
    SALTWATER("images/goat/face/saltwater.png", "Saltwater", isMask = true),
    STARFY_FACE("images/goat/face/starfy.png", "Starfy"),
    OWO("images/goat/face/OwO.png", "OwO"),
    PLANETARY_DEER("images/goat/face/planetary deer.png", "Planetary Deer"),
    FLOWEY("images/goat/face/flowey.png", "Flowey"),
    STEPSWITCHER("images/goat/face/stepswitcher.png", "Stepswitcher"),
    SANS("images/goat/face/sans.png", "Sans"),
    MIMIKYU("images/goat/face/mimikyu.png", "Mimikyu"),
    COLON_CAPITAL_D("images/goat/face/colon_capital_d.png", ":D"),
    CAPITAL_X_CAPITAL_D("images/goat/face/ecksdee.png", "XD"),
    PERIOD_UNDERSCORE_PERIOD("images/goat/face/period_underscore_period.png", "._."),
    COLON_CARAT_RIGHT_PARENTHESE("images/goat/face/tear smiley.png", ":^)"),
    YELLOW_RAPPER("images/goat/face/yellow rapper.png", "Yellow Rapper"),
    ONE_EYE("images/goat/face/one eye.png", "One-Eyed"),
    TIBBY("images/goat/face/tibby.png", "Tibby");

    companion object {
        val VALUES = values().toList()
    }

}

enum class GoatHats(val file: String, val nam: String, val helmet: Boolean = false, val head: Boolean = false) {

    GHOSTLY_GIBUS("images/goat/hat/ghostly_gibus.png", "Ghostly Gibus"),
    STOMP_FARMER("images/goat/hat/stomp farmer.png", "Stomp Farmer"),
    CHOWDER("images/goat/hat/chowder.png", "Chowder"),
    GRAND("images/goat/hat/grand.png", "Grand"),
    LANKY_VILLAIN("images/goat/hat/lanky villain.png", "Lanky Villain"),
    MARCHERS("images/goat/hat/marchers.png", "Marchers", helmet = true),
    GNOME("images/goat/hat/gnome.png", "Gnome"),
    SPACE_KICKER("images/goat/hat/space kicker.png", "Space Kicker", head = true),
    FEZ("images/goat/hat/fez.png", "Fez"),
    UPSIDE_DOWN_BEARD("images/goat/hat/upside down beard.png", "Upside-Down Beard"),
    HEADPHONES("images/goat/hat/headphones.png", "Headphones"),
    GBA("images/goat/hat/gba.png", "GBA", helmet = true),
    DS("images/goat/hat/ds.png", "DS", helmet = true),
    THREE_DS("images/goat/hat/3ds.png", "3DS", helmet = true),
    PAPRIKA("images/goat/hat/paprika.png", "Paprika"),
    SAFFRON("images/goat/hat/saffron.png", "Saffron"),
    SALTWATER("images/goat/hat/saltwater.png", "Saltwater"),
    CLAPPY_TRIO("images/goat/hat/clappy trio.png", "Clappy Trio"),
    GRAMPS("images/goat/hat/gramps.png", "Gramps", head = true),
    HEADPHONES_2("images/goat/hat/headphones 2.png", "Headphones 2"),
    SNAPPY_TRIO("images/goat/hat/snappy trio.png", "Snappy Trio"),
    STAINLESS_POT("images/goat/hat/stainless pot.png", "Stainless Pot"),
    TIBBY("images/goat/hat/tibby.png", "Tibby");

    companion object {
        val VALUES = values().toList()
    }

}