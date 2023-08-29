package cc.jambox;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SubtleAgilityPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SubtleAgilityPlugin.class);
		RuneLite.main(args);
	}
}