package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.maidmodel.ModelRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiModelHeldItemLayer<T extends LivingEntity & IHasMultiModel> extends LayerRenderer<T, MultiModel<T>> {
    private static final Logger LOGGER = LogManager.getLogger();

    public MultiModelHeldItemLayer(IEntityRenderer<T, MultiModel<T>> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.getPrimaryHand() == HandSide.RIGHT;
        ItemStack leftStack = flag ? entity.getHeldItemOffhand() : entity.getHeldItemMainhand();
        ItemStack rightStack = flag ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();
        if (!leftStack.isEmpty() || !rightStack.isEmpty()) {
            matrixStackIn.push();
            if (this.getEntityModel().isChild) {
                matrixStackIn.translate(0.0D, 0.75D, 0.0D);
                matrixStackIn.scale(0.5F, 0.5F, 0.5F);
            }

            this.handRender(entity, rightStack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn, bufferIn, packedLightIn);
            this.handRender(entity, leftStack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.pop();
        }
    }

    //todo 位置調整
    private void handRender(T entity, ItemStack stack, ItemCameraTransforms.TransformType type, HandSide hand, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        if (!stack.isEmpty()) {
            matrixStack.push();
            //((IHasArm)this.getEntityModel()).translateHand(hand, matrixStack);
            boolean isLeft = hand == HandSide.LEFT;
            ModelRenderer arm;
            try {
                arm = entity.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.BODY)
                        .orElseThrow(() -> new RuntimeException("モデルが存在しません")).Arms[isLeft ? 1 : 0];
            } catch (RuntimeException e) {
                LOGGER.warn(e.getMessage());
                return;
            }
            ModelRenderer.matrixStack = matrixStack;//ちとやり方が汚いか
            arm.postRender(0.0625F);
            if (entity.isSneaking()) {
                matrixStack.translate(0.0F, 0.2F, 0.0F);
            }

            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));
            /* 初期モデル構成で
             * x: 手の甲に垂直な方向(-で向かって右に移動)
             * y: 体の面に垂直な方向(-で向かって背面方向に移動)
             * z: 腕に平行な方向(-で向かって手の先方向に移動)
             */
            matrixStack.translate((float)(isLeft ? -1 : 1) / 16.0F, 0.05D, -0.15D);
            Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entity, stack, type, isLeft, matrixStack, buffer, light);
            matrixStack.pop();
        }
    }

}
