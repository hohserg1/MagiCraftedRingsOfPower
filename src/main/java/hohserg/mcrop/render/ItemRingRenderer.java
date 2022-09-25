package hohserg.mcrop.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.FIRST_PERSON_MAP;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.ENTITY_BOBBING;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.ENTITY_ROTATION;

public class ItemRingRenderer implements IItemRenderer {
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ENTITY_ROTATION || helper == ENTITY_BOBBING;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        switch (type) {
            case ENTITY:
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
            case INVENTORY: {

                EntityLivingBase e = data.length >= 2 && data[1] instanceof EntityLivingBase ? (EntityLivingBase) data[1] : Minecraft.getMinecraft().thePlayer;

                renderRegularPass(item, e, 0);
                renderRegularPass(item, e, 1);
            }
            break;
            case FIRST_PERSON_MAP:
                break;
        }
    }

    private void renderRegularPass(ItemStack item, EntityLivingBase e, int pass) {
        GL11.glTranslatef(0.9375F, 0.0625F, -0.0F);
        GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
        float f6 = 1f / 1.5F;
        GL11.glScalef(f6, f6, f6);
        GL11.glTranslatef(0, 0.3F, 0.0F);

        RenderManager.instance.itemRenderer.renderItem(e, item, pass, FIRST_PERSON_MAP);
    }
}
