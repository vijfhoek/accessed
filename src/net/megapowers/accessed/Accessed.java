package net.megapowers.accessed;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Accessed extends JavaPlugin {
	AccessedThread reloadedThread;
	
	@Override
	public void onDisable() {
		reloadedThread.running = false;
		Logger.getLogger("Minecraft").info("[Accessed] Waiting 2 seconds for the socket to close");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(7040);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		reloadedThread = new AccessedThread(serverSocket, this);
		reloadedThread.start();
		Logger.getLogger("Minecraft").info("[Accessed] Enabled and listening on port 7040");
	}

}
