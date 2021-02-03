package cn.buptleida.conf;

import java.io.*;
import java.util.HashMap;

public class CommandMapFactory {
    private static String CMD_TABLE_PATH;
    /**
     * 读取并生成commandTable
     * @return
     * @throws IOException
     */
    public static HashMap<String, Command> getCmdMap() throws IOException {
        File file = new File(CMD_TABLE_PATH);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashMap<String, Command> map = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] args = line.split(",");
            int flag = 0;
            for (char ch : args[3].toCharArray()) {
                flag |= flagMapping(ch);
            }
            int offset = Byte.parseByte(args[4]);
            Command cmd = new Command(args[0], args[1], Integer.parseInt(args[2]), flag,(byte)(1<<offset),
                    getClassArr(args[5]));
            map.put(args[0], cmd);
        }
        reader.close();
        return map;
    }

    private static int flagMapping(char ch) {
        switch (ch) {
            case 'r':
                return 0b1;
            case 'w':
                return 0b10;
        }
        return 0;
    }

    private static Class[] getClassArr(String classesStr){
        String[] strings = classesStr.split("_");
        if(strings.length==1&&strings[0].equals("null")) return null;
        Class[] classArr = new Class[strings.length];
        for(int i=0;i<strings.length;++i){
            classArr[i] = classMapping(strings[i]);
        }
        return classArr;
    }
    private static Class classMapping(String className){
        switch (className) {
            case "String":
                return String.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "boolean":
                return boolean.class;
        }
        return null;
    }

    public static void setCmdTablePath(String cmdTablePath) {
        CMD_TABLE_PATH = cmdTablePath;
    }
}
