package cn.buptleida.dataCoreObj.underObj;

public class zlentry {
    //前置節點長度
    int prevrawlen;
    //記錄前置節點長度的字節數
    int prevrawlensize;

    //编码值
    int encoding;
    //編碼encoding的字节数
    int encodingSize;

    //頭部長度
    int headerSize;

    //content的起始位置
    int contentPos;
    //content的字節數
    int contentSize;

    public int size() {
        return headerSize + contentSize;
    }
    public int startPos(){
        return contentPos - headerSize;
    }
    public int endPos(){
        return contentPos + contentSize;
    }
}
