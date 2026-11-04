package dk.revo.revoextraction;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Small, consistent stamp for Revo machine interfaces. Coordinates are GUI-relative. */
public final class RevoScreenBrand {
    private RevoScreenBrand() { }
    public static void draw(GuiGraphics graphics,Font font,int x,int y) {
        graphics.drawString(font,"R",x,y,0xFF000000,false);
    }
}
