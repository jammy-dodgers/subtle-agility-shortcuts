package cc.jambox;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("subtleagility")
public interface SubtleAgilityConfig extends Config
{

	@ConfigItem(
			keyName = "useIcon", name = "Draw icon", description = "", position = 0)
	default boolean getUseIcon()
	{
		return true;
	}
	@ConfigItem(
			keyName = "useCircle", name = "Draw circle", description = "", position = 1)
	default boolean getUseCircle()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "overlayColor",
			name = "Overlay Color",
			description = "Color of shortcut overlay", position = 2
	)
	default Color getOverlayColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			keyName = "circleSize",
			name = "Circle size",
			description = "Size of shortcut indicator circle", position = 3
	)
	default int getCircleSize()
	{
		// about as big as the agility icon
		return 26;
	}
}
