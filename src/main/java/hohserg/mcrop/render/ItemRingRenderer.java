package hohserg.mcrop.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        EntityLivingBase e = data.length >= 2 && data[1] instanceof EntityLivingBase ? (EntityLivingBase) data[1] : Minecraft.getMinecraft().thePlayer;
        switch (type) {
            case ENTITY: {
                renderItemEntityPass(stack, e);
            }
            break;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON: {
                renderRegularPass(stack, e, false);
            }
            break;
            case INVENTORY: {
                drawInventoryPass(stack, e);
            }
            break;
            case FIRST_PERSON_MAP:
                break;
        }

    }

    private void renderItemEntityPass(ItemStack stack, EntityLivingBase e) {
        GL11.glRotated(180, 0, 1, 0);
        GL11.glTranslated(-0.5, -0.275, 0);
        renderRegularPass(stack, e, false);
    }

    private void drawInventoryPass(ItemStack stack, EntityLivingBase e) {
        int scale = -16;
        GL11.glScaled(scale, scale, scale);
        GL11.glTranslated(-1, -1, 1);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4d(1, 1, 1, 1);
        renderRegularPass(stack, e, true);
    }

    private void renderRegularPass(ItemStack stack, EntityLivingBase e, boolean inventory) {
        double decalThickness = 0.01;
        GL11.glPushMatrix();
        {
            GL11.glScaled(1, 1, 1 + decalThickness);
            GL11.glTranslated(0, 0, 1d / 16 * decalThickness / 2);

            GL11.glTranslatef(0.9375F, 0.0625F, -0.0F);
            GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
            float f6 = 1f / 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0, 0.3F, 0.0F);

            //GL11.glColor4d(1, 0, 0, 1);
            RenderManager.instance.itemRenderer.renderItem(e, stack, 1, FIRST_PERSON_MAP);
        }
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        {

            GL11.glTranslatef(0.9375F, 0.0625F, -0.0F);
            GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
            float f6 = 1f / 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glTranslatef(0, 0.3F, 0.0F);

            GL11.glColor4d(1, 1, 1, 1);
            if (inventory)
                GL11.glDisable(GL11.GL_LIGHTING);
            RenderManager.instance.itemRenderer.renderItem(e, stack, 0, FIRST_PERSON_MAP);
        }
        GL11.glPopMatrix();

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }
}
