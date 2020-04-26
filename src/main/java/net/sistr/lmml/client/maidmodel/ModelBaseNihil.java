package net.sistr.lmml.client.maidmodel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class ModelBaseNihil<T extends LivingEntity> extends EntityModel<T> {
	//互換性を維持しつつ、バニラの処理を利用するための変数群
	public T entity;
	public float entityYaw;
	public float partialTicks;
	public IRenderTypeBuffer buffer;

	public boolean isAlphablend;
	public boolean isModelAlphablend;
	public IModelBaseMMM capsLink;
	public int lighting;
	public IModelCaps entityCaps;
	public boolean isRendering;
	/**
	 * レンダリングが実行された回数。
	 * ダメージ時などの対策。
	 */
	public int renderCount;

	public void showAllParts() {
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		renderCount++;
	}

	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	public void setLivingAnimations(T entity, float limbSwing, float limbSwingAmount, float partialTickTime) {

	}

	public RenderType getRenderTypeMM(ResourceLocation resourcelocation) {
		return null;
	}
}