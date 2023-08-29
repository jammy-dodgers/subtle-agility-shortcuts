/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Cas <https://github.com/casvandongen>
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

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.game.AgilityShortcut;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import javax.inject.Inject;

import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class SubtleAgilityOverlay extends Overlay {

    private static final int MAX_DISTANCE = 2350;
    private final Client client;
    private final SubtleAgilityPlugin plugin;
    private final SubtleAgilityConfig config;

    private final BufferedImage agilityIcon;

    @Inject
    private SubtleAgilityOverlay(Client client, SubtleAgilityPlugin plugin, SubtleAgilityConfig config, SkillIconManager skillicons)
    {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        agilityIcon = skillicons.getSkillImage(Skill.AGILITY);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
        Point mousePosition = client.getMouseCanvasPosition();

        plugin.getObstacles().forEach((object, obstacle) -> {
            Tile tile = obstacle.getTile();
            if (tile.getPlane() == client.getPlane()
                    && object.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE)
            {
                Shape objectClickbox = object.getClickbox();
                if (objectClickbox != null)
                {
                    AgilityShortcut agilityShortcut = obstacle.getShortcut();
                    if (agilityShortcut.getLevel() > plugin.getAgilityLevel()) {
                        return;
                    }
                    Color color = config.getOverlayColor();
                    if (objectClickbox.contains(mousePosition.getX(), mousePosition.getY()))
                    {
                        graphics.setColor(color.darker());
                    }
                    else
                    {
                        graphics.setColor(color);
                    }
                    Rectangle2D bounds = objectClickbox.getBounds2D();
                    int size = config.getCircleSize();
                    int offset = size / 2;
                    int heightOffGround = (int)bounds.getHeight() / 2;

                    if (config.getUseCircle()) {
                        Point ovalLocation = Perspective.localToCanvas(client, object.getLocalLocation(), object.getPlane(), heightOffGround);
                        graphics.drawOval(ovalLocation.getX() - offset, ovalLocation.getY() - offset, size, size);
                        graphics.setColor(ColorUtil.colorWithAlpha(color, color.getAlpha() / 3));
                        graphics.fillOval(ovalLocation.getX() - offset, ovalLocation.getY() - offset, size, size);
                    }
                    if (config.getUseIcon()) {
                        Point imageLocation = Perspective.getCanvasImageLocation(client, object.getLocalLocation(), agilityIcon, heightOffGround);
                        if (imageLocation != null) {
                            // Offset the agility icon slightly; 1 to the right and 1 up
                            graphics.drawImage(agilityIcon, imageLocation.getX() + 1, imageLocation.getY() - 1, null);
                        }
                    }
                }
            }

        });
        return null;
    }

}
