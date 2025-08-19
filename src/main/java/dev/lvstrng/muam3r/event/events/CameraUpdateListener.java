package dev.lvstrng.muam3r.event.events;

import dev.lvstrng.muam3r.event.CancellableEvent;
import dev.lvstrng.muam3r.event.Listener;

import java.util.ArrayList;

public interface CameraUpdateListener extends Listener {
	void onCameraUpdate(CameraUpdateEvent event);

	class CameraUpdateEvent extends CancellableEvent<CameraUpdateListener> {
		public double x, y, z;

		public CameraUpdateEvent(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getZ() {
			return z;
		}

		public void setX(double x) {
			this.x = x;
		}

		public void setY(double y) {
			this.y = y;
		}

		public void setZ(double z) {
			this.z = z;
		}

		@Override
		public void fire(ArrayList<CameraUpdateListener> listeners) {
			listeners.forEach(l -> l.onCameraUpdate(this));
		}

		@Override
		public Class<CameraUpdateListener> getListenerType() {
			return CameraUpdateListener.class;
		}
	}
}
