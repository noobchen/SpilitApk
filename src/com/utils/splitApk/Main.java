package com.utils.splitApk;

/**
 * Created by Administrator on 14-7-22.
 */
public class Main {
    /**
     * args[0] type 批量打包类型，0批量打包插件，1批量打包产品包
     * args[1] apk包名称
     * @param args
     */
    public static void main(String[] args) {

        String type = args[0];
        String apkFile = args[1];

        SplitApk sp = new SplitApk(type,apkFile);


        sp.mySplit();




    }
}
