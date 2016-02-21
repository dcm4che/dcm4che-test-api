package org.dcm4chee.archive.test.remoting;

import java.util.concurrent.Phaser;

/**
 * @author Roman K <roman.khazankin@gmail.com>
 */
public interface PhaserBean {

    Phaser get();
    void reset();

}
