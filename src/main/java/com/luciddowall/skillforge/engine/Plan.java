package com.luciddowall.skillforge.engine;

import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Immutable, compiled execution plan.
 * Runtime execution uses only primitive arrays + opcode switch.
 */
public final class Plan {
    public final String id;

    public final int[] opcodes;
    public final int[] nextPc;

    /** opcode -> index into argument arrays */
    public final int[] argIndex;

    public final float[] fArgs;
    public final int[] iArgs;

    public final Particle[] particles;
    public final Sound[] sounds;

    public Plan(String id,
                int[] opcodes,
                int[] nextPc,
                int[] argIndex,
                float[] fArgs,
                int[] iArgs,
                Particle[] particles,
                Sound[] sounds) {
        this.id = id;
        this.opcodes = opcodes;
        this.nextPc = nextPc;
        this.argIndex = argIndex;
        this.fArgs = fArgs;
        this.iArgs = iArgs;
        this.particles = particles;
        this.sounds = sounds;
    }
}
