package io.github.aquerr.eaglefactions.integrations.dynmap.util;

import io.github.aquerr.eaglefactions.api.entities.Claim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

/**
 * Util class for the Dynmap Integration. Contains some various functions just
 * to make code cleaner.
 *
 * @author Iterator
 *
 * Edited by Aquerr
 */

public class DynmapUtils {

    /**
     * WARNING: all code below is taken from Dynmap-Factions
     * https://github.com/webbukkit/Dynmap-Factions/
     */

    private static void floodFillTarget(TileFlags source, TileFlags destination, int x, int y)
    {
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { x, y });

        while (!stack.isEmpty())
        {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if (source.getFlag(x, y))
            { // Set in src
                source.setFlag(x, y, false); // Clear source
                destination.setFlag(x, y, true); // Set in destination
                if (source.getFlag(x + 1, y)) stack.push(new int[] { x + 1, y });
                if (source.getFlag(x - 1, y)) stack.push(new int[] { x - 1, y });
                if (source.getFlag(x, y + 1)) stack.push(new int[] { x, y + 1 });
                if (source.getFlag(x, y - 1)) stack.push(new int[] { x, y - 1 });
            }
        }
    }

    public static ArrayList<TempAreaMarker> createAreas(Set<Claim> chunks)
    {
        ArrayList<TempAreaMarker> ret = new ArrayList<>();

        // Loop through chunks: set flags on chunk map
        TileFlags allChunkFlags = new TileFlags();
        LinkedList<Claim> allChunks = new LinkedList<Claim>();
        for (Claim chunk : chunks)
        {
            allChunkFlags.setFlag(chunk.getChunkPosition().x(), chunk.getChunkPosition().z(), true); // Set flag for chunk
            allChunks.addLast(chunk);
        }

        // Loop through until we don't find more areas
        while (allChunks != null)
        {
            TileFlags ourChunkFlags = null;
            LinkedList<Claim> ourChunks = null;
            LinkedList<Claim> newChunks = null;

            int minimumX = Integer.MAX_VALUE;
            int minimumZ = Integer.MAX_VALUE;
            for (Claim chunk : allChunks)
            {
                int chunkX = chunk.getChunkPosition().x();
                int chunkZ = chunk.getChunkPosition().z();

                // If we need to start shape, and this block is not part of one yet
                if (ourChunkFlags == null && allChunkFlags.getFlag(chunkX, chunkZ))
                {
                    ourChunkFlags = new TileFlags(); // Create map for shape
                    ourChunks = new LinkedList<Claim>();
                    floodFillTarget(allChunkFlags, ourChunkFlags, chunkX, chunkZ); // Copy shape
                    ourChunks.add(chunk); // Add it to our chunk list
                    minimumX = chunkX;
                    minimumZ = chunkZ;
                }
                // If shape found, and we're in it, add to our node list
                else if (ourChunkFlags != null && ourChunkFlags.getFlag(chunkX, chunkZ))
                {
                    ourChunks.add(chunk);
                    if (chunkX < minimumX)
                    {
                        minimumX = chunkX;
                        minimumZ = chunkZ;
                    }
                    else if (chunkX == minimumX && chunkZ < minimumZ)
                    {
                        minimumZ = chunkZ;
                    }
                }
                // Else, keep it in the list for the next polygon
                else
                {
                    if (newChunks == null) newChunks = new LinkedList<>();
                    newChunks.add(chunk);
                }
            }

            // Replace list (null if no more to process)
            allChunks = newChunks;

            if (ourChunkFlags == null) continue;

            // Trace outline of blocks - start from minx, minz going to x+
            int initialX = minimumX;
            int initialZ = minimumZ;
            int currentX = minimumX;
            int currentZ = minimumZ;
            Direction direction = Direction.XPLUS;
            ArrayList<int[]> linelist = new ArrayList<>();
            linelist.add(new int[]{ initialX, initialZ }); // Add start point
            while ((currentX != initialX) || (currentZ != initialZ) || (direction != Direction.ZMINUS))
            {
                switch (direction)
                {
                    case XPLUS: // Segment in X+ direction
                        if (!ourChunkFlags.getFlag(currentX + 1, currentZ))
                        { // Right turn?
                            linelist.add(new int[]{ currentX + 1, currentZ }); // Finish line
                            direction = Direction.ZPLUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX + 1, currentZ - 1))
                        { // Straight?
                            currentX++;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[]{ currentX + 1, currentZ }); // Finish line
                            direction = Direction.ZMINUS;
                            currentX++;
                            currentZ--;
                        }
                        break;
                    case ZPLUS: // Segment in Z+ direction
                        if (!ourChunkFlags.getFlag(currentX, currentZ + 1))
                        { // Right turn?
                            linelist.add(new int[]{ currentX + 1, currentZ + 1 }); // Finish line
                            direction = Direction.XMINUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX + 1, currentZ + 1))
                        { // Straight?
                            currentZ++;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[]{ currentX + 1, currentZ + 1 }); // Finish line
                            direction = Direction.XPLUS;
                            currentX++;
                            currentZ++;
                        }
                        break;
                    case XMINUS: // Segment in X- direction
                        if (!ourChunkFlags.getFlag(currentX - 1, currentZ))
                        { // Right turn?
                            linelist.add(new int[]{ currentX, currentZ + 1 }); // Finish line
                            direction = Direction.ZMINUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX - 1, currentZ + 1))
                        { // Straight?
                            currentX--;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[] { currentX, currentZ + 1 }); // Finish line
                            direction = Direction.ZPLUS;
                            currentX--;
                            currentZ++;
                        }
                        break;
                    case ZMINUS: // Segment in Z- direction
                        if (!ourChunkFlags.getFlag(currentX, currentZ - 1))
                        { // Right turn?
                            linelist.add(new int[]{ currentX, currentZ }); // Finish line
                            direction = Direction.XPLUS; // Change direction
                        }
                        else if (!ourChunkFlags.getFlag(currentX - 1, currentZ - 1))
                        { // Straight?
                            currentZ--;
                        }
                        else
                        { // Left turn
                            linelist.add(new int[] { currentX, currentZ }); // Finish line
                            direction = Direction.XMINUS;
                            currentX--;
                            currentZ--;
                        }
                        break;
                }
            }

            int sz = linelist.size();
            double[] x = new double[sz];
            double[] z = new double[sz];
            for (int i = 0; i < sz; i++)
            {
                int[] line = linelist.get(i);
                x[i] = (double) line[0] * (double) 16;
                z[i] = (double) line[1] * (double) 16;
            }

            TempAreaMarker temp = new TempAreaMarker(x, z);

            ret.add(temp);
        }

        return ret;
    }

    enum Direction
    {
        XPLUS, ZPLUS, XMINUS, ZMINUS
    }
}
