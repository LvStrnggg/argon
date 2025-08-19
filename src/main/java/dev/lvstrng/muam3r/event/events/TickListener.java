package dev.lvstrng.muam3r.event.events;

import dev.lvstrng.muam3r.event.Event;
import dev.lvstrng.muam3r.event.Listener;

import java.util.ArrayList;

public interface TickListener extends Listener {
	void onTick();

	class TickEvent extends Event<TickListener> {

		@Override
		public void fire(ArrayList<TickListener> listeners) {
			listeners.forEach(TickListener::onTick);
		}

		@Override
		public Class<TickListener> getListenerType() {
			return TickListener.class;
		}
	}
}
