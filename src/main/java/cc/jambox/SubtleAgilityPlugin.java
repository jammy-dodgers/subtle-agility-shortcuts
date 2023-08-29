/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cc.jambox;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.AgilityShortcut;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.HashMap;
import java.util.Map;

import static net.runelite.api.Skill.AGILITY;

@Slf4j
@PluginDescriptor(
	name = "Subtle Agility Shortcuts"
)
public class SubtleAgilityPlugin extends Plugin
{
	@Getter
	private final Map<TileObject, Obstacle> obstacles = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private SubtleAgilityConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SubtleAgilityOverlay agilityOverlay;

	@Getter
	private int agilityLevel;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(agilityOverlay);
		agilityLevel = client.getBoostedSkillLevel(AGILITY);

	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(agilityOverlay);
		obstacles.clear();
		agilityLevel = 0;
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		if (statChanged.getSkill() != AGILITY)
		{
			return;
		}

		agilityLevel = statChanged.getBoostedLevel();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			obstacles.clear();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getGameObject(), null);
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getGroundObject(), null);
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getWallObject(), null);
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		onTileObject(event.getTile(), null, event.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		onTileObject(event.getTile(), event.getDecorativeObject(), null);
	}

	private void onTileObject(Tile tile, TileObject oldObject, TileObject newObject)
	{

		if (newObject == null)
		{
			return;
		}

		if (Obstacles.SHORTCUT_OBSTACLE_IDS.containsKey(newObject.getId()))
		{
			AgilityShortcut closestShortcut = null;
			int distance = -1;

			// Find the closest shortcut to this object
			for (AgilityShortcut shortcut : Obstacles.SHORTCUT_OBSTACLE_IDS.get(newObject.getId()))
			{
				if (!shortcut.matches(client, newObject))
				{
					continue;
				}

				if (shortcut.getWorldLocation() == null)
				{
					closestShortcut = shortcut;
					break;
				}
				else
				{
					int newDistance = shortcut.getWorldLocation().distanceTo2D(newObject.getWorldLocation());
					if (closestShortcut == null || newDistance < distance)
					{
						closestShortcut = shortcut;
						distance = newDistance;
					}
				}
			}

			if (closestShortcut != null)
			{
				obstacles.put(newObject, new Obstacle(tile, closestShortcut));
			}
		}
	}

	@Provides
	SubtleAgilityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SubtleAgilityConfig.class);
	}
}
