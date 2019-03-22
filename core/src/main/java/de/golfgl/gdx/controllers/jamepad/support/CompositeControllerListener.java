package de.golfgl.gdx.controllers.jamepad.support;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class CompositeControllerListener implements ControllerListener {
    private final Array<ControllerListener> listeners = new Array<>();

    @Override
    public void connected(Controller controller) {
        for (ControllerListener listener : listeners) {
            listener.connected(controller);
        }
    }

    @Override
    public void disconnected(Controller controller) {
        for (ControllerListener listener : listeners) {
            listener.disconnected(controller);
        }
    }

    @Override
    public boolean buttonDown(final Controller controller, final int buttonCode) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.buttonDown(controller, buttonCode);
            }
        });
    }

    @Override
    public boolean buttonUp(final Controller controller, final int buttonCode) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.buttonUp(controller, buttonCode);
            }
        });
    }

    @Override
    public boolean axisMoved(final Controller controller, final int axisCode, final float value) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.axisMoved(controller, axisCode, value);
            }
        });
    }

    @Override
    public boolean povMoved(final Controller controller, final int povCode, final PovDirection value) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.povMoved(controller, povCode, value);
            }
        });
    }

    @Override
    public boolean xSliderMoved(final Controller controller, final int sliderCode, final boolean value) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.xSliderMoved(controller, sliderCode, value);
            }
        });
    }

    @Override
    public boolean ySliderMoved(final Controller controller, final int sliderCode, final boolean value) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.ySliderMoved(controller, sliderCode, value);
            }
        });
    }

    @Override
    public boolean accelerometerMoved(final Controller controller, final int accelerometerCode, final Vector3 value) {
        return notifyListeners(new Notifier() {
            public boolean perform(ControllerListener listener) {
                return listener.accelerometerMoved(controller, accelerometerCode, value);
            }
        });
    }

    private boolean notifyListeners(Notifier notifier) {
        for (ControllerListener listener : listeners) {
            if (notifier.perform(listener)) {
                return true;
            }
        }

        return false;
    }

    public void addListener(ControllerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ControllerListener listener) {
        listeners.removeValue(listener, true);
    }

    public void clear() {
        listeners.clear();
    }

    private interface Notifier {
        boolean perform(ControllerListener listener);
    }
}
