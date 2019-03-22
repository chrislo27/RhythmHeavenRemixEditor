package de.golfgl.gdx.controllers.jamepad.support;

import com.badlogic.gdx.LifecycleListener;
import com.studiohartman.jamepad.ControllerManager;

public class JamepadShutdownHook implements LifecycleListener {
    private final ControllerManager controllerManager;

    public JamepadShutdownHook(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        controllerManager.quitSDLGamepad();
    }
}
