package com.utils.splitApk;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-7-22.
 */
public class SplitApk {
    String type;
    String apkFile;
    String curFilePath;

    List<String> configs;

    File interfacesDir;
    File enCryptyEdDir;
    File apksDir;

    public SplitApk(String type, String apkFile) {
        this.type = type;
        this.apkFile = apkFile;
        this.curFilePath = new File("").getAbsolutePath();
        configs = new ArrayList<String>();

        interfacesDir = new File("interfaces");         //存放打包好的插件
        enCryptyEdDir = new File("enCryptyEdDir");     //存放加密好了的插件
        apksDir = new File("apks");                    //存放打包好的产品

        if (!interfacesDir.exists()) {
            interfacesDir.mkdir();
        }

        if (!enCryptyEdDir.exists()) {
            enCryptyEdDir.mkdir();
        }

        if (!apksDir.exists()) {
            apksDir.mkdir();
        }
    }


    public void mySplit() {

        //1: 获取txt文件中的内容
        setChannelConfig();
        //2:打apk渠道包
        decodeApk();

    }


    public void setChannelConfig() {
        File txt = new File("Config.txt");

        if (!txt.exists()) {
            System.out.println("Config.txt donot exists !");

            return;
        }

        FileReader fr = null;
        BufferedReader br = null;


        try {
            fr = new FileReader(txt);

            br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                if (line.equals("")) {
                    System.out.print("Config.txt have empty String ");

                    return;
                }
                configs.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void decodeApk() {
        String cmd = "cmd.exe /C java -jar apktool.jar d -f -s " + apkFile;

        runCmd(cmd);

        String[] paths = apkFile.split("\\\\");
        String n = paths[paths.length - 1];
        String fn = n.split(".apk")[0];

        File file = new File(fn);

        String p1 = file.getAbsolutePath() + "\\AndroidManifest.xml";
        String p2 = curFilePath + "\\AndroidManifest.xml";

        File f1 = new File(p1);
        File f2 = new File(p2);

        f1.renameTo(f2);

        for (int i = 0; i < 10; i++) {
            if (f2.exists()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        String line = null;

        for (String c : configs) {
            String[] configs = c.split("_");

            String channelId = configs[1];
            String interfaceName = configs[0];

            FileWriter fw = null;
            FileReader fr = null;
            BufferedReader br = null;
            try {
                fr = new FileReader(p2);
                br = new BufferedReader(fr);
                StringBuffer sb = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    if (line.contains("\"public\"")) {
                        line = line.replace("public", channelId);

                    }

                    sb.append(line + "\n");


                }

                fw = new FileWriter(p1);

                fw.write(sb.toString());


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (type.equals("1")) {
                //复制加密好的插件到产品包的assets路径下
                String enCryptyEdPath = enCryptyEdDir.getAbsolutePath() + File.separator + c;
                File enCryptyEdFile = new File(enCryptyEdPath);

                if (!enCryptyEdFile.exists()) {
                    System.out.print(c + "donot exists!");
                    return;
                }

                String assetsPath = file.getAbsolutePath() + File.separator + "assets" + File.separator + interfaceName;
                File assetsFile = new File(assetsPath);

                if (assetsFile.exists()) {
                    assetsFile.delete();
                }

                enCryptyEdFile.renameTo(assetsFile);
            }

            String unsignApk;

            if (type.equals("0")) {
                unsignApk = c + ".apk";
            } else {
                unsignApk = fn + "_" + channelId + ".apk";
            }
            String cmdPack = String.format("cmd.exe /C java -jar apktool.jar b %s %s", fn, unsignApk);

            runCmd(cmdPack);

            File apk1 = new File(unsignApk);       //重打包好的Apk

            String apk2Path;

            if (type.equals("0")) {
                apk2Path = interfacesDir.getAbsolutePath() + File.separator + unsignApk;
            } else {
                apk2Path = apksDir.getAbsolutePath() + File.separator + unsignApk;
            }

            File apk2 = new File(apk2Path);         //打包好的存放路径

            apk1.renameTo(apk2);

            /** 删除自动签名功能，改为手动签名
             // 签名
             String signApk = "./apk/" + c.source_id + "_sign.apk";
             String cmdKey = String
             .format("cmd.exe /C jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore %s -signedjar %s %s %s -storepass  %s",
             keyFile, signApk, unsignApk, keyFile, keyPasswd);
             runCmd(cmdKey);

             // 删除未签名的包
             File unApk = new File(unsignApk);
             unApk.delete();
             */

        }


        String cmdKey = String.format("cmd.exe /C rd /s/q %s", fn);
        runCmd(cmdKey);
        f2.delete();

    }

    public void runCmd(String cmd) {
        Runtime rt = Runtime.getRuntime();
        BufferedReader br = null;
        InputStreamReader isr = null;
        try {
            Process p = rt.exec(cmd);
            // p.waitFor();
            isr = new InputStreamReader(p.getInputStream());
            br = new BufferedReader(isr);
            String msg = null;
            while ((msg = br.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
