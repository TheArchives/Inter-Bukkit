package com.archivesmc.inter.tasks;

import com.archivesmc.inter.Plugin;

public class HandlingTask implements Runnable {
    private Plugin plugin;

    public HandlingTask(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.handling.handleMessage();
    }
}
