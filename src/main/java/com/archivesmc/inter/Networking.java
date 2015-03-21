package com.archivesmc.inter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Networking {
    private String apiKey;
    private String hostname;
    private Integer port;
    private Double version = 3D;

    private Plugin plugin;

    private Socket socket;

    private InputStreamReader input;
    private OutputStreamWriter output;

    private Gson gson = new Gson();
    private Type token = new TypeToken<Map<String, Object>>(){}.getType();
    
    private boolean stopping = false;

    public Networking(Plugin plugin, String apiKey, String hostname, Integer port) {
        this.apiKey = apiKey;
        this.plugin = plugin;
        this.hostname = hostname;
        this.port = port;
    }

    public boolean connect() {
        this.plugin.logger.info("Connecting..");

        try {
            this.socket = new Socket(this.hostname, this.port);

            this.input = new InputStreamReader(new BufferedInputStream(this.socket.getInputStream()));
            this.output = new OutputStreamWriter(new BufferedOutputStream(this.socket.getOutputStream()));
        } catch (UnknownHostException e) {
            return false;  // Should never happen anyway
        } catch (IOException e) {
            this.plugin.logger.severe("Unable to connect!");
            e.printStackTrace();

            return false;  // Unable to connect
        }

        return this.handshake();
    }

    boolean handshake() {
        Map<String, Object> data = this.gson.fromJson(this.readLine(), token);

        assert data != null;  // Should never happen

        Double version = (Double) data.get("version");

        if (!version.equals(this.version)) {
            if (version < this.version) {
                this.plugin.logger.warning("Server is running an old version of Inter.");
            } else {
                this.plugin.logger.warning("Server is running a newer version of Inter.");
            }

            this.plugin.logger.warning(String.format("Expected version %s, but got %s instead.", this.version, version));

            return false;
        }

        Map<String, Object> toSend = new HashMap<>();
        toSend.put("api_key", this.apiKey);

        try {
            this.sendLine(this.gson.toJson(toSend));
        } catch (IOException e) {
            this.plugin.logger.severe("Error while sending to socket!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void disconnect() {
        this.stopping = true;
        
        if (this.socket == null) {
            return;
        }

        if (!this.socket.isClosed()) {
            try {
                this.input.close();
                this.output.close();
                this.socket.close();
            } catch (IOException e) {
                this.plugin.logger.warning(String.format("Error while disconnecting: %s", e.getLocalizedMessage()));
            }
        }
    }

    public String readLine() {
        if (!this.socket.isClosed()) {
            StringBuilder buffer = new StringBuilder();

            while (true) {
                if (this.stopping) {
                    return "";
                }

                int int_value;
                char value;

                try {
                    int_value = this.input.read();

                    if (int_value < 0) {
                        return "";
                    }

                    value = (char) int_value;
                } catch (IOException e) {
                    this.plugin.logger.severe("Error while reading from socket!");
                    e.printStackTrace();
                    return null;
                }

                buffer.append(value);

                if (buffer.indexOf("\n") != -1) {
                    return buffer.toString();
                }
            }
        } else {
            return null;
        }
    }

    void sendLine(String data) throws IOException {
        this.output.write(data);
        this.output.write("\r\n");
        this.output.flush();
    }

    String toJson(Map<String, Object> data) {
        return this.gson.toJson(data);
    }

    public Map<String, Object> fromJson(String data) {
        return this.gson.fromJson(data, this.token);
    }

    /**
     * Specific sending methods for various things
     */

    public void sendChat(CommandSender commandSender, String message) {
        Map<String, Object> args;

        if (message.length() < 2) {
            args = new HashMap<>();
            args.put("message", message);

            commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&', Utils.formatString("getStringNotAMessage", args)
                    )
            );

            return;
        } else if (message.length() < 3 && message.charAt(0) == message.charAt(1)) {
            args = new HashMap<>();
            args.put("message", message);

            commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                            '&', Utils.formatString("getStringNotAMessage", args)
                    )
            );

            return;
        }


        Map<String, Object> data = new HashMap<>();
        data.put("action", "chat");
        data.put("message", message);

        this.plugin.addVariables(commandSender, data);

        try {
            this.sendLine(this.toJson(data));
        } catch (IOException e) {
            this.plugin.logger.warning("Error sending pong!");
            e.printStackTrace();
        }
    }

    public void sendPong(String timestamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("pong", timestamp);

        try {
            this.sendLine(this.toJson(data));
        } catch (IOException e) {
            this.plugin.logger.warning("Error sending pong!");
            e.printStackTrace();
        }
    }

    public void sendGetPlayers() {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "players");
        data.put("type", "list");

        try {
            this.sendLine(this.toJson(data));
        } catch (IOException e) {
            this.plugin.logger.warning("Error sending pong!");
            e.printStackTrace();
        }
    }

    public void sendPlayerConnect(OfflinePlayer player) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "players");
        data.put("type", "online");
        data.put("player", player.getName());

        try {
            this.sendLine(this.toJson(data));
        } catch (IOException e) {
            this.plugin.logger.warning("Error sending pong!");
            e.printStackTrace();
        }
    }

    public void sendplayerDisconnect(OfflinePlayer player) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "players");
        data.put("type", "offline");
        data.put("player", player.getName());

        try {
            this.sendLine(this.toJson(data));
        } catch (IOException e) {
            this.plugin.logger.warning("Error sending pong!");
            e.printStackTrace();
        }
    }
}
