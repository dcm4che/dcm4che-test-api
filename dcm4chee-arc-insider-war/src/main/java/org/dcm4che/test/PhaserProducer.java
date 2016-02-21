package org.dcm4che.test;

import org.dcm4chee.archive.test.remoting.PhaserBean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.Phaser;

/**
 * @author Roman K <roman.khazankin@gmail.com>
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
