package net.punchtree.util.particle;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleShapes {

    private static ParticleBuilder particleBuilder = new ParticleBuilder(Particle.REDSTONE).color(Color.WHITE);

    public static void setParticleBuilder(ParticleBuilder particleBuilder) {
        ParticleShapes.particleBuilder = particleBuilder;
    }

    public static ParticleBuilder getParticleBuilder() {
        return particleBuilder;
    }

    public static void drawQuad(Location aa, Location ab, Location ba, Location bb, int steps) {
        spawnParticleLine(aa, ab, steps);
        spawnParticleLine(ab, bb, steps);
        spawnParticleLine(aa, ba, steps);
        spawnParticleLine(ba, bb, steps);
    }

    public static void spawnParticleLine(Location a, Location b, int steps) {
        Vector difference = b.clone().toVector().subtract(a.toVector());
        difference.multiply(1d/(steps-1));
        // change <= vs < for endpoint
        for ( int i = 0; i < steps; ++i ) {
            Location l = a.clone().add(difference.clone().multiply(i));
            spawnParticle(l);
        }
    }

    public static void spawnParticle(Location location) {
        particleBuilder.location(location).spawn();
    }
}
