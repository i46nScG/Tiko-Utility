package com.tokioshy;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TikoUtility implements ModInitializer {
	public static final String MOD_ID = "tiko-utility";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Set<UUID> AFK_PLAYERS = new HashSet<>();
	private static final Map<UUID, Vec3> LAST_POSITIONS = new HashMap<>();
	private static final String AFK_TEAM = "tiko_utility_afk";

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			dispatcher.register(Commands.literal("afk")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						UUID playerId = player.getUUID();

						if (AFK_PLAYERS.remove(playerId)) {
							removeAfkPrefix(player);
							LAST_POSITIONS.remove(playerId);
							player.sendSystemMessage(Component.literal("You are no longer AFK."));
						} else {
							AFK_PLAYERS.add(playerId);
							LAST_POSITIONS.put(playerId, player.position());
							applyAfkPrefix(player);
							player.sendSystemMessage(Component.literal("You are now AFK."));
						}

						return Command.SINGLE_SUCCESS;
					}));

			dispatcher.register(Commands.literal("trash")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						SimpleContainer trashContainer = new SimpleContainer(27);

						player.openMenu(new SimpleMenuProvider(
								(syncId, inventory, menuPlayer) -> new ChestMenu(
										MenuType.GENERIC_9x3,
										syncId,
										inventory,
										trashContainer,
										3) {
									@Override
									public void removed(Player closingPlayer) {
										trashContainer.clearContent();
										super.removed(closingPlayer);
									}
								},
								Component.literal("Trash")));

						return Command.SINGLE_SUCCESS;
					}));

			dispatcher.register(Commands.literal("heal")
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						player.setHealth(player.getMaxHealth());
						player.sendSystemMessage(Component.literal("You have been fully healed."));
						return Command.SINGLE_SUCCESS;
					}));

			dispatcher.register(Commands.literal("feed")
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						player.getFoodData().setFoodLevel(20);
						player.getFoodData().setSaturation(20.0f);
						player.sendSystemMessage(Component.literal("Your hunger has been restored."));
						return Command.SINGLE_SUCCESS;
					}));

			dispatcher.register(Commands.literal("ping")
					.then(Commands.argument("target", EntityArgument.player())
							.executes(context -> {
								ServerPlayer sender = context.getSource().getPlayerOrException();
								ServerPlayer target = EntityArgument.getPlayer(context, "target");

								if (sender.getUUID().equals(target.getUUID())) {
									sender.sendSystemMessage(Component.literal("You cannot ping yourself."));
									return 0;
								}

								target.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
								target.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§ePING!")));
								target.connection.send(new ClientboundSetSubtitleTextPacket(
										Component.literal("§f" + sender.getScoreboardName() + " §7just pinged you!")));

								sender.sendSystemMessage(
										Component.literal("You pinged " + target.getScoreboardName() + "."));
								return Command.SINGLE_SUCCESS;
							})));
		});

		ServerTickEvents.END_SERVER_TICK.register(TikoUtility::checkMovement);

		LOGGER.info("Tiko Utility initialized.");
	}

	private static void checkMovement(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			UUID playerId = player.getUUID();
			Vec3 currentPosition = player.position();
			Vec3 lastPosition = LAST_POSITIONS.put(playerId, currentPosition);

			if (!AFK_PLAYERS.contains(playerId) || lastPosition == null) {
				continue;
			}

			if (currentPosition.distanceToSqr(lastPosition) > 0.0001) {
				AFK_PLAYERS.remove(playerId);
				removeAfkPrefix(player);
				player.sendSystemMessage(Component.literal("You are no longer AFK."));
			}
		}
	}

	private static void applyAfkPrefix(ServerPlayer player) {
		Scoreboard scoreboard = player.level().getScoreboard();
		PlayerTeam team = scoreboard.getPlayerTeam(AFK_TEAM);

		if (team == null) {
			team = scoreboard.addPlayerTeam(AFK_TEAM);
			team.setPlayerPrefix(Component.literal("[AFK] "));
		}

		scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
	}

	private static void removeAfkPrefix(ServerPlayer player) {
		Scoreboard scoreboard = player.level().getScoreboard();
		PlayerTeam team = scoreboard.getPlayerTeam(AFK_TEAM);

		if (team != null) {
			scoreboard.removePlayerFromTeam(player.getScoreboardName(), team);
		}
	}

	public static boolean isAfk(UUID playerId) {
		return AFK_PLAYERS.contains(playerId);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}