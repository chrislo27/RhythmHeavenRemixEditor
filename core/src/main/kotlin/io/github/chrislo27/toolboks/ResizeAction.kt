package io.github.chrislo27.toolboks

/**
 * The action to take when the screen is resized.
 */
enum class ResizeAction {

    /**
     * The window can be adjusted to any size. Fonts do not reload.
     */
    ANY_SIZE,
    /**
     * The game is always emulated at a certain set of dimensions. Fonts do not reload.
     */
    LOCKED,
    /**
     * The window can be adjusted to any size, and the internal camera will attempt to keep a maximum fit of the
     * provided aspect ratio. Fonts **do** reload.
     */
    KEEP_ASPECT_RATIO

}