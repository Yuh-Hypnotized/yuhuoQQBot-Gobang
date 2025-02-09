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

    public List<gobang_board> boardList;

    @Override
    public void onEnable() {
        getLogger().info("Plugin loaded!");

        Config_gobang config = loadConfig();
        if (config == null) {
            getLogger().error("Failed to load config file!");
            return;
        }

        boardList = new ArrayList<>();
        //gobang_board Board = new gobang_board();
        //Board.initBoard();
        List<Long> whitelistedGroupID = config.whitelistedGroupID;

        Listener<GroupMessageEvent> listener = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class,
                event -> {
            MessageChain message = event.getMessage();
            long groupID = event.getGroup().getId();
            long userID = event.getSender().getId();

            if (whitelistedGroupID.contains(groupID)) {

                String messageString = message.contentToString().trim();

                if (messageString.startsWith("/gob join")) {
                    if (findGroupBoard(groupID).gameFlag == true) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 已经有一个游戏了！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {  //gameFlag == false
                        if (userID == findGroupBoard(groupID).player1 || userID == findGroupBoard(groupID).player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 你已经上桌了！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (findGroupBoard(groupID).player1 != -1 && findGroupBoard(groupID).player2 != -1) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 已满员！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (findGroupBoard(groupID).player1 == -1 && findGroupBoard(groupID).player2 == -1) {
                            findGroupBoard(groupID).player1 = userID;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 成功上桌1号位！\n当前桌上：[").append(event.getGroup().get(userID).getNick())
                                    .append(", <空位>]").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (findGroupBoard(groupID).player1 != -1 && findGroupBoard(groupID).player2 == -1) {
                            findGroupBoard(groupID).player2 = userID;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 成功上桌2号位！\n当前桌上：[").append(event.getGroup()
                                            .get(findGroupBoard(groupID).player1).getNick())
                                    .append(", ").append(event.getGroup().get(userID).getNick())
                                    .append("], 输入/gob start开始游戏！").build();
                            event.getGroup().sendMessage(chain);
                        }

                    }
                }
                else if (messageString.startsWith("/gob leave")) {
                    if (findGroupBoard(groupID).gameFlag == true) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 已经有一个游戏了！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != findGroupBoard(groupID).player1 && userID != findGroupBoard(groupID).player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 你不在桌上！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (userID == findGroupBoard(groupID).player1) {
                            findGroupBoard(groupID).player1 = findGroupBoard(groupID).player2;
                            findGroupBoard(groupID).player2 = -1;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 下桌成功！\n现在桌上：[").append(findGroupBoard(groupID).player1 == -1 ?
                                            "<空位>" : event.getGroup().get(findGroupBoard(groupID).player1).
                                            getNick()).append(", <空位>]").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else if (userID == findGroupBoard(groupID).player2) {
                            findGroupBoard(groupID).player2 = -1;
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 下桌成功！\n现在桌上：[").append(event.getGroup()
                                            .get(findGroupBoard(groupID).player1).getNick())
                                    .append(", <空位>]").build();
                            event.getGroup().sendMessage(chain);
                        }
                    }
                }
                else if (messageString.startsWith("/gob start")) {
                    if (findGroupBoard(groupID).gameFlag == true) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 已经有一个游戏了！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != findGroupBoard(groupID).player1 && userID != findGroupBoard(groupID).player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 只有桌上人才能开始游戏哦！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            if (findGroupBoard(groupID).player1 == -1 || findGroupBoard(groupID).player2 == -1) {
                                MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                        .append(" 人数不够！\n当前桌上：[").append(event.getGroup()
                                                .get(findGroupBoard(groupID).player1).getNick())
                                        .append(", <空位>]").build();
                                event.getGroup().sendMessage(chain);
                            }
                            else {   // GAME START!!!
                                MessageChainBuilder builder = new MessageChainBuilder();
                                builder.append(new At(findGroupBoard(groupID).player1))
                                        .append(new At(findGroupBoard(groupID).player2)).append(" 游戏开始！\n")
                                        .append("玩家1：" + event.getGroup().get(findGroupBoard(groupID).player1)
                                                .getNick() + "\n").append("玩家2：" + event.getGroup()
                                                .get(findGroupBoard(groupID).player2).getNick() + "\n");
                                MessageChain chain = builder.build();
                                event.getGroup().sendMessage(chain);

                                findGroupBoard(groupID).gameFlag = true;
                                try {
                                    File imageFile = gobang_board_renderer
                                            .generateChessBoardImage(findGroupBoard(groupID).board);
                                    ExternalResource resource = ExternalResource.create(imageFile);
                                    Image image = event.getGroup().uploadImage(resource);
                                    resource.close();

                                    MessageChainBuilder builder1 = new MessageChainBuilder();
                                    builder1.append(image).append("当前棋局如上：黑棋-")
                                            .append(event.getGroup().get(findGroupBoard(groupID).player1).getNick())
                                            .append(", 白棋-").append(event.getGroup().get(findGroupBoard(groupID).player2)
                                                    .getNick()).append("\n\n").append("当前轮到")
                                            .append(findGroupBoard(groupID).current_player == 1 ?
                                                    new At(findGroupBoard(groupID).player1) :
                                                    new At(findGroupBoard(groupID).player2));
                                    builder1.append("\n下棋方法：/gob play <行> <列>\n或 /gp <行> <列>");
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
                    if (userID != findGroupBoard(groupID).player1 && userID != findGroupBoard(groupID).player2) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 你不在游戏中！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != (findGroupBoard(groupID).current_player == 1 ? findGroupBoard(groupID).player1 :
                                findGroupBoard(groupID).player2)) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 当前不是你的回合！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            String[] commandParts = messageString.split("\\s+");
                            int row = Integer.parseInt(commandParts[2]);
                            int col = Integer.parseInt(commandParts[3]);
                            if (findGroupBoard(groupID).board.get(row).get(col) == 0) { //empty?
                                findGroupBoard(groupID).board.get(row).set(col, findGroupBoard(groupID).current_player);
                                MessageChainBuilder builder = new MessageChainBuilder();
                                builder.append(new At(userID)).append(" 在("+row+", "+col+")处落子：");
                                try {
                                    File imageFile = gobang_board_renderer
                                            .generateChessBoardImage(findGroupBoard(groupID).board);
                                    ExternalResource resource = ExternalResource.create(imageFile);
                                    Image image = event.getGroup().uploadImage(resource);
                                    resource.close();

                                    builder.append(image).append("当前棋局如上：黑棋-")
                                            .append(event.getGroup().get(findGroupBoard(groupID).player1).getNick())
                                            .append(", 白棋-").append(event.getGroup().get(findGroupBoard(groupID).player2)
                                                    .getNick());
                                    MessageChain chain = builder.build();
                                    event.getGroup().sendMessage(chain);

                                    if (findGroupBoard(groupID).checkVcitory(findGroupBoard(groupID).current_player)) {
                                        long winner = findGroupBoard(groupID).current_player == 1 ?
                                                findGroupBoard(groupID).player1 :
                                                findGroupBoard(groupID).player2;
                                        MessageChain chain1 = new MessageChainBuilder().append("恭喜 ")
                                                .append(findGroupBoard(groupID).current_player == 1 ?
                                                        new At(findGroupBoard(groupID).player1) :
                                                        new At(findGroupBoard(groupID).player2))
                                                .append(" 战胜了 ").append(findGroupBoard(groupID).current_player == 1 ?
                                                        new At(findGroupBoard(groupID).player2) :
                                                        new At(findGroupBoard(groupID).player1))
                                                .append("\n获得本局五子棋胜利！积分+1！").build();
                                        event.getGroup().sendMessage(chain1);
                                        playerAddPoint(winner, config);
                                        addGameRecord(findGroupBoard(groupID).player1, findGroupBoard(groupID).player2,
                                                winner, config);
                                        updateConfig(config);
                                        findGroupBoard(groupID).gameFlag = false;
                                        findGroupBoard(groupID).player1 = -1;
                                        findGroupBoard(groupID).player2 = -1;
                                        findGroupBoard(groupID).current_player = 1;
                                        findGroupBoard(groupID).clearBoard();
                                    }
                                    else {
                                        findGroupBoard(groupID).current_player =
                                                (findGroupBoard(groupID).current_player == 1 ? 2 : 1);
                                        MessageChain chain1 = new MessageChainBuilder().append("轮到 ")
                                                .append(findGroupBoard(groupID).current_player == 1 ?
                                                        new At(findGroupBoard(groupID).player1) :
                                                        new At(findGroupBoard(groupID).player2)).append(" 下棋了.")
                                                .append("\n下棋方法：/gob play <行> <列>\n或 /gp <行> <列>").build();
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
                    if (findGroupBoard(groupID).gameFlag == true) {
                        if (userID != findGroupBoard(groupID).player1 && userID != findGroupBoard(groupID).player2) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 你不在游戏中！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            long winner = (userID == findGroupBoard(groupID).player1 ?
                                    findGroupBoard(groupID).player2 : findGroupBoard(groupID).player1);
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 放弃了本局游戏！\n恭喜 ").append(new At(userID == findGroupBoard(groupID).player1 ?
                                            findGroupBoard(groupID).player2 : findGroupBoard(groupID).player1))
                                    .append(" 获得本局胜利！积分+1！").build();
                            event.getGroup().sendMessage(chain);
                            playerAddPoint(winner, config);
                            addGameRecord(findGroupBoard(groupID).player1, findGroupBoard(groupID).player2,
                                    winner, config);
                            updateConfig(config);
                            findGroupBoard(groupID).gameFlag = false;
                            findGroupBoard(groupID).player1 = -1;
                            findGroupBoard(groupID).player2 = -1;
                            findGroupBoard(groupID).current_player = 1;
                            findGroupBoard(groupID).clearBoard();
                        }
                    }
                    else {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 当前棋局未开始！").build();
                        event.getGroup().sendMessage(chain);
                    }

                }
                else if (messageString.startsWith("/gob status")) {
                    if (!findGroupBoard(groupID).gameFlag) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 当前棋局未开始\n桌上：[").append(findGroupBoard(groupID).player1 == -1 ? "<空位>, " :
                                        event.getGroup().get(findGroupBoard(groupID).player1).getNick()).append(", ")
                                .append(findGroupBoard(groupID).player2 == -1 ? "<空位>" :
                                        event.getGroup().get(findGroupBoard(groupID).player2).getNick())
                                .append("]").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 当前对局进行中：").append(event.getGroup().get(findGroupBoard(groupID).player1).getNick())
                                .append(" vs ").append(event.getGroup().get(findGroupBoard(groupID).player2).getNick()).build();
                        event.getGroup().sendMessage(chain);
                    }
                }
                else if (messageString.startsWith("/gp")) {
                    if (userID != findGroupBoard(groupID).player1 && userID != findGroupBoard(groupID).player2) {
                        MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                .append(" 你不在游戏中！").build();
                        event.getGroup().sendMessage(chain);
                    }
                    else {
                        if (userID != (findGroupBoard(groupID).current_player == 1 ?
                                findGroupBoard(groupID).player1 : findGroupBoard(groupID).player2)) {
                            MessageChain chain = new MessageChainBuilder().append(new At(userID))
                                    .append(" 当前不是你的回合！").build();
                            event.getGroup().sendMessage(chain);
                        }
                        else {
                            String[] commandParts = messageString.split("\\s+");
                            int row = Integer.parseInt(commandParts[1]);
                            int col = Integer.parseInt(commandParts[2]);
                            if (findGroupBoard(groupID).board.get(row).get(col) == 0) { //empty?
                                findGroupBoard(groupID).board.get(row).set(col, findGroupBoard(groupID).current_player);
                                MessageChainBuilder builder = new MessageChainBuilder();
                                builder.append(new At(userID)).append(" 在("+row+", "+col+")处落子：");
                                try {
                                    File imageFile = gobang_board_renderer.
                                            generateChessBoardImage(findGroupBoard(groupID).board);
                                    ExternalResource resource = ExternalResource.create(imageFile);
                                    Image image = event.getGroup().uploadImage(resource);
                                    resource.close();

                                    builder.append(image).append("当前棋局如上：黑棋-")
                                            .append(event.getGroup().get(findGroupBoard(groupID).player1).getNick())
                                            .append(", 白棋-").append(event.getGroup().get(findGroupBoard(groupID).player2).getNick());
                                    MessageChain chain = builder.build();
                                    event.getGroup().sendMessage(chain);

                                    if (findGroupBoard(groupID).checkVcitory(findGroupBoard(groupID).current_player)) {
                                        long winner = findGroupBoard(groupID).current_player == 1 ?
                                                findGroupBoard(groupID).player1 :
                                                findGroupBoard(groupID).player2;
                                        MessageChain chain1 = new MessageChainBuilder().append("恭喜 ")
                                                .append(findGroupBoard(groupID).current_player == 1 ?
                                                        new At(findGroupBoard(groupID).player1) :
                                                        new At(findGroupBoard(groupID).player2))
                                                .append(" 战胜了 ").append(findGroupBoard(groupID).current_player == 1 ?
                                                        new At(findGroupBoard(groupID).player2) :
                                                        new At(findGroupBoard(groupID).player1))
                                                .append("\n获得本局五子棋胜利！积分+1！").build();
                                        event.getGroup().sendMessage(chain1);
                                        playerAddPoint(winner, config);
                                        addGameRecord(findGroupBoard(groupID).player1, findGroupBoard(groupID).player2,
                                                winner, config);
                                        updateConfig(config);
                                        findGroupBoard(groupID).gameFlag = false;
                                        findGroupBoard(groupID).player1 = -1;
                                        findGroupBoard(groupID).player2 = -1;
                                        findGroupBoard(groupID).current_player = 1;
                                        findGroupBoard(groupID).clearBoard();
                                    }
                                    else {
                                        findGroupBoard(groupID).current_player =
                                                (findGroupBoard(groupID).current_player == 1 ? 2 : 1);
                                        MessageChain chain1 = new MessageChainBuilder().append("轮到 ")
                                                .append(findGroupBoard(groupID).current_player == 1 ?
                                                        new At(findGroupBoard(groupID).player1) :
                                                        new At(findGroupBoard(groupID).player2)).append(" 下棋了.")
                                                .append("\n下棋方法：/gob play <行> <列>\n或 /gp <行> <列>").build();
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
                else if (messageString.startsWith("/gob profile")) {
                    MessageChainBuilder builder = new MessageChainBuilder();
                    builder.append("---").append(new At(userID)).append(" 的五子棋个人资料---\n\n");
                    builder.append("昵称：").append(event.getGroup().get(userID).getNick()).append("\n");
                    builder.append("QQ账号：").append(userID + "\n");
                    builder.append("五子棋积分：").append(getPlayerPoint(userID, config) + "\n\n");
                    builder.append("近 5 局战绩：\n");
                    int count = 0;
                    for (int index = config.gobang_record.size() - 1; index >= 0; index--) {
                        long player1 = config.gobang_record.get(index).player1;
                        long player2 = config.gobang_record.get(index).player2;
                        long winner = config.gobang_record.get(index).winner;
                        if (userID == player1 || userID == player2) {
                            builder.append((event.getGroup().get(player1) == null ? "<非本群成员>" :
                                    event.getGroup().get(player1).getNick())).append(" vs ");
                            builder.append((event.getGroup().get(player2) == null ? "<非本群成员>" :
                                    event.getGroup().get(player2).getNick()));
                            builder.append(" --- ");
                            builder.append((userID == winner ? "胜\n" : "负\n"));
                            count++;
                        }
                        if (count >= 5) {
                            break;
                        }
                    }
                    while (count < 5) {
                        builder.append("<暂无记录>\n");
                        count++;
                    }
                    MessageChain chain = builder.build();
                    event.getGroup().sendMessage(chain);
                }
                else if (messageString.startsWith("/gob lb")) {
                    List<user_gobang> lb = config.gobang_leaderboard;
                    Collections.sort(lb, (a, b) -> Integer.compare(b.userPoints, a.userPoints));
                    MessageChainBuilder builder = new MessageChainBuilder();
                    builder.append("本群五子棋积分排行榜：\n").build();
                    int rank = 1;
                    for (int i=0; i<lb.size(); i++) {
                        if (event.getGroup().get(lb.get(i).userID) != null) {
                            builder.append(new PlainText((rank++) + ". "))
                                    .append(event.getGroup().get(lb.get(i).userID).getNick())
                                    .append(new PlainText("(" + lb.get(i).userID + ") - "
                                            + lb.get(i).userPoints + "分\n"));
                        }
                    }
                    MessageChain chain = builder.build();
                    event.getGroup().sendMessage(chain);
                }
            }
                });

    }

    private gobang_board findGroupBoard (long groupID) {
        if (boardList.size() > 0) {
            for (gobang_board board : boardList) {
                if (board.groupID == groupID) {
                    return board;
                }
            }
        }
        gobang_board newBoard = new gobang_board();
        newBoard.initBoard(groupID);
        boardList.add(newBoard);
        return newBoard;
    }

    private int playerAddPoint(long userID, Config_gobang configGobang) {
        if (configGobang.gobang_leaderboard.size() > 0) {
            for (user_gobang user : configGobang.gobang_leaderboard) {
                if (user.userID == userID) {
                    return ++user.userPoints;
                }
            }
        }
        configGobang.gobang_leaderboard.add(new user_gobang(userID, 1));
        return 1;
    }

    private int getPlayerPoint(long userID, Config_gobang configGobang) {
        if (configGobang.gobang_leaderboard.size() > 0) {
            for (user_gobang user : configGobang.gobang_leaderboard) {
                if (user.userID == userID) {
                    return user.userPoints;
                }
            }
        }
        return 0;
    }

    private void addGameRecord(long player1, long player2, long winner, Config_gobang configGobang) {
        gameRecord record = new gameRecord(player1,player2,winner);
        configGobang.gobang_record.add(record);
    }

    private Config_gobang loadConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        Config_gobang config;
        try {
            File file = new File("E:\\overflow-1.0.3\\config_gobang.json");
            config = objectMapper.readValue(file, Config_gobang.class);
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