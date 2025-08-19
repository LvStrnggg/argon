package dev.lvstrng.muam3r.mixin;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferRenderer.class)
public interface BufferRendererAccessor {
	@Accessor("currentVertexBuffer")
	static void setCurrentVertexBuffer(VertexBuffer vertexBuffer) {
	}
}
