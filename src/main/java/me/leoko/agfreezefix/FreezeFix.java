package me.leoko.agfreezefix;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FreezeFix extends Plugin implements Listener {

    private final String CHANNEL = "advancedgui:fix";
    private final Map<String, Integer> nextIds = new HashMap<>();

    @Override
    public void onEnable() {
        getProxy().registerChannel(CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        System.out.println("Enabled bungee freeze fix for AG");
    }

    public void sendMessage(Server server, String name, int nextId){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( name );
        out.writeInt( nextId );

        server.getInfo().sendData( CHANNEL, out.toByteArray() );
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event){
        if ( !event.getTag().equalsIgnoreCase( CHANNEL ) )
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput( event.getData() );
        String name = in.readUTF();
        int nextId = in.readInt();

        nextIds.put(name, nextId);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        nextIds.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        getProxy().getScheduler().schedule(this, () -> {
            String name = event.getPlayer().getName();
            Integer nextId = nextIds.get(name);
            if(nextId != null) sendMessage(event.getPlayer().getServer(), name, nextId);
        }, 1, TimeUnit.SECONDS);
    }
}
