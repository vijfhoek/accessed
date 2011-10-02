package net.megapowers.accessed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class AccessedThread extends Thread {
	ServerSocket serverSocket;
	Socket clientSocket;
	
	PluginManager pluginManager;
	Server server;
	Plugin plugin;
	Configuration config;
	
	public boolean running;
	
	BufferedReader reader;
	PrintWriter writer;
	
	public AccessedThread(ServerSocket serverSocket, JavaPlugin plugin) {
		this.serverSocket = serverSocket;
		try {
			serverSocket.setSoTimeout(2000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		this.plugin = plugin;
		this.server = plugin.getServer();
		this.config = plugin.getConfiguration();
		this.pluginManager = server.getPluginManager();
		
		running = true;		
	}
	
	public void run() {
		while (running) {
			try {
				clientSocket = serverSocket.accept();
				String hostAddress = clientSocket.getInetAddress().getHostAddress();
				if (!hostAddress.equals("127.0.0.1") && !hostAddress.equals("0:0:0:0:0:0:0:1")) {
					Logger.getLogger("Minecraft").info("[Accessed] " + clientSocket.getInetAddress().getHostAddress() + " tried accessing, but was denied.");
					clientSocket.close();
					continue;
				}
				reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer = new PrintWriter(clientSocket.getOutputStream(), true);
				
				String inputLine;
				String[] inputLineSplit;
				
				
				while ((inputLine = reader.readLine()) != null) {
					inputLineSplit = inputLine.split(" ");
					
					if (inputLineSplit[0].equalsIgnoreCase("reload")) {
						try {
							Plugin pluginToReload = pluginManager.getPlugin(inputLineSplit[1]);
							pluginManager.disablePlugin(pluginToReload);
							pluginManager.enablePlugin(pluginToReload);
							writer.println("done");
						} catch (Exception ex) {
							writer.println("failed");
						}
					} else if (inputLineSplit[0].equalsIgnoreCase("reloadall")) {
						if (reloadAll()) {
							writer.println("done");
						} else {
							writer.println("failed");
						}
					} else if (inputLineSplit[0].equalsIgnoreCase("command")) {
						try {
							String commandToSend = inputLine.substring(inputLineSplit[0].length() + 1);
							if (commandToSend.equals("reload")) {
								if (reloadAll()) {
									writer.println("done");
								} else {
									writer.println("failed");
								}
							} else {
								Logger.getLogger("Minecraft").info("[Accessed] Dispatching command \"" + commandToSend + "\" to console.");
								server.dispatchCommand(new ConsoleCommandSender(server), commandToSend);
								writer.println("done");
							}
						} catch (Exception ex) {
							writer.println("failed");
						} 
					} else {
						writer.println("unknown");
					}
				}
			} catch (IOException e) {
			}
		}
		try {
			if (serverSocket != null) serverSocket.close();
		} catch (IOException e) {

		}
	}
	
	boolean reloadAll() {
		try {
			for (Plugin plugin : pluginManager.getPlugins()) {
				if (plugin.getDescription().getName().equals("Accessed")) { continue; }
				pluginManager.disablePlugin(plugin);
			}
			for (Plugin plugin : pluginManager.getPlugins()) {
				if (plugin.getDescription().getName().equals("Accessed")) { continue; }
				pluginManager.enablePlugin(plugin);
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
}
