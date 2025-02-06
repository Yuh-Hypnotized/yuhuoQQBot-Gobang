package com.example;

import com.google.gson.Gson;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageRecallEvent.GroupRecall;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.message.data.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public final class Gobang extends JavaPlugin {
    public static final Gobang INSTANCE = new Gobang();

    private Gobang() {
        super(new JvmPluginDescriptionBuilder("com.example.gobang", "0.1.0")
                .name("Gobang")
                .author("Yuh_Hypnotized")

                .build());
    }

    public long player1 = -1;
    public long player2 = -1;
    public boolean gameFlag = false;
    public int current_player = 1;

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");

        Config_gobang config = loadConfig();
        if (config == null) {
            getLogger().error("Failed to load config file!");
            return;
        }

        gobang_board Board = new gobang_board();
        Board.initBoard();
        List<Long> whitelistedGroupID = config.whitelistedGroupID;

        Listener<GroupMessageEvent> listener = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                event -> {
            MessageChain message = event.getMessage();
            long groupID = event.getGroup().getId();
            long userID = event.getSender().getId();

            if (whitelistedGroupID.contains(groupID)) {
                String messageString = message.contentToString().trim();
                if (messageString.startsWith("/gob join")) {
                    if (gameFlag == true) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 已经有一个游戏了！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {  //gameFlag == false
                        if (userID == player1 || userID == player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 你已经上桌了！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (player1 != -1 && player2 != -1) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 已满员！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (player1 == -1 && player2 == -1) {
                            player1 = userID;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 成功上桌1号位！\n当前桌上：[").append(event.getGroup().get(userID).getNick())
                                    .append(", <空位>]").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (player1 != -1 && player2 == -1) {
                            player2 = userID;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 成功上桌2号位！\n当前桌上：[").append(event.getGroup().get(player1).getNick())
                                    .append(", ").append(event.getGroup().get(userID).getNick())
                                    .append("], 输入/gob start开始游戏！").build();
                            event.getGroup().sendMessage(chain);
                        }

                    }
                }
                else if (messageString.startsWith("/gob leave")) {
                    if (gameFlag == true) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 已经有一个游戏了！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != player1 && userID != player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 你不在桌上！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (userID == player1) {
                            player1 = player2;
                            player2 = -1;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 下桌成功！\n现在桌上：[").append(player1 == -1 ? "<空位>" :
                                            event.getGroup().get(player1).getNick()).append(", <空位>]").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (userID == player2) {
                            player2 = -1;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 下桌成功！\n现在桌上：[").append(event.getGroup().get(player1).getNick())
                                    .append(", <空位>]").build();
                            event.getGroup().sendMessage(chain);
                        }
                    }
                }
                else if (messageString.startsWith("/gob start")) {
                    if (gameFlag == true) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 已经有一个游戏了！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != player1 && userID != player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 只有桌上人才能开始游戏哦！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            if (player1 == -1 || player2 == -1) {
                                MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                        .append(" 人数不够！\n当前桌上：[").append(event.getGroup().get(player1).getNick())
                                        .append(", <空位>]").build();
                                event.getGroup().sendMessage(chain);
                            }
                            else {   // GAME START!!!
                                MessageChainBuilder builder = new MessageChainBuilder();
                                builder.append(new At(player1)).append(new At(player2)).append(" 游戏开始！\n")
                                        .append("玩家1：" + event.getGroup().get(player1).getNick() + "\n")
                                        .append("玩家2：" + event.getGroup().get(player2).getNick() + "\n");
                                MessageChain chain = builder.build();
                                event.getGroup().sendMessage(chain);

                                gameFlag = true;
                                try {
                                    File imageFile = gobang_board_renderer.generateChessBoardImage(Board.board);
                                    ExternalResource resource = ExternalResource.create(imageFile);
                                    Image image = event.getGroup().uploadImage(resource);
                                    resource.close();

                                    MessageChainBuilder builder1 = new MessageChainBuilder();
                                    builder1.append(image).append("当前棋局如上：黑棋-")
                                            .append(event.getGroup().get(player1).getNick()).append(", 白棋-")
                                            .append(event.getGroup().get(player2).getNick()).append("\n\n")
                                            .append("当前轮到").append(current_player == 1 ? new At(player1) : new At(player2));
                                    builder1.append("\n下棋方法：/gob play <行> <列>");
                                    MessageChain chain1 = builder1.build();
                                    event.getGroup().sendMessage(chain1);
                                }
                                catch (IOException e) {
                                    event.getGroup().sendMessage("生成棋盘失败：" + e.getMessage());
                                }
                            }
                        }
                    }
                }
                else if (messageString.startsWith("/gob play")) {
                    if (userID != player1 && userID != player2) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 你不在游戏中！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != (current_player == 1 ? player1 : player2)) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 当前不是你的回合！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            String[] commandParts = messageString.split("\\s+");
                            int row = Integer.parseInt(commandParts[2]);
                            int col = Integer.parseInt(commandParts[3]);
                            if (Board.board.get(row).get(col) == 0) { //empty?
                                Board.board.get(row).set(col, current_player);
                                MessageChainBuilder builder = new MessageChainBuilder();
                                builder.append(new At(userID)).append(" 在("+row+", "+col+")处落子：");
                                try {
                                    File imageFile = gobang_board_renderer.generateChessBoardImage(Board.board);
                                    ExternalResource resource = ExternalResource.create(imageFile);
                                    Image image = event.getGroup().uploadImage(resource);
                                    resource.close();

                                    builder.append(image).append("当前棋局如上：黑棋-")
                                            .append(event.getGroup().get(player1).getNick()).append(", 白棋-")
                                            .append(event.getGroup().get(player2).getNick());
                                    MessageChain chain = builder.build();
                                    event.getGroup().sendMessage(chain);

                                    if (Board.checkVcitory(current_player)) {
                                        MessageChain chain1 = new MessageChainBuilder().append("恭喜 ")
                                                .append(current_player == 1 ? new At(player1) : new At(player2))
                                                .append(" 战胜了 ").append(current_player == 1 ? new At(player2) : new At(player1))
                                                .append("\n获得本局五子棋胜利！").build();
                                        event.getGroup().sendMessage(chain1);
                                        gameFlag = false;
                                        player1 = -1;
                                        player2 = -1;
                                        current_player = 1;
                                        Board.clearBoard();
                                    }
                                    else {
                                        current_player = (current_player == 1 ? 2 : 1);
                                        MessageChain chain1 = new MessageChainBuilder().append("轮到 ")
                                                .append(current_player == 1 ? new At(player1) : new At(player2))
                                                .append(" 下棋了.").append("\n下棋方法：/gob play <行> <列>").build();
                                        event.getGroup().sendMessage(chain1);
                                    }
                                }
                                catch (IOException e) {
                                    event.getGroup().sendMessage("生成棋盘失败：" + e.getMessage());
                                }
                            }
                            else {  //not empty?
                                MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                        .append(" 此位置已经有棋了！请重新输入！").build();
                                event.getGroup().sendMessage(chain);
                            }
                        }
                    }
                }
                else if (messageString.startsWith("/gob ff")) {
                    if (gameFlag == true) {
                        if (userID != player1 && userID != player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 你不在游戏中！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 放弃了本局游戏！\n恭喜 ").append(new At(userID == player1 ?
                                            player2 : player1)).append(" 获得本局胜利！").build();
                            event.getGroup().sendMessage(chain);
                            gameFlag = false;
                            player1 = -1;
                            player2 = -1;
                            current_player = 1;
                            Board.clearBoard();
                        }
                    }
                    else {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 当前棋局未开始！").build();
                        event.getGroup().sendMessage(chain);
                    }

                }
                else if (messageString.startsWith("/gob status")) {
                    if (!gameFlag) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 当前棋局未开始\n桌上：[").append(player1 == -1 ? "<空位>, " :
                                        event.getGroup().get(player1).getNick()).append(", ")
                                .append(player2 == -1 ? "<空位>" : event.getGroup().get(player2).getNick())
                                .append("]").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 当前对局进行中：").append(event.getGroup().get(player1).getNick())
                                .append(" vs ").append(event.getGroup().get(player2).getNick()).build();
                        event.getGroup().sendMessage(chain);
                    }
                }
            }
                });

    }


    private Config_gobang loadConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        Config_gobang config;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config_gobang.json")) {
            if (inputStream == null) {
                getLogger().error("Config file does not exist!");
                return null;
            }
            config = objectMapper.readValue(inputStream, Config_gobang.class);
            return config;
        }
        catch (Exception e) {
            getLogger().error("Failed to load config file!", e);
            return null;
        }
    }
    private void updateConfig(Config_gobang newConfig) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            File file = new File("config_gobang.json");
            objectMapper.writeValue(file, newConfig);
        }
        catch (Exception e) {
            getLogger().error("Failed to save config file!", e);
        }
    }
}