package cn.buptleida.persistence;

import cn.buptleida.database.RedisServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AofHandler implements Runnable {
    private String[] command;

    public AofHandler(String[] cmd) {
        this.command = cmd;
    }

    @Override
    public void run() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(AOF.AOF_FILE_PATH), true));
            writer.write(AOF.catToAOFCommand(command));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
