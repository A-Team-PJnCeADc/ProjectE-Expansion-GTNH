package com.projecte.expansion.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Tessellator;

/**
 * 极简的盒模型渲染器:方块空间(0..1,y 向上,正面 = -Z)中的盒子,每面带 UV 矩形,
 * 移植自 Minecraft 的 JSON 方块模型(上游使用 JSON 模型,而 1.7.10 不渲染那些,
 * 所以这里用 Java 表达同样的几何形状)。
 */
public class BoxModelPX {

    public static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3, UP = 4, DOWN = 5;

    private static final float[][] NORMALS = { { 0, 0, -1 }, { 1, 0, 0 }, { 0, 0, 1 }, { -1, 0, 0 }, { 0, 1, 0 },
        { 0, -1, 0 } };

    public static final class Box {

        public final String group;
        public final int texture;
        public final float x0, y0, z0, x1, y1, z1;
        public final float[][] uv = new float[6][];

        Box(String group, int texture, float x0, float y0, float z0, float x1, float y1, float z1) {
            this.group = group;
            this.texture = texture;
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
        }

        public Box uv(int face, float u0, float v0, float u1, float v1) {
            uv[face] = new float[] { u0, v0, u1, v1 };
            return this;
        }
    }

    private final List<Box> boxes = new ArrayList<>();

    protected Box box(String group, int texture, float x0, float y0, float z0, float x1, float y1, float z1) {
        Box b = new Box(group, texture, x0, y0, z0, x1, y1, z1);
        boxes.add(b);
        return b;
    }

    /** 渲染所有使用给定贴图索引的盒子。 */
    public void renderTexture(int texture) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (Box b : boxes) {
            if (b.texture != texture) continue;
            addBox(t, b);
        }
        t.draw();
    }

    /** 渲染某一个组的所有盒子(例如 "lid" / "latch" / "body" / "base")。 */
    public void renderGroup(String group) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (Box b : boxes) {
            if (!group.equals(b.group)) continue;
            addBox(t, b);
        }
        t.draw();
    }

    public void renderAll() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (Box b : boxes) addBox(t, b);
        t.draw();
    }

    private static void addBox(Tessellator t, Box b) {
        if (b.uv[NORTH] != null) {
            t.setNormal(NORMALS[NORTH][0], NORMALS[NORTH][1], NORMALS[NORTH][2]);
            t.addVertexWithUV(b.x1, b.y0, b.z0, u0(b, NORTH), v1(b, NORTH));
            t.addVertexWithUV(b.x0, b.y0, b.z0, u1(b, NORTH), v1(b, NORTH));
            t.addVertexWithUV(b.x0, b.y1, b.z0, u1(b, NORTH), v0(b, NORTH));
            t.addVertexWithUV(b.x1, b.y1, b.z0, u0(b, NORTH), v0(b, NORTH));
        }
        if (b.uv[SOUTH] != null) {
            t.setNormal(NORMALS[SOUTH][0], NORMALS[SOUTH][1], NORMALS[SOUTH][2]);
            t.addVertexWithUV(b.x0, b.y0, b.z1, u1(b, SOUTH), v1(b, SOUTH));
            t.addVertexWithUV(b.x1, b.y0, b.z1, u0(b, SOUTH), v1(b, SOUTH));
            t.addVertexWithUV(b.x1, b.y1, b.z1, u0(b, SOUTH), v0(b, SOUTH));
            t.addVertexWithUV(b.x0, b.y1, b.z1, u1(b, SOUTH), v0(b, SOUTH));
        }
        if (b.uv[WEST] != null) {
            t.setNormal(NORMALS[WEST][0], NORMALS[WEST][1], NORMALS[WEST][2]);
            t.addVertexWithUV(b.x0, b.y0, b.z0, u1(b, WEST), v1(b, WEST));
            t.addVertexWithUV(b.x0, b.y0, b.z1, u0(b, WEST), v1(b, WEST));
            t.addVertexWithUV(b.x0, b.y1, b.z1, u0(b, WEST), v0(b, WEST));
            t.addVertexWithUV(b.x0, b.y1, b.z0, u1(b, WEST), v0(b, WEST));
        }
        if (b.uv[EAST] != null) {
            t.setNormal(NORMALS[EAST][0], NORMALS[EAST][1], NORMALS[EAST][2]);
            t.addVertexWithUV(b.x1, b.y0, b.z1, u1(b, EAST), v1(b, EAST));
            t.addVertexWithUV(b.x1, b.y0, b.z0, u0(b, EAST), v1(b, EAST));
            t.addVertexWithUV(b.x1, b.y1, b.z0, u0(b, EAST), v0(b, EAST));
            t.addVertexWithUV(b.x1, b.y1, b.z1, u1(b, EAST), v0(b, EAST));
        }
        if (b.uv[UP] != null) {
            t.setNormal(NORMALS[UP][0], NORMALS[UP][1], NORMALS[UP][2]);
            t.addVertexWithUV(b.x0, b.y1, b.z0, u0(b, UP), v1(b, UP));
            t.addVertexWithUV(b.x0, b.y1, b.z1, u0(b, UP), v0(b, UP));
            t.addVertexWithUV(b.x1, b.y1, b.z1, u1(b, UP), v0(b, UP));
            t.addVertexWithUV(b.x1, b.y1, b.z0, u1(b, UP), v1(b, UP));
        }
        if (b.uv[DOWN] != null) {
            t.setNormal(NORMALS[DOWN][0], NORMALS[DOWN][1], NORMALS[DOWN][2]);
            t.addVertexWithUV(b.x1, b.y0, b.z0, u1(b, DOWN), v0(b, DOWN));
            t.addVertexWithUV(b.x1, b.y0, b.z1, u1(b, DOWN), v1(b, DOWN));
            t.addVertexWithUV(b.x0, b.y0, b.z1, u0(b, DOWN), v1(b, DOWN));
            t.addVertexWithUV(b.x0, b.y0, b.z0, u0(b, DOWN), v0(b, DOWN));
        }
    }

    private static float u0(Box b, int face) {
        return b.uv[face][0];
    }

    private static float v0(Box b, int face) {
        return b.uv[face][1];
    }

    private static float u1(Box b, int face) {
        return b.uv[face][2];
    }

    private static float v1(Box b, int face) {
        return b.uv[face][3];
    }
}
