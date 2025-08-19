package dev.lvstrng.muam3r.event;

import dev.lvstrng.muam3r.Muam3r;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;


public final class EventManager {
	private final HashMap<Class<? extends Listener>, ArrayList<PrioritizedListener<? extends Listener>>> listenerMap;

	public EventManager() {
		listenerMap = new HashMap<>();
	}

	public static <L extends Listener, E extends Event<L>> void fire(E event) {
		EventManager eventManager = Muam3r.INSTANCE.getEventManager();
		if (eventManager != null) {
			eventManager.fireImpl(event);
		}
	}

	private <L extends Listener, E extends Event<L>> void fireImpl(E event) {
		Class<L> listenerType = event.getListenerType();
		ArrayList<PrioritizedListener<L>> listeners = (ArrayList<PrioritizedListener<L>>) (Object) listenerMap.get(listenerType);

		if (listeners == null || listeners.isEmpty())
			return;

		ArrayList<PrioritizedListener<L>> listeners2 = new ArrayList<>(listeners);
		listeners2.removeIf(Objects::isNull);
		listeners2.sort(Comparator.comparing(listener -> Integer.MAX_VALUE - listener.getPriority()));

		ArrayList<L> listeners3 = new ArrayList<>();
		listeners2.forEach(listener -> listeners3.add(listener.getListener()));

		event.fire(listeners3);
	}

	public <L extends Listener> void add(Class<L> type, L listener) {
		add(type, listener, 0);
	}

	public <L extends Listener> void add(Class<L> type, L listener, int priority) {
		ArrayList<PrioritizedListener<L>> listeners = (ArrayList<PrioritizedListener<L>>) (Object) listenerMap.get(type);
		if (listeners == null) {
			listeners = new ArrayList<>();
			listenerMap.put(type, (ArrayList<PrioritizedListener<? extends Listener>>) (Object) listeners);
		}
		listeners.add(new PrioritizedListener(listener, priority));
	}

	public <L extends Listener> void remove(Class<L> type, L listener) {
		ArrayList<PrioritizedListener<L>> listeners = (ArrayList<PrioritizedListener<L>>) (Object) listenerMap.get(type);
		if (listeners != null)
			listeners.removeIf(l -> l.getListener().equals(listener));
	}

	
	private static class PrioritizedListener<L extends Listener> {
		private final L listener;
		private final int priority;

		public PrioritizedListener(L listener) {
			this(listener, 0);
		}

		public PrioritizedListener(L listener, int priority) {
			this.listener = listener;
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}

		public L getListener() {
			return listener;
		}
	}
}
