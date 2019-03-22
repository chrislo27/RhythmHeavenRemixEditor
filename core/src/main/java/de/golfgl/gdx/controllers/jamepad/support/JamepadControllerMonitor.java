package de.golfgl.gdx.controllers.jamepad.support;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;

public class JamepadControllerMonitor implements Runnable {
    private final ControllerManager controllerManager;
    private final ControllerListener listener;
    private final IntMap<Tuple> indexToController = new IntMap<>();

    public JamepadControllerMonitor(ControllerManager controllerManager, ControllerListener listener) {
        this.controllerManager = controllerManager;
        this.listener = listener;
    }

    @Override
    public void run() {
        controllerManager.update();

        checkForNewControllers();
        update();

        Gdx.app.postRunnable(this);
    }

    private void checkForNewControllers() {
        int newNumControllers = controllerManager.getNumControllers();
        for (int i = 0; i < newNumControllers; i++) {
            ControllerIndex controllerIndex = controllerManager.getControllerIndex(i);

            if (!indexToController.containsKey(controllerIndex.getIndex())) {
                Tuple tuple1 = new Tuple(controllerIndex);
                tuple1.controller.addListener(listener);

                indexToController.put(controllerIndex.getIndex(), tuple1);
                listener.connected(tuple1.controller);
            }
        }
    }

    private void update() {
        IntArray disconnectedControllers = new IntArray(indexToController.size);
        for (Tuple tuple : indexToController.values()) {
            JamepadController controller = tuple.controller;
            boolean connected = controller.update();

            if (!connected) {
                listener.disconnected(tuple.controller);
                disconnectedControllers.add(tuple.index.getIndex());
            }
        }

        for (int i = 0; i < disconnectedControllers.size; i++) {
            indexToController.remove(disconnectedControllers.get(i));
        }
    }

    private class Tuple {
        public final ControllerIndex index;
        public final JamepadController controller;

        public Tuple(ControllerIndex index) {
            this.index = index;
            this.controller = new JamepadController(index);
        }
    }
}
