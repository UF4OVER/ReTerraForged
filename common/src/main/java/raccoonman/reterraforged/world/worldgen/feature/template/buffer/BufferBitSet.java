package raccoonman.reterraforged.world.worldgen.feature.template.buffer;

import java.util.BitSet;

public class BufferBitSet {
    private int minX;
    private int minY;
    private int minZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int sizeXZ;
    private BitSet bitSet;

    public void set(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);

        // Inclusive bounds. If min==max we still need space for 1 element.
        this.sizeX = (Math.max(x1, x2) - this.minX) + 1;
        this.sizeY = (Math.max(y1, y2) - this.minY) + 1;
        this.sizeZ = (Math.max(z1, z2) - this.minZ) + 1;

        if (this.sizeX <= 0 || this.sizeY <= 0 || this.sizeZ <= 0) {
            // Defensive: invalid bounds, keep an empty mask to avoid crashing
            this.sizeX = this.sizeY = this.sizeZ = this.sizeXZ = 0;
            if (this.bitSet == null) {
                this.bitSet = new BitSet(0);
            } else {
                this.bitSet.clear();
            }
            return;
        }

        this.sizeXZ = this.sizeX * this.sizeZ;
        int size = this.sizeX * this.sizeY * this.sizeZ;

        // BitSet.length() is based on highest-set bit, not capacity.
        if (this.bitSet == null || this.bitSet.size() < size) {
            this.bitSet = new BitSet(size);
        } else {
            this.bitSet.clear();
        }
    }

    public void clear() {
        if (this.bitSet != null) {
            this.bitSet.clear();
        }
    }

    public void set(int x, int y, int z) {
        int index = this.indexOf(x - this.minX, y - this.minY, z - this.minZ);
        if (index >= 0) {
            this.bitSet.set(index);
        }
    }

    public void unset(int x, int y, int z) {
        int index = this.indexOf(x - this.minX, y - this.minY, z - this.minZ);
        if (index >= 0) {
            this.bitSet.set(index, false);
        }
    }

    public boolean test(int x, int y, int z) {
        int index = this.indexOf(x - this.minX, y - this.minY, z - this.minZ);
        if (index < 0 || this.bitSet == null || index >= this.bitSet.length()) {
            return false;
        }
        return this.bitSet.get(index);
    }

    private int indexOf(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) {
            return -1;
        }
        if (x >= this.sizeX || y >= this.sizeY || z >= this.sizeZ) {
            return -1;
        }
        return (y * this.sizeXZ) + (z * this.sizeX) + x;
    }
}
