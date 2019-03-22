package de.golfgl.gdx.controllers.jamepad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import de.golfgl.gdx.controllers.jamepad.support.CompositeControllerListener;
import de.golfgl.gdx.controllers.jamepad.support.JamepadControllerMonitor;
import de.golfgl.gdx.controllers.jamepad.support.JamepadShutdownHook;

public class JamepadControllerManager implements ControllerManager, Disposable {
    private static boolean nativeLibInitialized = false;
    private static com.studiohartman.jamepad.ControllerManager controllerManager;

    private final Array<Controller> controllers = new Array<>();
    private final CompositeControllerListener compositeListener = new CompositeControllerListener();

    public JamepadControllerManager() {
        compositeListener.addListener(new ManageControllers());

        if (!nativeLibInitialized) {
            int maxNumControllers = 4;
            String mappingsPath = "gamecontrollerdb.txt";

            controllerManager = new com.studiohartman.jamepad.ControllerManager(maxNumControllers, mappingsPath);
            controllerManager.initSDLGamepad();

            JamepadControllerMonitor monitor = new JamepadControllerMonitor(controllerManager, compositeListener);
            monitor.run();

            Gdx.app.addLifecycleListener(new JamepadShutdownHook(controllerManager));
            Gdx.app.postRunnable(monitor);

            nativeLibInitialized = true;
        }
    }

    @Override
    public Array<Controller> getControllers() {
        return controllers;
    }

    @Override
    public void addListener(ControllerListener listener) {
        compositeListener.addListener(listener);
    }

    @Override
    public void removeListener(ControllerListener listener) {
        compositeListener.removeListener(listener);
    }

    @Override
    public Array<ControllerListener> getListeners() {
        Array array = new Array();
        array.add(compositeListener);
        return array;
    }

    @Override
    public void clearListeners() {
        compositeListener.clear();
        compositeListener.addListener(new ManageControllers());
    }


    public void dispose() {
        controllerManager.quitSDLGamepad();
    }

    private class ManageControllers extends ControllerAdapter {
        @Override
        public void connected(Controller controller) {
            synchronized (controllers) {
                controllers.add(controller);
            }
        }

        @Override
        public void disconnected(Controller controller) {
            synchronized (controllers) {
                controllers.removeValue(controller, true);
            }
        }
    }
}
