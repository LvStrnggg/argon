package dev.lvstrng.muam3r.event.events;

import dev.lvstrng.muam3r.event.Event;
import dev.lvstrng.muam3r.event.Listener;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public interface HudListener extends Listener {
	void onRenderHud(HudEvent event);

	class HudEvent extends Event<HudListener> {
		public DrawContext context;
		public float delta;

		public HudEvent(DrawContext context, float delta) {
			this.context = context;
			this.delta = delta;
		}

		@Override
		public void fire(ArrayList<HudListener> listeners) {
			listeners.forEach(e -> e.onRenderHud(this));
		}

		@Override
		public Class<HudListener> getListenerType() {
			return HudListener.class;
		}
	}
}