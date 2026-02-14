package net.cubespace.geSuit.configs;

import java.io.File;

/**
 * @author benjamincharlton on 26/08/2015.
 */
@SuppressWarnings("CanBeFinal")
public class LockDownConfig extends BaseConfig {

    public LockDownConfig() {
        super("lockdown");
    }

    protected LockDownConfig(File file) {
        super(file);
    }

    public String LockdownTime = "5m";
    public Boolean LockedDown = false; //if set to true the server will start lockedDown and will release in 5minutes
    public String StartupMsg = ""; //if set and no message is set when the lockdown is started this will be used
}
