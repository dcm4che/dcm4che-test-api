package org.dcm4che.test.remote;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.Phaser;

/**
 * @author rawmahn
 */
@ApplicationScoped
public class PhaserProducer implements PhaserBean{

    private Phaser phaser;

    @PostConstruct
    public void init() {
        reset();
    }

    @Override
    public Phaser get() {
        return phaser;
    }

    @Override
    public void reset() {
        phaser = new Phaser();
    }
}
