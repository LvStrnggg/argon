package dev.lvstrng.muam3r.mixin;

import net.minecraft.client.render.Frustum;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface FrustumAccessor {

	@Accessor
	FrustumIntersection getFrustumIntersection();

	@Accessor
	void setFrustumIntersection(FrustumIntersection vector4f);

	@Accessor("x")
	double getX();

	@Accessor("x")
	void setX(double x);

	@Accessor("y")
	double getY();

	@Accessor("y")
	void setY(double y);

	@Accessor("z")
	double getZ();

	@Accessor("z")
	void setZ(double z);
}
