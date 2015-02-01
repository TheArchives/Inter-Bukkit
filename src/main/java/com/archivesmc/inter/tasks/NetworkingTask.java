package com.archivesmc.inter.tasks;

import com.archivesmc.inter.Plugin;

public class NetworkingTask implements Runnable {
    private Plugin plugin;

    public NetworkingTask(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String data = this.plugin.networking.readLine();

        if (data != null && !data.isEmpty()) {
            try {
                this.plugin.handling.queueUp(this.plugin.networking.fromJson(data));
            } catch (Exception e) {
                this.plugin.logger.severe(String.format("Error parsing data: %s", e.getLocalizedMessage()));
                this.plugin.logger.info(String.format("Data: %s", data));
                e.printStackTrace();
            }
        }
    }
}
